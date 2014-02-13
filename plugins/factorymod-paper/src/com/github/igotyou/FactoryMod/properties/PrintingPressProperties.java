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
	private ItemList<NamedItemStack> plateMaterials;
	private ItemList<NamedItemStack> bindingMaterials;
	private ItemList<NamedItemStack> pageMaterials;
	private int pagesPerLot;
	private ItemList<NamedItemStack> pamphletMaterials;
	private int pamphletsPerLot;
	private ItemList<NamedItemStack> securityMaterials;
	private int securityNotesPerLot;
	private int energyTime;
	private String name;
	private int maxRepair;
	private ItemList<NamedItemStack> repairMaterials;
	private int pageLead;
	private int setPlateTime;
	private int repairTime;
	private int bookPagesCap;


	public int getPageLead() {
		return pageLead;
	}


	public PrintingPressProperties(
			ItemList<NamedItemStack> fuel,
			ItemList<NamedItemStack> constructionMaterials,
			ItemList<NamedItemStack> repairMaterials,
			ItemList<NamedItemStack> plateMaterials,
			ItemList<NamedItemStack> bindingMaterials,
			ItemList<NamedItemStack> pageMaterials,
			int pagesPerLot,
			ItemList<NamedItemStack> pamphletMaterials,
			int pamphletsPerLot,
			ItemList<NamedItemStack> securityMaterials,
			int securityNotesPerLot,
			int energyTime, String name, int repair, int paperRate,
			int pageLead, int setPlateTime, int repairTime, int bookPagesCap
			)
	{
		this.fuel = fuel;
		this.energyTime = energyTime;
		this.name = name;
		this.maxRepair=repair;
		this.constructionMaterials = constructionMaterials;
		this.repairMaterials = repairMaterials;
		this.plateMaterials = plateMaterials;
		this.bindingMaterials = bindingMaterials;
		this.pageMaterials = pageMaterials;
		this.pagesPerLot = pagesPerLot;
		this.pamphletMaterials = pamphletMaterials;
		this.pamphletsPerLot = pamphletsPerLot;
		this.securityMaterials = securityMaterials;
		this.securityNotesPerLot = securityNotesPerLot;
		this.pageLead = pageLead;
		this.setPlateTime = setPlateTime;
		this.repairTime = repairTime;
		this.bookPagesCap = bookPagesCap;
	}

	
	public int getSetPlateTime() {
		return setPlateTime;
	}


	public int getRepairTime() {
		return repairTime;
	}


	public ItemList<NamedItemStack> getBindingMaterials() {
		return bindingMaterials;
	}


	public ItemList<NamedItemStack> getPageMaterials() {
		return pageMaterials;
	}

	public ItemList<NamedItemStack> getSecurityMaterials() {
		return securityMaterials;
	}

	public ItemList<NamedItemStack> getRepairMaterials() {
		return repairMaterials;
	}
	
	public ItemList<NamedItemStack> getPlateMaterials() {
		return plateMaterials;
	}


	public static PrintingPressProperties fromConfig(FactoryModPlugin plugin, ConfigurationSection configPrintingPresses) {
		ItemList<NamedItemStack> ppFuel=plugin.getItems(configPrintingPresses.getConfigurationSection("fuel"));
		if(ppFuel.isEmpty())
		{
			ppFuel=new ItemList<NamedItemStack>();
			ppFuel.add(new NamedItemStack(Material.getMaterial("COAL"),1,(short)1,"Charcoal"));
		}
		ConfigurationSection costs = configPrintingPresses.getConfigurationSection("costs");
		ItemList<NamedItemStack> ppConstructionCost=plugin.getItems(costs.getConfigurationSection("construction"));
		ItemList<NamedItemStack> ppRepairCost=plugin.getItems(costs.getConfigurationSection("repair"));
		ItemList<NamedItemStack> ppPlateCost=plugin.getItems(costs.getConfigurationSection("plates"));
		ItemList<NamedItemStack> ppBindingCost=plugin.getItems(costs.getConfigurationSection("binding"));
		ItemList<NamedItemStack> ppPageCost=plugin.getItems(costs.getConfigurationSection("page_lot"));
		int pagesPerLot = costs.getInt("pages_per_lot",16); 
		ItemList<NamedItemStack> ppPamphletCost=plugin.getItems(costs.getConfigurationSection("pamphlet_lot"));
		int pamphletsPerLot = costs.getInt("pamphlets_per_lot",24);
		ItemList<NamedItemStack> ppSecurityCost=plugin.getItems(costs.getConfigurationSection("security_lot"));
		int securityNotesPerLot = costs.getInt("security_notes_per_lot",24);
		int ppEnergyTime = configPrintingPresses.getInt("fuel_time", 10);
		int ppRepair = costs.getInt("repair_multiple",1);
		String ppName = configPrintingPresses.getString("name", "Printing Press");
		int paperRate = configPrintingPresses.getInt("paper_rate",3);
		int pageLead = configPrintingPresses.getInt("page_lead",12);
		int setPageTime = configPrintingPresses.getInt("set_page_time",20);
		int repairTime = configPrintingPresses.getInt("repair_time",12);
		int bookPagesCap = configPrintingPresses.getInt("book_pages_cap",16);
		return new PrintingPressProperties(ppFuel, ppConstructionCost, ppRepairCost, ppPlateCost, ppBindingCost, ppPageCost, pagesPerLot, ppPamphletCost, pamphletsPerLot, ppSecurityCost, securityNotesPerLot, ppEnergyTime, ppName, ppRepair, paperRate, pageLead, setPageTime, repairTime, bookPagesCap);
	}


	public int getMaxRepair()
	{
		return maxRepair;
	}
	
	public int getPagesPerLot() {
		return pagesPerLot;
	}


	public ItemList<NamedItemStack> getPamphletMaterials() {
		return pamphletMaterials;
	}


	public int getPamphletsPerLot() {
		return pamphletsPerLot;
	}


	public int getSecurityNotesPerLot() {
		return securityNotesPerLot;
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


	public int getBookPagesCap() {
		return bookPagesCap;
	}
}
