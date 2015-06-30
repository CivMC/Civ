package com.github.igotyou.FactoryMod.properties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class CompactorProperties extends AFactoryProperties{
    
    private ItemList<NamedItemStack> constructionMaterials;
    private ItemList<NamedItemStack> fuel;
    private ItemList<NamedItemStack> repairMaterials;
    private ItemList<NamedItemStack> recipeMaterials;
    private ItemList<NamedItemStack> specificExclusions;
    private List<Material> generalExlusions;
    private int energyTime;
    private int repair;
    private double repairTime;
    private double productionTime;
    private String compactLore;
    private boolean continuous;
    

    public CompactorProperties(ItemList<NamedItemStack> constructionMaterials,
            ItemList<NamedItemStack> fuel, ItemList<NamedItemStack> repairMaterials,
            ItemList<NamedItemStack> recipeMaterials, int energyTime, int repair, 
            String name, double repairTime, double productionTime, String compactLore,
            boolean continuous, ItemList<NamedItemStack> specificExclusion, List<Material> generalExclusion) {
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

    public ItemList<NamedItemStack> getConstructionMaterials() {
        return constructionMaterials;
    }

    public ItemList<NamedItemStack> getFuel() {
        return fuel;
    }

    public ItemList<NamedItemStack> getRepairMaterials() {
        return repairMaterials;
    }

    public ItemList<NamedItemStack> getRecipeMaterials() {
        return recipeMaterials;
    }
    
    public ItemList<NamedItemStack> getSpecificExclusions() {
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
        ItemList<NamedItemStack> cFuel = plugin.getItems(config.getConfigurationSection("fuel"));
        if(cFuel.isEmpty()) {
            cFuel = new ItemList<NamedItemStack>();
            cFuel.add(new NamedItemStack(Material.getMaterial("COAL"), 1, (short)1, "Charcoal"));
        }
        ConfigurationSection costs = config.getConfigurationSection("costs");
        ItemList<NamedItemStack> constructionCost = plugin.getItems(costs.getConfigurationSection("construction"));
        ItemList<NamedItemStack> repairCost = plugin.getItems(costs.getConfigurationSection("repair"));
        ItemList<NamedItemStack> recipeUse = plugin.getItems(costs.getConfigurationSection("recipe"));
        ItemList<NamedItemStack> specificExclusion = plugin.getItems(config.getConfigurationSection("specific_exclusions"));
        ItemList<NamedItemStack> generalExclusion = plugin.getItems(config.getConfigurationSection("excluded_types"));
        int energyTime = config.getInt("fuel_time");
        int repair = costs.getInt("repair_multiple", 1);
        String name = config.getString("name", "Compactor");
        int repairTime = config.getInt("repair_time", 12);
        int productionTime = config.getInt("production_time");
        String compactLore = config.getString("compact_lore", "Compacted Item");
        boolean continuous = config.getBoolean("continuous", false);
		Iterator<NamedItemStack> genExcludeIter = generalExclusion.iterator();
		List<Material> generalExclude = new ArrayList<Material>();
		
		while (genExcludeIter.hasNext()) {
			NamedItemStack exclude = genExcludeIter.next();
			
			generalExclude.add(exclude.getType());
		}

        return new CompactorProperties(constructionCost, cFuel, repairCost, recipeUse, energyTime, repair, name, repairTime, productionTime, compactLore, continuous, specificExclusion, generalExclude);
    }

}
