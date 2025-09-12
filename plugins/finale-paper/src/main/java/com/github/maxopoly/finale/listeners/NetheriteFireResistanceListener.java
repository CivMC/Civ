package com.github.maxopoly.finale.listeners;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;

public class NetheriteFireResistanceListener {

    private final Plugin plugin;

    public NetheriteFireResistanceListener(Plugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                this.applyFireResistance();
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Applying fire resistance to netherite armour", ex);
            }
        }, 20, 20);
    }

    public void applyFireResistance() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            int pieces = 0;
            int mpieces = 0;
            for (ItemStack armour : player.getInventory().getArmorContents()) {
                if (armour == null) {
                    continue;
                }
                Material type = armour.getType();
                switch (type) {
                    case NETHERITE_BOOTS, NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS -> pieces++;
                }
                String customItem = CustomItem.getCustomItemKey(armour);
                if (customItem != null && customItem.startsWith("meteoric_iron_")) {
                    mpieces++;
                }
            }
            if (pieces == 4 || mpieces == 4) {
                PotionEffect currentEffect = player.getPotionEffect(PotionEffectType.FIRE_RESISTANCE);
                if (currentEffect == null || currentEffect.getDuration() < 5 * 20 + 19) {
                    player.addPotionEffect(new PotionEffect(
                        PotionEffectType.FIRE_RESISTANCE,
                        8 * 20 + 19,
                        0,
                        true,
                        false,
                        true
                    ));
                }
            }
        }
    }

}
