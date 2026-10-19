package com.untamedears.jukealert.broadcaster;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.untamedears.jukealert.JukeAlert;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BungeeSnitchAlertBroadcaster implements SnitchAlertBroadcaster {

    public static final String CHANNEL = "JukeAlert";
    public static final String TYPE_ALERT = "alert";

    @Override
    public void broadcast(final RemoteSnitchAlert alert) {
        final Iterator<? extends Player> players = Bukkit.getOnlinePlayers().iterator();
        if (!players.hasNext()) {
            return;
        }

        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Forward");
        output.writeUTF("ALL");
        output.writeUTF(CHANNEL);

        final ByteArrayOutputStream messageBytes = new ByteArrayOutputStream();
        final DataOutputStream messageOutput = new DataOutputStream(messageBytes);
        try {
            messageOutput.writeUTF(TYPE_ALERT);
            messageOutput.writeLong(alert.sentAt());
            messageOutput.writeUTF(alert.databaseName());
            messageOutput.writeUTF(alert.actionIdentifier());
            messageOutput.writeUTF(alert.playerId().toString());
            messageOutput.writeUTF(alert.playerName());
            messageOutput.writeUTF(alert.groupName());
            messageOutput.writeUTF(alert.snitchTypeName());
            messageOutput.writeUTF(alert.snitchName());
            messageOutput.writeUTF(alert.worldName());
            messageOutput.writeInt(alert.x());
            messageOutput.writeInt(alert.y());
            messageOutput.writeInt(alert.z());
        } catch (final IOException exception) {
            return;
        }

        output.writeShort(messageBytes.toByteArray().length);
        output.write(messageBytes.toByteArray());
        players.next().sendPluginMessage(JukeAlert.getInstance(), "BungeeCord", output.toByteArray());
    }
}
