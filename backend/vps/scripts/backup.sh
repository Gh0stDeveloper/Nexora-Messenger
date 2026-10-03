#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ ! -f .env ]]; then
  echo "[nexora-backup] Missing backend/vps/.env" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

BACKUP_ROOT="${BACKUP_ROOT:-/opt/nexora/backups}"
POSTGRES_DB="${POSTGRES_DB:-nexora}"
POSTGRES_USER="${POSTGRES_USER:-nexora}"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
DEST="$BACKUP_ROOT/$STAMP"

mkdir -p "$DEST"

echo "[nexora-backup] Creating backup at $DEST"

docker compose ps >/dev/null

echo "[nexora-backup] Dumping PostgreSQL database: $POSTGRES_DB"
docker compose exec -T postgres pg_dump \
  --clean \
  --if-exists \
  --no-owner \
  --no-privileges \
  -U "$POSTGRES_USER" \
  -d "$POSTGRES_DB" | gzip -9 > "$DEST/postgres.sql.gz"

echo "[nexora-backup] Archiving VPS uploads"
docker compose exec -T api tar -C /app -czf - uploads > "$DEST/uploads.tar.gz"

echo "[nexora-backup] Saving runtime config snapshot"
cp .env "$DEST/env.snapshot"
cat > "$DEST/manifest.json" <<JSON
{
  "app": "Nexora Messenger",
  "kind": "vps-backup",
  "createdAt": "$STAMP",
  "database": "$POSTGRES_DB",
  "postgresUser": "$POSTGRES_USER",
  "contains": ["postgres.sql.gz", "uploads.tar.gz", "env.snapshot"]
}
JSON

sha256sum "$DEST"/* > "$DEST/SHA256SUMS"

echo "[nexora-backup] Backup complete: $DEST"
