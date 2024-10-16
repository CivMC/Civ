package me.josvth.randomspawn.commands;


import java.util.List;
import me.josvth.randomspawn.RandomSpawn;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HelpCommand extends AbstractCommand{

	public HelpCommand(RandomSpawn instance){
		super(instance, "help");
	}

	public boolean onCommand(CommandSender sender, List<String> args){

		Player player = (Player)sender;

		player.sendMessage(ChatColor.WHITE +" ------------------- " + ChatColor.AQUA + "Random Spawn" + ChatColor.WHITE + " ------------------- ");
		player.sendMessage("");
		player.sendMessage(ChatColor.AQUA + "/rs help  	   " + ChatColor.WHITE + "Shows this screen." );
		player.sendMessage(ChatColor.AQUA + "/rs reload    " + ChatColor.WHITE + "Reloads the Random Spawn config files." );
		player.sendMessage(ChatColor.AQUA + "/rs info      " + ChatColor.WHITE + "Shows the Random Spawn settings for this world." );
		player.sendMessage(ChatColor.AQUA + "/rs enable    " + ChatColor.WHITE + "Enables Random Spawn in this world." );
		player.sendMessage(ChatColor.AQUA + "/rs disable   " + ChatColor.WHITE + "Disables Random Spawn in this world." );
		player.sendMessage("");
		player.sendMessage(ChatColor.AQUA + "/rs setarea   " + ChatColor.WHITE + "Sets the Random Spawn area." );
		player.sendMessage(ChatColor.AQUA + "/rs usebeds   " + ChatColor.WHITE + "Toggles the use beds setting." );
		player.sendMessage(ChatColor.AQUA + "/rs kfs  	   " + ChatColor.WHITE + "Toggles spawn saving." );
		player.sendMessage(ChatColor.AQUA + "/rs firstjoin " + ChatColor.WHITE + "Toggles randomspawning on first join." );
		player.sendMessage("");
		player.sendMessage(ChatColor.AQUA + "/rs setfirstspawn ");
		player.sendMessage(ChatColor.WHITE + "Sets the first spawn location for this world.");
		player.sendMessage(ChatColor.AQUA + "/rs tpfirstspawn  ");
		player.sendMessage(ChatColor.WHITE + "Teleports you to the first spawn location of this world." );
		player.sendMessage(ChatColor.AQUA + "/rs unsetfirstspawn  ");
		player.sendMessage(ChatColor.WHITE + "Unsets the first spawn location for this world." );
		player.sendMessage("");
		player.sendMessage(ChatColor.WHITE +" ---------------------------------------------------- ");

		return true;
		
	}
}
