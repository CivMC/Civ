package com.github.igotyou.FactoryMod.utility;

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
import vg.civcraft.mc.civmodcore.inventorygui.ScheduledInventoryOpen;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.factories.Pipe;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;

public class MenuBuilder {
	private FactoryModManager manager;
	private Map<UUID, String> factoryViewed = new HashMap<UUID, String>();

	private Map<UUID, Pipe> pipeViewed = new HashMap<UUID, Pipe>();

	// child is key, parent is value
	private Map<String, String> parentFactories = new HashMap<String, String>();

	public MenuBuilder() {
		manager = FactoryMod.getManager();
		for (IFactoryEgg egg : manager.getAllEggs().values()) {
			if (egg instanceof FurnCraftChestEgg) {
				FurnCraftChestEgg furnegg = (FurnCraftChestEgg) egg;
				for (IRecipe rec : furnegg.getRecipes()) {
					if (rec instanceof Upgraderecipe) {
						parentFactories.put(egg.getName(),
								((Upgraderecipe) rec).getEgg().getName());
					}
				}
			}
		}
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
			ClickableInventory browser = new ClickableInventory(
					InventoryType.CHEST, furnegg.getName());
			// creation option
			ItemStack creationStack = new ItemStack(Material.CHEST);
			ItemStackUtils.setName(creationStack, "Setup");
			ItemStackUtils.addLore(creationStack, ChatColor.LIGHT_PURPLE
					+ "Click to display more information",
					ChatColor.LIGHT_PURPLE + "on how to setup this factory");
			Clickable creationClickable = new Clickable(creationStack) {
				@Override
				public void clicked(Player arg0) {
					openSetupBrowser(arg0,
							factoryViewed.get(arg0.getUniqueId()));
				}
			};
			browser.setSlot(creationClickable, 10);

			// recipe option
			ItemStack recipeStack = new ItemStack(Material.WORKBENCH);
			ItemStackUtils.setName(recipeStack, "Recipes");
			ItemStackUtils.addLore(recipeStack, ChatColor.LIGHT_PURPLE
					+ "Click to display all recipes", ChatColor.LIGHT_PURPLE
					+ "this factory can run");
			Clickable recipeClickable = new Clickable(recipeStack) {
				@Override
				public void clicked(Player arg0) {
					openRecipeBrowser(arg0,
							factoryViewed.get(arg0.getUniqueId()));
				}
			};
			browser.setSlot(recipeClickable, 13);

			// upgrade option
			ItemStack upgradeStack = new ItemStack(Material.FURNACE);
			ItemStackUtils.setName(upgradeStack, "Upgrades");
			ItemStackUtils.addLore(upgradeStack, ChatColor.LIGHT_PURPLE
					+ "Click to display more information about",
					ChatColor.LIGHT_PURPLE
							+ "the possible upgrades to this factory");
			Clickable upgradeClickable = new Clickable(upgradeStack) {
				@Override
				public void clicked(Player arg0) {
					openUpgradeBrowser(arg0,
							factoryViewed.get(arg0.getUniqueId()));
				}
			};
			browser.setSlot(upgradeClickable, 16);

			ScheduledInventoryOpen.schedule(FactoryMod.getPlugin(), browser, p);
		}

	}

	private void openRecipeBrowser(Player p, String facName) {
		ClickableInventory.forceCloseInventory(p);
		ClickableInventory recipeInventory = new ClickableInventory(36,
				"All recipes for " + facName);
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager.getEgg(facName);
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
			recipeInventory.setSlot(c, i);
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
		recipeInventory.setSlot(backClickable, 31);
		ScheduledInventoryOpen.schedule(FactoryMod.getPlugin(),
				recipeInventory, p);
	}

	private void openSetupBrowser(Player p, String facName) {
		ClickableInventory.forceCloseInventory(p);
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager
				.getEgg(factoryViewed.get(p.getUniqueId()));
		FurnCraftChestEgg parEgg = (FurnCraftChestEgg) manager
				.getEgg(parentFactories.get(egg.getName()));
		ClickableInventory ci = new ClickableInventory(54, "How to get a "
				+ egg.getName());
		ItemStack cr = new ItemStack(Material.WORKBENCH);
		ItemStack fur = new ItemStack(Material.FURNACE);
		ItemStack che = new ItemStack(Material.CHEST);
		if (parEgg == null) {// creation factory
			ItemStackUtils.setLore(cr, "This factory can be created with",
					"a normal crafting table, furnace and chest");
			ItemStackUtils.setLore(che, "Arrange the 3 blocks like this,",
					"put the materials below in the chest",
					"and hit the craftingtable with a stick");
			DecorationStack furnDec = new DecorationStack(fur);
			DecorationStack chestDec = new DecorationStack(che);
			DecorationStack craStack = new DecorationStack(cr);
			ci.setSlot(furnDec, 12);
			ci.setSlot(craStack, 13);
			ci.setSlot(chestDec, 14);
			ItemMap im = manager.getSetupCost(FurnCraftChestStructure.class,
					egg.getName());
			int slot = 36;
			for (ItemStack is : im.getItemStackRepresentation()) {
				DecorationStack dec = new DecorationStack(is);
				ci.setSlot(dec, slot);
				slot++;
			}
		} else {
			Upgraderecipe rec = null;
			for (IRecipe reci : parEgg.getRecipes()) {
				if (reci instanceof Upgraderecipe
						&& ((Upgraderecipe) rec).getEgg().equals(egg)) {
					rec = (Upgraderecipe) reci;
				}
			}

			ItemStackUtils.setLore(cr, "Upgrade from a " + parEgg.getName());
			Clickable craCli = new Clickable(cr) {
				@Override
				public void clicked(Player arg0) {
					openFactoryBrowser(arg0, parentFactories.get(factoryViewed
							.get(arg0.getUniqueId())));
				}
			};
			ci.setSlot(craCli, 13);
			Clickable furCli = new Clickable(fur) {
				@Override
				public void clicked(Player arg0) {
					openFactoryBrowser(arg0, parentFactories.get(factoryViewed
							.get(arg0.getUniqueId())));
				}
			};
			ci.setSlot(furCli, 12);
			Clickable cheCli = new Clickable(che) {
				@Override
				public void clicked(Player arg0) {
					openFactoryBrowser(arg0, parentFactories.get(factoryViewed
							.get(arg0.getUniqueId())));
				}
			};
			ci.setSlot(cheCli, 14);
			int slot = 36;
			for (ItemStack is : rec.getInput().getItemStackRepresentation()) {
				DecorationStack dec = new DecorationStack(is);
				ci.setSlot(dec, slot);
				slot++;
			}
		}
		ScheduledInventoryOpen.schedule(FactoryMod.getPlugin(), ci, p);
	}

	private void openUpgradeBrowser(Player p, String facName) {
		ClickableInventory.forceCloseInventory(p);
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager
				.getEgg(factoryViewed.get(p.getUniqueId()));
		ClickableInventory ci = new ClickableInventory(18, "Possible upgrades");
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
					openFactoryBrowser(p, factoryViewed.get(p.getUniqueId()));
				}
			};
			ci.setSlot(noUpgrades, 4);
		} else {
			for (IRecipe recipe : upgrades) {
				Clickable c = new Clickable(
						((InputRecipe) recipe).getRecipeRepresentation()) {
					@Override
					public void clicked(Player p) {
						openDetailedRecipeBrowser(p,
								ItemStackUtils.getName(this.getItemStack()));
					}
				};
				ci.addSlot(c);
			}
		}
		ScheduledInventoryOpen.schedule(FactoryMod.getPlugin(), ci, p);
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
		ClickableInventory ci = new ClickableInventory(54, recipeName);
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
		ScheduledInventoryOpen.schedule(FactoryMod.getPlugin(), ci, p);
	}

	public void showPipeMaterials(Player p, Pipe pipe) {
		pipeViewed.put(p.getUniqueId(), pipe);
		showPipeMaterialPart(p, pipe, 0);
	}

	private void showPipeMaterialPart(Player p, Pipe pipe, int start) {
		List<Material> mats = pipe.getAllowedMaterials();
		if (mats == null) {
			p.sendMessage(ChatColor.RED
					+ "No allowed materials specified for this pipe");
			return;
		}
		ClickableInventory ci = new ClickableInventory(54,
				"Currently allowed materials");
		for (int i = start; i < mats.size() && i < (start + 45); i++) {
			ItemStack is = new ItemStack(mats.get(i));
			Clickable c = new Clickable(is) {
				@Override
				public void clicked(Player arg0) {
					pipeViewed.get(arg0.getUniqueId()).removeAllowedMaterial(
							this.getItemStack().getType());
					arg0.sendMessage(ChatColor.GOLD + "Removed "
							+ this.getItemStack().getType()
							+ " as allowed material");
				}
			};
			ci.addSlot(c);
		}
		if (mats.size() >= (start + 45)) {
			ItemStack nextPage = new ItemStack(Material.ARROW);
			ItemStackUtils.setName(nextPage, "Next page");
			ItemStackUtils.addLore(nextPage, ChatColor.LIGHT_PURPLE
					+ "Click to show entries upwards from " + (start + 45));

			Clickable nextClick = new Clickable(nextPage) {
				@Override
				public void clicked(Player arg0) {
					showPipeMaterialPart(
							arg0,
							pipeViewed.get(arg0.getUniqueId()),
							Integer.valueOf(this.getItemStack().getItemMeta()
									.getLore().get(0).split(" ")[7]));
				}
			};
			ci.setSlot(nextClick, 49);
		}
		ScheduledInventoryOpen.schedule(FactoryMod.getPlugin(), ci, p);
	}

}
