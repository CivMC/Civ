package isaac.bastion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class BastionType {
	
	private static LinkedHashMap<String, BastionType> types = new LinkedHashMap<>();
	private static String defaultType;
	private static int maxRadius = 0;

	private String name;
	private String itemName;
	private List<String> lore;
	private String shortName;
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
	private boolean onlyDirectDestruction;
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
	private boolean explodeOnBlock;
	private double explodeOnBlockStrength;
	private boolean damageFirstBastion;
	private int regenTime;
	private boolean allowPearlingOut;
	private boolean blockReinforcements;
	private Material material;
	private boolean destroyOnRemoveWhileImmature;
	private int proximityDamageRange;
	private double proximityDamageFactor;
	
	private String overLayName;
	
	public BastionType(
			String name,
			String itemName,
			Material material,
			List<String> lore,
			String shortName,
			boolean square,
			int effectRadius,
			boolean includeY,
			int startScaleFactor,
			double finalScaleFactor,
			long warmupTime,
			int erosionTime,
			long placementCooldown,
			boolean destroyOnRemove,
			boolean blockPearls,
			boolean blockMidair,
			int pearlScale,
			boolean pearlRequireMature,
			boolean consumeOnBlock,
			int blocksToErode,
			boolean blockElytra,
			boolean destroyOnBlockElytra,
			boolean damageElytra,
			int elytraScale,
			boolean elytraRequireMature,
			boolean explodeOnBlock,
			double explodeOnBlockStrength,
			boolean damageFirstBastion,
			int regenTime,
			boolean onlyDirectDestruction,
			boolean allowPearlingOut,
			boolean blockReinforcements,
			boolean destroyOnRemoveWhileImmature,
			int proximityDamageRange,
			double proximityDamageFactor,
			String overLayName
	) {
		this.name = name;
		this.material = material;
		this.itemName = itemName;
		this.lore = lore;
		this.shortName = shortName;
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
		this.onlyDirectDestruction = onlyDirectDestruction;
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
		this.explodeOnBlock = explodeOnBlock;
		this.explodeOnBlockStrength = explodeOnBlockStrength;
		this.damageFirstBastion = damageFirstBastion;
		this.regenTime = regenTime;
		this.allowPearlingOut = allowPearlingOut;
		this.blockReinforcements = blockReinforcements;
		this.destroyOnRemoveWhileImmature = destroyOnRemoveWhileImmature;
		this.proximityDamageFactor = proximityDamageFactor;
		this.proximityDamageRange = proximityDamageRange;
		this.overLayName = overLayName;
		
		maxRadius = effectRadius > maxRadius ? effectRadius : maxRadius;
	}

	/**
	 * @return The lore for this bastion type
	 */
	public List<String> getLore() {
		return lore;
	}

	/**
	 * @return The item / display name for this Bastion type
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * @return Short name for this Bastion type. Used by /bsl command
	 */
	public String getShortName() {
		return shortName;
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
	 * @return Whether players are allowed to pearl out of this field, 
	 * 	meaning they are standing inside of the field, but their pearl landed in unbastioned territory
	 */
	public boolean canPearlOut() {
		return allowPearlingOut;
	}
	
	/**
	 * @return Name to use in HUD to describe type
	 */
	public String getOverlayName() {
		return overLayName;
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
	 * @return true if a bastion can only be removed by destroying the block itself.
	 */
	public boolean isOnlyDirectDestruction() {
		return onlyDirectDestruction;
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
	 * @return true if there should be an explosion on elytra colision
	 */
	public boolean isExplodeOnBlock() {
		return explodeOnBlock;
	}
	
	/**
	 * 
	 * @return the strength of the explosion on elytra collision
	 */
	public double getExplosionStrength() {
		return explodeOnBlockStrength;
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
	
	public Material getMaterial() {
		return material;
	}
	
	/**
	 * Gets the name of the bastion type
	 * This is used to reduce space in maps elsewhere
	 * @return The name of this bastion type
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return true if the bastion should prevent citadel reinforcements
	 */
	public boolean isBlockReinforcements() {
		return blockReinforcements;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof BastionType)) return false;
		BastionType other = (BastionType) obj;
		return other.getName().equals(name);
	}
	
	/**
	 * Creates an item representation of the bastion type
	 * @return The bastion item
	 */
	public ItemStack getItemRepresentation() {
		ItemStack is = new ItemStack(material);
		if ((lore == null || lore.size() == 0) && itemName == null) return is;

		ItemMeta im = is.hasItemMeta() ? is.getItemMeta() : Bukkit.getItemFactory().getItemMeta(material);
		if (im == null) {
			Bastion.getPlugin().getLogger().log(Level.WARNING, "Invalid Bastion configuration, unable to represent as an item for {0}", name);
			return is;
		}
		if (lore != null) {
			im.setLore(lore);
		}
		if (itemName != null) {
			im.setDisplayName(itemName);
		}
		is.setItemMeta(im);
		//Bastion.getPlugin().getLogger().log(Level.INFO, "Bastion {0} represented as {1}", new Object[] {name, is.toString()});
		return is;
	}

	public static void loadBastionTypes(ConfigurationSection config) {
		for(String key : config.getKeys(false)) {
			Bastion.getPlugin().getLogger().log(Level.INFO, "Loading Bastion type {0}", key);
			BastionType type = getBastionType(config.getConfigurationSection(key));
			if(type != null) {
				if(defaultType == null) defaultType = key;
				types.put(key, type);
				Bastion.getPlugin().getLogger().log(Level.INFO, "Bastion type {0} loaded: {1}", new Object[]{key, type});
			}
		}
	}
	
	public static void startRegenAndErosionTasks() {
		for(BastionType type : types.values()) {
			if(type.erosionTime > 0) {
				new BukkitRunnable() {
					@Override
					public void run() {
						Bastion.getPlugin().getLogger().log(Level.INFO, "Erosion task begin, found " + 
								Bastion.getBastionStorage().getBastionsForType(type).size() + " to erode");
						for(BastionBlock bastion : Bastion.getBastionStorage().getBastionsForType(type)) {
							bastion.erode(1);
						}
						Bastion.getPlugin().getLogger().log(Level.INFO, "Erosion task ended, after erosion " + 
								Bastion.getBastionStorage().getBastionsForType(type).size() + " remain");
					}
				}.runTaskTimerAsynchronously(Bastion.getPlugin(), type.erosionTime, type.erosionTime);
			}
			if(type.regenTime > 0) {
				new BukkitRunnable() {
					@Override
					public void run() {
						Bastion.getPlugin().getLogger().log(Level.INFO, "Regen task begin, found " + 
								Bastion.getBastionStorage().getBastionsForType(type).size() + " to regen");
						for(BastionBlock bastion : Bastion.getBastionStorage().getBastionsForType(type)) {
							bastion.regen();
						}
						Bastion.getPlugin().getLogger().log(Level.INFO, "Regen task ended, after regen " + 
								Bastion.getBastionStorage().getBastionsForType(type).size() + " remain");

					}
				}.runTaskTimerAsynchronously(Bastion.getPlugin(), type.regenTime, type.regenTime);
			}
		}
	}
	
	public static BastionType getBastionType(String name) {
		return types.get(name);
	}
	
	public static BastionType getBastionType(Material mat, String itemName, List<String> lore) {
		if (lore != null && lore.size() == 0) lore = null;
		for (BastionType type : types.values()) {
			//StringBuilder sb = new StringBuilder();
			boolean test = type.material.equals(mat);
			//sb.append(type.getName()).append(" is").append(test ? " " : "n't ").append(mat);
			test &= ((itemName == null && type.itemName == null) || (type.itemName != null && type.itemName.equals(itemName)));
			//sb.append(" name is ").append(itemName).append(test ? " = " : " not ").append(type.itemName);
			test &= ((lore == null && (type.lore == null || type.lore.size() == 0)) || (type.lore != null && type.lore.equals(lore)));
			//sb.append(" lore is ").append(lore).append(test ? " = " : " not ").append(type.lore);
			//Bastion.getPlugin().getLogger().log(Level.INFO, "BastionType check {0}", sb);
			if (test) return type;
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
	
	public boolean isDestroyOnRemoveWhileImmature() {
		return destroyOnRemoveWhileImmature;
	}
	
	/**
	 * 
	 * @return The default bastion type
	 */
	public static String getDefaultType() {
		return defaultType;
	}
	
	public int getProximityDamageRange() {
		return proximityDamageRange;
	}
	
	public double getProximityDamageFactor() {
		return proximityDamageFactor;
	}
	
	public static BastionType getBastionType(ConfigurationSection config) {
		String name = config.getName();
		Material material = Material.getMaterial(config.getString("block.material"));
		if(!material.isBlock())  {
			return null;
		}
		String itemName = config.getString("block.name");
		List<String> lore = config.getStringList("block.lore");
		String shortName = config.getString("shortName");
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
		boolean onlyDirectDestroy = config.getBoolean("onlyDirectDestroy");
		boolean blockReinforcements = config.getBoolean("blockReinforcements");
		boolean blockPearls = config.getBoolean("pearls.block");
		boolean blockMidair = config.getBoolean("pearls.blockMidair");
		boolean allowPearlingOut = config.getBoolean("pearls.allowPearlingOut");
		int scaleFactor = config.getInt("pearls.scaleFactor");
		boolean requireMaturity = config.getBoolean("pearls.requireMaturity");
		boolean consumeOnBlock = config.getBoolean("pearls.consumeOnBlock");
		boolean damageFirstBastion = config.getBoolean("pearls.damageFirstBastion");
		int blocksToErode = config.getInt("blocksToErode");
		boolean blockElytra = config.getBoolean("elytra.block");
		boolean destroyElytra = config.getBoolean("elytra.destroyOnBlock");
		boolean damageElytra = config.getBoolean("elytra.damageOnBlock");
		int elytraScale = config.getInt("elytra.scaleFactor");
		boolean elytraRequireMature = config.getBoolean("elytra.requireMaturity");
		int regenTime = config.getInt("regenPerDay");
		int proximityDamageRange = config.getInt("proximityDamageRange", 0); 
		double proximityDamageFactor = config.getDouble("proximityDamageFactor", 0.5); 
		if(regenTime > 0) {
			regenTime = 1728000 / regenTime;
		} else if(regenTime < 0) {
			regenTime = 0;
		}
		boolean explodeOnBlock = config.getBoolean("elytra.explodeOnBlock");
		double explodeOnBlockStrength = config.getDouble("elytra.explodeOnBlockStrength");
		boolean destroyOnRemoveWhileImmature = config.getBoolean("destroyOnRemoveWhileImmature", true);
		String overlayName = config.getString("overlay_name");
		return new BastionType(name, itemName, material, lore, shortName, square, effectRadius, includeY, startScaleFactor, finalScaleFactor, warmupTime,
				erosionTime, placementCooldown, destroyOnRemove, blockPearls, blockMidair, scaleFactor, requireMaturity, consumeOnBlock, 
				blocksToErode, blockElytra, destroyElytra, damageElytra, elytraScale, elytraRequireMature, explodeOnBlock, 
				explodeOnBlockStrength, damageFirstBastion, regenTime, onlyDirectDestroy, allowPearlingOut, blockReinforcements, destroyOnRemoveWhileImmature,
				proximityDamageRange, proximityDamageFactor, overlayName);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.name);
		sb.append(": ").append(material)
			.append(" name:").append(itemName)
			.append(" lore[").append(lore != null ? lore.size() : 0).append("]: ").append(lore)
			.append(" scale[").append(this.startScaleFactor).append("->").append(this.finalScaleFactor)
			.append(" ").append(this.effectRadius).append(this.square ? "cb": "r")
			.append(" wm").append(this.warmupTime).append(" cd").append(this.placementCooldown)
			.append(" ed").append(this.erosionTime).append(" rd").append(this.regenTime)
			.append(" iY").append(this.includeY).append(" bte").append(this.blocksToErode)
			.append(" doR").append(this.destroyOnRemove).append(" oDD").append(this.onlyDirectDestruction)
			.append(" pearls[").append(this.blockPearls);
		if (this.blockPearls) {
			sb.append(": mid").append(this.blockMidair).append(" rM").append(this.pearlRequireMature)
				.append(" sc").append(this.pearlScale).append(" cob").append(this.consumeOnBlock)
				.append(" dfB").append(this.damageFirstBastion).append(" apo").append(allowPearlingOut);
		}
		sb.append("] elytra[").append(this.blockElytra);
		if (this.blockElytra) {
			sb.append(": rM").append(this.elytraRequireMature).append(" sc").append(this.elytraScale)
				.append(" dob").append(this.damageElytra).append(" Dob").append(this.destroyElytra)
				.append(" eob").append(this.explodeOnBlock).append(" eos").append(this.explodeOnBlockStrength);
		}
		sb.append("]");
		return sb.toString();
	}
}
