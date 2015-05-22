package com.github.igotyou.FactoryMod.properties;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class RepairFactoryProperties {

	private ItemList<NamedItemStack> constructionMaterials;
	private ItemList<NamedItemStack> fuel;
	private ItemList<NamedItemStack> repairMaterials;
	private ItemList<NamedItemStack> recipeMaterials;
	private int energyTime;
	private String name;
	private int repair;
	private double repairTime;
	private double productionTime;
	
	public RepairFactoryProperties(ItemList<NamedItemStack> constructionMaterials, ItemList<NamedItemStack> fuel,
			ItemList<NamedItemStack> repairMaterials, int energyTime, String name,int repair, double repairTime,
			double productionTime, ItemList<NamedItemStack> recipeMaterials){
		this.constructionMaterials = constructionMaterials;
		this.fuel = fuel;
		this.repairMaterials = repairMaterials;
		this.energyTime = energyTime;
		this.name = name;
		this.repair = repair;
		this.repairTime = repairTime;
		this.productionTime = productionTime;
		this.recipeMaterials = recipeMaterials;
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
		int rfEnergyTime = section.getInt("fuel_time");
		int rfRepair = costs.getInt("repair_multiple", 1);
		String rfName = section.getString("name", "Repair Factory");
		int repairTime = section.getInt("repair_time", 12);
		int productionTime = section.getInt("production_time");
		return new RepairFactoryProperties(rfConstructionCost, rfFuel, rfRepairCost, rfEnergyTime, rfName, rfRepair, repairTime, productionTime, rfRecipeUse);
	}
}
