package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CombatUtil;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.ExtraDurabilityTracker;
import com.github.maxopoly.finale.misc.WeaponModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ThreadLocalRandom;

// Hacky code that hooks into the internals of LivingEntity#hurtServer for use by CombatUtil
public class FinaleListener implements Listener {

    @EventHandler
    public void onDurabilityChange(PlayerItemDamageEvent e) {
        // Axes can do double armour damage

        ItemStack item = CombatUtil.DAMAGING_ITEM;
        if (item == null || item.isEmpty()) {
            return;
        }

        WeaponModifier weaponMod = Finale.getPlugin().getManager().getWeaponModifer();
        double multi = weaponMod.getArmourDamageMultiplier(item);
        if (multi > 1 && multi <= 2 && ThreadLocalRandom.current().nextDouble() < multi - 1) {
            return;
        }


        ItemStack is = e.getItem();
        ArmourModifier armourModifier = Finale.getPlugin().getManager().getArmourModifier();
        ExtraDurabilityTracker extraDurabilityTracker = armourModifier.getExtraDurabilityTracker();
        boolean reduce = extraDurabilityTracker.reduceDurability(e.getPlayer(), is);
        e.setCancelled(!reduce);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(EntityDamageByEntityEvent event) {
        ItemStack item = CombatUtil.DAMAGING_ITEM;
        if (item == null) {
            return;
        }

        float armorDamage = (float) event.getDamage();
        armorDamage += (float) event.getDamage(EntityDamageEvent.DamageModifier.INVULNERABILITY_REDUCTION);
        CombatUtil.DAMAGED = armorDamage > 0;
    }
}
