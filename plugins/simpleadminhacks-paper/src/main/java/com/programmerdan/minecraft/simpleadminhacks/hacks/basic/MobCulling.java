package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Turtle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitTask;

public class MobCulling extends BasicHack {

    private BukkitTask task;

    @AutoLoad
    private int turtleAllowance;
    @AutoLoad
    private int turtleMaxAge;

    @AutoLoad
    private int striderAllowance;
    @AutoLoad
    private int striderMaxAge;

    private final Map<World, Integer> turtleCount = new WeakHashMap<>();
    private final Map<World, Integer> striderCount = new WeakHashMap<>();

    public MobCulling(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                Collection<Turtle> turtles = world.getEntitiesByClass(Turtle.class);
                turtleCount.put(world, turtles.size());
                if (turtleMaxAge >= 0) {
                    for (Turtle turtle : turtles) {
                        if (turtle.getTicksLived() > turtleMaxAge && turtle.customName() != null) {
                            Location location = turtle.getLocation();
                            plugin.getLogger().info("Despawning turtle with " + turtle.getTicksLived() + " ticks lived at " + location.getX() + " " + location.getY() + " " + location.getZ());
                            turtle.remove();
                        }
                    }
                }

                Collection<Strider> striders = world.getEntitiesByClass(Strider.class);
                striderCount.put(world, striders.size());
                if (striderMaxAge >= 0) {
                    for (Strider strider : striders) {
                        if (strider.getTicksLived() > striderMaxAge && strider.customName() != null && !strider.hasSaddle()) {
                            Location location = strider.getLocation();
                            plugin.getLogger().info("Despawning strider with " + strider.getTicksLived() + " ticks lived at " + location.getX() + " " + location.getY() + " " + location.getZ());
                            strider.remove();
                        }
                    }
                }
            }
        }, 0, 20 * 60 * 10);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.task.cancel();
    }

    @EventHandler
    public void on(PreCreatureSpawnEvent e) {
        if (e.getReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        // The mob allowance is to allow some mobs to spawn with a normal chance so when the world is first
        // generated, players don't find it impossible to find the mob. But later on in the world the mobs just
        // keep accumulating, so you need to stop them from spawning.
        if (e.getType() == EntityType.TURTLE && turtleAllowance >= 0) {
            Integer currentCount = turtleCount.get(e.getSpawnLocation().getWorld());
            if (currentCount == null || currentCount >= turtleAllowance) {
                if (ThreadLocalRandom.current().nextFloat() >= 0.02) {
                    e.setCancelled(true);
                }
            }
        } else if (e.getType() == EntityType.STRIDER && striderAllowance >= 0) {
            Integer currentCount = striderCount.get(e.getSpawnLocation().getWorld());
            if (currentCount == null || currentCount >= striderAllowance) {
                if (ThreadLocalRandom.current().nextFloat() >= 0.02) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
