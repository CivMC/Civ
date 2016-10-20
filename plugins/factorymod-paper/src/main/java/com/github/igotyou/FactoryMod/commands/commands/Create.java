package com.github.igotyou.FactoryMod.commands.commands;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.commands.FactoryModCommandHandler;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.eggs.PipeEgg;
import com.github.igotyou.FactoryMod.eggs.SorterEgg;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.structures.BlockFurnaceStructure;
import com.github.igotyou.FactoryMod.structures.FurnCraftChestStructure;
import com.github.igotyou.FactoryMod.structures.PipeStructure;

public class Create extends PlayerCommand {

	public Create(String name) {
		super(name);
		setIdentifier("fmc");
		setDescription("Creates a factory at the blocks you are looking at");
		setUsage("/fmc <factory name>");
		setArguments(0, 10);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC
					+ "How the hell is this supposed to work");
			return true;
		}
		FactoryModManager manager = FactoryMod.getManager();
		String name = FactoryModCommandHandler.getFactoryName(args);
		IFactoryEgg egg = manager.getEgg(name);
		if (egg == null) {
			String comp = name.toLowerCase();
			// check for lower/uppercase miss spellings
			for (Entry<String, IFactoryEgg> entry : manager.getAllEggs()
					.entrySet()) {
				if (entry.getKey().toLowerCase().equals(comp)) {
					egg = entry.getValue();
					break;
				}
			}
			if (egg == null) {
				sender.sendMessage(ChatColor.RED
						+ "This factory does not exist");
				return true;
			}
		}
		Set<Material> transparent = null;
		List<Block> view = ((Player) sender).getLineOfSight(transparent, 10);
		Factory exis = manager.getFactoryAt(view.get(view.size() - 1));
		if (exis != null) {
			manager.removeFactory(exis);
		}
		if (egg instanceof FurnCraftChestEgg) {
			FurnCraftChestEgg fcce = (FurnCraftChestEgg) egg;
			if (view.get(view.size() - 1).getType() == Material.WORKBENCH) {
				FurnCraftChestStructure fccs = new FurnCraftChestStructure(
						view.get(view.size() - 1));
				if (!fccs.isComplete()) {
					sender.sendMessage(ChatColor.RED
							+ "The required block structure for this factory doesn't exist here");
					return true;
				}
				manager.addFactory(fcce.hatch(fccs, (Player) sender));
				sender.sendMessage(ChatColor.GREEN + "Created " + egg.getName());
			} else {
				sender.sendMessage(ChatColor.RED
						+ "You are not looking at the right block for this factory");
			}
			return true;
		}
		if (egg instanceof PipeEgg) {
			PipeEgg fcce = (PipeEgg) egg;
			if (view.get(view.size() - 1).getType() == Material.DISPENSER) {
				PipeStructure fccs = new PipeStructure(
						view.get(view.size() - 1));
				if (!fccs.isComplete()) {
					sender.sendMessage(ChatColor.RED
							+ "The required block structure for this factory doesn't exist here");
					return true;
				}
				manager.addFactory(fcce.hatch(fccs, (Player) sender));
				sender.sendMessage(ChatColor.GREEN + "Created " + egg.getName());
			} else {
				sender.sendMessage(ChatColor.RED
						+ "You are not looking at the right block for this factory");
			}
			return true;
		}
		if (egg instanceof SorterEgg) {
			SorterEgg fcce = (SorterEgg) egg;
			if (view.get(view.size() - 1).getType() == Material.DROPPER) {
				BlockFurnaceStructure fccs = new BlockFurnaceStructure(
						view.get(view.size() - 1));
				if (!fccs.isComplete()) {
					sender.sendMessage(ChatColor.RED
							+ "The required block structure for this factory doesn't exist here");
					return true;
				}
				manager.addFactory(fcce.hatch(fccs, (Player) sender));
				sender.sendMessage(ChatColor.GREEN + "Created " + egg.getName());
			} else {
				sender.sendMessage(ChatColor.RED
						+ "You are not looking at the right block for this factory");
			}
		}

		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return FactoryModCommandHandler.tabCompleteFactory(arg0, arg1);
	}
}
