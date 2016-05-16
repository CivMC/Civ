package net.minelink.ctplus.compat.v1_9_R2;

import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.IChatBaseComponent;
import net.minecraft.server.v1_9_R2.MinecraftServer;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayInAbilities;
import net.minecraft.server.v1_9_R2.PacketPlayInArmAnimation;
import net.minecraft.server.v1_9_R2.PacketPlayInBlockDig;
import net.minecraft.server.v1_9_R2.PacketPlayInBlockPlace;
import net.minecraft.server.v1_9_R2.PacketPlayInChat;
import net.minecraft.server.v1_9_R2.PacketPlayInClientCommand;
import net.minecraft.server.v1_9_R2.PacketPlayInCloseWindow;
import net.minecraft.server.v1_9_R2.PacketPlayInCustomPayload;
import net.minecraft.server.v1_9_R2.PacketPlayInEnchantItem;
import net.minecraft.server.v1_9_R2.PacketPlayInEntityAction;
import net.minecraft.server.v1_9_R2.PacketPlayInFlying;
import net.minecraft.server.v1_9_R2.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_9_R2.PacketPlayInKeepAlive;
import net.minecraft.server.v1_9_R2.PacketPlayInResourcePackStatus;
import net.minecraft.server.v1_9_R2.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_9_R2.PacketPlayInSettings;
import net.minecraft.server.v1_9_R2.PacketPlayInSpectate;
import net.minecraft.server.v1_9_R2.PacketPlayInSteerVehicle;
import net.minecraft.server.v1_9_R2.PacketPlayInTabComplete;
import net.minecraft.server.v1_9_R2.PacketPlayInTransaction;
import net.minecraft.server.v1_9_R2.PacketPlayInUpdateSign;
import net.minecraft.server.v1_9_R2.PacketPlayInUseEntity;
import net.minecraft.server.v1_9_R2.PacketPlayInWindowClick;
import net.minecraft.server.v1_9_R2.PlayerConnection;

public final class NpcPlayerConnection extends PlayerConnection {

    public NpcPlayerConnection(EntityPlayer entityplayer) {
        super(MinecraftServer.getServer(), new NpcNetworkManager(), entityplayer);
    }

    @Override
    public void c() {

    }

    @Override
    public void disconnect(String s) {

    }

    @Override
    public void a(PacketPlayInSteerVehicle packetplayinsteervehicle) {

    }

    @Override
    public void a(PacketPlayInFlying packetplayinflying) {

    }

    @Override
    public void a(PacketPlayInBlockDig packetplayinblockdig) {

    }

    @Override
    public void a(PacketPlayInBlockPlace packetplayinblockplace) {

    }

    @Override
    public void a(PacketPlayInSpectate packetplayinspectate) {

    }

    @Override
    public void a(PacketPlayInResourcePackStatus packetplayinresourcepackstatus) {

    }

    @Override
    public void a(IChatBaseComponent ichatbasecomponent) {

    }

    @Override
    public void sendPacket(Packet packet) {

    }

    @Override
    public void a(PacketPlayInHeldItemSlot packetplayinhelditemslot) {

    }

    @Override
    public void a(PacketPlayInChat packetplayinchat) {

    }

    @Override
    public void chat(String s, boolean async) {

    }

    @Override
    public void a(PacketPlayInArmAnimation packetplayinarmanimation) {

    }

    @Override
    public void a(PacketPlayInEntityAction packetplayinentityaction) {

    }

    @Override
    public void a(PacketPlayInUseEntity packetplayinuseentity) {

    }

    @Override
    public void a(PacketPlayInClientCommand packetplayinclientcommand) {

    }

    @Override
    public void a(PacketPlayInCloseWindow packetplayinclosewindow) {

    }

    @Override
    public void a(PacketPlayInWindowClick packetplayinwindowclick) {

    }

    @Override
    public void a(PacketPlayInEnchantItem packetplayinenchantitem) {

    }

    @Override
    public void a(PacketPlayInSetCreativeSlot packetplayinsetcreativeslot) {

    }

    @Override
    public void a(PacketPlayInTransaction packetplayintransaction) {

    }

    @Override
    public void a(PacketPlayInUpdateSign packetplayinupdatesign) {

    }

    @Override
    public void a(PacketPlayInKeepAlive packetplayinkeepalive) {

    }

    @Override
    public void a(PacketPlayInAbilities packetplayinabilities) {

    }

    @Override
    public void a(PacketPlayInTabComplete packetplayintabcomplete) {

    }

    @Override
    public void a(PacketPlayInSettings packetplayinsettings) {

    }

    @Override
    public void a(PacketPlayInCustomPayload packetplayincustompayload) {

    }

}
