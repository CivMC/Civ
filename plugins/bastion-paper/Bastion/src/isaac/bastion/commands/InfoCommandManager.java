package isaac.bastion.commands;

import isaac.bastion.commands.PlayersStates.Mode;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommandManager implements CommandExecutor{

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(!(sender instanceof Player))
			return false;
		PlayersStates.setModeForPlayer((Player) sender, Mode.INFO);
		sender.sendMessage(ChatColor.GREEN+"INFO mode");
		return true;
	}

}
