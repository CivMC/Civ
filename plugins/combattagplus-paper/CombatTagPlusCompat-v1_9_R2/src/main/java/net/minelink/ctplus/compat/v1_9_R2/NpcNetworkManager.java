package net.minelink.ctplus.compat.v1_9_R2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_9_R2.EnumProtocol;
import net.minecraft.server.v1_9_R2.EnumProtocolDirection;
import net.minecraft.server.v1_9_R2.NetworkManager;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketListener;

import javax.crypto.SecretKey;
import java.net.SocketAddress;

public final class NpcNetworkManager extends NetworkManager {

    public NpcNetworkManager() {
        super(EnumProtocolDirection.SERVERBOUND);
    }

    @Override
    public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {

    }

    @Override
    public void setProtocol(EnumProtocol enumprotocol) {

    }

    @Override
    public void channelInactive(ChannelHandlerContext channelhandlercontext) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) {

    }

    @Override
    protected void a(ChannelHandlerContext channelhandlercontext, Packet packet) {

    }

    @Override
    public void setPacketListener(PacketListener packetlistener) {

    }

    @Override
    public void sendPacket(Packet packet) {

    }

    @Override
    public void sendPacket(Packet packet, GenericFutureListener genericfuturelistener, GenericFutureListener... agenericfuturelistener) {

    }

    @Override
    public SocketAddress getSocketAddress() {
        return new SocketAddress() {};
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public void a(SecretKey secretkey) {

    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void stopReading() {

    }

    @Override
    public void setCompressionLevel(int i) {

    }

    @Override
    public void handleDisconnection() {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelhandlercontext, Packet object) throws Exception {

    }

}
