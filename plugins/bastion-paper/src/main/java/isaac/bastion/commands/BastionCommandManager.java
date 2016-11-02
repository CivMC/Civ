package isaac.bastion.commands;

import java.io.InputStream;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import isaac.bastion.Bastion;

public class BastionCommandManager implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(args.length>0){
			if(args[0].equalsIgnoreCase("License")){
				InputStream input = getClass().getResourceAsStream("/License.txt");
				sender.sendMessage(convertStreamToString(input));
				return true;
			} else if(args[0].equalsIgnoreCase("about")){
				sender.sendMessage(ChatColor.GREEN+"Bastion version "+Bastion.getPlugin().getDescription().getVersion());
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("resource")
	public static String convertStreamToString(InputStream is) {
		if (is == null) {
			return "";
		}
		Scanner s = new Scanner(is).useDelimiter("\\A");
		String ret = "";
		try {
			ret = s.hasNext() ? s.next() : "";
		} catch (IllegalStateException ise) {
			ret = "";
		} finally {
			s.close();
			ret = "";
		}
		return ret;
	}
}
