package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.persistence.PersistentDataType;
import vg.civcraft.mc.civmodcore.pdc.extensions.PersistentDataContainerExtensions;

import java.util.logging.Level;

public class FasterHorses extends BasicHack {

    @AutoLoad
    private double minSpeed;
    @AutoLoad
    private double maxSpeed;
    private final NamespacedKey speedChangedKey = new NamespacedKey(plugin, "speed-changed");

    public FasterHorses(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void on(EntityBreedEvent event) {
        if (event.getEntity().getType() != EntityType.HORSE) {
            return;
        }
        double dadSpeed = event.getFather().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
        double mumSpeed = event.getMother().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
        double irwinHallDist = (Math.random() * 0.3 + Math.random() * 0.3 + Math.random() * 0.3) * ((this.maxSpeed - this.minSpeed) / 0.9) + this.minSpeed;
        double newSpeed = (dadSpeed + mumSpeed + irwinHallDist) / 3;
        if (newSpeed < minSpeed) {
            newSpeed = minSpeed;
        } else if (newSpeed > maxSpeed) {
            newSpeed = maxSpeed;
        }
        event.getEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(newSpeed);
        event.getEntity().getPersistentDataContainer().set(speedChangedKey, PersistentDataType.BOOLEAN, true);
        plugin.getLogger().log(Level.INFO, "Horse bred to have speed: " + newSpeed);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void on(CreatureSpawnEvent event) {
        if (event.getEntity().getType() != EntityType.HORSE) {
            return;
        }
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING) {
            return;
        }
        if (((Tameable) event.getEntity()).isTamed() || event.getEntity().getPersistentDataContainer().has(speedChangedKey, PersistentDataType.BOOLEAN)) {
            return;
        }

        AttributeInstance moveSpeed = event.getEntity().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (moveSpeed == null) {
            return;
        }
        double irwinHallDist = (Math.random() * 0.3 + Math.random() * 0.3 + Math.random() * 0.3) * ((this.maxSpeed - this.minSpeed) / 0.9) + this.minSpeed;
        moveSpeed.setBaseValue(irwinHallDist);
        event.getEntity().getPersistentDataContainer().set(speedChangedKey, PersistentDataType.BOOLEAN, true);
        plugin.getLogger().log(Level.INFO, "Setting Horse Speed to: " + irwinHallDist);
    }
}
