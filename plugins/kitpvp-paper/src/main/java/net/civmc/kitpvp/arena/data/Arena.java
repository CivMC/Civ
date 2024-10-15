package net.civmc.kitpvp.arena.data;

import org.bukkit.Location;
import org.bukkit.Material;

public record Arena(
    String name,
    Location spawn,
    Material icon
) {

}
