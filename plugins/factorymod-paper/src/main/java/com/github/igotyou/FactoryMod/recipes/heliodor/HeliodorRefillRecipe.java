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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HeliodorRefillRecipe extends InputRecipe {

    private final int count;
    private final int addMaxCharge;

    public HeliodorRefillRecipe(String identifier, String name, int productionTime, ItemMap input, int count, int addMaxCharge) {
        super(identifier, name, productionTime, input);
        this.count = count;
        this.addMaxCharge = addMaxCharge;
    }

    @Override
    public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
        logBeforeRecipeRun(combo, fccf);

        GemInput gemInput = getGemsToRemove(inputInv.getStorageContents());
        if (gemInput == null || gemInput.amount < this.count) {
            return false;
        }
        int amount = this.count;

        ItemMap toRemove = input.clone();
        ItemStack gem = HeliodorGem.createHeliodorGem(gemInput.charge, Math.min(100, gemInput.charge + this.addMaxCharge));
        gem.setAmount(this.count);
        ItemMap toAdd = new ItemMap(gem);
        if (toRemove.isContainedIn(inputInv)) {
            for (ItemStack item : inputInv.getStorageContents()) {
                if (item == null) {
                    continue;
                }
                Integer boxedCharge = HeliodorGem.getCharge(item);
                Integer boxedMaxCharge = HeliodorGem.getMaxCharge(item);
                if (boxedCharge == null || boxedMaxCharge == null) {
                    continue;
                }

                int charge = boxedCharge;
                int maxCharge = boxedMaxCharge;
                if (charge != maxCharge || charge != gemInput.charge) {
                    continue;
                }

                int removeAmount = Math.min(amount, item.getAmount());
                if (removeAmount != 0) {
                    ItemStack cloneStack = item.clone();
                    cloneStack.setAmount(removeAmount);
                    if (!inputInv.removeItem(cloneStack).isEmpty()) {
                        break;
                    } else {
                        amount -= removeAmount;
                        if (amount <= 0) {
                            break;
                        }
                    }
                }
            }

            if (!toAdd.fitsIn(outputInv)) { // does not fit in chest
                return false;
            }
            boolean removedGem = amount == 0;
            if (removedGem && toRemove.removeSafelyFrom(inputInv)) {
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
        return "HELIODOR_REFILL";
    }

    @Override
    public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        if (i == null) {
            List<ItemStack> repr = input.getItemStackRepresentation();
            ItemStack gem = HeliodorGem.createHeliodorGem(10, 10);
            gem.setAmount(this.count);
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
        GemInput gemInput = getGemsToRemove(i.getStorageContents());
        ItemStack gem;
        int amount;
        if (gemInput != null) {
            gem = HeliodorGem.createHeliodorGem(gemInput.charge, gemInput.charge);
            amount = gemInput.amount;
        } else {
            gem = HeliodorGem.createHeliodorGem(0, 0);
            amount = 0;
        }
        gem.setAmount(this.count);
        for (ItemStack is : new ItemMap(gem).getItemStackRepresentation()) {
            ItemUtils.addLore(is, ChatColor.GREEN + "Enough materials for " + (amount / this.count) + " runs");
            result.add(is);
        }
        for (ItemStack is : input.getItemStackRepresentation()) {
            ItemUtils.addLore(is, ChatColor.GREEN + "Enough materials for " + possibleRuns.getAmount(is) + " runs");
            result.add(is);
        }
        return result;
    }

    @Override
    public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        ItemStack gem = HeliodorGem.createHeliodorGem(10, 20);
        gem.setAmount(this.count);
        return new ItemMap(gem).getItemStackRepresentation();
    }

    @Override
    public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        List<String> repr = super.getTextualInputRepresentation(i, fccf);
        repr.add(this.count + " Rough Heliodor Gem at max infusion");
        return repr;
    }

    @Override
    public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        return List.of(this.count + " Rough Heliodor Gem with +" + addMaxCharge + "% max infusion");
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.EMERALD_BLOCK;
    }

    private GemInput getGemsToRemove(ItemStack[] items) {
        Map<Integer, Integer> foundItemsPerCharge = new LinkedHashMap<>();
        for (ItemStack item : items) {
            Integer boxedCharge = HeliodorGem.getCharge(item);
            Integer boxedMaxCharge = HeliodorGem.getMaxCharge(item);
            if (boxedCharge == null || boxedMaxCharge == null) {
                continue;
            }

            int charge = boxedCharge;
            int maxCharge = boxedMaxCharge;
            if (charge != maxCharge || maxCharge == 100) {
                continue;
            }

            foundItemsPerCharge.merge(charge, item.getAmount(), Integer::sum);
        }

        if (foundItemsPerCharge.isEmpty()) {
            return null;
        }

        int selectedAmount = 0;
        int selectedCharge = 0;
        for (Map.Entry<Integer, Integer> entry : foundItemsPerCharge.entrySet()) {
            if (entry.getValue() > selectedAmount || entry.getValue() > this.count) {
                selectedAmount = entry.getValue();
                selectedCharge = entry.getKey();
            }
        }

        return new GemInput(selectedCharge, selectedAmount);
    }

    private record GemInput(int charge, int amount) {

    }
}
