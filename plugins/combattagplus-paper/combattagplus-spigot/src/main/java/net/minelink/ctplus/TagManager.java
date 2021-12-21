package net.minelink.ctplus;

import net.minelink.ctplus.compat.api.NpcPlayerHelper;
import net.minelink.ctplus.event.PlayerCombatTagEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TagManager {

    public enum Flag {
        TAG_VICTIM, TAG_ATTACKER
    }

    private final CombatTagPlus plugin;

    private final Map<UUID, Tag> tags = new HashMap<>();

    TagManager(CombatTagPlus plugin) {
        this.plugin = plugin;
    }

    void purgeExpired() {
        Iterator<Tag> iterator = tags.values().iterator();

        // Loop tags
        while (iterator.hasNext()) {
            Tag tag = iterator.next();

            // Remove expired tag
            if (tag.isExpired()) iterator.remove();
        }
    }

    public void tag(Player victim, Player attacker) {
        tag(victim, attacker, EnumSet.of(Flag.TAG_VICTIM, Flag.TAG_ATTACKER));
    }

    public void tag(Player victim, Player attacker, Set<Flag> flags) {
        NpcPlayerHelper helper = plugin.getNpcPlayerHelper();

        // Determine victim identity
        UUID victimId = null;
        if (victim != null) {
            if (victim.getHealth() <= 0 || victim.isDead()) {
                victim = null;
            } else if (helper.isNpc(victim)) {
                victimId = helper.getIdentity(victim).getId();
            } else if (!victim.hasPermission("ctplus.bypass.tag")) {
                victimId = victim.getUniqueId();
            } else {
                victim = null;
            }
        }

        // Determine attacker identity
        UUID attackerId = null;
        if (attacker != null) {
            if (attacker.getHealth() <= 0 || attacker.isDead() || attacker == victim) {
                attacker = null;
            } else if (helper.isNpc(attacker)) {
                attackerId = helper.getIdentity(attacker).getId();
            } else if (!attacker.hasPermission("ctplus.bypass.tag")) {
                attackerId = attacker.getUniqueId();
            } else {
                attacker = null;
            }
        }

        // Do nothing if both victim and attacker are blank
        if (victim == null && attacker == null) return;

        // Call tag event
        int tagDuration = plugin.getSettings().getTagDuration();
        PlayerCombatTagEvent event = new PlayerCombatTagEvent(victim, attacker, tagDuration);
        Bukkit.getPluginManager().callEvent(event);

        // Do nothing if event was cancelled
        if (event.isCancelled()) return;

        // Create new tag
        long expireTime = System.currentTimeMillis() + (event.getTagDuration() * 1000);
        Tag tag = new Tag(helper, expireTime, victim, attacker);

        // Add victim to tagged players
        if (victim != null && flags.contains(Flag.TAG_VICTIM)) {
            tags.put(victimId, tag);
        }

        // Add attacker to tagged players
        if (attacker != null && flags.contains(Flag.TAG_ATTACKER)) {
            tags.put(attackerId, tag);
        }
    }

    public boolean untag(UUID playerId) {
        Tag tag = tags.remove(playerId);
        return tag != null && !tag.isExpired();
    }

    public Tag getTag(UUID playerId) {
        return getTag(playerId, false);
    }

    public Tag getTag(UUID playerId, boolean includeHidden) {
        Tag tag = tags.get(playerId);

        if (tag == null || tag.isExpired() ||
                (!includeHidden && plugin.getSettings().onlyTagAttacker() &&
                        tag.getVictimId().equals(playerId))) {
            return null;
        }

        return tag;
    }

    public boolean isTagged(UUID playerId) {
        Tag tag = tags.get(playerId);
        boolean tagged = (tag != null && !tag.isExpired());

        if (tagged && plugin.getSettings().onlyTagAttacker()) {
            return !tag.getVictimId().equals(playerId);
        }

        return tagged;
    }

}
