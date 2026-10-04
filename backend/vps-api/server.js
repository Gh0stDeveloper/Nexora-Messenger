'use strict';

const crypto = require('node:crypto');
const fs = require('node:fs');
const path = require('node:path');
const express = require('express');
const multer = require('multer');
const admin = require('firebase-admin');
const { Pool } = require('pg');

const PORT = Number(process.env.PORT || 8080);
const UPLOAD_DIR = process.env.UPLOAD_DIR || path.join(process.cwd(), 'uploads');
const AVATAR_DIR = path.join(UPLOAD_DIR, 'avatars');
const MEDIA_DIR = path.join(UPLOAD_DIR, 'encrypted-media');
const PUBLIC_BASE_URL = (process.env.APP_PUBLIC_BASE_URL || '').replace(/\/$/, '');
const MAX_AVATAR_BYTES = Number(process.env.MAX_AVATAR_BYTES || 2 * 1024 * 1024);
const MAX_MEDIA_BYTES = Number(process.env.MAX_MEDIA_BYTES || 25 * 1024 * 1024);
const MAX_CIPHERTEXT_BYTES = Number(process.env.MAX_CIPHERTEXT_BYTES || 512 * 1024);
const PROTOCOL_VERSION = 'nexora-e2ee-v1';

fs.mkdirSync(AVATAR_DIR, { recursive: true });
fs.mkdirSync(MEDIA_DIR, { recursive: true });

function parseFirebaseCredential() {
  if (process.env.FIREBASE_SERVICE_ACCOUNT_JSON) return JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_JSON);
  if (process.env.FIREBASE_SERVICE_ACCOUNT_BASE64) return JSON.parse(Buffer.from(process.env.FIREBASE_SERVICE_ACCOUNT_BASE64, 'base64').toString('utf8'));
  return null;
}

if (!admin.apps.length) {
  const serviceAccount = parseFirebaseCredential();
  admin.initializeApp(serviceAccount ? { credential: admin.credential.cert(serviceAccount) } : {});
}

const pool = new Pool({ connectionString: process.env.DATABASE_URL, max: Number(process.env.PG_POOL_MAX || 10) });

async function migrate() {
  await pool.query(`
CREATE TABLE IF NOT EXISTS users (
  uid TEXT PRIMARY KEY,
  phone TEXT NOT NULL DEFAULT '',
  name TEXT NOT NULL DEFAULT '',
  photo_url TEXT,
  status TEXT NOT NULL DEFAULT 'Disponible',
  public_key TEXT,
  fcm_token TEXT,
  fcm_platform TEXT NOT NULL DEFAULT 'android',
  profile_completed BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  last_seen TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS contacts (
  owner_uid TEXT NOT NULL,
  contact_uid TEXT NOT NULL,
  alias TEXT NOT NULL DEFAULT '',
  favorite BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY(owner_uid, contact_uid)
);
CREATE TABLE IF NOT EXISTS groups (
  group_id TEXT PRIMARY KEY,
  title TEXT NOT NULL,
  owner_uid TEXT NOT NULL,
  photo_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS group_members (
  group_id TEXT NOT NULL REFERENCES groups(group_id) ON DELETE CASCADE,
  uid TEXT NOT NULL,
  role TEXT NOT NULL DEFAULT 'member',
  joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY(group_id, uid)
);
CREATE TABLE IF NOT EXISTS chats (
  chat_id TEXT PRIMARY KEY,
  participants TEXT[] NOT NULL,
  is_group BOOLEAN NOT NULL DEFAULT FALSE,
  title TEXT,
  photo_url TEXT,
  last_message_preview TEXT NOT NULL DEFAULT 'Mensaje cifrado',
  last_message_epoch_ms BIGINT NOT NULL DEFAULT 0,
  last_message_sender_id TEXT NOT NULL DEFAULT '',
  protocol_version TEXT NOT NULL DEFAULT '${PROTOCOL_VERSION}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE IF NOT EXISTS messages (
  chat_id TEXT NOT NULL REFERENCES chats(chat_id) ON DELETE CASCADE,
  message_id TEXT NOT NULL,
  sender_id TEXT NOT NULL,
  recipient_id TEXT NOT NULL,
  kind TEXT NOT NULL,
  encrypted_payload TEXT NOT NULL,
  iv TEXT NOT NULL,
  timestamp_ms BIGINT NOT NULL,
  media_url TEXT,
  media_mime TEXT,
  media_name TEXT,
  delivered BOOLEAN NOT NULL DEFAULT FALSE,
  read BOOLEAN NOT NULL DEFAULT FALSE,
  protocol_version TEXT NOT NULL DEFAULT '${PROTOCOL_VERSION}',
  server_received_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (chat_id, message_id)
);
CREATE TABLE IF NOT EXISTS statuses (
  status_id TEXT PRIMARY KEY,
  owner_uid TEXT NOT NULL,
  kind TEXT NOT NULL,
  encrypted_payload TEXT NOT NULL,
  iv TEXT NOT NULL,
  media_url TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  expires_at TIMESTAMPTZ NOT NULL DEFAULT NOW() + INTERVAL '24 hours'
);
CREATE INDEX IF NOT EXISTS idx_chats_participants ON chats USING GIN(participants);
CREATE INDEX IF NOT EXISTS idx_messages_recipient ON messages(recipient_id, timestamp_ms DESC);
CREATE INDEX IF NOT EXISTS idx_messages_chat_timestamp ON messages(chat_id, timestamp_ms ASC);
CREATE INDEX IF NOT EXISTS idx_group_members_uid ON group_members(uid);
CREATE INDEX IF NOT EXISTS idx_statuses_owner ON statuses(owner_uid, created_at DESC);
`);
}

