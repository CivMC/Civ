/**
 * @author Aleksey Terzi
 */

package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.DataComponents;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public class PrintNoteRecipe extends PrintBookRecipe {

    private static class BookInfo {

        public String title;
        public List<String> lines;
    }

    private static final String pamphletName = "Pamphlet";
    private static final String secureNoteName = "Secure Note";

    private boolean secureNote;
    private String title;

    public boolean isSecurityNote() {
        return this.secureNote;
    }

    public String getTitle() {
        return this.title;
    }

    public PrintNoteRecipe(
        String identifier,
        String name,
        int productionTime,
        ItemMap input,
        ItemMap printingPlate,
        int outputAmount,
        boolean secureNote,
        String title
    ) {
        super(identifier, name, productionTime, input, printingPlate, outputAmount);

        this.secureNote = secureNote;

        if (title != null && title.length() > 0) {
            this.title = title;
        } else {
            this.title = secureNote ? secureNoteName : pamphletName;
        }
    }

    @Override
    public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
        MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
        logBeforeRecipeRun(combo, fccf);

        ItemStack printingPlateStack = getPrintingPlateItemStack(inputInv, getPrintingPlate());
        ItemMap toRemove = this.input.clone();

        if (printingPlateStack != null
            && toRemove.isContainedIn(inputInv)
            && toRemove.removeSafelyFrom(inputInv)
        ) {
            BookInfo info = getBookInfo(printingPlateStack);
            ItemStack paper = new ItemStack(Material.PAPER, getOutputAmount());

            ItemMeta paperMeta = paper.getItemMeta();
            paperMeta.setDisplayName(ChatColor.RESET + info.title);
            paperMeta.setLore(info.lines);
            paper.setItemMeta(paperMeta);

            outputInv.addItem(paper);
        }

        logAfterRecipeRun(combo, fccf);
        return true;
    }

    private BookInfo getBookInfo(ItemStack printingPlateStack) {
        ItemStack book = createBook(printingPlateStack, 1);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        int version = getVersion(printingPlateStack);
        String text = bookMeta.getPageCount() > 0 ?
            version == 0 ? bookMeta.getPage(1) : String.join("", bookMeta.getPages())
            : "";
        String[] lines = text.split("\n");
        List<String> fixedLines = new ArrayList<>();

        for (String line : lines) {
            fixedLines.add(ChatColor.GRAY + line
                .replaceAll(ChatColor.RESET.toString(), ChatColor.GRAY.toString()));
        }

        String bookTitle = bookMeta.getTitle();

        BookInfo info = new BookInfo();
        info.lines = fixedLines;
        info.title = bookTitle != null && !bookTitle.isEmpty() ? bookTitle : this.title;

        if (this.secureNote) {
            net.minecraft.world.item.ItemStack bookItem = CraftItemStack.asNMSCopy(printingPlateStack);
            String bookSN = bookItem.get(DataComponents.CUSTOM_DATA).copyTag().getString("SN");
            info.lines.add(bookSN);
        }

        return info;
    }

    private int getVersion(ItemStack item) {
        var book = CraftItemStack.asNMSCopy(item);
        var tag = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (tag != null) return tag.generation();
        return 0;
    }

    @Override
    public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
        ItemStack paper = new ItemStack(Material.PAPER, getOutputAmount());
        ItemUtils.setDisplayName(paper, this.title);

        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(paper);
        stacks.add(getPrintingPlateRepresentation(getPrintingPlate(), PrintingPlateRecipe.itemName));

        if (i == null) {
            return stacks;
        }

        int possibleRuns = input.getMultiplesContainedIn(i);

        for (ItemStack is : stacks) {
            ItemUtils.addLore(is, ChatColor.GREEN + "Enough materials for "
                + String.valueOf(possibleRuns) + " runs");
        }

        return stacks;
    }

    @Override
    public ItemStack getRecipeRepresentation() {
        ItemStack res = new ItemStack(Material.PAPER);

        ItemUtils.setDisplayName(res, getName());

        return res;
    }

    @Override
    public String getTypeIdentifier() {
        return "PRINTNOTE";
    }
}
