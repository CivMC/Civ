package net.minelink.ctplus.factions.v1_6;

import java.util.UUID;
import javax.annotation.Nullable;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Conf;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Relation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class Voodoo {
    private Voodoo() {
    }

    // magic based on FactionsEntityListener.canDamagerHurtDamagee(EntityDamageByEntityEvent) modified to work for offline players

    public static boolean canDamagerHurtDamagee(UUID damager, UUID damagee) {
        @Nullable
        Entity attackerEntity = Bukkit.getPlayer(damager);
        @Nullable
        Entity defenderEntity = Bukkit.getPlayer(damagee);

        FPlayer defender = FPlayers.getInstance().getById(damagee.toString());

        if (defender == null) return true;

        Location defenderLoc = defenderEntity != null ? defenderEntity.getLocation() : null;
        Faction defLocFaction = defenderEntity != null ? Board.getInstance().getFactionAt(new FLocation(defenderLoc)) : null;

        /*
        // for damage caused by projectiles, getDamager() returns the projectile... what we need to know is the source
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;

            if (!(projectile.getShooter() instanceof Entity)) {
                return true;
            }

            damager = (Entity) projectile.getShooter();
        }
        */

        if (damager.equals(damagee)) return true; // ender pearl usage and other self-inflicted damage

        // Players can not take attack damage in a SafeZone, or possibly peaceful territory
        if (defenderEntity != null && defLocFaction.noPvPInTerritory()) {
            /*
            if (damager instanceof Player) {
                return false;
            }
            return !defLocFaction.noMonstersInTerritory();
            */
            return false;
        }

        /*
        if (!(damager instanceof Player)) {
            return true;
        }
        */

        FPlayer attacker = FPlayers.getInstance().getById(damager.toString());

        if (attacker == null) return true;

        if (Conf.playersWhoBypassAllProtection.contains(attacker.getName())) {
            return true;
        }

        if (attacker.hasLoginPvpDisabled()) {
            return false;
        }

        Faction locFaction = attackerEntity != null ? Board.getInstance().getFactionAt(new FLocation(attacker)) : null;

        // so we know from above that the defender isn't in a safezone... what about the attacker, sneaky dog that he might be?
        if (attackerEntity != null && locFaction.noPvPInTerritory()) {
            return false;
        }

        if (attackerEntity != null && locFaction.isWarZone() && Conf.warZoneFriendlyFire) {
            return true;
        }

        if (defenderEntity != null && Conf.worldsIgnorePvP.contains(defenderLoc.getWorld().getName())) {
            return true;
        }

        Faction defendFaction = defender.getFaction();
        Faction attackFaction = attacker.getFaction();

        if (attackFaction.isNone() && Conf.disablePVPForFactionlessPlayers) {
            return false;
        } else if (defendFaction.isNone()) {
            if (defLocFaction == attackFaction && Conf.enablePVPAgainstFactionlessInAttackersLand) {
                // Allow PVP vs. Factionless in attacker's faction territory
                return true;
            } else if (Conf.disablePVPForFactionlessPlayers) {
                return false;
            }
        }

        if (defendFaction.isPeaceful()) {
            return false;
        } else if (attackFaction.isPeaceful()) {
            return false;
        }

        Relation relation = defendFaction.getRelationTo(attackFaction);

        // You can not hurt neutral factions
        if (Conf.disablePVPBetweenNeutralFactions && relation.isNeutral()) {
            return false;
        }

        // Players without faction may be hurt anywhere
        if (!defender.hasFaction()) {
            return true;
        }

        // You can never hurt faction members or allies
        if (relation.isMember() || relation.isAlly()) {
            return false;
        }

        boolean ownTerritory = defender.isInOwnTerritory();

        // You can not hurt neutrals in their own territory.
        if (ownTerritory && relation.isNeutral()) {
            return false;
        }

        return true;
    }
}
