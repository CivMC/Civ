package com.github.igotyou.FactoryMod.interactionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.utility.MenuBuilder;

public class FurnCraftChestInteractionManager implements IInteractionManager {
	private FurnCraftChestFactory fccf;
	private HashMap<Clickable, InputRecipe> recipes = new HashMap<Clickable, InputRecipe>();
	private static ReinforcementManager rm;
	private static MenuBuilder mb;

	public FurnCraftChestInteractionManager(FurnCraftChestFactory fccf) {
		this.fccf = fccf;
	}

	public FurnCraftChestInteractionManager() {
		
	}

	public void setFactory(FurnCraftChestFactory fccf) {
		this.fccf = fccf;
	}

	public static void prep() {
		mb = FactoryMod.getMenuBuilder();
		if (FactoryMod.getManager().isCitadelEnabled()) {
			rm = Citadel.getReinforcementManager();
		} else {
			rm = null;
		}
	}

	public void redStoneEvent(BlockRedstoneEvent e) {
		if (rm != null) {
			// Note this also accomplishes all the Citadel checking we need.
			BlockFace powerFace = findPoweringFace(e.getBlock(), fccf
					.getMultiBlockStructure().getAllBlocks());
			if (powerFace != null) {
				int trueNewCurrent = e.getBlock().getBlockPower(powerFace);
				if (trueNewCurrent != e.getNewCurrent()) {
					e.setNewCurrent(trueNewCurrent);
				}
			} else { // null means citadel is enabled but no valid redstone
						// power was found.
				return;
			}
		}

		if (e.getNewCurrent() == e.getOldCurrent()) {
			return;
		}
		int threshold = FactoryMod.getManager().getRedstonePowerOn();
		boolean newState = false;
		if (e.getBlock().getLocation().equals(fccf.getFurnace().getLocation())) {
			if (e.getOldCurrent() >= threshold && e.getNewCurrent() < threshold
					&& fccf.isActive()) {
				// Falling Edge (turn off)
				newState = false;
			} else if (e.getOldCurrent() < threshold
					&& e.getNewCurrent() >= threshold && !fccf.isActive()) {
				// Rising Edge (turn on)
				newState = true;
			} else {
				return;
			}

			if (newState) {
				fccf.attemptToActivate(null);
			} else {
				fccf.deactivate();
			}
		} else if (!fccf.isActive()
				&& e.getBlock()
						.getLocation()
						.equals(((FurnCraftChestStructure) fccf
								.getMultiBlockStructure()).getCraftingTable())) {
			// Can't change recipe while active.
			int change = e.getOldCurrent() - e.getNewCurrent();
			if (Math.abs(change) >= FactoryMod.getManager()
					.getRedstoneRecipeChange()) {
				List<IRecipe> currentRecipes = fccf.getRecipes();
				if (currentRecipes.size() == 0) {
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
							idx++;
						}
					} else if (change < 0) { // prev
						if (idx == 0) {
							idx = currentRecipes.size() - 1;
						} else {
							idx--;
						}
					}
				}
				fccf.setRecipe(currentRecipes.get(idx));
			}
		}
	}

	/**
	 * Only deals with directly powered redstone interactions, not indirect
	 * power Finds the block face giving the highest power that is also on a
	 * compatible Citadel group.
	 * 
	 * @param here
	 *            The block to check around.
	 * @param exclude
	 *            The blocks to exclude from checks.
	 * @return The Face of the highest compatible power level.
	 */
	private BlockFace findPoweringFace(Block here, List<Block> exclude) {
		if (here.isBlockPowered()) {
			PlayerReinforcement pr = (rm != null) ? (PlayerReinforcement) rm
					.getReinforcement(here) : null;
			int prGID = (pr != null) ? pr.getGroup().getGroupId() : -1;
			boolean checkCitadel = pr != null;
			if (checkCitadel) {
				checkCitadel = !pr.isInsecure(); // don't check citadel if
													// insecure; any input is
													// good then
			}
			BlockFace max = null;
			int maxP = -1;
			for (BlockFace face : adjacentFaces) {
				Block rel = here.getRelative(face);
				if (!exclude.contains(rel) && here.isBlockFacePowered(face)) {
					int curP = here.getBlockPower(face);
					if (curP > maxP) {
						if (!checkCitadel
								|| prGID == ((PlayerReinforcement) rm
										.getReinforcement(rel)).getGroup()
										.getGroupId()) {
							max = face;
							maxP = curP;
							// TODO: consider shortcut of iff max == 15 return;
						}
					}
				}
			}
			return max;
		} else {
			return null;
		}
	}

	protected static BlockFace[] adjacentFaces = new BlockFace[] {
			BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH,
			BlockFace.DOWN, BlockFace.UP };

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

	public void leftClick(Player p, Block b, BlockFace bf) {
		if (p.getItemInHand().getType() != FactoryMod.getManager()
				.getFactoryInteractionMaterial()) {
			return;
		}
		if (b.equals(((FurnCraftChestStructure) fccf.getMultiBlockStructure())
				.getChest())) { // chest interaction
			if (p.isSneaking()) { // sneaking, so showing detailed recipe stuff
				ClickableInventory ci = new ClickableInventory(54, fccf
						.getCurrentRecipe().getRecipeName());
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
			ClickableInventory ci = new ClickableInventory(36,"Select a recipe");
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
				ci.addSlot(c);
			}

			ci.showInventory(p);
			return;
		}
		if (b.equals(fccf.getFurnace())) { // furnace interaction
			if (fccf.isActive()) {
				fccf.deactivate();
			} else {
				fccf.attemptToActivate(p);
			}
		}
	}

	public void rightClick(Player p, Block b, BlockFace bf) {
		// Nothing to do here, every block already has a right click
		// functionality
	}

}
