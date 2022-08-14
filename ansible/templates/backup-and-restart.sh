#!/usr/bin/env sh

echo "$(date) Stopping services for backup..."
docker service scale minecraft_waterfall=0
sleep 1m
docker service scale minecraft_paper=0
docker service scale minecraft_queue=0
sleep 20m

echo "$(date) Starting backup..."
export AWS_ACCESS_KEY_ID={{secret.backup.s3_access_key_id}}
export AWS_SECRET_ACCESS_KEY={{secret.backup.s3_access_key}}
export RESTIC_PASSWORD={{secret.backup.restic_password}}
restic \
  -r {{secret.backup.restic_shortterm_repo}} backup \
  /opt/stacks/minecraft/ \
  --exclude '**orebfuscator_cache'\
  --exclude '**dynmap'

echo "$(date) Starting services after backup..."
docker service scale minecraft_paper=1
docker service scale minecraft_queue=1
sleep 10m
docker service scale minecraft_waterfall=1

echo "$(date) Copying backup to longterm..."
restic \
  -r {{secret.backup.restic_shortterm_repo}} copy \
  --repo2 {{secret.backup.restic_repo}} \
  latest

echo "$(date) Pruning shortterm backups..."
restic \
  -r {{secret.backup.restic_shortterm_repo}} copy \
  forget \
  --keep-last 3 \
  --prune

echo "$(date) Backup finished!"
