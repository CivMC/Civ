package com.github.maxopoly.KiraBukkitGateway.rabbit;

import com.github.maxopoly.KiraBukkitGateway.KiraUtil;
import com.github.maxopoly.KiraBukkitGateway.listener.SnitchHitType;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.UnsafeValues;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;


public class RabbitCommands {

    private final RabbitHandler internal;
    private final String serverName;

    public RabbitCommands(RabbitHandler internalRabbit, String serverName) {
        this.internal = internalRabbit;
        this.serverName = serverName;
    }

    public void sendGroupChatMessage(String group, Player sender, String msg) {
        if (group == null || sender == null || msg == null) {
            throw new IllegalArgumentException("Arguments cant be null");
        }
        JsonObject json = new JsonObject();
        json.addProperty("group", group);
        json.addProperty("senderUUID", sender.getUniqueId().toString());
        json.addProperty("sender", sender.getName());
        json.addProperty("msg", msg);
        sendInternal("groupchatmessage", json);
    }

    public void sendConsoleRelay(String msg, String key) {
        nonNullArgs(msg, key);
        JsonObject json = new JsonObject();
        json.addProperty("consolekey", key);
        json.addProperty("message", msg);
        sendInternal("consolelog", json);
    }

    public void replyToRequestSession(JsonObject json) {
        nonNullArgs(json);
        sendInternal("requestsession", json);
    }

    public void playerLoginFirstTime(Player player) {
        nonNullArgs(player);
        JsonObject json = new JsonObject();
        json.addProperty("player", player.getName());
        json.addProperty("playerUUID", player.getUniqueId().toString());
        sendInternal("newplayer", json);
    }

    public void playerLoginOut(Player player, String action) {
        nonNullArgs(player, action);
        JsonObject json = new JsonObject();
        json.addProperty("player", player.getName());
        json.addProperty("playerUUID", player.getUniqueId().toString());
        json.addProperty("action", action);
        sendInternal("skynet", json);
    }

    public void syncGroupChatAccess(String group, Collection<UUID> members, UUID sender) {
        nonNullArgs(group, members);
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
        nonNullArgs(group, members);
        JsonObject json = new JsonObject();
        json.addProperty("group", group);
        json.addProperty("creator", creator.toString());
        json.addProperty("guildID", guildID);
        json.addProperty("channelID", channelID);
        json.addProperty("server", serverName);
        JsonArray array = new JsonArray();
        members.forEach(uuid -> array.add(uuid.toString()));
        json.add("members", array);
        sendInternal("creategroupchat", json);
    }

    public void deleteGroupChatChannel(String group, UUID sender) {
        nonNullArgs(group, sender);
        JsonObject json = new JsonObject();
        json.addProperty("group", group);
        json.addProperty("sender", sender.toString());
        sendInternal("deletegroupchat", json);
    }

    public void replyToUser(UUID user, String msg, long channelId) {
        nonNullArgs(user, msg);
        JsonObject json = new JsonObject();
        json.addProperty("user", user.toString());
        json.addProperty("msg", KiraUtil.cleanUp(msg));
        json.addProperty("channel", channelId);
        sendInternal("replytouser", json);
    }

    public void sendSnitchHit(Player victim, Location location, String snitchName, String groupName,
                              SnitchHitType hitType, String snitchType) {
        nonNullArgs(victim, location, groupName);
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
        json.addProperty("snitchtype", snitchType);
        sendInternal("sendsnitchhit", json);
    }

    public JsonObject serializeItemToJson(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return null;
        }
        UnsafeValues unsafeValues = Bukkit.getUnsafe();
        return unsafeValues.serializeItemAsJson(itemStack);
    }

    public void sendSuccessfulPurchase(String groupName, Player purchaser, Location location, 
            String tradeName, ItemStack[] input, ItemStack[] output) {
        nonNullArgs(groupName, purchaser, location);
        if (tradeName == null) {
            tradeName = "";
        }
        JsonObject json = new JsonObject();
        json.addProperty("purchaserUUID", purchaser.getUniqueId().toString());
        json.addProperty("purchaserName", purchaser.getName());
        json.addProperty("world", location.getWorld().getName());
        json.addProperty("x", location.getBlockX());
        json.addProperty("y", location.getBlockY());
        json.addProperty("z", location.getBlockZ());
        json.addProperty("groupName", groupName);
        json.addProperty("trade", tradeName);
        JsonArray inputArray = new JsonArray();
        for (ItemStack in : input) {
            JsonObject jsonIn = serializeItemToJson(in);
            if (jsonIn != null)
                inputArray.add(jsonIn);
        }
        json.add("input", inputArray);
        JsonArray outputArray = new JsonArray();
        for (ItemStack out : output) {
            JsonObject jsonOut = serializeItemToJson(out);
            if (jsonOut != null)
                outputArray.add(jsonOut);
        }
        json.add("output", outputArray);
        sendInternal("sendsuccessfulpurchase", json);
    }

    private void sendInternal(String id, JsonObject json) {
        json.addProperty("timestamp", System.currentTimeMillis());
        json.addProperty("packettype", id);
        json.addProperty("server", serverName);
        Gson gson = new Gson();
        String payload = gson.toJson(json);
        internal.sendMessage(payload);
    }

    private void nonNullArgs(Object... objects) {
        for (Object o : objects) {
            if (o == null) {
                throw new IllegalArgumentException("Arguments cant be null");
            }
        }
    }
}
