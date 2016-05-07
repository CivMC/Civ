package me.josvth.randomspawn.commands;

import java.util.List;

import me.josvth.randomspawn.RandomSpawn;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

/**
 * Add a spawn point using current location and given radius / exclusion and requirements.
 * 
 * @author ProgrammerDan <programmerdan@gmail.com>
 *
 */
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
	
		double checkradius = 500d;
		double radius = 500d;
		double exclusion = 0d;
		
		boolean requireNearby = false;
		
		String name;

		///rs addspawn <radius> <exclusion> true/false <check radius> name
		if (args.size() < 5) { // failure
			return false;
		} else {
			try {
				radius = Double.parseDouble(args.get(0));
			} catch (NumberFormatException nfe ) {
				return false;
			}
			
			try {
				exclusion = Double.parseDouble(args.get(1));
			} catch (NumberFormatException nfe) {
				return false;
			}
			
			requireNearby = Boolean.parseBoolean(args.get(2));
			
			try {
				checkradius = Double.parseDouble(args.get(3));
			} catch(NumberFormatException nfe) {
				return false;
			}
			
			StringBuilder nameSB = new StringBuilder(args.get(4));
			for (int a = 5; a < args.size(); a++) {
				nameSB.append(" ").append(args.get(a));
			}
			name = nameSB.toString();
		}
		
		int nextKey = 0;
		ConfigurationSection current = plugin.yamlHandler.worlds.getConfigurationSection(worldname + ".spawnpoints");
		if (current == null) {
			current = plugin.yamlHandler.worlds.createSection(worldname + ".spawnpoints");
		} else {
			nextKey = current.getKeys(false).size();
		}
		
		ConfigurationSection spawnpoint = current.createSection(String.valueOf(nextKey));

		spawnpoint.set("name", name);
		spawnpoint.set("x", x);
		spawnpoint.set("y", y);
		spawnpoint.set("z", z);
		spawnpoint.set("checkradius", checkradius);
		spawnpoint.set("radius", radius);
		spawnpoint.set("exclusion", exclusion);
		spawnpoint.set("nearby", requireNearby);
		
		plugin.yamlHandler.saveWorlds();
		
		plugin.playerInfo(player, "Added spawn location "+ name);
		
		return true;
	}
}

