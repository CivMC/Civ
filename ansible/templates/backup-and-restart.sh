#!/usr/bin/env sh

echo "$(date) Stopping services for backup..."
docker service scale minecraft_waterfall=0
sleep 1m
docker service scale minecraft_paper=0
sleep 10m

echo "$(date) Starting backup..."
export AWS_ACCESS_KEY_ID={{secret.backup.s3_access_key_id}}
export AWS_SECRET_ACCESS_KEY={{secret.backup.s3_access_key}}
export RESTIC_PASSWORD={{secret.backup.restic_password}}
export RESTIC_PASSWORD2={{secret.backup.restic_password}}
restic \
  -r {{secret.backup.restic_shortterm_repo}} backup \
  /opt/stacks/minecraft/ \
  --exclude '**orebfuscator_cache'\
  --exclude '**civmodcore_cache'\
  --exclude '**dynmap'\
  --exclude '**postgres-data'\
  --exclude '**plugins'

echo "$(date) Starting services after backup..."
docker service scale minecraft_paper=1
sleep 5m
docker service scale minecraft_waterfall=1

echo "$(date) Copying backup to longterm..."
restic \
  -r {{secret.backup.restic_shortterm_repo}} copy \
  --repo2 {{secret.backup.restic_repo}} \
  latest

echo "$(date) Pruning shortterm backups..."
restic \
  -r {{secret.backup.restic_shortterm_repo}} forget \
  --keep-last 1 \
  --prune

echo "$(date) Backup finished!"
