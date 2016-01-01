package com.github.igotyou.FactoryMod.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.factories.Pipe;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;

public class MenuBuilder {
	private FactoryModManager manager;
	private Map<UUID, String> factoryViewed = new HashMap<UUID, String>();

	public MenuBuilder() {
		manager = FactoryMod.getManager();
	}

	public void openFactoryBrowser(Player p, String startingFac) {
		ClickableInventory.forceCloseInventory(p);
		IFactoryEgg egg = manager.getEgg(startingFac);
		if (egg == null) {
			p.sendMessage(ChatColor.RED
					+ "There is no factory with the name you entered");
			return;
		}
		if (egg instanceof FurnCraftChestEgg) {
			FurnCraftChestEgg furnegg = (FurnCraftChestEgg) egg;
			factoryViewed.put(p.getUniqueId(), furnegg.getName());
			ArrayList<Clickable> clickables = new ArrayList<Clickable>();
			clickables.ensureCapacity(27);

			// creation option
			ItemStack creationStack = new ItemStack(Material.CHEST);
			ItemStackUtils.setName(creationStack, "Setup");
			ItemStackUtils
					.addLore(
							creationStack,
							ChatColor.LIGHT_PURPLE
									+ "Click to display more information on how to setup this factory");
			Clickable creationClickable = new Clickable(creationStack) {
				@Override
				public void clicked(Player arg0) {
					openSetupBrowser(arg0,
							factoryViewed.get(arg0.getUniqueId()));
				}
			};
			clickables.set(10, creationClickable);

			// recipe option
			ItemStack recipeStack = new ItemStack(Material.WORKBENCH);
			ItemStackUtils.setName(recipeStack, "Recipes");
			ItemStackUtils.addLore(recipeStack, ChatColor.LIGHT_PURPLE
					+ "Click to display all recipes this factory can run");
			Clickable recipeClickable = new Clickable(recipeStack) {
				@Override
				public void clicked(Player arg0) {
					openRecipeBrowser(arg0,
							factoryViewed.get(arg0.getUniqueId()));
				}
			};
			clickables.set(13, recipeClickable);

			// upgrade option
			ItemStack upgradeStack = new ItemStack(Material.FURNACE);
			ItemStackUtils.setName(upgradeStack, "Upgrades");
			ItemStackUtils
					.addLore(upgradeStack,
							"Click to display more information about the possible upgrades to this factory");
			Clickable upgradeClickable = new Clickable(upgradeStack) {
				@Override
				public void clicked(Player arg0) {
					openUpgradeBrowser(arg0,
							factoryViewed.get(arg0.getUniqueId()));
				}
			};
			clickables.set(16, upgradeClickable);

			ClickableInventory browser = new ClickableInventory(
					clickables,
					InventoryType.CHEST,
					furnegg.getName()
							+ "  --- Click on an option to display more information");
			browser.showInventory(p);
		}

	}

	private void openRecipeBrowser(Player p, String facName) {
		ClickableInventory.forceCloseInventory(p);
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager.getEgg(facName);
		ArrayList<Clickable> clickables = new ArrayList<Clickable>();
		clickables.ensureCapacity(36);
		List<IRecipe> recipes = egg.getRecipes();

		// put recipes
		for (int i = 0; i < recipes.size(); i++) {
			Clickable c = new Clickable(
					((InputRecipe) recipes.get(i)).getRecipeRepresentation()) {
				@Override
				public void clicked(Player arg0) {
					openDetailedRecipeBrowser(arg0,
							ItemStackUtils.getName(this.getItemStack()));
				}
			};
			clickables.set(i, c);
		}

		// back option
		ItemStack backStack = new ItemStack(Material.ARROW);
		ItemStackUtils.setName(backStack, "Back to factory overview");
		ItemStackUtils.addLore(backStack, "Click to go back");
		Clickable backClickable = new Clickable(backStack) {
			@Override
			public void clicked(Player arg0) {
				openFactoryBrowser(arg0, factoryViewed.get(arg0.getUniqueId()));
			}
		};
		clickables.set(31, backClickable);

		ClickableInventory recipeInventory = new ClickableInventory(clickables,
				36, "All recipes for " + facName
						+ " --- Click one to display more information about it");
		recipeInventory.showInventory(p);
	}

	private void openSetupBrowser(Player p, String facName) {
		ClickableInventory.forceCloseInventory(p);
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager
				.getEgg(factoryViewed.get(p.getUniqueId()));

	}

