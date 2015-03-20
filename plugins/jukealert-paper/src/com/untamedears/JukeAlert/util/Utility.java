package com.untamedears.JukeAlert.util;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

// Static methods only
public class Utility {
    private static boolean debugging_ = false;

    public static boolean isDebugging() {
        return debugging_;
    }

    public static void setDebugging(boolean debugging) {
        debugging_ = debugging;
    }
    
    public static void notifyGroup(Group g, String message) throws SQLException{
        if (g == null) return;
        final JukeAlert plugin = JukeAlert.getInstance();
        Set<String> skipUUID = plugin.getJaLogger().getIgnoreUUIDs(g.getName());
        if(skipUUID == null){
        	//this should be fine as it is how it used to be done
        	skipUUID = null;
        }
        OnlineGroupMembers iter = OnlineGroupMembers
            .get(g.getName())
            .skipList(skipUUID);
        for (Player player : iter) {
            RateLimiter.sendMessage(player, message);
        }
    }

    public static void notifyGroup(Snitch snitch, String message) throws SQLException {
        if (snitch.getGroup() == null) return;
        final JukeAlert plugin = JukeAlert.getInstance();
        Set<String> skipUUID = plugin.getJaLogger().getIgnoreUUIDs(snitch.getGroup().getName());
        if(skipUUID == null){
        	//this should be fine as it is how it used to be done
        	skipUUID = null;
        }
        OnlineGroupMembers iter = OnlineGroupMembers
            .get(snitch.getGroup().getName())
            .reference(snitch.getLoc())
            .skipList(skipUUID);
        if (!snitch.shouldLog()) {
            iter.maxDistance(
                JukeAlert.getInstance().getConfigManager().getMaxAlertDistanceNs());
        }
        for (Player player : iter) {
            RateLimiter.sendMessage(player, message);
        }
    }

    public static boolean isOnSnitch(Snitch snitch, UUID accountId) {
        Group faction = snitch.getGroup();
        if (faction == null) return false;
        return faction.isMember(accountId);
    }
    
    public static boolean isPartialOwnerOfSnitch(Snitch snitch, UUID accountId) {
        Group faction = snitch.getGroup();
        if (faction == null) return false;
        PlayerType type = faction.getPlayerType(accountId);
        GroupPermission perm = NameAPI.getGroupManager().getPermissionforGroup(faction);
        return faction.isOwner(accountId) || (type != null && perm.isAccessible(type, PermissionType.BLOCKS));
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