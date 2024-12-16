package com.github.maxopoly.essenceglue.reward;

import org.bukkit.entity.Player;

public interface Rewarder {
    void reward(Player player, int amount);
}
