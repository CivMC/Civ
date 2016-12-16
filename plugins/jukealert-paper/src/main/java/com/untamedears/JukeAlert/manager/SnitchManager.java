package com.untamedears.JukeAlert.manager;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.civmodcore.locations.QTBox;
import vg.civcraft.mc.civmodcore.locations.SparseQuadTree;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;

public class SnitchManager {

    public static final int EXIT_PADDING = 2;

    private final JukeAlert plugin;
    private final JukeAlertLogger logger;
    private Map<Integer, Snitch> snitchesById;
    private Map<World, SparseQuadTree> snitches;
    private Map<String, Set <Snitch>> snitchesByGroup;

    public SnitchManager() {
        plugin = JukeAlert.getInstance();
        logger = plugin.getJaLogger();
    }

    public void initialize() {
        snitchesById = new TreeMap<Integer, Snitch>();
        snitches = new HashMap<World, SparseQuadTree>(EXIT_PADDING);
        snitchesByGroup = new HashMap<String, Set<Snitch>>();
        List<World> worlds = plugin.getServer().getWorlds();
        for (World world : worlds) {
            SparseQuadTree worldSnitches = new SparseQuadTree(EXIT_PADDING);
            Enumeration<Snitch> se = logger.getAllSnitches(world);
            while (se.hasMoreElements()) {
                Snitch snitch = se.nextElement();
                snitchesById.put(snitch.getId(), snitch);
                worldSnitches.add(snitch);
				if (snitch.getGroup() != null) {
					Set <Snitch> groupSet = snitchesByGroup.get(snitch.getGroup().getName());
					if (groupSet == null) {
						groupSet  = new HashSet<Snitch>();
						snitchesByGroup.put(snitch.getGroup().getName(), groupSet);
					}
					groupSet.add(snitch);
				}
            }
            snitches.put(world, worldSnitches);
        }
        if (plugin.getConfigManager().getSnitchCullingEnabled()) {
            this.cullSnitches();
        }
        if (plugin.getConfigManager().getSnitchEntryCullingEnabled()) {
            logger.cullSnitchEntries();
        }
        if (plugin.getConfigManager().getToggleRestartCheckGroup()){
        	for (Snitch snitch: snitchesById.values()){
        		Location loc = snitch.getLoc();
        		Reinforcement rein = Citadel.getReinforcementManager().getReinforcement(loc);
        		if (rein == null || !(rein instanceof PlayerReinforcement))
        			continue;
        		PlayerReinforcement pRein = (PlayerReinforcement) rein;
        		Group reinfGroup = pRein.getGroup();
        		Group snitchGroup = snitch.getGroup();
        		if (reinfGroup == null) {
					plugin.getLogger().log(Level.SEVERE, "Juke at {0} is reinforced to a group that no longer exists: {1} (former id: {2}) - removing",
							new Object[] {snitch.getLoc(), (snitchGroup != null ? snitchGroup.getName() : null), pRein.getGroupId() });
					final String worldName = snitch.getLoc().getWorld().getName();
					final int x = snitch.getX();
					final int y = snitch.getY();
					final int z = snitch.getZ();
        			logger.logSnitchBreak(worldName, x, y, z);
        			continue;
        		}
        		if (snitchGroup == null || !reinfGroup.getName().equals(snitchGroup.getName())){
        			plugin.getLogger().log(Level.INFO, "Found mismatched juke at {0} correcting from {1} to {2}",
        					new Object[] { snitch.getLoc(), (snitchGroup != null ? snitchGroup.getName() : null),
        							reinfGroup.getName()});
        			snitch.setGroup(reinfGroup);
        		}
        	}
        }
    }

    public void saveSnitches() {
        this.logger.saveAllSnitches();
    }

