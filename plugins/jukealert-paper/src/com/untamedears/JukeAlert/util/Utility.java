package com.untamedears.JukeAlert.util;

import static com.untamedears.JukeAlert.util.Utility.isOnSnitch;

import java.util.List;
import java.util.Set;

import com.untamedears.citadel.entity.Faction;
import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

// Static methods only
public class Utility {

    public static boolean isOnSnitch(Snitch snitch, String playerName) {
        Faction faction = snitch.getGroup();
        if (faction == null) return false;
        return faction.isMember(playerName)
            || faction.isModerator(playerName)
            || faction.isFounder(playerName);
    }

    public static Snitch getSnitchUnderCursor(Player player) {
        SnitchManager manager = JukeAlert.getInstance().getSnitchManager();
        List<Block> lastTwo = player.getLastTwoTargetBlocks(null, 64);
        for (Block block : lastTwo) {
            Material mat = block.getType();
            if (mat != Material.JUKEBOX) {
                continue;
            }
            Snitch found = manager.getSnitch(block.getWorld(), block.getLocation());
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    //TODO: Fix this when Notes comes out
    public static boolean doesSnitchExist(Snitch snitch, boolean shouldCleanup) {
        Location loc = snitch.getLoc();
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        int type_id = world.getBlockAt(x, y, z).getType().getId();
        boolean exists = (type_id == 84);
        if (!exists && shouldCleanup) {
            System.out.println("Removing ghost snitch '" + snitch.getName() + "' at x:" + x + " y:" + y + " z:" + z);
            JukeAlert.getInstance().getSnitchManager().removeSnitch(snitch);
            JukeAlert.getInstance().getJaLogger().logSnitchBreak(world.getName(), x, y, z);
        }
        return exists;
    }
    
    public static Snitch findClosestOwnedSnitch(Player player) {
        Snitch closestSnitch = null;
        double closestDistance = Double.MAX_VALUE;
        Location playerLoc = player.getLocation();
        Set<Snitch> snitches = JukeAlert.getInstance().getSnitchManager().findSnitches(player.getWorld(), player.getLocation());
        for (final Snitch snitch : snitches) {
            if (isOnSnitch(snitch, player.getName())
                    && doesSnitchExist(snitch, true)) {
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
                && isOnSnitch(cursorSnitch, player.getName())
                && doesSnitchExist(cursorSnitch, true)) {
            return cursorSnitch;
        }
        return findClosestOwnedSnitch(player);
    }
}
