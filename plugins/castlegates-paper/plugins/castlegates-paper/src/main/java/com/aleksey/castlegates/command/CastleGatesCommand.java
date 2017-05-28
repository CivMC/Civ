/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.command;

import com.aleksey.castlegates.types.TimerMode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.types.CommandMode;
import com.aleksey.castlegates.types.TimerOperation;
import com.aleksey.castlegates.utils.Helper;
import com.google.common.base.Objects;

public class CastleGatesCommand {
	public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		Player player = (Player)sender;

        CommandMode mode;

        if(command.getName().equalsIgnoreCase("gear")) {
        	mode = CommandMode.CREATE;
        } else if(command.getName().equalsIgnoreCase("link")) {
        	mode = CommandMode.LINK;
        } else {
	        switch(args[0].toLowerCase()) {
				case "timer":
					return setTimeMode(player, args);
		        case "normal":
		        	mode = CommandMode.NORMAL;
		        	break;
		        case "create":
		        	mode = CommandMode.CREATE;
		        	break;
		        case "link":
		        	mode = CommandMode.LINK;
		        	break;
		        case "info":
		        	mode = CommandMode.INFO;
		        	break;
		        default:
		        	return false;
	        }

			if (args.length != 1) return false;
    	}

		CastleGates.getManager().setPlayerMode(player, mode, null, null, null);

		return true;
	}

	private static boolean setTimeMode(Player player, String[] args) {
		if(args.length > 4) return false;

		Integer timer;
		TimerOperation timerOperation;
		TimerMode timerMode;

		if(args.length == 2 && args[1].equalsIgnoreCase("door")) {
			timer = 1;
			timerOperation = TimerOperation.UNDRAW;
			timerMode = TimerMode.DOOR;
		} else {
			timer = args.length > 1
					? parseTimerTimeout(args[1], player)
					: (Integer) CastleGates.getConfigManager().getTimerDefault();

			if (timer == null)  return true;

			timerOperation = CastleGates.getConfigManager().getTimerDefaultOperation();
			timerMode = TimerMode.DEFAULT;

			if(args.length > 2) {
				if(args.length == 4 || !args[2].equalsIgnoreCase("door")) {
					timerOperation = Helper.parseTimerOperation(args[2]);

					if (timerOperation == null) {
						player.sendMessage(ChatColor.RED + "Allowed timer operations are draw, undraw and revert.");
						return true;
					}
				}

				if(args.length == 4 || args[2].equalsIgnoreCase("door")) {
					if(args.length == 4) {
						if(!args[3].equalsIgnoreCase("door")) {
							player.sendMessage(ChatColor.RED + "Allowed timer mode is door.");
							return true;
						}
					} else {
						timerOperation = TimerOperation.UNDRAW;
					}

					timerMode = TimerMode.DOOR;
				}
			}
		}

		CastleGates.getManager().setPlayerMode(player, CommandMode.TIMER, timer, timerOperation, timerMode);

		return true;
	}

	private static Integer parseTimerTimeout(String text, Player player) {
		int timer;

		try {
			timer = Integer.parseInt(text);
		}
		catch(NumberFormatException ex) {
			return null;
		}

		if(timer < CastleGates.getConfigManager().getTimerMin()
				|| timer > CastleGates.getConfigManager().getTimerMax()
				)
		{
			String errorMessage = "Timer interval is limited to the value between "
					+ CastleGates.getConfigManager().getTimerMin()
					+ " and "
					+ CastleGates.getConfigManager().getTimerMax()
					+ " sec";

			player.sendMessage(ChatColor.RED + errorMessage);

			return null;
		}

		return timer;
	}
}
