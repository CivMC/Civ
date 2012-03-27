package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomSpawnInfoCommand extends RandomSpawnCommandExecutor {
	
	public RandomSpawnInfoCommand(){
		this.name = "info";
	}
	
	public boolean onCommand(CommandSender sender, List<String> args){
		
		Player player = (Player)sender;
		String worldname = player.getWorld().getName(); 
		
		player.sendMessage(ChatColor.WHITE +" ------------------- " + ChatColor.AQUA + "Random Spawn" + ChatColor.WHITE + " ------------------- ");
		player.sendMessage(ChatColor.AQUA + "World: " + ChatColor.WHITE + worldname );
		if (!(plugin.getYamlHandler().worlds.contains(worldname))){
			player.sendMessage("Is not configured in Random Spawn ");
			player.sendMessage(ChatColor.WHITE +" ---------------------------------------------------- ");
			return true;
		}

		if(plugin.getYamlHandler().worlds.contains(worldname + ".randomspawnenabled")){
			if (plugin.getYamlHandler().worlds.getBoolean(worldname + ".randomspawnenabled")){
				player.sendMessage("Random spawning is ENABLED in this world");
			}else{
				player.sendMessage("Random spawning is DISABELD in this world");	
			}
		}

		if(plugin.getYamlHandler().worlds.contains(worldname + ".spawnarea")){
			player.sendMessage(ChatColor.AQUA + "Spawnarea:");
			
			int xmin = plugin.getYamlHandler().worlds.getInt(worldname +".spawnarea.x-min");
			int xmax = plugin.getYamlHandler().worlds.getInt(worldname +".spawnarea.x-max");
			int zmin = plugin.getYamlHandler().worlds.getInt(worldname +".spawnarea.z-min");
			int zmax = plugin.getYamlHandler().worlds.getInt(worldname +".spawnarea.z-max");
			
			player.sendMessage("x-min = "+ xmin + "  |  x-max = " + xmax + "  |  z-min = " + zmin + "  |  z-max = " + zmax);
		}else
		{
			player.sendMessage("");
			player.sendMessage("There is no spawn point set. Refering to defaults:");
			player.sendMessage("x-min = -100 | x-max = 100 | z-min = -100 | z-max = 100");
		}
		
		player.sendMessage(ChatColor.AQUA + "usebeds: "+ ChatColor.WHITE + plugin.getYamlHandler().worlds.getBoolean(worldname + ".usebeds", true) + 
				ChatColor.AQUA + "  keepspawns: " + ChatColor.WHITE + plugin.getYamlHandler().worlds.getBoolean(worldname + ".keeprandomspawns", false));
		player.sendMessage(ChatColor.AQUA + "firstjoin: " + ChatColor.WHITE + plugin.getYamlHandler().worlds.getBoolean(worldname + ".randomspawnonfirstjoin", false) +
				ChatColor.AQUA + "  worldchange: " + ChatColor.WHITE + plugin.getYamlHandler().worlds.getBoolean(worldname + ".randomspawnonworldchange", false)
				);
		
		if(plugin.getYamlHandler().worlds.contains(worldname + ".firstspawn")){
			player.sendMessage(ChatColor.AQUA + "Firstspawn:");
			
			int x = plugin.getYamlHandler().worlds.getInt(worldname +".firstspawn.x");
			int y = plugin.getYamlHandler().worlds.getInt(worldname +".firstspawn.y");
			int z = plugin.getYamlHandler().worlds.getInt(worldname +".firstspawn.z");
			double yaw = plugin.getYamlHandler().worlds.getInt(worldname +".firstspawn.yaw");
			double pitch = plugin.getYamlHandler().worlds.getInt(worldname +".firstspawn.pitch");
			
			player.sendMessage("x = "+ x + "  |  y = " + y + "  |  z = " + z + "  |  yaw = " + yaw + "  |  pitch = " + pitch);
		}else
		{
			player.sendMessage("There is no first spawn point set. Refering to worldspawn.");
		}
		
		player.sendMessage(ChatColor.WHITE +" ---------------------------------------------------- ");
					
		return true;
	}
}
