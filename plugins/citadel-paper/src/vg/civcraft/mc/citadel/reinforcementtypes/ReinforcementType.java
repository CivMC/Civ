package vg.civcraft.mc.citadel.reinforcementtypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;

public class ReinforcementType {

	private int amount;
	private double percentReturn;
	private int returnValue;
	private int hitpoints;
	private Material mat;
	private int maturationTime;
	private int acidTime;
	private int scale;
	private int gracePeriod;
	private ItemStack stack;
	private ReinforcementEffect effect;
	private Set <Material> allowedReinforceables;
	private Set <Material> disallowedReinforceables;
	
	private static Map<ItemStack, ReinforcementType> types = 
			new HashMap<ItemStack, ReinforcementType>();
	
	public ReinforcementType(Material mat, int amount, double percentReturn,
			int returnValue, int hitpoints, int maturationTime, int acidTime,
			int scale, List<String> lore, ReinforcementEffect effect, Set <Material> allowsReinforceables, 
			Set <Material> disallowedReinforceables, int gracePeriod) {
		this.mat = mat;
		this.amount = amount;
		this.percentReturn = percentReturn/100;
		this.returnValue = returnValue;
		this.hitpoints = hitpoints;
		this.maturationTime = maturationTime;
		this.acidTime = acidTime;
		this.scale = scale;
		this.effect = effect;
		ItemStack stack = new ItemStack(mat, amount);
		ItemMeta meta = stack.getItemMeta();
		meta.setLore(lore);
		stack.setItemMeta(meta);
		this.stack = stack;
		types.put(stack, this);
		this.allowedReinforceables = allowsReinforceables;
		this.disallowedReinforceables = disallowedReinforceables;
		this.gracePeriod = gracePeriod;
	}
	
	public static void initializeReinforcementTypes(){
    	List<String> types = CitadelConfigManager.getReinforcementTypes();
		for (String type: types){
			Material mat = CitadelConfigManager.getMaterial(type);
			int amount = CitadelConfigManager.getRequireMents(type);
			int percentReturn = CitadelConfigManager.getPercentReturn(type);
			int returnValue = CitadelConfigManager.getReturns(type);
			int hitpoints = CitadelConfigManager.getHitPoints(type);
			int maturation = CitadelConfigManager.getMaturationTime(type);
			int acid = CitadelConfigManager.getAcidTime(type);
			int maturation_scale = CitadelConfigManager.getMaturationScale(type);
			List<String> lore = CitadelConfigManager.getLoreValues(type);
			ReinforcementEffect effect = CitadelConfigManager.getReinforcementEffect(type);
			List <String> reinforceableMatString = CitadelConfigManager.getReinforceableMaterials(type);
			Set <Material> reinforceableMats = CitadelConfigManager.parseMaterialList(reinforceableMatString);
			List <String> unreinforceableMatString = CitadelConfigManager.getNonReinforceableMaterials(type);
			Set <Material> nonReinforceableMats = CitadelConfigManager.parseMaterialList(unreinforceableMatString);
			int gracePeriod = CitadelConfigManager.getGracePeriod(type);
			new ReinforcementType(mat, amount, percentReturn, returnValue,
					hitpoints, maturation, acid, maturation_scale, lore, effect, reinforceableMats, nonReinforceableMats, gracePeriod);
			if (CitadelConfigManager.shouldLogInternal()) {
				Citadel.getInstance().getLogger().log(Level.INFO,
						"Adding Reinforcement {0} with:\n  material {1} \n  amount {2} "
								+ "\n  return rate {3} \n  return? {4} \n  health {5} \n  maturation {6} "
								+ "\n  acid {7} \n  scaling {8} \n  lore: {9} \n  effect: \n {10} \n grace: {11}",
						new Object[] { type, mat.toString(), amount, percentReturn, returnValue, hitpoints, maturation,
								acid, maturation_scale, (lore != null ? String.join("   ", lore) : ""), (effect != null ? effect : ""),
								gracePeriod});
			}
		}
    }
	
	/**
	 * @return Returns the Material associated with this ReinforcementType.
	 */
	public Material getMaterial() {
		return mat;
	}
	/**
	 * @return Returns the required amount of items for this ReinforcementType.
	 */
	public int getRequiredAmount() {
		return amount;
	}
	/**
	 * @return The percent chance that a block will return the reinforcements. Scales with damage.  1 means it is 100% and .5 means 50%
	 */
	public double getPercentReturn() {
		return percentReturn;
	}
	/**
	 * @return The amount of materials to return on breakage.
	 */
	public int getReturnValue() {
		return returnValue;
	}
	/**
	 * @return Returns the durability of a ReinforcementType.
	 */
	public int getHitPoints() {
		return hitpoints;
	}
	/**
	 * @return Returns the Maturation time needed until this block is mature.
	 */
	public int getMaturationTime(){
		return maturationTime;
	}
	/**
	 * @return Returns the Acid time needed until this acid block is ready.
	 */
	public int getAcidTime() {
		return acidTime;
	}
	/**
	 * @return Get the scale of amount of damage a block should take when it is not fully mature.
	 */
	public int getMaturationScale(){
		return scale;
	}
	/**
	 * @return Get the effect to spawn around this type of reinforcement when it is created or damaged.
	 */
	public ReinforcementEffect getReinforcementEffect(){
		return effect;
	}
	/**
	 * Returns the ReinforcementType for a given ItemStack.
	 * @param The ItemStack that a player may be holding.
	 * @return Returns null if no ReinforcementType is found.
	 * @return Returns the ReinforcementType if found.
	 */
	public static ReinforcementType getReinforcementType(ItemStack stack){
		for (ItemStack storedStack: types.keySet())
			if (storedStack.isSimilar(stack)) {
				return types.get(storedStack);
			}
		return null;
	}
	/**
	 * @return Returns the ItemStack of a ReinforcementType.
	 */
	public ItemStack getItemStack(){
		return stack;
	}
	
	public boolean canBeReinforced(Material mat) {
		if (allowedReinforceables == null) {
			if (disallowedReinforceables == null || !disallowedReinforceables.contains(mat)) {
				return true;
			}
			else {
				return false;
			}
		}
		return allowedReinforceables.contains(mat);
	}
	
	public static List<ReinforcementType> getReinforcementTypes(){
		List<ReinforcementType> type = new ArrayList<ReinforcementType>();
		type.addAll(types.values());
		return type;
	}
	
	/**
	 * @return the time in minutes to "forgive" reinforcements and apply 100% return rate. Set to 0 to disable.
	 */
	public int getGracePeriod() {
		return this.gracePeriod;
	}
}
