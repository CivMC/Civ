package com.github.maxopoly.KiraBukkitGateway.rabbit.request;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.impersonation.PseudoPlayer;
import com.google.gson.JsonObject;

public class IngameCommandHandler extends AbstractRequestHandler {

	public IngameCommandHandler() {
		super("ingame", true);
	}

	@Override
	public void handle(JsonObject input, JsonObject output, long channelId) {
		UUID runner = UUID.fromString(input.get("uuid").getAsString());
		String command = input.get("command").getAsString();
		Logger logger = KiraBukkitGatewayPlugin.getInstance().getLogger();
		logger.info("Running command '" + command + "' for " + runner.toString());
		Bukkit.getScheduler().runTask(KiraBukkitGatewayPlugin.getInstance(), () -> {
			PseudoPlayer player = new PseudoPlayer(runner, channelId);
			try {
				Bukkit.getServer().dispatchCommand(player, command);
			} catch (Exception e) {
				output.addProperty("reply", "You can not run this command from out of game");
				logger.warning("Failed to run command from external source: " + e.getMessage());
				e.printStackTrace();
				sendRequestSessionReply(output);
				return;
			}
			List<String> replies = player.collectReplies();
			StringBuilder sb = new StringBuilder();
			for (String reply : replies) {
				sb.append(reply);
				sb.append('\n');
			}
			output.addProperty("reply", sb.toString());
			sendRequestSessionReply(output);
		});

	}

}
