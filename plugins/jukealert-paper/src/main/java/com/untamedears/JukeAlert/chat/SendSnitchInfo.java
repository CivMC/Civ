package com.untamedears.JukeAlert.chat;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;

import com.google.common.base.Strings;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.DeprecatedMethods;
import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.SnitchAction;
import com.untamedears.JukeAlert.storage.JukeAlertLogger;

public class SendSnitchInfo implements Runnable {

	private List<SnitchAction> info;

	private Player player;

	private int offset;

	private String snitchName;

	private boolean isJukebox;

	private boolean shouldCensor;

	private boolean isGroup;

	public SendSnitchInfo(List<SnitchAction> info, Player player, int offset, String snitchName, boolean isJukebox,
			boolean shouldCensor, boolean isGroup) {

		this.info = info;
		this.player = player;
		this.offset = offset;
		this.snitchName = snitchName;
		this.isJukebox = isJukebox;
		this.shouldCensor = shouldCensor;
		this.isGroup = isGroup;
	}

	public void run() {

		if (!isJukebox) {
			if (this.snitchName == null || this.snitchName.trim().isEmpty()) {
				player.sendMessage(ChatColor.AQUA + " * Unnamed entry snitch");
			} else {
				player.sendMessage(ChatColor.AQUA + " * Entry snitch " + this.snitchName);
			}
			return;
		}
		if (info != null && !info.isEmpty()) {
			String output = "";

			if (this.snitchName != null && !this.snitchName.trim().isEmpty()) {
				output += ChatColor.WHITE + " Log for snitch " + this.snitchName + " "
				        + ChatColor.DARK_GRAY + Strings.repeat("-", this.snitchName.length()) + "\n";
			} else {
				output += ChatColor.WHITE + " Log for unnamed snitch "
				        + ChatColor.DARK_GRAY + "----------------------------------------" + "\n";
			}

			try {
				TimeZone timeZone = Calendar.getInstance().getTimeZone();
				Date now = Calendar.getInstance().getTime();
				boolean daylightTime = timeZone.inDaylightTime(now);
				int offsetMinutes = timeZone.getOffset(now.getTime()) / 60000;
				int offsetHours = offsetMinutes / 60;
				offsetMinutes %= 60;
				output += ChatColor.DARK_AQUA + " All times are "
				       + timeZone.getDisplayName(daylightTime, TimeZone.SHORT)
				       + String.format(" (UTC%s%02d:%02d)", offsetHours > 0 ? "+" : "-", Math.abs(offsetHours),
				                       offsetMinutes)
				       + "\n";
			}
			catch (IllegalArgumentException iae) {
				JukeAlert.getInstance().getLogger().log(Level.WARNING,
					"Illegal Argument Exception while crafting timezone header in SendSnitchInfo", iae);
			}

			// Build ID header line
			String id = " ";
			Map<Integer, String> materials = new TreeMap<Integer, String>();
			for (SnitchAction entry: info) {
				Material mat = entry.getMaterial();
				if (mat != null && !mat.equals(Material.AIR)) {
					int mat_id = DeprecatedMethods.getMaterialId(mat);
					String mat_name = mat.name();
					if (mat_name != null && !mat_name.isEmpty() && !materials.containsKey(mat_id)) {
						materials.put(mat_id, mat_name);
					}
				}
			}
			for (Map.Entry<Integer, String> entry : materials.entrySet()) {
				id += String.format(ChatColor.WHITE + ", $" + ChatColor.RED + "%d " + ChatColor.WHITE + "= "
				    + ChatColor.RED + "%s", entry.getKey(), entry.getValue());
			}
			output += id.replaceFirst(",", "") + (id.length() > 1 ? "\n" : "");

			// Build table of entries
			output += ChatColor.GRAY + String.format("  %s %s %s", ChatFiller.fillString("Name", (double) 22),
				ChatFiller.fillString("Reason", (double) 22), ChatFiller.fillString("Details", (double) 30)) + "\n";
			for (SnitchAction entry : info) {
				output += JukeAlertLogger.createInfoString(entry, this.shouldCensor, this.isGroup) + "\n";
			}

			output += "\n";
			output += ChatColor.DARK_GRAY + " * Page " + offset + " ------------------------------------------";
			player.sendMessage(output);
		} else if (this.snitchName != null && !this.snitchName.trim().isEmpty()) {
			player.sendMessage(ChatColor.AQUA + " * Page " + offset + " is empty for snitch " + this.snitchName);
		} else {
			player.sendMessage(ChatColor.AQUA + " * Page " + offset + " is empty for unnamed snitch");
		}
	}
}
