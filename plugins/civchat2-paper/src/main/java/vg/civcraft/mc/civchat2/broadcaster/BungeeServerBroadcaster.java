package vg.civcraft.mc.civchat2.broadcaster;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.CivChat2;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

public class BungeeServerBroadcaster implements ServerBroadcaster {

    @Override
    public void broadcastGroup(UUID senderId, String senderName, String senderDisplayName, String groupName, String chatMessage) {
        Iterator<? extends Player> it = Bukkit.getOnlinePlayers().iterator();
        if (!it.hasNext()) {
            return;
        }
        Player player = it.next();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ALL");
        out.writeUTF("CivChat2");

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeUTF("group");
            msgout.writeLong(System.currentTimeMillis());
            msgout.writeUTF(senderId.toString());
            msgout.writeUTF(senderName);
            msgout.writeUTF(senderDisplayName);
            msgout.writeUTF(groupName);
            msgout.writeUTF(chatMessage);
        } catch (IOException ex) {
            return;
        }

        out.writeShort(msgbytes.toByteArray().length);
        out.write(msgbytes.toByteArray());

        player.sendPluginMessage(CivChat2.getInstance(), "BungeeCord", out.toByteArray());
    }
}
