package me.Josvth.RandomSpawn.Listeners;

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

import me.Josvth.RandomSpawn.RandomSpawn;

public class SignListener implements Listener {
	RandomSpawn plugin;
	
	public SignListener(RandomSpawn instance) {
		plugin = instance;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
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
						
						Location spawnLocation = plugin.chooseSpawn(world);
						
						plugin.sendGround(player, spawnLocation);
						
						player.teleport(spawnLocation);
						
						if (plugin.yamlHandler.worlds.getBoolean(world.getName() + ".keeprandomspawns",false)){
							plugin.setPlayerSpawn(player, spawnLocation);
						}
						
					}else{
						plugin.playerInfo(player, "You don't have the permission to use this Random Spawn Sign!");
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
