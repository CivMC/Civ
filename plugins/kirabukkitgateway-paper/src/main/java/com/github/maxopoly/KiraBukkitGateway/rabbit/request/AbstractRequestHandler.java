package com.github.maxopoly.KiraBukkitGateway.rabbit.request;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;

public abstract class AbstractRequestHandler {

    private String identifier;
    private boolean async;

    public AbstractRequestHandler(String identifier, boolean isAsync) {
        this.identifier = identifier;
        this.async = isAsync;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isAsync() {
        return async;
    }

    public abstract void handle(JsonObject input, JsonObject output, long channelId);

    protected void sendRequestSessionReply(JsonObject json) {
        KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToRequestSession(json);
    }

    protected boolean dispatchCommand(CommandSender rawSender, String commandLine) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        return server.getCommandMap().dispatch(rawSender, commandLine);
    }
}
