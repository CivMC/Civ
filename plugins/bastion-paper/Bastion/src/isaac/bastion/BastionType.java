package isaac.bastion;

import java.util.LinkedHashMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.MaterialData;

public class BastionType {
	
	private static LinkedHashMap<String, BastionType> types = new LinkedHashMap<String, BastionType>();
	private static String defaultType;
	private static int maxRadius = 0;

	private String name;
	private MaterialData material;
	private String lore;
	private boolean square;
	private int effectRadius;
	private int radiusSquared;
	private boolean includeY;
	private int startScaleFactor;
	private double finalScaleFactor;
	private long warmupTime;
	private int erosionTime;
	private long placementCooldown;
	private boolean destroyOnRemove;
	private boolean blockPearls;
	private boolean blockMidair;
	private int pearlScale;
	private boolean pearlRequireMature;
	private boolean consumeOnBlock;
	private int blocksToErode;
	private boolean blockElytra;
	private boolean destroyElytra;
	private boolean damageElytra;
	private int elytraScale;
	private boolean elytraRequireMature;
	private boolean damageFirstBastion;
	private int regenTime;
	
	public BastionType(String name, MaterialData material, String lore, boolean square, int effectRadius,
			boolean includeY, int startScaleFactor, double finalScaleFactor, long warmupTime,
			int erosionTime, long placementCooldown, boolean destroyOnRemove, boolean blockPearls,
			boolean blockMidair, int pearlScale, boolean pearlRequireMature, boolean consumeOnBlock, int blocksToErode,
			boolean blockElytra, boolean destroyOnBlockElytra, boolean damageElytra, int elytraScale,
			boolean elytraRequireMature, boolean damageFirstBastion, int regenTime) {
		this.name = name;
		this.material = material;
		this.lore = lore;
		this.square = square;
		this.effectRadius = effectRadius;
		this.radiusSquared = effectRadius*effectRadius;
		this.includeY = includeY;
		this.startScaleFactor = startScaleFactor;
		this.finalScaleFactor = finalScaleFactor;
		this.warmupTime = warmupTime;
		this.erosionTime = erosionTime;
		this.placementCooldown = placementCooldown;
		this.destroyOnRemove = destroyOnRemove;
		this.blockPearls = blockPearls;
		this.blockMidair = blockMidair;
		this.pearlScale = pearlScale;
		this.pearlRequireMature = pearlRequireMature;
		this.consumeOnBlock = consumeOnBlock;
		this.blocksToErode = blocksToErode;
		this.blockElytra = blockElytra;
		this.destroyElytra = destroyOnBlockElytra;
		this.damageElytra = damageElytra;
		this.elytraScale = elytraScale;
		this.elytraRequireMature = elytraRequireMature;
		this.damageFirstBastion = damageFirstBastion;
		maxRadius = effectRadius > maxRadius ? effectRadius : maxRadius;
	}

	/**
	 * @return The material for this bastion type
	 */
	public MaterialData getMaterial() {
		return material;
	}

	/**
	 * @return The lore for this bastion type
	 */
	public String getLore() {
		return lore;
	}

	/**
	 * @return true if bastion is square
	 */
	public boolean isSquare() {
		return square;
	}

	/**
	 * @return the radius of the bastion
	 */
	public int getEffectRadius() {
		return effectRadius;
	}

	/**
	 * 
	 * @return the radius of the bastion squared
	 */
	public int getRadiusSquared() {
		return radiusSquared;
	}
	
	/**
	 * 
	 * @return true if the bastion blocks on it's own y level
	 */
	public boolean isIncludeY() {
		return includeY;
	}

	/**
	 * 
	 * @return The scale for amplifying damage when a bastion is first placed
	 */
	public int getStartScaleFactor() {
		return startScaleFactor;
	}

	/**
	 * 
	 * @return The scale for amplifying damage when a bastion is mature
	 */
	public double getFinalScaleFactor() {
		return finalScaleFactor;
	}

	/**
	 * 
	 * @return The time in ms it takes for a bastion to mature
	 */
	public long getWarmupTime() {
		return warmupTime;
	}
	
	/**
	 * 
	 * @return The amount of time between erosions
	 */
	public int getErosionTime() {
		return erosionTime;
	}

	/**
	 * 
	 * @return true if the physical block should be destroyed when a bastion dies
	 */
	public boolean isDestroyOnRemove() {
		return destroyOnRemove;
	}

	/**
	 * 
	 * @return true if the bastion blocks pearls
	 */
	public boolean isBlockPearls() {
		return blockPearls;
	}

	/**
	 * 
	 * @return true if the bastion blocks midair rather than when the pearl lands
	 */
	public boolean isBlockMidair() {
		return blockMidair;
	}

	/**
	 * 
	 * @return The scale for increased bastion damage from pearls
	 */
	public int getPearlScaleFactor() {
		return pearlScale;
	}

	/**
	 * 
	 * @return true if the bastion must be mature to block pearls
	 */
	public boolean isRequireMaturity() {
		return pearlRequireMature;
	}

	/**
	 * 
	 * @return true if pearls are consumed when blocked by bastion
	 */
	public boolean isConsumeOnBlock() {
		return consumeOnBlock;
	}
	
