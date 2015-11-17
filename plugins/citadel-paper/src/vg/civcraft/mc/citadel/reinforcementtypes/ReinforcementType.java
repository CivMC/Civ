package vg.civcraft.mc.citadel.reinforcementtypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
	private ItemStack stack;
	
	private static Map<ItemStack, ReinforcementType> types = 
			new HashMap<ItemStack, ReinforcementType>();
	
	public ReinforcementType(Material mat, int amount, double percentReturn,
			int returnValue, int hitpoints, int maturationTime, int acidTime,
			int scale, List<String> lore) {
		this.mat = mat;
		this.amount = amount;
		this.percentReturn = percentReturn/100;
		this.returnValue = returnValue;
		this.hitpoints = hitpoints;
		this.maturationTime = maturationTime;
		this.acidTime = acidTime;
		this.scale = scale;
		ItemStack stack = new ItemStack(mat, amount);
		ItemMeta meta = stack.getItemMeta();
		meta.setLore(lore);
		stack.setItemMeta(meta);
		this.stack = stack;
		types.put(stack, this);
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
			new ReinforcementType(mat, amount, percentReturn, returnValue,
					hitpoints, maturation, acid, maturation_scale, lore);
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
	 * Returns the ReinforcementType for a given ItemStack.
	 * @param The ItemStack that a player may be holding.
	 * @return Returns null if no ReinforcementType is found.
	 * @return Returns the ReinforcementType if found.
	 */
	public static ReinforcementType getReinforcementType(ItemStack stack){
		for (ItemStack storedStack: types.keySet())
			if (storedStack.isSimilar(stack))
				return types.get(storedStack);
		return null;
	}
	/**
	 * @return Returns the ItemStack of a ReinforcementType.
	 */
	public ItemStack getItemStack(){
		return stack;
	}
	
	public static List<ReinforcementType> getReinforcementTypes(){
		List<ReinforcementType> type = new ArrayList<ReinforcementType>();
		type.addAll(types.values());
		return type;
	}
}
