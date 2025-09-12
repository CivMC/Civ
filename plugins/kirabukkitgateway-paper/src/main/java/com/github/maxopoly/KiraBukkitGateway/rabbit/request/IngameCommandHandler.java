package com.github.maxopoly.KiraBukkitGateway.rabbit.request;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.impersonation.PseudoPlayer;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;

public class IngameCommandHandler extends AbstractRequestHandler {

    public IngameCommandHandler() {
        super("ingame", true);
    }

    @Override
    public void handle(JsonObject input, JsonObject output, long channelId) {
        UUID runner = UUID.fromString(input.get("uuid").getAsString());
        String command = input.get("command").getAsString();
        Logger logger = KiraBukkitGatewayPlugin.getInstance().getLogger();
        logger.info("Running command '" + command + "' for " + runner);
        Bukkit.getScheduler().runTask(KiraBukkitGatewayPlugin.getInstance(), () -> {
            ArrayList<String> messages = new ArrayList<>();
            PseudoPlayer pseudoPlayerSender = new PseudoPlayer(runner, channelId, component -> messages.add(PlainTextComponentSerializer.plainText().serialize(component)));
            try {
                dispatchCommand(pseudoPlayerSender, command);
            } catch (Exception e) {
                output.addProperty("reply", "You can not run this command from out of game");
                logger.warning("Failed to run command from external source: " + e.getMessage());
                e.printStackTrace();
                sendRequestSessionReply(output);
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String reply : messages) {
                sb.append(reply);
                sb.append('\n');
            }
            output.addProperty("reply", sb.toString());
            sendRequestSessionReply(output);
        });

    }
}
