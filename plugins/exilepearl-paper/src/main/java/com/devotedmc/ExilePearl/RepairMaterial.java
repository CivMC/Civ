package com.devotedmc.ExilePearl;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.devotedmc.ExilePearl.config.Document;

import vg.civcraft.mc.civmodcore.util.Guard;

public class RepairMaterial {

	final String name;
	final ItemStack stack;
	final int repairAmount;
	
	/**
	 * Creates a new RepairMaterial instance
	 * @param name The repair name
	 * @param stack The repair item stack
	 * @param repairAmount The repair amount
	 */
	public RepairMaterial(final String name, final ItemStack stack, final int repairAmount) {
		Guard.ArgumentNotNullOrEmpty(name, "name");
		Guard.ArgumentNotNull(stack, "stack");
		
		this.name = name;
		this.stack = stack;
		this.repairAmount = repairAmount;
	}
	
	/**
	 * Gets the repair name
	 * @return The repair name
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Gets the repair item stack
	 * @return The repair item stack
	 */
	public ItemStack getStack() {
		return stack;
	}
	
	/**
	 * Gets the repair amount
	 * @return The repair amount
	 */
	public int getRepairAmount() {
		return repairAmount;
	}
	
	/**
	 * Creates a new RepairMaterial from a config document
	 * @param name The repair material name
	 * @param doc The configuration document
	 * @return The repair material instance
	 */
	public static RepairMaterial fromDocument(String name, Document doc) {
		Guard.ArgumentNotNull(doc, "doc");
		
		try {
			Material m = Material.getMaterial(doc.getString("material"));
			int repairAmount = doc.getInteger("repair", 1);
			ItemStack is = new ItemStack(m, 1);
			
			List<String> lore = doc.getStringList("lore");
			String itemName = doc.getString("name");
			if (itemName != null || lore.size() > 0) {
				ItemMeta im = is.getItemMeta();
				
				if (itemName != null) {
					im.setDisplayName(itemName);
				}
				
				if (lore.size() > 0) {
					im.setLore(lore);
				}
			}
			
			RepairMaterial repair = new RepairMaterial(name, is, repairAmount);
			return repair;
		} catch(Exception ex) {
			return null;
		}
	}
}
