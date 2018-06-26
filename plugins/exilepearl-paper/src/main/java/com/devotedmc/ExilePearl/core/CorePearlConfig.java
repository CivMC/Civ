package com.devotedmc.ExilePearl.core;

import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.RepairMaterial;
import com.devotedmc.ExilePearl.config.Configurable;
import com.devotedmc.ExilePearl.config.Document;
import com.devotedmc.ExilePearl.config.DocumentConfig;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.storage.StorageType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.util.Guard;

final class CorePearlConfig implements DocumentConfig, PearlConfig {

	private final Plugin plugin;
	private final PearlLogger logger;
	private Document doc;
	private Set<Configurable> configurables = new HashSet<>();

	public CorePearlConfig(final Plugin plugin, final PearlLogger logger) {
		Guard.ArgumentNotNull(plugin, "plugin");
		Guard.ArgumentNotNull(logger, "logger");

		this.plugin = plugin;
		this.logger = logger;

		doc = new Document(plugin.getConfig());
	}

	@Override
	public Document getDocument() {
		return this.doc;
	}

	@Override
	public void reload() {
		plugin.reloadConfig();
		doc = new Document(plugin.getConfig());

		for(Configurable c : configurables) {
			try {
				c.loadConfig(this);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Failed to load configuration for class %s", c.getClass().getName());
				ex.printStackTrace();
			}
		}
		logger.log("Configuration loaded.");
	}

	@Override
	public void saveToFile() {
		doc.savetoConfig(plugin.getConfig());
		plugin.saveConfig();
	}

	@Override
	public void addConfigurable(Configurable configurable) {
		configurables.add(configurable);
	}

	@Override
	public StorageType getStorageType() {
		return StorageType.valueOf(doc.getInteger("storage.type", 0));
	}

	@Override
	public String getMySqlHost() {
		return doc.getString("storage.mysql.host", "localhost");
	}

	@Override
	public String getMySqlDatabaseName() {
		return doc.getString("storage.mysql.dbname", "bukkit");
	}

	@Override
	public String getMySqlUsername() {
		return doc.getString("storage.mysql.username", "bukkit");
	}

	@Override
	public String getMySqlPassword() {
		return doc.getString("storage.mysql.password", "");
	}

	@Override
	public int getMySqlPort() {
		return doc.getInteger("storage.mysql.port", 3306);
	}

	@Override
	public int getMySqlPoolSize() {
		return doc.getInteger("storage.mysql.pool_size", 5);
	}

	@Override
	public int getMySqlConnectionTimeout() {
		return doc.getInteger("storage.mysql.connection_timeout", 5000);
	}

	@Override
	public int getMySqlIdleTimeout() {
		return doc.getInteger("storage.mysql.idle_timeout", 5000);
	}

	@Override
	public int getMySqlMaxLifetime() {
		return doc.getInteger("storage.mysql.max_lifetime", 5000);
	}

	@Override
	public boolean getMigratePrisonPearl() {
		return doc.getBoolean("storage.mysql.migrate_pp", false);
	}

	@Override
	public String getMySqlMigrateDatabaseName() {
		return doc.getString("storage.mysql.migrate_dbname", "prisonpearl");
	}

	@Override
	public String getPearlHealthDecayHumanInterval() {
		return doc.getString("pearls.decay_interval_human", "day");
	}

	@Override
	public int getPearlHealthDecayHumanIntervalMin() {
		return doc.getInteger("pearls.decay_interval_min_human", 1440);
	}

	@Override
	public int getPearlHealthDecayIntervalMin() {
		return doc.getInteger("pearls.decay_interval_min", 60);
	}

	@Override
	public int getPearlHealthDecayAmount() {
		return doc.getInteger("pearls.decay_amount", 1);
	}

	@Override
	public int getPearlHealthDecayTimeout() {
		return doc.getInteger("pearls.decay_timeout_min", 10080);
	}

	@Override
	public int getPearlHealthStartValue() {
		return doc.getInteger("pearls.start_value", 12);
	}

	@Override
	public int getPearlHealthMaxValue() {
		return doc.getInteger("pearls.max_value", 336);
	}

	@Override
	public boolean getShouldAutoFreeWorldBorder() {
		return doc.getBoolean("pearls.autofree_worldborder", true);
	}

	@Override
	public boolean getMustPrisonPearlHotBar() {
		return doc.getBoolean("pearls.hotbar_needed", true);
	}

