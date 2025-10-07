package net.civmc.kiragatewayvelocity.rabbit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

public class RabbitInputHandler {

    private Map<String, RabbitInput> commands;
    private Logger logger;

    public RabbitInputHandler(Logger logger) {
        this.commands = new HashMap<>();
        this.logger = logger;
        registerCommands();
    }

    private void registerCommands() {
        registerCommand(new RequestSessionHandler());
    }

    private void registerCommand(RabbitInput command) {
        commands.put(command.getIdentifier().toLowerCase(), command);
    }

    public void handle(String input) {
        if (input == null || input.equals("")) {
            logger.info("Invalid empty input in rabbit handler");
            return;
        }
        int spaceIndex = input.indexOf(" ");
        String arguments;
        String command;
        if (spaceIndex == -1) {
            arguments = "";
            command = input;
        } else {
            arguments = input.substring(spaceIndex + 1);
            command = input.substring(0, spaceIndex);
        }
        RabbitInput comm = commands.get(command);
        if (comm == null) {
            logger.error("Received invalid rabbit command: " + input);
            return;
        }
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(arguments);
        comm.handle((JsonObject) json);
    }

}
