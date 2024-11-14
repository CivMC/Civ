package net.civmc.heliodor.heliodor;

import net.civmc.heliodor.ChunkPos;
import net.civmc.heliodor.HeliodorPlugin;
import net.civmc.heliodor.vein.VeinCache;
import net.civmc.heliodor.vein.data.MeteoricIronVeinConfig;
import net.civmc.heliodor.vein.data.VeinPing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PickaxeBreakListener implements Listener {

    private final VeinCache cache;

    private final int meteoricIronLow;
    private final int meteoricIronHigh;

    private final HoverEvent<Component> meteoricIronHint;

    private final Map<ChunkPos, List<Location>> placedStone = new HashMap<>();

    public PickaxeBreakListener(VeinCache cache, int meteoricIronLow, int meteoricIronHigh, int spawnRadius) {
        this.cache = cache;
        this.meteoricIronLow = meteoricIronLow;
        this.meteoricIronHigh = meteoricIronHigh;

        this.meteoricIronHint = HoverEvent.showText(Component.text("""
            A "Low" reading indicates a vein within %s blocks
            A "High" reading indicates a vein within %s blocks
            Vein readings can be inaccurate up to %s blocks.
            Vein readings don't show if over 50%% of ores are mined."""
            .formatted(meteoricIronLow, meteoricIronHigh, meteoricIronHigh - spawnRadius)));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void on(BlockBreakEvent event) {
        Block block = event.getBlock();
        ChunkPos chunkPos = new ChunkPos(block.getChunk().getX(), block.getChunk().getZ());
        if (!Tag.BASE_STONE_OVERWORLD.isTagged(block.getType())
            || placedStone.getOrDefault(chunkPos, Collections.emptyList()).contains(block.getLocation())) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!HeliodorPickaxe.isPickaxe(item)) {
            return;
        }

        Logger logger = JavaPlugin.getPlugin(HeliodorPlugin.class).getLogger();

        VeinPing ping = cache.getVeinPing(block.getWorld().getName(), MeteoricIronVeinConfig.TYPE_NAME, meteoricIronLow, meteoricIronHigh, block.getX(), block.getY(), block.getZ());
        if (ping == null) {
            logger.info("Player " + player.getName() + " found no vein while probing with a heliodor pickaxe at " + block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ());
            player.sendMessage(Component.text("No veins nearby", NamedTextColor.GRAY, TextDecoration.ITALIC).hoverEvent(meteoricIronHint));
        } else if (ping == VeinPing.LOW) {
            logger.info("Player " + player.getName() + " found low vein while probing with a heliodor pickaxe at " + block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ());
            player.sendMessage(Component.text("Low indications of Meteoric Iron found", NamedTextColor.YELLOW, TextDecoration.ITALIC).hoverEvent(meteoricIronHint));
        } else {
            logger.info("Player " + player.getName() + " found high vein while probing with a heliodor pickaxe at " + block.getWorld().getName() + " " + block.getX() + " " + block.getY() + " " + block.getZ());
            player.sendMessage(Component.text("High traces of Meteoric Iron found", NamedTextColor.GOLD, TextDecoration.ITALIC).hoverEvent(meteoricIronHint));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void on(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (Tag.BASE_STONE_OVERWORLD.isTagged(block.getType())) {
            ChunkPos chunkPos = new ChunkPos(block.getChunk().getX(), block.getChunk().getZ());
            placedStone.computeIfAbsent(chunkPos, k -> new ArrayList<>()).add(block.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void on(BlockFormEvent event) {
        if (Tag.BASE_STONE_OVERWORLD.isTagged(event.getNewState().getType())) {
            Block block = event.getBlock();
            ChunkPos chunkPos = new ChunkPos(block.getChunk().getX(), block.getChunk().getZ());
            placedStone.computeIfAbsent(chunkPos, k -> new ArrayList<>()).add(block.getLocation());
        }
    }
}
