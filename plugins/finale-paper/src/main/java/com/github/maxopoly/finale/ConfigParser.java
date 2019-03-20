package com.github.maxopoly.finale;

import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parseTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionType;

import com.github.maxopoly.finale.misc.DamageModificationConfig;
import com.github.maxopoly.finale.misc.MultiplierMode;
import com.github.maxopoly.finale.misc.SaturationHealthRegenHandler;
import com.github.maxopoly.finale.misc.VelocityHandler;
import com.github.maxopoly.finale.misc.WeaponModifier;
import com.github.maxopoly.finale.potion.PotionHandler;
import com.github.maxopoly.finale.potion.PotionModification;

public class ConfigParser {
	private Finale plugin;
	private FinaleManager manager;
	private boolean pearlEnabled;
	private long pearlCooldown;
	private boolean combatTagOnPearl;
	private boolean setVanillaPearlCooldown;
	private boolean sideBarPearlCooldown;
	private PotionHandler potionHandler;
	private Collection<Enchantment> disabledEnchants;
	private VelocityHandler velocityHandler;
	private List<DamageModificationConfig> damageModifiers;

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

	public boolean setVanillaPearlCooldown() {
		return setVanillaPearlCooldown;
	}

	public boolean useSideBarForPearlCoolDown() {
		return sideBarPearlCooldown;
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
		int invulnerableTicks = config.getInt("alterAttack.invulnerableTicks", 10);
		plugin.info("Modified invulnerable ticks: " + invulnerableTicks);
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

		disabledEnchants = parseDisableEnchantments(config);
		potionHandler = parsePotionChanges(config.getConfigurationSection("potions"));
		velocityHandler = parseVelocityModification(config.getConfigurationSection("velocity"));
		damageModifiers = parseDamageModifiers(config.getConfigurationSection("damageModifiers"));

		// Initialize the manager
		manager = new FinaleManager(debug, attackEnabled, attackSpeed, invulnerableTicks, regenEnabled, regenhandler,
				weapMod, potionHandler);
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
		List<Enchantment> enchants = new LinkedList<Enchantment>();
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
		int interval = (int) parseTime(config.getString("interval", "4s"));
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
		pearlCooldown = parseTime(config.getString("cooldown", "10s"));
		plugin.info("Pearl cooldown set to " + pearlCooldown / 20 + " seconds");
		combatTagOnPearl = config.getBoolean("combatTag", true)
				&& Bukkit.getPluginManager().isPluginEnabled("CombatTagPlus");
		plugin.info("Combat tagging on pearling: " + combatTagOnPearl);
		setVanillaPearlCooldown = config.getBoolean("setVanillaCooldown", false);
		plugin.info("Setting vanilla cooldown on pearling: " + setVanillaPearlCooldown);
		sideBarPearlCooldown = config.getBoolean("useSideBar", true);
		plugin.info("Using sidebar to display pearl cooldown:" + sideBarPearlCooldown);
		return true;
	}

	public PotionHandler parsePotionChanges(ConfigurationSection config) {
		if (config == null) {
			plugin.info("No potion modifications found");
			return new PotionHandler(new HashMap<>(), 1.0);
		}
		ConfigurationSection potIntensitySection = config.getConfigurationSection("potIntensity");
		Map<PotionType, List<PotionModification>> potionMods = new HashMap<>();
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
		return new PotionHandler(potionMods, healthPotionMultiplier);
	}

	private VelocityHandler parseVelocityModification(ConfigurationSection config) {
		if (config == null) {
			return new VelocityHandler(new LinkedList<>(), new HashMap<>());
		}
		List<EntityType> revertedTypes = new LinkedList<>();
		if (config.isList("revertedVelocity")) {
			for (String entry : config.getStringList("revertedVelocity")) {
				try {
					EntityType type = EntityType.valueOf(entry);
					revertedTypes.add(type);
					plugin.info("Reverting launch velocity behavior of " + type.toString());
				} catch (IllegalArgumentException e) {
					plugin.warning("Failed to parse " + entry + " as entity type at " + config.getCurrentPath());
				}
			}
		}
		Map<EntityType, Double> velocityMultiplier = new TreeMap<>();
		if (config.isConfigurationSection("multiplier")) {
			ConfigurationSection multSection = config.getConfigurationSection("multiplier");
			for (String key : multSection.getKeys(false)) {
				if (multSection.isDouble(key)) {
					try {
						EntityType type = EntityType.valueOf(key);
						double multiplier = multSection.getDouble(key);
						velocityMultiplier.put(type, multiplier);
						plugin.info("Applying launch velocity multiplier of " + multiplier + " to " + type.toString());

					} catch (IllegalArgumentException e) {
						plugin.warning("Failed to parse " + key + " as entity type at " + multSection.getCurrentPath());
					}
				} else {
					plugin.warning("Ignoring invalid entry " + key + " at " + multSection.getCurrentPath());
				}
			}
		}
		return new VelocityHandler(revertedTypes, velocityMultiplier);
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
}
