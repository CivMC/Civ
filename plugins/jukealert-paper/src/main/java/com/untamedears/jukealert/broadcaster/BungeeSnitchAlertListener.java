package com.untamedears.jukealert.broadcaster;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.untamedears.jukealert.JukeAlert;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public class BungeeSnitchAlertListener implements PluginMessageListener {

    private static final long MAX_MESSAGE_AGE_MILLIS = 5000L;

    @Override
    public void onPluginMessageReceived(
        final @NotNull String channel,
        final @NotNull Player player,
        final byte @NotNull [] message
    ) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        final ByteArrayDataInput input = ByteStreams.newDataInput(message);
        final String subchannel = input.readUTF();
        if (!subchannel.equals(BungeeSnitchAlertBroadcaster.CHANNEL)) {
            return;
        }

        final short length = input.readShort();
        final byte[] messageBytes = new byte[length];
        input.readFully(messageBytes);

        final DataInputStream messageInput = new DataInputStream(new ByteArrayInputStream(messageBytes));
        try {
            final String type = messageInput.readUTF();
            if (!type.equals(BungeeSnitchAlertBroadcaster.TYPE_ALERT)) {
                JukeAlert.getInstance().getLogger().warning("Unknown type '" + type + "' in JukeAlert plugin message");
                return;
            }

            final RemoteSnitchAlert alert = readAlert(messageInput);
            if (System.currentTimeMillis() - alert.sentAt() > MAX_MESSAGE_AGE_MILLIS) {
                return;
            }
            if (!Objects.equals(alert.databaseName(), JukeAlert.getInstance().getConfigManager().getDatabaseName())) {
                return;
            }
            RemoteSnitchAlertDispatcher.send(alert);
        } catch (final IOException exception) {
            JukeAlert.getInstance().getLogger().log(Level.WARNING, "Reading plugin message", exception);
        } catch (final IllegalArgumentException exception) {
            JukeAlert.getInstance().getLogger().log(Level.WARNING, "Reading invalid plugin message", exception);
        }
    }

    private RemoteSnitchAlert readAlert(final DataInputStream input) throws IOException {
        return new RemoteSnitchAlert(
            input.readLong(),
            input.readUTF(),
            input.readUTF(),
            UUID.fromString(input.readUTF()),
            input.readUTF(),
            input.readUTF(),
            input.readUTF(),
            input.readUTF(),
            input.readUTF(),
            input.readInt(),
            input.readInt(),
            input.readInt()
        );
    }
}
