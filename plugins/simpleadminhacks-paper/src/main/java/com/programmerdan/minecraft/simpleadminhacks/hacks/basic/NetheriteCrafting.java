package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

// CraftEnhance doesn't let us block just the netherite scrap -> netherite ingot recipe
// It also blocks crafting netherite block, so this is a workaround to allow that
public class NetheriteCrafting extends BasicHack {

    public NetheriteCrafting(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private boolean isNetheriteScrapRecipe(Recipe recipe) {
        if (!(recipe instanceof ShapelessRecipe shapelessRecipe)) {
            return false;
        }

        if (shapelessRecipe.getResult().getType() != Material.NETHERITE_INGOT) {
            return false;
        }

        for (RecipeChoice choice : shapelessRecipe.getChoiceList()) {
            if (choice.test(new ItemStack(Material.NETHERITE_BLOCK))) {
                return false;
            }
        }

        return true;
    }

    @EventHandler
    public void on(PrepareItemCraftEvent e) {
        if (isNetheriteScrapRecipe(e.getRecipe())) {
            e.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void on(CrafterCraftEvent e) {
        if (isNetheriteScrapRecipe(e.getRecipe())) {
            e.setCancelled(true);
        }
    }
}
