package vg.civcraft.mc.citadel.reinforcementtypes;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ReinforcementType {

	private float health;
	private double returnChance;
	private ItemStack item;
	private long maturationTime;
	private int acidPriority;
	private long acidTime;
	private double scale;
	private long gracePeriod;
	private ReinforcementEffect creationEffect;
	private ReinforcementEffect damageEffect;
	private ReinforcementEffect destructionEffect;
	private Set<Material> allowedReinforceables;
	private Set<Material> disallowedReinforceables;
	private Set<Material> globalBlackList;
	private short id;
	private String name;
	private long decayTimer;
	private double decayMultiplier;
	private double deletedGroupMulitplier;
	private int legacyId;

	public ReinforcementType(float health, double returnChance, ItemStack item, long maturationTime, long acidTime, int acidPriority,
			double scale, long gracePeriod, ReinforcementEffect creationEffect, ReinforcementEffect damageEffect,
			ReinforcementEffect destructionEffect, Collection<Material> allowsReinforceables,
			Collection<Material> disallowedReinforceables, short id, String name, Collection<Material> globalBlackList,
			long decayTimer, double decayMultiplier, double deletedGroupMulitplier, int legacyId) {
		this.health = health;
		this.name = name;
		this.returnChance = returnChance;
		this.item = item;
		this.maturationTime = maturationTime;
		this.acidTime = acidTime;
		this.acidPriority = acidPriority;
		this.scale = scale;
		this.creationEffect = creationEffect;
		this.damageEffect = damageEffect;
		this.destructionEffect = destructionEffect;
		this.gracePeriod = gracePeriod;
		this.deletedGroupMulitplier = deletedGroupMulitplier;
		if (allowsReinforceables != null) {
			this.allowedReinforceables = new TreeSet<>(allowsReinforceables);
		} else {
			// can only black list OR white list
			if (disallowedReinforceables != null) {
				this.disallowedReinforceables = new TreeSet<>(disallowedReinforceables);
			}
		}
		this.globalBlackList = new TreeSet<>();
		if (globalBlackList != null) {
			this.globalBlackList.addAll(globalBlackList);
		}
		this.id = id;
		this.decayMultiplier = decayMultiplier;
		this.decayTimer = decayTimer;
		this.legacyId = legacyId;
	}

	public boolean canBeReinforced(Material mat) {
		if (globalBlackList.contains(mat)) {
			return false;
		}
		if (allowedReinforceables == null) {
			return disallowedReinforceables == null || !disallowedReinforceables.contains(mat);
		}
		return allowedReinforceables.contains(mat);
	}

	/**
	 * @return Returns the acid maturation time needed until this acid block is
	 *         ready.
	 */
	public long getAcidTime() {
		return acidTime;
	}

	/**
	 * @return the time in milli seconds to "forgive" reinforcements and apply 100%
	 *         return rate. Set to 0 to disable.
	 */
	public long getGracePeriod() {
		return this.gracePeriod;
	}

	/**
	 * @return Maximum health
	 */
	public float getHealth() {
		return health;
	}

	/**
	 * @return The unique id identifying this config
	 */
	public short getID() {
		return id;
	}
	
	/**
	 * @return Damage multiplier applied if the owning group was deleted
	 */
	public double getDeletedGroupMultiplier() {
		return deletedGroupMulitplier;
	}

	/**
	 * @return Item used to create instance of this item
	 */
	public ItemStack getItem() {
		return item;
	}
	
	/**
	 * @return Acid priority of this type. Acid blocks can only remove blocks of lower or equal priority
	 */
	public int getAcidPriority() {
		return acidPriority;
	}

	/**
	 * @return Get the scale of amount of damage a block should take when it is not
	 *         fully mature.
	 */
	public double getMaturationScale() {
		return scale;
	}

	/**
	 * @return Returns the Maturation time needed until this block is mature in
	 *         milliseconds
	 */
	public long getMaturationTime() {
		return maturationTime;
	}

	/**
	 * @return Nice name to use for messages
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Get the effect to spawn around this type of reinforcement when it is
	 *         created
	 */
	public ReinforcementEffect getCreationEffect() {
		return creationEffect;
	}

	/**
	 * @return Get the effect to spawn around this type of reinforcement when it is
	 *         damaged
	 */
	public ReinforcementEffect getDamageEffect() {
		return damageEffect;
	}

	/**
	 * @return Get the effect to spawn around this type of reinforcement when it is
	 *         destroyed
	 */
	public ReinforcementEffect getDestructionEffect() {
		return destructionEffect;
	}

	/**
	 * @return The percent chance that a block will return the reinforcements.
	 *         Scales with damage. 1 means it is 100% and .5 means 50%
	 */
	public double getReturnChance() {
		return returnChance;
	}
	
	/**
	 * Material id the material used for the reinforcement had pre-flattening (1.12.2 or earlier). Needed once for 
	 * proper migration of reinforcements to higher versions
	 * @return Old item id of the reinforcement item
	 */
	public int getLegacyId() {
		return legacyId;
	}
	
	public double getDecayDamageMultipler(long since) {
		if (decayTimer <= 0 || decayMultiplier == 1) {
			return 1;
		}
		long timePassed = System.currentTimeMillis() - since;
		if (timePassed <= decayTimer) {
			return 1;
		}
		double timeExponent = ((double) timePassed / (double) decayTimer);
		return Math.pow(decayMultiplier, timeExponent);
	}
}
