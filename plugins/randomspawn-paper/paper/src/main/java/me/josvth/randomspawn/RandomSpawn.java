package me.josvth.randomspawn;

import isaac.bastion.Bastion;
import isaac.bastion.manager.BastionBlockManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import me.josvth.randomspawn.handlers.CommandHandler;
import me.josvth.randomspawn.handlers.YamlHandler;
import me.josvth.randomspawn.listeners.DamageListener;
import me.josvth.randomspawn.listeners.JoinListener;
import me.josvth.randomspawn.listeners.RespawnListener;
import me.josvth.randomspawn.listeners.SignListener;
import me.josvth.randomspawn.listeners.WorldChangeListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RandomSpawn extends JavaPlugin {

	public YamlHandler yamlHandler;
	CommandHandler commandHandler;
	RespawnListener respawnListener;
	JoinListener joinListener;
	WorldChangeListener worldChangeListener;
	SignListener signListener;
	DamageListener damageListener;

	private static List<Material> defaultBlackList = Arrays.asList(
			new Material[] { Material.WATER, Material.LAVA, Material.FIRE, Material.CACTUS, Material.MAGMA_BLOCK });

	@Override
	public void onEnable() {

		// setup handlers
		yamlHandler = new YamlHandler(this);
		logDebug("Yamls loaded!");

		commandHandler = new CommandHandler(this);
		logDebug("Commands registered!");

		// setup listeners
		respawnListener = new RespawnListener(this);
		joinListener = new JoinListener(this);
		worldChangeListener = new WorldChangeListener(this);
		signListener = new SignListener(this);
		damageListener = new DamageListener(this);
	}

	public void logInfo(String message) {
		getLogger().info(message);
	}

	public void logDebug(String message) {
		if (yamlHandler.config.getBoolean("debug", false)) {
			getLogger().info("(DEBUG) " + message);
		}
	}

	public void logWarning(String message) {
		getLogger().warning(message);
	}

	public void playerInfo(Player player, String message) {
		player.sendMessage(ChatColor.AQUA + "[RandomSpawn] " + ChatColor.RESET + message);
	}

	// *------------------------------------------------------------------------------------------------------------*
	// | The following chooseSpawn methods contains code made by NuclearW |
	// | based on his SpawnArea plugin: |
	// |
	// http://forums.bukkit.org/threads/tp-spawnarea-v0-1-spawns-targetPlayers-in-a-set-area-randomly-1060.20408/
	// |
	// *------------------------------------------------------------------------------------------------------------*

	public Location chooseSpawn(World world) {
		String worldName = world.getName();
		List<Material> blacklist = getMaterialBlackList(worldName);
		if (yamlHandler.worlds.getBoolean(worldName + ".spawnbyplayer")) {
			List<Player> playersOnline = world.getPlayers();

			if (!playersOnline.isEmpty()) {
				Player randomPlayer = playersOnline.get((int) (Math.random() * playersOnline.size()));
				Location spawnNear = randomPlayer.getLocation();

				double radius = yamlHandler.worlds.getDouble(worldName + ".spawnbyplayerarea.radius", 500);

				double exclusionRadius = yamlHandler.worlds.getDouble(worldName + ".spawnbyplayerarea.exclusionradius",
						0);

				return chooseSpawn(radius, exclusionRadius, spawnNear, blacklist);
			}
		}

		String type = yamlHandler.worlds.getString(worldName + ".spawnarea.type", "square");
		Location ret;
		if (type.equalsIgnoreCase("square")) {
			double xmin = yamlHandler.worlds.getDouble(worldName + ".spawnarea.x-min", -100);
			double xmax = yamlHandler.worlds.getDouble(worldName + ".spawnarea.x-max", 100);
			double zmin = yamlHandler.worlds.getDouble(worldName + ".spawnarea.z-min", -100);
			double zmax = yamlHandler.worlds.getDouble(worldName + ".spawnarea.z-max", 100);
			// Spawn area thickness near border. If 0 spawns whole area
			int thickness = yamlHandler.worlds.getInt(worldName + ".spawnarea.thickness", 0);

			ret = chooseSpawn(world, xmin, xmax, zmin, zmax, thickness, blacklist);
		} else if (type.equalsIgnoreCase("circle")) {
			double exclusionRadius = yamlHandler.worlds.getDouble(worldName + ".spawnarea.exclusionradius", 0);
			double radius = yamlHandler.worlds.getDouble(worldName + ".spawnarea.radius", 100);
			double xcenter = yamlHandler.worlds.getDouble(worldName + ".spawnarea.xcenter", 0);
			double zcenter = yamlHandler.worlds.getDouble(worldName + ".spawnarea.zcenter", 0);

			ret = chooseSpawn(radius, exclusionRadius, new Location(world, xcenter, 0, zcenter), blacklist);
		} else {
			return null;
		}

		if (getServer().getPluginManager().isPluginEnabled("Bastion")) {
			BastionBlockManager bm = Bastion.getBastionManager();
			if (bm != null) {
				if (!bm.getBlockingBastions(ret).isEmpty()) {
					return chooseSpawn(world); // TODO: infinite recursion seems rather dangerous
				}
			}
		}

		// Spawn the player at the centre of the block
		ret.setX(ret.getBlockX() + 0.5);
		ret.setZ(ret.getBlockZ() + 0.5);
		return ret;
	}

	private Location chooseSpawn(double radius, double exclusionRadius, Location center, List<Material> blacklist) {
		// Uniformly distributed in "annulus". Explanation: https://forum.unity.com/threads/random-point-within-circle-with-min-max-radius.597523/#post-8524934
		final double ex2 = exclusionRadius * exclusionRadius;
		final double r2 = radius * radius;

		for (int attempts = 1000; --attempts > 0;) {
			final double r = Math.sqrt(Math.random() * (r2 - ex2) + ex2);
			final double phi = Math.random() * 2d * Math.PI;
			final double x = Math.round(center.getX() + Math.cos(phi) * r);
			final double z = Math.round(center.getZ() + Math.sin(phi) * r);

			final Double y = getValidY(center.getWorld(), x, z, blacklist);
			if (y == null) {
				continue;
			}

			return new Location(center.getWorld(), x, y, z);
		}

		return center;
	}

	private Location chooseSpawn(World world, double xmin, double xmax, double zmin, double zmax, double thickness,
			List<Material> blacklist) {

		if (thickness <= 0) {
			for (int attempts = 1000; --attempts > 0;) {
				final double x = xmin + Math.random() * (xmax - xmin + 1);
				final double z = zmin + Math.random() * (zmax - zmin + 1);

				final Double y = getValidY(world, x, z, blacklist);
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

				final Double y = getValidY(world, x, z, blacklist);
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
	private static @Nullable Double getValidY(
			final @NotNull World world,
			final double x,
			final double z,
			final @NotNull List<Material> blacklist
	) {
		world.getChunkAt((int) x, (int) z).load();

		Block floorBlock, feetBlock, headBlock;

		// Search the Nether for a valid Y from the bottom-up.
		if (world.getEnvironment() == Environment.NETHER) {
			floorBlock = world.getBlockAt((int) x, world.getMinHeight(), (int) z);
			feetBlock = floorBlock.getRelative(0, 1, 0);
			headBlock = floorBlock.getRelative(0, 2, 0);
			while (headBlock.getY() < world.getMaxHeight()) {
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
			headBlock = world.getBlockAt((int) x, world.getMaxHeight(), (int) z);
			feetBlock = headBlock.getRelative(0, -1, 0);
			floorBlock = headBlock.getRelative(0, -2, 0);
			while (floorBlock.getY() >= world.getMinHeight()) {
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
			final @NotNull List<Material> blackListedMaterials // TODO: This should be a set
	) {
		return floorBlock.isSolid() && !blackListedMaterials.contains(floorBlock.getType())
				&& !feetBlock.isSolid() && !blackListedMaterials.contains(feetBlock.getType())
				&& !headBlock.isSolid() && !blackListedMaterials.contains(headBlock.getType());
	}

	// Methods for a safe landing :)
	public void sendGround(Player player, Location location) {
		if (!location.getChunk().isLoaded()) {
			location.getChunk().load();
		}
	}

	/**
	 * Finds all the valid spawn points from the full set of configured spawn points
	 * in the world. What is valid? Spawn points can configurably require another
	 * player to be nearby, or allow spawn there regardless of nearby players. This
	 * function checks that, and checks it against the online players. If a player
	 * is sufficiently near as determined by the "checkradius", or if not set, the
	 * "radius" of the spawn point, then use that point. Of course if a nearby
	 * player is not required, the spawn point is added.
	 * 
	 * The final result of these checks is returned as the set of eligible spawn
	 * points; from which one will ultimately be chosen and used.
	 * 
	 * @author ProgrammerDan programmerdan@gmail.com
	 * @param world The world to restrict the check to. Only players from that world
	 *              are considered.
	 * @return The set of locations near valid spawn points; or an empty list.
	 */
	public List<Location> findSpawnPoints(World world) {
		String worldName = world.getName();

		if (!yamlHandler.worlds.contains(worldName + ".spawnpoints")) {
			return new ArrayList<>(0);
		}

		LinkedList<Location> spawnLocs = new LinkedList<>();

		List<Material> blacklist = getMaterialBlackList(world.getName());

		// reserve list of online players : TODO make sure this is just online players
		List<Player> playersOnline = world.getPlayers();

		// For each potential spawn location ...
		ConfigurationSection spawnpoints = yamlHandler.worlds.getConfigurationSection(worldName + ".spawnpoints");
		if (spawnpoints == null) {
			return new ArrayList<>(0);
		}
		for (String spawnpointlabel : spawnpoints.getKeys(false)) {
			ConfigurationSection spawnpoint = spawnpoints.getConfigurationSection(spawnpointlabel);
			Location location = new Location(world, spawnpoint.getDouble("x"), spawnpoint.getDouble("y"),
					spawnpoint.getDouble("z"));
			boolean skip = true;
			// check if a player must be nearby and look for a player if so
			if (spawnpoint.getBoolean("nearby")) {
				for (Player player : playersOnline) {
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
				Location derive = chooseSpawn(spawnpoint.getDouble("radius"), spawnpoint.getDouble("exclusion"),
						location, blacklist);
				if (derive != null) {
					spawnLocs.add(derive);
				}
			}
		}

		return spawnLocs;
	}

	private List<Material> getMaterialBlackList(String worldName) {
		if (yamlHandler.worlds.isList(worldName + ".spawnblacklist")) {
			List<Material> result = new LinkedList<>();
			List<String> matIdentifiers = yamlHandler.worlds.getStringList(worldName + ".spawnblacklist");
			for (String identifier : matIdentifiers) {
				Material mat;
				try {
					mat = Material.valueOf(identifier);
				}
				catch (IllegalArgumentException e) {
					getLogger().severe(identifier + " is not a valid material");
					continue;
				}
				result.add(mat);
			}
			return result;
		}
		return defaultBlackList;
	}
}
