package com.untamedears.jukealert.listener;

import com.untamedears.jukealert.SnitchManager;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.abstr.SnitchAction;
import com.untamedears.jukealert.model.actions.impl.BlockBreakAction;
import com.untamedears.jukealert.model.actions.impl.BlockPlaceAction;
import com.untamedears.jukealert.model.actions.impl.DestroyVehicleAction;
import com.untamedears.jukealert.model.actions.impl.DismountEntityAction;
import com.untamedears.jukealert.model.actions.impl.EditSignAction;
import com.untamedears.jukealert.model.actions.impl.EmptyBucketAction;
import com.untamedears.jukealert.model.actions.impl.EnterFieldAction;
import com.untamedears.jukealert.model.actions.impl.EnterVehicleAction;
import com.untamedears.jukealert.model.actions.impl.ExitVehicleAction;
import com.untamedears.jukealert.model.actions.impl.FillBucketAction;
import com.untamedears.jukealert.model.actions.impl.IgniteBlockAction;
import com.untamedears.jukealert.model.actions.impl.KillLivingEntityAction;
import com.untamedears.jukealert.model.actions.impl.KillPlayerAction;
import com.untamedears.jukealert.model.actions.impl.LeaveFieldAction;
import com.untamedears.jukealert.model.actions.impl.LoginAction;
import com.untamedears.jukealert.model.actions.impl.LogoutAction;
import com.untamedears.jukealert.model.actions.impl.MountEntityAction;
import com.untamedears.jukealert.model.actions.impl.OpenContainerAction;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Boat;
import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.namelayer.NameAPI;

public class LoggableActionListener implements Listener {

    private final SnitchManager snitchManager;
    private final Map<UUID, Set<Snitch>> insideFields;

    public LoggableActionListener(SnitchManager snitchManager) {
        this.snitchManager = snitchManager;
        this.insideFields = new TreeMap<>();
    }

