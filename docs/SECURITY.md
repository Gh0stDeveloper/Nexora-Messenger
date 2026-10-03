# Nexora Messenger Security Notes

## Current guarantees

- The VPS relay requires a Firebase ID token for relay endpoints.
- Android sends `Authorization: Bearer <Firebase ID Token>` automatically through OkHttp.
- `senderId` is checked against the authenticated Firebase UID.
- Message plaintext is encrypted locally before hitting the relay.
- Backend stores `encryptedPayload`, `iv`, metadata and generic preview only.
- Avatars are stored on the VPS, not Firebase Storage.
- Runtime secrets are expected in `.env`, never committed.

## Current encryption model

Current message payload encryption uses AES-GCM on Android before upload to the relay. This is useful to prevent accidental plaintext storage on the VPS, but it is not yet a complete Signal-style E2EE protocol.

## Required next E2EE phases

1. Device identity registry.
2. Signed prekey and one-time prekey bundles.
3. X3DH-style session setup.
4. Double Ratchet message keys.
5. Multi-device sender keys for groups.
6. Key rotation and recovery UX.
7. Encrypted media key wrapping.

## VPS hardening checklist

- Use HTTPS only.
- Put API behind Nginx.
- Keep API bound to `127.0.0.1:8080`.
- Use long PostgreSQL password.
- Keep `FIREBASE_SERVICE_ACCOUNT_BASE64` outside Git.
- Run `backup.sh` daily.
- Keep firewall public ports limited to 80/443.
- Restrict SSH to key-only login.
