/**
 * Created by Aleksey on 14.07.2017.
 */

package isaac.bastion.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import isaac.bastion.Bastion;

public class GroupCommandManager implements CommandExecutor {
	public enum CommandType { Add, Delete, List }

	private CommandType type;

	public GroupCommandManager(CommandType type) {
		this.type = type;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))  return false;

		Player player = (Player)sender;

		if(this.type == CommandType.Add) {
			if(args.length != 2) return false;
			Bastion.getGroupManager().addAllowedGroup(player, args[0], args[1]);
		} else if(this.type == CommandType.Delete) {
			if(args.length != 2) return false;
			Bastion.getGroupManager().deleteAllowedGroup(player, args[0], args[1]);
		} else if(this.type == CommandType.List) {
			if(args.length != 1) return false;
			Bastion.getGroupManager().listAllowedGroups(player, args[0]);
		} else {
			return false;
		}

		return true;
	}

}
