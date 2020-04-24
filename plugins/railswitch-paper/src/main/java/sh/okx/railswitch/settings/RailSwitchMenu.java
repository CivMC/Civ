package sh.okx.railswitch.settings;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;

/**
 * The menu for RailSwitch. This is what all RailSwitch settings will be registered to.
 */
public final class RailSwitchMenu extends MenuSection {

    public RailSwitchMenu() {
        super("RailSwitch", "Settings relating to RailSwitch", PlayerSettingAPI.getMainMenu(),
                new ItemStack(Material.RAIL));
    }

}
