package net.civmc.heliodor.heliodor;

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
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class PickaxeBreakListener implements Listener {

    private final VeinCache cache;

    private final int meteoricIronLow;
    private final int meteoricIronHigh;

    private final HoverEvent<Component> meteoricIronHint;

    private final List<Location> placedStone = new ArrayList<>();

    public PickaxeBreakListener(VeinCache cache, int meteoricIronLow, int meteoricIronHigh, int spawnRadius) {
        this.cache = cache;
        this.meteoricIronLow = meteoricIronLow;
        this.meteoricIronHigh = meteoricIronHigh;

        this.meteoricIronHint = HoverEvent.showText(Component.text("""
            A "Low" reading indicates a vein within %s blocks, ignoring Y levels
            A "High" reading indicates a vein within %s blocks
            Note that vein readings can be randomly offset from their
            true location by up to %s blocks."""
            .formatted(meteoricIronLow, meteoricIronHigh, meteoricIronHigh - spawnRadius)));
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!Tag.BASE_STONE_OVERWORLD.isTagged(block.getType()) || placedStone.contains(block.getLocation())) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!HeliodorPickaxe.isPickaxe(item)) {
            return;
        }


        VeinPing ping = cache.getVeinPing(block.getWorld().getName(), MeteoricIronVeinConfig.TYPE_NAME, meteoricIronLow, meteoricIronHigh, block.getX(), block.getY(), block.getZ());
        if (ping == null) {
            player.sendMessage(Component.text("No veins nearby", NamedTextColor.GRAY, TextDecoration.ITALIC).hoverEvent(meteoricIronHint));
        } else if (ping == VeinPing.LOW) {
            player.sendMessage(Component.text("Low indications of Meteoric Iron found", NamedTextColor.YELLOW, TextDecoration.ITALIC).hoverEvent(meteoricIronHint));
        } else {
            player.sendMessage(Component.text("High traces of Meteoric Iron found", NamedTextColor.GOLD, TextDecoration.ITALIC).hoverEvent(meteoricIronHint));
        }
    }

    @EventHandler
    public void on(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (Tag.BASE_STONE_OVERWORLD.isTagged(block.getType())) {
            placedStone.add(block.getLocation());
        }
    }

    @EventHandler
    public void on(BlockFormEvent event) {
        if (Tag.BASE_STONE_OVERWORLD.isTagged(event.getNewState().getType())) {
            placedStone.add(event.getBlock().getLocation());
        }
    }
}
