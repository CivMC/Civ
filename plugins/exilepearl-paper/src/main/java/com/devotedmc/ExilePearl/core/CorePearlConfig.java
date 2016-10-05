package com.devotedmc.ExilePearl.core;

import com.devotedmc.ExilePearl.ExileRule;
import com.devotedmc.ExilePearl.PearlConfig;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;

class CorePearlConfig implements PearlConfig {

	private final ACivMod plugin;
	
	public CorePearlConfig(final ACivMod plugin) {
		this.plugin = plugin;
	}
	
	@Override
	@CivConfig(name = "database.mysql.host", def = "localhost", type = CivConfigType.String)
	public String getDbHost() {
		return plugin.GetConfig().get("database.mysql.host").getString();
	}

	@Override
	@CivConfig(name = "database.mysql.username", def = "bukkit", type = CivConfigType.String)
	public String getDbUsername() {
		return plugin.GetConfig().get("database.mysql.username").getString();
	}

	@Override
	@CivConfig(name = "database.mysql.password", def = "", type = CivConfigType.String)
	public String getDbPassword() {
		return plugin.GetConfig().get("database.mysql.password").getString();
	}

	@Override
	@CivConfig(name = "database.mysql.dbname", def = "bukkit", type = CivConfigType.String)
	public String getDbName() {
		return plugin.GetConfig().get("database.mysql.dbname").getString();
	}

	@Override
	@CivConfig(name = "database.mysql.port", def = "3306", type = CivConfigType.Int)
	public int getDbPort() {
		return plugin.GetConfig().get("database.mysql.port").getInt();
	}

	@Override
	@CivConfig(name = "health.decay_interval_min", def = "60" , type = CivConfigType.Int)
	public int getPearlHealthDecayIntervalMin() {
		return plugin.GetConfig().get("health.decay_interval_min").getInt();
	}

	@Override
	@CivConfig(name = "health.decay_amount", def = "1" , type = CivConfigType.Int)
	public int getPearlHealthDecayAmount() {
		return plugin.GetConfig().get("health.decay_amount").getInt();
	}
	
	@Override
	@CivConfig(name = "health.repair_material", def = "OBSIDIAN", type = CivConfigType.String)
	public String getPearlRepairMaterial() {
		return plugin.GetConfig().get("health.repair_material").getString();
	}
	
	@Override
	@CivConfig(name = "health.repair_amount", def = "3" , type = CivConfigType.Int)
	public int getPearlRepairAmount() {
		return plugin.GetConfig().get("health.repair_amount").getInt();
	}

	@Override
	@CivConfig(name = "health.start_value", def = "4" , type = CivConfigType.Int)
	public int getPearlHealthStartValue() {
		return plugin.GetConfig().get("health.start_value").getInt();
	}

	@Override
	@CivConfig(name = "health.max_value", def = "336" , type = CivConfigType.Int)
	public int getPearlHealthMaxValue() {
		return plugin.GetConfig().get("health.max_value").getInt();
	}

	@Override
	@CivConfig(name = "autofree_worldborder", def = "true", type = CivConfigType.Bool)
	public boolean getShouldAutoFreeWorldBorder() {
		return plugin.GetConfig().get("autofree_worldborder").getBool();
	}

	@Override
	@CivConfig(name = "prison_musthotbar", def = "true", type = CivConfigType.Bool)
	public boolean getMustPrisonPearlHotBar() {
		return plugin.GetConfig().get("prison_musthotbar").getBool();
	}

	@Override
	@CivConfig(name = "suicide_time_seconds", def = "180" , type = CivConfigType.Int)
	public int getSuicideTimeoutSeconds() {
		return plugin.GetConfig().get("suicide_time_seconds").getInt();
	}

	@Override
	@CivConfig(name = "damagelog_min", def = "3" , type = CivConfigType.Int)
	public int getDamageLogMin() {
		return plugin.GetConfig().get("damagelog_min").getInt();
	}

	@Override
	@CivConfig(name = "damagelog_ticks", def = "600" , type = CivConfigType.Int)
	public int getDamagelogTicks() {
		return plugin.GetConfig().get("damagelog_ticks").getInt();
	}
	
	
	// Exile Rules

	@Override
	@CivConfig(name = "rules.pearl_radius", def = "1000", type = CivConfigType.Int)
	public int getRulePearlRadius() {
		return plugin.GetConfig().get("rules.pearl_radius").getInt();
	}

	@Override
	public boolean setRulePearlRadius(Integer value) {
		return plugin.GetConfig().set("rules.pearl_radius", value.toString());
	}

