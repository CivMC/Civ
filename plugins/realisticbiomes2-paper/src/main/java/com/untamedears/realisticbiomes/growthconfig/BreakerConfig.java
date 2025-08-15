package com.untamedears.realisticbiomes.growthconfig;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Objects;

public record BreakerConfig(Material type, int maxYield) {

    public static BreakerConfig fromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String type = section.getString("type");
        Material material = Objects.requireNonNull(Material.matchMaterial(type), "Unknown material: " + type);
        return new BreakerConfig(material, section.getInt("max_yield"));
    }
}
