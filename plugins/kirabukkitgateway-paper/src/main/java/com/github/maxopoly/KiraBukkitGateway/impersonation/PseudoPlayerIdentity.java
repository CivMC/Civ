package com.github.maxopoly.KiraBukkitGateway.impersonation;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;


public class PseudoPlayerIdentity extends ServerPlayer {

    public PseudoPlayerIdentity(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile) {
        super(minecraftserver, worldserver, gameprofile, ClientInformation.createDefault());
    }

    public static ServerPlayer generate(UUID uuid, String name) {
        MinecraftServer minecraftServer = MinecraftServer.getServer();
        ServerLevel worldServer = ((CraftWorld) Bukkit.getWorlds().getFirst()).getHandle();
        GameProfile gameProfile = new GameProfile(uuid, name);
        return new PseudoPlayerIdentity(minecraftServer, worldServer, gameProfile);
    }

}
