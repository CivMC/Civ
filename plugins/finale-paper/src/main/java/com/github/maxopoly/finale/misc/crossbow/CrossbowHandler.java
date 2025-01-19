package com.github.maxopoly.finale.misc.crossbow;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.CooldownHandler;
import com.github.maxopoly.finale.misc.ItemUtil;

import java.util.ArrayList;
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
import org.bukkit.Effect;
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

    public boolean isEnabled() {
        return enabled;
    }

    public double getRadius() {
        return radius;
    }

    public float getArtilleryDamage() {
        return artilleryDamage;
    }

    public double getReinforcementDamage() {
        return reinforcementDamage;
    }

    public double getBastionDamage() {
        return bastionDamage;
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

        List<Block> explodeLocs = getCircle(loc, radius, radius, false, true, 0);
        BlockExplodeEvent explodeEvent = new BlockExplodeEvent(loc.getBlock(), loc.getBlock().getState(), explodeLocs, 0, ExplosionResult.DESTROY_WITH_DECAY);
        Bukkit.getPluginManager().callEvent(explodeEvent);
        if (explodeEvent.isCancelled()) {
            return;
        }

        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 1, 0, 0, 0, 0);
        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, ThreadLocalRandom.current().nextFloat(0.56f, 0.84f));

        for (Block explodeBlock : explodeLocs) {
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

                Material type = Material.AIR;

                if (explodeBlock.getRelative(BlockFace.DOWN, 1).getType().isSolid() && random.nextInt(3) == 0) {
                    type = Material.FIRE;
                }

                explodeBlock.setType(type);
            }
        }
    }

    public static List<Block> getCircle(final Location loc, final int radius, final int height, final boolean hollow, final boolean sphere, final int plusY) {
        final List<Block> circleblocks = new ArrayList<>();
        final int cx = loc.getBlockX();
        final int cy = loc.getBlockY();
        final int cz = loc.getBlockZ();

        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                for (int y = (sphere ? cy - radius : cy); y < (sphere ? cy + radius : cy + height); y++) {
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);

                    if (dist < radius * radius && !(hollow && dist < (radius - 1) * (radius - 1))) {
                        final Location l = new Location(loc.getWorld(), x, y + plusY, z);
                        circleblocks.add(l.getBlock());
                    }
                }
            }
        }
        return circleblocks;
    }
}
