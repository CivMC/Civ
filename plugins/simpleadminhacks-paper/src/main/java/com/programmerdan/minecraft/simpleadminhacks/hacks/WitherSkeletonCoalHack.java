package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class WitherSkeletonCoalHack extends BasicHack {

    @AutoLoad
    private double coalDropChance;

    public WitherSkeletonCoalHack(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(final EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.WITHER_SKELETON) {
            return;
        }

        final Iterator<ItemStack> drops = event.getDrops().iterator();
        while (drops.hasNext()) {
            final ItemStack drop = drops.next();
            if (drop.getType() != Material.COAL) {
                continue;
            }
            int kept = 0;
            for (int amount = 0; amount < drop.getAmount(); amount++) {
                if (ThreadLocalRandom.current().nextDouble() < coalDropChance) {
                    kept++;
                }
            }
            if (kept == 0) {
                drops.remove();
            } else {
                drop.setAmount(kept);
            }
        }
    }

}
