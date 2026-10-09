package com.github.igotyou.FactoryMod.listeners;

import com.dre.brewery.Brew;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Makes drinking off a stack of brews take a single bottle, the way eating from a stack of food does.
 * <p>
 * BreweryX swaps the potion for a plain one so vanilla doesn't apply its effects, which means the server finishes using
 * that replacement instead of the held stack and puts the result in the hand slot. That overwrites the whole stack, so
 * without this a player drinking one brew off a stack of sixteen would be left holding a single glass bottle. Setting
 * the event's replacement decides what ends up in the hand, so the rest of the stack is put back there and the empty
 * bottle is handed over separately, since overriding the replacement drops the one the server made.
 * <p>
 * Runs after BreweryX's own handler, which sits on HIGHEST, so that a drink it refused is left well alone.
 */
public class BrewStackListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBrewStackConsume(PlayerItemConsumeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItem(event.getHand());
        if (held == null || held.getType() != Material.POTION || held.getAmount() <= 1 || !Brew.isBrew(held)) {
            return;
        }
        ItemStack remaining = held.clone();
        remaining.setAmount(held.getAmount() - 1);
        event.setReplacement(remaining);

        ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE);
        player.getInventory().addItem(bottle).values()
            .forEach(leftover -> player.getWorld().dropItem(player.getLocation(), leftover));
    }
}
