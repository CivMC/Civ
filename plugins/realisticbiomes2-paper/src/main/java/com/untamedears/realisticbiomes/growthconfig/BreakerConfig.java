package com.untamedears.realisticbiomes.growthconfig;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Objects;

public record BreakerConfig(Material type, int maxYield) {

    public static BreakerConfig fromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        return new BreakerConfig(Objects.requireNonNull(Material.matchMaterial(section.getString("type"))), section.getInt("max_yield"));
    }
}
