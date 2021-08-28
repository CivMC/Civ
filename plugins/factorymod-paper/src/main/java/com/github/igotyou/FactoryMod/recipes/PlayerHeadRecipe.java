package com.github.igotyou.FactoryMod.recipes;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.github.igotyou.FactoryMod.utility.MultiInventoryWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

/***
 * Outputs a player head belonging to a random player who is connected to the server when the recipe is run
 */
public class PlayerHeadRecipe extends InputRecipe {
	public PlayerHeadRecipe(String identifier, String name, int productionTime, ItemMap inputs) {
		super(identifier, name, productionTime, inputs);
	}

	@Override
	public List<ItemStack> getInputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		if (i == null) {
			return input.getItemStackRepresentation();
		}
		return createLoredStacksForInfo(i);
	}

	@Override
	public List<ItemStack> getOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
		ItemUtils.addLore(is,"The player head of a randomly chosen online player");
		return Collections.singletonList(is);
	}

	@Override
	public List<String> getTextualOutputRepresentation(Inventory i, FurnCraftChestFactory fccf) {
		return Collections.singletonList("The player head of a randomly chosen online player");
	}

	@Override
	public Material getRecipeRepresentationMaterial() {
		return Material.PLAYER_HEAD;
	}

	@Override
	public boolean applyEffect(Inventory inputInv, Inventory outputInv, FurnCraftChestFactory fccf) {
		MultiInventoryWrapper combo = new MultiInventoryWrapper(inputInv, outputInv);
		logBeforeRecipeRun(combo, fccf);
		ItemMap toRemove = input.clone();
		ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
		if (players.isEmpty()) {
			return false;
		}
		if (toRemove.isContainedIn(inputInv)) {
			if (toRemove.removeSafelyFrom(inputInv)) {
				Random rand = new Random();
				Player player = players.get(rand.nextInt(players.size()));
				ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
				SkullMeta im = (SkullMeta) is.getItemMeta();
				im.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
				im.setDisplayName(player.getDisplayName());
				is.setItemMeta(im);
				outputInv.addItem(is);
			}
		}
		logAfterRecipeRun(combo, fccf);
		return true;
	}

	@Override
	public String getTypeIdentifier() {
		return "PLAYERHEAD";
	}
}
