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
}
