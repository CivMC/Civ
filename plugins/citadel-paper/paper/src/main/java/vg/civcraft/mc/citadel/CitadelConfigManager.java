package vg.civcraft.mc.citadel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.citadel.acidtypes.AcidType;
import vg.civcraft.mc.citadel.model.WorldBorderBuffers;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementEffect;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;
import vg.civcraft.mc.civmodcore.config.ConfigParser;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

public class CitadelConfigManager extends ConfigParser {

	private ManagedDatasource database;
	private List<ReinforcementType> reinforcementTypes;
	private List<AcidType> acidTypes;

	private List<Material> globalBlackList;

	private boolean logHostileBreaks;
	private boolean logFriendlyBreaks;
	private boolean logDamage;
	private boolean logCreation;
	private boolean logMessages;

	private long globalDecayTimer;
	private double globalDecayMultiplier;

	private double redstoneRange;

	private boolean hangersInheritReinforcements;

	private int activityMapResolution;
	private int activityMapRadius;
	private long activityEntryRefreshAfterMs;
	private long activityRadiusRefreshAfterMs;

	private long activityDefault;
	private List<String> activityWorlds;

	private Map<UUID, WorldBorderBuffers> buffers;

	public CitadelConfigManager(ACivMod plugin) {
		super(plugin);
	}

	public int getActivityMapRadius() {
		return activityMapRadius;
	}

	public int getActivityMapResolution() {
		return activityMapResolution;
	}

	public long getActivityEntryRefreshAfterMs() {
		return activityEntryRefreshAfterMs;
	}

	public long getActivityRadiusRefreshAfterMs() {
		return activityRadiusRefreshAfterMs;
	}

	public long getActivityDefault() {
		return activityDefault;
	}

	public List<String> getActivityWorlds() {
		return activityWorlds;
	}

	public List<Material> getBlacklistedMaterials() {
		return globalBlackList;
	}

	public ManagedDatasource getDatabase() {
		return database;
	}

	public double getMaxRedstoneDistance() {
		return redstoneRange;
	}

	public boolean doHangersInheritReinforcements() {
		return hangersInheritReinforcements;
	}

	public Map<UUID, WorldBorderBuffers> getWorldBorderBuffers() {
		return Collections.unmodifiableMap(this.buffers);
	}

	private ReinforcementEffect getReinforcementEffect(ConfigurationSection config) {
		if (config == null) {
			return null;
		}
		Particle effect;
		try {
			String effectName = config.getString("type");
			effect = Particle.valueOf(effectName);
		} catch (IllegalArgumentException e) {
			logger.warning("Invalid effect at: " + config.getCurrentPath());
			return null;
		}
		float offSet = (float) config.getDouble("offset", 0);
		float offsetX = (float) config.getDouble("offsetX", offSet);
		float offsetY = (float) config.getDouble("offsetY", offSet);
		float offsetZ = (float) config.getDouble("offsetZ", offSet);
		float speed = (float) config.getDouble("speed", 1);
		int amount = config.getInt("particleCount", 50);
		return new ReinforcementEffect(effect, offsetX, offsetY, offsetZ, speed, amount);
	}

	public List<ReinforcementType> getReinforcementTypes() {
		return reinforcementTypes;
	}

	public List<AcidType> getAcidTypes() {
		return acidTypes;
	}

	public boolean logCreation() {
		return logCreation;
	}

	public boolean logDamage() {
		return logDamage;
	}

	public boolean logFriendlyBreaks() {
		return logFriendlyBreaks;
	}

	public boolean logHostileBreaks() {
		return logHostileBreaks;
	}

