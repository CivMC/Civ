package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public final class ModSupportConfig extends SimpleHackConfig {
    public ModSupportConfig(
        final @NotNull SimpleAdminHacks plugin,
        final @NotNull ConfigurationSection config
    ) {
        super(plugin, config);
    }

    @Override
    protected void wireup(
        final @NotNull ConfigurationSection config
    ) {

    }
}
