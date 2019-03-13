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

public class GlobalReinforcementManager extends CoreConfigManager {

	private ManagedDatasource database;
	private List<ReinforcementType> reinforcementTypes;
	private List<Material> acidMaterials;

	public GlobalReinforcementManager(ACivMod plugin) {
		super(plugin);
	}
	
	public List<Material> getAcidMaterials() {
		return acidMaterials;
	}

	@Override
	protected boolean parseInternal(ConfigurationSection config) {
		database = (ManagedDatasource) config.get("database");
		parseReinforcementTypes(config.getConfigurationSection("reinforcements"));
		parseAcidMaterials(config);
		return true;
	}
	
	private void parseAcidMaterials(ConfigurationSection config) {
		acidMaterials = parseMaterialList(config, "acidblock_material");
		if (acidMaterials == null) {
			logger.info("No valid acid materials found in config");
			acidMaterials = new LinkedList<>();
		}
		for(Material mat : acidMaterials) {
			logger.info("Adding " + mat.toString() + " as valid acid material");
		}
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

	private ReinforcementType parseReinforcementType(ConfigurationSection config) {
		if (!config.isItemStack("item")) {
			logger.warning(
					"Reinforcement config at " + config.getCurrentPath() + " had no valid item entry, it was ignored");
			return null;
		}
		ItemStack item = config.getItemStack("item");
		ReinforcementEffect effect = getReinforcementEffect(config.getConfigurationSection("effect"));
		long gracePeriod = ConfigParsing.parseTime(config.getString("grace_period", "0"), TimeUnit.MILLISECONDS);
		long maturationTime = ConfigParsing.parseTime(config.getString("mature_time", "0"), TimeUnit.MILLISECONDS);
		long acidTime = ConfigParsing.parseTime(config.getString("acid_time", "-1"),
				TimeUnit.MILLISECONDS);
		String name = config.getString("name");
		double maturationScale = config.getInt("scale_amount", 1);
		double health = config.getDouble("hit_points", 100);
		double returnChance = config.getDouble("return_chance", 1.0);
		List<Material> reinforceables = parseMaterialList(config, "reinforceables");
		List<Material> nonReinforceables = parseMaterialList(config, "non_reinforceables");
		int id = config.getInt("id", -1);
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
		return new ReinforcementType(health, returnChance, item, maturationTime, acidTime, maturationScale, gracePeriod, effect,
				reinforceables, nonReinforceables, id, name);
	}

	private List<Material> parseMaterialList(ConfigurationSection config, String key) {
		return parseList(config, key, s -> {
			try {
				return Material.valueOf(s.toUpperCase());
			} catch (IllegalArgumentException e) {
				logger.warning("Failed to parse " + s + " as material at " + config.getCurrentPath());
				return null;
			}
		});
	}

	private ReinforcementEffect getReinforcementEffect(ConfigurationSection config) {
		if (config == null) {
			return null;
		}
		Particle effect;
		try {
			String effectName = config.getString("type");
			effect = effectName.equals("FLYING_GLYPH") ? Particle.ENCHANTMENT_TABLE : Particle.valueOf(effectName);
		} catch (IllegalArgumentException e) {
			logger.warning("Invalid effect at: " + config.getCurrentPath());
			return null;
		}
		float offsetX = (float) config.getDouble("offsetX", 0);
		float offsetY = (float) config.getDouble("offsetY", 0);
		float offsetZ = (float) config.getDouble("offsetZ", 0);
		float speed = (float) config.getDouble("speed", 1);
		int amount = config.getInt("particleCount", 1);
		return new ReinforcementEffect(effect, offsetX, offsetY, offsetZ, speed, amount);
	}

	public ManagedDatasource getDatabase() {
		return database;
	}

	public List<ReinforcementType> getReinforcementTypes() {
		return reinforcementTypes;
	}

}
