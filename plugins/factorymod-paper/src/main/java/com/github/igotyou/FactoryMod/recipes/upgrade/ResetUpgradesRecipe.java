package com.github.igotyou.FactoryMod.recipes.upgrade;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import org.bukkit.inventory.Inventory;

public class ResetUpgradesRecipe implements IRecipe {

    public static String IDENTIFIER = "reset_upgrades";

    @Override
    public String getName() {
        return "Reset Upgrades";
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public int getProductionTime() {
        return 80;
    }

    @Override
    public boolean enoughMaterialAvailable(Inventory inputInv) {
        return true;
    }

    @Override
    public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        if (fccf.getSpeedLevel() == 0 && fccf.getCharcoalLevel() == 0) {
            return false;
        }
        fccf.setSpeedLevel(0);
        fccf.setCharcoalLevel(0);
        return false;
    }

    @Override
    public String getTypeIdentifier() {
        return "RESET_UPGRADES";
    }
}
