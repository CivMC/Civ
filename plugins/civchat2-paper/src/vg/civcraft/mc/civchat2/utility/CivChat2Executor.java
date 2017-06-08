package vg.civcraft.mc.civchat2.utility;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;

public class CivChat2Executor implements CommandExecutor {

	private CivChat2 plugin;

	private CivChat2Config config;

	private CivChat2Log logger;

	public CivChat2Executor(CivChat2 instance) {

		this.plugin = instance;
		this.config = plugin.getPluginConfig();
		this.logger = CivChat2.getCivChat2Log();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		ChatColor sayChat = ChatColor.valueOf(config.getOpChatColor());
		String msgString = null;
		if (cmd.getName().equalsIgnoreCase("say")) {
			StringBuilder msg = new StringBuilder();
			for (int arg = 0; arg < args.length; arg++) {
				msg.append(args[arg]);
				msg.append(" ");
			}
			msgString = msg.toString();
			msg.delete(0, msg.length());
		}
		if (sender instanceof Player) {
			if (msgString == null) {
				return true;
			}
			Player player = (Player) sender;
			if (player.hasPermission("bukkit.command.say") || player.isOp()) {
				this.plugin.getServer().broadcastMessage(sayChat + "[Server] " + msgString);
				StringBuilder result = new StringBuilder();
				result.append("Player had good permissions, bukkit.command.say or OP");
				result.append(", ");
				result.append("Broadcasted message =[");
				result.append(msgString);
				result.append("] ChatColor = [");
				result.append(sayChat);
				result.append("]");
				debugMsg(result.toString());
				result.delete(0, result.length());
				return true;
			} else {
				player.sendMessage(ChatColor.RED + "You do not have permission to use that command");
				debugMsg("Player did not have proper permissions (not op)");
				return true;
			}
		} else if (sender instanceof ConsoleCommandSender) {
			this.plugin.getServer().broadcastMessage(sayChat + "[Server] " + msgString);
			StringBuilder result = new StringBuilder();
			result.append("Console sent the command");
			result.append(", ");
			result.append("Broadcasted message =[");
			result.append(msgString);
			result.append("] ChatColor = [");
			result.append(sayChat);
			result.append("]");
			debugMsg(result.toString());
			result.delete(0, result.length());
			return true;
		}
		debugMsg("Command was not a \"say\" command");
		return true;
	}

	private void debugMsg(String text) {

		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append(", ");
		sb.append(text);
		logger.debug(sb.toString());
		sb.delete(0, sb.length());
	}
}
