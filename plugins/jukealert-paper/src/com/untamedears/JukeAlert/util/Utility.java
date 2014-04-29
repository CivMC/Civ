package com.untamedears.JukeAlert.util;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.untamedears.citadel.entity.Faction;
import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.util.IgnoreList;
import com.untamedears.JukeAlert.util.OnlineGroupMembers;
import com.untamedears.JukeAlert.util.RateLimiter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

// Static methods only
public class Utility {

    private static boolean debugging_ = false;

    public static boolean isDebugging() {
        return debugging_;
    }

    public static void setDebugging(boolean debugging) {
        debugging_ = debugging;
    }

    public static void notifyGroup(Snitch snitch, String message) {
        if (snitch.getGroup() == null) return;
        OnlineGroupMembers iter = OnlineGroupMembers
            .get(snitch.getGroup().getName())
            .reference(snitch.getLoc())
            .skipList(IgnoreList.GetPlayerIgnoreListByGroup(snitch.getGroup().getName()));
        if (!snitch.shouldLog()) {
            iter.maxDistance(
                JukeAlert.getInstance().getConfigManager().getMaxAlertDistanceNs());
        }
        for (Player player : iter) {
            RateLimiter.sendMessage(player, message);
        }
    }

    public static boolean isOnSnitch(Snitch snitch, UUID accountId) {
        Faction faction = snitch.getGroup();
        if (faction == null) return false;
        return faction.isMember(accountId)
            || faction.isModerator(accountId)
            || faction.isFounder(accountId);
    }
    
    public static boolean isPartialOwnerOfSnitch(Snitch snitch, UUID accountId) {
        Faction faction = snitch.getGroup();
        if (faction == null) return false;
        return faction.isModerator(accountId)
            || faction.isFounder(accountId);
    }

    public static Snitch getSnitchUnderCursor(Player player) {
        SnitchManager manager = JukeAlert.getInstance().getSnitchManager();
        Iterator<Block> itr = new BlockIterator(player, 40); // Within 2.5 chunks
        while (itr.hasNext()) {
            final Block block = itr.next();
            final Material mat = block.getType();
            if (mat != Material.JUKEBOX) {
                continue;
            }
            final Snitch found = manager.getSnitch(block.getWorld(), block.getLocation());
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    public static boolean doesSnitchExist(Snitch snitch, boolean shouldCleanup) {
        Location loc = snitch.getLoc();
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int type_id = world.getBlockAt(x, y, z).getType().getId();
        boolean exists = (type_id == 84 || type_id == 25);
        if (!exists && shouldCleanup) {
            final JukeAlert plugin = JukeAlert.getInstance();
            plugin.log("Removing ghost snitch '" + snitch.getName() + "' at x:" + x + " y:" + y + " z:" + z);
            plugin.getSnitchManager().removeSnitch(snitch);
            plugin.getJaLogger().logSnitchBreak(world.getName(), x, y, z);
        }
        return exists;
    }

    public static Snitch findClosestOwnedSnitch(Player player) {
        Snitch closestSnitch = null;
        double closestDistance = Double.MAX_VALUE;
        Location playerLoc = player.getLocation();
        UUID accountId = player.getUniqueId();
        Set<Snitch> snitches = JukeAlert.getInstance().getSnitchManager().findSnitches(player.getWorld(), player.getLocation());
        for (final Snitch snitch : snitches) {
            if (doesSnitchExist(snitch, true)
                    && isOnSnitch(snitch, accountId)) {
                double distance = snitch.getLoc().distanceSquared(playerLoc);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestSnitch = snitch;
                }
            }
        }
        return closestSnitch;
    }

    public static Snitch findTargetedOwnedSnitch(Player player) {
        Snitch cursorSnitch = getSnitchUnderCursor(player);
        if (cursorSnitch != null
                && doesSnitchExist(cursorSnitch, true)
                && isOnSnitch(cursorSnitch, player.getUniqueId())) {
            return cursorSnitch;
        }
        return findClosestOwnedSnitch(player);
    }
}
