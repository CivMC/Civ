package vg.civcraft.mc.citadel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;

import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementEffect;

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
	
	public static List<String> getReinforceableMaterials(String mat){
		if(config.getConfigurationSection("reinforcements." + mat).contains("reinforceables")) {
			return config.getConfigurationSection("reinforcements." + mat).getStringList("reinforceables");
		}
		return null;
	}
	
	public static List<String> getNonReinforceableMaterials(String mat){
		if(config.getConfigurationSection("reinforcements." + mat).contains("non_reinforceables")) {
			return config.getConfigurationSection("reinforcements." + mat).getStringList("non_reinforceables");
		}
		return null;
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
	
	public static int getGracePeriod(String type) {
		return config.getInt("reinforcements." + type + ".grace_period", 0); // default disabled
	}
	
	public static ReinforcementEffect getReinforcementEffect(String type){
		Particle effect = null;
		if (config.getString("reinforcements." + type + ".effect.type") != null) {
			try {
				String effectName = config.getString("reinforcements." + type + ".effect.type");
				effect = effectName.equals("FLYING_GLYPH") ? Particle.ENCHANTMENT_TABLE: Particle.valueOf(effectName);
			} catch (IllegalArgumentException e) {
				Citadel.getInstance().getLogger().log(Level.WARNING, "Invalid effect at: " + config.getCurrentPath());
				return null;
			}
			float offsetX = (float) config.getDouble("reinforcements." + type + ".effect.offsetX", 0);
			float offsetY = (float) config.getDouble("reinforcements." + type + ".effect.offsetY", 0);
			float offsetZ = (float) config.getDouble("reinforcements." + type + ".effect.offsetZ", 0);
			float speed = (float) config.getDouble("reinforcements." + type + ".effect.speed", 1);
			int amount = config.getInt("reinforcements." + type + ".effect.particleCount", 1);
			return new ReinforcementEffect(effect, offsetX, offsetY, offsetZ, speed, amount);
		}
		return null;
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

	public static int getPoolSize() {
		return config.getInt("mysql.poolsize", 20);
	}

	public static long getConnectionTimeout() {
		return config.getLong("mysql.connection_timeout", 10000l);
	}

	public static long getIdleTimeout() {
		return config.getLong("mysql.idle_timeout", 600000l);
	}

	public static long getMaxLifetime() {
		return config.getLong("mysql.max_lifetime", 7200000l);
	}

	public static boolean shouldLogInternal() {
		return config.getBoolean("internal_logging", false);
	}

	public static boolean shouldLogPlayerCommands() {
		return config.getBoolean("command_logging", false);
	}

	public static boolean shouldLogFriendlyBreaks() {
		return config.getBoolean("break_logging", false);
	}

	public static boolean shouldLogHostileBreaks() {
		return config.getBoolean("hostile_logging", false);
	}

	public static boolean shouldLogDamage() {
		return config.getBoolean("damage_logging", false);
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
	
	public static Set<Material> parseMaterialList(List <String> stringList) {
		Set <Material> reinforceableMats;
		if (stringList == null || stringList.size() == 0) {
			reinforceableMats = null;
		}
		else {
			reinforceableMats = new HashSet<Material>();
			for(String s : stringList) {
				try {
					Material reinmat = Material.valueOf(s);
					reinforceableMats.add(reinmat);
				}
				catch (IllegalArgumentException e) {
					Citadel.getInstance().getLogger().warning("The specified reinforceable material " + s + " could not be parsed");					
				}
			}
		}
		return reinforceableMats;
	}

	public static boolean breakAcidedBlockNaturally() {
		return config.getBoolean("break_acided_block_naturally", false);
	}

}
