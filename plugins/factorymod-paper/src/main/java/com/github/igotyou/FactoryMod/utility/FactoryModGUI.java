package com.github.igotyou.FactoryMod.utility;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.LClickable;
import vg.civcraft.mc.civmodcore.inventorygui.components.ComponableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.components.ComponableSection;
import vg.civcraft.mc.civmodcore.inventorygui.components.ContentAligners;
import vg.civcraft.mc.civmodcore.inventorygui.components.InventoryComponent;
import vg.civcraft.mc.civmodcore.inventorygui.components.Scrollbar;
import vg.civcraft.mc.civmodcore.inventorygui.components.SlotPredicates;
import vg.civcraft.mc.civmodcore.inventorygui.components.StaticDisplaySection;
import vg.civcraft.mc.civmodcore.inventorygui.history.HistoryItem;
import vg.civcraft.mc.civmodcore.inventorygui.history.HistoryTracker;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class FactoryModGUI {

	private Player player;
	private InventoryComponent topHalfComponent;
	private ComponableInventory inventory;
	private FurnCraftChestEgg currentFactory;
	private HistoryTracker<FMCHistoryItem> history;

	public FactoryModGUI(Player player) {
		this.player = player;
		this.history = new HistoryTracker<>();
	}

	public void showFactoryOverview(boolean addToHistory) {
		if (inventory == null) {
			inventory = new ComponableInventory(ChatColor.DARK_GREEN + "Factories", 6, player);
		} else {
			inventory.clear();
		}
		if (addToHistory) {
			history.add(new FMCHistoryItem() {

				@Override
				public void setStateTo() {
					showFactoryOverview(false);
				}

				@Override
				String toText() {
					return "Factory overview";
				}
			});
		}
		List<IClickable> clicks = new ArrayList<>();
		for (IFactoryEgg egg : FactoryMod.getInstance().getManager().getAllFactoryEggs()) {
			if (!(egg instanceof FurnCraftChestEgg)) {
				continue;
			}
			FurnCraftChestEgg fccEgg = (FurnCraftChestEgg) egg;
			InputRecipe firstRec = (InputRecipe) fccEgg.getRecipes().get(0);
			ItemStack is = new ItemStack(firstRec.getRecipeRepresentationMaterial());
			ItemAPI.setDisplayName(is, ChatColor.DARK_GREEN + fccEgg.getName());
			List<String> lore = new ArrayList<>();
			lore.add(ChatColor.DARK_AQUA + "Fuel: " + ChatColor.GRAY + ItemNames.getItemName(fccEgg.getFuel()));
			lore.add("");
			lore.add(ChatColor.GOLD + String.valueOf(fccEgg.getRecipes().size() + " recipes:"));
			for (IRecipe rec : fccEgg.getRecipes()) {
				if (rec instanceof Upgraderecipe) {
					lore.add(ChatColor.GRAY + " - " + ChatColor.GREEN + rec.getName());
				} else {
					lore.add(ChatColor.GRAY + " - " + ChatColor.AQUA + rec.getName());
				}
			}
			ItemAPI.addLore(is, lore);
			clicks.add(new LClickable(is, p -> {
				showForFactory(fccEgg);
			}));
		}
		Scrollbar middleBar = new Scrollbar(clicks, 45, 5, ContentAligners.getCenteredInOrder(clicks.size(), 45, 9));
		inventory.addComponent(middleBar, SlotPredicates.rows(5));
		StaticDisplaySection bottomLine = new StaticDisplaySection(9);
		inventory.addComponent(bottomLine, SlotPredicates.rows(1));
		inventory.show();
	}

	public void showForFactory(FurnCraftChestEgg factory) {
		showRecipeFor(factory, null, true);
	}

	private IClickable produceRecipeClickable(InputRecipe rec) {
		ItemStack is = rec.getRecipeRepresentation();
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
		FMCHistoryItem previous = history.peekPrevious();
		LClickable click = new LClickable(Material.SPECTRAL_ARROW, ChatColor.GOLD + "Show previous page", p -> {
			FMCHistoryItem actualPrevious = history.goBack();
			actualPrevious.setStateTo();
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
		if (inventory == null) {
			inventory = new ComponableInventory(ChatColor.DARK_GREEN + "Factories", 6, player);
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
		InventoryComponent updatedTopHalf;
		if (recipe == null) {
			updatedTopHalf = constructFactoryCreationComponent(factory);
		} else {
			updatedTopHalf = constructDetailedRecipeComponent(recipe);
		}
		if (currentFactory == factory) {
			if (history.peekBack(2) != null && history.peekBack(2).toText().startsWith("Re")) {
				// recipe before already, so keep recipe selector at bottom in current state
				inventory.removeComponent(this.topHalfComponent);
			} else {
				inventory.clear();
				Scrollbar recipeScroll = constructRecipeScrollbar(factory);
				inventory.addComponent(recipeScroll, SlotPredicates.offsetRectangle(1, 9, 5, 0));
			}
			this.topHalfComponent = updatedTopHalf;
			inventory.addComponent(updatedTopHalf, SlotPredicates.rows(5));
			inventory.update();
			inventory.updatePlayerView();
		} else {
			// reconstruct everything
			this.currentFactory = factory;
			inventory.clear();
			inventory.addComponent(updatedTopHalf, SlotPredicates.rows(6));
			inventory.show();
		}
	}

	private InventoryComponent constructFactoryCreationComponent(FurnCraftChestEgg factory) {
		ComponableSection section = new ComponableSection(54);

		ItemMap costs = null;
		if (factory.getSetupCost() != null) {
			costs = factory.getSetupCost();
		} else {
			FurnCraftChestEgg parent = getParent(factory);
			if (parent != null) {
				InputRecipe upgradeRecipe = getUpgradeRecipe(factory, parent);
				if (upgradeRecipe != null) {
					costs = upgradeRecipe.getInput();
				}
			}
		}
		List<IClickable> setupClicks = null;
		if (costs != null) {
			setupClicks = costs.getItemStackRepresentation().stream().map(DecorationStack::new)
					.collect(Collectors.toList());
		}
		int size = setupClicks == null ? 0 : setupClicks.size();
		Scrollbar inputSection = new Scrollbar(setupClicks, 24, 6, ContentAligners.getCenteredInOrder(size, 24, 4));
		// top right corner
		inputSection.setBackwardsClickSlot(4);
		section.addComponent(inputSection, SlotPredicates.rectangle(6, 4));

		IClickable setupClick = getSetupClick(factory);
		IClickable backClick = getBackClick();
		StaticDisplaySection middleLine = new StaticDisplaySection(setupClick, null, null, null, backClick);
		section.addComponent(middleLine, SlotPredicates.offsetRectangle(6, 1, 0, 4));

		List<IClickable> recipeClicks = factory.getRecipes().stream().map(i -> (InputRecipe) i)
				.map(this::produceRecipeClickable).collect(Collectors.toList());
		Scrollbar outputSection = new Scrollbar(recipeClicks, 24, 4,
				ContentAligners.getCenteredInOrder(recipeClicks.size(), 24, 4));
		outputSection.setBackwardsClickSlot(4);
		section.addComponent(outputSection, SlotPredicates.offsetRectangle(6, 4, 0, 5));

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

		IClickable setupClick = getSetupClick(this.currentFactory);
		IClickable backClick = getBackClick();
		IClickable recSumSup = new DecorationStack(recipe.getRecipeRepresentation());
		StaticDisplaySection middleLine = new StaticDisplaySection(setupClick, null, recSumSup, null, backClick);
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