function appBaseUrl(req) { return PUBLIC_BASE_URL || `${req.protocol}://${req.get('host')}`; }
function canonicalChatId(a, b) { return [String(a), String(b)].sort().join('_'); }
function safe(value, max = 512) { return String(value || '').trim().slice(0, max); }
function problem(res, status, error, details) { return res.status(status).json({ ok: false, error, details }); }

async function authRequired(req, res, next) {
  const header = req.headers.authorization || '';
  const [scheme, token] = header.split(' ');
  if (scheme !== 'Bearer' || !token) return problem(res, 401, 'missing_bearer_token');
  try {
    req.user = await admin.auth().verifyIdToken(token, true);
    return next();
  } catch (error) {
    return problem(res, 401, 'invalid_token', error.message);
  }
}

async function notifyRecipient(recipientId, payload) {
  const { rows } = await pool.query('SELECT fcm_token FROM users WHERE uid=$1', [recipientId]);
  const token = rows[0]?.fcm_token;
  if (!token) return;
  try {
    await admin.messaging().send({
      token,
      notification: { title: 'Nexora', body: 'Tienes un mensaje cifrado nuevo' },
      data: { chatId: payload.chatId, messageId: payload.messageId, senderId: payload.senderId, kind: payload.kind },
      android: { priority: 'high', notification: { channelId: 'nexora_messages', tag: payload.chatId } },
    });
  } catch (error) {
    console.warn('[nexora-fcm] notification_failed', { recipientId, code: error.code, message: error.message });
    if (error.code === 'messaging/registration-token-not-registered' || error.code === 'messaging/invalid-registration-token') {
      await pool.query('UPDATE users SET fcm_token=NULL, updated_at=NOW() WHERE uid=$1', [recipientId]);
    }
  }
}

function diskStorage(dir, prefix) {
  return multer.diskStorage({
    destination: (_req, _file, cb) => cb(null, dir),
    filename: (req, file, cb) => {
      const ext = path.extname(file.originalname || '').slice(0, 12) || '.bin';
      cb(null, `${prefix}-${req.user.uid}-${Date.now()}-${crypto.randomBytes(6).toString('hex')}${ext}`);
    },
  });
}

const avatarUpload = multer({
  storage: diskStorage(AVATAR_DIR, 'avatar'),
  limits: { fileSize: MAX_AVATAR_BYTES, files: 1 },
  fileFilter: (_req, file, cb) => cb(['image/jpeg', 'image/png', 'image/webp'].includes(file.mimetype) ? null : new Error('invalid_avatar_type'), true),
});
const mediaUpload = multer({ storage: diskStorage(MEDIA_DIR, 'media'), limits: { fileSize: MAX_MEDIA_BYTES, files: 1 } });

