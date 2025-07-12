package com.github.maxopoly.finale.misc.crossbow;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.CooldownHandler;
import com.github.maxopoly.finale.misc.ItemUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.manager.BastionBlockManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.ExplosionResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;

public class CrossbowHandler {

    private boolean enabled;
    private int radius;
    private float artilleryDamage;
    private double reinforcementDamage;
    private double bastionDamage;

    private Map<String, AntiAirMissile> antiAirMissiles;

    private CooldownHandler cooldowns;

    public CrossbowHandler(boolean enabled, int radius, float artilleryDamage, double reinforcementDamage, double bastionDamage, long cooldown, Map<String, AntiAirMissile> antiAirMissiles) {
        this.enabled = enabled;
        this.radius = radius;
        this.artilleryDamage = artilleryDamage;
        this.reinforcementDamage = reinforcementDamage;
        this.bastionDamage = bastionDamage;

        this.antiAirMissiles = antiAirMissiles;

        this.cooldowns = new CooldownHandler("crossbowCooldown", cooldown, (player, cooldowns) ->
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "Crossbow: " +
                ChatColor.RED + CooldownHandler.formatCoolDown(cooldowns, player.getUniqueId())
        );

        new BukkitRunnable() {

            @Override
            public void run() {
                for (AntiAirMissile antiAirMissile : antiAirMissiles.values()) {
                    antiAirMissile.progressInstances();
                }
            }

        }.runTaskTimer(Finale.getPlugin(), 0L, 1L);
    }

    public boolean onCooldown(Player shooter) {
        return cooldowns.onCooldown(shooter);
    }

    public void putOnCooldown(Player shooter) {
        cooldowns.putOnCooldown(shooter);
    }

    public AntiAirMissile getAntiAirMissile(ItemStack is) {
        String key = ItemUtil.getAAKey(is);
        return getAntiAirMissile(key);
    }

    public AntiAirMissile getAntiAirMissile(String key) {
        return antiAirMissiles.get(key);
    }

    private Random random = new Random();

    public void handleFireworkExplode(FireworkExplodeEvent event) {
        Firework firework = event.getEntity();
        Player shooter = (Player) firework.getShooter();
        Location loc = firework.getLocation();

        if (Bukkit.getPluginManager().isPluginEnabled("Bastion")) {
            BastionBlockManager bastionBlockManager = Bastion.getBastionManager();
            Set<BastionBlock> bastions = bastionBlockManager.getBlockingBastions(loc);
            if (!bastions.isEmpty()) {
                bastionBlockManager.erodeFromPlace(shooter, bastions);
            }
        }

        Block block = loc.getBlock();

        Set<Block> explodeLocs = new HashSet<>();
        explodeLocs.add(block);
        Deque<Block> explodeLocQueue = new ArrayDeque<>();
        explodeLocQueue.add(block);

        Block queueExplodeBlock;
        while ((queueExplodeBlock = explodeLocQueue.poll()) != null) {
            if (!queueExplodeBlock.isEmpty()) {
                continue;
            }
            for (BlockFace face : new BlockFace[] {BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
                Block relative = queueExplodeBlock.getRelative(face);
                double dist = relative.getLocation().distanceSquared(block.getLocation());
                if (dist < radius * radius && explodeLocs.add(relative)) {
                    explodeLocQueue.add(relative);
                }
            }
        }

        List<Block> explodeLocsList = new ArrayList<>(explodeLocs);
        BlockExplodeEvent explodeEvent = new BlockExplodeEvent(block, block.getState(), explodeLocsList, 0, ExplosionResult.DESTROY_WITH_DECAY);
        Bukkit.getPluginManager().callEvent(explodeEvent);
        if (explodeEvent.isCancelled()) {
            return;
        }

        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1, 0, 0, 0, 0);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, ThreadLocalRandom.current().nextFloat(0.56f, 0.84f));

        for (Block explodeBlock : explodeLocsList) {
            if (explodeBlock.getType() != Material.AIR && explodeBlock.getType() != Material.BEDROCK && explodeBlock.getType() != Material.BARRIER) {
                if (Bukkit.getPluginManager().isPluginEnabled("Citadel")) {
                    Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(explodeBlock);
                    if (rein != null) {
                        for (int i = 0; i < reinforcementDamage; i++) {
                            if (rein.isBroken()) {
                                break;
                            }
                            float damage = ReinforcementLogic.getDamageApplied(rein, null, null);
                            ReinforcementLogic.damageReinforcement(rein, damage, shooter);
                        }
                        continue;
                    }
                }

                if (explodeBlock.getType().getBlastResistance() < 10) {
                    Material type = Material.AIR;

                    if (explodeBlock.getRelative(BlockFace.DOWN, 1).getType().isSolid() && random.nextInt(3) == 0) {
                        type = Material.FIRE;
                    }

                    explodeBlock.setType(type);
                }
            }
        }
    }
}
