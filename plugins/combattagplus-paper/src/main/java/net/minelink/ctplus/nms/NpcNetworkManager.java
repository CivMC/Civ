package net.minelink.ctplus.nms;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minelink.ctplus.util.EmptyChannel;

public class NpcNetworkManager extends Connection {

    public NpcNetworkManager() {
        super(PacketFlow.SERVERBOUND);
        channel = new EmptyChannel(null);
    }
}
