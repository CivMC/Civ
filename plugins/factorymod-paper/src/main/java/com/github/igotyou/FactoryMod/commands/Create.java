package com.github.igotyou.FactoryMod.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.eggs.PipeEgg;
import com.github.igotyou.FactoryMod.eggs.SorterEgg;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "fmc")
public class Create extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		FactoryModManager manager = FactoryMod.getInstance().getManager();
		String name = String.join(" ", args);
		IFactoryEgg egg = manager.getEgg(name);
		if (egg == null) {
			sender.sendMessage(ChatColor.RED + "This factory does not exist");
			return true;
		}
		Set<Material> transparent = null;
		List<Block> view = ((Player) sender).getLineOfSight(transparent, 10);
		Factory exis = manager.getFactoryAt(view.get(view.size() - 1));
		if (exis != null) {
			manager.removeFactory(exis);
		}
		if (egg instanceof FurnCraftChestEgg) {
			FurnCraftChestEgg fcce = (FurnCraftChestEgg) egg;
			if (view.get(view.size() - 1).getType() == Material.CRAFTING_TABLE) {
				FurnCraftChestStructure fccs = new FurnCraftChestStructure(view.get(view.size() - 1));
				if (!fccs.isComplete()) {
					sender.sendMessage(
							ChatColor.RED + "The required block structure for this factory doesn't exist here");
					return true;
				}
				manager.addFactory(fcce.hatch(fccs, (Player) sender));
				sender.sendMessage(ChatColor.GREEN + "Created " + egg.getName());
			} else {
				sender.sendMessage(ChatColor.RED + "You are not looking at the right block for this factory");
			}
			return true;
		}
		if (egg instanceof PipeEgg) {
			PipeEgg fcce = (PipeEgg) egg;
			if (view.get(view.size() - 1).getType() == Material.DISPENSER) {
				PipeStructure fccs = new PipeStructure(view.get(view.size() - 1));
				if (!fccs.isComplete()) {
					sender.sendMessage(
							ChatColor.RED + "The required block structure for this factory doesn't exist here");
					return true;
				}
				manager.addFactory(fcce.hatch(fccs, (Player) sender));
				sender.sendMessage(ChatColor.GREEN + "Created " + egg.getName());
			} else {
				sender.sendMessage(ChatColor.RED + "You are not looking at the right block for this factory");
			}
			return true;
		}
		if (egg instanceof SorterEgg) {
			SorterEgg fcce = (SorterEgg) egg;
			if (view.get(view.size() - 1).getType() == Material.DROPPER) {
				BlockFurnaceStructure fccs = new BlockFurnaceStructure(view.get(view.size() - 1));
				if (!fccs.isComplete()) {
					sender.sendMessage(
							ChatColor.RED + "The required block structure for this factory doesn't exist here");
					return true;
				}
				manager.addFactory(fcce.hatch(fccs, (Player) sender));
				sender.sendMessage(ChatColor.GREEN + "Created " + egg.getName());
			} else {
				sender.sendMessage(ChatColor.RED + "You are not looking at the right block for this factory");
			}
		}

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] args) {
		return FactoryTabCompleters.completeFactory(String.join(" ", args));
	}
}
