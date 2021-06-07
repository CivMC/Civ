package com.github.maxopoly.KiraBukkitGateway.rabbit.in;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.impersonation.PseudoPlayer;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitInput;
import com.google.gson.JsonObject;
import java.util.UUID;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.namelayer.GroupManager;

public class GroupChatMessageHandler extends RabbitInput {

	public GroupChatMessageHandler() {
		super("sendgroupmessage");
	}

	@Override
	public void handle(final JsonObject input) {
		final var senderUUID = UUID.fromString(input.get("sender").getAsString());
		final var groupName = input.get("group").getAsString();
		final var message = input.get("message").getAsString();

		final var fakeSender = new PseudoPlayer(senderUUID, -1);
		final var logger = KiraBukkitGatewayPlugin.getInstance().getLogger();

		final var foundGroup = GroupManager.getGroup(groupName);
		if (foundGroup == null) {
			fakeSender.sendMessage("Could not find that group to send a message to.");
			logger.severe("Tried to send message to group \"" + groupName + "\", but it wasn't found");
			return;
		}
		if (foundGroup.isDisciplined()) {
			fakeSender.sendMessage("That group \"" + groupName + "\" is disciplined.");
			logger.severe("Tried to send message to group \"" + groupName + "\", but it's disciplined.");
			return;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(KiraBukkitGatewayPlugin.getInstance(), () -> {
			CivChat2.getInstance().getCivChat2Manager().sendGroupMsg(fakeSender, foundGroup, message);
		});
	}

}
