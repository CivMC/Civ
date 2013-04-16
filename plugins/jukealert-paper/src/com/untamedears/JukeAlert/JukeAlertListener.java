<<<<<<< HEAD
package com.untamedears.JukeAlert;

import com.untamedears.JukeAlert.sql.JukeAlertLogger;
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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class JukeAlertListener implements Listener {

	private JukeAlert ja;
	private JukeAlertLogger jal;
	public JukeAlertListener(JukeAlert ja) {
		this.ja = ja;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void placeSnitchBlock(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		if (!block.getType().equals(Material.JUKEBOX)) {
			return;
		}
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		if (Utility.isReinforced(loc)) {
			AccessDelegate access = AccessDelegate.getDelegate(block);
			IReinforcement rei = access.getReinforcement();
			if (rei instanceof PlayerReinforcement) {
				PlayerReinforcement reinforcement = (PlayerReinforcement) rei;
				Faction owner = reinforcement.getOwner();
				if (reinforcement.getSecurityLevel().equals(SecurityLevel.GROUP)) {
					ja.getJaLogger().logSnitchPlace(player.getWorld().getName(), owner.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
					player.sendMessage(ChatColor.AQUA + "You've created a snitch block registered to the group " + owner.getName() + ".");
				} else {
					ja.getJaLogger().logSnitchPlace(player.getWorld().getName(), "p:" + owner.getFounder(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
					player.sendMessage(ChatColor.AQUA + "You've created a private snitch block; Reinforce it with a group to register others.");
				}
			}

			ja.getJaLogger().logSnitchPlace(player.getWorld().getName(), "p:" + player.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			player.sendMessage(ChatColor.AQUA + "You've created a private snitch; reinforce it to add others to it.");
			return;
		}
		player.sendMessage(ChatColor.YELLOW + "You've placed a jukebox reinforce it to register it as a snitch.");
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
		ja.getJaLogger().logSnitchBreak(player.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		//TODO: Make sure this is 100% complete. Also make it remove from the List in JukeAlert.java
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void enterSnitchProximity(PlayerMoveEvent event) {
		Location loc= event.getPlayer().getLocation();
		double x=0,y=0,z=0;
		jal.updateCubiodSize(x, y, z);//gets snitch field.
		if (loc.getX() == x || loc.getY() == y || loc.getZ() == z){ //if snitch field and player have the same cords.
			// todo: send player message.
		}
		
		
		//TODO: Add/remove players to/from the JukeAlertSnitch's list and notify the player's who own the snitch if they have entered.
	}
	 
	//Registers the events in this to JukeAlert.java
	public void registerEvents() {
		Bukkit.getServer().getPluginManager().registerEvents(this, ja);
	}
}
=======
package com.untamedears.JukeAlert;

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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class JukeAlertListener implements Listener {

	private JukeAlert ja;

	public JukeAlertListener(JukeAlert ja) {
		this.ja = ja;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void placeSnitchBlock(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		if (!block.getType().equals(Material.JUKEBOX)) {
			return;
		}
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		if (Utility.isReinforced(loc)) {
			AccessDelegate access = AccessDelegate.getDelegate(block);
			IReinforcement rei = access.getReinforcement();
			if (rei instanceof PlayerReinforcement) {
				PlayerReinforcement reinforcement = (PlayerReinforcement) rei;
				Faction owner = reinforcement.getOwner();
				if (reinforcement.getSecurityLevel().equals(SecurityLevel.GROUP)) {
					ja.getJaLogger().logSnitchPlace(player.getWorld().getName(), owner.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
					player.sendMessage(ChatColor.AQUA + "You've created a snitch block registered to the group " + owner.getName() + ".");
				} else {
					ja.getJaLogger().logSnitchPlace(player.getWorld().getName(), "p:" + owner.getFounder(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
					player.sendMessage(ChatColor.AQUA + "You've created a private snitch block; Reinforce it with a group to register others.");
				}
			}

			ja.getJaLogger().logSnitchPlace(player.getWorld().getName(), "p:" + player.getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			player.sendMessage(ChatColor.AQUA + "You've created a private snitch; reinforce it to add others to it.");
			return;
		}
		player.sendMessage(ChatColor.YELLOW + "You've placed a jukebox reinforce it to register it as a snitch.");
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
		ja.getJaLogger().logSnitchBreak(player.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		//TODO: Make sure this is 100% complete. Also make it remove from the List in JukeAlert.java
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void enterSnitchProximity(PlayerMoveEvent event) {
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
	}
	 
	//Registers the events in this to JukeAlert.java
	public void registerEvents() {
		Bukkit.getServer().getPluginManager().registerEvents(this, ja);
	}
}
>>>>>>> 283796ff88b49865ceefb333e7872259208f978f
