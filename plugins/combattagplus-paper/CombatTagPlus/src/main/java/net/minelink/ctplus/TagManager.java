package net.minelink.ctplus;

import net.minelink.ctplus.compat.api.NpcPlayerHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class TagManager {

    private final CombatTagPlus plugin;

    private final Map<UUID, Tag> tags = new HashMap<>();

    public TagManager(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    public void purgeExpired() {
        Iterator<Tag> iterator = tags.values().iterator();

        while (iterator.hasNext()) {
            Tag tag = iterator.next();
            if (tag.isExpired()) iterator.remove();
        }
    }

    public void tag(Player victim, Player attacker) {
        NpcPlayerHelper helper = plugin.getNpcPlayerHelper();

        UUID victimId = null;
        if (victim != null) {
            if (helper.isNpc(victim)) {
                victimId = helper.getIdentity(victim).getId();
            } else if (!victim.hasPermission("ctplus.bypass.tag")) {
                victimId = victim.getUniqueId();
            } else {
                victim = null;
            }
        }

        UUID attackerId = null;
        if (attacker != null) {
            if (helper.isNpc(attacker)) {
                attackerId = helper.getIdentity(attacker).getId();
            } else if (!attacker.hasPermission("ctplus.bypass.tag")) {
                attackerId = attacker.getUniqueId();
            } else {
                attacker = null;
            }
        }

        if (victim == null && attacker == null) return;

        int tagDuration = plugin.getSettings().getTagDuration();
        PlayerCombatTagEvent event = new PlayerCombatTagEvent(victim, attacker, tagDuration);

        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        long expireTime = System.currentTimeMillis() + (event.getTagDuration() * 1000);
        Tag tag = new Tag(helper, expireTime, victim, attacker);

        if (victim != null) {
            tags.put(victimId, tag);
        }

        if (attacker != null) {
            tags.put(attackerId, tag);
        }
    }

    public boolean untag(UUID playerId) {
        Tag tag = tags.remove(playerId);
        return tag != null && !tag.isExpired();
    }

    public Tag getTag(UUID playerId) {
        Tag tag = tags.get(playerId);
        return tag != null && !tag.isExpired() ? tag : null;
    }

    public boolean isTagged(UUID playerId) {
        return getTag(playerId) != null;
    }

}
