package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.activity.ActivityMapStat;

@CommandAlias("am")
public class Activity extends BaseCommand {
	@CommandAlias("stat")
	@Description("Shows ActivityMap statistics.")
	@CommandPermission("citadel.admin")
	public void execute(CommandSender sender) {
		ActivityMapStat stat = Citadel.getInstance().getActivityMap().getStat();
		if (stat == null) {
			sender.sendMessage(Component.text("ActivityMap is disabled").color(NamedTextColor.RED));
			return;
		}

		sender.sendMessage(Component.text("[ActivityMap] object statistics:").color(NamedTextColor.YELLOW));
		sender.sendMessage("World count: " + stat.worlds);
		sender.sendMessage("In-memory loaded regions: " + longToStr(stat.loadedRegions));
		sender.sendMessage("In-memory NOT loaded regions: " + longToStr(stat.notLoadedRegions));
		sender.sendMessage("In-memory groups: " + longToStr(stat.uniqueGroups));
		sender.sendMessage("In-memory activities: " + longToStr(stat.loadedActivities));

		sender.sendMessage(Component.text("[ActivityMap] region load statistics:").color(NamedTextColor.YELLOW));
		sender.sendMessage("Regions loaded: " + longToStr(stat.regionLoadCount));
		sender.sendMessage("Regions un-loaded: " + longToStr(stat.regionUnloadCount));
		sender.sendMessage("Total load time: " + nanoToMsStr(stat.regionLoadSumNano));
		sender.sendMessage("Min load time: " + nanoToMsStr(stat.regionLoadMinTimeNano));
		sender.sendMessage("Max load time: " + nanoToMsStr(stat.regionLoadMaxTimeNano));
		sender.sendMessage("Avg load time: " + nanoToMsStr(stat.regionLoadSumNano / stat.regionLoadCount));
	}

	private static String longToStr(long i) {
		return String.format("%,d", i);
	}

	private static String nanoToMsStr(long nano) {
		double ms = (double)Math.round(nano / 10000L) / 100.0;
		return String.format("%,.2f ms", ms);
	}
}
