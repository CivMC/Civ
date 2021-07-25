package com.github.maxopoly.KiraBukkitGateway.impersonation;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;

public class PseudoPlayerIdentity extends EntityPlayer {

	public PseudoPlayerIdentity(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile) {
		super(minecraftserver, worldserver, gameprofile);
	}

	public static EntityPlayer generate(UUID uuid, String name) {
		MinecraftServer minecraftServer = MinecraftServer.getServer();
		WorldServer worldServer = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
		GameProfile gameProfile = new GameProfile(uuid, name);
		return new PseudoPlayerIdentity(minecraftServer, worldServer, gameProfile);
	}

}
