package com.devotedmc.ExilePearl.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlConfig;
import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.Location;
import org.bukkit.World;

class PearlBoundaryTask extends ExilePearlTask {

	private final PearlConfig config;

	// Tracks players who are being moved outside the pearl border.
	private Set<UUID> handlingPlayers = Collections.synchronizedSet(new LinkedHashSet<UUID>());
	
	//these material IDs are acceptable for places to teleport player; breathable blocks and water
	public static final LinkedHashSet<Integer> safeOpenBlocks = new LinkedHashSet<Integer>(Arrays.asList(
		 new Integer[] {0, 6, 8, 9, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 55, 59, 63, 64, 65, 66, 68, 69, 70, 71, 72, 75, 76, 77, 78, 83, 90, 93, 94, 96, 104, 105, 106, 115, 131, 132, 141, 142, 149, 150, 157, 171}
	));

	//these material IDs are ones we don't want to drop the player onto, like cactus or lava or fire or activated Ender portal
	public static final LinkedHashSet<Integer> painfulBlocks = new LinkedHashSet<Integer>(Arrays.asList(
		 new Integer[] {10, 11, 51, 81, 119}
	));

	public PearlBoundaryTask(final ExilePearlApi pearlApi) {
		super(pearlApi);

		this.config = pearlApi.getPearlConfig();
	}


	@Override
	public String getTaskName() {
		return "Pearl Boundary";
	}


	@Override
	public int getTickInterval() {
		return TICKS_PER_SECOND;
	}

	@Override
	public void run()
	{
		// if radius is set to 0, simply return
		if (config.getRulePearlRadius() == 0)
			return;

		Collection<Player> players = ImmutableList.copyOf(Bukkit.getServer().getOnlinePlayers());

		for (Player player : players) {
			checkPlayer(player);
		}
	}


	/**
	 * Checks whether a player needs to be snapped back
	 * @param player The player to check
	 */
	private void checkPlayer(Player player) {
		if (player == null || !player.isOnline()) {
			return;
		}

		// Ignore creative/spectating players
		if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
			return;
		}

		// Ignore if not pearled
		ExilePearl pearl = pearlApi.getPearl(player.getUniqueId());
		if (pearl == null) {
			return;
		}

		// Ignore non-block holders
		if (!pearl.getHolder().isBlock()) {
			return;
		}

		Location pearlLocation = pearl.getLocation();
		Location playerLocation = player.getLocation();

		// Ignore if different world
		if (pearlLocation.getWorld() != playerLocation.getWorld()) {
			return;
		}

		// Ignore if player outside the radius
		if (pearlLocation.distance(playerLocation) >= config.getRulePearlRadius()) {
			return;
		}

		// tag this player as being handled so we can't get stuck in a loop due to Bukkit currently sometimes repeatedly providing incorrect location through teleport event
		handlingPlayers.add(player.getUniqueId());

		Location newLoc = getCorrectedLocation(pearlLocation, playerLocation, pearl.getPlayer().getPlayer().isFlying());
		player.teleport(newLoc, TeleportCause.PLUGIN);
		
