package com.aleksey.castlegates.utils;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.types.BlockCoord;
import com.aleksey.castlegates.types.Gearblock;

public class EffectHelper {
	public static enum Type { Info, Warning }
	
	public static void play(Player player, Gearblock gearblock, Type type) {
		BlockCoord coord = gearblock.getCoord();
		Location location = new Location(player.getWorld(), 0.5 + coord.getX(), 0.5 + coord.getY(), 0.5 + coord.getZ());

		play(location, type);
	}
	
	public static void play(Block block, Type type) {
		Location location = new Location(block.getWorld(), 0.5 + block.getX(), 0.5 + block.getY(), 0.5 + block.getZ());

		play(location, type);
	}

	public static void play(Location location, Type type) {
		final Effect effect = type == Type.Info ? Effect.FLYING_GLYPH: Effect.EXPLOSION;
		final int id = 0;
		final int data = 0;
		final float offsetX = 0;
		final float offsetY = 0;
		final float offsetZ = 0;
		final float speed = 0.5f;
		final int particleCount = 80;
		
		final int viewDistance = CastleGates.getConfigManager().getMaxBridgeLength()
				+ (int)CastleGates.getConfigManager().getMaxRedstoneDistance();

		location.getWorld().spigot().playEffect(location, effect, id, data, offsetX, offsetY, offsetZ, speed, particleCount, viewDistance);
	}
}
