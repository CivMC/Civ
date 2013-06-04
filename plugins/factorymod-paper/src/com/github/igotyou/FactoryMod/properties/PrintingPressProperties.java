package com.github.igotyou.FactoryMod.properties;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class PrintingPressProperties {

	private ItemList<NamedItemStack> fuel;
	private ItemList<NamedItemStack> constructionMaterials;
	private ItemList<NamedItemStack> bindingMaterials;
	private ItemList<NamedItemStack> pageMaterials;
	private ItemList<NamedItemStack> pamphletMaterials;
	private ItemList<NamedItemStack> securityMaterials;
	private int energyTime;
	private String name;
	private int maxRepair;
	private ItemList<NamedItemStack> repairMaterials;
	
	public PrintingPressProperties(
			ItemList<NamedItemStack> fuel,
			ItemList<NamedItemStack> constructionMaterials,
			ItemList<NamedItemStack> repairMaterials,
			ItemList<NamedItemStack> bindingMaterials,
			ItemList<NamedItemStack> pageMaterials,
			ItemList<NamedItemStack> pamphletMaterials,
			ItemList<NamedItemStack> securityMaterials, 
			int energyTime, String name, int repair
			)
	{
		this.fuel = fuel;
		this.energyTime = energyTime;
		this.name = name;
		this.maxRepair=repair;
		this.constructionMaterials = constructionMaterials;
		this.repairMaterials = repairMaterials;
		this.bindingMaterials = bindingMaterials;
		this.pageMaterials = pageMaterials;
		this.pamphletMaterials = pamphletMaterials;
		this.securityMaterials = securityMaterials;
	}

	
	public ItemList<NamedItemStack> getBindingMaterials() {
		return bindingMaterials;
	}


	public ItemList<NamedItemStack> getPageMaterials() {
		return pageMaterials;
	}


	public ItemList<NamedItemStack> getPamphletMaterials() {
		return pamphletMaterials;
	}


	public ItemList<NamedItemStack> getSecurityMaterials() {
		return securityMaterials;
	}


	public ItemList<NamedItemStack> getRepairMaterials() {
		return repairMaterials;
	}


	public static PrintingPressProperties fromConfig(FactoryModPlugin plugin, ConfigurationSection configPrintingPresses) {
		ItemList<NamedItemStack> ppFuel=plugin.getItems(configPrintingPresses.getConfigurationSection("fuel"));
		if(ppFuel.isEmpty())
		{
			ppFuel=new ItemList<NamedItemStack>();
			ppFuel.add(new NamedItemStack(Material.getMaterial("COAL"),1,(short)1,"Charcoal"));
		}
		ConfigurationSection costs = configPrintingPresses.getConfigurationSection("costs");
		ItemList<NamedItemStack> ppConstructionCost=plugin.getItems(configPrintingPresses.getConfigurationSection("construction"));
		ItemList<NamedItemStack> ppRepairCost=plugin.getItems(configPrintingPresses.getConfigurationSection("repair"));
		ItemList<NamedItemStack> ppBindingCost=plugin.getItems(configPrintingPresses.getConfigurationSection("binding"));
		ItemList<NamedItemStack> ppPageCost=plugin.getItems(configPrintingPresses.getConfigurationSection("page"));
		ItemList<NamedItemStack> ppPamphletCost=plugin.getItems(configPrintingPresses.getConfigurationSection("pamphlet"));
		ItemList<NamedItemStack> ppSecurityCost=plugin.getItems(configPrintingPresses.getConfigurationSection("security"));
		int ppEnergyTime = configPrintingPresses.getInt("fuel_time", 2);
		int ppRepair = configPrintingPresses.getInt("max_repair",100);
		String ppName = configPrintingPresses.getString("name", "Printing Press");
		return new PrintingPressProperties(ppFuel, ppConstructionCost, ppRepairCost, ppBindingCost, ppPageCost, ppPamphletCost, ppSecurityCost, ppEnergyTime, ppName, ppRepair);
	}


	public int getMaxRepair()
	{
		return maxRepair;
	}
	
	public ItemList<NamedItemStack> getFuel()
	{
		return fuel;
	}
	
	public int getEnergyTime()
	{
		return energyTime;
	}
	
	public String getName()
	{
		return name;
	}

	public ItemList<NamedItemStack> getConstructionMaterials() {
		return constructionMaterials;
	}
}
