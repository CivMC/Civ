package com.github.igotyou.FactoryMod.interactionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;

public class FurnCraftChestInteractionManager implements IInteractionManager {
	private FurnCraftChestFactory fccf;
	private HashMap<Clickable, InputRecipe> recipes = new HashMap<Clickable, InputRecipe>();

	public FurnCraftChestInteractionManager(FurnCraftChestFactory fccf) {
		this.fccf = fccf;
	}

	public FurnCraftChestInteractionManager() {
	}

	public void setFactory(FurnCraftChestFactory fccf) {
		this.fccf = fccf;
	}

	public void redStoneEvent(BlockRedstoneEvent e) {
		if (e.getNewCurrent() == e.getOldCurrent()) {
			return;
		}
		int threshold = 1;
		int rThreshold = 1;
		boolean newState = false;
		if (e.getBlock().getLocation().equals(fccf.getFurnace().getLocation())) {
			if (e.getOldCurrent() >= threshold && e.getNewCurrent() < threshold && fccf.isActive()) {
				// Falling Edge (turn off)
				newState = false;
			} else if (e.getOldCurrent() < threshold && e.getNewCurrent() >= threshold && !fccf.isActive()) {
				// Rising Edge (turn on)
				newState = true;
			} else {
				return;
			}
			
			// TODO: Check citadel here
			if (checkCitadelAround(e.getBlock().getLocation(), e.getNewCurrent())) {
				if (newState) {
					fccf.activate();
				} else {
					fccf.deactivate();
				}
			}
		} else if (!fccf.isActive() && e.getBlock().getLocation().equals( 
				((FurnCraftChestStructure) fccf.getMultiBlockStructure()).getCraftingTable())) {
			// Can't change recipe while active.
			int change = e.getOldCurrent() - e.getNewCurrent();
			if (Math.abs(change) >= rThreshold) {
				List<IRecipe> currentRecipes = fccf.getRecipes();
				if (currentRecipes.size() == 0 || !checkCitadelAround(e.getBlock().getLocation(), e.getNewCurrent())) {
					return;
				}
				IRecipe current = fccf.getCurrentRecipe();
				int idx = 0;
				// edge case?
				if (current != null) {
					idx = currentRecipes.indexOf(current);
					if (change > 0) { // next
						if (idx >= currentRecipes.size() - 1) {
							idx = 0;
						} else {
							idx ++;
						}
					} else if (change < 0) { // prev
						if (idx == 0) {
							idx = currentRecipes.size() - 1;
						} else { 
							idx --;
						}
					}
				}
				fccf.setRecipe(currentRecipes.get(idx));
			}
		}
	}
	
	/**
	 * Utility method to check Citadel properties of potential power-giving blocks
	 * surrounding the location passed, but skipping any locations owned by the Factory.
	 * 
	 * In other words, the factory won't transmit power to other blocks within its multiblock
	 * structure.
	 * 
	 * @param here The Location (part of the factory) to check around
	 * @param level The power level to compare against
	 * @return True if something found that is powered at the level indicated and on a compatible group 
	 */
	private boolean checkCitadelAround(Location here, int level) {
		return true;
	}

	public void blockBreak(Player p, Block b) {
		fccf.getRepairManager().breakIt();
		if (p != null) {
			p.sendMessage(ChatColor.DARK_RED
					+ "You broke the factory, it is in disrepair now");
		}
		if (fccf.isActive()) {
			fccf.deactivate();
		}
	}

	public void leftClick(Player p, Block b) {
		if (b.equals(((FurnCraftChestStructure) fccf.getMultiBlockStructure())
				.getChest())) { // chest interaction
			if (p.isSneaking()) { // sneaking, so showing detailed recipe stuff
				ClickableInventory ci = new ClickableInventory(
						new ArrayList<Clickable>(), 54, fccf.getCurrentRecipe()
								.getRecipeName());
				int index = 4;
				for (ItemStack is : ((InputRecipe) fccf.getCurrentRecipe())
						.getInputRepresentation(fccf.getInventory())) {
					Clickable c = new Clickable(is) {
						@Override
						public void clicked(Player arg0) {
							// nothing, just supposed to look nice
						}
					};
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
				index = 49;
				for (ItemStack is : ((InputRecipe) fccf.getCurrentRecipe())
						.getOutputRepresentation(fccf.getInventory())) {
					Clickable c = new Clickable(is) {
						@Override
						public void clicked(Player arg0) {
							// nothing, just supposed to look nice
						}
					};
					ci.setSlot(c, index);
					if ((index % 9) == 4) {
						index++;
						continue;
					}
					if ((index % 9) > 4) {
						index -= (((index % 9) - 4) * 2);
					} else {
						if ((index % 9) == 0) {
							index -= 9;
						} else {
							index += (((4 - (index % 9)) * 2) + 1);
						}
					}

				}
				ci.showInventory(p);

			} else { // not sneaking, so just a short sumup
				p.sendMessage(ChatColor.GOLD + fccf.getName()
						+ " currently turned "
						+ (fccf.isActive() ? "on" : "off"));
				if (fccf.isActive()) {
					p.sendMessage(ChatColor.GOLD
							+ String.valueOf((fccf.getCurrentRecipe()
									.getProductionTime() - fccf
									.getRunningTime()) / 20)
							+ " seconds remaining until current run is complete");
				}
				p.sendMessage(ChatColor.GOLD + "Currently selected recipe: "
						+ fccf.getCurrentRecipe().getRecipeName());
				p.sendMessage(ChatColor.GOLD + "Currently at "
						+ fccf.getRepairManager().getHealth() + " health");
			}

			return;
		}
		if (b.equals(((FurnCraftChestStructure) fccf.getMultiBlockStructure())
				.getCraftingTable())) { // crafting table interaction
			ArrayList<Clickable> clickables = new ArrayList<Clickable>();
			for (IRecipe rec : fccf.getRecipes()) {
				InputRecipe recipe = (InputRecipe) (rec);
				Clickable c = new Clickable(recipe.getRecipeRepresentation()) {

					@Override
					public void clicked(Player p) {
						if (fccf.isActive()) {
							p.sendMessage(ChatColor.RED
									+ "You can't switch recipes while the factory is running");
						} else {
							fccf.setRecipe(recipes.get(this));
							p.sendMessage(ChatColor.GREEN
									+ "Switched recipe to "
									+ recipes.get(this).getRecipeName());
						}

					}
				};
				recipes.put(c, recipe);
				clickables.add(c);
			}
			ClickableInventory ci = new ClickableInventory(clickables,
					InventoryType.CHEST, "Select a recipe");
			ci.showInventory(p);
			return;
		}
		if (b.equals(fccf.getFurnace()) { // furnace interaction
			if (fccf.isActive()) {
				fccf.deactivate();
			} else {
				fccf.attemptToActivate(p);
			}
		}
	}

	public void rightClick(Player p, Block b) {
		// Nothing to do here, every block already has a right click
		// functionality
	}

}
