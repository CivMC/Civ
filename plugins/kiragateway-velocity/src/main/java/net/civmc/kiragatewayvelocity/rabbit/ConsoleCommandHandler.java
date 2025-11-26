package net.civmc.kiragatewayvelocity.rabbit;

import com.google.gson.JsonObject;
import java.util.UUID;
import net.civmc.kiragatewayvelocity.KiraGateway;
import net.civmc.kiragatewayvelocity.KiraUtil;
import net.civmc.kiragatewayvelocity.impersonation.PseudoConsoleSender;
import org.slf4j.Logger;
import org.spongepowered.configurate.serialize.SerializationException;

public class ConsoleCommandHandler extends AbstractRequestHandler {

    public ConsoleCommandHandler() {
        super("consolemessageop", true);
    }

    @Override
    public void handle(JsonObject input, JsonObject output, long channelId) {
        UUID sender = UUID.fromString(input.get("sender").getAsString());
        String command = input.get("command").getAsString();
        Logger logger = KiraGateway.getInstance().getInstance().logger;
        try {
            boolean isOp = false;
            for (String op : KiraGateway.getInstance().getConfig().node("ops").getList(String.class)) {
                if (op.equals(sender.toString())) {
                    isOp = true;
                    break;
                }
            }
            if (!isOp) {
                logger.warn("Non op player " + sender + " tried to run console command");
                output.addProperty("replymsg", "You are not op");
                sendRequestSessionReply(output);
                return;
            }
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        PseudoConsoleSender console = new PseudoConsoleSender(sender, channelId);
        dispatchCommand(KiraGateway.getInstance().getProxy(), console, command).thenAccept(ignored -> {
            StringBuilder sb = new StringBuilder();
            for (String s : console.getRepliesAndFinish()) {
                sb.append(KiraUtil.cleanUp(s));
                sb.append('\n');
            }
            output.addProperty("replymsg", sb.toString());
            sendRequestSessionReply(output);
        });
    }
}
