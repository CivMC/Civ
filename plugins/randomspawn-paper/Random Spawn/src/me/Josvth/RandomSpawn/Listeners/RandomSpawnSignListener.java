package me.Josvth.RandomSpawn.Listeners;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class RandomSpawnSignListener implements Listener {
RandomSpawn plugin;
	
	public RandomSpawnSignListener(RandomSpawn instance){
		this.plugin = instance;
		this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}
	
	@EventHandler
	public void onPlayerSignInteract(PlayerInteractEvent event){
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			Block block = event.getClickedBlock();
			if (block.getTypeId() == 68 || block.getTypeId() == 63){
				Sign sign = (Sign)block.getState();
				Player player = event.getPlayer();
				if (sign.getLine(0).equalsIgnoreCase("[RandomSpawn]")){
					if (player.hasPermission("RandomSpawn.usesign")){
						World world = player.getWorld();
						Location playerLocation = player.getLocation();
						
						Location skyLocation = new Location(playerLocation.getWorld(), playerLocation.getX(), +10000.0, playerLocation.getY());
						
						player.teleport(skyLocation);
															
						plugin.logDebug(player.getName() + " is teleported somewhere in the sky.");
						
						plugin.randomSpawnPlayer(player, world);
					}else{
						this.plugin.playerInfo(player, "You don't have the permission to use this Random Spawn Sign!");
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerSignPlace(SignChangeEvent event){
		if (event.getLine(0).equalsIgnoreCase("[RandomSpawn]")){
			Player player = event.getPlayer();
			if (player.hasPermission("RandomSpawn.placesign")){
				this.plugin.playerInfo(player, "Random Spawn Sign created!");
			}else{
				event.setLine(0, "");
				this.plugin.playerInfo(player, "You don't have the permission to place a Random Spawn Sign!");
			}
		}
	}
}