		handlingPlayers.remove(player.getName().toLowerCase());
	}
	
	private Location getCorrectedLocation(Location pearlLocation, Location playerLocation, boolean flying) {
		
		double x = pearlLocation.getX();
		double z = pearlLocation.getZ();
		double xLoc = playerLocation.getX();
		double zLoc = playerLocation.getZ();
		double yLoc = playerLocation.getY();
		double radius = config.getRulePearlRadius();
		double knockback = 5;
		boolean wrapping = false;

		// algorithm originally from: http://stackoverflow.com/questions/300871/best-way-to-find-a-point-on-a-circle-closest-to-a-given-point
		// modified by Lang Lukas to support elliptical border shape

		//Transform the ellipse to a circle with radius 1 (we need to transform the point the same way)
		double dX = xLoc - x;
		double dZ = zLoc - z;
		double dU = Math.sqrt(dX *dX + dZ * dZ); //distance of the untransformed point from the center
		double dT = Math.sqrt(dX *dX / radius + dZ * dZ / radius); //distance of the transformed point from the center
		double f = (1 / dT - knockback / dU); //"correction" factor for the distances
		if (wrapping)
		{
			xLoc = x - dX * f;
			zLoc = z - dZ * f;
		} else {
			xLoc = x + dX * f;
			zLoc = z + dZ * f;
		}

		int ixLoc = Location.locToBlock(xLoc);
		int izLoc = Location.locToBlock(zLoc);

		// Make sure the chunk we're checking in is actually loaded
		Chunk tChunk = pearlLocation.getWorld().getChunkAt(ixLoc >> 4, ixLoc >> 4);
		if (!tChunk.isLoaded()) {
			tChunk.load();
		}

		yLoc = getSafeY(playerLocation.getWorld(), ixLoc, Location.locToBlock(yLoc), izLoc, flying);
		if (yLoc == -1)
			return null;

		return new Location(playerLocation.getWorld(), Math.floor(xLoc) + 0.5, yLoc, Math.floor(zLoc) + 0.5, playerLocation.getYaw(), playerLocation.getPitch());
	}
	
	// find closest safe Y position from the starting position
	private double getSafeY(World world, int X, int Y, int Z, boolean flying)
	{
		// artificial height limit of 127 added for Nether worlds since CraftBukkit still incorrectly returns 255 for their max height, leading to players sent to the "roof" of the Nether
		final boolean isNether = world.getEnvironment() == World.Environment.NETHER;
		int limTop = isNether ? 125 : world.getMaxHeight() - 2;
		final int highestBlockBoundary = Math.min(world.getHighestBlockYAt(X, Z) + 1, limTop);

		// if Y is larger than the world can be and user can fly, return Y - Unless we are in the Nether, we might not want players on the roof
		if (flying && Y > limTop && !isNether)
			return (double) Y;

		// make sure Y values are within the boundaries of the world.
		if (Y > limTop)
		{
			if (isNether) 
				Y = limTop; // because of the roof, the nether can not rely on highestBlockBoundary, so limTop has to be used
			else
			{
				if (flying)
					Y = limTop;
				else
					Y = highestBlockBoundary; // there will never be a save block to stand on for Y values > highestBlockBoundary
			}
		}
		if (Y < 0)
			Y = 0;

		// for non Nether worlds we don't need to check upwards to the world-limit, it is enough to check up to the highestBlockBoundary, unless player is flying
		if (!isNether && !flying)
			limTop = highestBlockBoundary;
		// Expanding Y search method adapted from Acru's code in the Nether plugin

		for(int y1 = Y, y2 = Y; (y1 > 0) || (y2 < limTop); y1--, y2++){
			// Look below.
			if(y1 > 0)
			{
				if (isSafeSpot(world, X, y1, Z, flying))
					return (double)y1;
			}

			// Look above.
			if(y2 < limTop && y2 != y1)
			{
				if (isSafeSpot(world, X, y2, Z, flying))
					return (double)y2;
			}
		}

		return -1.0;	// no safe Y location?!?!? Must be a rare spot in a Nether world or something
	}
	
	// check if a particular spot consists of 2 breathable blocks over something relatively solid
	@SuppressWarnings("deprecation")
	private boolean isSafeSpot(World world, int X, int Y, int Z, boolean flying)
	{
		boolean safe = safeOpenBlocks.contains((Integer)world.getBlockTypeIdAt(X, Y, Z))		// target block open and safe
					&& safeOpenBlocks.contains((Integer)world.getBlockTypeIdAt(X, Y + 1, Z));	// above target block open and safe
		if (!safe || flying)
			return safe;

		Integer below = (Integer)world.getBlockTypeIdAt(X, Y - 1, Z);
		return (safe
			 && (!safeOpenBlocks.contains(below) || below == 8 || below == 9)	// below target block not open/breathable (so presumably solid), or is water
			 && !painfulBlocks.contains(below)									// below target block not painful
			);
	}
}