const app = express();
app.use(express.json({ limit: '1mb' }));
app.use('/uploads', express.static(UPLOAD_DIR, { immutable: true, maxAge: '30d' }));

app.get('/health', (_req, res) => res.json({ ok: true, service: 'nexora-vps-relay-api' }));
app.get('/ready', async (_req, res) => { await pool.query('SELECT 1'); res.json({ ok: true }); });
app.get('/v1/relay/version', (_req, res) => res.json({ ok: true, app: 'Nexora', protocolVersion: PROTOCOL_VERSION }));

app.post('/v1/relay/users/me/bootstrap', authRequired, async (req, res) => {
  const uid = req.user.uid;
  const name = safe(req.body.name, 80);
  const phone = safe(req.body.phone || req.user.phone_number, 32);
  const photoUrl = safe(req.body.photoUrl, 2048) || null;
  const publicKey = safe(req.body.publicKey, 4096) || null;
  const fcmToken = safe(req.body.fcmToken, 4096) || null;
  await pool.query(
    `INSERT INTO users(uid, phone, name, photo_url, public_key, fcm_token, profile_completed, updated_at, last_seen)
     VALUES($1,$2,$3,$4,$5,$6,$7,NOW(),NOW())
     ON CONFLICT(uid) DO UPDATE SET phone=COALESCE(NULLIF($2,''), users.phone), name=COALESCE(NULLIF($3,''), users.name), photo_url=COALESCE($4, users.photo_url), public_key=COALESCE($5, users.public_key), fcm_token=COALESCE($6, users.fcm_token), profile_completed=(users.profile_completed OR $7), updated_at=NOW(), last_seen=NOW()`,
    [uid, phone, name, photoUrl, publicKey, fcmToken, Boolean(name)]
  );
  res.json({ ok: true, uid });
});

app.post('/v1/relay/users/me/fcm-token', authRequired, async (req, res) => {
  const token = safe(req.body.token, 4096);
  if (!token) return problem(res, 400, 'missing_fcm_token');
  await pool.query(`UPDATE users SET fcm_token=$2, fcm_platform='android', updated_at=NOW() WHERE uid=$1`, [req.user.uid, token]);
  res.json({ ok: true });
});

app.post('/v1/relay/users/me/avatar', authRequired, avatarUpload.single('avatar'), async (req, res) => {
  if (!req.file) return problem(res, 400, 'missing_avatar');
  const photoUrl = `${appBaseUrl(req).replace(/\/$/, '')}/uploads/avatars/${req.file.filename}`;
  await pool.query(`UPDATE users SET photo_url=$2, updated_at=NOW() WHERE uid=$1`, [req.user.uid, photoUrl]);
  res.json({ ok: true, photoUrl });
});

app.get('/v1/relay/contacts', authRequired, async (req, res) => {
  const { rows } = await pool.query(
    `SELECT u.uid, u.phone, COALESCE(NULLIF(c.alias,''), u.name) AS name, u.photo_url, u.status, c.favorite
     FROM contacts c JOIN users u ON u.uid=c.contact_uid WHERE c.owner_uid=$1 ORDER BY c.favorite DESC, name ASC`,
    [req.user.uid]
  );
  res.json({ ok: true, contacts: rows });
});

app.post('/v1/relay/contacts', authRequired, async (req, res) => {
  const contactUid = safe(req.body.contactUid, 128);
  const alias = safe(req.body.alias, 80);
  if (!contactUid || contactUid === req.user.uid) return problem(res, 400, 'invalid_contact');
  await pool.query(
    `INSERT INTO contacts(owner_uid, contact_uid, alias, updated_at) VALUES($1,$2,$3,NOW())
     ON CONFLICT(owner_uid, contact_uid) DO UPDATE SET alias=$3, updated_at=NOW()`,
    [req.user.uid, contactUid, alias]
  );
  res.json({ ok: true });
});

