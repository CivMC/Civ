package com.devotedmc.ExilePearl;

import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;

public class ExilePearlConfig {

	private final ExilePearlPlugin plugin;
	
	public ExilePearlConfig(ExilePearlPlugin plugin) {
		this.plugin = plugin;
	}
	
	@CivConfig(name = "database.mysql.username", def = "bukkit", type = CivConfigType.String)
	public String getUsername() {
		return plugin.GetConfig().get("database.mysql.username").getString();
	}
	
	@CivConfig(name = "database.mysql.password", def = "", type = CivConfigType.String)
	public String getPassword() {
		return plugin.GetConfig().get("database.mysql.password").getString();
	}
	
	@CivConfig(name = "database.mysql.dbname", def = "bukkit", type = CivConfigType.String)
	public String getDBName() {
		return plugin.GetConfig().get("database.mysql.dbname").getString();
	}
	
	@CivConfig(name = "database.mysql.host", def = "localhost", type = CivConfigType.String)
	public String getHost() {
		return plugin.GetConfig().get("database.mysql.host").getString();
	}
	
	@CivConfig(name = "database.mysql.port", def = "3306", type = CivConfigType.Int)
	public int getPort() {
		return plugin.GetConfig().get("database.mysql.port").getInt();
	}
	
	@CivConfig(name = "upkeep.interval_min", def = "60" , type = CivConfigType.Int)
	public int getPearlUpkeepIntervalMin() {
		return plugin.GetConfig().get("upkeep.interval_min").getInt();
	}
	
	@CivConfig(name = "upkeep.resource", def = "263", type = CivConfigType.Int)
	public int getPearlUpkeepMaterial() {
		return plugin.GetConfig().get("upkeep.resource").getInt();
	}
	
	@CivConfig(name = "upkeep.amount", def = "1" , type = CivConfigType.Int)
	public int getPearlUpkeepAmount() {
		return plugin.GetConfig().get("upkeep.amount").getInt();
	}
	
	@CivConfig(name = "upkeep.start_strength", def = "4" , type = CivConfigType.Int)
	public int getPearlStartStrength() {
		return plugin.GetConfig().get("upkeep.start_strength").getInt();
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
}
