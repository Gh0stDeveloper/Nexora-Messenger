# Nexora Messenger Backend

Backend self-hosted para Nexora Messenger.

## Carpetas

```text
backend/vps-api/      API Node.js/Express del relay cifrado
backend/vps/          Docker Compose, Nginx, scripts y entorno VPS
backend/VPS_DEPLOYMENT.md
```

## Responsabilidades del backend

- Verificar Firebase ID Tokens con Firebase Admin SDK.
- Registrar/actualizar usuarios.
- Guardar avatares en volumen persistente del VPS.
- Guardar chats y mensajes cifrados en PostgreSQL.
- Mantener preview genérico `Mensaje cifrado`.
- Rechazar mensajes donde `senderId` no coincide con el usuario autenticado.
- Exponer health checks para Nginx/monitorización.

## API local

```bash
cd backend/vps
docker compose up -d --build
curl http://127.0.0.1:8080/health
```

## Datos persistentes

Docker Compose usa volúmenes para:

```text
nexora_postgres   Base de datos PostgreSQL
nexora_uploads    Avatares y futuros archivos cifrados
```

## Backup/restore

```bash
bash backend/vps/scripts/backup.sh
bash backend/vps/scripts/list-backups.sh
bash backend/vps/scripts/restore.sh /opt/nexora/backups/<backup-id>
```

## Producción

En producción usa:

- Nginx con TLS.
- `.env` con `FIREBASE_SERVICE_ACCOUNT_BASE64`.
- contraseñas largas.
- backups periódicos.
- firewall permitiendo solo 80/443 públicos.
- API escuchando en `127.0.0.1:8080` detrás de Nginx.
