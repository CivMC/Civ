package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.compaction.Compaction;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.ClonedInventory;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

/**
 * Used to decompact itemstacks, which means a single item with compacted lore
 * is turned into a whole stack without lore. This reverses the functionality of
 * the CompactingRecipe
 */
public class DecompactingRecipe extends InputRecipe {

    public DecompactingRecipe(String identifier, ItemMap input, String name, int productionTime) {
        super(identifier, name, productionTime, input);
    }

    @Override
    public boolean enoughMaterialAvailable(Inventory inputInv) {
        if (!input.isContainedIn(inputInv)) {
            return false;
        }
        for (ItemStack is : inputInv.getContents()) {
            if (is != null) {
                if (Compaction.isCompacted(is)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public EffectFeasibility evaluateEffectFeasibility(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        boolean isFeasible = true;
        for (ItemStack itemStack : inputInv.getContents()) {
            if (itemStack != null) {
                if (Compaction.isCompacted(itemStack)) {
                    ItemStack removeClone = itemStack.clone();
                    removeClone.setAmount(1);
                    removeClone.editMeta(Compaction::demarkAsCompacted);
                    ItemMap toAdd = new ItemMap();
                    toAdd.addItemAmount(removeClone, CompactingRecipe.getCompactStackSize(removeClone));
                    if (!InventoryUtils.safelyAddItemsToInventory(
                        ClonedInventory.cloneInventory(outputInv), toAdd.getItemStackRepresentation().toArray(new ItemStack[0]))) {
                        isFeasible = false;
                        break;
                    }
                }
            }
        }
        return new EffectFeasibility(
            isFeasible,
            isFeasible ? null : "it ran out of storage space"
        );
    }

    @Override
    public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
        logBeforeRecipeRun(combo, fccf);
        if (input.isContainedIn(inputInv)) {
            for (ItemStack is : inputInv.getContents()) {
                if (is != null) {
                    if (Compaction.isCompacted(is)) {
                        ItemStack removeClone = is.clone();
                        removeClone.setAmount(1);
                        ItemMap toRemove = new ItemMap(removeClone);
                        ItemMap toAdd = new ItemMap();
                        removeClone.editMeta(Compaction::demarkAsCompacted);
                        toAdd.addItemAmount(removeClone, CompactingRecipe.getCompactStackSize(removeClone));
                        ItemStack[] itemsToAdd = toAdd.getItemStackRepresentation().toArray(new ItemStack[0]);
                        if (!InventoryUtils.safelyAddItemsToInventory(
                            ClonedInventory.cloneInventory(outputInv), itemsToAdd)) {
                            return false; // does not fit in chest
                        }
                        if (input.removeSafelyFrom(inputInv)) { //remove extra input
                            if (toRemove.removeSafelyFrom(inputInv)) { //remove one compacted item
                                for (ItemStack add : toAdd.getItemStackRepresentation()) {
                                    outputInv.addItem(add);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        logAfterRecipeRun(combo, fccf);
        return true;
    }

    @Override
    public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        List<ItemStack> result = new LinkedList<>();
        if (i == null) {
            ItemStack is = new ItemStack(Material.STONE, 64);
            Compaction.markAsCompacted(is.getItemMeta());
            is.setAmount(1);
            result.add(is);
            return result;
        }
        result = createLoredStacksForInfo(i);
        for (ItemStack is : i.getContents()) {
            if (is != null) {
                if (Compaction.isCompacted(is)) {
                    ItemStack compactedStack = is.clone();
                    result.add(compactedStack);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.TRAPPED_CHEST;
    }

    @Override
    public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        List<ItemStack> result = new LinkedList<>();
        if (i == null) {
            result.add(new ItemStack(Material.STONE, 64));
            return result;
        }
        for (ItemStack is : i.getContents()) {
            if (is != null) {
                if (Compaction.isCompacted(is)) {
                    ItemStack copy = is.clone();
                    copy.editMeta(Compaction::demarkAsCompacted);
                    ItemMap output = new ItemMap();
                    output.addItemAmount(copy, CompactingRecipe.getCompactStackSize(copy));
                    result.addAll(output.getItemStackRepresentation());
                }
            }
        }
        return result;
    }

    @Override
    public String getTypeIdentifier() {
        return "DECOMPACT";
    }

    @Override
    public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        return Arrays.asList("An entire stack of the decompacted item if it's stackable", "---OR---", "Eight of the decompacted item if it's not stackable");
    }

    @Override
    public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        return Arrays.asList("A single compacted item");
    }
}
