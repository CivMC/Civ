package com.devotedmc.ExilePearl;

import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;

public class ExilePearlConfig {

	private final ExilePearlPlugin plugin;
	
	public ExilePearlConfig(ExilePearlPlugin plugin) {
		this.plugin = plugin;
	}
	
	@CivConfig(name = "database.mysql.username", def = "bukkit", type = CivConfigType.String)
	public String getDbUsername() {
		return plugin.GetConfig().get("database.mysql.username").getString();
	}
	
	@CivConfig(name = "database.mysql.password", def = "", type = CivConfigType.String)
	public String getDbPassword() {
		return plugin.GetConfig().get("database.mysql.password").getString();
	}
	
	@CivConfig(name = "database.mysql.dbname", def = "bukkit", type = CivConfigType.String)
	public String getDbName() {
		return plugin.GetConfig().get("database.mysql.dbname").getString();
	}
	
	@CivConfig(name = "database.mysql.host", def = "localhost", type = CivConfigType.String)
	public String getDbHost() {
		return plugin.GetConfig().get("database.mysql.host").getString();
	}
	
	@CivConfig(name = "database.mysql.port", def = "3306", type = CivConfigType.Int)
	public int getDbPort() {
		return plugin.GetConfig().get("database.mysql.port").getInt();
	}
	
	@CivConfig(name = "health.decay_interval_min", def = "60" , type = CivConfigType.Int)
	public int getPearlHealthDecayIntervalMin() {
		return plugin.GetConfig().get("health.decay_interval_min").getInt();
	}
	
	@CivConfig(name = "health.amount", def = "1" , type = CivConfigType.Int)
	public int getPearlHealthDecayAmount() {
		return plugin.GetConfig().get("health.amount").getInt();
	}
	
	@CivConfig(name = "health.resource", def = "263", type = CivConfigType.Int)
	public int getPearlHealthMaterial() {
		return plugin.GetConfig().get("health.resource").getInt();
	}
	
	@CivConfig(name = "health.start_value", def = "4" , type = CivConfigType.Int)
	public int getPearlHealthStartValue() {
		return plugin.GetConfig().get("health.start_value").getInt();
	}
	
	@CivConfig(name = "health.max_value", def = "336" , type = CivConfigType.Int)
	public int getPearlHealthMaxValue() {
		return plugin.GetConfig().get("upkeep.max_value").getInt();
	}
	
	@CivConfig(name = "autofree_worldborder", def = "true", type = CivConfigType.Bool)
	public boolean getShouldAutoFreeWorldBorder() {
		return plugin.GetConfig().get("autofree_worldborder").getBool();
	}
	
	@CivConfig(name = "prison_musthotbar", def = "true", type = CivConfigType.Bool)
	public boolean getMustPrisonPearlHotBar() {
		return plugin.GetConfig().get("prison_musthotbar").getBool();
	}
	
	@CivConfig(name = "prison_stealing", def = "true", type = CivConfigType.Bool)
	public boolean getAllowPrisonStealing() {
		return plugin.GetConfig().get("prison_stealing").getBool();
	}
	
	@CivConfig(name = "damagelog_min", def = "3" , type = CivConfigType.Int)
	public int getDamageLogMin() {
		return plugin.GetConfig().get("damagelog_min").getInt();
	}
	
	@CivConfig(name = "damagelog_ticks", def = "600" , type = CivConfigType.Int)
	public int getDamagelogTicks() {
		return plugin.GetConfig().get("damagelog_ticks").getInt();
	}
	
	@CivConfig(name = "prison.unloadTimerTicks", def = "1200", type = CivConfigType.Int)
	public int getPrisonUnloadTimerTicks() {
		return plugin.GetConfig().get("prison.unloadTimerTicks").getInt();
	}
	
	
	// Exile Rules

	@CivConfig(name = "rules.pearl_radius", def = "1000", type = CivConfigType.Int)
	public int getRulePearlRadius() {
		return plugin.GetConfig().get("rules.pearl_radius").getInt();
	}
	
	public boolean setRulePearlRadius(Integer value) {
		return plugin.GetConfig().set("rules.pearl_radius", value.toString());
	}
	
	@CivConfig(name = "rules.damage_reinforcement", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanDamageReinforcement() {
		return plugin.GetConfig().get("rules.damage_reinforcement").getBool();
	}
	
	public boolean setRuleCanDamageReinforcement(Boolean value) {
		return plugin.GetConfig().set("rules.damage_reinforcement", value.toString());
	}
	
	@CivConfig(name = "rules.damage_bastion", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanDamageBastion() {
		return plugin.GetConfig().get("rules.damage_bastion").getBool();
	}

	@CivConfig(name = "rules.enter_bastion", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanEnterBastion() {
		return plugin.GetConfig().get("rules.enter_bastion").getBool();
	}
	
	@CivConfig(name = "rules.throw_pearl", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanThrowEnderPearl() {
		return plugin.GetConfig().get("rules.throw_pearl").getBool();
	}

	@CivConfig(name = "rules.chat_local", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanChatLocal() {
		return plugin.GetConfig().get("rules.chat_local").getBool();
	}

	@CivConfig(name = "rules.pvp", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanPvp() {
		return plugin.GetConfig().get("rules.pvp").getBool();
	}

	@CivConfig(name = "rules.ignite", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanIgnite() {
		return plugin.GetConfig().get("rules.ignite").getBool();
	}

	@CivConfig(name = "rules.use_bucket", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanUseBucket() {
		return plugin.GetConfig().get("rules.use_bucket").getBool();
	}

	@CivConfig(name = "rules.place_water", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanPlaceWater() {
		return plugin.GetConfig().get("rules.place_water").getBool();
	}

	@CivConfig(name = "rules.place_lava", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanPlaceLava() {
		return plugin.GetConfig().get("rules.place_lava").getBool();
	}

	@CivConfig(name = "rules.use_potions", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanUsePotions() {
		return plugin.GetConfig().get("rules.use_potions").getBool();
	}

	@CivConfig(name = "rules.use_bed", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanUseBed() {
		return plugin.GetConfig().get("rules.use_bed").getBool();
	}

	@CivConfig(name = "rules.suicide", def = "true", type = CivConfigType.Bool)
	public boolean getRuleCanSuicide() {
		return plugin.GetConfig().get("rules.suicide").getBool();
	}

	@CivConfig(name = "rules.place_snitch", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanPlaceSnitch() {
		return plugin.GetConfig().get("rules.place_snitch").getBool();
	}

	@CivConfig(name = "rules.mine_blocks", def = "true", type = CivConfigType.Bool)
	public boolean getRuleCanMine() {
		return plugin.GetConfig().get("rules.mine_blocks").getBool();
	}

	@CivConfig(name = "rules.brew", def = "true", type = CivConfigType.Bool)
	public boolean getRuleCanBrew() {
		return plugin.GetConfig().get("rules.brew").getBool();
	}

	@CivConfig(name = "rules.enchant", def = "true", type = CivConfigType.Bool)
	public boolean getRuleCanEnchant() {
		return plugin.GetConfig().get("rules.enchant").getBool();
	}
}
