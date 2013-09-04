	package com.untamedears.JukeAlert.chat;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SendSnitchList implements Runnable {
	private List<String> info;
	private Player player;
	private int offset;

	public SendSnitchList(List<String> info, Player player, int offset) {
		this.info = info;
		this.player = player;
		this.offset = offset;
	}

	public void run() {
		if (info != null && !info.isEmpty()) {
			String output = "";
			
			output += ChatColor.WHITE + " Snitch List " + ChatColor.DARK_GRAY + "----------------------------------------" + "\n";
			output += ChatColor.GRAY + String.format("%s %s %s", ChatFiller.fillString("MC World", (double) 15), ChatFiller.fillString("Location", (double) 24), ChatFiller.fillString("Hours to Cull", (double) 21)) + "Group\n";
			
			
			output += ChatColor.WHITE;
			for (String dataEntry : info)
			{
				output += dataEntry + "\n";
			}

			output += "\n";
			output += ChatColor.DARK_GRAY + " * Page " + offset + " ------------------------------------------";
			player.sendMessage(output);
		} else {
			player.sendMessage(ChatColor.AQUA + " * Page " + offset + " is empty");
		}

	}
}
