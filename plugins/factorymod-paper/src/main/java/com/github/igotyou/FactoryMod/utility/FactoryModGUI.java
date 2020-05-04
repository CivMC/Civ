package com.github.igotyou.FactoryMod.utility;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.LClickable;
import vg.civcraft.mc.civmodcore.inventorygui.components.ComponableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.components.ComponableSection;
import vg.civcraft.mc.civmodcore.inventorygui.components.ContentAligner;
import vg.civcraft.mc.civmodcore.inventorygui.components.ContentAligners;
import vg.civcraft.mc.civmodcore.inventorygui.components.InventoryComponent;
import vg.civcraft.mc.civmodcore.inventorygui.components.Scrollbar;
import vg.civcraft.mc.civmodcore.inventorygui.components.SlotPredicates;
import vg.civcraft.mc.civmodcore.inventorygui.components.StaticDisplaySection;
import vg.civcraft.mc.civmodcore.inventorygui.history.HistoryItem;
import vg.civcraft.mc.civmodcore.inventorygui.history.HistoryTracker;

public class FactoryModGUI {

	private Player player;
	private InventoryComponent topHalfComponent;
	private ComponableInventory inventory;
	private FurnCraftChestEgg currentFactory;
	private static DecimalFormat formatter = new DecimalFormat("#####.#");
	private HistoryTracker<FMCHistoryItem> history;

	public FactoryModGUI(Player player) {
		this.player = player;
		this.history = new HistoryTracker<>();
	}
	
	public void showFactoryOverview() {
		
	}

	public void showForFactory(FurnCraftChestEgg factory) {
		inventory = new ComponableInventory(ChatColor.GOLD + factory.getName(), 6, player);
		showRecipeFor(factory, null, true);
	}

	private IClickable produceRecipeClickable(InputRecipe rec) {
		ItemStack is = rec.getRecipeRepresentation();
		ItemAPI.setDisplayName(is, ChatColor.GOLD + rec.getName());
		return new LClickable(is, p -> showRecipeFor(currentFactory, rec, true));
	}

	private IClickable getSetupClick(FurnCraftChestEgg factory) {
		ItemStack is = new ItemStack(Material.CRAFTING_TABLE);
		ItemAPI.setDisplayName(is, ChatColor.GOLD + "Show creation cost");
		FurnCraftChestEgg parent = getParent(factory);
		if (parent == null) {
			ItemAPI.addLore(is, ChatColor.GREEN + factory.getName() + " can be created directly");
		} else {
			ItemAPI.addLore(is, ChatColor.GREEN + factory.getName() + " is an upgrade of " + parent.getName());
		}
		return new LClickable(is, p -> showFactoryCreation(factory, true));
	}

	private IClickable getBackClick() {
		if (!history.hasPrevious()) {
			return null;
		}
		FMCHistoryItem previous = history.goBack();
		LClickable click = new LClickable(Material.ARROW, ChatColor.GOLD + "Show previous page", p -> {
			history.goBack();
			previous.setStateTo();
		});
		ItemAPI.addLore(click.getItemStack(), ChatColor.GREEN + previous.toText());
		return click;
	}

	private InputRecipe getUpgradeRecipe(FurnCraftChestEgg child, FurnCraftChestEgg parent) {
		return null;
	}

	private void showFactoryCreation(FurnCraftChestEgg factory, boolean addToHistory) {
		showRecipeFor(factory, null, addToHistory);
	}

	private void showRecipeFor(FurnCraftChestEgg factory, InputRecipe recipe, boolean addToHistory) {
		InventoryComponent updatedTopHalf;
		if (recipe == null) {
			updatedTopHalf = constructFactoryCreationComponent(factory);
		} else {
			updatedTopHalf = constructDetailedRecipeComponent(recipe);
		}
		if (currentFactory == factory) {
			inventory.removeComponent(this.topHalfComponent);
			this.topHalfComponent = updatedTopHalf;
			inventory.addComponent(updatedTopHalf, SlotPredicates.rows(5));
			inventory.update();
			inventory.updatePlayerView();
		} else {
			// reconstruct everything
			this.currentFactory = factory;
			inventory.clear();

			topHalfComponent = constructFactoryCreationComponent(factory);
			inventory.addComponent(topHalfComponent, SlotPredicates.rows(5));

			Scrollbar recipeScrollbar = constructRecipeScrollbar(factory);
			inventory.addComponent(recipeScrollbar, SlotPredicates.rows(1));

			inventory.show();
		}
		if (addToHistory) {
			if (recipe == null) {
				history.add(new FMCHistoryItem() {

					@Override
					public void setStateTo() {
						showFactoryCreation(factory, false);
					}

					@Override
					String toText() {
						return ChatColor.GREEN + "Setup for " + factory.getName();
					}
				});
			} else {
				history.add(new FMCHistoryItem() {

					@Override
					public void setStateTo() {
						showRecipeFor(factory, recipe, false);
					}

					@Override
					String toText() {
						return ChatColor.GREEN + "Recipe " + recipe.getName();
					}
				});
			}
		}
	}

	private static InventoryComponent constructFactoryCreationComponent(FurnCraftChestEgg factory) {
		ComponableSection section = new ComponableSection(45);

		return section;
	}

	private InventoryComponent constructDetailedRecipeComponent(InputRecipe recipe) {
		ComponableSection section = new ComponableSection(45);

		List<IClickable> inputClicks = recipe.getInputRepresentation(null, null).stream().map(DecorationStack::new)
				.collect(Collectors.toList());
		Scrollbar inputSection = new Scrollbar(inputClicks, 20, 4,
				ContentAligners.getCenteredInOrder(inputClicks.size(), 20, 4));
		// top right corner
		inputSection.setBackwardsClickSlot(4);
		section.addComponent(inputSection, SlotPredicates.rectangle(5, 4));

		IClickable recipeSumup = new DecorationStack(Material.PAPER, ChatColor.GOLD + recipe.getName(),
				ChatColor.GREEN + "Runtime: " + formatter.format(recipe.getProductionTime() / 20.0),
				ChatColor.AQUA + "Fuel consumption: " + recipe.getTotalFuelConsumed());
		IClickable setupClick = getSetupClick(this.currentFactory);
		IClickable backClick = getBackClick();
		StaticDisplaySection middleLine = new StaticDisplaySection(recipeSumup, null, setupClick, null, backClick);
		section.addComponent(middleLine, SlotPredicates.offsetRectangle(5, 1, 0, 4));

		List<IClickable> outputClicks = recipe.getOutputRepresentation(null, null).stream().map(DecorationStack::new)
				.collect(Collectors.toList());
		Scrollbar outputSection = new Scrollbar(outputClicks, 20, 4,
				ContentAligners.getCenteredInOrder(outputClicks.size(), 20, 4));
		outputSection.setBackwardsClickSlot(4);
		section.addComponent(outputSection, SlotPredicates.offsetRectangle(5, 4, 0, 5));

		return section;
	}

	private Scrollbar constructRecipeScrollbar(FurnCraftChestEgg factory) {
		List<IClickable> recipeClicks = factory.getRecipes().stream().map(i -> (InputRecipe) i)
				.map(this::produceRecipeClickable).collect(Collectors.toList());
		return new Scrollbar(recipeClicks, 9);
	}

	private FurnCraftChestEgg getParent(FurnCraftChestEgg factory) {
		return null;
	}

	private abstract class FMCHistoryItem implements HistoryItem {
		abstract String toText();
	}

}
