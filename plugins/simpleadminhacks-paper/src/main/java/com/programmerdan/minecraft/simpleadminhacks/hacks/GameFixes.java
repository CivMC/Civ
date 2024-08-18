package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.configs.GameFixesConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.PacketManager;
import java.util.logging.Level;

import net.minecraft.world.item.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.InventoryHolder;

public class GameFixes extends SimpleHack<GameFixesConfig> implements Listener {

    public static final String NAME = "GameFixes";

    private final PacketManager protocol = new PacketManager();

    public GameFixes(SimpleAdminHacks plugin, GameFixesConfig config) {
        super(plugin, config);
    }

    @Override
    public void onDisable() {
        this.protocol.removeAllAdapters();
        HandlerList.unregisterAll(this);
        super.onDisable();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        super.onEnable();
    }

    @Override
    public String status() {
        StringBuilder genStatus = new StringBuilder();
        genStatus.append("GameFixes is ");
        if (config != null && config.isEnabled()) {
            genStatus.append(ChatColor.GREEN).append("active\n").append(ChatColor.RESET);
            genStatus.append("   Block elytra break bug is ");
            if (config.isBlockElytraBreakBug()) {
                genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
                genStatus.append("   Will deal " + config.getDamageOnElytraBreakBug() + " damage to players\n");
            } else {
                genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
            }
            genStatus.append("   Block storage entities from teleporting to prevents exploits ");
            if (!config.canStorageTeleport()) {
                genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
            } else {
                genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
            }
            genStatus.append("  Bed Bombing in Nether / Hell Biomes fix ");
            if (config.stopBedBombing()) {
                genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
            } else {
                genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
            }
            genStatus.append("  Tree wraparound fix ");
            if (config.stopTreeWraparound()) {
                genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
            } else {
                genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
            }
            genStatus.append("  Maintain flat bedrock ");
            if (config.maintainFlatBedrock()) {
                genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
            } else {
                genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
            }
            genStatus.append("  Maintain flat bedrock ");
            if (config.maintainFlatBedrock()) {
                genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
            } else {
                genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
            }
            genStatus.append("  Prevent long signs ");
            if (config.isPreventLongSigns()) {
                genStatus.append(ChatColor.GREEN).append("enabled\n").append(ChatColor.RESET);
            } else {
                genStatus.append(ChatColor.RED).append("disabled\n").append(ChatColor.RESET);
            }
        } else {
            genStatus.append(ChatColor.RED).append("inactive").append(ChatColor.RESET);
        }
        return genStatus.toString();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.isEnabled() || !config.isBlockElytraBreakBug()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (!player.getLocation().equals(block.getLocation())
            && player.getEyeLocation().getBlock().getType() != Material.AIR) {
            event.setCancelled(true);
            player.damage(config.getDamageOnElytraBreakBug());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityTeleport(EntityTeleportEvent event) {
        if (!config.isEnabled() || config.canStorageTeleport()) {
            return;
        }
        if (event.getEntity() instanceof InventoryHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        if (!config.isEnabled() || config.canStorageTeleport()) {
            return;
        }
        if (event.getEntity() instanceof InventoryHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerEnterBed(BlockPlaceEvent event) {
        if (!config.isEnabled() || !config.stopBedBombing()) return;

        Block b = event.getBlock();
        switch (b.getType()) {
            case BLACK_BED:
            case BLUE_BED:
            case BROWN_BED:
            case CYAN_BED:
            case GRAY_BED:
            case GREEN_BED:
            case LIME_BED:
            case MAGENTA_BED:
            case LIGHT_GRAY_BED:
            case PURPLE_BED:
            case PINK_BED:
            case YELLOW_BED:
            case WHITE_BED:
            case RED_BED:
            case ORANGE_BED:
            case LIGHT_BLUE_BED:
                break;
            default:
                return;
        }
        Environment env = b.getLocation().getWorld().getEnvironment();
        Biome biome = b.getLocation().getBlock().getBiome();
        if (Environment.NETHER.equals(env) || Environment.THE_END.equals(env) || Biome.NETHER_WASTES.equals(biome)
            || Biome.END_BARRENS.equals(biome) || Biome.END_HIGHLANDS.equals(biome)
            || Biome.END_MIDLANDS.equals(biome) || Biome.SMALL_END_ISLANDS.equals(biome)
            || Biome.THE_END.equals(biome)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAnchorInteraction(PlayerInteractEvent event) {
        if (!config.isEnabled() || !config.stopAnchorBombing()) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!event.getClickedBlock().getType().equals(Material.RESPAWN_ANCHOR)) {
            return;
        }
        RespawnAnchor anchor = (RespawnAnchor) event.getClickedBlock().getBlockData();
        if (!event.getMaterial().equals(Material.GLOWSTONE) || anchor.getCharges() == anchor.getMaximumCharges()) {
            boolean flag = event.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR || event.getPlayer().getInventory().getItemInOffHand().getType() != Material.AIR;
            boolean flag1 = event.getPlayer().isSneaking() && flag;
            if (!flag1) {
                event.setUseInteractedBlock(Event.Result.DENY);
                event.getPlayer().sendMessage(ChatColor.RED + "Respawn anchor bombing is disabled");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (config.isEnabled() && config.stopTreeWraparound()) {
            int maxY = 0;
            int minY = 257;
            for (BlockState bs : event.getBlocks()) {
                final int y = bs.getLocation().getBlockY();
                maxY = Math.max(maxY, y);
                minY = Math.min(minY, y);
            }
            if (maxY - minY > 240) {
                event.setCancelled(true);
                final Location loc = event.getLocation();
                plugin().log(Level.INFO, String.format("Prevented structure wraparound at %s: %d, %d, %d",
                    loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
            }
        }
    }

    @EventHandler
    public void onTouchBedrock(PlayerInteractEvent event) {
        if (config.isEnabled() && config.maintainFlatBedrock() && event.getClickedBlock() != null
            && event.getClickedBlock().getType() == Material.BEDROCK && event.getClickedBlock().getY() > 0) {
            Bukkit.getScheduler().runTask(plugin(), () -> {
                event.getClickedBlock().setType(Material.STONE);
            });
        }
    }

    //fixes a small side effect of the above
    BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

    @EventHandler
    public void preventWrongIce(BlockFormEvent event) {
        Block block = event.getBlock();
        if (event.getNewState().getType() == Material.ICE) {
            for (BlockFace face : faces) {
                if (block.getRelative(face).getType().isSolid()) return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (config.isEnabled() && config.isPreventLongSigns()) {
            String[] signdata = event.getLines();
            for (int i = 0; i < signdata.length; i++) {
                if (signdata[i] != null && signdata[i].length() > config.getSignLengthLimit()) {
                    Player player = event.getPlayer();
                    Location loc = event.getBlock().getLocation();
                    plugin().log(Level.WARNING, String.format("Player '%s' [%s] attempted to place sign at ([%s] %d, %d, %d) with line %d having length %d > %d. Preventing.",
                        player.getDisplayName(), player.getUniqueId(), loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                        i, signdata[i], config.getSignLengthLimit()));
                    if (config.isCancelLongSignEvent()) {
                        event.setCancelled(true);
                        return;
                    }
                    if (config.isPreventLongSignsAbsolute()) {
                        event.setLine(i, "");
                    } else {
                        event.setLine(i, signdata[i].substring(0, config.getSignLengthLimit()));
                    }
                }
            }
        }
    }

    public static GameFixesConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
        return new GameFixesConfig(plugin, config);
    }

}
