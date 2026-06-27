package com.github.igotyou.FactoryMod.recipes.space;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.EffectFeasibility;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import com.github.igotyou.FactoryMod.recipes.scaling.ProductionRecipeModifier;
import java.util.ArrayList;
import java.util.List;
import net.civmc.zorweth.ZorwethPlugin;
import net.civmc.zorweth.oxygen.OxygenManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public final class OxygenRefillRecipe extends ProductionRecipe {

    public OxygenRefillRecipe(final String identifier, final String name, final int productionTime,
                              final ItemMap inputs, final ItemMap output, final ItemStack recipeRepresentation,
                              final ProductionRecipeModifier modifier) {
        super(identifier, name, productionTime, inputs, output, recipeRepresentation, modifier);
    }

    @Override
    public EffectFeasibility evaluateEffectFeasibility(final Inventory inputInv, final Inventory outputInv,
                                                       final FurnCraftChestFactory fccf) {
        final EffectFeasibility productionFeasibility = super.evaluateEffectFeasibility(inputInv, outputInv, fccf);
        if (!productionFeasibility.isFeasible()) {
            return productionFeasibility;
        }

        final OxygenManager oxygenManager = getOxygenManager();
        if (fccf.getFurnace().getWorld().getEnvironment() == World.Environment.NETHER) {
            return new EffectFeasibility(false, "oxygen cannot be refilled in the nether");
        }
        if (oxygenManager != null && !oxygenManager.hasOxygen(fccf.getFurnace().getBiome())) {
            return new EffectFeasibility(false, "oxygen is required");
        }
        return new EffectFeasibility(true, null);
    }

    @Override
    public ItemStack getRecipeRepresentation() {
        final ItemStack result = super.getRecipeRepresentation();
        final ItemMeta meta = result.getItemMeta();
        final List<String> lore = new ArrayList<>(meta.getLore());
        lore.add(ChatColor.DARK_AQUA + "Requires oxygen in the local biome.");
        lore.add(ChatColor.DARK_AQUA + "Immune to factory speed upgrade.");
        meta.setLore(lore);
        result.setItemMeta(meta);
        return result;
    }

    @Override
    public String getTypeIdentifier() {
        return "OXYGEN_REFILL";
    }

    private OxygenManager getOxygenManager() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Zorweth")) {
            return null;
        }
        return JavaPlugin.getPlugin(ZorwethPlugin.class).getOxygenManager();
    }

    @Override
    public boolean canApplySpeed() {
        return false;
    }
}
