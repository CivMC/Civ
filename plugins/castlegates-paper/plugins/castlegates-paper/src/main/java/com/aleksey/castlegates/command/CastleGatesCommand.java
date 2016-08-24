/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.types.CommandMode;
import com.google.common.base.Objects;

public class CastleGatesCommand {
	public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		
        CommandMode mode;
        
        if(Objects.equal(command.getName(), "gear")) {
        	mode = CommandMode.CREATE;
        } else if(Objects.equal(command.getName(), "link")) {
        	mode = CommandMode.LINK;
        } else {
	        if (args.length != 1) return false;
	        
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
		
		CastleGates.getManager().setPlayerMode((Player) sender, mode);
			
		return true;
	}
}
