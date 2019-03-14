package vg.civcraft.mc.citadel.reinforcementtypes;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ReinforcementType {

	private double health;
	private double returnChance;
	private ItemStack item;
	private long maturationTime;
	private long acidTime;
	private double scale;
	private long gracePeriod;
	private ReinforcementEffect effect;
	private Set<Material> allowedReinforceables;
	private Set<Material> disallowedReinforceables;
	private Set<Material> globalBlackList;
	private int id;
	private String name;

	public ReinforcementType(double health, double returnChance, ItemStack item, long maturationTime, long acidTime,
			double scale, long gracePeriod, ReinforcementEffect effect, Collection<Material> allowsReinforceables,
			Collection<Material> disallowedReinforceables, int id, String name, Collection<Material> globalBlackList) {
		this.health = health;
		this.name = name;
		this.returnChance = returnChance;
		this.item = item;
		this.maturationTime = maturationTime;
		this.acidTime = acidTime;
		this.scale = scale;
		this.effect = effect;
		this.gracePeriod = gracePeriod;
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
	}

	public boolean canBeReinforced(Material mat) {
		if (globalBlackList.contains(mat)) {
			return false;
		}
		if (allowedReinforceables == null) {
			if (disallowedReinforceables == null || !disallowedReinforceables.contains(mat)) {
				return true;
			} else {
				return false;
			}
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
	public double getHealth() {
		return health;
	}

	/**
	 * @return The unique id identifying this config
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return Item used to create instance of this item
	 */
	public ItemStack getItem() {
		return item;
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
	 *         created or damaged.
	 */
	public ReinforcementEffect getReinforcementEffect() {
		return effect;
	}

	/**
	 * @return The percent chance that a block will return the reinforcements.
	 *         Scales with damage. 1 means it is 100% and .5 means 50%
	 */
	public double getReturnChance() {
		return returnChance;
	}
}
