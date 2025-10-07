package net.civmc.kiragatewayvelocity.impersonation;

import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import net.civmc.kiragatewayvelocity.KiraGateway;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PseudoConsoleSender implements ConsoleCommandSource {

    private List<String> replies = new ArrayList<>();
    private final UUID actualUser;
    private final long discordChannelId;

    public PseudoConsoleSender(UUID actualUser, long discordChannelId) {
        this.actualUser = actualUser;
        this.discordChannelId = discordChannelId;
    }

    public synchronized List<String> getRepliesAndFinish() {
        List<String> result = replies;
        replies = null;
        return result;
    }

    private synchronized void handleReply(String input) {
        if (replies != null) {
            replies.add(input);
            return;
        }
        KiraGateway.getInstance().getRabbit().replyToUser(actualUser, input, discordChannelId);
    }

    /**
     * @noinspection UnstableApiUsage
     */
    @Override
    public void sendMessage(@NotNull Identity identity, @NotNull Component message, @NotNull MessageType type) {
        handleReply(PlainTextComponentSerializer.plainText().serialize(message));
    }

    @Override
    public Tristate getPermissionValue(String permission) {
        return Tristate.TRUE;
    }
}