	@Override
	public boolean getFreeByThrowing() {
		return doc.getBoolean("pearls.free_by_throwing", false);
	}

	@Override
	public boolean getShouldFreeTeleport() {
		return doc.getBoolean("pearls.free_teleport", true);
	}

	@Override
	public boolean getShouldAnnounceExileLocation(){
		return doc.getBoolean("pearls.announce_exile_location", true);
	}

	@Override
	public double getBastionDamage() {
		return doc.getDouble("pearls.bastion_harm_amount", 0.5);
	}

	@Override
	public Set<RepairMaterial> getRepairMaterials(PearlType type) {
		Set<RepairMaterial> repairs = new HashSet<>();
		Document repairRecipes = doc.getDocument("pearls.repair_materials." + type);

		if(repairRecipes == null){
			return repairs;
		}

		for(String repairName : repairRecipes.keySet()) {
			repairs.add(RepairMaterial.fromDocument(repairName, repairRecipes.getDocument(repairName)));
		}

		return repairs;
	}

	@Override
	public Set<String> getDisallowedWorlds() {
		HashSet<String> worlds = new HashSet<>();
		for(String str : doc.getStringList("rules.disallowed_worlds"))
			worlds.add(str);
		return worlds;
	}

	@Override
	public List<String> getProtectedAnimals() {
		return doc.getStringList("rules.protected_mobs");
	}

	@Override
	public int getSuicideTimeoutSeconds() {
		return doc.getInteger("general.suicide_time_seconds", 180);
	}

	@Override
	public int getRulePearlRadius() {
		return doc.getInteger("rules.pearl_radius", 1000);
	}

	@Override
	public void setRulePearlRadius(int value) {
		doc.append("rules.pearl_radius", value);
	}

	@Override
	public boolean canPerform(ExileRule rule) {
		switch(rule) {
		case PEARL_RADIUS:
			return getRulePearlRadius() > 0;

		case DAMAGE_REINFORCEMENT:
			return doc.getBoolean("rules.damage_reinforcement", false);

		case DAMAGE_BASTION:
			return doc.getBoolean("rules.damage_bastion", false);

		case CREATE_BASTION:
			return doc.getBoolean("rules.create_bastion", false);

		case ENTER_BASTION:
			return doc.getBoolean("rules.enter_bastion", false);

		case THROW_PEARL:
			return doc.getBoolean("rules.throw_pearl", false);

		case CHAT:
			return doc.getBoolean("rules.chat_local", false);

		case PVP:
			return doc.getBoolean("rules.pvp", false);

		case KILL_PETS:
			return doc.getBoolean("rules.kill_pets", false);

		case KILL_MOBS:
			return doc.getBoolean("rules.kill_mobs", false);

		case IGNITE:
			return doc.getBoolean("rules.ignite", false);

		case USE_BUCKET:
			return doc.getBoolean("rules.use_bucket", false);

		case USE_POTIONS:
			return doc.getBoolean("rules.use_potions", false);

		case USE_BED:
			return doc.getBoolean("rules.use_bed", false);

		case SUICIDE:
			return doc.getBoolean("rules.suicide", false);

		case SNITCH:
			return doc.getBoolean("rules.place_snitch", false);

		case MINE:
			return doc.getBoolean("rules.mine_blocks", true);

		case BREW:
			return doc.getBoolean("rules.brew", true);

		case ENCHANT:
			return doc.getBoolean("rules.enchant", true);

		case COLLECT_XP:
			return doc.getBoolean("rules.collect_xp", true);

		case USE_ANVIL:
			return doc.getBoolean("rules.use_anvil", true);

		case PLACE_TNT:
			return doc.getBoolean("rules.place_tnt", false);

		case DRINK_BREWS:
			return doc.getBoolean("rules.drink_brews", true);

		case FILL_CAULDRON:
			return doc.getBoolean("rules.fill_cauldron", true);

		case FILL_BUCKET:
			return doc.getBoolean("rules.fill_bucket", false);

		case MILK_COWS:
			return doc.getBoolean("rules.milk_cows", true);

		default:
			return false;
		}
	}

