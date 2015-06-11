package com.github.igotyou.FactoryMod.properties;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.igotyou.FactoryMod.FactoryModPlugin;
import com.github.igotyou.FactoryMod.utility.ItemList;
import com.github.igotyou.FactoryMod.utility.NamedItemStack;

public class CompactorProperties {
    
    private ItemList<NamedItemStack> constructionMaterials;
    private ItemList<NamedItemStack> fuel;
    private ItemList<NamedItemStack> repairMaterials;
    private ItemList<NamedItemStack> recipeMaterials;
    private int energyTime;
    private int repair;
    private String name;
    private double repairTime;
    private double productionTime;
    private String compactLore;

    public CompactorProperties(ItemList<NamedItemStack> constructionMaterials,
            ItemList<NamedItemStack> fuel,
            ItemList<NamedItemStack> repairMaterials,
            ItemList<NamedItemStack> recipeMaterials, int energyTime,
            int repair, String name, double repairTime, double productionTime, String compactLore) {
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

    public int getEnergyTime() {
        return energyTime;
    }

    public int getRepair() {
        return repair;
    }

    public String getName() {
        return name;
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
        int energyTime = config.getInt("fuel_time");
        int repair = costs.getInt("repair_multiple", 1);
        String name = config.getString("name", "Compactor");
        int repairTime = config.getInt("repair_time", 12);
        int productionTime = config.getInt("production_time");
        String compactLore = config.getString("compact_lore", "Compacted Item");
        return new CompactorProperties(constructionCost, cFuel, repairCost, recipeUse, energyTime, repair, name, repairTime, productionTime, compactLore);
    }

}
