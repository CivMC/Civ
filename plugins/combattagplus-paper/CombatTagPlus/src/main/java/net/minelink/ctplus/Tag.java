package net.minelink.ctplus;

import net.minelink.ctplus.compat.api.NpcPlayerHelper;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.UUID;

public final class Tag {

    private long tagTime = System.currentTimeMillis();

    private long expireTime;

    private UUID victimId;

    private String victimName;

    private UUID attackerId;

    private String attackerName;

    Tag(NpcPlayerHelper helper, long expireTime, Player victim, Player attacker) {
        this.expireTime = expireTime;

        // Determine victim identity
        if (victim != null) {
            if (helper.isNpc(victim)) {
                this.victimId = helper.getIdentity(victim).getId();
                this.victimName = helper.getIdentity(victim).getName();
            } else {
                this.victimId = victim.getUniqueId();
                this.victimName = victim.getName();
            }
        }

        // Determine attacker identity
        if (attacker != null) {
            if (helper.isNpc(attacker)) {
                this.attackerId = helper.getIdentity(attacker).getId();
                this.attackerName = helper.getIdentity(attacker).getName();
            } else {
                this.attackerId = attacker.getUniqueId();
                this.attackerName = attacker.getName();
            }
        }
    }

    public long getTagTime() {
        return tagTime;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public UUID getVictimId() {
        return victimId;
    }

    public String getVictimName() {
        return victimName;
    }

    public UUID getAttackerId() {
        return attackerId;
    }

    public String getAttackerName() {
        return attackerName;
    }

    public int getTagDuration() {
        long currentTime = System.currentTimeMillis();
        return expireTime > currentTime ? NumberConversions.ceil((expireTime - currentTime) / 1000D) : 0;
    }

    public boolean isExpired() {
        return getTagDuration() < 1;
    }

}
