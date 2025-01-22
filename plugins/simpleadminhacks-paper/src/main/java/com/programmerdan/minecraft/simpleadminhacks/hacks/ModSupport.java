package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.ModSupportConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public final class ModSupport extends SimpleHack<ModSupportConfig> implements Listener {
    private static final String MOD_SUPPORT_CHANNEL = "sah:mod_support";

    public ModSupport(
        final @NotNull SimpleAdminHacks plugin,
        final @NotNull ModSupportConfig config
    ) {
        super(plugin, config);
    }

    public static @NotNull ModSupportConfig generate(
        final @NotNull SimpleAdminHacks plugin,
        final @NotNull ConfigurationSection config
    ) {
        return new ModSupportConfig(plugin, config);
    }

    @Override
    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this.plugin, MOD_SUPPORT_CHANNEL);
        this.plugin.registerListener(this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        HandlerList.unregisterAll(this);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this.plugin, MOD_SUPPORT_CHANNEL);
    }
}
