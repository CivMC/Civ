package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

public class ReinforcedChestBreakConfig extends SimpleHackConfig{

    /**
     * This is the delay between messages
     */
    private int delay;

    /**
     * The message if someone is raiding a reinforced chest
     */
    private String message;

    public ReinforcedChestBreakConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
        super(plugin, base);
    }

    @Override
    protected void wireup(ConfigurationSection config) {
        this.delay = config.getInt("delay", 180);
        this.message = config.getString("message", "&4%player% is raiding a chest at %x% %y% %z%.");
    }

    public int getDelay() {
        return delay;
    }

    public String getMessage() {
        return message;
    }
}