	/**
	 * 
	 * @return The number of bastions that should be eroded from a block place
	 */
	public int getBlocksToErode() {
		return blocksToErode;
	}
	
	/**
	 * 
	 * @return The cooldown for damaging a bastion
	 */
	public long getPlacementCooldown() {
		return placementCooldown;
	}
	
	/**
	 * 
	 * @return true if the bastion blocks elytra
	 */
	public boolean isBlockElytra() {
		return blockElytra;
	}

	/**
	 * 
	 * @return true if the bastion destroys elytra
	 */
	public boolean isDestroyElytra() {
		return destroyElytra;
	}

	/**
	 * 
	 * @return true if the bastion just damages elytra
	 */
	public boolean isDamageElytra() {
		return damageElytra;
	}

	/**
	 * 
	 * @return The scale for increased damage from elytra collision
	 */
	public int getElytraScale() {
		return elytraScale;
	}

	/**
	 * 
	 * @return true if bastion must be mature to block elytra
	 */
	public boolean isElytraRequireMature() {
		return elytraRequireMature;
	}
	
	/**
	 * 
	 * @return true if only the first bastion hit should be damaged by pearls
	 */
	public boolean damageFirstBastion() {
		return damageFirstBastion;
	}
	
	/**
	 * 
	 * @return The amount of time between regenerations
	 */
	public int getRegenTime() {
		return regenTime;
	}
	
	/**
	 * Gets the name of the bastion type
	 * This is used to reduce space in maps elsewhere
	 * @return The name of this bastion type
	 */
	public String getName() {
		return name;
	}

	public int hashCode() {
		return name.hashCode();
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof BastionType)) return false;
		BastionType other = (BastionType) obj;
		return other.getName().equals(name);
	}

	public static void loadBastionTypes(ConfigurationSection config) {
		for(String key : config.getKeys(false)) {
			if(defaultType == null) defaultType = key;
			types.put(key, getBastionType(config.getConfigurationSection(key)));
		}
	}
	
	public static BastionType getBastionType(String name) {
		return types.get(name);
	}
	
	public static BastionType getBastionType(MaterialData mat, String lore) {
		if(lore == null) lore = "";
		for(BastionType type : types.values()) {
			if(type.material.equals(mat) && type.lore.equals(lore)) return type;
		}
		return null;
	}
	
	/**
	 * 
	 * @return The radius of the largest bastion type
	 */
	public static int getMaxRadius() {
		return maxRadius;
	}
	
	/**
	 * 
	 * @return The default bastion type
	 */
	public static String getDefaultType() {
		return defaultType;
	}
	
	@SuppressWarnings("deprecation")
	public static BastionType getBastionType(ConfigurationSection config) {
		String name = config.getName();
		Material mat = Material.getMaterial(config.getString("block.material"));
		byte data = config.contains("block.durability") ? (byte)config.getInt("block.durability") : 0;
		MaterialData material = new MaterialData(mat, data);
		String lore = config.getString("block.lore");
		boolean square = config.getBoolean("squarefield");
		int effectRadius = config.getInt("effectRadius");
		boolean includeY = config.getBoolean("includeY");
		int startScaleFactor = config.getInt("startScaleFactor");
		double finalScaleFactor = config.getDouble("finalScaleFactor");
		long warmupTime = config.getLong("warmupTime");
		int erosionTime = config.getInt("erosionPerDay");
		if(erosionTime > 0) {
			erosionTime = 1728000 / erosionTime;
		} else if(erosionTime < 0) {
			erosionTime = 0;
		}
		long placementCooldown = config.getLong("placementCooldown");
		boolean destroyOnRemove = config.getBoolean("destroyOnRemove");
		boolean blockPearls = config.getBoolean("blockPearls");
		boolean blockMidair = config.getBoolean("blockPearlsMidair");
		int scaleFactor = config.getInt("pearlScaleFactor");
		boolean requireMaturity = config.getBoolean("pearlRequireMaturity");
		boolean consumeOnBlock = config.getBoolean("consumePearl");
		boolean damageFirstBastion = config.getBoolean("damageFirstBastion");
		int blocksToErode = config.getInt("blocksToErode");
		boolean blockElytra = config.getBoolean("blockElytra");
		boolean destroyElytra = config.getBoolean("destroyElytra");
		boolean damageElytra = config.getBoolean("damageElytra");
		int elytraScale = config.getInt("elytraScale");
		boolean elytraRequireMature = config.getBoolean("elytraRequireMaturity");
		int regenTime = config.getInt("regenPerDay");
		if(regenTime > 0) {
			regenTime = 1728000 / regenTime;
		} else if(regenTime < 0) {
			regenTime = 0;
		}
		return new BastionType(name, material, lore, square, effectRadius, includeY, startScaleFactor, finalScaleFactor, warmupTime,
				erosionTime, placementCooldown, destroyOnRemove, blockPearls, blockMidair, scaleFactor, requireMaturity, consumeOnBlock, 
				blocksToErode, blockElytra, destroyElytra, damageElytra, elytraScale, elytraRequireMature, damageFirstBastion, regenTime);
	}
}