	@Override
	@CivConfig(name = "rules.damage_reinforcement", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanDamageReinforcement() {
		return plugin.GetConfig().get("rules.damage_reinforcement").getBool();
	}

	@Override
	public boolean setRuleCanDamageReinforcement(Boolean value) {
		return plugin.GetConfig().set("rules.damage_reinforcement", value.toString());
	}

	@Override
	@CivConfig(name = "rules.damage_bastion", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanDamageBastion() {
		return plugin.GetConfig().get("rules.damage_bastion").getBool();
	}

	@Override
	public boolean setRuleCanDamageBastion(Boolean value) {
		return plugin.GetConfig().set("rules.damage_bastion", value.toString());
	}

	@Override
	@CivConfig(name = "rules.create_bastion", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanCreateBastion() {
		return plugin.GetConfig().get("rules.create_bastion").getBool();
	}

	@Override
	public boolean setRuleCanCreateBastion(Boolean value) {
		return plugin.GetConfig().set("rules.create_bastion", value.toString());
	}

	@Override
	@CivConfig(name = "rules.enter_bastion", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanEnterBastion() {
		return plugin.GetConfig().get("rules.enter_bastion").getBool();
	}

	@Override
	public boolean setRuleCanEnterBastion(Boolean value) {
		return plugin.GetConfig().set("rules.enter_bastion", value.toString());
	}

	@Override
	@CivConfig(name = "rules.throw_pearl", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanThrowEnderPearl() {
		return plugin.GetConfig().get("rules.throw_pearl").getBool();
	}

	@Override
	public boolean setRuleCanThrowEnderPearl(Boolean value) {
		return plugin.GetConfig().set("rules.throw_pearl", value.toString());
	}

	@Override
	@CivConfig(name = "rules.chat_local", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanChatLocal() {
		return plugin.GetConfig().get("rules.chat_local").getBool();
	}

	@Override
	public boolean setRuleCanChatLocal(Boolean value) {
		return plugin.GetConfig().set("rules.chat_local", value.toString());
	}

	@Override
	@CivConfig(name = "rules.pvp", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanPvp() {
		return plugin.GetConfig().get("rules.pvp").getBool();
	}

	@Override
	public boolean setRuleCanPvp(Boolean value) {
		return plugin.GetConfig().set("rules.pvp", value.toString());
	}

	@Override
	@CivConfig(name = "rules.ignite", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanIgnite() {
		return plugin.GetConfig().get("rules.ignite").getBool();
	}

	@Override
	public boolean setRuleCanIgnite(Boolean value) {
		return plugin.GetConfig().set("rules.ignite", value.toString());
	}

	@Override
	@CivConfig(name = "rules.use_bucket", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanUseBucket() {
		return plugin.GetConfig().get("rules.use_bucket").getBool();
	}

	@Override
	public boolean setRuleCanUseBucket(Boolean value) {
		return plugin.GetConfig().set("rules.use_bucket", value.toString());
	}

	@Override
	@CivConfig(name = "rules.use_potions", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanUsePotions() {
		return plugin.GetConfig().get("rules.use_potions").getBool();
	}

	@Override
	public boolean setRuleCanUsePotions(Boolean value) {
		return plugin.GetConfig().set("rules.use_potions", value.toString());
	}

	@Override
	@CivConfig(name = "rules.use_bed", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanUseBed() {
		return plugin.GetConfig().get("rules.use_bed").getBool();
	}

	@Override
	public boolean setRuleCanUseBed(Boolean value) {
		return plugin.GetConfig().set("rules.use_bed", value.toString());
	}

	@Override
	@CivConfig(name = "rules.suicide", def = "true", type = CivConfigType.Bool)
	public boolean getRuleCanSuicide() {
		return plugin.GetConfig().get("rules.suicide").getBool();
	}

	@Override
	public boolean setRuleCanSuicide(Boolean value) {
		return plugin.GetConfig().set("rules.suicide", value.toString());
	}

	@Override
	@CivConfig(name = "rules.place_snitch", def = "false", type = CivConfigType.Bool)
	public boolean getRuleCanPlaceSnitch() {
		return plugin.GetConfig().get("rules.place_snitch").getBool();
	}

	@Override
	public boolean setRuleCanPlaceSnitch(Boolean value) {
		return plugin.GetConfig().set("rules.place_snitch", value.toString());
	}

	@Override
	@CivConfig(name = "rules.mine_blocks", def = "true", type = CivConfigType.Bool)
	public boolean getRuleCanMine() {
		return plugin.GetConfig().get("rules.mine_blocks").getBool();
	}

	@Override
	public boolean setRuleCanMine(Boolean value) {
		return plugin.GetConfig().set("rules.mine_blocks", value.toString());
	}

	@Override
	@CivConfig(name = "rules.brew", def = "true", type = CivConfigType.Bool)
	public boolean getRuleCanBrew() {
		return plugin.GetConfig().get("rules.brew").getBool();
	}

	@Override
	public boolean setRuleCanBrew(Boolean value) {
		return plugin.GetConfig().set("rules.brew", value.toString());
	}

	@Override
	@CivConfig(name = "rules.enchant", def = "true", type = CivConfigType.Bool)
	public boolean getRuleCanEnchant() {
		return plugin.GetConfig().get("rules.enchant").getBool();
	}

	@Override
	public boolean setRuleCanEnchant(Boolean value) {
		return plugin.GetConfig().set("rules.enchant", value.toString());
	}

	@Override
	public boolean canPerform(ExileRule rule) {
		switch(rule) {
		
		case PEARL_RADIUS:
			return getRulePearlRadius() > 0;

		case DAMAGE_REINFORCEMENT:
			return getRuleCanDamageReinforcement();
			
		case DAMAGE_BASTION:
			return getRuleCanDamageBastion();
			
		case CREATE_BASTION:
			return getRuleCanCreateBastion();
			
		case ENTER_BASTION:
			return getRuleCanEnterBastion();
			
		case THROW_PEARL:
			return getRuleCanThrowEnderPearl();
			
		case CHAT:
			return getRuleCanChatLocal();
			
		case PVP:
			return getRuleCanPvp();
			
		case IGNITE:
			return getRuleCanIgnite();
			
		case USE_BUCKET:
			return getRuleCanUseBucket();
			
		case USE_POTIONS:
			return getRuleCanUsePotions();
			
		case USE_BED:
			return getRuleCanUseBed();
			
		case SUICIDE:
			return getRuleCanSuicide();
			
		case SNITCH:
			return getRuleCanPlaceSnitch();
			
		case MINE:
			return getRuleCanMine();
			
		case BREW:
			return getRuleCanBrew();
			
		case ENCHANT:
			return getRuleCanEnchant();
			
		default:
			return false;
		}
	}
}
