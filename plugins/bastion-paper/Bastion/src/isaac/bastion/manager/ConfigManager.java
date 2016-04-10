package isaac.bastion.manager;

import isaac.bastion.Bastion;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
	private FileConfiguration config;
	
	private String host;
	private int port;
	private String database;
	private String prefix;
	
	private String username;
	private String password;
	
	private String meta;
	
	private Material bastionBlockMaterial;
	private int bastionBlockEffectRadius;
	private double bastionBlockScaleFacStart;
	private double bastionBlockScaleFacEnd;
	private int bastionBlockScaleTime;
	private int bastionBlockMaxBreaks;
	private int bastionBlockErosion;
	private int bastionBlocksToErode;
	private int saveTimeInt;
	private boolean preventEnderPearl;
	private boolean enderPearlBlockingRequiresMaturity;
	private boolean destroy;
	private double enderPearlErosionScale;
	private boolean squareField;
	private boolean blockMidAir;
	private boolean includeBastionYLevel;
	private boolean consumePearlOnBlock;
	
	// elytra
	private boolean blockElytra;
	private boolean destroyElytraOnBlock;
	private boolean damageElytraOnBlock;
	private double elytraErosionScale;
	private boolean elytraBlockingRequiresMaturity;
	
	static String file_name = "config.yml";
	
	public ConfigManager() {
		Bastion.getPlugin().saveDefaultConfig();
		Bastion.getPlugin().reloadConfig();
		config = Bastion.getPlugin().getConfig();
		
		load();
	}
	
	private void load() {
		host = loadString("mysql.host");
		port = loadInt("mysql.port");
		database = loadString("mysql.database");
		prefix = loadString("mysql.prefix");
		
		username = loadString("mysql.username");
		password = loadString("mysql.password");
		int savesPerDay = loadInt("mysql.savesPerDay");
		if (savesPerDay !=0) {
			saveTimeInt = 1728000 / savesPerDay; // ticks * secs * mins * hours
		} else{
			saveTimeInt = 0;
		}
		
		bastionBlockMaterial = Material.getMaterial(loadString("BastionBlock.material"));
		bastionBlockEffectRadius = loadInt("BastionBlock.effectRadius");
		bastionBlockMaxBreaks = loadInt("BastionBlock.maxBreaksPerMinute");
		bastionBlockErosion = loadInt("BastionBlock.erosionRatePerDay");
		bastionBlockScaleFacStart = loadDouble("BastionBlock.startScaleFactor");
		bastionBlockScaleFacEnd = loadDouble("BastionBlock.finalScaleFactor");
		bastionBlockScaleTime = loadInt("BastionBlock.warmUpTime");
		destroy = loadBool("BastionBlock.destroyOnRemove");
		bastionBlocksToErode = loadInt("BastionBlock.blocksToErode");
		squareField = loadBool("BastionBlock.squarefield");
		includeBastionYLevel = loadBool("BastionBlock.includeYlevel");
		
		preventEnderPearl = loadBool("BastionBlock.EnderPearls.preventEnderPearl");
		enderPearlBlockingRequiresMaturity = loadBool("BastionBlock.EnderPearls.requireMaturity");
		enderPearlErosionScale = loadDouble("BastionBlock.EnderPearls.scaleFac");
		blockMidAir = loadBool("BastionBlock.EnderPearls.block_midair");
		consumePearlOnBlock = loadBool("BastionBlock.EnderPearls.consumeOnBlock");
		
		blockElytra = loadBool("BastionBlock.Elytra.block");
		destroyElytraOnBlock = loadBool("BastionBlock.Elytra.destroyOnBlock");
		damageElytraOnBlock = loadBool("BastionBlock.Elytra.damageOnBlock");
		elytraErosionScale = loadDouble("BastionBlock.Elytra.scaleFac");
		elytraBlockingRequiresMaturity = loadBool("BastionBlock.Elytra.requireMaturity");
	}
	
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	public String getDatabase() {
		return database;
	}
	public String getPrefix() {
		return prefix;
	}
	
	public boolean includeSameYLevel() {
		return includeBastionYLevel;
	}
	
	public String getUsername() {
		return username;
	}
	public String getPassword() {
		return password;
	}
	public int getTimeBetweenSaves() {
		return saveTimeInt;
	}
	
	public Material getBastionBlockMaterial() {
		return bastionBlockMaterial;
	}
	
	public boolean squareField() {
		return squareField;
	}
	
	public boolean blockMidAir() {
		return blockMidAir;
	}
	
	public String getNeededMeta() {
		return meta;
	}
	
	public int getBastionBlockEffectRadius() {
		return bastionBlockEffectRadius;
	}
	public double getBastionBlockScaleFacStart() {
		return bastionBlockScaleFacStart;
	}
	public double getBastionBlockScaleFacEnd() {
		return bastionBlockScaleFacEnd;
	}
	public int getBastionBlockScaleTime() {
		return bastionBlockScaleTime;
	}
	public int getBastionBlockMaxBreaks() {
		return bastionBlockMaxBreaks;
	}
	public int getBastionBlockErosion() {
		return bastionBlockErosion;
	}
	public int getBastionBlocksToErode() {
		return bastionBlocksToErode;
	}
	public boolean getDestroy() {
		return destroy;
	}
	
	public boolean getEnderPearlsBlocked() {
		return preventEnderPearl;
	}
	public boolean getEnderPearlRequireMaturity() {
		return enderPearlBlockingRequiresMaturity;
	}
	public double getEnderPearlErosionScale() {
		return enderPearlErosionScale;
	}
	public boolean getConsumePearlOnBlock() {
		return consumePearlOnBlock;
	}
	
	public boolean getElytraIsBlocked() {
		return blockElytra;
	}
	public boolean getElytraIsDestroyOnBlock() {
		return destroyElytraOnBlock;
	}
	public boolean getElytraIsDamagedOnBlock() {
		return damageElytraOnBlock;
	}
	public double getElytraErosionScale() {
		return elytraErosionScale;
	}
	public boolean getElytraBlockingRequiresMaturity() {
		return elytraBlockingRequiresMaturity;
	}
	
	private int loadInt(String field) {
		if (config.isInt(field)) {
			int value = config.getInt(field);
			return value;
		}
		return Integer.MIN_VALUE;
		
	}
	private String loadString(String field) {
		if (config.isString(field)) {
			String value = config.getString(field);
			return value;
		}
		return null;
	}
	private boolean loadBool(String field) {
		if (config.isBoolean(field)) {
			boolean value = config.getBoolean(field);
			return value;
		}
		return false;
	}
	private double loadDouble(String field) {
		if (config.isDouble(field)) {
			double value = config.getDouble(field);
			return value;
		}
		if (config.isInt(field)) {
			double value = config.getInt(field);
			return value;
		}
		return Double.NaN;
	}
}
