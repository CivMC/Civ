package com.biggestnerd.namecolors;


import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import static com.biggestnerd.namecolors.NameColorSetting.COLOR_PERMISSION;
import static com.biggestnerd.namecolors.NameColorSetting.RAINBOW_PERMISSION;
import static com.biggestnerd.namecolors.NameColorSetting.RGB_COLOR_PERMISSION;

public class NameColors extends ACivMod implements Listener {

    private static NameColors instance;

    public static NameColors getInstance() {
        return instance;
    }

    public NameColorSetting setting;

    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();
        setting = new NameColorSetting(this);
        PlayerSettingAPI.registerSetting(setting, PlayerSettingAPI.getMainMenu());
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(setting.getValue(event.getPlayer()) == null) {
            setting.setValue(event.getPlayer(), event.getPlayer().name());
            return;
        }
        updatePlayerName(event.getPlayer());
    }

    public void updatePlayerName(Player player) {
        //If player has lost permission but also currently has a color set, then lets reset them on login
        if (!doesPlayerHavePermissions(player)) {
            player.displayName(player.name());
            player.playerListName(player.name());
        } else {
            player.displayName(setting.getValue(player.getUniqueId()));
            player.playerListName(setting.getValue(player.getUniqueId()));
        }
    }

    public static boolean doesPlayerHavePermissions(Player player) {
        return player != null
            && player.hasPermission(COLOR_PERMISSION)
            && player.hasPermission(RAINBOW_PERMISSION)
            && player.hasPermission(RGB_COLOR_PERMISSION);
    }
}
