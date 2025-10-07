package vg.civcraft.mc.civchat2.broadcaster;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class BungeeServerListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (!subchannel.equals("CivChat2")) {
            return;
        }

        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);

        DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(msgbytes));
        try {
            String type = msgIn.readUTF();
            CivChat2Manager manager = CivChat2.getInstance().getCivChat2Manager();
            if (type.equals("group")) {
                long timestamp = msgIn.readLong();
                if (System.currentTimeMillis() - timestamp > 5000) {
                    return;
                }
                manager.sendRemoteGroupMsg(UUID.fromString(msgIn.readUTF()), msgIn.readUTF(), GsonComponentSerializer.gson().deserialize(msgIn.readUTF()), msgIn.readUTF(), GsonComponentSerializer.gson().deserialize(msgIn.readUTF()));
            } else {
                CivChat2.getInstance().getLogger().log(Level.WARNING, "Unknown type '" + type + "' in CivChat2 plugin message");
            }
        } catch (IOException ex) {
            CivChat2.getInstance().getLogger().log(Level.WARNING, "Reading plugin message", ex);
        }
    }
}
