package com.untamedears.JukeAlert.chat;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.JukeAlert;

import net.md_5.bungee.api.chat.TextComponent;

public class SendSnitchList implements Runnable {
    private List<TextComponent> info;
    private String worldName;
    private Player player;
    private int offset;

    public SendSnitchList(List<TextComponent> info, String worldName, Player player, int offset) {
        this.info = info;
        this.worldName = worldName;
        this.player = player;
        this.offset = offset;
    }

    public void run() {
        if (info != null && !info.isEmpty()) {
            double locationColWidth = (double)21;
            double cullColWidth = (double)17;
            double groupColWidth = (double)20;
            if (JukeAlert.getInstance().getConfigManager().getMultipleWorldSupport() == true){
                locationColWidth = (double)31;
                groupColWidth = (double)15;
            }
            String topLine = " Snitch List ";
            if (worldName != null && !worldName.isEmpty()){
                topLine += "for " + worldName + " ";
            }
        
            topLine = ChatColor.WHITE + topLine + ChatColor.DARK_GRAY 
                    + "--------------------------------------------------------".substring(topLine.length()) + "\n";
            
            String columnNames = ChatColor.GRAY + String.format("    %s %s %s", ChatFiller.fillString("Location", locationColWidth), 
                                                                ChatFiller.fillString("Cull (h)", cullColWidth), 
                                                                ChatFiller.fillString("Group", groupColWidth) + "Name\n")
                                                + ChatColor.WHITE;
            TextComponent output = new TextComponent(topLine);
            output.addExtra(columnNames);
            for (TextComponent dataEntry : info){
                output.addExtra(dataEntry);
            }
            
            String bottomLine = ChatColor.DARK_GRAY + " * Page " + offset + " ";
            bottomLine = bottomLine + "-------------------------------------------------------".substring(bottomLine.length());
            output.addExtra(bottomLine);
            player.spigot().sendMessage(output);
        } else {
            player.sendMessage(ChatColor.AQUA + " * Page " + offset + " is empty");
        }
    }
}

