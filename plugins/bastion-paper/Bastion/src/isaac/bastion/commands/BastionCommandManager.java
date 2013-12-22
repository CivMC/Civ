package isaac.bastion.commands;

import java.io.InputStream;
import java.util.Scanner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BastionCommandManager implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(args.length>0){
			if(args[0].equalsIgnoreCase("License")){
				InputStream input = getClass().getResourceAsStream("/License.txt");
				sender.sendMessage(convertStreamToString(input));
				return true;
			}
		}
		return false;
	}
	public static String convertStreamToString(InputStream is) {
	    Scanner s = new Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}
