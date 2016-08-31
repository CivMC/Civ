package com.github.igotyou.FactoryMod.utility;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.civmodcore.itemHandling.NiceNames;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.Pipe;
import com.github.igotyou.FactoryMod.factories.Sorter;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.recipes.Upgraderecipe;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;

public class MenuBuilder {
	private FactoryModManager manager;
	private Map<UUID, String> factoryViewed = new HashMap<UUID, String>();

	private Map<UUID, Factory> viewed = new HashMap<UUID, Factory>();
	// child is key, parent is value
	private Map<String, String> parentFactories = new HashMap<String, String>();
	private DecorationStack input;
	private IFactoryEgg defaultMenu;

	public MenuBuilder(String defaultFactory) {
		manager = FactoryMod.getManager();
		for (IFactoryEgg egg : manager.getAllEggs().values()) {
			if (egg instanceof FurnCraftChestEgg) {
				FurnCraftChestEgg furnegg = (FurnCraftChestEgg) egg;
				for (IRecipe rec : furnegg.getRecipes()) {
					if (rec instanceof Upgraderecipe) {
						parentFactories.put(((Upgraderecipe) rec).getEgg()
								.getName(), egg.getName());
					}
				}
			}
		}
		ItemStack inp = new ItemStack(Material.PAPER);
		ISUtils.setName(inp, "Input");
		ISUtils.setLore(inp, ChatColor.LIGHT_PURPLE
				+ "The items below are required");
		input = new DecorationStack(inp);
		ItemStack outp = new ItemStack(Material.PAPER);
		ISUtils.setName(outp, "Output");
		ISUtils.setLore(outp, ChatColor.LIGHT_PURPLE
				+ "The output of this recipe");
		if (defaultFactory != null) {
			defaultMenu = manager.getEgg(defaultFactory);
		}
	}

