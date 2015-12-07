package com.github.igotyou.FactoryMod.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;

public class RepairFactoryProperties implements IFactoryProperties{

	private ItemList<AdvancedItemStack> constructionMaterials;
	private ItemList<AdvancedItemStack> fuel;
	private ItemList<AdvancedItemStack> repairMaterials;
	private ItemList<AdvancedItemStack> recipeMaterials;
	private List<Material> allowedRepairable;
	private int energyTime;
	private String name;
	private int repair;
	private double repairTime;
	private double productionTime;
	private int resetLevel;
	
	public RepairFactoryProperties(ItemList<AdvancedItemStack> constructionMaterials, ItemList<AdvancedItemStack> fuel,
			ItemList<AdvancedItemStack> repairMaterials, int energyTime, String name,int repair, double repairTime,
			double productionTime, int resetLevel, ItemList<AdvancedItemStack> recipeMaterials, List<Material> allowedRepairable){
		this.constructionMaterials = constructionMaterials;
		this.fuel = fuel;
		this.repairMaterials = repairMaterials;
		this.energyTime = energyTime;
		this.name = name;
		this.repair = repair;
		this.repairTime = repairTime;
		this.productionTime = productionTime;
		this.resetLevel = resetLevel;
		this.recipeMaterials = recipeMaterials;
		this.allowedRepairable = allowedRepairable;
	}
	
	public int getRepair() {
		return repair;
	}
	
	public ItemList<AdvancedItemStack> getConstructionMaterials(){
		return constructionMaterials;
	}
	
	public ItemList<AdvancedItemStack> getFuel(){
		return fuel;
	}
	
	public ItemList<AdvancedItemStack> getRepairMaterials(){
		return repairMaterials;
	}
	
	public ItemList<AdvancedItemStack> getRecipeMaterials(){
		return recipeMaterials;
	}
	
	public List<Material> getAllowedRepairable(){
		return allowedRepairable;
	}
	
	public int getEnergyTime()
	{
		return energyTime;
	}
	
	public String getName()
	{
		return name;
	}

	public double getRepairTime() 
	{
		return repairTime;
	}
	
	public double getProductionTime(){
		return productionTime;
	}
	
	public int getResetLevel() {
		return resetLevel;
	}
	
	public static RepairFactoryProperties fromConfig(FactoryModPlugin plugin, ConfigurationSection section){
		ItemList<AdvancedItemStack> rfFuel = plugin.getItems(section.getConfigurationSection("fuel"));
		if (rfFuel.isEmpty()){
			rfFuel = new ItemList<AdvancedItemStack>();
			rfFuel.add(new AdvancedItemStack(Material.getMaterial("COAL"), 1, (short) 1, "Charcoal"));
		}
		ConfigurationSection costs = section.getConfigurationSection("costs");
		ItemList<AdvancedItemStack> rfConstructionCost = plugin.getItems(costs.getConfigurationSection("construction"));
		ItemList<AdvancedItemStack> rfRepairCost = plugin.getItems(costs.getConfigurationSection("repair"));
		ItemList<AdvancedItemStack> rfRecipeUse = plugin.getItems(costs.getConfigurationSection("recipe"));
		ItemList<AdvancedItemStack> rfAllowed = plugin.getItems(section.getConfigurationSection("repairable"));
		int rfEnergyTime = section.getInt("fuel_time");
		int rfRepair = costs.getInt("repair_multiple", 1);
		String rfName = section.getString("name", "Reset Factory");
		int repairTime = section.getInt("repair_time", 12);
		int productionTime = section.getInt("production_time");
		int resetLevel = section.getInt("reset_level", 1);
		
		// We only care about raw material types for repair purposes.
		Iterator<AdvancedItemStack> canRepair = rfAllowed.iterator();
		List<Material> repairable = new ArrayList<Material>();
		
		while (canRepair.hasNext()) {
			AdvancedItemStack repair = canRepair.next();
			
			repairable.add(repair.getType());
			
			FactoryModPlugin.sendConsoleMessage("Adding repairable: " + repair.getType());
		}
		return new RepairFactoryProperties(rfConstructionCost, rfFuel, rfRepairCost, rfEnergyTime, rfName,
				rfRepair, repairTime, productionTime, resetLevel, rfRecipeUse, repairable);
	}
}
