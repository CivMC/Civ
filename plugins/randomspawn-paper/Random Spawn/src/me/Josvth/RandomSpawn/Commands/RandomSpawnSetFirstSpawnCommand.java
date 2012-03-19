package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomSpawnSetFirstSpawnCommand extends RandomSpawnCommandExecutor{
	
	public RandomSpawnSetFirstSpawnCommand(){
		this.name = "setfirstspawn";
	}
		
	public boolean onCommand(CommandSender sender, List<String> args){
		Player player = (Player)sender;

		String worldname = player.getWorld().getName();
		
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();

		double yaw = (double)player.getLocation().getYaw();
		double pitch = (double)player.getLocation().getPitch();
		
		plugin.getYamlHandler().worlds.set(worldname+".firstspawn.x", x);
		plugin.getYamlHandler().worlds.set(worldname+".firstspawn.y", y);
		plugin.getYamlHandler().worlds.set(worldname+".firstspawn.z", z);
		plugin.getYamlHandler().worlds.set(worldname+".firstspawn.yaw", yaw);
		plugin.getYamlHandler().worlds.set(worldname+".firstspawn.pitch", pitch);
		
		plugin.getYamlHandler().worlds.set(worldname+".randomspawnonfirstjoin", false);
		
		plugin.getYamlHandler().saveWorlds();
		
		plugin.playerInfo(player, "First spawn location set!");
		plugin.playerInfo(player, "Random spawning on first join is now disabled!");
		
		return true;
	}
}
