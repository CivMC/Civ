package com.github.maxopoly.essenceglue.reward;

import com.github.maxopoly.essenceglue.VirtualEssenceManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public class VirtualRewarder implements Rewarder {

    private final VirtualEssenceManager manager;

    public VirtualRewarder(VirtualEssenceManager manager) {
        this.manager = manager;
    }

    @Override
    public void reward(Player player, int amount) {
        manager.setEssence(player.getUniqueId(), manager.getEssence(player.getUniqueId()) + amount);
    }
}
