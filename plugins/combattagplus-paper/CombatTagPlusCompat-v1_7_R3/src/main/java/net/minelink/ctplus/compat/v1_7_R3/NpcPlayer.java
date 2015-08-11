package net.minelink.ctplus.compat.v1_7_R3;

import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.MinecraftServer;
import net.minecraft.server.v1_7_R3.PlayerInteractManager;
import net.minecraft.server.v1_7_R3.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import net.minelink.ctplus.compat.api.NpcIdentity;
import net.minelink.ctplus.compat.api.NpcNameGeneratorFactory;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class NpcPlayer extends EntityPlayer {

    private NpcIdentity identity;

    private NpcPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
    }

    public NpcIdentity getNpcIdentity() {
        return identity;
    }

    public static NpcPlayer valueOf(Player player) {
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
        PlayerInteractManager playerInteractManager = new PlayerInteractManager(worldServer);
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), NpcNameGeneratorFactory.getNameGenerator().generate(player));

        for (Map.Entry<String, Property> entry: ((CraftPlayer) player).getProfile().getProperties().entries()) {
            gameProfile.getProperties().put(entry.getKey(), entry.getValue());
        }

        NpcPlayer npcPlayer = new NpcPlayer(minecraftServer, worldServer, gameProfile, playerInteractManager);
        npcPlayer.identity = new NpcIdentity(player);

        new NpcPlayerConnection(npcPlayer);

        return npcPlayer;
    }

}