    public void cullSnitches() {
        // Snitch culling works by the last login time of the owning group's
        //  founder and moderators. If none of those players have logged in
        //  within the inactivity threshold, the snitch gets culled.
        // Player last online times and group culling state is cached as it's
        //  calculated.
        plugin.log("Culling snitches...");
        Map<String, Boolean> cullGroups = new HashMap<String, Boolean>();
        Map<UUID, Long> uuids = new TreeMap<UUID, Long>();
        long timeThreshold = System.currentTimeMillis() - (
            86400000L * (long)plugin.getConfigManager().getMaxSnitchLifetime());
        for (Snitch snitch : getAllSnitches()) {
            final Group group = snitch.getGroup();
            if (group == null) {
            	plugin.getLogger().log(Level.WARNING, "Snitch at {0} lacks a valid group, skipping cull.", snitch.getLoc());
            	continue;
            }
            final String groupName = group.getName();
            Boolean performCull = cullGroups.get(groupName);
            if (performCull == null) {
                UUID playerName = group.getOwner();
                if (!uuids.containsKey(playerName)) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                    if (player != null) {
                    	uuids.put(playerName, player.getLastPlayed());
                    } else {
                    	uuids.put(playerName, null);
                    }
                }
                Long maxLastPlayed = uuids.get(playerName);
                for (UUID mod : group.getAllMembers()) {
                    playerName = mod;
                    if (!NameAPI.getGroupManager().hasAccess(group, playerName, PermissionType.getPermission("LIST_SNITCHES"))) // If they have permission to break blocks.
                    	continue;
                    if (!uuids.containsKey(playerName)) {
                        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                        if (player != null) {
                        	uuids.put(playerName, player.getLastPlayed());
                        } else {
                        	uuids.put(playerName, null);
                        }
                    }
                    Long lastPlayed = uuids.get(playerName);
                    if (maxLastPlayed == null || (lastPlayed != null && lastPlayed > maxLastPlayed)) {
                        maxLastPlayed = lastPlayed;
                    }
                }
                performCull = maxLastPlayed == null || maxLastPlayed < timeThreshold;
                cullGroups.put(groupName, performCull);
            }
            if (!performCull) {
                continue;
            }
            // Cull the snitch
            final String worldName = snitch.getLoc().getWorld().getName();
            final int x = snitch.getX();
            final int y = snitch.getY();
            final int z = snitch.getZ();
            logger.logSnitchBreak(worldName, x, y, z);
            plugin.log(String.format(
                    "Culling snitch '%s' @ (%s,%d,%d,%d) for '%s'",
                    snitch.getName(), worldName, x, y, z, group.getName()));
        }
        plugin.log("Snitch culling complete!");
    }

    public Collection<Snitch> getAllSnitches() {
        return this.snitchesById.values();
    }

    public void setSnitches(Map<World, SparseQuadTree> snitches) {
        this.snitches = snitches;
    }

    public Snitch getSnitch(int snitch_id) {
        return this.snitchesById.get(snitch_id);
    }

    public Snitch getSnitch(World world, Location location) {
        Set<? extends QTBox> potentials = snitches.get(world).find(location.getBlockX(), location.getBlockZ());
        for (QTBox box : potentials) {
            Snitch sn = (Snitch) box;
            if (sn.at(location)) {
                return sn;
            }
        }
        return null;
    }

    public void addSnitch(Snitch snitch) {
        World world = snitch.getLoc().getWorld();
        if (snitches.get(world) == null) {
            SparseQuadTree map = new SparseQuadTree(EXIT_PADDING);
            map.add(snitch);
            snitches.put(world, map);
        } else {
            snitches.get(world).add(snitch);
        }
        Set <Snitch> groupSet = snitchesByGroup.get(snitch.getGroup().getName());
        if (groupSet == null) {
        	groupSet  = new HashSet<Snitch>();
        	snitchesByGroup.put(snitch.getGroup().getName(), groupSet);
        }
        groupSet.add(snitch);
        snitchesById.put(snitch.getId(), snitch);
    }

    public void removeSnitch(Snitch snitch) {
        snitches.get(snitch.getLoc().getWorld()).remove(snitch);
        snitchesById.remove(snitch.getId());
        Set <Snitch> groupSet = snitchesByGroup.get(snitch.getGroup().getName());
        if (groupSet != null) {
        	groupSet.remove(snitch);
        }
    }

    public Set<Snitch> findSnitches(World world, Location location) {
        return findSnitches(world, location, false);
    }

    public Set<Snitch> findSnitches(World world, Location location, boolean includePaddingZone) {
        if (snitches.get(world) == null) {
            return new TreeSet<Snitch>();
        }
        Set<Snitch> results = new TreeSet<Snitch>();
        Set<QTBox> found = snitches.get(world).find(
            location.getBlockX(), location.getBlockZ(), includePaddingZone);
        for (QTBox box : found) {
            Snitch sn = (Snitch) box;
            if (sn.isWithinHeight(location.getBlockY())) {
                results.add(sn);
            }
        }
        return results;
    }

    public Set <Snitch> getSnitchesForGroup(String groupName) {
    	return snitchesByGroup.get(groupName);
    }
}
