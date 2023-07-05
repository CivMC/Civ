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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;

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

		return ret;

	}

	private Location chooseSpawn(double radius, double exclusionRadius, Location center, List<Material> blacklist) {
		Location result = new Location(center.getWorld(), center.getX(), center.getY(), center.getZ());

		int maxTries = 50 * (int) radius;
		if (maxTries < 50) {
			maxTries = 50;
		}
		if (maxTries > 1000) {
			maxTries = 1000; // sensible limits plz
		}

		// Uniformly distributed in "annulus". Explanation: https://forum.unity.com/threads/random-point-within-circle-with-min-max-radius.597523/#post-8524934
		double ex2 = exclusionRadius * exclusionRadius;
		double r2 = radius * radius;
		do {
			double r = Math.sqrt(Math.random() * (r2 - ex2) + ex2);

			double phi = Math.random() * 2d * Math.PI;

			double x = Math.round(center.getX() + Math.cos(phi) * r);
			double z = Math.round(center.getZ() + Math.sin(phi) * r);

			result.setX(x);
			result.setZ(z);
			result.setY(getValidHighestY(center.getWorld(), x, z, blacklist));
		} while (result.getY() == -1 && --maxTries >= 0);
		return result;
	}

	private Location chooseSpawn(World world, double xmin, double xmax, double zmin, double zmax, double thickness,
			List<Material> blacklist) {
		Location result = new Location(world, xmin, 0, zmin);

		if (thickness <= 0) {
			do {
				double x = xmin + Math.random() * (xmax - xmin + 1);
				double z = zmin + Math.random() * (zmax - zmin + 1);

				result.setX(x);
				result.setZ(z);
				result.setY(getValidHighestY(world, x, z, blacklist));
			} while (result.getY() == -1);
		} else {
			do {
				double x = 0, z = 0;
				int side = (int) (Math.random() * 4d);
				double borderOffset = Math.random() * thickness;
				if (side == 0) {
					x = xmin + borderOffset;
					// Also balancing probability considering thickness
					z = zmin + Math.random() * (zmax - zmin + 1 - 2 * thickness) + thickness;
				} else if (side == 1) {
					x = xmax - borderOffset;
					z = zmin + Math.random() * (zmax - zmin + 1 - 2 * thickness) + thickness;
				} else if (side == 2) {
					x = xmin + Math.random() * (xmax - xmin + 1);
					z = zmin + borderOffset;
				} else {
					x = xmin + Math.random() * (xmax - xmin + 1);
					z = zmax - borderOffset;
				}

				result.setX(x);
				result.setZ(z);
				result.setY(getValidHighestY(world, x, z, blacklist));
			} while (result.getY() == -1);
		}
		return result;
	}

	private double getValidHighestY(World world, double x, double z, List<Material> blacklist) {
		world.getChunkAt(new Location(world, x, 0, z)).load();
		double y = 0;
		Material blockMat = Material.AIR;
		if (world.getEnvironment() == Environment.NETHER) {
			Material blockYMat = world.getBlockAt((int)x,(int) y,(int) z).getType();
			Material blockY2Mat = world.getBlockAt((int)x,(int) y+1,(int) z).getType();
			while (y < 128 && !(MaterialUtils.isAir(blockYMat) && MaterialUtils.isAir(blockY2Mat))) {
				y++;
				blockYMat = blockY2Mat;
				blockY2Mat = world.getBlockAt((int)x,(int) y+1,(int) z).getType();
			}
			if (y == 127)
				return -1;
		} else {
			y = 257;
			while (y >= 0 && MaterialUtils.isAir(blockMat)) {
				y--;
				blockMat = world.getBlockAt((int)x,(int) y,(int) z).getType();
			}
			if (y == 0)
				return -1;
			y++;
		}

		if (blacklist.contains(blockMat)) {
			return -1;
		}

		return y;
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
