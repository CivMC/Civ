package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.external.FinaleSettingManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public class ToolProtectionListener implements Listener {

    private FinaleSettingManager settingMan;

    public ToolProtectionListener(FinaleSettingManager settingMan) {
        this.settingMan = settingMan;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemDamageByPlayer(PlayerItemDamageEvent e) {
        if (!settingMan.useToolProtection(e.getPlayer().getUniqueId())) {
            return;
        }
        ItemStack is = e.getItem();
        PlayerInventory inventory = e.getPlayer().getInventory();
        if (is.getEnchantments().isEmpty()
            || !(inventory.getItemInMainHand() != is
            || inventory.getItemInOffHand() != is)) {
            return;
        }
        Damageable meta = ItemUtils.getDamageable(is);
        if (meta == null) {
            return;
        }
        int health = is.getType().getMaxDurability() - meta.getDamage();
        if (health <= settingMan.getToolProtectionThreshhold(e.getPlayer().getUniqueId())) {
            for (int i = 0; i < 5; i++) {
                e.getPlayer().sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Your %s is almost broken".formatted(
                    e.getItem().getType().name().replace('_', ' ').toLowerCase()
                ));
            }
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 10, 4));
        }
    }
}
