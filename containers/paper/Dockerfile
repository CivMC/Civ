FROM itzg/minecraft-server

# https://github.com/itzg/docker-minecraft-server#optional-plugins-mods-and-config-attach-points
COPY plugins /plugins
ENV REMOVE_OLD_MODS=TRUE

# https://github.com/itzg/docker-minecraft-server#patching-existing-files
COPY patches /patches
ENV PATCH_DEFINITIONS=/patches

# https://github.com/itzg/docker-minecraft-server#optional-plugins-mods-and-config-attach-points
COPY config /config
ENV COPY_CONFIG_DEST=/data
ENV SYNC_SKIP_NEWER_IN_DESTINATION=false

# Kind of annoying we have to set this all here instead of a patch...
# https://github.com/itzg/docker-minecraft-server#server-configuration
ENV OVERRIDE_SERVER_PROPERTIES=true
ENV MOTD="You're a §l§abeautiful§r person."
ENV MAX_PLAYERS="150"
ENV SPAWN_PROTECTION="0"
ENV ONLINE_MODE="false"

# https://github.com/itzg/docker-minecraft-server#running-a-paper-server
ENV TYPE=PAPER
ENV EULA=TRUE
# https://github.com/itzg/docker-minecraft-server#replacing-variables-inside-configs
ENV REPLACE_ENV_DURING_SYNC=TRUE
ENV REPLACE_ENV_VARIABLE_PREFIX=CIV
