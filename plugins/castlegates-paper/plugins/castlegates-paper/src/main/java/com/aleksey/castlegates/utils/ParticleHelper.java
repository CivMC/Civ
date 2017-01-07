package com.aleksey.castlegates.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.types.BlockCoord;
import com.aleksey.castlegates.types.Gearblock;

public class ParticleHelper {
	public static enum Type { Info, Warning }
	
	public static void spawn(Player player, Gearblock gearblock, Type type) {
		BlockCoord coord = gearblock.getCoord();
		Location location = new Location(player.getWorld(), 0.5 + coord.getX(), 0.5 + coord.getY(), 0.5 + coord.getZ());

		spawn(location, type);
	}
	
	public static void spawn(Block block, Type type) {
		Location location = new Location(block.getWorld(), 0.5 + block.getX(), 0.5 + block.getY(), 0.5 + block.getZ());

		spawn(location, type);
	}

	public static void spawn(Location location, Type type) {
		final Particle particle = type == Type.Info ? Particle.ENCHANTMENT_TABLE: Particle.EXPLOSION_NORMAL;
		final float offsetX = 0;
		final float offsetY = 0;
		final float offsetZ = 0;
		final float speed = 0.5f;
		final int particleCount = 80;
		
		location.getWorld().spawnParticle(particle, location, particleCount, offsetX, offsetY, offsetZ, speed, null);
	}
}
