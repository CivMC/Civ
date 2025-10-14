package net.civmc.kiragatewayvelocity.rabbit;

import com.google.gson.JsonObject;

public abstract class RabbitInput {

    private String identifier;

    public RabbitInput(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public abstract void handle(JsonObject input);

}
