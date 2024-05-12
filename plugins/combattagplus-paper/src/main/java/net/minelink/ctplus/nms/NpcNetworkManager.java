package net.minelink.ctplus.nms;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketFlow;
import net.minelink.ctplus.util.EmptyChannel;

import java.lang.reflect.Field;

public class NpcNetworkManager extends Connection {
    public NpcNetworkManager() {
        super(PacketFlow.SERVERBOUND);
        channel = new EmptyChannel(null);
    }

    @Override
    public void setListener(PacketListener packetListener) {
        // Will need to be remapped by version, See: https://mappings.cephx.dev/YOUR_VERSION/net/minecraft/network/Connection.html for mappings
        Field connectionPacketListener = null;
        Field connectionDisconnectListener = null;

        try {
            connectionPacketListener = Connection.class.getDeclaredField("q"); // net.minecraft.network.Connection.packetListener
            connectionPacketListener.setAccessible(true);

            connectionDisconnectListener = Connection.class.getDeclaredField("p");  // net.minecraft.network.Connection.disconnectListener
            connectionDisconnectListener.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }


        try {
            connectionPacketListener.set(this, packetListener); // q -> private volatile PacketListener packetListener
            connectionDisconnectListener.set(this, null); // p -> private volatile PacketListener disconnectListener
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
