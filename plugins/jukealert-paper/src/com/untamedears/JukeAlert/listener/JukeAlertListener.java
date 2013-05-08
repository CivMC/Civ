package com.untamedears.JukeAlert.listener;

import java.util.List;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.Utility;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class JukeAlertListener implements Listener {

    private JukeAlert plugin = JukeAlert.getInstance();
    SnitchManager snitchManager = plugin.getSnitchManager();

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
        AccessDelegate access = AccessDelegate.getDelegate(block);
        IReinforcement rei = access.getReinforcement();
        if (rei instanceof PlayerReinforcement) {
            PlayerReinforcement reinforcement = (PlayerReinforcement) rei;
            Faction owner = reinforcement.getOwner();
            if (reinforcement.getSecurityLevel().equals(SecurityLevel.GROUP)) {
                plugin.getJaLogger().logSnitchPlace(player.getWorld().getName(), owner.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                Snitch snitch = new Snitch(loc, owner);
                snitch.setId(plugin.getJaLogger().getLastSnitchID()+1);
                plugin.getJaLogger().increaseLastSnitchID();
                snitchManager.addSnitch(snitch);
                player.sendMessage(ChatColor.AQUA + "You've created a snitch block registered to the group " + owner.getName() + ".");
            } else {
                plugin.getJaLogger().logSnitchPlace(player.getWorld().getName(), "p:" + owner.getFounder(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                Snitch snitch = new Snitch(loc, owner);
                snitch.setId(plugin.getJaLogger().getLastSnitchID()+1);
                plugin.getJaLogger().increaseLastSnitchID();
                snitchManager.addSnitch(snitch);
                player.sendMessage(ChatColor.AQUA + "You've created a private snitch block; reinforce it with a group to register others.");
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
        plugin.getJaLogger().logSnitchBreak(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        snitchManager.removeSnitch(snitchManager.getSnitch(loc.getWorld(), loc));
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
        Location location = player.getLocation();
        World world = location.getWorld();

        List<Snitch> snitches = snitchManager.getSnitchesByWorld(world);
        for (Snitch snitch : snitches) {
            if (!snitch.getGroup().isMember(player.getName()) && !snitch.getGroup().isFounder(player.getName()) && !snitch.getGroup().isModerator(player.getName())) {
	            if (snitch.isWithinCuboid(location)) {
	                if(!snitch.checkProximity(player.getName())) {
	                	snitch.add(player.getName());
	                    plugin.getJaLogger().logSnitchEntry(snitch, location, player);
	                }
	            } else if(snitch.checkProximity(player.getName())) {
	            	snitch.remove(player.getName());
	            }
            }
        }
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
        List<Snitch> snitches = snitchManager.getSnitchesByWorld(player.getWorld());
        for (Snitch snitch : snitches) {
            if (!snitch.getGroup().isMember(player.getName()) && !snitch.getGroup().isFounder(player.getName()) && !snitch.getGroup().isModerator(player.getName())) {
	            if (snitch.checkProximity(player.getName())) {
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

        List<Snitch> snitches = snitchManager.getSnitchesByWorld(killer.getWorld());
        for (Snitch snitch : snitches) {
        	if (!snitch.getGroup().isMember(killer.getName()) && !snitch.getGroup().isFounder(killer.getName()) && !snitch.getGroup().isModerator(killer.getName())) {
	            if (snitch.checkProximity(killer.getName())) {
	                plugin.getJaLogger().logSnitchPlayerKill(snitch, killer, killed);
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
        List<Snitch> snitches = snitchManager.getSnitchesByWorld(block.getWorld());
        for (Snitch snitch : snitches) {
            if (!snitch.isWithinCuboid(block)) {
                continue;
            }
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
        List<Snitch> snitches = snitchManager.getSnitchesByWorld(player.getWorld());
        for (Snitch snitch : snitches) {
        	if (!snitch.getGroup().isMember(player.getName()) && !snitch.getGroup().isFounder(player.getName()) && !snitch.getGroup().isModerator(player.getName())) {
	            if (snitch.checkProximity(player.getName())) {
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
        List<Snitch> snitches = snitchManager.getSnitchesByWorld(player.getWorld());
        for (Snitch snitch : snitches) {
        	if (!snitch.getGroup().isMember(player.getName()) && !snitch.getGroup().isFounder(player.getName()) && !snitch.getGroup().isModerator(player.getName())) {
	            if (snitch.checkProximity(player.getName())) {
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
        List<Snitch> snitches = snitchManager.getSnitchesByWorld(player.getWorld());
        for (Snitch snitch : snitches) {
        	if (!snitch.getGroup().isMember(player.getName()) && !snitch.getGroup().isFounder(player.getName()) && !snitch.getGroup().isModerator(player.getName())) {
	            if (snitch.checkProximity(player.getName())) {
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
        List<Snitch> snitches = snitchManager.getSnitchesByWorld(player.getWorld());
        for (Snitch snitch : snitches) {
        	if (!snitch.getGroup().isMember(player.getName()) && !snitch.getGroup().isFounder(player.getName()) && !snitch.getGroup().isModerator(player.getName())) {
	            if (snitch.checkProximity(player.getName())) {
	                plugin.getJaLogger().logSnitchBucketEmpty(snitch, player, block.getLocation(), player.getItemInHand());
	            }
            }
        }
    }
}
