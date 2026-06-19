package net.civmc.zorweth.mechanics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.civmc.zorweth.ZorwethPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.CraftingRecipe;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;

public class OilMechanics {

    private final String world;
    private final List<OilVein> oilPos;
    private final int radius;
    private final long activeExtractorWindowMillis;
    private final Map<OilVein, Map<Location, Long>> activeExtractors;

    public OilMechanics(ZorwethPlugin plugin, String mechanicsWorld) {
        YamlConfiguration mechanics = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "mechanics.yml"));

        List<OilVein> oilPos = new ArrayList<>();
        List<Map<?, ?>> positions = mechanics.getMapList("oil");
        for (Map<?, ?> position : positions) {
            oilPos.add(new OilVein((int) position.get("x"), (int) position.get("z"), (int) position.get("yield")));
        }

        this.oilPos = oilPos;
        this.world = mechanicsWorld;
        this.radius = mechanics.getInt("radius");
        this.activeExtractorWindowMillis = ConfigHelper.parseTime(mechanics.getString("active-extractor-window", "30s"));
        this.activeExtractors = new HashMap<>();

        List<CraftingRecipe> recipes = new ArrayList<>(TotemRecipes.getRecipes());
        recipes.add(SeismicScanner.getRecipe(plugin));
        for (CraftingRecipe recipe : recipes) {
            Bukkit.addRecipe(recipe);
        }
        plugin.getServer().getPluginManager().registerEvents(new SeismicScannerListener(recipes, this), plugin);

        Fuel.createCrudeOil();
        Fuel.createRocketFuel();
    }

    public int recordOilExtraction(final Location location) {
        OilVein vein = findOilVein(location);
        if (vein == null) {
            return 0;
        }

        long now = System.currentTimeMillis();
        Map<Location, Long> extractors = activeExtractors.computeIfAbsent(vein, ignored -> new HashMap<>());
        pruneExpiredExtractors(extractors, now);
        extractors.put(location.toBlockLocation(), now);
        return vein.yield() * extractors.size();
    }

    private OilVein findOilVein(final Location location) {
        if (!world.equals(location.getWorld().getName())) {
            return null;
        }

        OilVein richestVein = null;
        for (OilVein vein : oilPos) {
            if (location.toBlockLocation().distanceSquared(new Location(location.getWorld(), vein.x(), location.getY(), vein.z())) < radius * radius) {
                if (richestVein == null || vein.yield() > richestVein.yield()) {
                    richestVein = vein;
                }
            }
        }
        return richestVein;
    }

    public VeinPing ping(final Location location) {
        if (!world.equals(location.getWorld().getName())) {
            return null;
        }

        OilVein closestVein = null;
        double distance = Double.MAX_VALUE;

        for (OilVein vein : oilPos) {
            double veinDistance = ((vein.x() - location.blockX()) * (vein.x() - location.blockX()))
                + ((vein.z() -  location.blockZ()) * (vein.z() - location.blockZ()));

            if (closestVein == null || veinDistance < distance) {
                closestVein = vein;
                distance = veinDistance;
            }
        }

        if (distance < radius * radius) {
            return VeinPing.VEIN;
        } else if (distance < 100 * 100) {
            return VeinPing.HIGH;
        } else if (distance < 200 * 200) {
            return VeinPing.LOW;
        } else {
            return null;
        }
    }

    public enum VeinPing {
        VEIN,
        HIGH,
        LOW
    }

    private void pruneExpiredExtractors(final Map<Location, Long> extractors, final long now) {
        extractors.entrySet().removeIf(
            entry -> now - entry.getValue() > activeExtractorWindowMillis);
    }
}
