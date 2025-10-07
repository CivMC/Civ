package net.civmc.kiragatewayvelocity.rabbit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.UUID;
import net.civmc.kiragatewayvelocity.KiraGateway;
import net.civmc.kiragatewayvelocity.KiraUtil;

public class RabbitCommands {

    private final RabbitHandler internal;

    public RabbitCommands(RabbitHandler internalRabbit) {
        this.internal = internalRabbit;
    }

    public void sendAuthCode(String code, String playerName, UUID playerUUID) {
        nonNullArgs(code, playerName, playerUUID);
        JsonObject json = new JsonObject();
        json.addProperty("uuid", playerUUID.toString());
        json.addProperty("name", playerName);
        json.addProperty("code", code);
        sendInternal("addauth", json);
    }

    private void sendInternal(String id, JsonObject json) {
        json.addProperty("server", KiraGateway.PROXY_SERVER_NAME);
        json.addProperty("timestamp", System.currentTimeMillis());
        json.addProperty("packettype", id);
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

    public void replyToUser(UUID user, String msg, long channelId) {
        nonNullArgs(user, msg);
        JsonObject json = new JsonObject();
        json.addProperty("user", user.toString());
        json.addProperty("msg", KiraUtil.cleanUp(msg));
        json.addProperty("channel", channelId);
        sendInternal("replytouser", json);
    }

    public void replyToRequestSession(JsonObject json) {
        nonNullArgs(json);
        sendInternal("requestsession", json);
    }
}
