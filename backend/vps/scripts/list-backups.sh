#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

BACKUP_ROOT="${BACKUP_ROOT:-/opt/nexora/backups}"

if [[ ! -d "$BACKUP_ROOT" ]]; then
  echo "No backups found at $BACKUP_ROOT"
  exit 0
fi

find "$BACKUP_ROOT" -mindepth 1 -maxdepth 1 -type d -print | sort -r | while read -r backup; do
  size="$(du -sh "$backup" | awk '{print $1}')"
  created="$(basename "$backup")"
  echo "$created  $size  $backup"
done
