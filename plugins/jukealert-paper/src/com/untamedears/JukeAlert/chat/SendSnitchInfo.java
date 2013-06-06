package com.untamedears.JukeAlert.chat;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class SendSnitchInfo implements Runnable {
	private List<String> info;
	private Player player;
	private int offset;

	public SendSnitchInfo(List<String> info, Player player, int offset) {
		this.info = info;
		this.player = player;
		this.offset = offset;
	}

	public void run() {
		if (info != null && !info.isEmpty()) {
			String output = "";
			String id = " ";

			for (String dataEntry : info) {
				if (dataEntry.contains("["))
				{
					String data = dataEntry.split("\\[")[0].replace(" ", "").replaceAll("§[0-9]", "").replaceAll("[^\\d]", "");
					if (Material.matchMaterial(data) != null)
						if (!id.contains(Material.matchMaterial(data).toString()))
							id += String.format(ChatColor.WHITE + ", $" + ChatColor.RED + "%s " + ChatColor.WHITE + "= " + ChatColor.RED + "%s", Integer.parseInt(data), Material.matchMaterial(data));
				}
			}
			
			id = id.replaceFirst(",", "") + "\n";

			output += ChatColor.WHITE + " Snitch Log " + ChatColor.DARK_GRAY + "----------------------------------------" + "\n";
			output += id;
			output += ChatColor.GRAY + String.format("  %s %s %s", ChatFiller.fillString("Name", (double) 25), ChatFiller.fillString("Reason", (double) 20), ChatFiller.fillString("Details", (double) 30)) + "\n";

			for (String dataEntry : info) {
				output += dataEntry + "\n";
			}

			output += "\n";
			output += ChatColor.DARK_GRAY + " * Page " + offset + " ------------------------------------------";
			output += "\n";
			player.sendMessage
			(output);
		} else {
			player.sendMessage(ChatColor.AQUA + " * Page " + offset + " is empty");
		}

	}
}