package com.github.maxopoly.KiraBukkitGateway.rabbit.in;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.PseudoPlayer;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitInput;
import com.google.gson.JsonObject;

public class IngameCommandHandler extends RabbitInput {

	public IngameCommandHandler() {
		super("ingame");
	}

	@Override
	public void handle(JsonObject input) {
		UUID runner = UUID.fromString(input.get("uuid").getAsString());
		String command = input.get("command").getAsString();
		Logger logger = KiraBukkitGatewayPlugin.getInstance().getLogger();
		logger.info("Running command '" + command +  "' for " + runner.toString());
		try {
			Bukkit.getServer().dispatchCommand(new PseudoPlayer(runner), command);

		} catch (Exception e) {
			logger.warning("Failed to run command from external source: "  + e.getMessage());
		}
	}
}
