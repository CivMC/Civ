package com.biggestnerd.namecolors;


import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;

public class NameColors extends ACivMod implements Listener {

    private static NameColors instance;

    public static NameColors getInstance() {
        return instance;
    }

    public static Component rainbowify(String text) {
        return MiniMessage.miniMessage().deserialize("<rainbow><name>", Placeholder.component("name", Component.text(text)));
    }

    private NameColorSetting setting;

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
        Player player = event.getPlayer();
        if (!player.hasPermission("namecolor.retainprefix")) {
            resetPrefix(player);
        }
        String value = setting.getValue(player);
        if (("rainbow".equals(value) && !player.hasPermission(NameColorSetting.RAINBOW_PERMISSION) || !player.hasPermission(NameColorSetting.COLOR_PERMISSION))) {
            setting.setValue(player, "");
        }
        updatePlayerName(player, setting.getValue(player));
    }

    private void resetPrefix(Player player) {
        if (getServer().getPluginManager().isPluginEnabled("TAB")) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                // For some reason setPrefix directly doesn't work
                TAB.getInstance().getConfiguration().getUsers().setProperty(player.getName(), "tabprefix", null, null, null);
                TAB.getInstance().getFeatureManager().onGroupChange(TAB.getInstance().getPlayer(player.getUniqueId()));
            }, 20L);
        }
    }

    public void updatePlayerName(Player player, String tag) {
        if (tag == null || tag.isEmpty()) {
            CivChat2.getInstance().getCivChat2Manager().removeCustomName(player.getUniqueId());
            if (getServer().getPluginManager().isPluginEnabled("TAB")) {
                //TAB is enabled, so lets reset the player name.
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    TabPlayer tabPlayer = TabAPI.getInstance()
                        .getPlayer(player.getUniqueId());
                    if (tabPlayer != null) {
                        TabAPI.getInstance().getTabListFormatManager().setName(tabPlayer, null);
                    }
                }, 20L);
            }
        } else {
            Component name = MiniMessage.miniMessage().deserialize("<" + tag + "><name>", Placeholder.component("name", Component.text(player.getName())));
            CivChat2.getInstance().getCivChat2Manager().setCustomName(player.getUniqueId(), name);
            if (getServer().getPluginManager().isPluginEnabled("TAB")) {
                //TAB enabled, so now we need to re-apply this name as a "custom name"
                //Side note: we do this temporarily so players if they lose their permission don't keep their colored name in TAB.
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    TabPlayer tabPlayer = TabAPI.getInstance()
                        .getPlayer(player.getUniqueId());
                    if (tabPlayer != null) {
                        TabAPI.getInstance().getTabListFormatManager().setName(tabPlayer, MiniMessage.miniMessage().serialize(name));
                    }
                }, 20);
            }
        }
    }
}
