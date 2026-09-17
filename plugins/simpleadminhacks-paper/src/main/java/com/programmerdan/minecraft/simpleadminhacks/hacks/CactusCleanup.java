package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.EntitiesUnloadEvent;

public final class CactusCleanup extends BasicHack {

    public CactusCleanup(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitiesUnload(final EntitiesUnloadEvent event) {
        for (final Entity entity : event.getEntities()) {
            if (entity instanceof Item item && item.getItemStack().getType() == Material.CACTUS) {
                item.remove();
            }
        }
    }

}
