package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftMinecart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prevents minecarts from derailing by forcibly slowing them down to 8m/s when they would otherwise derail
 */
public class AntiDerailment extends BasicHack {

    private Map<Minecart, Vector> previousTickMinecartVelocity;
    private boolean ticking = false;

    public AntiDerailment(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        previousTickMinecartVelocity = new HashMap<>();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        previousTickMinecartVelocity.clear();
    }

    @EventHandler
    public void on(EntityRemoveFromWorldEvent e) {
        if (e.getEntity() instanceof Minecart minecart) {
            previousTickMinecartVelocity.remove(minecart);
        }
    }

    @EventHandler
    public void onDerailment(VehicleMoveEvent e) {
        if (ticking) {
            return;
        }
        // This method is EXTREMELY fucked but basically if it detects a super fast minecart is going to derail, it will
        // undo one tick of movement and redo it at 8m/s (the default minecart speed)
        // To do this it also needs to store the velocity from the previous tick to recreate the movement properly

        if (!(e.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        List<Entity> passengers = minecart.getPassengers();
        if (passengers.size() != 1 || !(passengers.get(0) instanceof Player)) {
            previousTickMinecartVelocity.put(minecart, minecart.getVelocity());
            return;
        }

        if (minecart.getMaxSpeed() == 0.4D || !previousTickMinecartVelocity.containsKey(minecart)) {
            previousTickMinecartVelocity.put(minecart, minecart.getVelocity());
            return;
        }

        Block fromBlock = e.getFrom().getBlock();
        if (!Tag.RAILS.isTagged(fromBlock.getType())) {
            previousTickMinecartVelocity.put(minecart, minecart.getVelocity());
            return;
        }

        Block block = e.getTo().getBlock();
        if (Tag.RAILS.isTagged(block.getType())) {
            previousTickMinecartVelocity.put(minecart, minecart.getVelocity());
            return;
        }

        AbstractMinecart handle = ((CraftMinecart) minecart).getHandle();
        handle.setPos(e.getFrom().getX(), e.getFrom().getY(), e.getFrom().getZ());
        minecart.setVelocity(previousTickMinecartVelocity.get(minecart));
        double maxSpeed = minecart.getMaxSpeed();
        minecart.setMaxSpeed(0.4D); // 8m/s, default minecart speed
        try {
            ticking = true;
            handle.tick();
        } finally {
            ticking = false;
        }
        minecart.setMaxSpeed(maxSpeed);
        previousTickMinecartVelocity.put(minecart, minecart.getVelocity());
    }
}
