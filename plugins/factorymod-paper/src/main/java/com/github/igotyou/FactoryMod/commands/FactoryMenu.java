package com.github.igotyou.FactoryMod.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.eggs.FurnCraftChestEgg;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.utility.FactoryModGUI;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "fm")
public class FactoryMenu extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (args.length == 0 || args[0].length() == 0) {
			FactoryModGUI gui = new FactoryModGUI((Player) sender);
			gui.showFactoryOverview(true);
		} else {
			String concat = String.join(" ", args);
			IFactoryEgg egg = FactoryMod.getInstance().getManager().getEgg(concat);
			if (egg == null) {
				p.sendMessage(ChatColor.RED + "The factory " + concat + " does not exist");
				return true;
			}
			FactoryModGUI gui = new FactoryModGUI((Player) sender);
			gui.showForFactory((FurnCraftChestEgg) egg);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] args) {
		return FactoryTabCompleters.completeFactory(args);
	}
}
