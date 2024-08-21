package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

// Thanks kicky

public final class NoOozing extends BasicHack {
    public NoOozing(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler
    public void on(EntityPotionEffectEvent event) {
        PotionEffect newEffect = event.getNewEffect();
        if (newEffect == null) {
            return;
        }
        PotionEffectType type = newEffect.getType();
        // Mojang added some busted potions that break the balance by letting you get stuff too easily
        if (type == PotionEffectType.OOZING || type == PotionEffectType.WEAVING || type == PotionEffectType.INFESTED || type == PotionEffectType.WIND_CHARGED) {
            event.setCancelled(true);
        }
    }
}
