package com.github.igotyou.FactoryMod.interactionManager;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryType;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.multiBlockStructures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;

public class FurnCraftChestInteractionManager implements IInteractionManager {
	private FurnCraftChestFactory fccf;
	private ClickableInventory ci;
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
		//TODO
	}

	public void blockBreak(Player p, Block b) {
		fccf.getRepairManager().breakIt();
		if (p != null) {
			p.sendMessage(ChatColor.DARK_RED
					+ "You broke the factory, it is in disrepair now");
		}
	}

	public void leftClick(Player p, Block b) {
		if (b == ((FurnCraftChestStructure) fccf.getMultiBlockStructure())
				.getChest()) { // chest interaction
			System.out.println("chest hit");
			// TODO Display information

			return;
		}
		if (b == ((FurnCraftChestStructure) fccf.getMultiBlockStructure())
				.getCraftingTable()) { // crafting table interaction
			System.out.println("craft hit");
			ArrayList<Clickable> clickables = new ArrayList<Clickable>();
			for (IRecipe rec : fccf.getRecipes()) {
				System.out.println(rec.getRecipeName());
				InputRecipe recipe = (InputRecipe) (rec);
				Clickable c = new Clickable(recipe.getRecipeRepresentation()) {

					@Override
					public void clicked(Player p) {
						if (fccf.isActive()) {
							p.sendMessage(ChatColor.RED
									+ "You can't switch recipes while the factory is running");
						} else {
							fccf.setRecipe(recipes.get(this));
						}

					}
				};
				recipes.put(c, recipe);
				clickables.add(c);
			}
			ci = new ClickableInventory(clickables, InventoryType.CHEST,
					"Select a recipe");
			ci.showInventory(p);
			return;
		}
		if (b == ((FurnCraftChestStructure) fccf.getMultiBlockStructure())
				.getFurnace()) { // furnace interaction
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
