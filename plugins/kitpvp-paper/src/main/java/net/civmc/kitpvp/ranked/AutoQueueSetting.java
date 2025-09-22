package net.civmc.kitpvp.ranked;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.players.settings.impl.IntegerSetting;

public final class AutoQueueSetting extends IntegerSetting {

    public AutoQueueSetting(JavaPlugin plugin) {
        super(plugin, 15, "Auto queue", "autoqueue", new ItemStack(Material.NETHER_STAR),
            "The time it takes for you to be auto requeued. Set to 0 to disable.", false);
    }

    @Override
    public String toText(Integer value) {
        if (value == null || value < 1) {
            return "disabled";
        }
        return Integer.toString(value);
    }

}
