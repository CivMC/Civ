package com.github.igotyou.FactoryMod.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.utility.FactoryCommandUtils;
import com.github.igotyou.FactoryMod.utility.MenuBuilder;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "fm")
public class Menu extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC + "Fuck off console man");
			return true;
		}
		MenuBuilder mb = FactoryMod.getMenuBuilder();
		Player p = (Player) sender;
		if (p.isInsideVehicle()) {
			p.sendMessage(ChatColor.RED + "You can't use this command in vehicles");
			return true;
		}
		if (args.length == 0) {
			mb.openFactoryBrowser(p, null);
		} else {
			mb.openFactoryBrowser(p, FactoryCommandUtils.getFactoryName(args));
		}
		return true;
	}

	@Override
	public List <String> tabComplete(CommandSender arg0, String [] arg1) {
		return FactoryCommandUtils.tabCompleteFactory(arg0, arg1);
	}
}
