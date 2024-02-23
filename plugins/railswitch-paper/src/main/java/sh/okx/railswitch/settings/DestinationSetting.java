package sh.okx.railswitch.settings;

import com.google.common.base.Strings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.players.settings.impl.StringSetting;

/**
 * Setting representing a player's destination.
 */
public final class DestinationSetting extends StringSetting {

    public DestinationSetting(JavaPlugin plugin) {
        super(plugin, "", "Destination", "dest", new ItemStack(Material.MINECART),
                "The destination(s) that will be used to route you at rail junctions.");
    }

    @Override
    public String toText(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return "<empty>";
        }
        return value;
    }

}
