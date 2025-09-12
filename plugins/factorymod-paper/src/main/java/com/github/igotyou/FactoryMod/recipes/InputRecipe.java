package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.utility.LoggingUtils;
import java.awt.*;
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
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
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
        ItemStack res = new ItemStack(getRecipeRepresentationMaterial());
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
        for (Entry<ItemStack, Integer> entry : ingredients.getItems().entrySet()) {
            if (entry.getValue() > 0) {
                if (!entry.getKey().hasItemMeta()) {
                    result.add(entry.getValue() + " " + ItemUtils.getItemName(entry.getKey()));
                } else {
                    String lore = String.format("%s %s%s", entry.getValue(), ChatColor.ITALIC, ItemUtils.getItemName(entry.getKey()));
                    if (entry.getKey().getItemMeta().hasDisplayName()) {
                        lore += String.format("%s [%s%1$s]", ChatColor.DARK_AQUA, StringUtils.abbreviate(entry.getKey().getItemMeta().getDisplayName(), 20));
                    }
                    result.add(lore);
                }
            }
        }
        // Custom items should have their custom name displayed more prominently, their actual item type is irrelevant
        for (Entry<String, Integer> entry : ingredients.getCustomItems().entrySet()) {
            if (entry.getValue() > 0) {
                ItemStack item = CustomItem.getCustomItem(entry.getKey());
                if (!item.hasItemMeta()) {
                    result.add(entry.getValue() + " " + ItemUtils.getItemName(item));
                } else {
                    String lore;
                    if (item.getItemMeta().hasDisplayName()) {
                        lore = String.format("%s %s", entry.getValue(), StringUtils.abbreviate(item.getItemMeta().getDisplayName(), 35));
                    } else if (item.getItemMeta().hasItemName()) {
                        lore = String.format("%s %s", entry.getValue(), StringUtils.abbreviate(item.getItemMeta().getItemName(), 35));
                    } else {
                        lore = String.format("%s %s%s", entry.getValue(), ChatColor.ITALIC, ItemUtils.getItemName(item));
                    }
                    result.add(lore);
                }
            }
        }
        return result;
    }

}
