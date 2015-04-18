package com.untamedears.JukeAlert.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerManager {

    public Player[] getPlayers() {
    	Player[] players = new Player[Bukkit.getServer().getOnlinePlayers().size()];
    	int x = 0;
    	for (Player p: Bukkit.getServer().getOnlinePlayers()){
    		players[x] = p;
    		x++;
    	}
        return players;
    }

}
