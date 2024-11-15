package com.github.igotyou.FactoryMod.recipes.upgrade;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.powerManager.FurnacePowerManager;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

public class CharcoalConsumptionUpgradeRecipe implements IRecipe {

    public static final String IDENTIFIER = "upgrade_charcoal_consumption";

    @Override
    public String getName() {
        return "Upgrade Charcoal Consumption";
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
        if (fccf.getCharcoalLevel() == 4 || fccf.getSpeedLevel() > 0 || ((FurnacePowerManager) fccf.getPowerManager()).getFuel().getType() != Material.CHARCOAL) {
            return false;
        }
        if (Upgrade.removeUpgrade(inputInv)) {
            fccf.setCharcoalLevel(fccf.getCharcoalLevel() + 1);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getTypeIdentifier() {
        return "UPGRADE_CHARCOAL_CONSUMPTION";
    }
}
