package net.civmc.kitpvp.arena.data;

import org.bukkit.Location;
import org.bukkit.Material;

public record Arena(
    String name,
    String displayName,
    String category,
    Location spawn,
    Material icon
) {
    public String displayName() {
        return this.displayName == null ? this.name : this.displayName;
    }
}
