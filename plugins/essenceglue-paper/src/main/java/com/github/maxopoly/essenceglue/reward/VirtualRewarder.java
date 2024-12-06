package com.github.maxopoly.essenceglue.reward;

import com.github.maxopoly.essenceglue.VirtualEssenceManager;
import com.github.maxopoly.essenceglue.commands.EssenceCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
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
        player.sendMessage(Component.empty().color(EssenceCommand.NICE_BLUE)
            .append(Component.text("Use "))
            .append(Component.text("/essence withdraw", NamedTextColor.AQUA))
            .append(Component.text(" to claim your essence.")));
    }
}
