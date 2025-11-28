package com.github.igotyou.FactoryMod.recipes.heliodor;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import net.civmc.heliodor.heliodor.HeliodorGem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeliodorFinishRecipe extends InputRecipe {

    private final int inputCount;
    private final int outputCount;

    public HeliodorFinishRecipe(String identifier, String name, int productionTime, ItemMap input, int inputCount, int outputCount) {
        super(identifier, name, productionTime, input);
        this.inputCount = inputCount;
        this.outputCount = outputCount;
    }

    @Override
    public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
        logBeforeRecipeRun(combo, fccf);

        int count = 0;
        for (ItemStack inventoryStack : inputInv.getStorageContents()) {
            if (inventoryStack == null) {
                continue;
            }
            Integer charge = HeliodorGem.getCharge(inventoryStack);
            if (charge != null && charge == 100) {
                count += inventoryStack.getAmount();
            }
        }
        if (count < inputCount) {
            return false;
        }

        ItemMap toRemove = input.clone();
        ItemStack gem = HeliodorGem.FINISHED_HELIODOR_GEM.createItem();
        gem.setAmount(this.outputCount);
        ItemMap toAdd = new ItemMap(gem);
        if (toRemove.isContainedIn(inputInv)) {
            if (!toAdd.fitsIn(outputInv)) { // does not fit in chest
                return false;
            }

            int amountToRemove = this.inputCount;
            for (ItemStack inventoryStack : inputInv.getStorageContents()) {
                if (inventoryStack == null) {
                    continue;
                }
                Integer charge = HeliodorGem.getCharge(inventoryStack);
                if (charge != null && charge == 100) {
                    int removeAmount = Math.min(amountToRemove, inventoryStack.getAmount());
                    if (removeAmount != 0) {
                        ItemStack cloneStack = inventoryStack.clone();
                        cloneStack.setAmount(removeAmount);
                        if (!inputInv.removeItem(cloneStack).isEmpty()) {
                            return false;
                        } else {
                            amountToRemove -= removeAmount;
                            if (amountToRemove <= 0) {
                                break;
                            }
                        }
                    }
                }
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
        return "HELIODOR_FINISH";
    }

    @Override
    public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        if (i == null) {
            List<ItemStack> repr = input.getItemStackRepresentation();
            ItemStack gem = HeliodorGem.createHeliodorGem(100, 100);
            gem.setAmount(this.inputCount);
            repr.addAll(new ItemMap(gem).getItemStackRepresentation());
            return repr;
        }
        List<ItemStack> result = new ArrayList<>();
        ItemMap inventoryMap = new ItemMap(i);
        ItemMap possibleRuns = new ItemMap();
        for (Map.Entry<ItemStack, Integer> entry : input.getAllItems().entrySet()) {
            if (inventoryMap.getAmount(entry.getKey()) != 0) {
                possibleRuns.addItemAmount(entry.getKey(), inventoryMap.getAmount(entry.getKey()) / entry.getValue());
            } else {
                possibleRuns.addItemAmount(entry.getKey(), 0);
            }
        }

        int count = 0;
        for (ItemStack inventoryStack : i.getStorageContents()) {
            if (inventoryStack == null) {
                continue;
            }
            Integer charge = HeliodorGem.getCharge(inventoryStack);
            if (charge != null && charge == 100) {
                count += inventoryStack.getAmount();
            }
        }

        ItemStack gem = HeliodorGem.createHeliodorGem(100, 100);
        gem.setAmount(this.inputCount);
        for (ItemStack is : new ItemMap(gem).getItemStackRepresentation()) {
            ItemUtils.addLore(is, ChatColor.GREEN + "Enough materials for " + (count / this.inputCount) + " runs");
            result.add(is);
        }

        for (ItemStack is : input.getItemStackRepresentation()) {
            ItemUtils.addLore(is, ChatColor.GREEN + "Enough materials for " + possibleRuns.getAmount(is) + " runs");
            result.add(is);
        }
        return result;
    }

    @Override
    public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        List<String> repr = super.getTextualInputRepresentation(i, fccf);
        repr.add(this.inputCount + " Rough Heliodor Gem (100% infused)");
        return repr;
    }

    @Override
    public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        ItemStack gem = HeliodorGem.FINISHED_HELIODOR_GEM.createItem();
        gem.setAmount(this.outputCount);
        return new ItemMap(gem).getItemStackRepresentation();
    }

    @Override
    public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        return List.of(this.outputCount + " Heliodor Gem");
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.BRUSH;
    }
}