	@Override
	public void setRule(ExileRule rule, boolean value) {
		switch(rule) {
		case DAMAGE_REINFORCEMENT:
			doc.append("rules.damage_reinforcement", value);
			break;

		case DAMAGE_BASTION:
			doc.append("rules.damage_bastion", value);
			break;

		case CREATE_BASTION:
			doc.append("rules.create_bastion", value);
			break;

		case ENTER_BASTION:
			doc.append("rules.enter_bastion", value);
			break;

		case THROW_PEARL:
			doc.append("rules.throw_pearl", value);
			break;

		case CHAT:
			doc.append("rules.chat_local", value);
			break;

		case PVP:
			doc.append("rules.pvp", value);
			break;

		case KILL_PETS:
			doc.append("rules.kill_pets", value);
			break;

		case KILL_MOBS:
			doc.append("rules.kill_mobs", value);
			break;

		case IGNITE:
			doc.append("rules.ignite", value);
			break;

		case USE_BUCKET:
			doc.append("rules.use_bucket", value);
			break;

		case USE_POTIONS:
			doc.append("rules.use_potions", value);
			break;

		case USE_BED:
			doc.append("rules.use_bed", value);
			break;

		case SUICIDE:
			doc.append("rules.suicide", value);
			break;

		case SNITCH:
			doc.append("rules.place_snitch", value);
			break;

		case MINE:
			doc.append("rules.mine_blocks", value);
			break;

		case BREW:
			doc.append("rules.brew", value);
			break;

		case ENCHANT:
			doc.append("rules.enchant", value);
			break;

		case COLLECT_XP:
			doc.append("rules.collect_xp", value);
			break;

		case USE_ANVIL:
			doc.append("rules.use_anvil", value);
			break;

		case PLACE_TNT:
			doc.append("rules.place_tnt", value);
			break;

		case DRINK_BREWS:
			doc.append("rules.drink_brews", value);
			break;

		case FILL_CAULDRON:
			doc.append("rules.fill_cauldron", value);
			break;

		case FILL_BUCKET:
			doc.append("rules.fill_bucket", value);
			break;

		case MILK_COWS:
			doc.append("rules.milk_cows", value);
			break;

		default:
			break;
		}
	}

	@Override
	public boolean getUseHelpItem() {
		return doc.getBoolean("help_item.enabled", true);
	}

	@Override
	public String getHelpItemName() {
		return doc.getString("help_item.item_name", "You've been exiled!");
	}

	@Override
	public List<String> getHelpItemText() {
		return doc.getStringList("help_item.item_text");
	}

	@Override
	public boolean getDamageLogEnabled() {
		return doc.getBoolean("damage_log.enabled", true);
	}

	@Override
	public int getDamageLogAlgorithm() {
		return doc.getInteger("damage_log.algorithm", 0);
	}

	@Override
	public int getDamageLogInterval() {
		return doc.getInteger("damage_log.tick_interval", 20);
	}

	@Override
	public double getDamageLogDecayAmount() {
		return doc.getDouble("damage_log.decay_amount", 1.0);
	}

	@Override
	public double getDamageLogMaxDamage() {
		return doc.getDouble("damage_log.max_amount", 30);
	}

	@Override
	public double getDamageLogPotionDamage() {
		return doc.getDouble("damage_log.potion_damge", 6);
	}

	@Override
	public World getPrisonWorld() {
		return Bukkit.getWorld(doc.getString("pearls.prison_world", "world_the_end"));
	}

	@Override
	public World getMainWorld() {
		return Bukkit.getWorld(doc.getString("pearls.main_world", "world"));
	}

	@Override
	public Set<RepairMaterial> getUpgradeMaterials() {
		Set<RepairMaterial> upgrades = new HashSet<>();
		Document upgradeRecipes = doc.getDocument("pearls.upgrade_materials");

		if(upgradeRecipes == null){
			return upgrades;
		}

		for(String upgradeName : upgradeRecipes.keySet()) {
			upgrades.add(RepairMaterial.fromDocument(upgradeName, upgradeRecipes.getDocument(upgradeName)));
		}

		return upgrades;
	}

	@Override
	public boolean allowPearlStealing() {
		return doc.getBoolean("pearls.allow_pearl_stealing", true);
	}

	@Override
	public boolean allowSummoning() {
		return doc.getBoolean("pearls.allow_summoning", true);
	}

    @Override
    public int maxAltsPearled() {
        return doc.getInteger("general.max_pearled_alts", 1);
    }

    @Override
    public String altBanMessage() {
        return doc.getString("general.max_pearled_message", "You have too many imprisoned alts");
    }
}
