package vg.civcraft.mc.civchat2.commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civmodcore.command.CivCommand;

import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.civmodcore.playersettings.impl.LongSetting;
import vg.civcraft.mc.civmodcore.util.ConfigParsing;
import vg.civcraft.mc.civmodcore.util.TextUtil;
import vg.civcraft.mc.namelayer.NameAPI;

@CivCommand(id = "globalmute")
public class GlobalMute extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		UUID player = NameAPI.getUUID(args [1]);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "The player " + args[1] + " does not exist");
			return true;
		}
		LongSetting banSetting = CivChat2.getInstance().getCivChat2SettingsManager().getGlobalChatMuteSetting();
		switch (args[0].toLowerCase()) {
		case "set":
			if (args.length < 3) {
				sender.sendMessage(ChatColor.RED + "You need to supply a ban timer");
				return false;
			}
			long time = ConfigParsing.parseTime(args [2]);
			if (time <= 0) {
				sender.sendMessage(ChatColor.RED + "Invalid ban time frame");
				return false;
			}
			banSetting.setValue(player, System.currentTimeMillis() + time);
			sender.sendMessage(ChatColor.GREEN + args [1] + " will be banned for " + TextUtil.formatDuration(time, TimeUnit.MILLISECONDS));
			return true;
		case "remove":
			banSetting.setValue(player, 0L);
			return true;
		case "check":
		case "get":
			long timeForUnban = banSetting.getValue(player);
			timeForUnban -= System.currentTimeMillis();
			if (timeForUnban <= 0) {
				sender.sendMessage(ChatColor.GREEN + args [1] + " is not chat muted");
				return true;
			}
			sender.sendMessage(ChatColor.GREEN + args [1] + " is banned for another " + TextUtil.formatDuration(timeForUnban, TimeUnit.MILLISECONDS));
			return true;
		default:
			sender.sendMessage(ChatColor.RED + args[0] + " is not a valid action");
			return false;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		switch (args.length) {
		case 0:
			return doTabComplete("", Arrays.asList("set", "remove", "check", "get"), false);
		case 1:
			return doTabComplete(args[0], Arrays.asList("set", "remove", "check", "get"), false);
		case 2:
			return doTabComplete(args[1], Bukkit.getOnlinePlayers(), Player::getName, false);
		default:
			return Collections.emptyList();
		}
	}

}
