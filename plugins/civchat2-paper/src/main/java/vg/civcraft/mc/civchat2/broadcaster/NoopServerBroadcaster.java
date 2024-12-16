package vg.civcraft.mc.civchat2.broadcaster;

import java.util.UUID;

public class NoopServerBroadcaster implements ServerBroadcaster {

    @Override
    public void broadcastGroup(UUID senderId, String senderName, String senderDisplayName, String groupName, String chatMessage) {

    }
}