    public void setupScheduler(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                for (World world : Bukkit.getWorlds()) {
                    for (Player player : world.getPlayers()) {
                        Entity entity = player.getVehicle();
                        if (!(entity instanceof HappyGhast)) {
                            continue;
                        }
                        handleSnitchEntry(player, entity.getLocation());
                    }
                }
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Ticking ghast positions", ex);
            }
        }, 0, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void enterSnitchProximity(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
            && from.getBlockZ() == to.getBlockZ() && from.getWorld().equals(to.getWorld())) {
            // Player didn't move by at least one block
            return;
        }
        handleSnitchEntry(event.getPlayer(), to);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null || event.getPlayer() == null) {
            return;
        }
        handleSnitchEntry(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleMovement(VehicleMoveEvent event) {
        for (Entity e : event.getVehicle().getPassengers()) {
            if (e instanceof Player) {
                enterSnitchProximity(new PlayerMoveEvent((Player) e, event.getFrom(), event.getTo()));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        handlePlayerAction(event.getPlayer(), s -> new BlockPlaceAction(System.currentTimeMillis(), s,
            event.getPlayer().getUniqueId(), event.getBlock().getLocation(), event.getBlock().getType()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        handlePlayerAction(event.getPlayer(), s -> new BlockBreakAction(System.currentTimeMillis(), s,
            event.getPlayer().getUniqueId(), event.getBlock().getLocation(), event.getBlock().getType()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityKill(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.getKiller() == null) {
            return;
        }
        Player killer = victim.getKiller();
        if (victim.getType() == EntityType.PLAYER) {
            handlePlayerAction(killer, s -> new KillPlayerAction(System.currentTimeMillis(), s, killer.getUniqueId(),
                victim.getLocation(), victim.getUniqueId()));
            return;
        }
        String victimName = getEntityName(victim);
        handlePlayerAction(killer, s -> new KillLivingEntityAction(System.currentTimeMillis(), s, killer.getUniqueId(),
            victim.getLocation(), victimName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDestroyVehicle(VehicleDestroyEvent event) {
        if (event.getAttacker() == null || event.getAttacker().getType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getAttacker();

        handlePlayerAction(player, s -> new DestroyVehicleAction(System.currentTimeMillis(), s,
            player.getUniqueId(), event.getVehicle().getLocation(), getEntityName(event.getVehicle())));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnterVehicle(VehicleEnterEvent event) {
        if (event.getEntered().getType() != EntityType.PLAYER || event.getVehicle().getSpawnCategory() != SpawnCategory.MISC) {
            return;
        }

        Player player = (Player) event.getEntered();

        handlePlayerAction(player, s -> new EnterVehicleAction(System.currentTimeMillis(), s,
            player.getUniqueId(), event.getVehicle().getLocation(), getVehicleName(event.getVehicle())));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExitVehicle(VehicleExitEvent event) {
        if (event.getExited().getType() != EntityType.PLAYER || event.getVehicle().getSpawnCategory() != SpawnCategory.MISC) {
            return;
        }

        Player player = (Player) event.getExited();

        handlePlayerAction(player, s -> new ExitVehicleAction(System.currentTimeMillis(), s,
            player.getUniqueId(), event.getVehicle().getLocation(), getVehicleName(event.getVehicle())));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEmptyBucket(PlayerBucketEmptyEvent event) {
        handlePlayerAction(event.getPlayer(), s -> new EmptyBucketAction(System.currentTimeMillis(), s,
            event.getPlayer().getUniqueId(), event.getBlock().getLocation(), event.getBucket()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFillBucket(PlayerBucketFillEvent event) {
        handlePlayerAction(event.getPlayer(), s -> new FillBucketAction(System.currentTimeMillis(), s,
            event.getPlayer().getUniqueId(), event.getBlock().getLocation(), event.getBlockClicked().getType()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMountEntity(EntityMountEvent event) {
        if (event.getEntityType() != EntityType.PLAYER || event.getMount().getSpawnCategory() != SpawnCategory.ANIMAL) {
            return;
        }
        Player player = (Player) event.getEntity();
        String mountName = getEntityName(event.getMount());
        handlePlayerAction(player, s -> new MountEntityAction(System.currentTimeMillis(), s, player.getUniqueId(),
            event.getMount().getLocation(), mountName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDismountEntity(EntityDismountEvent event) {
        if (event.getEntityType() != EntityType.PLAYER || event.getDismounted().getSpawnCategory() != SpawnCategory.ANIMAL) {
            return;
        }
        Player player = (Player) event.getEntity();
        String mountName = getEntityName(event.getDismounted());
        handlePlayerAction(player, s -> new DismountEntityAction(System.currentTimeMillis(), s, player.getUniqueId(),
            event.getDismounted().getLocation(), mountName));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onOpenInventory(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        Location location;
        Material material;

        if (holder instanceof BlockInventoryHolder blockHolder) {
            location = blockHolder.getBlock().getLocation();
            material = blockHolder.getBlock().getType();
        } else if (holder instanceof DoubleChest doubleChest) {
            location = doubleChest.getLocation();
            material = Material.CHEST;
        } else if (holder instanceof StorageMinecart minecart) {
            location = minecart.getLocation();
            material = Material.CHEST_MINECART;
        } else if (holder instanceof HopperMinecart minecart) {
            location = minecart.getLocation();
            material = Material.HOPPER_MINECART;
        } else if (holder instanceof ChestBoat boat) {
            location = boat.getLocation();
            material = boat.getBoatMaterial();
        } else {
            return;
        }

        Player player = (Player) event.getPlayer();
        handlePlayerAction(player, s -> new OpenContainerAction(System.currentTimeMillis(), s, player.getUniqueId(),
            location, material));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Set<Snitch> covering = new HashSet<>(snitchManager.getSnitchesCovering(event.getPlayer().getLocation()));
        handlePlayerAction(player, s -> new LoginAction(System.currentTimeMillis(), s, player.getUniqueId()));
        insideFields.put(event.getPlayer().getUniqueId(), covering);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerQuitEvent(PlayerQuitEvent event) {
        handleSnitchLogout(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerKickEvent(PlayerKickEvent event) {
        // TODO Old JA had this listener, is it really needed?
        handleSnitchLogout(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerIgniteBlock(BlockIgniteEvent event) {
        if (event.getCause() != IgniteCause.FLINT_AND_STEEL || event.getPlayer() == null) {
            return;
        }
        Player player = event.getPlayer();
        handlePlayerAction(player, s -> new IgniteBlockAction(System.currentTimeMillis(), s, player.getUniqueId(),
            event.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void playerEditSign(SignChangeEvent event) {
        handlePlayerAction(event.getPlayer(), s -> new EditSignAction(System.currentTimeMillis(), s,
            event.getPlayer().getUniqueId(), event.getBlock().getLocation(), event.getBlock().getType()));
    }

    private void handleSnitchLogout(Player player) {
        handlePlayerAction(player, s -> new LogoutAction(System.currentTimeMillis(), s, player.getUniqueId()));
    }

    private void handlePlayerAction(Player player, Function<Snitch, SnitchAction> actionCreator) {
        if (isPlayerSnitchImmune(player)) {
            return;
        }
        Collection<Snitch> snitches = snitchManager.getSnitchesCovering(player.getLocation());
        for (Snitch snitch : snitches) {
            if (!snitch.hasPermission(player, JukeAlertPermissionHandler.getSnitchImmune())) {
                snitch.processAction(actionCreator.apply(snitch));
            }
        }
    }

    private void handleSnitchEntry(Player player, Location location) {
        if (isPlayerSnitchImmune(player)) {
            return;
        }
        if (!player.getMetadata("NPC").isEmpty() || NameAPI.getCurrentName(player.getUniqueId()) == null) {
            //CombatTagPlus
            return;
        }
        Collection<Snitch> insideNow = snitchManager.getSnitchesCovering(location);
        Set<Snitch> previouslyIn = insideFields.computeIfAbsent(player.getUniqueId(), s -> new HashSet<>());
        insideNow.stream().filter(s -> !previouslyIn.contains(s)).forEach(s -> {
            s.processAction(new EnterFieldAction(System.currentTimeMillis(), s, player.getUniqueId()));
            previouslyIn.add(s);
        });
        List<Snitch> toRemove = new LinkedList<>();
        previouslyIn.stream().filter(s -> !insideNow.contains(s)).forEach(s -> {
            s.processAction(new LeaveFieldAction(System.currentTimeMillis(), s, player.getUniqueId()));
            toRemove.add(s);
        });
        // need to do this afterwards to avoid ConcurrentModificationExceptions
        previouslyIn.removeAll(toRemove);
    }

    private boolean isPlayerSnitchImmune(Player player) {
        return player.hasPermission("jukealert.vanish");
    }

    private String getEntityName(Entity entity) {
        if (entity.getCustomName() != null) {
            return entity.getCustomName();
        } else {
            return entity.getType().toString();
        }
    }

    private String getVehicleName(Vehicle vehicle) {
        if (vehicle instanceof Boat boat) {
            return boat.getBoatMaterial().name();
        } else {
            return vehicle.getType().toString();
        }
    }

}
