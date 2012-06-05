package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand extends AbstractCommand {
	
	public InfoCommand(RandomSpawn instance){
		super(instance, "info");
	}
	
	public boolean onCommand(CommandSender sender, List<String> args){
		
		Player player = (Player)sender;
		String worldname = player.getWorld().getName(); 
		
		player.sendMessage(ChatColor.WHITE +" ---------------- " + ChatColor.AQUA + "Random Spawn Info" + ChatColor.WHITE + " ------------------ ");
		player.sendMessage(ChatColor.AQUA + "World: " + ChatColor.WHITE + worldname );
		if (!(plugin.yamlHandler.worlds.contains(worldname))){
			player.sendMessage("Is not configured in Random Spawn ");
			player.sendMessage(ChatColor.WHITE +" --------------------------------------------------- ");
			return true;
		}
		
		String flags = "";
		
		for(String flag : plugin.yamlHandler.worlds.getStringList(worldname + ".randomspawnon")){
			flags += flag + ", ";
		}
		
		player.sendMessage(ChatColor.AQUA + "Random spawn on: " + ChatColor.WHITE + flags);

		if(plugin.yamlHandler.worlds.contains(worldname + ".spawnarea")){
			player.sendMessage(ChatColor.AQUA + "Spawnarea:");
			
			int xmin = plugin.yamlHandler.worlds.getInt(worldname +".spawnarea.x-min");
			int xmax = plugin.yamlHandler.worlds.getInt(worldname +".spawnarea.x-max");
			int zmin = plugin.yamlHandler.worlds.getInt(worldname +".spawnarea.z-min");
			int zmax = plugin.yamlHandler.worlds.getInt(worldname +".spawnarea.z-max");
			
			player.sendMessage("x-min = "+ xmin + "  |  x-max = " + xmax);
			player.sendMessage("z-min = "+ zmin + "  |  z-max = " + zmax);
		}else
		{
			player.sendMessage("There is no spawn area set. Refering to defaults:");
			player.sendMessage("x-min = -100 | x-max = 100");
			player.sendMessage("z-min = -100 | z-max = 100");
		}
		
		player.sendMessage(ChatColor.AQUA + "keepspawns: " + ChatColor.WHITE + plugin.yamlHandler.worlds.getBoolean(worldname + ".keeprandomspawns", false));
			
		if(plugin.yamlHandler.worlds.contains(worldname + ".firstspawn")){
			player.sendMessage(ChatColor.AQUA + "Firstspawn:");
			
			int x = plugin.yamlHandler.worlds.getInt(worldname +".firstspawn.x");
			int y = plugin.yamlHandler.worlds.getInt(worldname +".firstspawn.y");
			int z = plugin.yamlHandler.worlds.getInt(worldname +".firstspawn.z");
			double yaw = plugin.yamlHandler.worlds.getInt(worldname +".firstspawn.yaw");
			double pitch = plugin.yamlHandler.worlds.getInt(worldname +".firstspawn.pitch");
			
			player.sendMessage("x = "+ x + "  |  y = " + y + "  |  z = " + z + "  |  yaw = " + yaw + "  |  pitch = " + pitch);
		}else
		{
			player.sendMessage("There is no first spawn point set. Refering to worldspawn.");
		}
		
		player.sendMessage(ChatColor.WHITE +" --------------------------------------------------- ");
					
		return true;
	}
}
