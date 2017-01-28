/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.command;

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
        Integer timer = null;
        TimerOperation timerOperation = null;

        if(Objects.equal(command.getName(), "gear")) {
        	mode = CommandMode.CREATE;
        } else if(Objects.equal(command.getName(), "link")) {
        	mode = CommandMode.LINK;
        } else if(args.length > 0 && Objects.equal(args[0], "timer")) {
        	if(args.length > 3) return false;

    		mode = CommandMode.TIMER;

    		timer = args.length > 1
    				? parseTimer(args[1], player)
    				: (Integer)CastleGates.getConfigManager().getTimerDefault();

    		if(timer == null) {
    			return true;
    		}

    		timerOperation = args.length == 3
    				? Helper.parseTimerOperation(args[2])
    				: CastleGates.getConfigManager().getTimerDefaultOperation();

			if(timerOperation == null) {
				player.sendMessage(ChatColor.RED + "Allowed timer operations are draw, undraw or revert.");
				return true;
			}
    	} else if (args.length != 1) {
    		return false;
    	}
    	else {
	        switch(args[0].toLowerCase()) {
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
    	}

		CastleGates.getManager().setPlayerMode((Player) sender, mode, timer, timerOperation);

		return true;
	}

	private static Integer parseTimer(String text, Player player) {
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
