package com.github.maxopoly.KiraBukkitGateway.rabbit.request;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.KiraUtil;
import com.github.maxopoly.KiraBukkitGateway.impersonation.PseudoConsoleSender;
import com.google.gson.JsonObject;

public class ConsoleCommandHandler extends AbstractRequestHandler {

	public ConsoleCommandHandler() {
		super("consolemessageop", true);
	}

	@Override
	public void handle(JsonObject input, JsonObject output, long channelId) {
		UUID sender = UUID.fromString(input.get("sender").getAsString());
		String command = input.get("command").getAsString();
		OfflinePlayer player = Bukkit.getOfflinePlayer(sender);
		Logger logger = KiraBukkitGatewayPlugin.getInstance().getLogger();
		if (player == null) {
			logger.warning("Console player with uuid " + sender + " does not exist");
			output.addProperty("replymsg", "You are not a known player on the server");
			sendRequestSessionReply(output);
			return;
		}
		if (!player.isOp()) {
			logger.warning("Non op player " + sender + " tried to run console command");
			output.addProperty("replymsg", "You are not op");
			sendRequestSessionReply(output);
			return;
		}
		PseudoConsoleSender console = new PseudoConsoleSender(sender, Bukkit.getConsoleSender(), channelId);
		Bukkit.getScheduler().runTask(KiraBukkitGatewayPlugin.getInstance(), () -> {
			Bukkit.getServer().dispatchCommand(console, command);
			StringBuilder sb = new StringBuilder();
			for (String s : console.getRepliesAndFinish()) {
				sb.append(KiraUtil.cleanUp(s));
				sb.append('\n');
			}
			output.addProperty("replymsg", sb.toString());
			sendRequestSessionReply(output);
		});
	}
}
