package net.minelink.ctplus.nms;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class NpcPlayerConnection extends ServerGamePacketListenerImpl {

    public NpcPlayerConnection(ServerPlayer player) {
        super(MinecraftServer.getServer(), new NpcNetworkManager(), player, new CommonListenerCookie(player.gameProfile, -1, player.clientInformation()));
    }

}
