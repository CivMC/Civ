package com.github.igotyou.FactoryMod.recipes.upgrade;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import org.bukkit.inventory.Inventory;

public class SpeedUpgradeRecipe implements IRecipe {

    public static String IDENTIFIER = "upgrade_speed";

    @Override
    public String getName() {
        return "Upgrade Speed";
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
        return Upgrade.hasUpgrade(inputInv.getStorageContents());
    }

    @Override
    public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        if (fccf.getSpeedLevel() == 4 || fccf.getCharcoalLevel() > 0) {
            return false;
        }
        if (Upgrade.removeUpgrade(inputInv)) {
            fccf.setSpeedLevel(fccf.getSpeedLevel() + 1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getTypeIdentifier() {
        return "UPGRADE_SPEED";
    }
}
