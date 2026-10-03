# Nexora Messenger

**Nexora Messenger** es una aplicación Android nativa de mensajería privada para el ecosistema Nexora. Usa registro por número telefónico, perfil con avatar en servidor propio, caché local, notificaciones push y un relay cifrado desplegable en VPS.

El objetivo es construir una base real, controlada por el dueño del proyecto, sin depender de Supabase, Vercel Blob ni almacenamiento externo para los datos principales de mensajería.

## Estado actual

La rama `main` ya contiene una base funcional creada directamente en el repositorio:

- Android nativo con **Kotlin + Jetpack Compose**.
- Login por teléfono con **Firebase Auth OTP**.
- Sincronización de **FCM token** con el VPS.
- Servicio FCM real para token refresh y notificaciones locales.
- Perfil obligatorio con nombre.
- Avatar guardado en el **VPS propio**, no en Firebase Storage.
- Room para perfiles, chats y mensajes locales.
- Relay Android por Retrofit/OkHttp con `Authorization: Bearer <Firebase ID Token>`.
- Pantalla de chats.
- Pantalla `ChatDetail`.
- Cifrado local **AES-GCM** antes de enviar mensajes al relay.
- Backend VPS en **Node.js + Express**.
- Base de datos **PostgreSQL**.
- Docker Compose para API + PostgreSQL.
- Nginx reverse proxy preparado.
- Scripts de backup/restore para PostgreSQL + uploads.
- GitHub Actions para validar backend, Docker y APK debug.

## Arquitectura

```text
Android / Nexora Messenger
 ├─ Firebase Auth
 │   └─ OTP por número telefónico
 ├─ Firebase Cloud Messaging
 │   ├─ refresh de token
 │   └─ notificación local de mensajes cifrados
 ├─ Room
 │   ├─ perfil local
 │   ├─ chats
 │   └─ mensajes cifrados
 ├─ Android Keystore
 │   ├─ identity public key
 │   └─ AES-GCM local para payload de mensajes
 └─ HTTPS
     └─ VPS Relay API

VPS Nexora
 ├─ Nginx + TLS
 ├─ Node.js / Express
 ├─ PostgreSQL
 ├─ volumen persistente de uploads/avatars
 ├─ backup/restore scripts
 └─ Docker Compose
```

## Seguridad del relay

El servidor no debe recibir texto plano. La app cifra el mensaje en Android antes de enviarlo:

```json
{
  "senderId": "uid1",
  "recipientId": "uid2",
  "kind": "TEXT",
  "encryptedPayload": "base64",
  "iv": "base64",
  "messageId": "uuid",
  "timestamp": 1760000000000
}
```

El backend:

- verifica `Authorization: Bearer FIREBASE_ID_TOKEN`;
- rechaza requests sin sesión válida;
- comprueba que `senderId` sea el usuario autenticado;
- guarda `encryptedPayload + iv`, no texto plano;
- usa `Mensaje cifrado` como preview genérico;
- guarda avatares en el VPS;
- guarda usuarios, chats y mensajes en PostgreSQL.

> Nota técnica: el cifrado actual es AES-GCM local antes del relay. La capa pendiente para E2EE completo tipo Signal es: bundles de prekeys, establecimiento de sesión por dispositivo, Double Ratchet y rotación de claves.

## Estructura del repositorio

```text
android/                 App Android nativa Kotlin/Compose
backend/vps-api/         API Node.js/Express del relay
backend/vps/             Docker Compose, Nginx, scripts VPS
backend/VPS_DEPLOYMENT.md
.github/workflows/       CI y deploy VPS
README.md                Documentación principal
```

## Endpoints principales

```text
GET  /health
GET  /ready
GET  /v1/relay/version
POST /v1/relay/users/me/bootstrap
POST /v1/relay/users/me/avatar
POST /v1/relay/users/me/fcm-token
POST /v1/relay/messages
GET  /v1/relay/chats
GET  /v1/relay/chats/:chatId/messages
```

Todos los endpoints `/v1/relay/*` requieren:

```text
Authorization: Bearer FIREBASE_ID_TOKEN
```

## Configuración Android

1. Crea el proyecto en Firebase.
2. Activa **Phone Authentication**.
3. Agrega una app Android con package:

```text
com.nexora.app
```

4. Descarga `google-services.json`.
5. Colócalo en:

```text
android/app/google-services.json
```

6. Compila apuntando al VPS:

```bash
cd android
gradle :app:assembleDebug -PNEXORA_RELAY_BASE_URL=https://api.tu-dominio.com
```

Para emulador local:

```bash
gradle :app:assembleDebug -PNEXORA_RELAY_BASE_URL=http://10.0.2.2:8080
```

## Configuración VPS

```bash
sudo mkdir -p /opt/nexora
sudo chown -R $USER:$USER /opt/nexora
rsync -az ./ /opt/nexora/
cd /opt/nexora/backend/vps
cp .env.example .env
nano .env
bash scripts/deploy.sh
```

Variables mínimas:

```env
POSTGRES_DB=nexora
POSTGRES_USER=nexora
POSTGRES_PASSWORD=pon_una_password_larga
APP_PUBLIC_BASE_URL=https://api.tu-dominio.com
FIREBASE_SERVICE_ACCOUNT_BASE64=base64_del_service_account
MAX_AVATAR_BYTES=2097152
MAX_CIPHERTEXT_BYTES=524288
BACKUP_ROOT=/opt/nexora/backups
```

## Backup y restore

Crear backup:

```bash
cd /opt/nexora/backend/vps
bash scripts/backup.sh
```

Listar backups:

```bash
bash scripts/list-backups.sh
```

Restaurar backup:

```bash
bash scripts/restore.sh /opt/nexora/backups/20261003T000000Z
```

El backup incluye:

- `postgres.sql.gz`
- `uploads.tar.gz`
- `env.snapshot`
- `manifest.json`
- `SHA256SUMS`

## GitHub Actions

Workflow principal:

```text
.github/workflows/ci.yml
```

Valida:

- backend Node.js
- `node --check server.js`
- Docker build
- Android debug APK
- artifact APK debug

Workflow de despliegue VPS:

```text
.github/workflows/deploy-vps.yml
```

Secrets recomendados:

```text
VPS_HOST
VPS_USER
VPS_SSH_KEY
VPS_APP_DIR
NEXORA_RELAY_BASE_URL
GOOGLE_SERVICES_JSON
```

## Roadmap inmediato

- Capa E2EE completa por sesión/dispositivo.
- Contactos por teléfono con hashing.
- Búsqueda de usuario por número sin exponer teléfono público.
- Entrega offline con TTL y ACK real.
- Adjuntos cifrados: imagen, audio, documento.
- Grupos.
- Estados/stories efímeros.
- Release firmado V1/V2/V3.

## Créditos

Proyecto desarrollado para el ecosistema **Nexora** por **Ghost Developer**.
