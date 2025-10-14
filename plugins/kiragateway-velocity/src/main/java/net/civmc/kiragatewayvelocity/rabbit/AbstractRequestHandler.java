package net.civmc.kiragatewayvelocity.rabbit;

import com.google.gson.JsonObject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import net.civmc.kiragatewayvelocity.KiraGateway;
import java.util.concurrent.CompletableFuture;

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
        KiraGateway.getInstance().getRabbit().replyToRequestSession(json);
    }

    protected CompletableFuture<Boolean> dispatchCommand(ProxyServer server, CommandSource rawSender, String commandLine) {
        return server.getCommandManager().executeAsync(rawSender, commandLine);
    }
}
