package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BedsCommand extends AbstractCommand{
	
	public BedsCommand(RandomSpawn instance){
		super(instance,"usebeds");
	}
		
	public boolean onCommand(CommandSender sender, List<String> args){
		
		Player player = (Player) sender;
		String worldName = player.getWorld().getName();
		
		List<String> randomSpawnFlags = plugin.yamlHandler.worlds.getStringList(worldName + ".randomspawnon");
		
		if (args.size() == 0){
			if (randomSpawnFlags.contains("bedrespawn")){
				randomSpawnFlags.remove("bedrespawn");
				plugin.yamlHandler.worlds.set(worldName + ".randomspawnon", randomSpawnFlags);
				plugin.playerInfo((Player)sender, "Beds will now work like normal.");				
				plugin.yamlHandler.saveWorlds();
				return true;
			}else{
				randomSpawnFlags.add("bedrespawn");
				plugin.yamlHandler.worlds.set(worldName + ".randomspawnon", randomSpawnFlags);
				plugin.playerInfo((Player)sender, "Beds are now disabled.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
		}
		
		if (args.size() == 1){
			if (args.get(0).matches("true")){
				randomSpawnFlags.add("bedrespawn");
				plugin.yamlHandler.worlds.set(worldName + ".randomspawnon", randomSpawnFlags);
				plugin.playerInfo((Player)sender, "Beds are now disabled.");				
				plugin.yamlHandler.saveWorlds();
				return true;
			}
			if (args.get(0).matches("false")){
				randomSpawnFlags.remove("bedrespawn");
				plugin.yamlHandler.worlds.set(worldName + ".randomspawnon", randomSpawnFlags);
				plugin.playerInfo((Player)sender, "Beds will now work like normal.");
				plugin.yamlHandler.saveWorlds();
				return true;
			}
			
		}
		return false;
	}
}
