package com.github.maxopoly.KiraBukkitGateway.rabbit.in;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitInput;
import com.google.gson.JsonObject;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class RequestRelayCreationHandler extends RabbitInput {

	public RequestRelayCreationHandler() {
		super("requestrelaycreation");
	}

	@Override
	public void handle(JsonObject input) {
		UUID sender = UUID.fromString(input.get("sender").getAsString());
		String groupName = input.get("group").getAsString();
		Group group = GroupManager.getGroup(groupName);
		long channelID = input.get("channelID").getAsLong();
		long guildID = input.get("guildID").getAsLong();
		if (group == null) {
			KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToUser(sender,
					"The group " + groupName + " does not exist", -1);
			return;
		}
		if (!NameAPI.getGroupManager().hasAccess(group, sender, PermissionType.getPermission("KIRA_MANAGE_CHANNEL"))) {
			KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToUser(sender,
					"You don't have the required permission KIRA_MANAGE_CHANNEL for the group " + group.getName(), -1);
			return;
		}
		GroupManager gm = NameAPI.getGroupManager();
		PermissionType perm = PermissionType.getPermission("READ_CHAT");
		Collection<UUID> members = new HashSet<>();
		group.getAllMembers().stream().filter(m -> gm.hasAccess(group, m, perm)).forEach(m -> members.add(m));
		KiraBukkitGatewayPlugin.getInstance().getRabbit().createGroupChatChannel(group.getName(), members, sender,
				guildID, channelID);
		KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToUser(sender,
				"Confirmed permissions, proceeding with channel setup...", -1);
	}
}
