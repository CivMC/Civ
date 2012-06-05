package me.Josvth.RandomSpawn.Commands;

import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawn;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisableCommand extends AbstractCommand{
	
	public DisableCommand(RandomSpawn instance){
		super(instance,"disable");
	}
	
	public boolean onCommand(CommandSender sender, List<String> args){
		Player player = (Player) sender;
		String worldname = player.getWorld().getName();
		plugin.yamlHandler.worlds.set(worldname + ".randomspawnon", null);
		plugin.yamlHandler.saveWorlds();
		plugin.playerInfo(player, "Random Spawn is now disabled in this world!");
		return true;
	}
}
