package vg.civcraft.mc.citadel.command.commands;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class ReinforcementsGUI extends PlayerCommand {

	private DecimalFormat format;

	public ReinforcementsGUI(String name) {
		super(name);
		setIdentifier("ctdl");
		setDescription("Open GUI to display all reinforcement materials");
		setUsage("/ctdl");
		setArguments(0, 0);
		this.format = new DecimalFormat("##.##");
	}

	@Override
	public boolean execute(CommandSender sender, String[] arg1) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Sorry bae, I cant open GUIs for you");
			return true;
		}
		List<ReinforcementType> types = ReinforcementType.getReinforcementTypes();
		int rows = types.size() / 9;
		if ((types.size() % 9) != 0) {
			rows++;
		}
		//sort ascending by health
		Collections.sort(types, new Comparator<ReinforcementType>() {

			@Override
			public int compare(ReinforcementType o1, ReinforcementType o2) {
				return new Integer(o1.getHitPoints()).compareTo(o2.getHitPoints());
			}
		});
		ClickableInventory ci = new ClickableInventory(rows * 9, ChatColor.GOLD + "Citadel");
		int slot = 0;
		List <ItemStack> items = new LinkedList<ItemStack>();
		for (ReinforcementType type : types) {
			ItemStack is = type.getItemStack().clone();
			is.setAmount(type.getRequiredAmount());
			ISUtils.addLore(is, ChatColor.GREEN + "Hit points: " + type.getHitPoints());
			int daysMature = type.getMaturationTime() / 60 / 24;
			int hoursMature = (type.getMaturationTime() - (daysMature * 60 * 24)) / 60;
			int minutesMature = (type.getMaturationTime() - (daysMature * 60 * 24) - (hoursMature * 60));
			String matureString = ChatColor.AQUA + "Maturation time: " + createOutputTime(daysMature, "day")
					+ createOutputTime(hoursMature, "hour") + createOutputTime(minutesMature, "minute");
			ISUtils.addLore(is, matureString.substring(0, matureString.length() - 2));
			int daysAcid = type.getAcidTime() / 60 / 24;
			int hoursAcid = (type.getAcidTime() - (daysAcid * 60 * 24)) / 60;
			int minutesAcid = (type.getAcidTime() - (daysAcid * 60 * 24) - (hoursAcid * 60));
			String acidString = ChatColor.GOLD + "Acidblock maturation time: " + createOutputTime(daysAcid, "day")
					+ createOutputTime(hoursAcid, "hour") + createOutputTime(minutesAcid, "minute");
			ISUtils.addLore(is, acidString.substring(0, acidString.length() - 2));
			ISUtils.addLore(is, ChatColor.WHITE + "Return chance: " + format.format(type.getPercentReturn() * 100.0) + " %");
			items.add(is);
			ci.setSlot(new DecorationStack(is), slot);
			slot++;
		}
		ci.showInventory((Player) sender);
		return true;
	}

	private String createOutputTime(int value, String timeUnit) {
		if (value == 0) {
			return "";
		}
		if (value == 1) {
			return value + " " + timeUnit + ", ";
		}
		return value + " " + timeUnit + "s, ";
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}

}
