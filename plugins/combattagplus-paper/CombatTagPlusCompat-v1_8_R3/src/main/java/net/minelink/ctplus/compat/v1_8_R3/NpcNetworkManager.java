package net.minelink.ctplus.compat.v1_8_R3;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.server.v1_8_R3.EnumProtocol;
import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketListener;

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
    public void a(EnumProtocol enumprotocol) {

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
    public void a(PacketListener packetlistener) {

    }

    @Override
    public void handle(Packet packet) {

    }

    @Override
    public void a(Packet packet, GenericFutureListener genericfuturelistener, GenericFutureListener... agenericfuturelistener) {

    }

    @Override
    public SocketAddress getSocketAddress() {
        return new SocketAddress() {};
    }

    @Override
    public boolean c() {
        return false;
    }

    @Override
    public void a(SecretKey secretkey) {

    }

    @Override
    public boolean g() {
        return true;
    }

    @Override
    public void k() {

    }

    @Override
    public void a(int i) {

    }

    @Override
    public void l() {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelhandlercontext, Packet object) throws Exception {

    }

}
