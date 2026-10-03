# Nexora Messenger

**Nexora Messenger** es una aplicación Android nativa de mensajería privada con registro por número telefónico, perfil de usuario, relay cifrado en VPS propio y almacenamiento local en el dispositivo.

El objetivo del proyecto es construir una alternativa moderna y profesional de mensajería: interfaz limpia, privacidad real, backend controlado por el dueño del proyecto y arquitectura preparada para crecer hacia grupos, multimedia, estados, llamadas y multi-dispositivo.

## Estado actual

Esta versión está preparada como **Android + servidor VPS**.

Incluye en el paquete fuente generado:

- App Android en **Kotlin + Jetpack Compose**.
- Registro/login con **Firebase Auth por teléfono**.
- Sincronización de **FCM token** para notificaciones.
- Perfil con nombre obligatorio y avatar opcional.
- Avatares guardados en el **VPS propio**, no en Firebase Storage.
- Backend propio en **Node.js + Express**.
- **PostgreSQL** para usuarios, chats y mensajes cifrados.
- Relay de mensajes que guarda solo `encryptedPayload + iv`.
- Docker Compose para levantar servidor y PostgreSQL.
- Nginx + Certbot para HTTPS.
- GitHub Actions para validar Android, backend, Docker y despliegue VPS.

## Arquitectura

```text
Nexora Android
 ├─ Firebase Auth
 │   └─ OTP por número telefónico
 ├─ Firebase Cloud Messaging
 │   └─ notificaciones push
 ├─ Room
 │   └─ caché local
 ├─ Android Keystore
 │   └─ clave local del dispositivo
 └─ HTTPS
     └─ VPS Relay API

VPS Nexora
 ├─ Nginx + TLS
 ├─ Node.js / Express
 ├─ PostgreSQL
 ├─ almacenamiento persistente de avatares
 └─ Docker Compose
```

## Seguridad del relay

El servidor no recibe mensajes en texto plano. Cada mensaje se envía como payload cifrado:

```json
{
  "chatId": "uid1_uid2",
  "messageId": "uuid",
  "senderId": "uid1",
  "recipientId": "uid2",
  "kind": "TEXT",
  "encryptedPayload": "base64",
  "iv": "base64",
  "timestamp": 1760000000000
}
```

El backend:

- verifica `Authorization: Bearer FIREBASE_ID_TOKEN`;
- comprueba que `senderId` sea el usuario autenticado;
- valida `chatId` canónico;
- bloquea previews enviados por cliente;
- guarda siempre `Mensaje cifrado` como preview;
- rechaza duplicados por `messageId`;
- solo permite ACK de entrega/lectura al destinatario real.

## Endpoints principales del VPS

```text
GET  /health
GET  /ready
GET  /v1/relay/version
POST /v1/relay/users/me/bootstrap
POST /v1/relay/users/me/avatar
POST /v1/relay/users/me/fcm-token
GET  /v1/relay/users/:uid/public
POST /v1/relay/messages
GET  /v1/relay/chats
GET  /v1/relay/chats/:chatId/messages
POST /v1/relay/ack
```

Todos los endpoints `/v1/relay/*` requieren:

```text
Authorization: Bearer FIREBASE_ID_TOKEN
```

## Configuración Android

1. Crea el proyecto en Firebase.
2. Agrega la app Android con package:

```text
com.nexora.app
```

3. Descarga `google-services.json`.
4. Colócalo en:

```text
android/app/google-services.json
```

5. Compila apuntando al VPS:

```bash
cd android
gradle :app:assembleDebug -PNEXORA_RELAY_BASE_URL=https://api.tu-dominio.com
```

## Configuración VPS

```bash
sudo bash backend/vps/scripts/install-vps.sh
sudo mkdir -p /opt/nexora
sudo chown -R $USER:$USER /opt/nexora
rsync -az ./ /opt/nexora/
cd /opt/nexora/backend/vps
cp .env.example .env
nano .env
./scripts/deploy.sh
```

Variables mínimas:

```env
DOMAIN=api.tu-dominio.com
POSTGRES_PASSWORD=pon_una_password_larga
DATABASE_URL=postgresql://nexora:pon_una_password_larga@postgres:5432/nexora
FIREBASE_PROJECT_ID=tu-proyecto-firebase
APP_PUBLIC_BASE_URL=https://api.tu-dominio.com
UPLOAD_DIR=/app/uploads
MAX_AVATAR_BYTES=2097152
```

## GitHub Actions previstos

- `CI`: Android debug APK, VPS API check, Docker build y Firebase Functions build.
- `Android Build`: compilación rápida del APK debug.
- `Deploy VPS Relay`: despliegue por SSH al VPS.

Secrets recomendados:

```text
GOOGLE_SERVICES_JSON
NEXORA_RELAY_BASE_URL
VPS_HOST
VPS_USER
VPS_SSH_KEY
VPS_APP_DIR
```

## Nota de entrega

El paquete fuente completo fue preparado como ZIP con Android + backend VPS + workflows. Si este repositorio se inicializó desde el conector, sube/descomprime ese paquete en la raíz del repo para dejar visible todo el árbol de código.

## Roadmap inmediato

- Integrar sesiones E2EE por destinatario/dispositivo.
- Agregar contactos por agenda telefónica con hashing.
- Crear grupos con roles.
- Añadir estados efímeros.
- Añadir subida cifrada de imágenes/documentos al VPS.
- Crear release firmado por GitHub Actions.
- Añadir página web pública de descarga.

## Créditos

Proyecto desarrollado para el ecosistema **Nexora** por **Ghost Developer**.
