package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.combat.CombatUtil;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.ExtraDurabilityTracker;
import com.github.maxopoly.finale.misc.WeaponModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ThreadLocalRandom;

public class ExtraDurabilityListener implements Listener {

    @EventHandler
    public void onDurabilityChange(PlayerItemDamageEvent e) {
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

}
