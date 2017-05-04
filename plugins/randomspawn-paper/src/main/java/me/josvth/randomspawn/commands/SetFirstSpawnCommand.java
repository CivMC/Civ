package me.josvth.randomspawn.commands;

import java.util.List;

import me.josvth.randomspawn.RandomSpawn;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetFirstSpawnCommand extends AbstractCommand{
	
	public SetFirstSpawnCommand(RandomSpawn instance){
		super(instance,"setfirstspawn");
	}
		
	public boolean onCommand(CommandSender sender, List<String> args){
		Player player = (Player)sender;

		String worldname = player.getWorld().getName();
		
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();

		double yaw = (double)player.getLocation().getYaw();
		double pitch = (double)player.getLocation().getPitch();
		
		plugin.yamlHandler.worlds.set(worldname+".firstspawn.x", x);
		plugin.yamlHandler.worlds.set(worldname+".firstspawn.y", y);
		plugin.yamlHandler.worlds.set(worldname+".firstspawn.z", z);
		plugin.yamlHandler.worlds.set(worldname+".firstspawn.yaw", yaw);
		plugin.yamlHandler.worlds.set(worldname+".firstspawn.pitch", pitch);
		
		List<String> randomSpawnOn = plugin.yamlHandler.worlds.getStringList(worldname+".randomspawnon");
		if (randomSpawnOn != null && randomSpawnOn.contains("firstjoin")) {
			randomSpawnOn.remove("firstjoin");
			plugin.yamlHandler.worlds.set(worldname+".randomspawnon", randomSpawnOn);
		}
		List<String> spawnPointsOn = plugin.yamlHandler.worlds.getStringList(worldname+".spawnpointson");
		if (spawnPointsOn != null && spawnPointsOn.contains("firstjoin")) {
			spawnPointsOn.remove("firstjoin");
			plugin.yamlHandler.worlds.set(worldname+".spawnpointson", spawnPointsOn);
		}
		
		plugin.yamlHandler.saveWorlds();
		
		plugin.playerInfo(player, "First spawn location set!");
		plugin.playerInfo(player, "Random spawning on first join is now disabled!");
		
		return true;
	}
}
