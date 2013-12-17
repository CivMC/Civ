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
    private String snitchName;
	private boolean shouldCensor;

	public SendSnitchInfo(List<String> info, Player player, int offset, String snitchName, boolean shouldCensor) {
		this.info = info;
		this.player = player;
		this.offset = offset;
        this.snitchName = snitchName;
		this.shouldCensor = shouldCensor;
        if (this.snitchName != null && this.snitchName.length() > 32) {
            this.snitchName = this.snitchName.substring(0, 32);
        }
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

            if (this.snitchName != null) {
			    output += ChatColor.WHITE + " Snitch Log for " + this.snitchName + " "
                       + ChatColor.DARK_GRAY + "-----------------------------------".substring(this.snitchName.length()) + "\n";
            } else {
			    output += ChatColor.WHITE + " Snitch Log " + ChatColor.DARK_GRAY + "----------------------------------------" + "\n";
            }
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
		} else if (this.snitchName != null) {
			player.sendMessage(ChatColor.AQUA + " * Page " + offset + " is empty for snitch " + this.snitchName);
        } else {
			player.sendMessage(ChatColor.AQUA + " * Page " + offset + " is empty");
		}

	}
}
