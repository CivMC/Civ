package com.untamedears.itemexchange;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;

public final class ItemExchangeSettings {
    public static final MenuSection MAIN_MENU = PlayerSettingAPI.getMainMenu().createMenuSection(
        "ItemExchange",
        "Settings for ItemExchange",
        new ItemStack(Material.CHEST)
    );

    public static final BooleanSetting RECEIVE_RECEIPTS = new BooleanSetting(
        ItemExchangePlugin.getInstance(),
        true,
        "Receive receipts?",
        "ie_receive_receipts",
        "Would you like your purchases to print a receipt? (Some exchanges will always produce a receipt regardless of this setting!)"
    );

    @ApiStatus.Internal
    public static void init() {
        PlayerSettingAPI.registerSetting(RECEIVE_RECEIPTS, MAIN_MENU);
    }
}
