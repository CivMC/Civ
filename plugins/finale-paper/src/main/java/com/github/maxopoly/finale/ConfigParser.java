package com.github.maxopoly.finale;

import static vg.civcraft.mc.civmodcore.config.ConfigHelper.parseTime;


import com.github.maxopoly.finale.combat.CombatConfig;
import com.github.maxopoly.finale.combat.CombatSoundConfig;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.DamageModificationConfig;
import com.github.maxopoly.finale.misc.MultiplierMode;
import com.github.maxopoly.finale.misc.SaturationHealthRegenHandler;
import com.github.maxopoly.finale.misc.WeaponModifier;
import com.github.maxopoly.finale.misc.velocity.VelocityConfig;
import com.github.maxopoly.finale.misc.velocity.VelocityHandler;
import com.github.maxopoly.finale.potion.PotionHandler;
import com.github.maxopoly.finale.potion.PotionModification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

public class ConfigParser {
	private Finale plugin;
	private FinaleManager manager;
	private boolean pearlEnabled;
	private long pearlCooldown;
	private boolean combatTagOnPearl;
	private PotionHandler potionHandler;
	private Collection<Enchantment> disabledEnchants;
	private VelocityHandler velocityHandler;
	private List<DamageModificationConfig> damageModifiers;
	private CombatConfig combatConfig;

	public ConfigParser(Finale plugin) {
		this.plugin = plugin;
	}

	public boolean combatTagOnPearl() {
		return combatTagOnPearl;
	}

	public Collection<DamageModificationConfig> getDamageModifiers() {
		return damageModifiers;
	}

	public Collection<Enchantment> getDisabledEnchants() {
		return disabledEnchants;
	}

	public long getPearlCoolDown() {
		return pearlCooldown;
	}

	public PotionHandler getPotionHandler() {
		return potionHandler;
	}

	public VelocityHandler getVelocityHandler() {
		return velocityHandler;
	}

	public boolean isPearlEnabled() {
		return pearlEnabled;
	}
	
	public CombatConfig getCombatConfig() {
		return combatConfig;
	}

	public FinaleManager parse() {
		plugin.info("Parsing Finale config...");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		// Attack Speed modification for all players
		boolean debug = config.getBoolean("debug", false);
		plugin.info("Debug: " + debug);
		boolean attackEnabled = config.getBoolean("alterAttack.enabled", true);
		plugin.info("Attack speed modification enabled: " + attackEnabled);
		double attackSpeed = config.getDouble("alterAttack.speed", 9.4);
		plugin.info("Modified attack speed: " + attackSpeed);
		//CombatTag players on login
		boolean ctpOnLogin = config.getBoolean("ctpOnLogin");
		plugin.info("CombatTag on login is set to: " + ctpOnLogin);
		// Food Health Regen modifications for all players
		boolean regenEnabled = config.getBoolean("foodHealthRegen.enabled", false);
		SaturationHealthRegenHandler regenhandler = regenEnabled
				? parseHealthRegen(config.getConfigurationSection("foodHealthRegen"))
				: null;
		if (regenhandler == null) {
			plugin.info("Food regen modification is disabled");
		}
		// Pearl cooldown changes
		this.pearlEnabled = parsePearls(config.getConfigurationSection("pearls"));
		plugin.info("Ender pearl additions: " + pearlEnabled);
		WeaponModifier weapMod = parseWeaponModification(config.getConfigurationSection("weaponModification"));
		ArmourModifier armourMod = parseArmourModification(config.getConfigurationSection("armourModification"));
		boolean invulTicksEnabled = config.getBoolean("invulTicksEnabled", false);
		Map<EntityDamageEvent.DamageCause, Integer> invulnerableTicks = parseInvulnerabilityTicks(config.getConfigurationSection("invulnerableTicks"));

		disabledEnchants = parseDisableEnchantments(config);
		potionHandler = parsePotionChanges(config.getConfigurationSection("potions"));
		velocityHandler = parseVelocityModification(config.getConfigurationSection("velocity"));
		damageModifiers = parseDamageModifiers(config.getConfigurationSection("damageModifiers"));
		combatConfig = parseCombatConfig(config.getConfigurationSection("cleanerCombat"));

		// Initialize the manager
		manager = new FinaleManager(debug, attackEnabled, attackSpeed,invulTicksEnabled, invulnerableTicks, regenEnabled, ctpOnLogin, regenhandler, weapMod, armourMod,
				potionHandler, combatConfig);
		plugin.info("Successfully parsed config");
		return manager;
	}

