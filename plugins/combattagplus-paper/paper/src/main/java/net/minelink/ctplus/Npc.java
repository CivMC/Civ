package net.minelink.ctplus;

import net.minelink.ctplus.compat.base.NpcIdentity;
import net.minelink.ctplus.compat.base.NpcPlayerHelper;
import org.bukkit.entity.Player;

public final class Npc {

    private final NpcIdentity identity;

    private final Player entity;

    Npc(NpcPlayerHelper helper, Player entity) {
        this.identity = helper.getIdentity(entity);
        this.entity = entity;
    }

    public NpcIdentity getIdentity() {
        return identity;
    }

    public Player getEntity() {
        return entity;
    }

}
