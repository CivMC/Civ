package net.civmc.kitpvp.ranked;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;

/**
 * The menu for RailSwitch. This is what all RailSwitch settings will be registered to.
 */
public final class KitPvpMenu extends MenuSection {

    public KitPvpMenu() {
        super("Kit PvP", "PvP server settings", PlayerSettingAPI.getMainMenu(),
            new ItemStack(Material.TNT));
    }

}
