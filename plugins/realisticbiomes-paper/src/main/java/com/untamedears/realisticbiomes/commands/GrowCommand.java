package com.untamedears.realisticbiomes.commands;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.realisticbiomes.RBConfigManager;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.model.gauss.GaussTree;
import com.untamedears.realisticbiomes.model.ltree.BlockTransformation;
import com.untamedears.realisticbiomes.model.ltree.LTree;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "rbgrow")
public class GrowCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		GaussTree tree = new GaussTree(Double.parseDouble(args[0]), 0.15, Double.parseDouble(args[1]), 0.15,
				Double.parseDouble(args[2]), Double.parseDouble(args[3]),
				new BlockTransformation(Material.DARK_OAK_LOG, new HashMap<>()), Double.parseDouble(args[4]), 0.15,
				Double.parseDouble(args[5]), 0.15, Double.parseDouble(args[6]), Double.parseDouble(args[7]), 0.15,
				Double.parseDouble(args[8]), 0.15, Double.parseDouble(args[9]), 0.15,
				new BlockTransformation(Material.OAK_LEAVES, new HashMap<>()));
		tree.genAt(((Player) sender).getLocation());
		sender.sendMessage("Done");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
