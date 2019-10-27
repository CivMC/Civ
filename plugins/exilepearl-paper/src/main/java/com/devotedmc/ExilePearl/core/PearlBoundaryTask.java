package com.devotedmc.ExilePearl.core;

import static vg.civcraft.mc.civmodcore.util.TextUtil.msg;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import com.devotedmc.ExilePearl.BorderHandler;
import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.event.PlayerFreedEvent;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.devotedmc.ExilePearl.util.BastionWrapper;
import com.google.common.collect.ImmutableList;

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

	private Set<UUID> pearledPlayers = new HashSet<>();

	private int radius = 1000;
	private double bastionDamage = 1;

	public static final int KNOCKBACK = 3; 

	//these material IDs are acceptable for places to teleport player; breathable blocks and water	
	public static final LinkedHashSet<Material> safeOpenBlocks = new LinkedHashSet<>(Arrays.asList(
			 new Material[] { 
					Material.AIR, Material.WATER, Material.RAIL, Material.ACTIVATOR_RAIL, 
					Material.POWERED_RAIL, Material.DETECTOR_RAIL, Material.GRASS, Material.FERN,
					Material.LARGE_FERN, Material.DEAD_BUSH, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
					Material.TORCH, Material.REDSTONE_WIRE, Material.WHEAT, Material.LADDER, Material.LEVER, Material.STONE_PRESSURE_PLATE}
		));

	public static final LinkedHashSet<Material> okBaseBlocks = new LinkedHashSet<>();

	//these material IDs are ones we don't want to drop the player onto, like cactus or lava or fire or activated Ender portal
	public static final LinkedHashSet<Material> painfulBlocks = 
			new LinkedHashSet<>(Arrays.asList( new Material[] {Material.LAVA, Material.FIRE, Material.CACTUS, Material.END_PORTAL} ));
	
	static {
		safeOpenBlocks.addAll(Tag.BUTTONS.getValues());
		safeOpenBlocks.addAll(Tag.CARPETS.getValues());
		okBaseBlocks.addAll(Tag.LOGS.getValues());
		okBaseBlocks.addAll(Tag.LEAVES.getValues());
	}

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

		// Ignore if dead already
		if (player.isDead()) {
			return;
		}

		if (!pushoutBastion(player)) {
			checkBastion(player);
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
		double distance = Math.sqrt(Math.pow(pearlLocation.getX() - playerLocation.getX(), 2) + Math.pow(pearlLocation.getZ() - playerLocation.getZ(), 2));
		if (distance >= radius) {
			return;
		}

		Location newLoc = getCorrectedLocation(pearlLocation, playerLocation, pearl.getPlayer().isFlying());
		if (newLoc != null) {
			player.teleport(newLoc, TeleportCause.PLUGIN);
			msg(pearl.getPlayer(), "<i>You can't come within %d blocks of your pearl at (%d, %d).", radius, 
				pearl.getLocation().getBlockX(), pearl.getLocation().getBlockZ());
		}
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
		if (yLoc == -1) {
			return null;
		}

		return new Location(playerLocation.getWorld(), Math.round(xLoc) + 0.5, yLoc, Math.round(zLoc) + 0.5, playerLocation.getYaw(), playerLocation.getPitch());
	}

	// find closest safe Y position from the starting position
	private double getSafeY(World world, int X, int Y, int Z, boolean flying)
	{
		// artificial height limit of 127 added for Nether worlds since CraftBukkit still incorrectly returns 255 for their max height, leading to players sent to the "roof" of the Nether
		final boolean isNether = world.getEnvironment() == World.Environment.NETHER;
		int limTop = isNether ? 125 : (world.getMaxHeight() - 2);
		final int highestBlockBoundary = Math.min(world.getHighestBlockYAt(X, Z) + 1, limTop);

		// if Y is larger than the world can be and user can fly, return Y - Unless we are in the Nether, we might not want players on the roof
		if (flying && Y > limTop && !isNether)
			return Y;

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
		if (!isNether && !flying) {
			limTop = highestBlockBoundary;
			// Expanding Y search method adapted from Acru's code in the Nether plugin

			// look full up first. Start at highest Y; we want to prefer landing on the surface.
			int lastOkY = -1;
			for(int y2 = limTop - 1; (y2 >= Y); y2--){
				// Look above.
				if (isSafeSpot(world, X, y2, Z, flying)) {
					Material below = world.getBlockAt(X, y2 - 1, Z).getType();
					if (okBaseBlocks.contains(below)) {
						lastOkY = y2;
					} else {
						return y2;
					}
				}
			}
			// We don't prefer to teleport into trees, but beats caves. If we found an option, take it.
			if (lastOkY > -1) {
				return lastOkY;
			}
		} else {
			// look full up first, starting from active Y (try to preserve level height for nether and flying)
			for(int y2 = Y; (y2 > limTop); y2++){
				// Look above.
				if(y2 < limTop)
				{
					if (isSafeSpot(world, X, y2, Z, flying))
						return y2;
				}
			}
		}
		// the look fully below.
		for (int y1 = Y; (y1 > 0); y1--) {
			// Look below.
			if(y1 > 0)
			{
				if (isSafeSpot(world, X, y1, Z, flying))
					return y1;
			}
		}
		// this should help prevent so many cavespawns on pushback.

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
	private boolean isSafeSpot(World world, int X, int Y, int Z, boolean flying)
	{
		boolean safe = safeOpenBlocks.contains(world.getBlockAt(X, Y, Z).getType())		// target block open and safe
					&& safeOpenBlocks.contains(world.getBlockAt(X, Y + 1, Z).getType());	// above target block open and safe
		if (!safe || flying)
			return safe;

		Material below = world.getBlockAt(X, Y - 1, Z).getType();
		return (safe
			 && (!safeOpenBlocks.contains(below) || below == Material.WATER)	// below target block not open/breathable (so presumably solid), or is water
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
			player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_HURT, 1.0F, 1.0F);
			player.playEffect(EntityEffect.HURT);
			msg(player, "<b>You aren't allowed in this bastion field when exiled.");
		}
	}

	/**
	 * Makes a best effort to push a player out of overlapping bastion fields using some fancy vector math.
	 * Will work with all current types of bastions, square and circle, and resolves the vector intersections
	 * with ease ... if I remember my maths.
	 * @param player The player to check
	 * @return true if the player was in a bastion field AND could be pushed back safely, false otherwise.
	 *    False does _not_ mean they are not in a bastion field.
	 */
	private boolean pushoutBastion(Player player) {
		List<BastionWrapper> bastions = pearlApi.getPlayerInUnpermittedBastion(player);
		if (bastions.isEmpty()) {
			return false;
		}
		Location loc = player.getLocation().clone();
		Vector v = null;
		for (BastionWrapper computeShell : bastions) {
			if (v == null) {
				v = computeShell.getPushout(loc, KNOCKBACK);
			} else {
				Vector p = computeShell.getPushout(loc, KNOCKBACK);
				if (p != null) {
					v.add(p);
				}
			}
		}
		if (v != null) {
			Location tLoc = loc.add(v);

			double yLoc = getSafeY(loc.getWorld(), tLoc.getBlockX(), tLoc.getBlockY(), tLoc.getBlockZ(), player.isFlying());
			if (yLoc == -1)
				return false;

			Location newLoc = new Location(loc.getWorld(), tLoc.getX(), yLoc, tLoc.getZ(), tLoc.getYaw(), tLoc.getPitch());

			player.teleport(newLoc, TeleportCause.PLUGIN);

			msg(player, "<b>You aren't allowed to enter this bastion field when exiled.");
			return true;
		}

		return false;
	}
}
