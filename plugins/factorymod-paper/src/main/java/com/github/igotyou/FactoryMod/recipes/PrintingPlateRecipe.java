/**
 * @author Aleksey Terzi
 *
 */

package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.TagManager;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

public class PrintingPlateRecipe extends PrintingPressRecipe {
	public static final String itemName = "Printing Plate";
	
	private ItemMap output;
	
	public ItemMap getOutput() {
		return this.output;
	}
	
	public PrintingPlateRecipe(String identifier, String name, int productionTime, ItemMap input, ItemMap output) {
		super(identifier, name, productionTime, input);
		this.output = output;
	}	

	public boolean enoughMaterialAvailable(Inventory i) {
		return this.input.isContainedIn(i) && getBook(i) != null;
	}

	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		
		ItemStack book = getBook(i);
		BookMeta bookMeta = (BookMeta)book.getItemMeta();
		String serialNumber = UUID.randomUUID().toString();
		
		ItemMap toRemove = input.clone();
		ItemMap toAdd = output.clone();
		
		if (toRemove.isContainedIn(i) && toRemove.removeSafelyFrom(i)) {
			for(ItemStack is: toAdd.getItemStackRepresentation()) {
				is = addTags(i, serialNumber, is);
				
				ISUtils.setName(is, itemName);
				ISUtils.setLore(is,
						serialNumber,
						ChatColor.WHITE + bookMeta.getTitle(),
						ChatColor.GRAY + "by " + bookMeta.getAuthor(),
						ChatColor.GRAY + getGenerationName(bookMeta.getGeneration())
						);
				
				i.addItem(is);
			}
		}
		
		logAfterRecipeRun(i, fccf);
	}
	
	private ItemStack addTags(Inventory i, String serialNumber, ItemStack is) {
		ItemStack book = getBook(i);
		TagManager bookTag = new TagManager(book);
		TagManager isTag = new TagManager(is);
		
		isTag.setString("SN", serialNumber);
		isTag.setCompound("Book", bookTag);
		
		addEnchTag(isTag);
		
		return isTag.enrichWithNBT(is);
	}
	
	private static String getGenerationName(Generation gen) {
		switch(gen) {
		case ORIGINAL: return "Original";
		case COPY_OF_ORIGINAL: return "Copy of Original";
		case COPY_OF_COPY: return "Copy of Copy";
		case TATTERED: return "Tattered";
		default: return "";
		}
	}
	
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> result = new LinkedList<ItemStack>();
		
		if (i == null) {
			result.add(new ItemStack(Material.WRITTEN_BOOK, 1));
			result.addAll(this.input.getItemStackRepresentation());
			return result;
		}
		
		result = createLoredStacksForInfo(i);
		
		ItemStack book = getBook(i);
		
		if(book != null) {
			result.add(book.clone());
		}
		
		return result;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		stacks.add(getPrintingPlateRepresentation(this.output, itemName));
		stacks.add(new ItemStack(Material.WRITTEN_BOOK));
		
		if (i == null) {
			return stacks;
		}
		
		int possibleRuns = this.input.getMultiplesContainedIn(i);
		
		for (ItemStack is : stacks) {
			ISUtils.addLore(is, ChatColor.GREEN + "Enough materials for "
					+ String.valueOf(possibleRuns) + " runs");
		}
		
		return stacks;
	}

	public ItemStack getRecipeRepresentation() {
		return getPrintingPlateRepresentation(this.output, getName());
	}
	
	private ItemStack getBook(Inventory i) {
		for (ItemStack is : i.getContents()) {
			if (is != null && is.getType() == Material.WRITTEN_BOOK) {
				return is;
			}
		}
		
		return null;
	}
	
	@Override
	public String getTypeIdentifier() {
		return "PRINTINGPLATE";
	}
}