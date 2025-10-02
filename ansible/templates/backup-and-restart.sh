#!/usr/bin/env sh

echo "$(date) Stopping services for backup..."
docker service scale minecraft_waterfall=0
sleep 30s
docker service scale minecraft_paper=0
docker service scale minecraft_gamma=0
docker service scale minecraft_pvp=0
sleep 5m

echo "$(date) Starting backup..."

rsync -av --exclude postgres-data --exclude orebfuscator_cache /opt/stacks/minecraft/ /opt/backups/sync/
rm /opt/backups/old/*
mv /opt/backups/compressed/* /opt/backups/old
backupname="/opt/backups/compressed/$(date +"%Y_%m_%d")-backup.tar.zstd"
tar -c --exclude orebfuscator_cache --exclude civmodcore_cache --exclude postgres-data /opt/backups/sync | zstd -T0 -8 -o $backupname

echo "$(date) Starting services after backup..."
docker service scale minecraft_paper=1
docker service scale minecraft_gamma=1
docker service scale minecraft_pvp=1
docker service scale minecraft_waterfall=1

echo "$(date) Copying backup to longterm..."
sshpass -p {{secret.backup.archive_password}} rsync -a $backup pca@gateways.storage.us-east-va.cloud.ovh.us:civmc-pufferfish-backups

echo "$(date) Backup finished!"
