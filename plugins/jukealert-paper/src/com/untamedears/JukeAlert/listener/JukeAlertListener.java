package com.untamedears.JukeAlert.listener;

import java.util.List;
import java.util.Map;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.Utility;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import org.bukkit.Bukkit;
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
                snitchManager.addSnitch(snitch);
                player.sendMessage(ChatColor.AQUA + "You've created a snitch block registered to the group " + owner.getName() + ".");
            } else {
                plugin.getJaLogger().logSnitchPlace(player.getWorld().getName(), "p:" + owner.getFounder(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                player.sendMessage(ChatColor.AQUA + "You've created a private snitch block; reinforce it with a group to register others.");
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void breakSnitchBlock(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (!block.getType().equals(Material.JUKEBOX)) {
            return;
        }
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        plugin.getJaLogger().logSnitchBreak(player.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        //TODO: Make sure this is 100% complete. Also make it remove from the List in JukeAlert.java
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void enterSnitchProximity(PlayerMoveEvent event) {		
		Location from = event.getFrom();
		Location to   = event.getTo();
		
		 if (from.getBlockX() == to.getBlockX()
				 && from.getBlockY() == to.getBlockY()
	             && from.getBlockZ() == to.getBlockZ()
	             && from.getWorld().equals(to.getWorld())) 
		 {
			 // Player didn't move by at least one block.
			 return;
	     }
		 
        //TODO: Add/remove players to/from the JukeAlertSnitch's list and notify the players who own the snitch if they have entered.
            /*
         * Pseudo Code (Code that wont just work if copy and pasted but gives a general idea of what we want)
         * Location loc = event.getPlayer().getLocation();
         * for (JukeAlertSnitch snitch : listOSnitches) {
         *      if (snitch.isWithinCuboid(loc)) {
         *          snitch.add(event.getPlayer().getName();
         *      }
         * }
         */
		 
		 /*Player player = event.getPlayer();		 
		 Location location = player.getLocation();
		 World world = location.getWorld();
		 
		 List<Snitch> snitches = snitchManager.getSnitchesByWorld(world);
		 for(Snitch snitch : snitches) {
			 if(snitch.isWithinCuboid(location)) {
				 snitch.add(player.getName());
			 }
		 }*/
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
            //TODO
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerKillPlayer(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            //TODO: Put stuff here
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerBreakBlock(BlockPlaceEvent event) {
        //TODO: Put stuff here
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerPlaceBlock(BlockPlaceEvent event) {
        //TODO: Put stuff here
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerFillBucket(PlayerBucketFillEvent event) {
        //TODO: Put stuff here
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerEmptyBucket(PlayerBucketEmptyEvent event) {
        //TODO: Put stuff here
    }
}
