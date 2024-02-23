package com.github.maxopoly.KiraBukkitGateway.rabbit.in;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitInput;
import com.google.gson.JsonObject;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SendMessageHandler extends RabbitInput {

	public SendMessageHandler() {
		super("sendmessage");
	}

	@Override
	public void handle(JsonObject input) {
		UUID target = UUID.fromString(input.get("receiver").getAsString());
		String msg = input.get("message").getAsString();
		Player p = Bukkit.getPlayer(target);
		if (p != null) {
			p.sendMessage(ChatColor.GOLD + msg);
		}
		else {
			//player is offline, so we use their discord PM as backup
			KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToUser(target, msg, -1);
		}
	}
}
