package net.minelink.ctplus.compat.v1_7_R4;

import net.minecraft.server.v1_7_R4.EnumProtocol;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;

import javax.crypto.SecretKey;
import java.net.SocketAddress;

public final class NpcNetworkManager extends NetworkManager {

    public NpcNetworkManager() {
        super(false);
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
    public void handle(Packet packet, GenericFutureListener... agenericfuturelistener) {

    }

    @Override
    public void a() {

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
    public boolean isConnected() {
        return true;
    }

    @Override
    public void g() {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelhandlercontext, Object object) {

    }

    // Spigot override
    public int getVersion() {
        return -1;
    }

}
