package com.untamedears.JukeAlert.chat;

import java.util.List;

import org.bukkit.ChatColor;
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
            player.sendMessage(ChatColor.WHITE + " Snitch Log " + ChatColor.DARK_GRAY + "----------------------------------------");
            player.sendMessage(ChatColor.GRAY + String.format("  %s %s %s", ChatFiller.fillString("Name", (double) 25), ChatFiller.fillString("Reason", (double) 20), ChatFiller.fillString("Details", (double) 30)));
            for (String dataEntry : info) {
                player.sendMessage(dataEntry);
            }
            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_GRAY + " * Page " + offset + " ------------------------------------------");
            player.sendMessage("");
        } else {
            player.sendMessage(ChatColor.AQUA + " * Page " + offset + " is empty");
        }

    }
}