app.post('/v1/relay/groups', authRequired, async (req, res) => {
  const title = safe(req.body.title, 80);
  const members = Array.isArray(req.body.members) ? req.body.members.map((v) => safe(v, 128)).filter(Boolean) : [];
  if (!title) return problem(res, 400, 'missing_group_title');
  const groupId = `grp_${crypto.randomUUID()}`;
  const uniqueMembers = Array.from(new Set([req.user.uid, ...members]));
  await pool.query('BEGIN');
  try {
    await pool.query('INSERT INTO groups(group_id,title,owner_uid) VALUES($1,$2,$3)', [groupId, title, req.user.uid]);
    for (const uid of uniqueMembers) await pool.query('INSERT INTO group_members(group_id,uid,role) VALUES($1,$2,$3)', [groupId, uid, uid === req.user.uid ? 'owner' : 'member']);
    await pool.query(
      `INSERT INTO chats(chat_id,participants,is_group,title,last_message_preview,last_message_epoch_ms,last_message_sender_id)
       VALUES($1,$2,true,$3,'Grupo creado',$4,$5)`,
      [groupId, uniqueMembers, title, Date.now(), req.user.uid]
    );
    await pool.query('COMMIT');
  } catch (error) { await pool.query('ROLLBACK'); throw error; }
  res.json({ ok: true, groupId });
});

app.get('/v1/relay/groups', authRequired, async (req, res) => {
  const { rows } = await pool.query(
    `SELECT g.group_id, g.title, g.owner_uid, g.photo_url, array_agg(gm.uid ORDER BY gm.joined_at) AS members
     FROM groups g JOIN group_members gm ON gm.group_id=g.group_id
     WHERE g.group_id IN (SELECT group_id FROM group_members WHERE uid=$1)
     GROUP BY g.group_id ORDER BY g.updated_at DESC`,
    [req.user.uid]
  );
  res.json({ ok: true, groups: rows });
});

app.post('/v1/relay/media', authRequired, mediaUpload.single('media'), async (req, res) => {
  if (!req.file) return problem(res, 400, 'missing_media');
  const mediaUrl = `${appBaseUrl(req).replace(/\/$/, '')}/uploads/encrypted-media/${req.file.filename}`;
  res.json({ ok: true, mediaUrl, mediaMime: req.file.mimetype, mediaName: req.file.originalname });
});

app.post('/v1/relay/statuses', authRequired, async (req, res) => {
  const kind = safe(req.body.kind || 'TEXT', 24).toUpperCase();
  const encryptedPayload = String(req.body.encryptedPayload || '');
  const iv = String(req.body.iv || '');
  const mediaUrl = safe(req.body.mediaUrl, 2048) || null;
  if (!encryptedPayload || !iv) return problem(res, 400, 'invalid_status_ciphertext');
  const statusId = `sts_${crypto.randomUUID()}`;
  await pool.query(
    `INSERT INTO statuses(status_id,owner_uid,kind,encrypted_payload,iv,media_url) VALUES($1,$2,$3,$4,$5,$6)`,
    [statusId, req.user.uid, kind, encryptedPayload, iv, mediaUrl]
  );
  res.json({ ok: true, statusId });
});

app.get('/v1/relay/statuses', authRequired, async (req, res) => {
  const { rows } = await pool.query(
    `SELECT s.status_id, s.owner_uid, u.name AS owner_name, s.kind, s.encrypted_payload, s.iv, s.media_url,
            EXTRACT(EPOCH FROM s.created_at) * 1000 AS created_at_ms,
            EXTRACT(EPOCH FROM s.expires_at) * 1000 AS expires_at_ms
     FROM statuses s JOIN users u ON u.uid=s.owner_uid
     WHERE s.expires_at > NOW() AND (s.owner_uid=$1 OR s.owner_uid IN (SELECT contact_uid FROM contacts WHERE owner_uid=$1))
     ORDER BY s.created_at DESC`,
    [req.user.uid]
  );
  res.json({ ok: true, statuses: rows });
});

