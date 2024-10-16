package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.stat.LoadStatistic;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.stat.LoadStatisticManager;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.stat.PluginStatistic;

@CommandAlias("cmc")
public class StatCommand extends BaseCommand {
	@Subcommand("stat")
	@Description("Shows chunk loading statistics.")
	@CommandPermission("cmc.debug")
	public void save(CommandSender sender) {
		LoadStatistic loadStatistic = LoadStatisticManager.getLoadStatistic();
		if (loadStatistic == null) {
			sender.sendMessage(ChatColor.RED + "Statistics polling is disabled");
			return;
		}

		sender.sendMessage(ChatColor.WHITE + "Chunk loading statistics:");

		sender.sendMessage("World Count: " + loadStatistic.worldCount);
		sender.sendMessage("Thread Count: " + loadStatistic.threadCount);

		for (PluginStatistic pluginStatistic : loadStatistic.pluginStatistics) {
			sender.sendMessage(ChatColor.WHITE + "[" + pluginStatistic.pluginName + "]");
			sender.sendMessage("Load count: " + pluginStatistic.chunkLoadCount);
			sender.sendMessage("Total load time: " + nanoToMsStr(pluginStatistic.chunkLoadSumNanoSec));
			sender.sendMessage("Min load time: " + nanoToMsStr(pluginStatistic.chunkLoadMinTimeNanoSec));
			sender.sendMessage("Max load time: " + nanoToMsStr(pluginStatistic.chunkLoadMaxTimeNanoSec));
			sender.sendMessage("Avg load time: " + nanoToMsStr(pluginStatistic.chunkLoadSumNanoSec / pluginStatistic.chunkLoadCount));
		}

		for (LoadStatistic.WorldThreads worldThreads : loadStatistic.worldThreadsList) {
			sender.sendMessage(ChatColor.WHITE + "[World: " + worldThreads.world.getName() + "]");

			if (worldThreads.mainThreadTime != null)
				sender.sendMessage("Main thread is now executing for time: " + nanoToMsStr(worldThreads.mainThreadTime));

			for (LoadStatistic.ThreadTime threadTime : worldThreads.threadTimes)
				sender.sendMessage("Thread #" + threadTime.threadIndex + " is now executing for time: " + nanoToMsStr(threadTime.time));
		}
	}

	private static String nanoToMsStr(long nano) {
		double ms = (double)Math.round(nano / 10000L) / 100.0;
		return String.format("%.2f ms", ms);
	}
}
