package isaac.bastion;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.material.MaterialData;

public class BastionType {
	
	private static HashMap<String, BastionType> types = new HashMap<String, BastionType>();
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
	private int erosionPerDay;
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
	
	public BastionType(String name, MaterialData material, String lore, boolean square, int effectRadius,
			boolean includeY, int startScaleFactor, double finalScaleFactor, long warmupTime,
			int erosionPerDay, long placementCooldown, boolean destroyOnRemove, boolean blockPearls,
			boolean blockMidair, int pearlScale, boolean pearlRequireMature, boolean consumeOnBlock, int blocksToErode,
			boolean blockElytra, boolean destroyOnBlockElytra, boolean damageElytra, int elytraScale,
			boolean elytraRequireMature) {
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
		this.erosionPerDay = erosionPerDay;
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
		maxRadius = effectRadius > maxRadius ? effectRadius : maxRadius;
	}

	public MaterialData getMaterial() {
		return material;
	}

	public String getLore() {
		return lore;
	}

	public boolean isSquare() {
		return square;
	}

	public int getEffectRadius() {
		return effectRadius;
	}

	public int getRadiusSquared() {
		return radiusSquared;
	}
	
	public boolean isIncludeY() {
		return includeY;
	}

	public int getStartScaleFactor() {
		return startScaleFactor;
	}

	public double getFinalScaleFactor() {
		return finalScaleFactor;
	}

	public long getWarmupTime() {
		return warmupTime;
	}
	
	public int getErosionPerDay() {
		return erosionPerDay;
	}

	public boolean isDestroyOnRemove() {
		return destroyOnRemove;
	}

	public boolean isBlockPearls() {
		return blockPearls;
	}

	public boolean isBlockMidair() {
		return blockMidair;
	}

	public int getPearlScaleFactor() {
		return pearlScale;
	}

	public boolean isRequireMaturity() {
		return pearlRequireMature;
	}

	public boolean isConsumeOnBlock() {
		return consumeOnBlock;
	}
	
	public int getBlocksToErode() {
		return blocksToErode;
	}
	
	public long getPlacementCooldown() {
		return placementCooldown;
	}
	
	public boolean isBlockElytra() {
		return blockElytra;
	}

	public boolean isDestroyElytra() {
		return destroyElytra;
	}

	public boolean isDamageElytra() {
		return damageElytra;
	}

	public int getElytraScale() {
		return elytraScale;
	}

	public boolean isElytraRequireMature() {
		return elytraRequireMature;
	}

	public int hashCode() {
		return name.hashCode();
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof BastionType)) return false;
		BastionType other = (BastionType) obj;
		return other.equals(name);
	}

	public static void loadBastionTypes(ConfigurationSection config) {
		for(String key : config.getKeys(false)) {
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
	
	public static int getMaxRadius() {
		return maxRadius;
	}
	
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
		int erosionPerDay = config.getInt("erosionPerDay");
		if(erosionPerDay != 0) {
			erosionPerDay = 1728000 / erosionPerDay;
		}
		long placementCooldown = config.getLong("placementCooldown");
		boolean destroyOnRemove = config.getBoolean("destroyOnRemove");
		boolean blockPearls = config.getBoolean("blockPearls");
		boolean blockMidair = config.getBoolean("blockMidair");
		int scaleFactor = config.getInt("pearlScaleFactor");
		boolean requireMaturity = config.getBoolean("requireMaturity");
		boolean consumeOnBlock = config.getBoolean("consumeOnBlock");
		int blocksToErode = config.getInt("blocksToErode");
		boolean blockElytra = config.getBoolean("blockElytra");
		boolean destroyElytra = config.getBoolean("destroyElytra");
		boolean damageElytra = config.getBoolean("damageElytra");
		int elytraScale = config.getInt("elytraScale");
		boolean elytraRequireMature = config.getBoolean("elytraRequireMaturity");
		return new BastionType(name, material, lore, square, effectRadius, includeY, startScaleFactor, finalScaleFactor, warmupTime,
				erosionPerDay, placementCooldown, destroyOnRemove, blockPearls, blockMidair, scaleFactor, requireMaturity, consumeOnBlock, 
				blocksToErode, blockElytra, destroyElytra, damageElytra, elytraScale, elytraRequireMature);
	}
}