package com.github.igotyou.FactoryMod.recipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.TagManager;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

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
			) 
	{
		super(identifier, name, productionTime, input);
		this.printingPlate = printingPlate;
		this.outputAmount = outputAmount;
	}	

	@Override
	public boolean enoughMaterialAvailable(Inventory i) {
		return this.input.isContainedIn(i) && getPrintingPlateItemStack(i, this.printingPlate) != null;
	}

	@Override
	public void applyEffect(Inventory i, FurnCraftChestFactory fccf) {
		logBeforeRecipeRun(i, fccf);

		ItemStack printingPlateStack = getPrintingPlateItemStack(i, this.printingPlate);
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

	protected ItemStack createBook(ItemStack printingPlateStack, int amount) {
		TagManager printingPlateTag = new TagManager(printingPlateStack);
		TagManager bookTag = printingPlateTag.getCompound("Book");

		ItemStack book = new ItemStack(Material.WRITTEN_BOOK, amount);

		return bookTag.enrichWithNBT(book);
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> result = new LinkedList<>();

		if (i == null) {
			ItemStack is = getPrintingPlateRepresentation(this.printingPlate, PrintingPlateRecipe.itemName);

			result.addAll(this.input.getItemStackRepresentation());
			result.add(is);
			return result;
		}

		result = createLoredStacksForInfo(i);

		ItemStack printingPlateStack = getPrintingPlateItemStack(i, this.printingPlate);

		if(printingPlateStack != null) {
			result.add(printingPlateStack.clone());
		}

		return result;
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		List<ItemStack> stacks = new ArrayList<>();
		stacks.add(new ItemStack(Material.WRITTEN_BOOK, this.outputAmount));
		stacks.add(getPrintingPlateRepresentation(this.printingPlate, PrintingPlateRecipe.itemName));

		if (i == null) {
			return stacks;
		}

		int possibleRuns = input.getMultiplesContainedIn(i);

		for (ItemStack is : stacks) {
			ItemAPI.addLore(is, ChatColor.GREEN + "Enough materials for "
					+ String.valueOf(possibleRuns) + " runs");
		}

		return stacks;
	}

	@Override
	public ItemStack getRecipeRepresentation() {
		ItemStack res = new ItemStack(Material.WRITTEN_BOOK);

		ItemAPI.setDisplayName(res, getName());

		return res;
	}

	protected ItemStack getPrintingPlateItemStack(Inventory i, ItemMap printingPlate) {
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
