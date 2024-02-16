package net.minelink.ctplus.nms;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;

public class NpcNetworkManager extends Connection {
    public NpcNetworkManager() {
        super(PacketFlow.SERVERBOUND);
    }
}
