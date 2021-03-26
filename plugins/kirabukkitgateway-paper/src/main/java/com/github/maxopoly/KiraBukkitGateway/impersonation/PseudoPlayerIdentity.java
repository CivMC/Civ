package com.github.maxopoly.KiraBukkitGateway.impersonation;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;

public class PseudoPlayerIdentity extends EntityPlayer {

	public PseudoPlayerIdentity(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile,
			PlayerInteractManager playerinteractmanager) {
		super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
	}

	public static EntityPlayer generate(UUID uuid, String name) {
		MinecraftServer minecraftServer = MinecraftServer.getServer();
		WorldServer worldServer = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
		PlayerInteractManager playerInteractManager = new PlayerInteractManager(worldServer);
		GameProfile gameProfile = new GameProfile(uuid, name);
		return new PseudoPlayerIdentity(minecraftServer, worldServer, gameProfile, playerInteractManager);
	}

}
