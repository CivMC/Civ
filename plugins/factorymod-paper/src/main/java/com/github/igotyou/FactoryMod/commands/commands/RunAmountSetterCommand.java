package com.github.igotyou.FactoryMod.commands.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.FactoryModManager;
import com.github.igotyou.FactoryMod.factories.Factory;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class RunAmountSetterCommand extends PlayerCommand {

	public RunAmountSetterCommand(String name) {
		super(name);
		setIdentifier("fmsrc");
		setDescription("Sets the amount of runs for the currently selected recipe in the factory you are looking at");
		setUsage("/fmsrc <runCount>");
		setArguments(0, 1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC
					+ "How the hell is this supposed to work");
			return true;
		}
		Player p = (Player) sender;
		int newAmount; 
		try {
			newAmount = Integer.parseInt(args[0]);
		}
		catch(NumberFormatException e) {
			p.sendMessage(ChatColor.RED + args [0] + " is not a number");
			return true;
		}
		FactoryModManager manager = FactoryMod.getManager();
		for(Block b : p.getLineOfSight((Set <Material>)null, 15)) {
			Factory f = manager.getFactoryAt(b);
			if (f instanceof FurnCraftChestFactory) {
				FurnCraftChestFactory fccf = (FurnCraftChestFactory) f;
				fccf.setRunCount(fccf.getCurrentRecipe(), newAmount);
				p.sendMessage(ChatColor.GREEN + "Set runcount for recipe " + fccf.getCurrentRecipe().getName() + " in " + fccf.getName() + " to "+ newAmount);
			}
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}
	

}
