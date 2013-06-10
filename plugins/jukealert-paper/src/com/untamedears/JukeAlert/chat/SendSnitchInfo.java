package com.untamedears.JukeAlert.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;


public class SendSnitchInfo implements Runnable {
	private List<String> info;
	private Player player;
	private int offset;
	private boolean shouldCensor;

	public SendSnitchInfo(List<String> info, Player player, int offset, boolean shouldCensor) {
		this.info = info;
		this.player = player;
		this.offset = offset;
		this.shouldCensor = shouldCensor;
	}

	public void run() {
		if (info != null && !info.isEmpty()) {
			String output = "";
			String id = " ";

			for (String dataEntry : info) {
				if (dataEntry.contains("["))
				{
					String data = ChatColor.stripColor(dataEntry.split("\\[")[0]);
					data = data.split(" ")[data.split(" ").length - 1];
					if (Material.matchMaterial(data) != null)
						if (!id.contains(Material.matchMaterial(data).toString()))
							id += String.format(ChatColor.WHITE + ", $" + ChatColor.RED + "%s " + ChatColor.WHITE + "= " + ChatColor.RED + "%s", Integer.parseInt(data), Material.matchMaterial(data));
				}
			}

			id = id.replaceFirst(",", "") + (id.length() > 1 ? "\n" : "");

			output += ChatColor.WHITE + " Snitch Log " + ChatColor.DARK_GRAY + "----------------------------------------" + "\n";
			output += id;
			output += ChatColor.GRAY + String.format("  %s %s %s", ChatFiller.fillString("Name", (double) 22), ChatFiller.fillString("Reason", (double) 22), ChatFiller.fillString("Details", (double) 30)) + "\n";
			
			for (String dataEntry : info)
			{
				if (shouldCensor)
				{
					output += dataEntry.replaceAll("\\[((-)?[0-9]*( )?){3}\\]", "[*** *** ***]") + "\n";
				}
				else
				{
					output += dataEntry + "\n";
				}
			}

			output += "\n";
			output += ChatColor.DARK_GRAY + " * Page " + offset + " ------------------------------------------";
			player.sendMessage(output);
		} else {
			player.sendMessage(ChatColor.AQUA + " * Page " + offset + " is empty");
		}

	}
}
