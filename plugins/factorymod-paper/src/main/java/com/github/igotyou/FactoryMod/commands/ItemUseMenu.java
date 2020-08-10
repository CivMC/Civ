package com.github.igotyou.FactoryMod.commands;

import com.github.igotyou.FactoryMod.utility.ItemUseGUI;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

import java.util.List;

@CivCommand(id = "item")
public class ItemUseMenu extends StandaloneCommand {
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (p.isInsideVehicle()) {
			p.sendMessage(ChatColor.RED + "You can't use this command in vehicles");
			return true;
		}
		if (args.length == 0 || args[0].length() == 0) {
			ItemUseGUI gui = new ItemUseGUI((Player) sender);
			gui.showItemOverview(p.getInventory().getItemInMainHand());
		} else {
			String concat = String.join(" ", args);
			Material mat = Material.getMaterial(concat);
			if (mat == null) {
				p.sendMessage(ChatColor.RED + "The item " + concat + " does not exist");
				return true;
			}
			ItemUseGUI gui = new ItemUseGUI((Player) sender);
			gui.showItemOverview(new ItemStack(mat));
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		Material[] mats = Material.values();
		List<String> completions = Lists.newArrayList();
		if (args.length == 1) {
			for (Material s : mats) {
				if (s.name().toLowerCase().startsWith(args[0].toLowerCase())) {
					completions.add(s.name());
				}
			}
			return completions;
		}
		return null;
	}
}
