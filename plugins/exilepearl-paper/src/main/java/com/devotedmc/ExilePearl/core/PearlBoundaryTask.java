package com.devotedmc.ExilePearl.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import com.devotedmc.ExilePearl.BorderHandler;
import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.event.PlayerFreedEvent;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.google.common.collect.ImmutableList;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.Location;
import org.bukkit.World;

import static vg.civcraft.mc.civmodcore.util.TextUtil.*;

/**
 * This class tracks the pearls players that are online and prevents them
 * from entering the zone around their pearl location.
 * <p>
 * It also prevents them from entering bastion fields that they don't
 * have permission on. (this part isn't done yet)
 * 
 * @author Gordon
 */
final class PearlBoundaryTask extends ExilePearlTask implements BorderHandler {

	private Set<UUID> pearledPlayers = new HashSet<UUID>();
	
	private int radius = 1000;
	private double bastionDamage = 1;
	
	public static final int KNOCKBACK = 3; 
	
	//these material IDs are acceptable for places to teleport player; breathable blocks and water
	public static final LinkedHashSet<Integer> safeOpenBlocks = new LinkedHashSet<Integer>(Arrays.asList(
		 new Integer[] { 
				 0, 6, 8, 9, 27, 28, 30, 31, 32, 37, 38, 39, 40, 50, 55, 
				 59, 63, 64, 65, 66, 68, 69, 70, 71, 72, 75, 76, 77, 78, 
				 83, 90, 93, 94, 96, 104, 105, 106, 115, 131, 132, 141, 
				 142, 149, 150, 157, 171}
	));

	//these material IDs are ones we don't want to drop the player onto, like cactus or lava or fire or activated Ender portal
	public static final LinkedHashSet<Integer> painfulBlocks = 
			new LinkedHashSet<Integer>(Arrays.asList( new Integer[] {10, 11, 51, 81, 119} ));