	private void openUpgradeBrowser(Player p, String facName) {
		ClickableInventory.forceCloseInventory(p);
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager
				.getEgg(factoryViewed.get(p.getUniqueId()));
		ClickableInventory ci = new ClickableInventory(
				new ArrayList<Clickable>(), 18,
				"Click to display more information on an upgrade");
		List<IRecipe> upgrades = new LinkedList<IRecipe>();
		for (IRecipe recipe : egg.getRecipes()) {
			if (recipe instanceof Upgraderecipe) {
				upgrades.add(recipe);
			}
		}
		if (upgrades.size() == 0) {
			ItemStack bar = new ItemStack(Material.BARRIER);
			ItemStackUtils.setName(bar, "No upgrades available");
			ItemStackUtils.addLore(bar, "Click to go back");
			Clickable noUpgrades = new Clickable(bar) {
				@Override
				public void clicked(Player p) {
					openUpgradeBrowser(p, factoryViewed.get(p.getUniqueId()));
				}
			};
			ci.setSlot(noUpgrades, 4);
		} else {
			for (IRecipe recipe : upgrades) {
				Clickable c= new Clickable(((InputRecipe)recipe).getRecipeRepresentation()) {
					@Override
					public void clicked(Player p) {
						openDetailedRecipeBrowser(p, ItemStackUtils.getName(this.getItemStack()));
					}
				};
				ci.addSlot(c);
			}
		}
		ci.showInventory(p);
	}

	private void openDetailedRecipeBrowser(Player p, String recipeName) {
		ClickableInventory.forceCloseInventory(p);
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager
				.getEgg(factoryViewed.get(p.getUniqueId()));
		InputRecipe rec = null;
		for (IRecipe recipe : egg.getRecipes()) {
			if (recipe.getRecipeName().equals(recipeName)) {
				rec = (InputRecipe) recipe;
				break;
			}
		}
		ClickableInventory ci = new ClickableInventory(
				new ArrayList<Clickable>(), 54, recipeName + " in factory "
						+ factoryViewed.get(p.getUniqueId()));
		ItemStack inputStack = new ItemStack(Material.PAPER);
		ItemStackUtils.setName(inputStack, "Input materials");
		ItemStackUtils.addLore(inputStack,
				"The materials required to run this recipe");
		Clickable inputClickable = new Clickable(inputStack) {
			@Override
			public void clicked(Player arg0) {
			}
		};
		ci.setSlot(inputClickable, 4);
		int index = 13;
		for (ItemStack is : rec.getInputRepresentation(null)) {
			Clickable c = new DecorationStack(is);
			ci.setSlot(c, index);
			// weird math to fill up the gui nicely
			if ((index % 9) == 4) {
				index++;
				continue;
			}
			if ((index % 9) > 4) {
				index -= (((index % 9) - 4) * 2);
			} else {
				if ((index % 9) == 0) {
					index += 9;
				} else {
					index += (((4 - (index % 9)) * 2) + 1);
				}
			}

		}

		ItemStack outputStack = new ItemStack(Material.PAPER);
		ItemStackUtils.setName(outputStack, "Output/effect");
		Clickable outputClickable = new Clickable(outputStack) {
			@Override
			public void clicked(Player arg0) {
			}
		};

		ItemStack backStack = new ItemStack(Material.ARROW);
		ItemStackUtils.setName(backStack, "Back to recipe overview");
		ItemStackUtils.addLore(backStack, "Click to go back");
		Clickable backClickable = new Clickable(backStack) {
			@Override
			public void clicked(Player arg0) {
				openRecipeBrowser(arg0, factoryViewed.get(arg0.getUniqueId()));
			}
		};
		ci.setSlot(backClickable, 27);

		ci.setSlot(outputClickable, 31);
		index = 40;
		for (ItemStack is : rec.getOutputRepresentation(null)) {
			Clickable c = new DecorationStack(is);
			ci.setSlot(c, index);
			if ((index % 9) == 4) {
				index++;
				continue;
			}
			if ((index % 9) > 4) {
				index -= (((index % 9) - 4) * 2);
			} else {
				if ((index % 9) == 0) {
					index += 9;
				} else {
					index += (((4 - (index % 9)) * 2) + 1);
				}
			}
		}
		ci.showInventory(p);
	}
	
	public void showPipeMaterials(Player p, Pipe pipe) {
		
	}

}
