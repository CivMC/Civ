/**
 * @author Aleksey Terzi
 *
 */

package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.TagReader;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

public class PrintingPlateRecipe extends InputRecipe {
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
		Map<String, Object> tags = createTags(i, serialNumber);
		
		ItemMap toRemove = input.clone();
		ItemMap toAdd = output.clone();
		
		if (toRemove.isContainedIn(i) && toRemove.removeSafelyFrom(i)) {
			for(ItemStack is: toAdd.getItemStackRepresentation()) {
				ISUtils.setName(is, itemName);
				ISUtils.setLore(is,
						serialNumber,
						ChatColor.WHITE + bookMeta.getTitle(),
						ChatColor.GRAY + "by " + bookMeta.getAuthor(),
						ChatColor.GRAY + getGenerationName(bookMeta.getGeneration())
						);
				
				is = ItemMap.enrichWithNBT(is, is.getAmount(), tags);

				i.addItem(is);
			}
		}
		
		logAfterRecipeRun(i, fccf);
	}
	
	private Map<String, Object> createTags(Inventory i, String serialNumber) {
		ItemStack book = getBook(i);
		TagReader reader = new TagReader(book);
		
		Map<String, Object> tags = new WeakHashMap<String, Object>();
		
		tags.put("sn", serialNumber);
		
		tags.put("Book.pages", reader.getStringList("pages"));
		tags.put("Book.author", reader.getString("author"));
		tags.put("Book.generation", reader.getInt("generation"));
		tags.put("Book.resolved", reader.getByte("resolved"));
		tags.put("Book.title", reader.getString("title"));
		
		addEnchTag(tags);
		
		return tags;
	}
	
	private static void addEnchTag(Map<String, Object> tags) {
		Map<String, Object> unb = new WeakHashMap<String, Object>();
		unb.put("id", (short)34);
		unb.put("lvl", (short)1);
		
		List<Object> ench = new ArrayList<Object>();
		ench.add(unb);
		
		tags.put("ench", ench);
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
	
	public static ItemStack getPrintingPlateRepresentation(ItemMap printingPlate, String name) {
		List<ItemStack> out = printingPlate.getItemStackRepresentation();
		ItemStack res = out.size() == 0 ? new ItemStack(Material.STONE) : out.get(0);
		
		Map<String, Object> tags = new WeakHashMap<String, Object>();
		addEnchTag(tags);
		
		res = ItemMap.enrichWithNBT(res, 1, tags);

		ISUtils.setName(res, name);
		
		return res;
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