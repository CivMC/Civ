package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;

public class TooHappyGhasts extends BasicHack {

    public TooHappyGhasts(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getScheduler().runTaskTimer(SimpleAdminHacks.instance(), () -> {
            try {
                for (World world : Bukkit.getWorlds()) {
                    for (Player player : world.getPlayers()) {
                        Entity entity = player.getVehicle();
                        if (!(entity instanceof HappyGhast)) {
                            continue;
                        }
                        if (entity.getY() > 316) {
                            Location loc = entity.getLocation();
                            loc.setY(315.5);
                            for (Entity passenger : entity.getPassengers()) {
                                entity.removePassenger(passenger);
                            }
                            entity.teleport(loc);
                        }
                    }
                }
            } catch (RuntimeException ex) {
                SimpleAdminHacks.instance().getLogger().log(Level.WARNING, "Ticking ghast positions", ex);
            }
        }, 0, 1);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
