package com.github.maxopoly.KiraBukkitGateway.rabbit.in;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitInput;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.UUID;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class RequestRelayCreationHandler extends RabbitInput {

	public RequestRelayCreationHandler() {
		super("requestrelaycreation");
	}

	@Override
	public void handle(JsonObject input) {
		final var senderUUID = UUID.fromString(input.get("sender").getAsString());
		final var groupName = input.get("group").getAsString();
		final long channelID = input.get("channelID").getAsLong();
		final long guildID = input.get("guildID").getAsLong();

		final var foundGroup = GroupManager.getGroup(groupName);
		if (foundGroup == null) {
			KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToUser(senderUUID,
					"The group " + groupName + " does not exist", -1);
			return;
		}
		if (foundGroup.isDisciplined()) {
			KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToUser(senderUUID,
					"You cannot relay that group, it's disciplined.", -1);
			return;
		}

		final var groupManager = NameAPI.getGroupManager();
		final var kiraManagePermission = PermissionType.getPermission("KIRA_MANAGE_CHANNEL");
		if (!groupManager.hasAccess(foundGroup, senderUUID, kiraManagePermission)) {
			KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToUser(senderUUID,
					"You don't have the required permission KIRA_MANAGE_CHANNEL " +
							"for the group " + foundGroup.getName(), -1);
			return;
		}

		final var readChatPermission = PermissionType.getPermission("READ_CHAT");
		final var recipients = new HashSet<UUID>();

		foundGroup.getAllMembers().stream()
				.filter(member -> groupManager.hasAccess(foundGroup, member, readChatPermission))
				.forEach(recipients::add);

		KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToUser(senderUUID,
				"Confirmed permissions, proceeding with channel setup...", -1);

		KiraBukkitGatewayPlugin.getInstance().getRabbit().createGroupChatChannel(
				foundGroup.getName(), recipients, senderUUID, guildID, channelID);
	}
}
