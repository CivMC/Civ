package me.josvth.randomspawn.commands;

import java.util.List;

import me.josvth.randomspawn.RandomSpawn;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddSpawnPointCommand extends AbstractCommand{
	
	public AddSpawnPointCommand(RandomSpawn instance){
		super(instance,"addspawn");
	}
		
	public boolean onCommand(CommandSender sender, List<String> args){
		if (!(sender instanceof Player)) {
			sender.sendMessage("For console users, please manually add spawn points to the worlds.yml and reload.");
			return false;
		}

		Player player = (Player)sender;

		String worldname = player.getWorld().getName();
		
		double x = player.getLocation().getX();
		double y = player.getLocation().getY();
		double z = player.getLocation().getZ();

		if (args.length < 2) { // failure

		} else if (args.length == 2) { // 

		plugin.yamlHandler.worlds.set(worldname+".firstspawn.x", x);
		plugin.yamlHandler.worlds.set(worldname+".firstspawn.y", y);
		plugin.yamlHandler.worlds.set(worldname+".firstspawn.z", z);
		
		plugin.yamlHandler.worlds.set(worldname+".randomspawnonfirstjoin", false);
		
		plugin.yamlHandler.saveWorlds();
		
		plugin.playerInfo(player, "First spawn location set!");
		plugin.playerInfo(player, "Random spawning on first join is now disabled!");
		
		return true;
	}
}

