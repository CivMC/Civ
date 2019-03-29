package com.github.maxopoly.KiraBukkitGateway.rabbit;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.github.maxopoly.KiraBukkitGateway.listener.SnitchHitType;
import com.github.maxopoly.KiraBukkitGateway.listener.SnitchType;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RabbitCommands {

	private RabbitHandler internal;

	public RabbitCommands(RabbitHandler internalRabbit) {
		this.internal = internalRabbit;
	}

	public void sendAuthCode(String code, String playerName, UUID playerUUID) {
		if (code == null || playerName == null || playerUUID == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("uuid", playerUUID.toString());
		json.addProperty("name", playerName);
		json.addProperty("code", code);
		sendInternal("addauth", json);
	}

	public void sendGroupChatMessage(String group, String sender, String msg) {
		if (group == null || sender == null || msg == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("sender", sender);
		json.addProperty("msg", msg);
		sendInternal("groupchatmessage", json);
	}

	public void replyToRequestSession(JsonObject json) {
		if (json == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		sendInternal("requestsession", json);
	}

	public void playerLoginOut(String player, String action) {
		if (player == null || action == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("player", player);
		json.addProperty("action", action);
		sendInternal("skynet", json);
	}

	public void syncGroupChatAccess(String group, Collection<UUID> members, UUID sender) {
		if (group == null || members == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("sender", sender.toString());
		JsonArray array = new JsonArray();
		members.forEach(uuid -> array.add(uuid.toString()));
		json.add("members", array);
		sendInternal("syncgroupchatmembers", json);
	}

	public void createGroupChatChannel(String group, Collection<UUID> members, UUID creator, long guildID,
			long channelID) {
		if (group == null || members == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("creator", creator.toString());
		json.addProperty("guildID", guildID);
		json.addProperty("channelID", channelID);
		JsonArray array = new JsonArray();
		members.forEach(uuid -> array.add(uuid.toString()));
		json.add("members", array);
		sendInternal("creategroupchat", json);
	}

	public void deleteGroupChatChannel(String group, UUID sender) {
		if (group == null || sender == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("sender", sender.toString());
		sendInternal("deletegroupchat", json);
	}

	public void removeGroupMember(String group, UUID member) {
		if (group == null || member == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("member", member.toString());
		sendInternal("removegroupmember", json);
	}

	public void addGroupMember(String group, UUID member) {
		if (group == null || member == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("member", member.toString());
		sendInternal("addgroupmember", json);
	}

	public void replyToUser(UUID user, String msg) {
		if (user == null || msg == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("user", user.toString());
		json.addProperty("msg", msg);
		sendInternal("replytouser", json);
	}

	public void sendSnitchHit(Player victim, Location location, String snitchName, String groupName,
			SnitchHitType hitType, SnitchType snitchType) {
		if (victim == null || location == null || groupName == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		if (snitchName == null) {
			snitchName = "";
		}
		JsonObject json = new JsonObject();
		json.addProperty("victimUUID", victim.getUniqueId().toString());
		json.addProperty("victimName", victim.getName());
		json.addProperty("world", location.getWorld().getName());
		json.addProperty("x", location.getBlockX());
		json.addProperty("y", location.getBlockY());
		json.addProperty("z", location.getBlockZ());
		json.addProperty("snitchName", snitchName);
		json.addProperty("groupName", groupName);
		json.addProperty("type", hitType.toString());
		json.addProperty("snitchtype", snitchType.toString());
		sendInternal("sendsnitchhit", json);
	}

	private void sendInternal(String id, JsonObject json) {
		json.addProperty("timestamp", System.currentTimeMillis());
		Gson gson = new Gson();
		String payload = gson.toJson(json);
		internal.sendMessage(id + " " + payload);
	}

}
