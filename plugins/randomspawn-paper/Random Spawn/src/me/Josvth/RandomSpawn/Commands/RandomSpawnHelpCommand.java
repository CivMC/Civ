package me.Josvth.RandomSpawn.Commands;


import java.util.List;

import me.Josvth.RandomSpawn.RandomSpawnCommandExecutor;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomSpawnHelpCommand extends RandomSpawnCommandExecutor{
	
	public RandomSpawnHelpCommand(){
		this.name = "help";
	}
	
	public boolean onCommand(CommandSender sender, List<String> args){

		String version = plugin.getDescription().getVersion();
		Player player = (Player)sender;

		if (args.size() == 0 || (args.size() == 1 && args.get(0).matches("1"))){

			player.sendMessage(ChatColor.WHITE +" ---------------- " + ChatColor.AQUA + "Random Spawn ver: " + version + ChatColor.WHITE + " ----------------");
			player.sendMessage(ChatColor.AQUA + "PAGE 1 of 3");
			player.sendMessage("");
			player.sendMessage(ChatColor.AQUA + "/rs help (1/2/3)  " + ChatColor.WHITE + "Shows this screen." );
			player.sendMessage(ChatColor.AQUA + "/rs reload  " + ChatColor.WHITE + "Reloads the Random Spawn config files." );
			player.sendMessage(ChatColor.AQUA + "/rs info  " + ChatColor.WHITE + "Shows the Random Spawn settings for this world." );
			player.sendMessage(ChatColor.AQUA + "/rs enable  " + ChatColor.WHITE + "Enables Random Spawn in this world." );
			player.sendMessage(ChatColor.AQUA + "/rs disable  " + ChatColor.WHITE + "Disables Random Spawn in this world." );
			player.sendMessage("");
			player.sendMessage(ChatColor.WHITE +" -------------------------------------------------- ");

			return true;
		}
		if (args.get(0).matches("2")){

			player.sendMessage(ChatColor.WHITE +" ---------------- " + ChatColor.AQUA + "Random Spawn ver: " + version + ChatColor.WHITE + " ----------------");
			player.sendMessage(ChatColor.AQUA + "PAGE 2 of 3");
			player.sendMessage("");
			player.sendMessage(ChatColor.AQUA + "/rs setarea  " + ChatColor.WHITE + "Sets the Random Spawn area." );
			player.sendMessage(ChatColor.AQUA + "/rs usebeds  " + ChatColor.WHITE + "Toggles the use beds setting." );
			player.sendMessage(ChatColor.AQUA + "/rs keeprandomspawns  " + ChatColor.WHITE + "Toggles spawn saving." );
			player.sendMessage(ChatColor.AQUA + "/rs firstjoin  " + ChatColor.WHITE + "Toggles randomspawning on first join." );
			player.sendMessage("");
			player.sendMessage("");
			player.sendMessage(ChatColor.WHITE +" -------------------------------------------------- ");

			return true;
		}
		if (args.get(0).matches("3")){

			player.sendMessage(ChatColor.WHITE +" ---------------- " + ChatColor.AQUA + "Random Spawn ver: " + version + ChatColor.WHITE + " ----------------");
			player.sendMessage(ChatColor.AQUA + "PAGE 3 of 3");
			player.sendMessage("");
			player.sendMessage(ChatColor.AQUA + "/rs setfirstspawn  ");
			player.sendMessage(ChatColor.WHITE + "Sets the first spawn location for this world.");
			player.sendMessage(ChatColor.AQUA + "/rs tpfirstspawn  ");
			player.sendMessage(ChatColor.WHITE + "Teleports you to the first spawn location of this world." );
			player.sendMessage(ChatColor.AQUA + "/rs unsetfirstspawn  ");
			player.sendMessage(ChatColor.WHITE + "Unsets the first spawn location for this world." );
			player.sendMessage(ChatColor.WHITE +" -------------------------------------------------- ");

			return true;
		}
		return false;
	}
}
