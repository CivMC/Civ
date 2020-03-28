package com.github.igotyou.FactoryMod.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import com.github.igotyou.FactoryMod.recipes.ProductionRecipe;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "fmco")
public class CheatOutput extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		Set<Material> transparent = null;
		List<Block> view = ((Player) sender).getLineOfSight(transparent, 10);
		FactoryModManager manager = FactoryMod.getInstance().getManager();
		Factory exis = manager.getFactoryAt(view.get(view.size() - 1));
		if (exis != null && exis instanceof FurnCraftChestFactory) {
			FurnCraftChestFactory fcc = (FurnCraftChestFactory) exis;
			if (fcc.getCurrentRecipe() == null) {
				player.sendMessage(ChatColor.RED + "This factory has no recipe selected");
				return true;
			}
			IRecipe rec = fcc.getCurrentRecipe();
			if (!(rec instanceof ProductionRecipe)) {
				player.sendMessage(ChatColor.RED + "The selected recipe is not a production recipe");
				return true;
			}
			ProductionRecipe prod = (ProductionRecipe) rec;
			for (ItemStack is : prod.getOutput().getItemStackRepresentation()) {
				player.getInventory().addItem(is);
			}
			player.sendMessage(ChatColor.GREEN + "Gave you all items for recipe " + ChatColor.GREEN + prod.getName());
		} else {
			player.sendMessage(ChatColor.RED + "You are not looking at a valid factory");
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
