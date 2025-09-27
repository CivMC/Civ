package xyz.huskydog.kiragatewayVelocity.rabbit;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.UUID;

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

}
