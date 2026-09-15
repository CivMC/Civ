package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.DataParser;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Strider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitTask;

public class MobCulling extends BasicHack {

    private BukkitTask task;

    @AutoLoad(processor = DataParser.ENTITY_TYPE)
    private List<EntityType> mobs = List.of();
    @AutoLoad
    private int allowance;
    @AutoLoad
    private int maxAge;
    private final Set<EntityType> mobTypes = EnumSet.noneOf(EntityType.class);
    private final Map<World, Map<EntityType, Integer>> mobCounts = new WeakHashMap<>();

    public MobCulling(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        for (final EntityType mob : mobs) {
            if (mob == null || !mob.isAlive()) {
                plugin.getLogger().warning("Ignoring invalid MobCulling entity type: " + mob);
                continue;
            }
            mobTypes.add(mob);
        }
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (final World world : Bukkit.getWorlds()) {
                final Map<EntityType, Integer> counts = new EnumMap<>(EntityType.class);
                for (final EntityType mobType : mobTypes) {
                    counts.put(mobType, 0);
                }
                for (final LivingEntity mob : world.getLivingEntities()) {
                    if (!mobTypes.contains(mob.getType())) {
                        continue;
                    }
                    counts.merge(mob.getType(), 1, Integer::sum);
                    if (maxAge >= 0 && mob.getTicksLived() > maxAge && mob.customName() == null
                        && (!(mob instanceof Strider strider) || !strider.hasSaddle())) {
                        final Location location = mob.getLocation();
                        plugin.getLogger().info("Despawning " + mob.getType() + " with " + mob.getTicksLived()
                            + " ticks lived at " + location.getX() + " " + location.getY() + " " + location.getZ());
                        mob.remove();
                    }
                }
                mobCounts.put(world, counts);
            }
        }, 0, 20 * 60 * 10);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (this.task != null) {
            this.task.cancel();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void on(final PreCreatureSpawnEvent e) {
        if (e.getReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        // The mob allowance is to allow some mobs to spawn with a normal chance so when the world is first
        // generated, players don't find it impossible to find the mob. But later on in the world the mobs just
        // keep accumulating, so you need to stop them from spawning.
        if (!mobTypes.contains(e.getType()) || allowance < 0) {
            return;
        }
        final Map<EntityType, Integer> counts = mobCounts.get(e.getSpawnLocation().getWorld());
        final Integer currentCount = counts == null ? null : counts.get(e.getType());
        if ((currentCount == null || currentCount >= allowance)
            && ThreadLocalRandom.current().nextFloat() >= 0.02) {
            e.setCancelled(true);
        }
    }
}
