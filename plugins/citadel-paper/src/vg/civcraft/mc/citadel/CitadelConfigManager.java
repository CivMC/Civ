package vg.civcraft.mc.citadel;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class CitadelConfigManager {

	private static FileConfiguration config;
	public CitadelConfigManager(FileConfiguration con){
		config = con;
	}
	
	public static List<String> getReinforcementTypes(){
		List<String> reinforcementTypes = new ArrayList<String>();
		for (String sect: config.getConfigurationSection("reinforcements").getKeys(false))
			reinforcementTypes.add(sect);
		return reinforcementTypes;
	}
	
	public static List<String> getNaturalReinforcementTypes(){
		List<String> naturalReinforcementTypes = new ArrayList<String>();
		if (config.getConfigurationSection("natural_reinforcements") == null)
			return naturalReinforcementTypes;
		for (String sect: config.getConfigurationSection
				("natural_reinforcements").getKeys(false))
			naturalReinforcementTypes.add(sect);
		return naturalReinforcementTypes;
	}
	
	public static List<String> getNonReinforceableTypes(){
		List<String> nonReinforcementTypes = new ArrayList<String>();
		if (config.getStringList("non_reinforceables") == null){
			return nonReinforcementTypes;
		}
		for (String sect: config.getStringList("non_reinforceables"))
			nonReinforcementTypes.add(sect);
		return nonReinforcementTypes;
	}
	
	public static int getRequireMents(String type){
		return config.getInt("reinforcements." + type + ".requirements");
	}
	
	public static int getReturns(String type){
		return config.getInt("reinforcements." + type + ".return");
	}
	
	public static Material getMaterial(String type){
		return Material.getMaterial(config.getString("reinforcements." + type + ".material"));
	}
	
	public static int getPercentReturn(String type){
		return config.getInt("reinforcements." + type + ".percent_chance");
	}
	
	public static int getHitPoints(String type){
		return config.getInt("reinforcements." + type + ".hit_points");
	}
	
	public static int getMaturationTime(String type){
		return config.getInt("reinforcements." + type + ".mature_time");
	}

	public static int getAcidTime(String type) {
		return config.getInt("reinforcements." + type + ".acid_time", getMaturationTime(type));
	}
	
	public static int getMaturationScale(String type){
		return config.getInt("reinforcements." + type + ".scale_amount");
	}
	
	public static List<String> getLoreValues(String type){
		return config.getStringList("reinforcements." + type + ".lore");
	}
	
	public static Material getNaturalReinforcementMaterial(String type){
		return Material.valueOf(config.getString("natural_reinforcements." +
				type + ".material"));
	}
	
	public static int getNaturalReinforcementHitPoints(String type){
		return config.getInt("natural_reinforcements." + type + ".hit_points");
	}
	
	public static int getPlayerStateReset(){
		return config.getInt("reset_player_state");
	}
	
	public static boolean isMaturationEnabled(){
		return config.getBoolean("enable_maturation");
	}
	
	public static int getMaxCacheSize(){
		return config.getInt("max_cache_size");
	}
	
	public static long getMaxCacheMinutes(){
		return config.getLong("max_cache_load_time");
	}
	
	public static int getMaxRedstoneDistance(){
		return config.getInt("redstone_distance");
	}
	
	public static Material getAcidBlock(){
		return Material.valueOf(config.getString("acidblock_material"));
	}
	
	public static boolean shouldDropReinforcedBlock(){
		return config.getBoolean("drop_reinforced_block");
	}
	
	public static int getTickRepeatingSave(){
		return config.getInt("save_interval_ticks", 500);
	}
	
	public static int getDayMultiplier(){
		return config.getInt("reinforcement_damageMultiplier", 7);
	}
	
	public static String getHostName(){
		return config.getString("mysql.hostname", "localhost");
	}
	
	public static int getPort(){
		return config.getInt("mysql.port", 3306);
	}
	
	public static String getDBName(){
		return config.getString("mysql.dbname", "bukkit");
	}
	
	public static String getUserName(){
		return config.getString("mysql.username", "bukkit");
	}
	
	public static String getPassword(){
		return config.getString("mysql.password", "");
	}

	public static boolean shouldLogInternal() {
		return config.getBoolean("internal_logging", false);
	}

	public static boolean shouldLogPlayerCommands() {
		return config.getBoolean("command_logging", false);
	}

	public static boolean shouldLogBreaks() {
		return config.getBoolean("break_logging", false);
	}

	public static boolean shouldLogReinforcement() {
		return config.getBoolean("reinf_logging", false);
	}

	public static boolean showHealthAsPercent(){
		return config.getBoolean("show_health_as_percent", false);
	}
	
	public static boolean defaultBypassOn() {
		return config.getBoolean("default_bypass_mode");
	}
}
