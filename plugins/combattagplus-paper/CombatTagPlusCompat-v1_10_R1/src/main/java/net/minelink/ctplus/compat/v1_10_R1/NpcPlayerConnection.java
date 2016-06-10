package net.minelink.ctplus.compat.v1_10_R1;

import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.IChatBaseComponent;
import net.minecraft.server.v1_10_R1.MinecraftServer;
import net.minecraft.server.v1_10_R1.Packet;
import net.minecraft.server.v1_10_R1.PacketPlayInAbilities;
import net.minecraft.server.v1_10_R1.PacketPlayInArmAnimation;
import net.minecraft.server.v1_10_R1.PacketPlayInBlockDig;
import net.minecraft.server.v1_10_R1.PacketPlayInBlockPlace;
import net.minecraft.server.v1_10_R1.PacketPlayInChat;
import net.minecraft.server.v1_10_R1.PacketPlayInClientCommand;
import net.minecraft.server.v1_10_R1.PacketPlayInCloseWindow;
import net.minecraft.server.v1_10_R1.PacketPlayInCustomPayload;
import net.minecraft.server.v1_10_R1.PacketPlayInEnchantItem;
import net.minecraft.server.v1_10_R1.PacketPlayInEntityAction;
import net.minecraft.server.v1_10_R1.PacketPlayInFlying;
import net.minecraft.server.v1_10_R1.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_10_R1.PacketPlayInKeepAlive;
import net.minecraft.server.v1_10_R1.PacketPlayInResourcePackStatus;
import net.minecraft.server.v1_10_R1.PacketPlayInSetCreativeSlot;
import net.minecraft.server.v1_10_R1.PacketPlayInSettings;
import net.minecraft.server.v1_10_R1.PacketPlayInSpectate;
import net.minecraft.server.v1_10_R1.PacketPlayInSteerVehicle;
import net.minecraft.server.v1_10_R1.PacketPlayInTabComplete;
import net.minecraft.server.v1_10_R1.PacketPlayInTransaction;
import net.minecraft.server.v1_10_R1.PacketPlayInUpdateSign;
import net.minecraft.server.v1_10_R1.PacketPlayInUseEntity;
import net.minecraft.server.v1_10_R1.PacketPlayInWindowClick;
import net.minecraft.server.v1_10_R1.PlayerConnection;

public final class NpcPlayerConnection extends PlayerConnection {

    public NpcPlayerConnection(EntityPlayer entityplayer) {
        super(MinecraftServer.getServer(), new NpcNetworkManager(), entityplayer);
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