	public PearlBoundaryTask(final ExilePearlApi pearlApi) {
		super(pearlApi);
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
	public void start() {		
		
		// Track any players who are already online
		for(ExilePearl p : pearlApi.getPearls()) {
			Player player = p.getPlayer();
			if (player != null && player.isOnline()) {
				pearledPlayers.add(player.getUniqueId());
			}
		}
		
		super.start();
		if (enabled) {
			pearlApi.log("Using pearl radius value of %d.", radius);
		}
	}

	@Override
	public void run() {
		if (radius == 0)
			return;

		Collection<UUID> players = ImmutableList.copyOf(pearledPlayers);

		for (UUID uid : players) {
			checkPlayer(uid);
		}
	}


	/**
	 * Checks whether a player needs to be snapped back
	 * @param player The player to check
	 */
	private void checkPlayer(UUID playerId) {
		Player player = pearlApi.getPlayer(playerId);
		
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
		
		checkBastion(player);

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
		double distance = Math.sqrt(Math.pow(pearlLocation.getX() - playerLocation.getX(), 2) + Math.pow(pearlLocation.getZ() - playerLocation.getZ(), 2));
		if (distance >= radius) {
			return;
		}

		Location newLoc = getCorrectedLocation(pearlLocation, playerLocation, pearl.getPlayer().isFlying());
		
		player.teleport(newLoc, TeleportCause.PLUGIN);
		msg(pearl.getPlayer(), "<i>You can't come within %d blocks of your pearl at (%d, %d).", radius, 
				pearl.getLocation().getBlockX(), pearl.getLocation().getBlockZ());
	}
	
	private Location getCorrectedLocation(Location pearlLocation, Location playerLocation, boolean flying) {
		
		double x = pearlLocation.getX();
		double z = pearlLocation.getZ();
		double xLoc = playerLocation.getX();
		double zLoc = playerLocation.getZ();
		double yLoc = playerLocation.getY();
		double radiusSquared = (radius * radius) + 5;

		// algorithm originally from: http://stackoverflow.com/questions/300871/best-way-to-find-a-point-on-a-circle-closest-to-a-given-point
		// modified by Lang Lukas to support elliptical border shape

		//Transform the ellipse to a circle with radius 1 (we need to transform the point the same way)
		double dX = xLoc - x;
		double dZ = zLoc - z;
		double dU = Math.sqrt(dX *dX + dZ * dZ); //distance of the untransformed point from the center
		double dT = Math.sqrt(dX *dX / radiusSquared + dZ * dZ / radiusSquared); //distance of the transformed point from the center
		double f = (1 / dT + KNOCKBACK / dU); //"correction" factor for the distances

		xLoc = x + dX * f;
		zLoc = z + dZ * f;
		
		int ixLoc = Location.locToBlock(xLoc);
		int izLoc = Location.locToBlock(zLoc);
		
		Location wbTest = new Location(playerLocation.getWorld(), xLoc, yLoc, zLoc);
		
		// Jump to other side of circle if this location is outside the world border
		if (!pearlApi.isLocationInsideBorder(wbTest)) {
			xLoc = x - dX * f;
			zLoc = z - dZ * f;
			ixLoc = Location.locToBlock(xLoc);
			izLoc = Location.locToBlock(zLoc);
		}

		// Make sure the chunk we're checking in is actually loaded
		Chunk tChunk = pearlLocation.getWorld().getChunkAt(ixLoc >> 4, ixLoc >> 4);
		if (!tChunk.isLoaded()) {
			tChunk.load();
		}

		yLoc = getSafeY(playerLocation.getWorld(), ixLoc, Location.locToBlock(yLoc), izLoc, flying);
		if (yLoc == -1)
			return null;

		return new Location(playerLocation.getWorld(), Math.round(xLoc) + 0.5, yLoc, Math.round(zLoc) + 0.5, playerLocation.getYaw(), playerLocation.getPitch());
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
	
	
	/**
	 * Checks if a particular spot consists of 2 breathable blocks over something relatively solid
	 * @param world The world
	 * @param X The x coordinate
	 * @param Y The y coordinate
	 * @param Z The z coordinate
	 * @param flying whether the player is flying
	 * @return true if the spot is safe
	 */
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
	

	/**
	 * Tracks pearled players that log in 
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (pearlApi.isPlayerExiled(e.getPlayer())) {
			pearledPlayers.add(e.getPlayer().getUniqueId());
		}
	}
	
	/**
	 * Stops tracking pearled players that log out
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent e) {
		pearledPlayers.remove(e.getPlayer().getUniqueId());
	}

	/**
	 * Tracks newly pearled players
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPearled(PlayerPearledEvent e) {
		Player p = e.getPearl().getPlayer();
		if (p != null && p.isOnline()) {
			pearledPlayers.add(e.getPearl().getPlayerId());
		}
	}
	
	/**
	 * Stops tracking freed players
	 * @param e The event args
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerFreed(PlayerFreedEvent e) {
		pearledPlayers.remove(e.getPearl().getPlayerId());
	}
	
	@Override
	public void loadConfig(PearlConfig config) {
		this.radius = config.getRulePearlRadius();
		this.bastionDamage = config.getBastionDamage();
	}
	
	/**
	 * Gets whether a player is being tracked
	 * @param player The player to check
	 * @return true if the player is being tracked
	 */
	public boolean isPlayerTracked(Player player) {
		return pearledPlayers.contains(player.getUniqueId());
	}
	
	/**
	 * Checks whether the exiled player is inside any bastion fields they don't have
	 * permission on and deals them damage if they are.
	 * @param player The player to check
	 */
	private void checkBastion(Player player) {
		if (pearlApi.isPlayerInUnpermittedBastion(player) && player.getHealth() > 0) {
			player.setHealth(Math.max(0, player.getHealth() - bastionDamage));
			msg(player, "<b>You aren't allowed in this bastion field when exiled.");
		}
	}
}
