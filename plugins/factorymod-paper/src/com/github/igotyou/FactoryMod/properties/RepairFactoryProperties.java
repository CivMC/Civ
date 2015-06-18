package com.github.igotyou.FactoryMod.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class RepairFactoryProperties implements IFactoryProperties{

	private ItemList<NamedItemStack> constructionMaterials;
	private ItemList<NamedItemStack> fuel;
	private ItemList<NamedItemStack> repairMaterials;
	private ItemList<NamedItemStack> recipeMaterials;
	private List<Material> allowedRepairable;
	private int energyTime;
	private String name;
	private int repair;
	private double repairTime;
	private double productionTime;
	
	public RepairFactoryProperties(ItemList<NamedItemStack> constructionMaterials, ItemList<NamedItemStack> fuel,
			ItemList<NamedItemStack> repairMaterials, int energyTime, String name,int repair, double repairTime,
			double productionTime, ItemList<NamedItemStack> recipeMaterials, List<Material> allowedRepairable){
		this.constructionMaterials = constructionMaterials;
		this.fuel = fuel;
		this.repairMaterials = repairMaterials;
		this.energyTime = energyTime;
		this.name = name;
		this.repair = repair;
		this.repairTime = repairTime;
		this.productionTime = productionTime;
		this.recipeMaterials = recipeMaterials;
		this.allowedRepairable = allowedRepairable;
	}
	
	public int getRepair() {
		return repair;
	}
	
	public ItemList<NamedItemStack> getConstructionMaterials(){
		return constructionMaterials;
	}
	
	public ItemList<NamedItemStack> getFuel(){
		return fuel;
	}
	
	public ItemList<NamedItemStack> getRepairMaterials(){
		return repairMaterials;
	}
	
	public ItemList<NamedItemStack> getRecipeMaterials(){
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
	
	public static RepairFactoryProperties fromConfig(FactoryModPlugin plugin, ConfigurationSection section){
		ItemList<NamedItemStack> rfFuel = plugin.getItems(section.getConfigurationSection("fuel"));
		if (rfFuel.isEmpty()){
			rfFuel = new ItemList<NamedItemStack>();
			rfFuel.add(new NamedItemStack(Material.getMaterial("COAL"), 1, (short) 1, "Charcoal"));
		}
		ConfigurationSection costs = section.getConfigurationSection("costs");
		ItemList<NamedItemStack> rfConstructionCost = plugin.getItems(costs.getConfigurationSection("construction"));
		ItemList<NamedItemStack> rfRepairCost = plugin.getItems(costs.getConfigurationSection("repair"));
		ItemList<NamedItemStack> rfRecipeUse = plugin.getItems(costs.getConfigurationSection("recipe"));
		ItemList<NamedItemStack> rfAllowed = plugin.getItems(costs.getConfigurationSection("repairable"));
		int rfEnergyTime = section.getInt("fuel_time");
		int rfRepair = costs.getInt("repair_multiple", 1);
		String rfName = section.getString("name", "Repair Factory");
		int repairTime = section.getInt("repair_time", 12);
		int productionTime = section.getInt("production_time");
		
		// We only care about raw material types for repair purposes.
		Iterator<NamedItemStack> canRepair = rfAllowed.iterator();
		List<Material> repairable = new ArrayList<Material>();
		
		while (canRepair.hasNext()) {
			NamedItemStack repair = canRepair.next();
			
			repairable.add(repair.getType());
		}
		return new RepairFactoryProperties(rfConstructionCost, rfFuel, rfRepairCost, rfEnergyTime, rfName, rfRepair, repairTime, productionTime, rfRecipeUse, repairable);
	}
}
