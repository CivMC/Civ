package com.github.maxopoly.KiraBukkitGateway.impersonation;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;


public class PseudoPlayerIdentity extends ServerPlayer {

	public PseudoPlayerIdentity(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile) {
		super(minecraftserver, worldserver, gameprofile);
	}

	public static ServerPlayer generate(UUID uuid, String name) {
		MinecraftServer minecraftServer = MinecraftServer.getServer();
		ServerLevel worldServer = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
		GameProfile gameProfile = new GameProfile(uuid, name);
		return new PseudoPlayerIdentity(minecraftServer, worldServer, gameProfile);
	}

}
