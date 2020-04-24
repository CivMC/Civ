package sh.okx.railswitch.settings;

import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.StringSetting;

/**
 * ResetSetting, a setting that functions more like a GUI button than an actual setting.
 */
public final class ResetSetting extends StringSetting {

    private final DestinationSetting destinationSetting;

    public ResetSetting(JavaPlugin plugin, DestinationSetting destinationSetting) {
        super(plugin, "", "Clear Destination", "dest", new ItemStack(Material.BARRIER), "Clears your destination.");
        this.destinationSetting = destinationSetting;
    }

    @Override
    public void handleMenuClick(Player player, MenuSection menu) {
        resetPlayerDestination(player);
        menu.showScreen(player);
    }

    /**
     * Resets a player's destination value.
     *
     * @param player The player to reset the destination for.
     */
    public void resetPlayerDestination(Player player) {
        this.destinationSetting.setValue(player, "");
        player.sendMessage(ChatColor.GREEN + "Your destination has been reset.");
    }

    @Override
    public String getValue(UUID player) {
        return this.destinationSetting.getValue(player);
    }

    @Override
    public String toText(String value) {
        return this.destinationSetting.toText(value);
    }

}
