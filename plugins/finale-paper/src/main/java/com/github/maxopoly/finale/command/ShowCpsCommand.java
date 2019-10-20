package com.github.maxopoly.finale.command;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CPSHandler;

import net.md_5.bungee.api.ChatColor;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "cps")
public class ShowCpsCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender arg0, String[] arg1) {
		if (!(arg0 instanceof Player)) {
			return true;
		}
		
		Player player = (Player) arg0;
		
		CPSHandler cpsHandler = Finale.getPlugin().getManager().getCPSHandler();
		if (cpsHandler.isShowingCPS(player)) {
			cpsHandler.hideCPS(player);
			player.sendMessage(ChatColor.RED + "You are no longer viewing your CPS!");
		} else {
			cpsHandler.showCPS(player);
			player.sendMessage(ChatColor.GREEN + "You are now viewing your CPS!");
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return new LinkedList<>();
	}

}
