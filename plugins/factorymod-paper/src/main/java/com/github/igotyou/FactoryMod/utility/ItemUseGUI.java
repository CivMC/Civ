package com.github.igotyou.FactoryMod.utility;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.recipes.CompactingRecipe;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.LClickable;
import vg.civcraft.mc.civmodcore.inventorygui.components.ComponableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.components.ContentAligners;
import vg.civcraft.mc.civmodcore.inventorygui.components.Scrollbar;
import vg.civcraft.mc.civmodcore.inventorygui.components.SlotPredicates;
import vg.civcraft.mc.civmodcore.inventorygui.components.StaticDisplaySection;

import java.util.ArrayList;
import java.util.List;

public class ItemUseGUI {

	private Player player;
	private ComponableInventory inventory;

	public ItemUseGUI(Player player) {
		this.player = player;
	}

	/**
	 * Shows an interactive menu containing all factory recipes which take or produce a specified item
	 * @param item
	 */
	public void showItemOverview(ItemStack item) {
		if (inventory == null) {
			inventory = new ComponableInventory(ChatColor.DARK_GRAY
					+ "As Input            As Output", 6, player);
		}
		FactoryModGUI gui = new FactoryModGUI(player);
		List<IClickable> itemAsInput = new ArrayList<>();
		List<IClickable> itemAsOutput = new ArrayList<>();
		List<IFactoryEgg> eggList = new ArrayList<>(FactoryMod.getInstance().getManager().getAllFactoryEggs());
		for (IFactoryEgg egg : eggList) {
			if (!(egg instanceof FurnCraftChestEgg)) {
				continue;
			}
			FurnCraftChestEgg fccEgg = (FurnCraftChestEgg) egg;
			for (IRecipe recipe : fccEgg.getRecipes()) {
				if (!(recipe instanceof InputRecipe)) {
					continue;
				}
				InputRecipe inputRecipe = (InputRecipe) recipe;

				ItemStack getItemAsInput = getItemAsInput(fccEgg, inputRecipe, item);
				if (getItemAsInput != null) {
					itemAsInput.add(new LClickable(getItemAsInput, p -> {
						gui.showForFactory(fccEgg, inputRecipe);
					}));
				}
				ItemStack getItemAsOutput = getItemAsOutput(fccEgg, inputRecipe, item);
				if (getItemAsOutput != null) {
					itemAsOutput.add(new LClickable(getItemAsOutput, p -> {
						gui.showForFactory(fccEgg, inputRecipe);
					}));
				}
			}
		}
		if (itemAsInput.isEmpty()) {
			ItemStack noItems = new ItemStack(Material.BARRIER);
			ItemAPI.setDisplayName(noItems, ChatColor.RED + "No recipes take input " + ItemNames.getItemName(item));
			itemAsInput.add(new LClickable(noItems, p -> { }));
		}
		if (itemAsOutput.isEmpty()) {
			ItemStack noItems = new ItemStack(Material.BARRIER);
			ItemAPI.setDisplayName(noItems, ChatColor.RED + "No recipes output " + ItemNames.getItemName(item));
			itemAsOutput.add(new LClickable(noItems, p -> { }));
		}
		Scrollbar itemAsInputBar = new Scrollbar(itemAsInput, 24, 8, ContentAligners.getCenteredInOrder(itemAsInput.size(), 24, 4));
		itemAsInputBar.setBackwardsClickSlot(3);
		inventory.addComponent(itemAsInputBar, SlotPredicates.rectangle(6, 4));

		IClickable dividerClick = getDividerClick();
		StaticDisplaySection middleLine = new StaticDisplaySection(dividerClick, dividerClick, dividerClick, dividerClick, dividerClick , dividerClick);
		inventory.addComponent(middleLine, SlotPredicates.offsetRectangle(6, 1, 0, 4));

		Scrollbar itemAsOutputBar = new Scrollbar(itemAsOutput, 24, 5, ContentAligners.getCenteredInOrder(itemAsOutput.size(), 24, 4));
		inventory.addComponent(itemAsOutputBar,  SlotPredicates.offsetRectangle(6, 4, 0, 5));
		inventory.show();
	}

	private IClickable getDividerClick() {
		ItemStack is = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemAPI.setDisplayName(is, "Divider");
		return new LClickable(is, p -> { });
	}

	private ItemStack getItemAsInput(FurnCraftChestEgg fccEgg, InputRecipe recipe, ItemStack item) {
		if (recipe.getInput().getAmount(item) != 0) {
			return getItemRecipeStack(fccEgg, recipe, item);
		}
		return null;
	}

	private ItemStack getItemAsOutput(FurnCraftChestEgg fccEgg, InputRecipe recipe, ItemStack item) {
		if (recipe instanceof ProductionRecipe) {
			ProductionRecipe output = (ProductionRecipe) recipe;
			if (output.getOutput().getAmount(item) != 0) {
				return getItemRecipeStack(fccEgg, recipe, item);
			}
		}
		if (recipe instanceof CompactingRecipe) {
			CompactingRecipe output = (CompactingRecipe) recipe;
			if (String.join("", ItemAPI.getLore(item)).equals(output.getCompactedLore())) {
				return getItemRecipeStack(fccEgg, recipe, item);
			}
		}
		return null;
	}

	private ItemStack getItemRecipeStack(FurnCraftChestEgg fccEgg, InputRecipe recipe, ItemStack item) {
		ItemStack is = new ItemStack(recipe.getRecipeRepresentationMaterial());
		ItemAPI.setDisplayName(is, ChatColor.DARK_GREEN + fccEgg.getName());
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.DARK_AQUA + recipe.getName());
		lore.add(ChatColor.GOLD + "input:");
		for (String input : recipe.getTextualInputRepresentation(null, null)) {
			lore.add(formatIngredient(input, item));
		}
		lore.add(ChatColor.GOLD + "output:");
		for (String output : recipe.getTextualOutputRepresentation(null, null)) {
			lore.add(formatIngredient(output, item));
		}
		ItemAPI.addLore(is, lore);
		return is;
	}

	private String formatIngredient(String recipeRepresentation, ItemStack is) {
		if (recipeRepresentation.matches("\\d+ " + ItemNames.getItemName(is))) {
			return String.format("%s - %s%s%s", ChatColor.GRAY, ChatColor.AQUA, ChatColor.BOLD, recipeRepresentation);
		} else {
			return String.format("%s - %s%s", ChatColor.GRAY, ChatColor.AQUA, recipeRepresentation);
		}
	}
}
