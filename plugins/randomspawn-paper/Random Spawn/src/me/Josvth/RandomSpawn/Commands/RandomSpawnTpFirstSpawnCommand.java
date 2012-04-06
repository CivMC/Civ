package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomSpawnTpFirstSpawnCommand extends RandomSpawnCommandExecutor{
	
	public RandomSpawnTpFirstSpawnCommand(){
		this.name = "tpfirstspawn";
	}
		
	@Override
	public boolean onCommand(CommandSender sender, List<String> args){
		
		Player player = (Player)sender;
		String worldname = player.getWorld().getName();
		
		if (plugin.yamlHandler.worlds.contains(worldname +".firstspawn")){
			
			double x = plugin.yamlHandler.worlds.getDouble(worldname+".firstspawn.x");
			double y = plugin.yamlHandler.worlds.getDouble(worldname+".firstspawn.y");
			double z = plugin.yamlHandler.worlds.getDouble(worldname+".firstspawn.z");
			
			double dyaw = plugin.yamlHandler.worlds.getDouble(worldname+".firstspawn.yaw");
			double dpitch = plugin.yamlHandler.worlds.getDouble(worldname+".firstspawn.pitch");
			
			float yaw = (float)dyaw;
			float pitch = (float)dpitch;
			
			Location firstSpawn = new Location(player.getWorld(),x,y,z,yaw,pitch);
							
			player.teleport(firstSpawn);
			
			plugin.playerInfo(player, "You've been teleported to the first spawn location of this world!");
		}else{
			plugin.playerInfo(player,  "There's no first spawnpoint set in this world!");
		}
		return true;
	}
}
