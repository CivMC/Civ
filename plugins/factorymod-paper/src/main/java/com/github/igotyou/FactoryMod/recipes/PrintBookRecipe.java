package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.papermc.paper.datacomponent.DataComponentTypes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import org.jetbrains.annotations.Nullable;

import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public class PrintBookRecipe extends PrintingPressRecipe {

    private ItemMap printingPlate;
    private int outputAmount;

    public ItemMap getPrintingPlate() {
        return this.printingPlate;
    }

    public int getOutputAmount() {
        return this.outputAmount;
    }

    public PrintBookRecipe(
        String identifier,
        String name,
        int productionTime,
        ItemMap input,
        ItemMap printingPlate,
        int outputAmount
    ) {
        super(identifier, name, productionTime, input);
        this.printingPlate = printingPlate;
        this.outputAmount = outputAmount;
    }

    @Override
    public boolean enoughMaterialAvailable(Inventory inputInv) {
        return this.input.isContainedIn(inputInv)
            && getPrintingPlateItemStack(inputInv, this.printingPlate) != null;
    }

    @Override
    public boolean applyEffect(
        Inventory inputInv,
        Inventory outputInv,
        FurnCraftChestFactory fccf
    ) {
        MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
        logBeforeRecipeRun(combo, fccf);

        ItemStack printingPlateStack =
            getPrintingPlateItemStack(inputInv, this.printingPlate);

        ItemMap toRemove = this.input.clone();

        if (printingPlateStack != null
            && toRemove.isContainedIn(inputInv)
            && toRemove.removeSafelyFrom(inputInv)
        ) {
            ItemStack book = createBook(printingPlateStack, this.outputAmount);
            book.editMeta(
                BookMeta.class,
                x -> x.setGeneration(getNextGeneration(x.getGeneration()))
            );
            outputInv.addItem(book);
        }

        logAfterRecipeRun(combo, fccf);
        return true;
    }

    protected ItemStack createBook(ItemStack printingPlateStack, int amount) {
        net.minecraft.world.item.ItemStack book =
            CraftItemStack.asNMSCopy(printingPlateStack);

        CompoundTag bookData =
            (CompoundTag) book.get(DataComponents.CUSTOM_DATA)
                .copyTag()
                .get("Book");

        ItemStack bookItem =
            new ItemStack(Material.WRITTEN_BOOK, amount);

        if (printingPlateStack.hasData(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
            bookItem.setData(
                DataComponentTypes.WRITTEN_BOOK_CONTENT,
                printingPlateStack.getData(
                    DataComponentTypes.WRITTEN_BOOK_CONTENT
                )
            );
        } else {
            // Handle legacy (pre-1.20.5) plates
            bookItem.editMeta(BookMeta.class, meta -> {
                meta.title(
                    Component.text(
                        bookData.get("title").asString().get()
                    )
                );

                meta.author(
                    Component.text(
                        bookData.get("author").asString().get()
                    )
                );

                meta.setGeneration(
                    BookMeta.Generation.values()[
                        ((IntTag) bookData.get("generation")).intValue()
                    ]
                );

                List<Component> pages = new ArrayList<>();

                ((ListTag) bookData.get("pages")).forEach(page -> {
                    pages.add(
                        GsonComponentSerializer.gson()
                            .deserialize(page.asString().get())
                    );
                });

                meta.pages(pages);
            });
        }

        return bookItem;
    }

    protected BookMeta.Generation getNextGeneration(
        @Nullable BookMeta.Generation generation
    ) {
        if (generation == null) {
            generation = BookMeta.Generation.ORIGINAL;
        }

        return switch (generation) {
            case ORIGINAL -> BookMeta.Generation.COPY_OF_ORIGINAL;
            case COPY_OF_ORIGINAL -> BookMeta.Generation.COPY_OF_COPY;
            default -> BookMeta.Generation.TATTERED;
        };
    }

    @Override
    public List<ItemStack> getInputRepresentation(
        Inventory i,
        FurnCraftChestFactory fccf
    ) {
        List<ItemStack> result = new LinkedList<>();

        if (i == null) {
            ItemStack is = getPrintingPlateRepresentation(
                this.printingPlate,
                PrintingPlateRecipe.itemName
            );

            result.addAll(this.input.getItemStackRepresentation());
            result.add(is);
            return result;
        }

        result = createLoredStacksForInfo(i);

        ItemStack printingPlateStack =
            getPrintingPlateItemStack(i, this.printingPlate);

        if (printingPlateStack != null) {
            result.add(printingPlateStack.clone());
        }

        return result;
    }

    /**
     * Creates the recipe item shown in the recipe scrollbar.
     *
     * When an inventory is supplied, the input section shows the
     * available/required amount for the normal inputs as well as
     * whether the required Printing Plate is present.
     */
    @Override
    public ItemStack getRecipeRepresentation(Inventory inputInv) {
        ItemStack res = getRecipeRepresentationType();
        ItemMeta im = res.getItemMeta();

        im.setDisplayName(ChatColor.DARK_GREEN + getName());

        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.GOLD + "Input:");

        if (inputInv == null) {
            for (String s : getTextualInputRepresentation(null, null)) {
                lore.add(
                    ChatColor.GRAY + " - "
                        + ChatColor.AQUA + s
                );
            }

            lore.add(
                ChatColor.GRAY + " - "
                    + ChatColor.AQUA
                    + "1 " + PrintingPlateRecipe.itemName
            );
        } else {
            ItemMap inventoryMap = new ItemMap(inputInv);

            // Normal recipe inputs.
            for (
                java.util.Map.Entry<ItemStack, Integer> entry
                    : input.getAllItems().entrySet()
            ) {
                if (entry.getValue() <= 0) {
                    continue;
                }

                int required = entry.getValue();
                int available =
                    inventoryMap.getAmount(entry.getKey());

                ChatColor color =
                    available >= required
                        ? ChatColor.GREEN
                        : ChatColor.RED;

                lore.add(
                    ChatColor.GRAY + " - "
                        + color
                        + available + "/" + required + " "
                        + formatIngredientName(entry.getKey())
                );
            }

            // A Printing Plate is always required for this recipe.
            ItemStack printingPlateStack =
                getPrintingPlateItemStack(
                    inputInv,
                    printingPlate
                );

            ChatColor plateColor =
                printingPlateStack != null
                    ? ChatColor.GREEN
                    : ChatColor.RED;

            lore.add(
                ChatColor.GRAY + " - "
                    + plateColor
                    + (printingPlateStack != null
                        ? "1/1 "
                        : "0/1 ")
                    + PrintingPlateRecipe.itemName
            );
        }

        lore.add("");

        lore.add(ChatColor.GOLD + "Output:");

        for (
            String s
                : getTextualOutputRepresentation(null, null)
        ) {
            lore.add(
                ChatColor.GRAY + " - "
                    + ChatColor.AQUA + s
            );
        }

        lore.add("");

        lore.add(
            ChatColor.DARK_AQUA + "Time: "
                + ChatColor.GRAY
                + vg.civcraft.mc.civmodcore.utilities.TextUtil
                    .formatDuration(
                        getProductionTime() * 50,
                        java.util.concurrent.TimeUnit.MILLISECONDS
                    )
        );

        im.setLore(lore);
        res.setItemMeta(im);

        return res;
    }

    @Override
    public List<ItemStack> getOutputRepresentation(
        Inventory i,
        FurnCraftChestFactory fccf
    ) {
        List<ItemStack> stacks = new ArrayList<>();

        stacks.add(
            new ItemStack(
                Material.WRITTEN_BOOK,
                this.outputAmount
            )
        );

        stacks.add(
            getPrintingPlateRepresentation(
                this.printingPlate,
                PrintingPlateRecipe.itemName
            )
        );

        if (i == null) {
            return stacks;
        }

        int possibleRuns = input.getMultiplesContainedIn(i);

        for (ItemStack is : stacks) {
            ItemUtils.addLore(
                is,
                ChatColor.GREEN + "Enough materials for "
                    + String.valueOf(possibleRuns)
                    + " runs"
            );
        }

        return stacks;
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.WRITTEN_BOOK;
    }

    protected ItemStack getPrintingPlateItemStack(
        Inventory i,
        ItemMap printingPlate
    ) {
        ItemMap items =
            new ItemMap(i).getStacksByMaterial(
                printingPlate
                    .getItemStackRepresentation()
                    .get(0)
                    .getType()
            );

        for (ItemStack is : items.getItemStackRepresentation()) {
            ItemMeta itemMeta = is.getItemMeta();

            if (itemMeta.getDisplayName().equals(
                    PrintingPlateRecipe.itemName
                )
                && itemMeta.hasEnchant(Enchantment.UNBREAKING)
            ) {
                return is;
            }
        }

        return null;
    }

    @Override
    public String getTypeIdentifier() {
        return "PRINTBOOK";
    }

    @Override
    public List<String> getTextualOutputRepresentation(
        Inventory i,
        FurnCraftChestFactory fccf
    ) {
        ItemStack is =
            new ItemStack(
                Material.WRITTEN_BOOK,
                outputAmount
            );

        return formatLore(new ItemMap(is));
    }
}