package net.minelink.ctplus.factions.v1_6;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;

import net.minelink.ctplus.hook.Hook;
import net.minelink.ctplus.hook.factions.FactionRelation;
import net.minelink.ctplus.hook.factions.FactionsPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import static com.google.common.base.Preconditions.*;

public class FactionsHook implements Hook, FactionsPlugin {

    @Override
    public boolean isPvpEnabledAt(Location location) {
        FLocation fLocation = new FLocation(checkNotNull(location, "Null location"));
        Faction faction = Board.getInstance().getFactionAt(fLocation);
        return !faction.isSafeZone();
    }

    @Override
    public net.minelink.ctplus.hook.factions.Faction getFaction(UUID playerId) {
        FPlayer playerHandle = FPlayers.getInstance().getById(checkNotNull(playerId, "Null playerId").toString());
        Faction faction = playerHandle.getFaction();
        return faction.isNone() ? null : new v16Faction(faction);
    }

    @Override
    public boolean mayAttack(UUID attacker, @Nullable Location attackerLocation, UUID defender, @Nullable Location defenderLocation) {
        return Voodoo.canDamagerHurtDamagee(attacker, attackerLocation, defender, defenderLocation);
    }

    // We compile against an old version that doesn't have the TRUCE relationship
    private static final ImmutableMap<Relation, FactionRelation> RELATION_MAP;
    @Nullable
    private static final Relation TRUCE_RELATION;
    static {
        try {
            TRUCE_RELATION = (Relation) Relation.class.getDeclaredField("TRUCE").get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            TRUCE_RELATION = null;
            Bukkit.getPluginManager().getPlugin("CombatTagPlus").getLogger().warning("Old version of FactionsUUID found without truce support");
        }
        ImmutableMap.Builder<Relation, FactionRelation> relationBuilder = ImmutableMap.builder();
        relationBuilder.put(Relation.ENEMY, FactionRelation.ENEMY);
        relationBuilder.put(Relation.NEUTRAL, FactionRelation.NEUTRAL);
        if (TRUCE_RELATION != null) {
            relationBuilder.put(TRUCE_RELATION, FactionRelation.TRUCE);
        }
        relationBuilder.put(Relation.ALLY, FactionRelation.ALLY);
        relationBuilder.put(Relation.MEMBER, FactionRelation.MEMBER);
        RELATION_MAP = relationBuilder.build();
    }

    @Nonnull
    public static FactionRelation toAPIRelation(Relation relation) {
        FactionRelation wrapped = RELATION_MAP.get(Preconditions.checkNotNull(relation, "Null relation"));
        if (wrapped == null) throw new IllegalArgumentException("Relation " + relation.name() + " has no corresponding api relation.");
        return wrapped;
    }
}
