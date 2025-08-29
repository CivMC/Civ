package com.untamedears.realisticbiomes.growthconfig;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Objects;

public record BreakerConfig(Material type, Material type2, int maxYield) {

    public static BreakerConfig fromConfig(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String type = section.getString("type");
        String type2 = section.getString("type2");
        Material material = Objects.requireNonNull(Material.matchMaterial(type), "Unknown material: " + type);
        return new BreakerConfig(material, type2 == null ? null : Objects.requireNonNull(Material.matchMaterial(type2), "Unknown material: " + type2), section.getInt("max_yield"));
    }
}
