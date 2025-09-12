package com.untamedears.realisticbiomes.noise;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import com.untamedears.realisticbiomes.breaker.BreakManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

public class MeasureCommand extends BaseCommand {

    private final BiomeConfiguration biomes;
    private final BreakManager breakManager;

    public MeasureCommand(BiomeConfiguration biomes, BreakManager breakManager) {
        this.biomes = biomes;
        this.breakManager = breakManager;
    }

    @CommandAlias("rbmeasure")
    @CommandPermission("realisticbiomes.measure")
    public void measure(Player player) {
        Block block = player.getLocation().getBlock();

        Climate climate = biomes.getClimate(block.getBiome());
        if (climate == null) {
            player.sendMessage(Component.text("Invalid biome"));
            return;
        }


        player.sendMessage(Component.text("Biome temperature: " + (int) (100 * climate.temperature())));
        player.sendMessage(Component.text("Biome humidity: " + (int) (100 * climate.humidity())));

        player.sendMessage(Component.text("Soil fertility: " + NumberFormat.getPercentInstance().format(biomes.getFertility(block))));
        player.sendMessage(Component.text("Ambient temperature: " + (int) (100 * (biomes.getTemperature(block) + climate.temperature()))));
        player.sendMessage(Component.text("Ambient humidity: " + (int) (100 * (biomes.getHumidity(block) + climate.humidity()))));

        NumberFormat f = new DecimalFormat("#.##%");
        for (Map.Entry<Material, Climate> entry : breakManager.getClimates().entrySet()) {
            double yield = this.biomes.getYield(block, entry.getValue(), entry.getKey(), breakManager.getMaxYield().getOrDefault(entry.getKey(), 0));
            if (yield > 0.001) {
                String format = f.format(yield);

                int temp = (int) (entry.getValue().temperature() * 100);
                int hum = (int) (entry.getValue().humidity() * 100);

                player.sendMessage(Component.text(entry.getKey() + ": t=" + temp + " h=" + hum + " " + format));
            }
        }
    }
}
