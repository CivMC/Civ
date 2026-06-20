package com.github.igotyou.FactoryMod.recipes.heliodor;

import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import com.github.igotyou.FactoryMod.recipes.EffectFeasibility;
import com.github.igotyou.FactoryMod.recipes.InputRecipe;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.vein.VeinSpawner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public final class MeteorForecastRecipe extends InputRecipe {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneOffset.UTC);

    public MeteorForecastRecipe(final String identifier, final String name, final int productionTime,
                                final ItemMap input) {
        super(identifier, name, productionTime, input);
    }

    @Override
    public List<ItemStack> getInputRepresentation(final Inventory i, final FurnCraftChestFactory fccf) {
        if (i == null) {
            return input.getItemStackRepresentation();
        }
        return createLoredStacksForInfo(i);
    }

    @Override
    public List<ItemStack> getOutputRepresentation(final Inventory i, final FurnCraftChestFactory fccf) {
        return List.of();
    }

    @Override
    public List<String> getTextualOutputRepresentation(final Inventory i, final FurnCraftChestFactory fccf) {
        return List.of("Reports a one hour window for the next meteor");
    }

    @Override
    public Material getRecipeRepresentationMaterial() {
        return Material.CLOCK;
    }

    @Override
    public EffectFeasibility evaluateEffectFeasibility(final Inventory inputInv, final Inventory outputInv,
                                                       final FurnCraftChestFactory fccf) {
        if (getVeinSpawner() == null || getVeinSpawner().getForecastWindow() == null) {
            return new EffectFeasibility(false, "Heliodor meteors are not enabled");
        }
        return new EffectFeasibility(true, null);
    }

    @Override
    public boolean applyEffect(final Inventory inputInv, final Inventory outputInv, final FurnCraftChestFactory fccf) {
        final VeinSpawner spawner = getVeinSpawner();
        if (spawner == null || fccf.getActivator() == null || !input.isContainedIn(inputInv)) {
            return false;
        }
        final VeinSpawner.ForecastWindow forecast = spawner.getForecastWindow();
        if (forecast == null || !input.removeSafelyFrom(inputInv)) {
            return false;
        }
        sendForecastMessage(fccf.getActivator(), forecast);
        return false; // you don't want to run this multiple times
    }

    @Override
    public String getTypeIdentifier() {
        return "METEOR_FORECAST";
    }

    private void sendForecastMessage(final UUID activator, final VeinSpawner.ForecastWindow forecast) {
        final Player player = Bukkit.getPlayer(activator);
        if (player == null) {
            return;
        }
        final long now = System.currentTimeMillis();
        player.sendMessage(Component.text("The next meteor is expected between "
            + TIME_FORMAT.format(Instant.ofEpochMilli(forecast.startMillis())) + " and "
            + TIME_FORMAT.format(Instant.ofEpochMilli(forecast.endMillis())) + " GMT (in "
            + formatDuration(Duration.ofMillis(Math.max(0L, forecast.startMillis() - now))) + " to "
            + formatDuration(Duration.ofMillis(Math.max(0L, forecast.endMillis() - now))) + ").", NamedTextColor.GOLD));
    }

    private static String formatDuration(final Duration duration) {
        final long totalSeconds = duration.getSeconds();
        final long days = totalSeconds / 86_400L;
        final long hours = totalSeconds % 86_400L / 3_600L;
        final long minutes = totalSeconds % 3_600L / 60L;
        final long seconds = totalSeconds % 60L;
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }

    private VeinSpawner getVeinSpawner() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Heliodor")) {
            return null;
        }
        return JavaPlugin.getPlugin(HeliodorPlugin.class).getVeinSpawner();
    }
}
