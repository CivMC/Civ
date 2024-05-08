package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

import javax.annotation.Nullable;

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
		return this.input.isContainedIn(inputInv) && getPrintingPlateItemStack(inputInv, this.printingPlate) != null;
	}

	@Override
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
		logBeforeRecipeRun(combo, fccf);

		ItemStack printingPlateStack = getPrintingPlateItemStack(inputInv, this.printingPlate);
		ItemMap toRemove = this.input.clone();

		if (printingPlateStack != null
				&& toRemove.isContainedIn(inputInv)
				&& toRemove.removeSafelyFrom(inputInv)
		) {
			ItemStack book = createBook(printingPlateStack, this.outputAmount);
			book.editMeta(BookMeta.class, x -> x.setGeneration(getNextGeneration(x.getGeneration())));
			outputInv.addItem(book);
		}

		logAfterRecipeRun(combo, fccf);
		return true;
	}

	protected ItemStack createBook(ItemStack printingPlateStack, int amount) {
		net.minecraft.world.item.ItemStack book = CraftItemStack.asNMSCopy(printingPlateStack);
		CompoundTag tag = book.getTag().getCompound("Book");

		ItemStack bookItem = new ItemStack(Material.WRITTEN_BOOK, amount);
		net.minecraft.world.item.ItemStack newBook = CraftItemStack.asNMSCopy(bookItem);
		newBook.setTag(tag);

		return CraftItemStack.asBukkitCopy(newBook);
	}

	protected BookMeta.Generation getNextGeneration(@Nullable BookMeta.Generation generation) {
		if (generation == null) generation = BookMeta.Generation.ORIGINAL;
		return switch (generation) {
			case ORIGINAL -> BookMeta.Generation.COPY_OF_ORIGINAL;
			case COPY_OF_ORIGINAL -> BookMeta.Generation.COPY_OF_COPY;
			default -> BookMeta.Generation.TATTERED;
		};
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

		if (printingPlateStack != null) {
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
			ItemUtils.addLore(is, ChatColor.GREEN + "Enough materials for "
					+ String.valueOf(possibleRuns) + " runs");
		}

		return stacks;
	}

	@Override
	public Material getRecipeRepresentationMaterial() {
		return Material.WRITTEN_BOOK;
	}

	protected ItemStack getPrintingPlateItemStack(Inventory i, ItemMap printingPlate) {
		ItemMap items = new ItemMap(i).getStacksByMaterial(printingPlate.getItemStackRepresentation().get(0).getType());

		for (ItemStack is : items.getItemStackRepresentation()) {
			ItemMeta itemMeta = is.getItemMeta();

			if (itemMeta.getDisplayName().equals(PrintingPlateRecipe.itemName)
					&& itemMeta.hasEnchant(Enchantment.DURABILITY)
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
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack is = new ItemStack(Material.WRITTEN_BOOK, outputAmount);
		return formatLore(new ItemMap(is));
	}
}
