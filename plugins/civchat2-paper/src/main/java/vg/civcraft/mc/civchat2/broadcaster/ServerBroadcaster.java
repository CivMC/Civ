package vg.civcraft.mc.civchat2.broadcaster;

import net.kyori.adventure.text.Component;
import java.util.UUID;

public interface ServerBroadcaster {
    void broadcastGroup(UUID senderId, String senderName, Component senderDisplayName, String groupName, Component chatMessage);
}
