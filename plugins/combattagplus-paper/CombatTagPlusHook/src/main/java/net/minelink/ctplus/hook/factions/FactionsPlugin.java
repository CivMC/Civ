package net.minelink.ctplus.hook.factions;

import java.util.UUID;

import net.minelink.ctplus.hook.Hook;

import org.bukkit.entity.Player;

public interface FactionsPlugin extends Hook {

    default Faction getFaction(Player player) {
        return getFaction(player.getUniqueId());
    }

    Faction getFaction(UUID playerId);

    default FactionRelation getRelation(UUID first, UUID second) {
        Faction firstFaction = getFaction(first);
        Faction secondFaction = getFaction(second);
        return firstFaction == null || secondFaction == null ? FactionRelation.NEUTRAL : firstFaction.getRelationTo(secondFaction);
    }

    default boolean mayAttack(UUID first, UUID second) {
        Faction firstFaction = getFaction(first);
        Faction secondFaction = getFaction(second);
        return firstFaction != null && secondFaction != null && firstFaction.mayAttack(secondFaction);
    }
}
