package com.untamedears.JukeAlert.manager;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerManager {

    private ArrayList<Player> players;

    public PlayerManager() {
        loadPlayers();
    }

    public void loadPlayers() {
    	players = new ArrayList<Player>(Arrays.asList(Bukkit.getServer().getOnlinePlayers()));
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

}
