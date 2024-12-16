package net.civmc.heliodor.heliodor.infusion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.heliodor.infusion.chunkmeta.CauldronInfusion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;

public class InfusionManager {

    private static final int LAVA_TICKS = 100 * 4; // 1m 40s
    private static final int CHARGE_AMOUNT = 5;

    private final List<CauldronInfusion> infusions = new ArrayList<>();

    public InfusionManager() {
        Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(HeliodorPlugin.class), () -> {
            try {
                this.tickInfusionParticles();
            } catch (RuntimeException e) {
                JavaPlugin.getPlugin(HeliodorPlugin.class).getLogger()
                    .log(Level.WARNING, "Ticking infusion particles", e);
            }
        }, 5, 5);
    }

    private void tickInfusionParticles() {
        Iterator<CauldronInfusion> iterator = infusions.iterator();
        while (iterator.hasNext()) {
            CauldronInfusion infusion = iterator.next();
            if (infusion.getCacheState() == CacheState.DELETED) {
                iterator.remove();
                continue;
            }
            Location location = infusion.getLocation();
            if (!location.getChunk().isLoaded()) {
                iterator.remove();
                continue;
            }

            if (!isValidInfusion(infusion)) {
                iterator.remove();
                continue;
            } else if (!isProgressingInfusion(infusion)) {
                continue;
            }

            location.getBlock().setType(Material.GOLD_BLOCK);

            if (infusion.getTicks() >= LAVA_TICKS) {
                if (!location.getNearbyPlayers(35).isEmpty()) {
                    location.getWorld().spawnParticle(Particle.LARGE_SMOKE, location.clone().add(0.5, 0.25, 0.5), 40, 0.25, 0.25, 0.25, 0.05);
                }
                this.chargeInfusion(infusion);
            } else {
                if (ThreadLocalRandom.current().nextInt(16) == 0) {
                    if (!location.getNearbyPlayers(35).isEmpty()) {
                        location.getWorld().spawnParticle(Particle.LAVA, location.clone().add(0.5, 0.5, 0.5), 5, 0.25, 0.25, 0.25);
                    }
                }
                infusion.setTicks(infusion.getTicks() + 1);
            }
        }
    }

    private boolean isValidInfusion(CauldronInfusion infusion) {
        Location location = infusion.getLocation();
        return location.getBlock().getType() == Material.GOLD_BLOCK || location.getBlock().getType() == Material.EMERALD_BLOCK;
    }

    private boolean isProgressingInfusion(CauldronInfusion infusion) {
        Location location = infusion.getLocation();
        return location.getBlock().getRelative(BlockFace.DOWN).getType() == Material.LAVA_CAULDRON
            && infusion.getCharge() < infusion.getMaxCharge();
    }

    private void chargeInfusion(CauldronInfusion infusion) {
        infusion.getLocation().getBlock().getRelative(BlockFace.DOWN).setType(Material.CAULDRON);
        infusion.getLocation().getBlock().setType(Material.EMERALD_BLOCK);
        infusion.setCharge(Math.min(infusion.getMaxCharge(), infusion.getCharge() + CHARGE_AMOUNT));
        infusion.setTicks(0);
    }

    public boolean addInfusion(CauldronInfusion infusion) {
        if (!isValidInfusion(infusion)) {
            return false;
        } else {
            this.infusions.add(infusion);
            return true;
        }
    }

    public void removeInfusions(Collection<CauldronInfusion> infusions) {
        this.infusions.removeAll(infusions);
    }
}
