package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;

/**
 * A recipe with any form of item input to run it
 */
public abstract class InputRecipe implements IRecipe {

    protected String name;
    protected int productionTime;
    protected ItemMap input;
    protected int fuel_consumption_intervall = -1;
    protected String identifier;

    public InputRecipe(String identifier, String name, int productionTime, ItemMap input) {
        this.name = name;
        this.productionTime = productionTime;
        this.input = input;
        this.identifier = identifier;
    }

    /**
     * Used to get a representation of a recipes input materials, which is
     * displayed in an item gui to illustrate the recipe and to give additional
     * information. If null is given instead of an inventory or factory, just
     * general information should be returned, which doesnt depend on a specific
     * instance
     *
     * @param i    Inventory for which the recipe would be run, this is used to
     *             add lore to the items, which tells how often the recipe could
     *             be run
     * @param fccf Factory for which the representation is meant. Needed for
     *             recipe run scaling
     * @return List of itemstacks which represent the input required to run this
     * recipe
     */
    public abstract List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf);

    /**
     * Used to get a representation of a recipes input materials, which is
     * displayed in chat or an items lore to illustrate the recipe and to give additional
     * information. If null is given instead of an inventory or factory, just
     * general information should be returned, which doesnt depend on a specific
     * instance
     *
     * @param i    Inventory for which the recipe would be run, this is used to
     *             add a count how often the recipe could be run
     * @param fccf Factory for which the representation is meant. Needed for
     *             recipe run scaling
     * @return List of Strings each describing one component needed as input for this recipe
     */
    public List<String> getTextualInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        return formatLore(input);
    }

    /**
     * Used to get a representation of a recipes output materials, which is
     * displayed in an item gui to illustrate the recipe and to give additional
     * information. If null is given instead of an inventory or factory, just
     * general information should be returned, which doesnt depend on a specific
     * instance
     *
     * @param i    Inventory for which the recipe would be run, this is used to
     *             add lore to the items, which tells how often the recipe could
     *             be run
     * @param fccf Factory for which the representation is meant. Needed for
     *             recipe run scaling
     * @return List of itemstacks which represent the output returned when
     * running this recipe
     */
    public abstract List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf);

    /**
     * Used to get a representation of a recipes output, which is
     * displayed in chat or an items lore to illustrate the recipe and to give additional
     * information. If null is given instead of an inventory or factory, just
     * general information should be returned, which doesnt depend on a specific
     * instance
     *
     * @param i    Inventory for which the recipe would be run, this is used to
     *             add a count how often the recipe could be run
     * @param fccf Factory for which the representation is meant. Needed for
     *             recipe run scaling
     * @return List of Strings each describing one component produced as output of this recipe
     */
    public abstract List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf);

    @Override
    public String getName() {
        return name;
    }

    public int getTotalFuelConsumed() {
        if (fuel_consumption_intervall == 0) {
            return 0;
        }
        return productionTime / fuel_consumption_intervall;
    }

    public int getFuelConsumptionIntervall() {
        return fuel_consumption_intervall;
    }

    public void setFuelConsumptionIntervall(int intervall) {
        this.fuel_consumption_intervall = intervall;
    }

    @Override
    public int getProductionTime() {
        return productionTime;
    }

    public ItemMap getInput() {
        return input;
    }

    @Override
    public boolean enoughMaterialAvailable(Inventory inputInv) {
        return input.isContainedIn(inputInv);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return A single itemstack which is used to represent this recipe as a
     * whole in an item gui
     */
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
        lore.add(ChatColor.DARK_AQUA + "Time: " + ChatColor.GRAY + TextUtil
            .formatDuration(getProductionTime() * 50, TimeUnit.MILLISECONDS));
        im.setLore(lore);
        res.setItemMeta(im);
        return res;
    }

    public abstract Material getRecipeRepresentationMaterial();

    public ItemStack getRecipeRepresentationType() {
        return new ItemStack(getRecipeRepresentationMaterial());
    }

    /**
     * Creates a list of ItemStack for a GUI representation. This list contains
     * all the itemstacks contained in the itemstack representation of the input
     * map and adds to each of the stacks how many runs could be made with the
     * material available in the chest
     *
     * @param i Inventory to calculate the possible runs for
     * @return ItemStacks containing the additional information, ready for the
     * GUI
     */
    protected List<ItemStack> createLoredStacksForInfo(Inventory i) {
        LinkedList<ItemStack> result = new LinkedList<>();
        ItemMap inventoryMap = new ItemMap(i);
        ItemMap possibleRuns = new ItemMap();
        for (Entry<ItemStack, Integer> entry : input.getAllItems().entrySet()) {
            if (inventoryMap.getAmount(entry.getKey()) != 0) {
                possibleRuns.addItemAmount(entry.getKey(), inventoryMap.getAmount(entry.getKey()) / entry.getValue());
            } else {
                possibleRuns.addItemAmount(entry.getKey(), 0);
            }
        }
        for (ItemStack is : input.getItemStackRepresentation()) {
            ItemUtils.addLore(is, ChatColor.GREEN + "Enough materials for " + String.valueOf(possibleRuns.getAmount(is))
                + " runs");
            result.add(is);
        }
        return result;
    }

    protected void logBeforeRecipeRun(Inventory i, Factory f) {
        LoggingUtils.logInventory(i, "Before executing recipe " + name + " for " + f.getLogData());
    }

    protected void logAfterRecipeRun(Inventory i, Factory f) {
        LoggingUtils.logInventory(i, "After executing recipe " + name + " for " + f.getLogData());
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    protected List<String> formatLore(ItemMap ingredients) {
        List<String> result = new ArrayList<>();
        for (Entry<ItemStack, Integer> entry : ingredients.getAllItems().entrySet()) {
            if (entry.getValue() > 0) {
                ItemStack item = entry.getKey();
                String customItemKey = CustomItem.getCustomItemKey(item);
                if (customItemKey != null) {
                    ItemStack customItem = CustomItem.getCustomItem(customItemKey);
                    if (customItem != null && customItem.hasItemMeta() && customItem.getItemMeta().hasDisplayName()) {
                        result.add(String.format("%s %s", entry.getValue(),
                            StringUtils.abbreviate(customItem.getItemMeta().getDisplayName(), 35)));
                    } else if (customItem != null && customItem.hasItemMeta() && customItem.getItemMeta().hasItemName()) {
                        result.add(String.format("%s %s", entry.getValue(),
                            StringUtils.abbreviate(customItem.getItemMeta().getItemName(), 35)));
                    } else {
                        result.add(String.format("%s %s", entry.getValue(), customItemKey));
                    }
                } else if (!item.hasItemMeta()) {
                    result.add(entry.getValue() + " " + ItemUtils.getItemName(item));
                } else {
                    String lore = String.format("%s %s%s", entry.getValue(), ChatColor.ITALIC, ItemUtils.getItemName(item));
                    if (item.getItemMeta().hasDisplayName()) {
                        lore += String.format("%s [%s%1$s]", ChatColor.DARK_AQUA,
                            StringUtils.abbreviate(item.getItemMeta().getDisplayName(), 20));
                    }
                    result.add(lore);
                }
            }
        }
        return result;
    }
    protected String formatIngredientName(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return "Unknown";
        }

        if (CustomItem.isCustomItem(item)) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName()) {
                    return StringUtils.abbreviate(meta.getDisplayName(), 35);
                } else if (meta.hasItemName()) {
                    return StringUtils.abbreviate(meta.getItemName(), 35);
                }
            }

            return ChatColor.ITALIC + ItemUtils.getItemName(item);
        }

        if (!item.hasItemMeta()) {
            return ItemUtils.getItemName(item);
        }

        ItemMeta meta = item.getItemMeta();
        String name = ChatColor.ITALIC + ItemUtils.getItemName(item);

        if (meta.hasDisplayName()) {
            name += String.format(
                "%s [%s%1$s]",
                ChatColor.DARK_AQUA,
                StringUtils.abbreviate(meta.getDisplayName(), 20)
            );
        }

        return name;
    }

    protected boolean canFitInOutput(ItemMap outputMap, Inventory outputInv) {
        ItemStack[] currentContent = outputInv.getStorageContents();
        ItemStack[] simulatedOutput = new ItemStack[currentContent.length];

        for (int i = 0; i < currentContent.length; i++) {
            ItemStack slot = currentContent[i];
            simulatedOutput[i] = slot == null ? null : slot.clone();
        }

        for (Entry<ItemStack, Integer> outputEntry : outputMap.getAllItems().entrySet()) {
            ItemStack outputTemplate = outputEntry.getKey();
            int remainingAmount = outputEntry.getValue();

            if (outputTemplate == null || outputTemplate.isEmpty() || remainingAmount <= 0) {
                continue;
            }

            int maxStackSize = Math.max(1, outputTemplate.getMaxStackSize());

            for (int i = 0; i < simulatedOutput.length && remainingAmount > 0; i++) {
                ItemStack existingStack = simulatedOutput[i];

                if (existingStack == null
                        || existingStack.isEmpty()
                        || !existingStack.isSimilar(outputTemplate)) {
                    continue;
                }

                int existingMaxStackSize = Math.max(1, existingStack.getMaxStackSize());
                int freeSpace = Math.max(0, existingMaxStackSize - existingStack.getAmount());

                if (freeSpace <= 0) {
                    continue;
                }

                int movedAmount = Math.min(remainingAmount, freeSpace);
                existingStack.setAmount(existingStack.getAmount() + movedAmount);
                remainingAmount -= movedAmount;
            }

            for (int i = 0; i < simulatedOutput.length && remainingAmount > 0; i++) {
                ItemStack existingStack = simulatedOutput[i];

                if (existingStack != null && !existingStack.isEmpty()) {
                    continue;
                }

                int movedAmount = Math.min(remainingAmount, maxStackSize);
                ItemStack toInsert = outputTemplate.clone();
                toInsert.setAmount(movedAmount);
                simulatedOutput[i] = toInsert;
                remainingAmount -= movedAmount;
            }

            if (remainingAmount > 0) {
                return false;
            }
        }

        return true;
    }

    protected boolean addOutputToInventorySafely(
            ItemMap outputMap,
            Inventory outputInv,
            List<ItemStack> insertedOutput) {

        for (Entry<ItemStack, Integer> outputEntry : outputMap.getAllItems().entrySet()) {
            ItemStack outputTemplate = outputEntry.getKey();
            int remainingAmount = outputEntry.getValue();

            if (outputTemplate == null || outputTemplate.isEmpty() || remainingAmount <= 0) {
                continue;
            }

            int maxStackSize = Math.max(1, outputTemplate.getMaxStackSize());

            while (remainingAmount > 0) {
                int movedAmount = Math.min(remainingAmount, maxStackSize);

                ItemStack toInsert = outputTemplate.clone();
                toInsert.setAmount(movedAmount);

                java.util.Map<Integer, ItemStack> overflow = outputInv.addItem(toInsert);

                int overflowAmount = 0;
                for (ItemStack overflowStack : overflow.values()) {
                    overflowAmount += overflowStack.getAmount();
                }

                int insertedAmount = movedAmount - overflowAmount;

                if (insertedAmount > 0) {
                    ItemStack insertedStack = outputTemplate.clone();
                    insertedStack.setAmount(insertedAmount);
                    insertedOutput.add(insertedStack);
                }

                if (!overflow.isEmpty()) {
                    return false;
                }

                remainingAmount -= movedAmount;
            }
        }

        return true;
    }

    protected void rollbackOutput(
            Inventory outputInv,
            List<ItemStack> insertedOutput) {

        for (ItemStack outputStack : insertedOutput) {
            outputInv.removeItem(outputStack);
        }
    }

    protected void restoreInput(
            ItemMap removedInput,
            Inventory inputInv,
            FurnCraftChestFactory fccf) {

        for (Entry<ItemStack, Integer> removedEntry : removedInput.getAllItems().entrySet()) {
            ItemStack removedTemplate = removedEntry.getKey();
            int remainingAmount = removedEntry.getValue();

            if (removedTemplate == null || removedTemplate.isEmpty() || remainingAmount <= 0) {
                continue;
            }

            int maxStackSize = Math.max(1, removedTemplate.getMaxStackSize());

            while (remainingAmount > 0) {
                int movedAmount = Math.min(remainingAmount, maxStackSize);

                ItemStack removedStack = removedTemplate.clone();
                removedStack.setAmount(movedAmount);

                java.util.Map<Integer, ItemStack> overflow = inputInv.addItem(removedStack);

                if (!overflow.isEmpty()) {
                    FactoryMod.getInstance().warning(
                        "Failed to fully restore input after recipe rollback :(,"
                        + fccf.getLogData()
                    );
                    return;
                }

                remainingAmount -= movedAmount;
            }
        }
    }
}