app.post('/v1/relay/messages', authRequired, async (req, res) => {
  const senderId = safe(req.body.senderId, 128);
  const recipientId = safe(req.body.recipientId, 128);
  const kind = safe(req.body.kind || 'TEXT', 24).toUpperCase();
  const encryptedPayload = String(req.body.encryptedPayload || '');
  const iv = String(req.body.iv || '');
  const messageId = safe(req.body.messageId || crypto.randomUUID(), 128);
  const timestampMs = Number(req.body.timestamp || Date.now());
  const mediaUrl = safe(req.body.mediaUrl, 2048) || null;
  const mediaMime = safe(req.body.mediaMime, 128) || null;
  const mediaName = safe(req.body.mediaName, 256) || null;
  if (senderId !== req.user.uid) return problem(res, 403, 'sender_mismatch');
  if (!recipientId || recipientId === senderId) return problem(res, 400, 'invalid_recipient');
  if (!encryptedPayload || Buffer.byteLength(encryptedPayload, 'utf8') > MAX_CIPHERTEXT_BYTES) return problem(res, 400, 'invalid_ciphertext');
  if (!iv) return problem(res, 400, 'missing_iv');
  const group = await pool.query('SELECT group_id FROM group_members WHERE group_id=$1 AND uid=$2', [recipientId, senderId]);
  const isGroup = group.rowCount > 0;
  const chatId = isGroup ? recipientId : canonicalChatId(senderId, recipientId);
  const participants = isGroup ? (await pool.query('SELECT uid FROM group_members WHERE group_id=$1', [recipientId])).rows.map((r) => r.uid) : [senderId, recipientId];
  await pool.query('BEGIN');
  try {
    await pool.query(
      `INSERT INTO chats(chat_id, participants, is_group, last_message_preview, last_message_epoch_ms, last_message_sender_id, updated_at)
       VALUES($1,$2,$3,$4,$5,$6,NOW())
       ON CONFLICT(chat_id) DO UPDATE SET participants=$2, is_group=$3, last_message_preview=$4, last_message_epoch_ms=$5, last_message_sender_id=$6, updated_at=NOW()`,
      [chatId, participants, isGroup, kind === 'MEDIA' ? 'Multimedia cifrada' : 'Mensaje cifrado', timestampMs, senderId]
    );
    await pool.query(
      `INSERT INTO messages(chat_id,message_id,sender_id,recipient_id,kind,encrypted_payload,iv,timestamp_ms,media_url,media_mime,media_name)
       VALUES($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11) ON CONFLICT(chat_id,message_id) DO NOTHING`,
      [chatId, messageId, senderId, recipientId, kind, encryptedPayload, iv, timestampMs, mediaUrl, mediaMime, mediaName]
    );
    await pool.query('COMMIT');
  } catch (error) { await pool.query('ROLLBACK'); throw error; }
  for (const uid of participants.filter((id) => id !== senderId)) notifyRecipient(uid, { chatId, messageId, senderId, kind }).catch((error) => console.warn('[nexora-fcm] async_notification_failed', error.message));
  res.json({ ok: true, chatId, messageId });
});

app.get('/v1/relay/chats', authRequired, async (req, res) => {
  const limit = Math.min(Number(req.query.limit || 100), 300);
  const { rows } = await pool.query(
    `SELECT chat_id, participants, is_group, title, photo_url, last_message_preview, last_message_epoch_ms, last_message_sender_id FROM chats WHERE $1 = ANY(participants) ORDER BY last_message_epoch_ms DESC LIMIT $2`,
    [req.user.uid, limit]
  );
  res.json({ ok: true, chats: rows });
});

app.get('/v1/relay/chats/:chatId/messages', authRequired, async (req, res) => {
  const chatId = safe(req.params.chatId, 260);
  const since = Number(req.query.since || 0);
  const limit = Math.min(Number(req.query.limit || 200), 300);
  const chat = await pool.query(`SELECT participants FROM chats WHERE chat_id=$1 AND $2 = ANY(participants)`, [chatId, req.user.uid]);
  if (chat.rowCount === 0) return problem(res, 404, 'chat_not_found');
  const { rows } = await pool.query(
    `SELECT chat_id,message_id,sender_id,recipient_id,kind,encrypted_payload,iv,timestamp_ms,media_url,media_mime,media_name,delivered,read,protocol_version FROM messages WHERE chat_id=$1 AND timestamp_ms > $2 ORDER BY timestamp_ms ASC LIMIT $3`,
    [chatId, since, limit]
  );
  res.json({ ok: true, messages: rows });
});

app.use((error, _req, res, _next) => problem(res, 500, 'internal_error', error.message));

migrate().then(() => {
  app.listen(PORT, () => console.log(JSON.stringify({ ok: true, service: 'nexora-vps-relay-api', port: PORT })));
}).catch((error) => { console.error(error); process.exit(1); });
