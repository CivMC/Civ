package me.josvth.randomspawn.spawn;

import isaac.bastion.Bastion;
import isaac.bastion.manager.BastionBlockManager;
import me.josvth.randomspawn.handlers.YamlHandler;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class BlockingSpawnSelector implements SpawnSelector {

	private static final Set<Material> DEFAULT_BLACKLIST = Set.of(
			Material.WATER, Material.LAVA, Material.FIRE, Material.CACTUS, Material.MAGMA_BLOCK);


	private final Random random = new Random();
	private final Plugin plugin;
	private final YamlHandler yamlHandler;
	private final Logger logger;

	public BlockingSpawnSelector(Plugin plugin, YamlHandler yamlHandler, Logger logger) {
		this.plugin = plugin;
		this.yamlHandler = yamlHandler;
		this.logger = logger;
	}

	@Override
	public Location getRandomSpawnLocation(World world) {
		return getRandomSpawnLocationAsync(world, true).join();
	}

	protected CompletableFuture<Location> getRandomSpawnLocationAsync(World world, boolean block) {
		String worldName = world.getName();
		Set<Material> blacklist = getMaterialBlackList(worldName);
		if (yamlHandler.worlds.getBoolean(worldName + ".spawnbyplayer")) {
			List<Player> playersOnline = world.getPlayers();

			if (!playersOnline.isEmpty()) {
				Player randomPlayer = playersOnline.get((int) (Math.random() * playersOnline.size()));
				Location spawnNear = randomPlayer.getLocation();

				double radius = yamlHandler.worlds.getDouble(worldName + ".spawnbyplayerarea.radius", 500);

				double exclusionRadius = yamlHandler.worlds.getDouble(worldName + ".spawnbyplayerarea.exclusionradius",
						0);

				return chooseSpawn(radius, exclusionRadius, spawnNear, blacklist, block);
			}
		}

		String type = yamlHandler.worlds.getString(worldName + ".spawnarea.type", "square");
		CompletableFuture<Location> ret;
		if (type.equalsIgnoreCase("square")) {
			double xmin = yamlHandler.worlds.getDouble(worldName + ".spawnarea.x-min", -100);
			double xmax = yamlHandler.worlds.getDouble(worldName + ".spawnarea.x-max", 100);
			double zmin = yamlHandler.worlds.getDouble(worldName + ".spawnarea.z-min", -100);
			double zmax = yamlHandler.worlds.getDouble(worldName + ".spawnarea.z-max", 100);
			// Spawn area thickness near border. If 0 spawns whole area
			int thickness = yamlHandler.worlds.getInt(worldName + ".spawnarea.thickness", 0);

			// square can block too bad (TODO fix)
			ret = CompletableFuture.completedFuture(chooseSpawn(world, xmin, xmax, zmin, zmax, thickness, blacklist, block));
		} else if (type.equalsIgnoreCase("circle")) {
			double exclusionRadius = yamlHandler.worlds.getDouble(worldName + ".spawnarea.exclusionradius", 0);
			double radius = yamlHandler.worlds.getDouble(worldName + ".spawnarea.radius", 100);
			double xcenter = yamlHandler.worlds.getDouble(worldName + ".spawnarea.xcenter", 0);
			double zcenter = yamlHandler.worlds.getDouble(worldName + ".spawnarea.zcenter", 0);

			ret = chooseSpawn(radius, exclusionRadius, new Location(world, xcenter, 0, zcenter), blacklist, block);
		} else {
			return CompletableFuture.completedFuture(null);
		}

		if (Bukkit.getServer().getPluginManager().isPluginEnabled("Bastion")) {
			ret = ret.thenCompose(location -> {
				BastionBlockManager bm = Bastion.getBastionManager();
				if (bm != null) {
					if (!bm.getBlockingBastions(location).isEmpty()) {
						return getRandomSpawnLocationAsync(world, block);
					}
				}
				return CompletableFuture.completedFuture(location);
			});
		}

		return ret.thenApply(location -> {
			// Spawn the player at the centre of the block
			location.setX(location.getBlockX() + 0.5);
			location.setZ(location.getBlockZ() + 0.5);
			return location;
		});
	}

	@Override
	public Location getSpawnPointLocation(World world) {
		return getSpawnPointLocationAsync(world, true).join();
	}

	protected CompletableFuture<Location> getSpawnPointLocationAsync(World world, boolean block) {
		String worldName = world.getName();

		if (!yamlHandler.worlds.contains(worldName + ".spawnpoints")) {
			return CompletableFuture.completedFuture(null);
		}

		Set<Material> blacklist = getMaterialBlackList(world.getName());

		// For each potential spawn location ...
		ConfigurationSection spawnpoints = yamlHandler.worlds.getConfigurationSection(worldName + ".spawnpoints");
		if (spawnpoints == null) {
			return CompletableFuture.completedFuture(null);
		}

		List<String> keys = new ArrayList<>(spawnpoints.getKeys(false));
		Collections.shuffle(keys, random);

		return getSpawnPoint(new AtomicInteger(0), keys, spawnpoints, world, blacklist, block);
	}

	private CompletableFuture<Location> getSpawnPoint(AtomicInteger index, List<String> keys, ConfigurationSection spawnpoints, World world, Set<Material> blacklist, boolean block) {
		return getSpawnPointAttempt(spawnpoints, keys.get(index.get()), world, blacklist, block).thenCompose(location -> {
			if (location == null) {
				if (index.incrementAndGet() >= keys.size()) {
					return CompletableFuture.completedFuture(null);
				}
				return getSpawnPointAttempt(spawnpoints, keys.get(index.get()), world, blacklist, block);
			} else {
				return CompletableFuture.completedFuture(location);
			}
		});
	}

	private CompletableFuture<Location> getSpawnPointAttempt(ConfigurationSection spawnpoints, String label, World world, Set<Material> blacklist, boolean block) {
		ConfigurationSection spawnpoint = spawnpoints.getConfigurationSection(label);
		Location location = new Location(world, spawnpoint.getDouble("x"), spawnpoint.getDouble("y"),
				spawnpoint.getDouble("z"));
		boolean skip = true;
		// check if a player must be nearby and look for a player if so
		if (spawnpoint.getBoolean("nearby")) {
			for (Player player : world.getPlayers()) {
				if (location.distance(player.getLocation()) < spawnpoint.getDouble("checkradius",
						spawnpoint.getDouble("radius", 0d))) {
					skip = false;
					break;
				}
			}
		} else { // doesn't require nearby player
			skip = false;
		}
		if (!skip) {
			// Now pick a location to spawn the player.
			return chooseSpawn(spawnpoint.getDouble("radius"), spawnpoint.getDouble("exclusion"),
					location, blacklist, block);
		}
		return CompletableFuture.completedFuture(null);
	}

	private CompletableFuture<Location> chooseSpawn(double radius, double exclusionRadius, Location center, Set<Material> blacklist, boolean block) {
		return chooseSpawnAttempt(block, new AtomicInteger(1000), radius, exclusionRadius, center, blacklist).thenApply(validSpawn -> {
			if (validSpawn != null) {
				return validSpawn.toLocation(center.getWorld());
			} else {
				return center;
			}
		});
	}

	private CompletableFuture<Vector> chooseSpawnAttempt(boolean block, AtomicInteger attempts, double radius, double exclusionRadius, Location center, Set<Material> blacklist) {
		return chooseOneSpawn(block, radius, exclusionRadius, center, blacklist).thenCompose(validSpawn -> {
			if (validSpawn == null) {
				if (attempts.decrementAndGet() <= 0) {
					return CompletableFuture.completedFuture(null);
				}
				return chooseSpawnAttempt(block, attempts, radius, exclusionRadius, center, blacklist);
			} else {
				return CompletableFuture.completedFuture(validSpawn);
			}
		});
	}

	private CompletableFuture<Vector> chooseOneSpawn(boolean block, double radius, double exclusionRadius, Location center, Set<Material> blacklist) {
		// Uniformly distributed in "annulus". Explanation: https://forum.unity.com/threads/random-point-within-circle-with-min-max-radius.597523/#post-8524934
		final double ex2 = exclusionRadius * exclusionRadius;
		final double r2 = radius * radius;

		final double r = Math.sqrt(Math.random() * (r2 - ex2) + ex2);
		final double phi = Math.random() * 2d * Math.PI;
		final double x = Math.round(center.getX() + Math.cos(phi) * r);
		final double z = Math.round(center.getZ() + Math.sin(phi) * r);

		return getValidY(center.getWorld(), x, z, blacklist, block).thenApply(y -> y == null ? null : new Vector(x, y, z));
	}

	private Location chooseSpawn(World world, double xmin, double xmax, double zmin, double zmax, double thickness,
															 Set<Material> blacklist, boolean block) {

		if (thickness <= 0) {
			for (int attempts = 1000; --attempts > 0;) {
				final double x = xmin + Math.random() * (xmax - xmin + 1);
				final double z = zmin + Math.random() * (zmax - zmin + 1);

				final Double y = getValidY(world, x, z, blacklist, block).join();
				if (y == null) {
					continue;
				}

				return new Location(world, x, y, z);
			}
		}
		else {
			for (int attempts = 1000; --attempts > 0;) {
				final double x, z;
				final int side = (int) (Math.random() * 4d);
				final double borderOffset = Math.random() * thickness;
				if (side == 0) {
					x = xmin + borderOffset;
					// Also balancing probability considering thickness
					z = zmin + Math.random() * (zmax - zmin + 1 - 2 * thickness) + thickness;
				}
				else if (side == 1) {
					x = xmax - borderOffset;
					z = zmin + Math.random() * (zmax - zmin + 1 - 2 * thickness) + thickness;
				}
				else if (side == 2) {
					x = xmin + Math.random() * (xmax - xmin + 1);
					z = zmin + borderOffset;
				}
				else {
					x = xmin + Math.random() * (xmax - xmin + 1);
					z = zmax - borderOffset;
				}

				final Double y = getValidY(world, x, z, blacklist, block).join();
				if (y == null) {
					continue;
				}

				return new Location(world, x, y, z);
			}
		}

		return new Location(world, xmin, 0, zmin);
	}

	/**
	 * Attempts to find a valid Y-level for a given X and Z coordinate.
	 *
	 * @return Returns a Y coordinate, or null if no valid Y-level could be found.
	 */
	private static CompletableFuture<Double> getValidY(
			final @NotNull World world,
			final double x,
			final double z,
			final @NotNull Set<Material> blacklist,
			boolean block
	) {
		CompletableFuture<Chunk> chunkAtAsync = block
				? CompletableFuture.completedFuture(world.getChunkAt(Math.floorDiv((int) x, 16), Math.floorDiv((int) z, 16)))
				: world.getChunkAtAsync(Math.floorDiv((int) x, 16), Math.floorDiv((int) z, 16));
		return chunkAtAsync.thenApply(chunk -> {
			Block floorBlock, feetBlock, headBlock;

			// Search the Nether for a valid Y from the bottom-up.
			if (world.getEnvironment() == World.Environment.NETHER) {
				// TODO: These are here due to an odd behaviour with vanilla nether. The roof-bedrock
				//       will be at y128, but the world-height is 256. Using the max-height API would
				//       mean that players occasionally get spawned above the roof. The empty blocks
				//       above the roof are AIR, not VOID_AIR, so you cannot check for that either.
				final int netherMinHeight = 0; // world.getMinHeight();
				final int netherMaxHeight = 128; // world.getMaxHeight();

				floorBlock = world.getBlockAt((int) x, netherMinHeight, (int) z);
				feetBlock = floorBlock.getRelative(0, 1, 0);
				headBlock = floorBlock.getRelative(0, 2, 0);
				while (headBlock.getY() < netherMaxHeight) {
					if (isValidSpawnLocation(headBlock, feetBlock, floorBlock, blacklist)) {
						return (double) feetBlock.getY();
					}
					final Block nextBlock = headBlock.getRelative(0, 1, 0);
					floorBlock = feetBlock;
					feetBlock = headBlock;
					headBlock = nextBlock;
				}
			}

			// Otherwise do a top-down search.
			else {
				// TODO: This is here to prevent players from rare instances of being spawned deep
				//       underground. Lava lakes typically only generate at around y30, so players
				//       unfortunate enough to be spawned underground should have relative safety.
				//       Keep in mind that this else also applies to other dimensions like the End.
				final int otherMinHeight = 40; // world.getMinHeight();

				headBlock = world.getBlockAt((int) x, world.getMaxHeight(), (int) z);
				feetBlock = headBlock.getRelative(0, -1, 0);
				floorBlock = headBlock.getRelative(0, -2, 0);
				while (floorBlock.getY() >= otherMinHeight) {
					if (isValidSpawnLocation(headBlock, feetBlock, floorBlock, blacklist)) {
						return (double) feetBlock.getY();
					}
					final Block nextBlock = floorBlock.getRelative(0, -1, 0);
					headBlock = feetBlock;
					feetBlock = floorBlock;
					floorBlock = nextBlock;
				}
			}

			return null;
		});
	}

	/**
	 * This is a 1x3 area predicate where the bottom-most block (ie the "floorBlock") must be solid, and the two block
	 * above it must not be solid. This function defers to Minecraft what "solid" means. See {@link Block#isSolid()} for
	 * more information there. Also, all blocks must not be made of blacklisted materials.
	 */
	private static boolean isValidSpawnLocation(
			final @NotNull Block headBlock,
			final @NotNull Block feetBlock,
			final @NotNull Block floorBlock,
			final @NotNull Set<Material> blackListedMaterials
	) {
		return floorBlock.isSolid() && !blackListedMaterials.contains(floorBlock.getType())
				&& !feetBlock.isSolid() && !blackListedMaterials.contains(feetBlock.getType())
				&& !headBlock.isSolid() && !blackListedMaterials.contains(headBlock.getType());
	}

	private Set<Material> getMaterialBlackList(String worldName) {
		if (yamlHandler.worlds.isList(worldName + ".spawnblacklist")) {
			Set<Material> result = new HashSet<>();
			List<String> matIdentifiers = yamlHandler.worlds.getStringList(worldName + ".spawnblacklist");
			for (String identifier : matIdentifiers) {
				Material mat;
				try {
					mat = Material.valueOf(identifier);
				}
				catch (IllegalArgumentException e) {
					logger.severe(identifier + " is not a valid material");
					continue;
				}
				result.add(mat);
			}
			return result;
		}
		return DEFAULT_BLACKLIST;
	}

}
