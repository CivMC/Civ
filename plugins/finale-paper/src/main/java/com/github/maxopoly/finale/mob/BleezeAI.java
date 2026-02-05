package com.github.maxopoly.finale.mob;

import com.github.maxopoly.finale.Finale;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

public class BleezeAI {

    private final Map<Mob, BleezeData> dataMap = new HashMap<>();

    class BleezeData {
        private int aiStopTicks;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(Finale.getPlugin(), () -> {
            for (ActiveMob mob : MythicBukkit.inst().getMobManager().getActiveMobs()) {
                if (!mob.getType().getInternalName().equals("Bleeze")) {
                    continue;
                }

                Mob entity = (Mob) mob.getEntity().getBukkitEntity();

                if (entity.getWorld().getWeatherDuration() > 1) {
                    entity.remove();
                    continue;
                }

                if (entity.getTarget() == null) {
                    continue;
                }

                BleezeData bleezeData = dataMap.computeIfAbsent(entity, k -> new BleezeData());
                if (bleezeData.aiStopTicks > 0) {
                    bleezeData.aiStopTicks--;
                    continue;
                }

                if (ThreadLocalRandom.current().nextInt(750) == 0) {
                    bleezeData.aiStopTicks = 350;
                    continue;
                }

                Vector vec3 = entity.getVelocity();
                double d = vec3.getY();
                LivingEntity target = entity.getTarget();
                if (entity.getY() < target.getY() + 5.0 && ThreadLocalRandom.current().nextBoolean()) {
                    d = Math.max(0.0, d);
                    d += 0.1 - d * 0.6F;
                }

                vec3 = new Vector(vec3.getX(), d, vec3.getZ());
                Vector vec31 = new Vector(target.getX() - entity.getX(), 0.0, target.getZ() - entity.getZ());
                if (vec31.lengthSquared() > 11 * 11) {
                    Vector vec32 = vec31.normalize();
                    vec3 = vec3.add(new Vector(vec32.getX() * 0.1 - vec3.getX() * 0.6, 0.0, vec32.getZ() * 0.1 - vec3.getZ() * 0.6));
                } else if (vec31.lengthSquared() < 7 * 7) {
                    Vector vec32 = vec31.normalize().multiply(-1);
                    vec3 = vec3.add(new Vector(vec32.getX() * 0.1 - vec3.getX() * 0.6, 0.0, vec32.getZ() * 0.1 - vec3.getZ() * 0.6));
                }
                entity.setVelocity(vec3);

                if (entity.getY() < entity.getTarget().getY() + 5) {
                    if (ThreadLocalRandom.current().nextInt(3) == 0) {
                        entity.setVelocity(entity.getVelocity().add(new Vector(0, 0.08f, 0)));
                    }
                }
            }
        }, 0, 1);
    }
}