	public boolean logMessages() {
		return logMessages;
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		database = ManagedDatasource.construct((ACivMod) plugin, (DatabaseCredentials) config.get("database"));
		globalBlackList = ConfigHelper.parseMaterialList(config, "non_reinforceables");
		logHostileBreaks = config.getBoolean("logHostileBreaks", true);
		logFriendlyBreaks = config.getBoolean("logFriendlyBreaks", true);
		logDamage = config.getBoolean("logDamage", false);
		logCreation = config.getBoolean("logCreation", true);
		logMessages = config.getBoolean("logMessages", true);
		redstoneRange = config.getDouble("redstoneDistance", 3);
		globalDecayMultiplier = config.getDouble("global_decay_multiplier", 2.0);
		globalDecayTimer = ConfigHelper.parseTime(config.getString("global_decay_timer", "0"));
		parseReinforcementTypes(config.getConfigurationSection("reinforcements"));
		parseAcidTypes(config.getConfigurationSection("acids"));
		hangersInheritReinforcements = config.getBoolean("hangers_inherit_reinforcement", false);

		activityMapRadius = config.getInt("activity-map-radius", 1);
		activityMapResolution = config.getInt("activity-map-resolution", 512);
		activityEntryRefreshAfterMs = config.getLong("activity-entry-refresh-after-ms", 3 * 60L * 60L * 1000L);
		activityRadiusRefreshAfterMs = config.getLong("activity-radius-refresh-after-ms", 3 * 60L * 60L * 1000L);
		activityDefault = config.getLong("activity-default", System.currentTimeMillis());
		activityWorlds = config.getStringList("activity-map-worlds");

		parseWorldBorderBuffers(config.getConfigurationSection("world-border-buffers"));

		return true;
	}

	private ReinforcementType parseReinforcementType(ConfigurationSection config) {
		if (!config.isItemStack("item")) {
			logger.warning(
					"Reinforcement config at " + config.getCurrentPath() + " had no valid item entry, it was ignored");
			return null;
		}
		ItemStack item = config.getItemStack("item");
		ReinforcementEffect creationEffect = getReinforcementEffect(config.getConfigurationSection("creation_effect"));
		ReinforcementEffect damageEffect = getReinforcementEffect(config.getConfigurationSection("damage_effect"));
		ReinforcementEffect destructionEffect = getReinforcementEffect(
				config.getConfigurationSection("destruction_effect"));
		long gracePeriod = ConfigHelper.parseTime(config.getString("grace_period", "0"), TimeUnit.MILLISECONDS);
		long maturationTime = ConfigHelper.parseTime(config.getString("mature_time", "0"), TimeUnit.MILLISECONDS);
		long acidTime = ConfigHelper.parseTime(config.getString("acid_time", "-1"), TimeUnit.MILLISECONDS);
		int acidPriority = config.getInt("acid_priority", 0);
		String name = config.getString("name");
		double maturationScale = config.getInt("scale_amount", 1);
		float health = (float) config.getDouble("hit_points", 100);
		double returnChance = config.getDouble("return_chance", 1.0);
		List<Material> reinforceables = ConfigHelper.parseMaterialList(config, "reinforceables");
		List<Material> nonReinforceables = ConfigHelper.parseMaterialList(config, "non_reinforceables");
		short id = (short) config.getInt("id", -1);
		long decayTimer = ConfigHelper
				.parseTime(config.getString("decay_timer", String.valueOf(globalDecayTimer / 1000L) + "s"));
		double decayMultiplier = config.getDouble("decay_multiplier", globalDecayMultiplier);
		double multiplerOnDeletedGroup = config.getDouble("deleted_group_multipler", 4);
		int legacyId = config.getInt("legacy_id", -1);
		List<String> allowedWorlds = ConfigHelper.getStringList(config, "allowed_worlds");
		if (name == null) {
			logger.warning("No name specified for reinforcement type at " + config.getCurrentPath());
			name = item.getType().name();
		}
		if (id == -1) {
			logger.warning("Reinforcement type at " + config.getCurrentPath() + " had no id, it was ignored");
			return null;
		}
		if (!reinforceables.isEmpty() && !nonReinforceables.isEmpty()) {
			logger.warning("Both blacklist and whitelist specified for reinforcement type at " + config.getCurrentPath()
					+ ". This does not make sense and the type will be ignored");
			return null;
		}
		//TODO Should really be replaced with a toString() method
		logger.info("Parsed reinforcement type " + name + " for item " + item.toString() + ", returnChance: "
				+ returnChance + ", maturationTime: " + TextUtil.formatDuration(maturationTime, TimeUnit.MILLISECONDS)
				+ ", acidTime: " + TextUtil.formatDuration(acidTime, TimeUnit.MILLISECONDS) + ", gracePeriod: "
				+ gracePeriod + ", id: " + id + ", allowedWorlds: " + StringUtils.join(allowedWorlds, ", "));
		return new ReinforcementType(health, returnChance, item, maturationTime, acidTime, acidPriority, maturationScale, gracePeriod,
				creationEffect, damageEffect, destructionEffect, reinforceables, nonReinforceables, id, name,
				globalBlackList, decayTimer, decayMultiplier, multiplerOnDeletedGroup, legacyId, allowedWorlds);
	}

