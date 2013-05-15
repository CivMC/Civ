package com.untamedears.JukeAlert.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.PlayerManager;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.Utility;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.events.CreateReinforcementEvent;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JukeAlertListener implements Listener {

    private JukeAlert plugin = JukeAlert.getInstance();
    SnitchManager snitchManager = plugin.getSnitchManager();
    PlayerManager playerManager = plugin.getPlayerManager();
    private Map<String, Set<Snitch>> playersInSnitches = new TreeMap<String, Set<Snitch>>();

    private boolean checkProximity(Snitch snitch, String playerName) {
        Set<Snitch> inList = playersInSnitches.get(playerName);
        if (inList == null) {
            inList = new TreeSet<Snitch>();
            playersInSnitches.put(playerName, inList);
        }
        return inList.contains(snitch);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerJoinEvent(PlayerJoinEvent event) {
        playerManager.addPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerQuitEvent(PlayerQuitEvent event) {
        playerManager.removePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void placeSnitchBlock(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (!block.getType().equals(Material.JUKEBOX)) {
            return;
        }
        Player player = event.getPlayer();
        Location loc = block.getLocation();
        if (!Utility.isReinforced(loc)) {
            player.sendMessage(ChatColor.YELLOW + "You've placed a jukebox; reinforce it to register it as a snitch.");

            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void reinforceSnitchBlock(CreateReinforcementEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (!block.getType().equals(Material.JUKEBOX)) {
            return;
        }
        System.out.println("Snitch Reinforced");
        Player player = event.getPlayer();
        Location loc = block.getLocation();
        AccessDelegate access = AccessDelegate.getDelegate(block);
        IReinforcement rei = event.getReinforcement();
        if (rei instanceof PlayerReinforcement) {
            PlayerReinforcement reinforcement = (PlayerReinforcement) rei;
            Faction owner = reinforcement.getOwner();
            if (reinforcement.getSecurityLevel().equals(SecurityLevel.GROUP)) {
            	Snitch snitch;
                if(snitchManager.getSnitch(loc.getWorld(), loc) != null) {
                	snitch = snitchManager.getSnitch(loc.getWorld(), loc);
                	plugin.getJaLogger().updateSnitchGroup(snitchManager.getSnitch(loc.getWorld(), loc), owner.getName());
                	snitchManager.removeSnitch(snitch);
                	snitch.setGroup(owner);
                } else {
                	snitch = new Snitch(loc, owner);
                    plugin.getJaLogger().logSnitchPlace(player.getWorld().getName(), owner.getName(), "", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    snitch.setId(plugin.getJaLogger().getLastSnitchID());
                    plugin.getJaLogger().increaseLastSnitchID();
                }
            	snitchManager.addSnitch(snitch);
                
                player.sendMessage(ChatColor.AQUA + "You've created a snitch block registered to the group " + owner.getName() + ".  To name your snitch, type /janame.");
            } else {
            	Snitch snitch;
                if(snitchManager.getSnitch(loc.getWorld(), loc) != null) {
                	snitch = snitchManager.getSnitch(loc.getWorld(), loc);
                	plugin.getJaLogger().updateSnitchGroup(snitchManager.getSnitch(loc.getWorld(), loc), owner.getFounder());
                	snitchManager.removeSnitch(snitch);
                	snitch.setGroup(owner);
                } else {
                	snitch = new Snitch(loc, owner);
                    plugin.getJaLogger().logSnitchPlace(player.getWorld().getName(), owner.getFounder(), "", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                    snitch.setId(plugin.getJaLogger().getLastSnitchID());
                    plugin.getJaLogger().increaseLastSnitchID();
                }
            	snitchManager.addSnitch(snitch);
                
                player.sendMessage(ChatColor.AQUA + "You've created a private snitch block; reinforce it with a group to register members.  To name your snitch, type /janame.");
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void breakSnitchBlock(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (!block.getType().equals(Material.JUKEBOX)) {
            return;
        }
        Location loc = block.getLocation();
        if(snitchManager.getSnitch(loc.getWorld(), loc) != null) {
        	snitchManager.removeSnitch(snitchManager.getSnitch(loc.getWorld(), loc));
        	plugin.getJaLogger().logSnitchBreak(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void enterSnitchProximity(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()
                && from.getWorld().equals(to.getWorld())) {
            // Player didn't move by at least one block.
            return;
        }
        Player player = event.getPlayer();
        String playerName = player.getName();
        Location location = player.getLocation();
        World world = location.getWorld();
        Set<Snitch> inList = playersInSnitches.get(playerName);
        if (inList == null) {
            inList = new TreeSet<Snitch>();
            playersInSnitches.put(player.getName(), inList);
        }
        Set<Snitch> snitches = snitchManager.findSnitches(world, location);
        for (Snitch snitch : snitches) {
            if (!JukeAlert.isOnSnitch(snitch, playerName)) {
                if (!inList.contains(snitch)) {
                    inList.add(snitch);
                    for (Player remoteplayer : playerManager.getPlayers()) {
                        if (snitch.getGroup().isMember(remoteplayer.getName()) || snitch.getGroup().isFounder(remoteplayer.getName()) || snitch.getGroup().isModerator(remoteplayer.getName())) {
                            remoteplayer.sendMessage(ChatColor.AQUA + " * " + playerName + " entered snitch at " + snitch.getName() + " [" + snitch.getX() + " " + snitch.getY() + " " + snitch.getZ() + "]");
                        }
                    }
                    plugin.getJaLogger().logSnitchEntry(snitch, location, player);
                }
            }
        }
        Set<Snitch> rmList = new TreeSet<Snitch>();
        for (Snitch snitch : inList) {
            if (snitches.contains(snitch)) {
                continue;
            }
            rmList.add(snitch);
        }
        inList.removeAll(rmList);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerKillEntity(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity killer = entity.getKiller();
        if (entity instanceof Player) {
            return;
        }
        if (!(killer instanceof Player)) {
            return;
        }
        Player player = (Player) killer;
        Set<Snitch> snitches = snitchManager.findSnitches(player.getWorld(), player.getLocation());
        for (Snitch snitch : snitches) {
            if (!JukeAlert.isOnSnitch(snitch, player.getName())) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchEntityKill(snitch, player, entity);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerKillPlayer(PlayerDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }
        Player killed = event.getEntity();
        Player killer = killed.getKiller();
        Set<Snitch> snitches = snitchManager.findSnitches(killed.getWorld(), killed.getLocation());
        for (Snitch snitch : snitches) {
            if (!JukeAlert.isOnSnitch(snitch, killer.getName())) {
                if (checkProximity(snitch, killed.getName()) || checkProximity(snitch, killer.getName())) {
                    plugin.getJaLogger().logSnitchPlayerKill(snitch, killer, killed);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (!JukeAlert.isOnSnitch(snitch, player.getName())) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchIgnite(snitch, player, block);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBurnEvent(BlockBurnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (snitch.getGroup() != null) {
                continue;
            }
            plugin.getJaLogger().logSnitchBlockBurn(snitch, block);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerBreakBlock(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (!JukeAlert.isOnSnitch(snitch, player.getName())) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchBlockBreak(snitch, player, block);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerPlaceBlock(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (!JukeAlert.isOnSnitch(snitch, player.getName())) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchBlockPlace(snitch, player, block);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerFillBucket(PlayerBucketFillEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlockClicked();
        Player player = event.getPlayer();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (!JukeAlert.isOnSnitch(snitch, player.getName())) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchBucketFill(snitch, player, block);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerEmptyBucket(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlockClicked();
        Player player = event.getPlayer();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (!JukeAlert.isOnSnitch(snitch, player.getName())) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchBucketEmpty(snitch, player, block.getLocation(), player.getItemInHand());
                }
            }
        }
    }
}
