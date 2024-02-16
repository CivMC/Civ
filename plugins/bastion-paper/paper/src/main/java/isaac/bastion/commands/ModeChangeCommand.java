package isaac.bastion.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ModeChangeCommand implements CommandExecutor {
	
	PlayersStates.Mode mode;
	
	public ModeChangeCommand(PlayersStates.Mode mode){
		this.mode = mode;
	}

	// TODO: Add World Aware.
	@Override
	public boolean onCommand(CommandSender sender, Command command, String arg2,
			String[] arguments) {
		if (!(sender instanceof Player)) {
			return false;
		}
		
		PlayersStates.toggleModeForPlayer((Player) sender, mode);
		
		if (sender.hasPermission("Bastion.dev")) {
			if (arguments.length >= 3) {
				try {
					Integer x = Integer.parseInt(arguments[0]);
					Integer y = Integer.parseInt(arguments[1]);
					Integer z = Integer.parseInt(arguments[2]);
					Player player = (Player) sender;
					Bukkit.getServer().getPluginManager().callEvent(
							new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK,
							player.getInventory().getItemInMainHand(), new Location(player.getLocation().getWorld(),
							x, y - 1, z).getBlock(), BlockFace.UP ));
				} catch (NumberFormatException e) {
					sender.sendMessage("One of the arguments you provided was not a number");
				}
			}
		}
			
		return true;
	}

}
