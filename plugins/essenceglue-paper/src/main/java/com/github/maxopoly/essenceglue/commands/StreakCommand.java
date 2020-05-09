package com.github.maxopoly.essenceglue.commands;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.maxopoly.essenceglue.EssenceGluePlugin;
import com.github.maxopoly.essenceglue.StreakManager;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.civmodcore.util.TextUtil;

@CivCommand(id = "streak")
public class StreakCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		UUID uuid = StreakManager.getTrueUUID(p.getUniqueId());
		StreakManager streakMan = EssenceGluePlugin.instance().getStreakManager();
		p.sendMessage(ChatColor.GREEN + "Your current login streak is " + streakMan.getCurrentStreak(uuid, true));
		long cooldown = streakMan.getRewardCooldown(uuid);
		if (cooldown> 0) {
			p.sendMessage(ChatColor.YELLOW + "You will be eligible for daily rewards again in " + TextUtil.formatDuration(cooldown));
		}
		else {
			long left = streakMan.untilTodaysReward(uuid);
			p.sendMessage(ChatColor.GREEN + "You will receive your daily rewards in " + TextUtil.formatDuration(left));
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return Collections.emptyList();
	}

}
