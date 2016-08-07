package com.github.igotyou.FactoryMod.interactionManager;

import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import com.github.igotyou.FactoryMod.repairManager.PercentageHealthRepairManager;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;

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

    public void redStoneEvent(BlockRedstoneEvent e, Block factoryBlock) {
	ReinforcementManager rm = FactoryMod.getManager().isCitadelEnabled() ? Citadel
		.getReinforcementManager() : null;
	int threshold = FactoryMod.getManager().getRedstonePowerOn();
	if (factoryBlock.getLocation().equals(fccf.getFurnace().getLocation())) {
	    if (e.getOldCurrent() >= threshold && e.getNewCurrent() < threshold
		    && fccf.isActive()) {
		if ((rm == null || MultiBlockStructure.citadelRedstoneChecks(e
			.getBlock()))) {
		    fccf.deactivate();
		}
	    } else if (e.getOldCurrent() < threshold
		    && e.getNewCurrent() >= threshold && !fccf.isActive()) {
		if (rm == null
			|| MultiBlockStructure.citadelRedstoneChecks(e
				.getBlock())) {
		    fccf.attemptToActivate(null);
		}
	    } else {
		return;
	    }
	}
    }

    public void blockBreak(Player p, Block b) {
	if (p != null && !fccf.getRepairManager().inDisrepair()) {
	    p.sendMessage(ChatColor.DARK_RED
		    + "You broke the factory, it is in disrepair now");
	    FactoryMod.sendResponse("FactoryBreak", p);
	}
	if (fccf.isActive()) {
	    fccf.deactivate();
	}
	fccf.getRepairManager().breakIt();
    }

    public void leftClick(Player p, Block b, BlockFace bf) {
	if (p.getItemInHand().getType() != FactoryMod.getManager()
		.getFactoryInteractionMaterial()) {
	    return;
	}
	if (FactoryMod.getManager().isCitadelEnabled()) {
	    ReinforcementManager rm = Citadel.getReinforcementManager();
	    // is this cast safe? Let's just assume yes for now
	    PlayerReinforcement rein = (PlayerReinforcement) rm
		    .getReinforcement(b);
	    if (rein != null) {
		Group g = rein.getGroup();
		if (!NameAPI.getGroupManager().hasAccess(g.getName(),
			p.getUniqueId(),
			PermissionType.getPermission("USE_FACTORY"))) {
		    p.sendMessage(ChatColor.RED
			    + "You dont have permission to interact with this factory");
		    FactoryMod.sendResponse("FactoryNoPermission", p);
		    return;
		}
	    }
	}
	if (b.equals(((FurnCraftChestStructure) fccf.getMultiBlockStructure())
		.getChest())) { // chest interaction
	    if (p.isSneaking()) { // sneaking, so showing detailed recipe stuff
		ClickableInventory ci = new ClickableInventory(54, fccf
			.getCurrentRecipe().getRecipeName());
		int index = 4;
		List<ItemStack> inp = ((InputRecipe) fccf.getCurrentRecipe())
			.getInputRepresentation(fccf.getInventory());
		if (inp.size() > 18) {
		    inp = new ItemMap(inp).getLoredItemCountRepresentation();
		}
		for (ItemStack is : inp) {
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
			    index += 13;
			} else {
			    index += (((4 - (index % 9)) * 2) + 1);
			}
		    }

		}
		index = 49;
		List<ItemStack> outp = ((InputRecipe) fccf.getCurrentRecipe())
			.getOutputRepresentation(fccf.getInventory());
		if (outp.size() > 18) {
		    outp = new ItemMap(outp).getLoredItemCountRepresentation();
		}
		for (ItemStack is : outp) {
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
			    index -= 13;
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
		if (fccf.getRepairManager().inDisrepair()) {
			PercentageHealthRepairManager rm = ((PercentageHealthRepairManager) fccf.getRepairManager());
		    long leftTime = rm.getGracePeriod() - rm.getBreakTime();
		    long months = leftTime % (60 * 60 * 24 * 30 * 1000);
		    long days = leftTime - (months * 60 * 60 * 24 * 30 * 1000)
			    % (60 * 60 * 24 * 1000);
		    long hours = leftTime - (months * 60 * 60 * 24 * 30 * 1000)
			    - (days * 60 * 60 * 24 * 1000) % (60 * 60 * 1000);
		    String time = (months != 0 ? months + " months, " : "")
			    + (days != 0 ? days + " days, " : "")
			    + (hours != 0 ? hours + " hours" : "");
		    if (time.equals("")) {
		    	time = " less than an hour";
		    }
		     p.sendMessage(ChatColor.GOLD + "It will break permanently in " + time);
		}
	    }

	    return;
	}
	if (b.equals(((FurnCraftChestStructure) fccf.getMultiBlockStructure())
		.getCraftingTable())) { // crafting table interaction
	    int rows = (fccf.getRecipes().size() / 9) + 1;
	    if (fccf.getRecipes().size() > 53 || rows > 6) {
		p.sendMessage(ChatColor.RED
			+ "This factory has more than 53 recipes and the GUI for it can't be opened. Either complain to "
			+ "your admin to have them put less recipes in this factory or complain to /u/maxopoly to add "
			+ "scrollviews to this");
		return;
	    }
	    ClickableInventory ci = new ClickableInventory(rows * 9,
		    "Select a recipe");
	    for (IRecipe rec : fccf.getRecipes()) {
		InputRecipe recipe = (InputRecipe) (rec);
		ItemStack recStack = recipe.getRecipeRepresentation();
		ISUtils.addLore(
			recStack,
			ChatColor.GOLD + "Ran "
				+ String.valueOf(fccf.getRunCount(recipe))
				+ " times");
		Clickable c = new Clickable(recStack) {

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
			    FactoryMod.sendResponse("RecipeSwitch", p);
			}

		    }
		};
		recipes.put(c, recipe);
		ci.addSlot(c);
	    }
	    ItemStack menuStack = new ItemStack(Material.PAINTING);
	    ISUtils.setName(menuStack, "Open menu");
	    ISUtils.addLore(menuStack, ChatColor.LIGHT_PURPLE
		    + "Click to open a detailed menu");
	    Clickable menuC = new Clickable(menuStack) {
		@Override
		public void clicked(Player arg0) {
		    FactoryMod.getMenuBuilder().openFactoryBrowser(arg0,
			    fccf.getName());
		}
	    };
	    ci.setSlot(menuC, (rows * 9) - 1);

	    ci.showInventory(p);
	    return;
	}
	if (b.equals(fccf.getFurnace())) { // furnace interaction
	    if (fccf.isActive()) {
		fccf.deactivate();
		p.sendMessage(ChatColor.RED + "Deactivated " + fccf.getName());
		FactoryMod.sendResponse("FactoryActivation", p);
	    } else {
		fccf.attemptToActivate(p);
		FactoryMod.sendResponse("FactoryDeactivation", p);
	    }
	}
    }

    public void rightClick(Player p, Block b, BlockFace bf) {
	// Nothing to do here, every block already has a right click
	// functionality
    }

}
