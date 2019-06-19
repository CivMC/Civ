package com.github.maxopoly.KiraBukkitGateway.impersonation;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;

public class PseudoPlayerIdentity extends EntityPlayer {

	public PseudoPlayerIdentity(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile,
			PlayerInteractManager playerinteractmanager) {
		super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
	}

	public static PseudoPlayerIdentity generate(UUID uuid, String name) {
		MinecraftServer minecraftServer = MinecraftServer.getServer();
		WorldServer worldServer = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
		PlayerInteractManager playerInteractManager = new PlayerInteractManager(worldServer);
		GameProfile gameProfile = new GameProfile(uuid, name);
		return new PseudoPlayerIdentity(minecraftServer, worldServer, gameProfile, playerInteractManager);
	}

}
