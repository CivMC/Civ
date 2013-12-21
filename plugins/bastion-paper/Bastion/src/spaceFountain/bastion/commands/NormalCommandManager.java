package spaceFountain.bastion.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NormalCommandManager implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(!(sender instanceof Player))
			return false;
		PlayersStates.setModeForPlayer((Player) sender, PlayersStates.Mode.NORMAL);
		sender.sendMessage(ChatColor.GREEN+"NORMAL mode");
		return true;
	}

}
