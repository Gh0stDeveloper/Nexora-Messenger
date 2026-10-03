# Nexora Messenger — Despliegue en VPS

Esta guía instala el backend propio de Nexora Messenger en un VPS usando Docker Compose, PostgreSQL y Nginx como reverse proxy.

## 1. Requisitos

- Ubuntu 22.04/24.04 recomendado
- Docker + Docker Compose plugin
- Nginx
- Certbot
- Dominio apuntando al VPS, por ejemplo `api.tu-dominio.com`
- Proyecto Firebase con Phone Auth activado
- Service Account de Firebase codificado en Base64 para `FIREBASE_SERVICE_ACCOUNT_BASE64`

## 2. Variables

Copia el template:

```bash
cd backend/vps
cp .env.example .env
nano .env
```

Configura como mínimo:

```env
POSTGRES_DB=nexora
POSTGRES_USER=nexora
POSTGRES_PASSWORD=CAMBIA_ESTA_PASSWORD
APP_PUBLIC_BASE_URL=https://api.tu-dominio.com
FIREBASE_SERVICE_ACCOUNT_BASE64=BASE64_DEL_SERVICE_ACCOUNT
```

Para generar el Base64:

```bash
base64 -w 0 firebase-service-account.json
```

## 3. Levantar backend

```bash
cd backend/vps
chmod +x scripts/deploy.sh
./scripts/deploy.sh
```

Prueba local:

```bash
curl http://127.0.0.1:8080/health
```

## 4. Nginx

```bash
sudo cp backend/vps/nginx/nexora-api.conf /etc/nginx/sites-available/nexora-api.conf
sudo sed -i 's/api.tu-dominio.com/api.TU-DOMINIO.com/g' /etc/nginx/sites-available/nexora-api.conf
sudo ln -sf /etc/nginx/sites-available/nexora-api.conf /etc/nginx/sites-enabled/nexora-api.conf
sudo nginx -t
sudo systemctl reload nginx
sudo certbot --nginx -d api.TU-DOMINIO.com
```

## 5. Android

Compila apuntando al dominio real:

```bash
cd android
gradle :app:assembleDebug -PNEXORA_RELAY_BASE_URL=https://api.TU-DOMINIO.com
```

Antes de compilar en local, coloca tu archivo real:

```text
android/app/google-services.json
```

No subas `google-services.json` ni llaves privadas al repositorio.

## 6. Rutas principales

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

## Seguridad

El servidor no guarda mensajes en texto plano. Guarda `encrypted_payload`, `iv`, remitente, destinatario y estados de entrega. Los avatares sí se guardan en el VPS bajo `/uploads/avatars` y se sirven públicamente por URL.
