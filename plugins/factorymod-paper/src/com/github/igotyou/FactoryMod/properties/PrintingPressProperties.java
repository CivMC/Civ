package com.github.igotyou.FactoryMod.properties;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;

public class PrintingPressProperties extends AFactoryProperties{

	private ItemList<AdvancedItemStack> fuel;
	private ItemList<AdvancedItemStack> constructionMaterials;
	private ItemList<AdvancedItemStack> plateMaterials;
	private ItemList<AdvancedItemStack> bindingMaterials;
	private ItemList<AdvancedItemStack> pageMaterials;
	private int pagesPerLot;
	private ItemList<AdvancedItemStack> pamphletMaterials;
	private int pamphletsPerLot;
	private ItemList<AdvancedItemStack> securityMaterials;
	private int securityNotesPerLot;
	private int energyTime;
	private int maxRepair;
	private ItemList<AdvancedItemStack> repairMaterials;
	private int pageLead;
	private int setPlateTime;
	private int repairTime;
	private int bookPagesCap;


	public int getPageLead() {
		return pageLead;
	}


	public PrintingPressProperties(
			ItemList<AdvancedItemStack> fuel,
			ItemList<AdvancedItemStack> constructionMaterials,
			ItemList<AdvancedItemStack> repairMaterials,
			ItemList<AdvancedItemStack> plateMaterials,
			ItemList<AdvancedItemStack> bindingMaterials,
			ItemList<AdvancedItemStack> pageMaterials,
			int pagesPerLot,
			ItemList<AdvancedItemStack> pamphletMaterials,
			int pamphletsPerLot,
			ItemList<AdvancedItemStack> securityMaterials,
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


	public ItemList<AdvancedItemStack> getBindingMaterials() {
		return bindingMaterials;
	}


	public ItemList<AdvancedItemStack> getPageMaterials() {
		return pageMaterials;
	}

	public ItemList<AdvancedItemStack> getSecurityMaterials() {
		return securityMaterials;
	}

	public ItemList<AdvancedItemStack> getRepairMaterials() {
		return repairMaterials;
	}
	
	public ItemList<AdvancedItemStack> getPlateMaterials() {
		return plateMaterials;
	}


	public static PrintingPressProperties fromConfig(FactoryModPlugin plugin, ConfigurationSection configPrintingPresses) {
		ItemList<AdvancedItemStack> ppFuel=plugin.getItems(configPrintingPresses.getConfigurationSection("fuel"));
		if(ppFuel.isEmpty())
		{
			ppFuel=new ItemList<AdvancedItemStack>();
			ppFuel.add(new AdvancedItemStack(Material.getMaterial("COAL"),1,(short)1,"Charcoal"));
		}
		ConfigurationSection costs = configPrintingPresses.getConfigurationSection("costs");
		ItemList<AdvancedItemStack> ppConstructionCost=plugin.getItems(costs.getConfigurationSection("construction"));
		ItemList<AdvancedItemStack> ppRepairCost=plugin.getItems(costs.getConfigurationSection("repair"));
		ItemList<AdvancedItemStack> ppPlateCost=plugin.getItems(costs.getConfigurationSection("plates"));
		ItemList<AdvancedItemStack> ppBindingCost=plugin.getItems(costs.getConfigurationSection("binding"));
		ItemList<AdvancedItemStack> ppPageCost=plugin.getItems(costs.getConfigurationSection("page_lot"));
		int pagesPerLot = costs.getInt("pages_per_lot",16); 
		ItemList<AdvancedItemStack> ppPamphletCost=plugin.getItems(costs.getConfigurationSection("pamphlet_lot"));
		int pamphletsPerLot = costs.getInt("pamphlets_per_lot",24);
		ItemList<AdvancedItemStack> ppSecurityCost=plugin.getItems(costs.getConfigurationSection("security_lot"));
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


	public ItemList<AdvancedItemStack> getPamphletMaterials() {
		return pamphletMaterials;
	}


	public int getPamphletsPerLot() {
		return pamphletsPerLot;
	}


	public int getSecurityNotesPerLot() {
		return securityNotesPerLot;
	}


	public ItemList<AdvancedItemStack> getFuel()
	{
		return fuel;
	}
	
	public int getEnergyTime()
	{
		return energyTime;
	}

	public ItemList<AdvancedItemStack> getConstructionMaterials() {
		return constructionMaterials;
	}


	public int getBookPagesCap() {
		return bookPagesCap;
	}
}