	private List<DamageModificationConfig> parseDamageModifiers(ConfigurationSection config) {
		List<DamageModificationConfig> modifierList = new LinkedList<>();
		if (config == null) {
			return modifierList;
		}
		for (String key : config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				plugin.warning("Ignoring invalid entry " + key + " at " + config.getCurrentPath());
				continue;
			}
			ConfigurationSection current = config.getConfigurationSection(key);
			DamageModificationConfig.Type type;
			try {
				type = DamageModificationConfig.Type.valueOf(key.toUpperCase());
			} catch (IllegalArgumentException e) {
				plugin.warning("Failed to parse damage modification type " + key + " at " + current.getCurrentPath()
						+ ". It was skipped");
				continue;
			}
			MultiplierMode mode;
			String modeString = current.getString("mode", "LINEAR");
			try {
				mode = MultiplierMode.valueOf(modeString.toUpperCase());
			} catch (IllegalArgumentException e) {
				plugin.warning("Failed to parse damage modification mode " + modeString + " at "
						+ current.getCurrentPath() + ". It was skipped");
				continue;
			}
			double multiplier = current.getDouble("multiplier", 1.0);
			double flatAddition = current.getDouble("flatAddition", 0.0);
			DamageModificationConfig dmgConfig = new DamageModificationConfig(type, mode, multiplier, flatAddition);
			modifierList.add(dmgConfig);
			plugin.info("Applying damage modification for " + type.toString() + ", multiplier: " + multiplier
					+ ", multiplierMode: " + mode.toString() + ", flatAddition: " + flatAddition);
		}
		return modifierList;
	}

	private Collection<Enchantment> parseDisableEnchantments(ConfigurationSection config) {
		List<Enchantment> enchants = new LinkedList<>();
		if (!config.isList("disabledEnchantments")) {
			return enchants;
		}
		for (String ench : config.getStringList("disabledEnchantments")) {
			Enchantment en = Enchantment.getByName(ench);
			if (en == null) {
				plugin.warning("Could not parse disabled enchantment " + ench);
			} else {
				plugin.info("Disabling usage of enchant " + en.getName());
				enchants.add(en);
			}
		}
		return enchants;
	}

	private SaturationHealthRegenHandler parseHealthRegen(ConfigurationSection config) {
		// default values are vanilla 1.8 behavior
		int interval = (int) parseTime(config.getString("interval", "4s")) / 50;
		float exhaustionPerHeal = (float) config.getDouble("exhaustionPerHeal", 3.0);
		int minimumFood = config.getInt("minimumFood", 18);
		double healthPerCycle = config.getDouble("healthPerCycle", 1.0);
		boolean blockFoodRegen = config.getBoolean("blockFoodRegen", true);
		boolean blockSaturationRegen = config.getBoolean("blockSaturationRegen", true);
		return new SaturationHealthRegenHandler(interval, healthPerCycle, minimumFood, exhaustionPerHeal,
				blockSaturationRegen, blockFoodRegen);
	}

	private boolean parsePearls(ConfigurationSection config) {
		if (config == null || !config.getBoolean("enabled", false)) {
			return false;
		}
		String cooldown = config.getString("cooldown", "10s");
		pearlCooldown = parseTime(cooldown);
		plugin.info("Pearl cooldown set to " + pearlCooldown / 20 + " seconds");
		combatTagOnPearl = config.getBoolean("combatTag", true)
				&& Bukkit.getPluginManager().isPluginEnabled("CombatTagPlus");
		plugin.info("Combat tagging on pearling: " + combatTagOnPearl);
		return true;
	}

	public PotionHandler parsePotionChanges(ConfigurationSection config) {
		if (config == null) {
			plugin.info("No potion modifications found");
			return new PotionHandler(new HashMap<>(), 1.0, 0.3, 0.5);
		}
		ConfigurationSection potIntensitySection = config.getConfigurationSection("potIntensity");
		Map<PotionType, List<PotionModification>> potionMods = new EnumMap<>(PotionType.class);
		if (potIntensitySection != null) {
			for (String key : potIntensitySection.getKeys(false)) {
				ConfigurationSection current = potIntensitySection.getConfigurationSection(key);
				if (current == null) {
					plugin.getLogger().info(
							"Ignoring invalid config entry " + key + " at " + potIntensitySection.getCurrentPath());
					continue;
				}
				PotionType type = null;
				try {
					type = PotionType.valueOf(
							current.getString("type", PotionModification.wildCardType.toString()).toUpperCase());
				} catch (IllegalArgumentException e) {
					plugin.getLogger().info("Could not parse potion type at  " + current.getCurrentPath());
					continue;
				}
				Boolean extended = null;
				if (current.isBoolean("extended")) {
					extended = current.getBoolean("extended");
				}
				Boolean upgraded = null;
				if (current.isBoolean("upgraded")) {
					upgraded = current.getBoolean("upgraded");
				}
				Boolean splash = null;
				if (current.isBoolean("splash")) {
					splash = current.getBoolean("splash");
				}
				double multiplier = current.getDouble("multiplier", 1.0);
				if (multiplier < 0) {
					plugin.getLogger().info("Negative multipliers are not allowed for potion duration. Entry at "
							+ current.getCurrentPath() + " was ignored");
					continue;
				}
				PotionModification mod = new PotionModification(type, extended, upgraded, multiplier, splash);
				List<PotionModification> list = potionMods.get(type);
				if (list == null) {
					list = new ArrayList<>();
					potionMods.put(type, list);
				}
				plugin.info(String.format(
						"Parsed potion modification %s: type: %s, extended: %s, upgraded: %s, splash: %s, multiplier: %f",
						key, type, extended, upgraded, splash, multiplier));
				list.add(mod);
			}
		}
		double healthPotionMultiplier = config.getDouble("healthMultiplier", 1.0);
		double minIntensityCutOff = config.getDouble("minIntensityCutOff", 1.0);
		double minIntensityImpact = config.getDouble("minIntensityImpact", 1.0);
		return new PotionHandler(potionMods, healthPotionMultiplier, minIntensityCutOff, minIntensityImpact);
	}

	private VelocityHandler parseVelocityModification(ConfigurationSection config) {
		if (config == null) {
			return new VelocityHandler(new HashMap<>());
		}
		Map<EntityType, VelocityConfig> velocityConfigs = new EnumMap<>(EntityType.class);
		for (String key : config.getKeys(false)) {
			if (!config.isConfigurationSection(key)) {
				plugin.warning("Ignoring invalid entry " + key + " at " + config.getCurrentPath());
				continue;
			}
			ConfigurationSection current = config.getConfigurationSection(key);
			EntityType entityType;
			try {
				entityType = EntityType.valueOf(key.toUpperCase());
			} catch (IllegalArgumentException e) {
				plugin.warning("Failed to parse entity type " + key + " at " + current.getCurrentPath()
						+ ". It was skipped");
				continue;
			}
			String strType = current.getString("type");
			VelocityConfig.Type type;
			try {
				type = VelocityConfig.Type.valueOf(strType.toUpperCase());
			} catch (IllegalArgumentException e) {
				plugin.warning("Failed to parse velocity type " + key + " at " + current.getCurrentPath()
						+ ". It was skipped");
				continue;
			}
			double power = current.getDouble("power", 1.0);
			double horizontal = current.getDouble("horizontal", 1.0);
			double vertical = current.getDouble("vertical", 1.0);
			double pitchOffset = current.getDouble("pitchOffset", 0.0);
			VelocityConfig velocityConfig = VelocityConfig.createVelocityConfig(type, horizontal, vertical, power, pitchOffset);
			velocityConfigs.put(entityType, velocityConfig);
			plugin.info("Applying velocity config for " + entityType.toString() + ", type: " + type.toString()
					+ ", horizontal: " + horizontal + ", vertical: " + vertical + ", power: " + power
					+ ", pitchOffset: " + pitchOffset);
		}
		return new VelocityHandler(velocityConfigs);
	}

	private WeaponModifier parseWeaponModification(ConfigurationSection config) {
		WeaponModifier wm = new WeaponModifier();
		if (config == null) {
			return wm;
		}
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				plugin.warning("Found invalid value " + key + " at " + config + " only mapping values allowed here");
				continue;
			}
			String matString = current.getString("material");
			if (matString == null) {
				plugin.warning("Found no material specified at " + current + ". Skipping attack damage adjustment");
				continue;
			}
			Material mat;
			try {

				mat = Material.valueOf(matString);
			} catch (IllegalArgumentException e) {
				plugin.warning("Found invalid material " + matString + " specified at " + current
						+ ". Skipping attack damage adjustment for it");
				continue;
			}
			int damage = current.getInt("damage", -1);
			double attackSpeed = current.getDouble("attackSpeed", -1.0);
			plugin.info("Modifying " + matString + ": attackSpeed: " + attackSpeed + ", damage: " + damage);
			wm.addWeapon(mat, damage, attackSpeed);
		}
		return wm;
	}
	
	private ArmourModifier parseArmourModification(ConfigurationSection config) {
		ArmourModifier am = new ArmourModifier();
		if (config == null) {
			return am;
		}
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				plugin.warning("Found invalid value " + key + " at " + config + " only mapping values allowed here");
				continue;
			}
			String matString = current.getString("material");
			if (matString == null) {
				plugin.warning("Found no material specified at " + current + ". Skipping attack damage adjustment");
				continue;
			}
			Material mat;
			try {
				mat = Material.valueOf(matString);
			} catch (IllegalArgumentException e) {
				plugin.warning("Found invalid material " + matString + " specified at " + current
						+ ". Skipping attack damage adjustment for it");
				continue;
			}
			double toughness = current.getDouble("toughness", -1);
			double armour = current.getDouble("armour", -1);
			double knockbackResistance = current.getDouble("knockbackResistance", -1);
			int extraDurabilityHits = current.getInt("extraDurabilityHits", 0);
			plugin.info("Modifying " + matString + ": toughness: " + toughness + ", armour: " + armour
					+ ", knockbackResistance: " + knockbackResistance, ", extraDurabilityHits: " + extraDurabilityHits);
			am.addArmour(mat, toughness, armour, knockbackResistance, extraDurabilityHits);
		}
		return am;
	}
	
	private CombatConfig parseCombatConfig(ConfigurationSection config) {
		double maxReach = config.getDouble("maxReach", 6.0);
		Vector knockbackMultiplier = parseVector(config, "knockbackMultiplier", new Vector(1, 1, 1));
		Vector sprintMultiplier = parseVector(config, "sprintMultiplier", new Vector(1, 1, 1));
		Vector waterKnockbackMultiplier = parseVector(config, "waterKnockbackMultiplier", new Vector(1, 1, 1));
		Vector airKnockbackMultiplier = parseVector(config, "airKnockbackMultiplier", new Vector(1, 1, 1));
		Vector victimMotion = parseVector(config, "victimMotion", new Vector(0.5, 0.5, 0.5));
		Vector maxVictimMotion = parseVector(config, "maxVictimMotion", new Vector(10, 1, 10));
		Vector attackerMotion = parseVector(config, "attackerMotion", new Vector(0.6, 1, 0.6));
		plugin.info("Setting knockbackMultiplier to " + knockbackMultiplier);
		plugin.info("Setting sprintMultiplier to " + sprintMultiplier);
		plugin.info("Setting waterKnockbackMultiplier to " + waterKnockbackMultiplier);
		plugin.info("Setting airKnockbackMultiplier to " + airKnockbackMultiplier);
		plugin.info("Setting victimMotion to " + victimMotion);
		plugin.info("Setting maxVictimMotion to " + maxVictimMotion);
		plugin.info("Setting attackerMotion to " + attackerMotion);

		boolean weakSoundEnabled = false;
		boolean strongSoundEnabled = false;
		boolean knockbackSoundEnabled = false;
		boolean critSoundEnabled = true;
		if (config.isConfigurationSection("sounds")) {
			ConfigurationSection soundsSection = config.getConfigurationSection("sounds");
			weakSoundEnabled = soundsSection.getBoolean("weak", weakSoundEnabled);
			strongSoundEnabled = soundsSection.getBoolean("strong", strongSoundEnabled);
			knockbackSoundEnabled = soundsSection.getBoolean("knockback", knockbackSoundEnabled);
			critSoundEnabled = soundsSection.getBoolean("crit", critSoundEnabled);
		}
		plugin.info("Weak sounds are " + (weakSoundEnabled ? "ON" : "OFF"));
		plugin.info("Strong sounds are " + (strongSoundEnabled ? "ON" : "OFF"));
		plugin.info("Knockback sounds are " + (knockbackSoundEnabled ? "ON" : "OFF"));
		plugin.info("Crit sounds are " + (critSoundEnabled ? "ON" : "OFF"));
		
		CombatSoundConfig combatSounds = new CombatSoundConfig(weakSoundEnabled, strongSoundEnabled, knockbackSoundEnabled, critSoundEnabled);
		boolean attackCooldownEnabled = config.getBoolean("attackCooldownEnabled", false);
		boolean knockbackSwordsEnabled = config.getBoolean("knockbackSwordsEnabled", true);
		boolean sprintResetEnabled = config.getBoolean("sprintResetEnabled", true);
		boolean waterSprintResetEnabled = config.getBoolean("waterSprintResetEnabled", false);
		boolean sweepEnabled = config.getBoolean("sweepEnabled", false);
		double knockbackLevelMultiplier = config.getDouble("knockbackLevelMultiplier", 0.6);
		int cpsLimit = 9;
		long cpsCounterInterval = 1000;
		if (config.isConfigurationSection("cps")) {
			ConfigurationSection cpsSection = config.getConfigurationSection("cps");
			cpsLimit = cpsSection.getInt("limit", cpsLimit);
			cpsCounterInterval = cpsSection.getLong("counterInterval", cpsCounterInterval);
		}

		return new CombatConfig(attackCooldownEnabled, knockbackSwordsEnabled, sprintResetEnabled, waterSprintResetEnabled, cpsLimit, cpsCounterInterval, maxReach, sweepEnabled, combatSounds,
				knockbackLevelMultiplier, knockbackMultiplier, sprintMultiplier, waterKnockbackMultiplier, airKnockbackMultiplier, victimMotion, maxVictimMotion, attackerMotion);
	}

	private Vector parseVector(ConfigurationSection config, String name, Vector def) {
		double x = config.getDouble(name + ".x", def.getX());
		double y = config.getDouble(name + ".y", def.getY());
		double z = config.getDouble(name + ".z", def.getZ());
		return new Vector(x, y, z);
	}
	
	private Map<EntityDamageEvent.DamageCause, Integer> parseInvulnerabilityTicks(ConfigurationSection config) {
		Map<EntityDamageEvent.DamageCause, Integer> invulnTicks = new HashMap<>();
		for (String key : config.getKeys(false)) {
			EntityDamageEvent.DamageCause cause;
			try {
				cause = EntityDamageEvent.DamageCause.valueOf(key.toUpperCase());
			} catch (IllegalArgumentException e) {
				plugin.warning("Failed to parse tick invulnerability modification " + key + " at " + config.getCurrentPath()
						+ ". It was skipped");
				continue;
			}
			
			int ticks = config.getInt(key);
			invulnTicks.put(cause, ticks);
			plugin.info("Applying tick invulnerability modification for " + cause.toString() + ", ticks: " + ticks);
		}
		return invulnTicks;
	}
}


