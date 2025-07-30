package com.github.igotyou.FactoryMod.recipes.heliodor;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import net.civmc.heliodor.heliodor.HeliodorGem;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import java.util.List;

public class HeliodorCreateRecipe extends InputRecipe {

    private final int outputCount;
    private final int maxCharge;

    public HeliodorCreateRecipe(String identifier, String name, int productionTime, ItemMap input, int outputCount, int maxCharge, int damagePerRun) {
        super(identifier, name, productionTime, input, damagePerRun);
        this.outputCount = outputCount;
        this.maxCharge = maxCharge;
    }

    @Override
    public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        super.applyEffect(inputInv, outputInv, fccf);
        MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
        logBeforeRecipeRun(combo, fccf);
        ItemMap toRemove = input.clone();
        ItemStack gem = HeliodorGem.createHeliodorGem(0, this.maxCharge);
        gem.setAmount(this.outputCount);
        ItemMap toAdd = new ItemMap(gem);
        if (toRemove.isContainedIn(inputInv)) {
            if (!toAdd.fitsIn(outputInv)) { // does not fit in chest
                return false;
            }
            if (toRemove.removeSafelyFrom(inputInv)) {
                for (ItemStack is : toAdd.getItemStackRepresentation()) {
                    outputInv.addItem(is);
                }
            }
        }
        logAfterRecipeRun(combo, fccf);
        return true;
    }

    @Override
    public String getTypeIdentifier() {
        return "HELIODOR_CREATE";
    }

    @Override
    public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        if (i == null) {
            return input.getItemStackRepresentation();
        }
        return createLoredStacksForInfo(i);
    }

    @Override
    public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        ItemStack gem = HeliodorGem.createHeliodorGem(0, 10);
        gem.setAmount(this.outputCount);
        return new ItemMap(gem).getItemStackRepresentation();
    }

    @Override
    public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        return List.of(this.outputCount + " Rough Heliodor Gem (0% infused)");
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.GOLD_BLOCK;
    }
}