	private void parseReinforcementTypes(ConfigurationSection config) {
		reinforcementTypes = new LinkedList<>();
		if (config == null) {
			logger.info("No reinforcement types found in config");
			return;
		}
		for (String key : config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				logger.warning("Ignoring invalid entry " + key + " at " + config.getCurrentPath());
				continue;
			}
			ReinforcementType type = parseReinforcementType(config.getConfigurationSection(key));
			if (type != null) {
				reinforcementTypes.add(type);
			}
		}
	}

	@Nullable
	private AcidType parseAcidType(ConfigurationSection config) {
		String materialName = config.getString("material");
		if (materialName == null) {
			logger.info("Ignoring invalid acid material");
			return null;
		}
		double multiplier = config.getDouble("maturation_time_multiplier");

		Material material = Material.getMaterial(materialName);

		List<BlockFace> blockFaces = config.getStringList("faces").stream().map(BlockFace::valueOf).toList();

		return new AcidType(material, multiplier, blockFaces);
	}

	private void parseAcidTypes(ConfigurationSection config) {
		acidTypes = new ArrayList<>();

		if (config == null) {
			logger.info("No acid types found in config");
			return;
		}

		for (String key: config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				logger.warning("Ignoring invalid entry " + key + " at " + config.getCurrentPath());
				continue;
			}
			AcidType type = parseAcidType(config.getConfigurationSection(key));
			if (type != null) {
				acidTypes.add(type);
			}
		}
	}

	private void parseWorldBorderBuffers(ConfigurationSection config) {
		buffers = new HashMap<>();
		if (config == null) {
			logger.info("No Buffers zones found in config");
			return;
		}
		for (String key : config.getKeys(false)) {
			World world = Bukkit.getWorld(key);
			if (world == null) {
				logger.warning("WorldBuffer at " + config.getCurrentPath() + " couldn't find a world with this name: " + key);
				continue;
			}
			ConfigurationSection insideWorld = config.getConfigurationSection(key);
			if (insideWorld == null) {
				logger.warning("Couldn't loop inside a world buffer config section");
				continue;
			}
			WorldBorderBuffers.Shape worldBorderShape;
			try {
				worldBorderShape = WorldBorderBuffers.Shape.valueOf(insideWorld.getString("shape", "square").toUpperCase());
			} catch (IllegalArgumentException exception) {
				logger.warning("Shape at " + insideWorld.getCurrentPath() + " was not a valid input");
				continue;
			}
			double worldBorderBufferSize = insideWorld.getDouble("starting_radius", 100D);
			ConfigurationSection insideCenter = insideWorld.getConfigurationSection("center");
			if (insideCenter == null) {
				logger.info("No center for world border buffer found at " + insideWorld.getCurrentPath());
				continue;
			}
			double centerX = insideCenter.getDouble("x", 0.0);
			double centerZ = insideCenter.getDouble("z", 0.0);
			logger.info("Parsed World Border Buffer zone for world " + world.getName() + " with radius " + worldBorderBufferSize + " in shape " + worldBorderShape + " centered at  " + centerX + ", " + centerZ);
			buffers.put(world.getUID(), new WorldBorderBuffers(centerX, centerZ, worldBorderShape, worldBorderBufferSize));
		}

	}
}
