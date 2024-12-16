package vg.civcraft.mc.civchat2.broadcaster;

import java.util.UUID;

public interface ServerBroadcaster {
    void broadcastGroup(UUID senderId, String senderName, String senderDisplayName, String groupName, String chatMessage);
}
