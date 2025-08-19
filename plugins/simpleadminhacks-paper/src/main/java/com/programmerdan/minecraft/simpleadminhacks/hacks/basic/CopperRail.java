package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.MaterialTags;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class CopperRail extends BasicHack {

    @AutoLoad
    private boolean deoxidise;

    @AutoLoad
    private double damage;

    private boolean formingBlock = false;

    public CopperRail(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler
    public void on(VehicleMoveEvent event) {
        if (this.damage <= 0 || !(event.getVehicle() instanceof Minecart minecart)) {
            return;
        }

        boolean hasPlayer = false;
        for (Entity entity : minecart.getPassengers()) {
            if (entity instanceof Player) {
                hasPlayer = true;
                break;
            }
        }

        if (!hasPlayer) {
            return;
        }

        Location to = event.getTo();
        Location from = event.getFrom();
        if (to.getBlockX() == from.getBlockX() && to.getBlockY() == from.getBlockY() && to.getBlockZ() == from.getBlockZ()) {
            return;
        }

        int signX = from.getBlockX() > to.getBlockX() ? 1 : -1;
        int signZ = from.getBlockZ() > to.getBlockZ() ? 1 : -1;
        boolean firstBlock = true;

        List<Block> copperBlocks = new ArrayList<>(4);
        for (int x = to.getBlockX(); x != to.getBlockX() + (from.getBlockX() - to.getBlockX()) + signX; x += signX) {
            for (int z = to.getBlockZ(); z != to.getBlockZ() + (from.getBlockZ() - to.getBlockZ()) + signZ; z += signZ) {
                if (firstBlock) {
                    firstBlock = false;
                    continue;
                }
                Location location = new Location(minecart.getWorld(), x, from.getY(), z);
                Block topCopperBlock = location.getBlock().getRelative(BlockFace.DOWN);
                Optional<net.minecraft.world.level.block.Block> next = WeatheringCopper.getNext(((CraftBlock) topCopperBlock).getNMS().getBlock());
                if (next.isPresent()) {
                    copperBlocks.add(topCopperBlock);
                }
                Block belowCopperBlock = topCopperBlock.getRelative(BlockFace.DOWN);
                next = WeatheringCopper.getNext(((CraftBlock) belowCopperBlock).getNMS().getBlock());
                if (next.isPresent()) {
                    copperBlocks.add(belowCopperBlock);
                }
            }
        }

        for (Block copperBlock : copperBlocks) {
            CraftBlock craftBlock = (CraftBlock) copperBlock;
            BlockState state = craftBlock.getNMS();
            ServerLevel level = ((CraftWorld) copperBlock.getWorld()).getHandle();
            // We damage the copper directly instead of using random ticking, as random ticking is easy to cheese
            // by placing waxed copper next to the rail, entirely preventing the rest of the rail from oxidising.
            WeatheringCopper copper = (WeatheringCopper) state.getBlock();
            float chanceModifier = copper.getChanceModifier();
            if (this.damage * chanceModifier > ThreadLocalRandom.current().nextFloat()) {
                copper.getNext(state).ifPresent((iblockdata2) -> {
                    try {
                        formingBlock = true;
                        CraftEventFactory.handleBlockFormEvent(level, craftBlock.getPosition(), iblockdata2, 3);
                    } finally {
                        formingBlock = false;
                    }
                });
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void on(PlayerInteractEvent event) {
        if (!this.deoxidise) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || !MaterialTags.AXES.isTagged(item)) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null || !MaterialTags.RAILS.isTagged(block)) {
            return;
        }

        Block copperBlock = block.getRelative(BlockFace.DOWN);
        Optional<BlockState> previous = WeatheringCopper.getPrevious(((CraftBlock) copperBlock).getNMS());

        boolean damaged = false;
        CraftPlayer player = (CraftPlayer) event.getPlayer();

        while (previous.isPresent() && event.getItem().getType() != Material.AIR) {
            copperBlock.setType(previous.get().getBukkitMaterial());
            damaged = true;

            item.damage(1, player);
            previous = WeatheringCopper.getPrevious(((CraftBlock) copperBlock).getNMS());
        }

        copperBlock = copperBlock.getRelative(BlockFace.DOWN);
        previous = WeatheringCopper.getPrevious(((CraftBlock) copperBlock).getNMS());

        while (previous.isPresent() && event.getItem().getType() != Material.AIR) {
            copperBlock.setType(previous.get().getBukkitMaterial());
            damaged = true;

            item.damage(1, player);
            previous = WeatheringCopper.getPrevious(((CraftBlock) copperBlock).getNMS());
        }

        if (!damaged) {
            return;
        }

        block.getWorld().playSound(block.getLocation(), Sound.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1, 1);
        block.getWorld().playEffect(block.getLocation(), Effect.OXIDISED_COPPER_SCRAPE, 0);

        event.setCancelled(true);
    }

    // It's not really fair for copper blocks that are below rails to naturally oxidise,
    // as it is easy to cheese by placing a waxed copper block every 9 blocks
    @EventHandler
    public void on(BlockFormEvent event) {
        if (formingBlock) {
            return;
        }

        Block block = event.getBlock();

        Optional<net.minecraft.world.level.block.Block> next = WeatheringCopper.getNext(((CraftBlock) block).getNMS().getBlock());
        if (next.isEmpty()) {
            return;
        }

        Block railAbove = block.getRelative(BlockFace.UP);
        if (!MaterialTags.RAILS.isTagged(railAbove)) {
            railAbove = railAbove.getRelative(BlockFace.UP);
        }

        if (!MaterialTags.RAILS.isTagged(railAbove)) {
            return;
        }

        event.setCancelled(true);
    }
}