	public void openFactoryBrowser(Player p, String startingFac) {
		IFactoryEgg egg;
		if (startingFac == null) {
			egg = defaultMenu;
			if (egg == null) {
				egg = manager.getAllEggs().values().iterator().next();
				// no default in config and nothing specified, so just a pick
				// any existing one
			}
		} else {
			egg = manager.getEgg(startingFac);
		}
		if (egg == null) {
			String comp = startingFac.toLowerCase();
			// check for lower/uppercase miss spellings
			for (Entry<String, IFactoryEgg> entry : manager.getAllEggs()
					.entrySet()) {
				if (entry.getKey().toLowerCase().equals(comp)) {
					egg = entry.getValue();
					break;
				}
			}
			if (egg == null) {
				FactoryMod.getPlugin().warning(
						"There is no factory with name " + comp);
				p.sendMessage(ChatColor.RED
						+ "There is no factory with the name you entered");
				return;
			}
		}
		if (egg instanceof FurnCraftChestEgg) {
			FurnCraftChestEgg furnegg = (FurnCraftChestEgg) egg;
			factoryViewed.put(p.getUniqueId(), furnegg.getName());
			ClickableInventory browser = new ClickableInventory(
					InventoryType.CHEST, furnegg.getName());
			// creation option
			ItemStack creationStack = new ItemStack(Material.CHEST);
			ISUtils.setName(creationStack, "Setup");
			ISUtils.addLore(creationStack, ChatColor.LIGHT_PURPLE
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
			ISUtils.setName(recipeStack, "Recipes");
			ISUtils.addLore(recipeStack, ChatColor.LIGHT_PURPLE
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
			ISUtils.setName(upgradeStack, "Upgrades");
			ISUtils.addLore(upgradeStack, ChatColor.LIGHT_PURPLE
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
			browser.showInventory(p);
		}
	}

	private void openRecipeBrowser(Player p, String facName) {
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager.getEgg(facName);
		List<IRecipe> recipes = egg.getRecipes();
		int size = (recipes.size() / 9) + 2;
		if ((recipes.size() % 9) == 0) {
			size--;
		}
		size *= 9;
		
		ClickableInventory recipeInventory = new ClickableInventory(size,
				"Recipes for " + facName); // Bukkit has 32 char limit on
											// inventory
		// put recipes
		int j = 0;
		for (int i = 0; i < recipes.size(); i++) {
			if (recipes.get(i) == null) {
				continue;
			}
			Clickable c = new Clickable(
					((InputRecipe) recipes.get(i)).getRecipeRepresentation()) {
				@Override
				public void clicked(Player arg0) {
					openDetailedRecipeBrowser(arg0,
							ISUtils.getName(this.getItemStack()));
				}
			};
			recipeInventory.setSlot(c, j++);
		}

		// back option
		ItemStack backStack = new ItemStack(Material.ARROW);
		ISUtils.setName(backStack, "Back to factory overview");
		ISUtils.addLore(backStack, ChatColor.LIGHT_PURPLE + "Click to go back");
		Clickable backClickable = new Clickable(backStack) {
			@Override
			public void clicked(Player arg0) {
				openFactoryBrowser(arg0, factoryViewed.get(arg0.getUniqueId()));
			}
		};
		recipeInventory.setSlot(backClickable, size - 5);
		recipeInventory.showInventory(p);
	}

	private void openSetupBrowser(Player p, String facName) {
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager.getEgg(facName);
		FurnCraftChestEgg parEgg = (FurnCraftChestEgg) manager
				.getEgg(parentFactories.get(facName));
		ClickableInventory ci = new ClickableInventory(54, "Create a "
				+ egg.getName()); // Bukkit has 32 char limit on inventory
		ItemStack cr = new ItemStack(Material.WORKBENCH);
		ItemStack fur = new ItemStack(Material.FURNACE);
		ItemStack che = new ItemStack(Material.CHEST);
		if (parEgg == null) {// creation factory
			ISUtils.setLore(cr, ChatColor.LIGHT_PURPLE
					+ "This factory can be created with",
					ChatColor.LIGHT_PURPLE
							+ "a normal crafting table, furnace and chest");
			ISUtils.setLore(che, ChatColor.LIGHT_PURPLE
					+ "Arrange the 3 blocks like this,", ChatColor.LIGHT_PURPLE
					+ "put the materials below in the chest",
					ChatColor.LIGHT_PURPLE
							+ "and hit the crafting table with a stick");
			DecorationStack furnDec = new DecorationStack(fur);
			DecorationStack chestDec = new DecorationStack(che);
			DecorationStack craStack = new DecorationStack(cr);
			ci.setSlot(furnDec, 3);
			ci.setSlot(craStack, 4);
			ci.setSlot(chestDec, 5);
			ItemMap im = manager.getSetupCost(FurnCraftChestStructure.class,
					egg.getName());
			int slot = 31;
			for (ItemStack is : im.getItemStackRepresentation()) {
				DecorationStack dec = new DecorationStack(is);
				ci.setSlot(dec, slot);
				if ((slot % 9) == 4) {
					slot++;
					continue;
				}
				if ((slot % 9) > 4) {
					slot -= (((slot % 9) - 4) * 2);
				} else {
					if ((slot % 9) == 0) {
						slot += 13;
					} else {
						slot += (((4 - (slot % 9)) * 2) + 1);
					}
				}
			}
		} else {
			Upgraderecipe rec = null;
			for (IRecipe reci : parEgg.getRecipes()) {
				if (reci instanceof Upgraderecipe
						&& ((Upgraderecipe) reci).getEgg().equals(egg)) {
					rec = (Upgraderecipe) reci;
				}
			}

			ISUtils.setLore(cr, ChatColor.LIGHT_PURPLE + "Upgrade from a "
					+ parEgg.getName());
			Clickable craCli = new Clickable(cr) {
				@Override
				public void clicked(Player arg0) {
					openFactoryBrowser(arg0, parentFactories.get(factoryViewed
							.get(arg0.getUniqueId())));
				}
			};
			ci.setSlot(craCli, 4);
			ISUtils.setLore(fur, ChatColor.LIGHT_PURPLE
					+ "Click to display information", ChatColor.LIGHT_PURPLE
					+ "on this factory");
			Clickable furCli = new Clickable(fur) {
				@Override
				public void clicked(Player arg0) {
					openFactoryBrowser(arg0, parentFactories.get(factoryViewed
							.get(arg0.getUniqueId())));
				}
			};
			ci.setSlot(furCli, 3);
			Clickable cheCli = new Clickable(che) {
				@Override
				public void clicked(Player arg0) {
					openFactoryBrowser(arg0, parentFactories.get(factoryViewed
							.get(arg0.getUniqueId())));
				}
			};
			ci.setSlot(cheCli, 5);
			int slot = 31;
			List <ItemStack> itms;
			if (rec.getInput().getItemStackRepresentation().size() > 27) {
				itms = rec.getInput().getLoredItemCountRepresentation();
			}
			else {
				itms = rec.getInput().getItemStackRepresentation();
			}
			for (ItemStack is : itms) {
				DecorationStack dec = new DecorationStack(is);
				ci.setSlot(dec, slot);
				if ((slot % 9) == 4) {
					slot++;
					continue;
				}
				if ((slot % 9) > 4) {
					slot -= (((slot % 9) - 4) * 2);
				} else {
					if ((slot % 9) == 0) {
						slot += 13;
					} else {
						slot += (((4 - (slot % 9)) * 2) + 1);
					}
				}
			}
		}
		ci.setSlot(input, 22);
		ItemStack backStack = new ItemStack(Material.ARROW);
		ISUtils.setName(backStack, "Back to factory overview");
		ISUtils.addLore(backStack, ChatColor.LIGHT_PURPLE + "Click to go back");
		Clickable backClickable = new Clickable(backStack) {
			@Override
			public void clicked(Player arg0) {
				openFactoryBrowser(arg0, factoryViewed.get(arg0.getUniqueId()));
			}
		};
		ci.setSlot(backClickable, 18);
		ci.showInventory(p);
	}

	private void openUpgradeBrowser(Player p, String facName) {
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager
				.getEgg(factoryViewed.get(p.getUniqueId()));
		List<IRecipe> upgrades = new LinkedList<IRecipe>();
		for (IRecipe recipe : egg.getRecipes()) {
			if (recipe instanceof Upgraderecipe) {
				upgrades.add(recipe);
			}
		}
		ClickableInventory ci = new ClickableInventory(Math.max(18,
				(upgrades.size() / 9) * 9), "Possible upgrades");
		if (upgrades.size() == 0) {
			ItemStack bar = new ItemStack(Material.BARRIER);
			ISUtils.setName(bar, "No upgrades available");
			ISUtils.addLore(bar, ChatColor.LIGHT_PURPLE + "Click to go back");
			Clickable noUpgrades = new Clickable(bar) {
				@Override
				public void clicked(Player p) {
					openFactoryBrowser(p, factoryViewed.get(p.getUniqueId()));
				}
			};
			ci.setSlot(noUpgrades, 4);
		} else {
			for (IRecipe recipe : upgrades) {
				ItemStack recStack = ((InputRecipe) recipe)
						.getRecipeRepresentation();
				ISUtils.setLore(recStack, ChatColor.LIGHT_PURPLE
						+ "Click to display more information");
				Clickable c = new Clickable(
						((InputRecipe) recipe).getRecipeRepresentation()) {
					@Override
					public void clicked(Player p) {
						openDetailedRecipeBrowser(p,
								ISUtils.getName(this.getItemStack()));
					}
				};
				ci.addSlot(c);
			}
		}
		ItemStack backStack = new ItemStack(Material.ARROW);
		ISUtils.setName(backStack, "Back to factory overview");
		ISUtils.addLore(backStack, ChatColor.LIGHT_PURPLE + "Click to go back");
		Clickable backClickable = new Clickable(backStack) {
			@Override
			public void clicked(Player arg0) {
				openFactoryBrowser(arg0, factoryViewed.get(arg0.getUniqueId()));
			}
		};
		ci.setSlot(backClickable, 17);
		ci.showInventory(p);
	}

	private void openDetailedRecipeBrowser(Player p, String recipeName) {
		if (recipeName == null) {
			FactoryMod
					.getPlugin()
					.warning(
							"Recipe name cannot be null in openDetailedRecipeBrowser calls");
			return;
		}
		FurnCraftChestEgg egg = (FurnCraftChestEgg) manager
				.getEgg(factoryViewed.get(p.getUniqueId()));
		InputRecipe rec = null;
		for (IRecipe recipe : egg.getRecipes()) {
			if (recipe == null || recipe.getName() == null) {
				FactoryMod.getPlugin().warning(
						"Null recipe or recipe name registered with "
								+ egg.getName());
				continue;
			}
			if (recipeName.equals(recipe.getName())) {
				rec = (InputRecipe) recipe;
				break;
			}
		}
		if (rec == null) {
			FactoryMod.getPlugin().warning(
					"There is no recipe with name " + recipeName);
			p.sendMessage(ChatColor.RED + "There is no recipe that matches "
					+ recipeName);
			return;
		}
		ClickableInventory ci = new ClickableInventory(54, recipeName);
		ItemStack inputStack = new ItemStack(Material.PAPER);
		ISUtils.setName(inputStack, "Input materials");
		ISUtils.addLore(inputStack, ChatColor.LIGHT_PURPLE
				+ "The materials required to run this recipe");
		DecorationStack inputClickable = new DecorationStack(inputStack);
		ci.setSlot(inputClickable, 4);
		int index = 13;
		List <ItemStack> ins = rec.getInputRepresentation(null);
		if (ins.size() > 18) {
			ins = new ItemMap(ins).getLoredItemCountRepresentation();
		}
		for (ItemStack is : ins) {
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
					index += 13;
				} else {
					index += (((4 - (index % 9)) * 2) + 1);
				}
			}

		}

		ItemStack outputStack = new ItemStack(Material.PAPER);
		ISUtils.setName(outputStack, "Output/effect");
		DecorationStack outputClickable = new DecorationStack(outputStack);
		ItemStack backStack = new ItemStack(Material.ARROW);
		ISUtils.setName(backStack, "Back to recipe overview");
		ISUtils.addLore(backStack, ChatColor.LIGHT_PURPLE + "Click to go back");
		Clickable backClickable = new Clickable(backStack) {
			@Override
			public void clicked(Player arg0) {
				openRecipeBrowser(arg0, factoryViewed.get(arg0.getUniqueId()));
			}
		};
		ci.setSlot(backClickable, 27);

		ci.setSlot(outputClickable, 31);
		index = 40;
		List <ItemStack> out = rec.getOutputRepresentation(null);
		if (out.size() > 18) {
			out = new ItemMap(out).getLoredItemCountRepresentation();
		}
		for (ItemStack is : out) {
			Clickable c;
			if (rec instanceof Upgraderecipe) {
				c = new Clickable(is) {
					@Override
					public void clicked(Player arg0) {
						IFactoryEgg egg = manager.getEgg(factoryViewed.get(arg0
								.getUniqueId()));
						for (IRecipe re : ((FurnCraftChestEgg) egg)
								.getRecipes()) {
							if (re instanceof Upgraderecipe
									&& ((Upgraderecipe) re)
											.getEgg()
											.getName()
											.equals(ISUtils.getName(this
													.getItemStack()))) {
								openFactoryBrowser(arg0, ((Upgraderecipe) re)
										.getEgg().getName());
								break;
							}
						}
					}
				};
			} else {
				c = new DecorationStack(is);
			}
			ci.setSlot(c, index);
			if ((index % 9) == 4) {
				index++;
				continue;
			}
			if ((index % 9) > 4) {
				index -= (((index % 9) - 4) * 2);
			} else {
				if ((index % 9) == 0) {
					index += 13;
				} else {
					index += (((4 - (index % 9)) * 2) + 1);
				}
			}
		}
		int fuelInterval = rec.getFuelConsumptionIntervall() != -1? rec.getFuelConsumptionIntervall() : egg.getFuelConsumptionIntervall();
		int fuelConsumed = rec.getProductionTime()/fuelInterval;
		ItemStack fuels = egg.getFuel().clone();
		fuels.setAmount(fuelConsumed);
		ItemStack fuelStack;
		if (fuelConsumed > fuels.getType().getMaxStackSize()) {
			fuelStack = new ItemMap(fuels).getLoredItemCountRepresentation().get(0);
		}
		else {
			fuelStack = fuels;
		}
		ISUtils.addLore(fuelStack, ChatColor.LIGHT_PURPLE + "Total duration of " + rec.getProductionTime() / 20 + " seconds");
		ci.setSlot(new DecorationStack(fuelStack), 30);
		ci.showInventory(p);
	}

	public void showPipeMaterials(Player p, Pipe pipe) {
		viewed.put(p.getUniqueId(), pipe);
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
					((Pipe) viewed.get(arg0.getUniqueId()))
							.removeAllowedMaterial(this.getItemStack()
									.getType());
					arg0.sendMessage(ChatColor.GOLD + "Removed "
							+ this.getItemStack().getType()
							+ " as allowed material");
				}
			};
			ci.addSlot(c);
		}
		if (mats.size() >= (start + 45)) {
			ItemStack nextPage = new ItemStack(Material.ARROW);
			ISUtils.setName(nextPage, "Next page");
			ISUtils.addLore(nextPage, ChatColor.LIGHT_PURPLE
					+ "Click to show entries upwards from " + (start + 45));

			Clickable nextClick = new Clickable(nextPage) {
				@Override
				public void clicked(Player arg0) {
					showPipeMaterialPart(
							arg0,
							(Pipe) viewed.get(arg0.getUniqueId()),
							Integer.valueOf(this.getItemStack().getItemMeta()
									.getLore().get(0).split(" ")[7]));
				}
			};
			ci.setSlot(nextClick, 49);
		}
		ci.showInventory(p);
	}

	public void showSorterFace(Player p, Sorter s, BlockFace face) {
		ClickableInventory ci = new ClickableInventory(54,
				"Items for this side");
		viewed.put(p.getUniqueId(), s);
		for (ItemStack is : s.getItemsForSide(face)
				.getItemStackRepresentation()) {
			is.setAmount(1);
			ci.addSlot(new Clickable(is) {
				@Override
				public void clicked(Player arg0) {
					((Sorter) viewed.get(arg0.getUniqueId()))
							.removeAssignment(this.getItemStack());
					arg0.sendMessage(ChatColor.GOLD + "Removed "
							+ NiceNames.getName(this.getItemStack()));
				}
			});
		}
		ci.showInventory(p);
	}

}
