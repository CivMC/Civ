package com.github.igotyou.FactoryMod.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.AdvancedItemStack;

public class CompactorProperties extends AFactoryProperties{
    
    private ItemList<AdvancedItemStack> constructionMaterials;
    private ItemList<AdvancedItemStack> fuel;
    private ItemList<AdvancedItemStack> repairMaterials;
    private ItemList<AdvancedItemStack> recipeMaterials;
    private ItemList<AdvancedItemStack> specificExclusions;
    private List<Material> generalExlusions;
    private int energyTime;
    private int repair;
    private double repairTime;
    private double productionTime;
    private String compactLore;
    private boolean continuous;
    

    public CompactorProperties(ItemList<AdvancedItemStack> constructionMaterials,
            ItemList<AdvancedItemStack> fuel, ItemList<AdvancedItemStack> repairMaterials,
            ItemList<AdvancedItemStack> recipeMaterials, int energyTime, int repair, 
            String name, double repairTime, double productionTime, String compactLore,
            boolean continuous, ItemList<AdvancedItemStack> specificExclusion, List<Material> generalExclusion) {
        this.constructionMaterials = constructionMaterials;
        this.fuel = fuel;
        this.repairMaterials = repairMaterials;
        this.recipeMaterials = recipeMaterials;
        this.energyTime = energyTime;
        this.repair = repair;
        this.name = name;
        this.repairTime = repairTime;
        this.productionTime = productionTime;
        this.compactLore = compactLore;
        this.continuous = continuous;
        this.specificExclusions = specificExclusion;
        this.generalExlusions = generalExclusion;
    }

    public ItemList<AdvancedItemStack> getConstructionMaterials() {
        return constructionMaterials;
    }

    public ItemList<AdvancedItemStack> getFuel() {
        return fuel;
    }

    public ItemList<AdvancedItemStack> getRepairMaterials() {
        return repairMaterials;
    }

    public ItemList<AdvancedItemStack> getRecipeMaterials() {
        return recipeMaterials;
    }
    
    public ItemList<AdvancedItemStack> getSpecificExclusions() {
    	return specificExclusions;
    }
    
    public List<Material> getGeneralExclusions() {
    	return generalExlusions;
    }

    public int getEnergyTime() {
        return energyTime;
    }

    public int getRepair() {
        return repair;
    }
    
    public double getRepairTime() {
        return repairTime;
    }

    public double getProductionTime() {
        return productionTime;
    }

    public String getCompactLore() {
        return compactLore;
    }
    
    public boolean getContinuous() {
		return continuous;
    }
    
    public static CompactorProperties fromConfig(FactoryModPlugin plugin, ConfigurationSection config) {
        ItemList<AdvancedItemStack> cFuel = plugin.getItems(config.getConfigurationSection("fuel"));
        if(cFuel.isEmpty()) {
            cFuel = new ItemList<AdvancedItemStack>();
            cFuel.add(new AdvancedItemStack(Material.getMaterial("COAL"), 1, (short)1, "Charcoal"));
        }
        ConfigurationSection costs = config.getConfigurationSection("costs");
        ItemList<AdvancedItemStack> constructionCost = plugin.getItems(costs.getConfigurationSection("construction"));
        ItemList<AdvancedItemStack> repairCost = plugin.getItems(costs.getConfigurationSection("repair"));
        ItemList<AdvancedItemStack> recipeUse = plugin.getItems(costs.getConfigurationSection("recipe"));
        ItemList<AdvancedItemStack> specificExclusion = plugin.getItems(config.getConfigurationSection("specific_exclusions"));
        ItemList<AdvancedItemStack> generalExclusion = plugin.getItems(config.getConfigurationSection("excluded_types"));
        int energyTime = config.getInt("fuel_time");
        int repair = costs.getInt("repair_multiple", 1);
        String name = config.getString("name", "Compactor");
        int repairTime = config.getInt("repair_time", 12);
        int productionTime = config.getInt("production_time");
        String compactLore = config.getString("compact_lore", "Compacted Item");
        boolean continuous = config.getBoolean("continuous", false);
		Iterator<AdvancedItemStack> genExcludeIter = generalExclusion.iterator();
		List<Material> generalExclude = new ArrayList<Material>();
		
		while (genExcludeIter.hasNext()) {
			AdvancedItemStack exclude = genExcludeIter.next();
			
			generalExclude.add(exclude.getType());
		}

        return new CompactorProperties(constructionCost, cFuel, repairCost, recipeUse, energyTime, repair, name, repairTime, productionTime, compactLore, continuous, specificExclusion, generalExclude);
    }

}
