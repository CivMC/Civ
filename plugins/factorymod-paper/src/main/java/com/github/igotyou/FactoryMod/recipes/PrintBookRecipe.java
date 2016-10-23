package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.TagReader;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

public class PrintBookRecipe extends InputRecipe {
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
			) 
	{
		super(identifier, name, productionTime, input);
		this.printingPlate = printingPlate;
		this.outputAmount = outputAmount;
	}	

	public boolean enoughMaterialAvailable(Inventory i) {
		return this.input.isContainedIn(i) && getPrintingPlate(i, this.printingPlate) != null;
	}

	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);
		
		ItemStack printingPlateStack = getPrintingPlate(i, this.printingPlate);
		ItemMap toRemove = this.input.clone();

		if (printingPlateStack != null
				&& toRemove.isContainedIn(i)
				&& toRemove.removeSafelyFrom(i)
				)
		{
			ItemStack book = createBook(printingPlateStack, this.outputAmount);			
			i.addItem(book);
		}
		
		logAfterRecipeRun(i, fccf);
	}
	
	public static ItemStack createBook(ItemStack printingPlateStack, int amount) {
		TagReader reader = new TagReader(printingPlateStack);
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

		Map<String, Object> tags = new WeakHashMap<String, Object>();
		tags.put("pages", reader.getStringList("Book.pages"));
		tags.put("author", reader.getString("Book.author"));
		tags.put("generation", reader.getInt("Book.generation"));
		tags.put("resolved", reader.getByte("Book.resolved"));
		tags.put("title", reader.getString("Book.title"));
		
		return ItemMap.enrichWithNBT(book, amount, tags);
	}
	
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> result = new LinkedList<ItemStack>();

		if (i == null) {
			ItemStack is = PrintingPlateRecipe.getPrintingPlateRepresentation(this.printingPlate, PrintingPlateRecipe.itemName);
			
			result.addAll(this.input.getItemStackRepresentation());
			result.add(is);
			return result;
		}
		
		result = createLoredStacksForInfo(i);
		
		ItemStack printingPlateStack = getPrintingPlate(i, this.printingPlate);
		
		if(printingPlateStack != null) {
			result.add(printingPlateStack.clone());
		}
		
		return result;
	}

	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		stacks.add(new ItemStack(Material.WRITTEN_BOOK, this.outputAmount));
		stacks.add(PrintingPlateRecipe.getPrintingPlateRepresentation(this.printingPlate, PrintingPlateRecipe.itemName));

		if (i == null) {
			return stacks;
		}
		
		int possibleRuns = input.getMultiplesContainedIn(i);
		
		for (ItemStack is : stacks) {
			ISUtils.addLore(is, ChatColor.GREEN + "Enough materials for "
					+ String.valueOf(possibleRuns) + " runs");
		}
		
		return stacks;
	}

	public ItemStack getRecipeRepresentation() {
		ItemStack res = new ItemStack(Material.WRITTEN_BOOK);
		
		ISUtils.setName(res, getName());
		
		return res;
	}
	
	public static ItemStack getPrintingPlate(Inventory i, ItemMap printingPlate) {
		ItemMap items = new ItemMap(i).getStacksByMaterial(printingPlate.getItemStackRepresentation().get(0).getType());
		
		for(ItemStack is : items.getItemStackRepresentation()) {
			ItemMeta itemMeta = is.getItemMeta();
			
			if(itemMeta.getDisplayName().equals(PrintingPlateRecipe.itemName)
					&& itemMeta.hasEnchant(Enchantment.DURABILITY)
					)
			{
				return is;
			}
		}
		
		return null;
	}
	
	@Override
	public String getTypeIdentifier() {
		return "PRINTBOOK";
	}
}
