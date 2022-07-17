#!/usr/bin/env sh

echo "$(date) Stopping services for backup..."
docker service scale minecraft_waterfall=0
sleep 1m
docker service scale minecraft_paper=0
docker service scale minecraft_queue=0
sleep 20m

echo "$(date) Starting backup..."
tar --exclude /opt/stacks/minecraft/orebfuscator_cache -zcvf "/opt/backups/$(date).tgz" /opt/stacks/minecraft/

echo "$(date) Starting services after backup..."
docker service scale minecraft_paper=1
docker service scale minecraft_queue=1
sleep 10m
docker service scale minecraft_waterfall=1

echo "$(date) Backup finished!"
