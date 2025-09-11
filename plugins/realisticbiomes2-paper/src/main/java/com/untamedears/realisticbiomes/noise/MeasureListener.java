package com.untamedears.realisticbiomes.noise;

import com.untamedears.realisticbiomes.RealisticBiomes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class MeasureListener implements Listener {

    private final BiomeConfiguration biomes;

    private final Material temperatureMeasure;
    private final Material humidityMeasure;
    private final Material fertilityMeasure;

    public MeasureListener(BiomeConfiguration biomes, Material temperatureMeasure, Material humidityMeasure, Material fertilityMeasure) {
        this.biomes = biomes;
        this.temperatureMeasure = temperatureMeasure;
        this.humidityMeasure = humidityMeasure;
        this.fertilityMeasure = fertilityMeasure;
    }

    public static MeasureListener fromConfiguration(BiomeConfiguration configuration, ConfigurationSection section) {
        return new MeasureListener(
            configuration,
            Material.matchMaterial(section.getString("temperature_measure")),
            Material.matchMaterial(section.getString("humidity_measure")),
            Material.matchMaterial(section.getString("fertility_measure"))
        );
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || block == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.isEmpty()) {
            return;
        }

        Material type = item.getType();

        Climate biomeClimate = biomes.getClimate(block.getBiome());
        if (type == this.temperatureMeasure) {
            double lt = biomes.getTemperature(block) + biomeClimate.temperature();
            int temperature = (int) Math.round(lt * 100);
            player.sendMessage(Component.empty().color(TextColor.color(232, 41, 41))
                .append(Component.text("Temperature: "))
                .append(Component.text(temperature + "°T", NamedTextColor.RED)));
            event.setUseItemInHand(Event.Result.DENY);
            RealisticBiomes.getInstance().getLogger().info(player.getName() + " found " + lt + " temperature at " + block.getX() + " " + block.getY() + " " + block.getZ());
        } else if (type == this.humidityMeasure) {
            double lh = biomes.getHumidity(block) + biomeClimate.humidity();
            // rescale humidity between 0% and 100% because it makes more sense that way
            lh += biomes.getHumidityScale();
            lh /= 1.0 + biomes.getHumidityScale() * 2.0;
            int humidity = (int) Math.round(lh * 100);
            humidity = Math.max(0, Math.min(100, humidity));
            player.sendMessage(Component.empty().color(TextColor.color(55, 159, 163))
                .append(Component.text("Humidity: "))
                .append(Component.text(humidity + "%", NamedTextColor.AQUA)));
            event.setUseItemInHand(Event.Result.DENY);
            RealisticBiomes.getInstance().getLogger().info(player.getName() + " found " + lh + " humidity at " + block.getX() + " " + block.getY() + " " + block.getZ());
        } else if (type == this.fertilityMeasure) {
            double lf = biomes.getFertility(block);
            int fertility = (int) Math.round(lf * 100);
            player.sendMessage(Component.empty().color(NamedTextColor.GOLD)
                .append(Component.text("Soil fertility: "))
                .append(Component.text(fertility + "%", NamedTextColor.YELLOW)));
            event.setUseItemInHand(Event.Result.DENY);
            RealisticBiomes.getInstance().getLogger().info(player.getName() + " found " + lf + " humidity at " + block.getX() + " " + block.getY() + " " + block.getZ());
        }
    }
}
