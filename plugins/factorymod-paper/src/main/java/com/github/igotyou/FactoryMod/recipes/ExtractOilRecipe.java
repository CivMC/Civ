package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.scaling.ProductionRecipeModifier;
import java.util.ArrayList;
import java.util.List;
import net.civmc.zorweth.ZorwethPlugin;
import net.civmc.zorweth.mechanics.OilMechanics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public class ExtractOilRecipe extends ProductionRecipe {

    public ExtractOilRecipe(
        String identifier,
        String name,
        int productionTime,
        ItemMap inputs,
        ItemMap output,
        ItemStack recipeRepresentation,
        ProductionRecipeModifier modifier
    ) {
        super(identifier, name, productionTime, inputs, output, recipeRepresentation, modifier);
    }

    @Override
    public EffectFeasibility evaluateEffectFeasibility(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Zorweth")) {
            return new EffectFeasibility(false, "there is oil here");
        }
        OilMechanics.VeinPing ping = JavaPlugin.getPlugin(ZorwethPlugin.class).getMechanics().ping(fccf.getMultiBlockStructure().getCenter());
        if (ping != OilMechanics.VeinPing.VEIN) {
            return new EffectFeasibility(false, "there is no oil here");
        }
        return new EffectFeasibility(true, null);
    }

    @Override
    public int getProductionTime(FurnCraftChestFactory fccf) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Zorweth")) {
            return getProductionTime();
        }
        return JavaPlugin.getPlugin(ZorwethPlugin.class).recordOilExtraction(fccf.getMultiBlockStructure().getCenter()) * 20;
    }

    @Override
    public ItemStack getRecipeRepresentation() {
        ItemStack res = getRecipeRepresentationType();
        ItemMeta im = res.getItemMeta();
        im.setDisplayName(ChatColor.DARK_GREEN + getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Input:");
        for (String s : getTextualInputRepresentation(null, null)) {
            lore.add(ChatColor.GRAY + " - " + ChatColor.AQUA + s);
        }
        lore.add("");
        lore.add(ChatColor.GOLD + "Output:");
        for (String s : getTextualOutputRepresentation(null, null)) {
            lore.add(ChatColor.GRAY + " - " + ChatColor.AQUA + s);
        }
        lore.add("");
        lore.add(ChatColor.DARK_AQUA + "Requires crude oil deposit nearby.");
        lore.add(ChatColor.DARK_AQUA + "Duration is variable based on yield.");
        lore.add(ChatColor.DARK_AQUA + "Multiple factories near a deposit will split the yield.");
        lore.add(ChatColor.DARK_AQUA + "Immune to factory speed upgrade.");
        im.setLore(lore);
        res.setItemMeta(im);
        return res;
    }

    @Override
    public String getTypeIdentifier() {
        return "EXTRACT_OIL";
    }

    @Override
    public boolean canApplySpeed() {
        return false;
    }

    @Override
    public boolean mustBeLoaded() {
        return true;
    }
}
