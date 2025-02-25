package com.github.maxopoly.essenceglue.reward;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import java.util.HashMap;

public class PhysicalRewarder implements Rewarder {

    private final ItemMap items;

    public PhysicalRewarder(ItemMap items) {
        this.items = items;
    }

    @Override
    public void reward(Player p, int amount) {
        ItemMap clone = items.clone();
        clone.multiplyContent(amount);
        if (clone.fitsIn(p.getInventory())) {
            for (ItemStack is : clone.getItemStackRepresentation()) {
                HashMap<Integer, ItemStack> notAdded = p.getInventory().addItem(is);
                for (ItemStack toDrop : notAdded.values()) {
                    p.sendMessage(ChatColor.GREEN + "Your inventory was full, so it was dropped on the ground");
                    p.getWorld().dropItemNaturally(p.getLocation(), toDrop);
                }
            }
        } else {
            p.sendMessage(ChatColor.GREEN + "Your inventory was full, so it was dropped on the ground");
            for (ItemStack is : clone.getItemStackRepresentation()) {
                p.getWorld().dropItemNaturally(p.getLocation(), is);
            }
        }
    }
}
