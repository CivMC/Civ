package vg.civcraft.mc.citadel;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementEffect;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.CoreConfigManager;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.util.ConfigParsing;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class CitadelConfigManager extends CoreConfigManager {

	private ManagedDatasource database;
	private List<ReinforcementType> reinforcementTypes;
	private List<Material> acidMaterials;

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
	private long activityDefault;
	private List<String> activityWorlds;

	public CitadelConfigManager(ACivMod plugin) {
		super(plugin);
	}

	public List<Material> getAcidMaterials() {
		return acidMaterials;
	}

	public int getActivityMapRadius() {
		return activityMapRadius;
	}

	public int getActivityMapResolution() {
		return activityMapResolution;
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

	private void parseAcidMaterials(ConfigurationSection config) {
		acidMaterials = parseMaterialList(config, "acidblock_material");
		if (acidMaterials == null) {
			logger.info("No valid acid materials found in config");
			acidMaterials = new LinkedList<>();
		}
		for (Material mat : acidMaterials) {
			logger.info("Adding " + mat.toString() + " as valid acid material");
		}
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		database = (ManagedDatasource) config.get("database");
		globalBlackList = parseMaterialList(config, "non_reinforceables");
		parseAcidMaterials(config);
		logHostileBreaks = config.getBoolean("logHostileBreaks", true);
		logFriendlyBreaks = config.getBoolean("logFriendlyBreaks", true);
		logDamage = config.getBoolean("logDamage", false);
		logCreation = config.getBoolean("logCreation", true);
		logMessages = config.getBoolean("logMessages", true);
		redstoneRange = config.getDouble("redstoneDistance", 3);
		globalDecayMultiplier = config.getDouble("global_decay_multiplier", 2.0);
		globalDecayTimer = ConfigParsing.parseTime(config.getString("global_decay_timer", "0"));
		parseReinforcementTypes(config.getConfigurationSection("reinforcements"));
		hangersInheritReinforcements = config.getBoolean("hangers_inherit_reinforcement", false);

		activityMapRadius = config.getInt("activity-map-radius", 1);
		activityMapResolution = config.getInt("activity-map-resolution", 512);
		activityDefault = config.getLong("activity-default", System.currentTimeMillis());
		activityWorlds = config.getStringList("activity-map-worlds");

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
		long gracePeriod = ConfigParsing.parseTime(config.getString("grace_period", "0"), TimeUnit.MILLISECONDS);
		long maturationTime = ConfigParsing.parseTime(config.getString("mature_time", "0"), TimeUnit.MILLISECONDS);
		long acidTime = ConfigParsing.parseTime(config.getString("acid_time", "-1"), TimeUnit.MILLISECONDS);
		int acidPriority = config.getInt("acid_priority", 0);
		String name = config.getString("name");
		double maturationScale = config.getInt("scale_amount", 1);
		float health = (float) config.getDouble("hit_points", 100);
		double returnChance = config.getDouble("return_chance", 1.0);
		List<Material> reinforceables = parseMaterialList(config, "reinforceables");
		List<Material> nonReinforceables = parseMaterialList(config, "non_reinforceables");
		short id = (short) config.getInt("id", -1);
		long decayTimer = ConfigParsing
				.parseTime(config.getString("decay_timer", String.valueOf(globalDecayTimer / 1000L) + "s"));
		double decayMultiplier = config.getDouble("decay_multiplier", globalDecayMultiplier);
		double multiplerOnDeletedGroup = config.getDouble("deleted_group_multipler", 4);
		int legacyId = config.getInt("legacy_id", -1);
		if (name == null) {
			logger.warning("No name specified for reinforcement type at " + config.getCurrentPath());
			name = item.getType().name();
		}
		if (id == -1) {
			logger.warning("Reinforcement type at " + config.getCurrentPath() + " had no id, it was ignored");
			return null;
		}
		if (reinforceables != null && nonReinforceables != null) {
			logger.warning("Both blacklist and whitelist specified for reinforcement type at " + config.getCurrentPath()
					+ ". This does not make sense and the type will be ignored");
			return null;
		}
		logger.info("Parsed reinforcement type " + name + " for item " + item.toString() + ", returnChance: "
				+ returnChance + ", maturationTime: " + TextUtil.formatDuration(maturationTime, TimeUnit.MILLISECONDS)
				+ ", acidTime: " + TextUtil.formatDuration(acidTime, TimeUnit.MILLISECONDS) + ", gracePeriod: "
				+ gracePeriod + ", id: " + id);
		return new ReinforcementType(health, returnChance, item, maturationTime, acidTime, acidPriority, maturationScale, gracePeriod,
				creationEffect, damageEffect, destructionEffect, reinforceables, nonReinforceables, id, name,
				globalBlackList, decayTimer, decayMultiplier, multiplerOnDeletedGroup, legacyId);
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

}
