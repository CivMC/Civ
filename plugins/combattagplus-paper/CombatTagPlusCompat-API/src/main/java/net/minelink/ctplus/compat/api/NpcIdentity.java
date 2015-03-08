package net.minelink.ctplus.compat.api;

import org.bukkit.entity.Player;

import java.util.UUID;

public final class NpcIdentity {

    private final UUID id;

    private final String name;

    public NpcIdentity(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public NpcIdentity(Player player) {
        this(player.getUniqueId(), player.getName());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
