package isaac.bastion.commands;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ModeChangeCommand implements CommandExecutor {
	
	PlayersStates.Mode mode;
	
	public ModeChangeCommand(PlayersStates.Mode mode){
		this.mode=mode;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2,
			String[] arg3) {
		if(!(sender instanceof Player))
			return false;
		PlayersStates.toggleModeForPlayer((Player) sender, mode);
		return true;
	}

}
