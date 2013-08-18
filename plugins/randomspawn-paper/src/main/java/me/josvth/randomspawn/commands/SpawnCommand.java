package me.josvth.randomspawn.commands;

import java.util.List;

import me.josvth.randomspawn.RandomSpawn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class SpawnCommand extends AbstractCommand {

	public SpawnCommand(RandomSpawn instance) {
		super(instance, "spawn");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {
		
		Player target = null;
		World world = null;
		
		if (args.size() == 0) {
			target = (Player) sender;
			world = target.getWorld();
		}
		
		if (args.size() == 2) {
			
			List<Player> players = plugin.getServer().matchPlayer(args.get(0));
			
			if ( players.isEmpty() ){
				sender.sendMessage("No player named: "+ args.get(0) +" found.");
				return true;
			}
			
			target = players.get(0);
			
			world = plugin.getServer().getWorld(args.get(1));
			
			if ( world == null){
				sender.sendMessage("No world named: "+ args.get(1) +" found.");
				return true;
			}
			
		}
		
		if (world == null || target == null) return false;
		
		Location spawn = plugin.chooseSpawn(world);
		
		target.teleport(spawn);
		
		target.setMetadata("lasttimerandomspawned", new FixedMetadataValue(plugin, System.currentTimeMillis()));
		
		if (plugin.yamlHandler.worlds.getBoolean(world.getName() + ".keeprandomspawns",false))
			target.setBedSpawnLocation(spawn);

		if (plugin.yamlHandler.config.getString("messages.randomspawned") != null)
			target.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.yamlHandler.config.getString("messages.randomspawned")));
				
		if(target != sender)
			sender.sendMessage("Player: "+ target.getName() + " was random teleported!");
		
		return true;
		
	}

}
