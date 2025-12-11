package vg.civcraft.mc.citadel.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;


import io.papermc.paper.event.entity.ItemTransportingEntityValidateTargetEvent;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.events.ReinforcementBypassEvent;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.database.GroupManagerDao;

public class EntityListener implements Listener {

    protected GroupManager gm = NameLayerAPI.getGroupManager();

    // prevent zombies from breaking reinforced doors
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void breakDoor(EntityBreakDoorEvent ebde) {
        Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(ebde.getBlock());
        if (rein != null) {
            ReinforcementLogic.damageReinforcement(rein, ReinforcementLogic.getDamageApplied(rein, null, null), ebde.getEntity());
            if (!rein.isBroken()) {
                ebde.setCancelled(true);
            }
        }
    }

    // Prevent Piglins and other "smart" mobs from opening reinforced doors, trapdoors, and fence gates
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onOpenDoor(EntityInteractEvent eie) {
        Material material = eie.getBlock().getType();

        if (!(Tag.DOORS.isTagged(material) || Tag.TRAPDOORS.isTagged(material) || Tag.FENCE_GATES.isTagged(material))) {
            return;
        }

        Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(eie.getBlock());
        if (rein != null) {
            eie.setCancelled(true);
            if (eie.getEntity() instanceof Mob mob) {
                mob.getPathfinder().stopPathfinding(); // Prevent trying to interact every tick
            }
        }
    }

    // For some ungodly reason, when you break a block below a block with gravity, it spawns a FallingBlock entity
    // that then attempts to change the block. To prevent this change from ticking damage and creating a ghost block
    // the entity needs to have its gravity disabled so it immediately lands.
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFallingBlockSpawn(EntitySpawnEvent event) {
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }
        Block block = event.getLocation().getBlock();
        Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(block);
        if (rein == null) {
            return;
        }
        Entity entity = event.getEntity();
        entity.setGravity(false);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void changeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(block);
        // Do not allow a falling block entity to damage reinforcements.
        if (rein == null || event.getEntityType() == EntityType.FALLING_BLOCK) {
            return;
        }
        if (event.getBlock().getType() == Material.BIG_DRIPLEAF) {
            return;
        }
        if (event.getBlock().getType() == Material.CAVE_VINES || event.getBlock().getType() == Material.CAVE_VINES_PLANT) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }

        boolean hasAccess = rein.hasPermission(player, CitadelPermissionHandler.getBypass());
        BooleanSetting setting = (BooleanSetting) PlayerSettingAPI.getSetting("citadelBypass");
        boolean hasBypass = setting.getValue(player);
        if (hasAccess && !hasBypass) {
            CitadelUtility.sendAndLog(player, ChatColor.GREEN,
                "You could bypass this reinforcement if you turn bypass mode on with '/ctb'",
                block.getLocation());
            event.setCancelled(true);
        } else if (!hasAccess && !hasBypass) {
            player.sendMessage(Component.text("You do not have permission to bypass this block!").color(NamedTextColor.RED));
            event.setCancelled(true);
        }
    }

    // apply explosion damage to reinforcements
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void explode(EntityExplodeEvent eee) {
        Iterator<Block> iterator = eee.blockList().iterator();
        // we can edit the result by removing blocks from the list
        while (iterator.hasNext()) {
            Block block = iterator.next();
            Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(block);
            if (rein == null) {
                continue;
            }
            ReinforcementLogic.damageReinforcement(rein, ReinforcementLogic.getDamageApplied(rein, null, null), eee.getEntity());
            if (!rein.isBroken()) {
                iterator.remove();
            }
        }
    }

    private List<Block> getGolemBlocks(EntityType type, Block base) {
        List<Block> blocks = new ArrayList<>();
        if (type == EntityType.COPPER_GOLEM) {
            blocks.add(base);
            base = base.getRelative(BlockFace.UP);
            blocks.add(base);
        } else {
            blocks.add(base);
            base = base.getRelative(BlockFace.UP);
            blocks.add(base);
            if (type == EntityType.IRON_GOLEM) {
                for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
                    BlockFace.WEST}) {
                    Block arm = base.getRelative(face);
                    if (arm.getType() == Material.IRON_BLOCK)
                        blocks.add(arm);
                }
            }
            base = base.getRelative(BlockFace.UP);
            blocks.add(base);
        }
        return blocks;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        final UUID uuid = p.getUniqueId();


        new BukkitRunnable() {
            @Override
            public void run() {
                GroupManagerDao db = NameLayerPlugin.getGroupManagerDao();
                for (String groupName : db.getGroupNames(uuid)) {
                    if (NameLayerAPI.getGroupManager().hasAccess(groupName, uuid,
                        CitadelPermissionHandler.getBypass())) {
                        GroupManager.getGroup(groupName).updateActivityTimeStamp();
                    }
                }
            }
        }.runTaskAsynchronously(Citadel.getInstance());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void powderedSnowPickup(PlayerBucketFillEvent event) {
        if (event.getBlockClicked().getType() != Material.POWDER_SNOW) {
            return;
        }

        Block clickedBlock = event.getBlockClicked();
        Reinforcement reinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(clickedBlock);
        if (reinforcement == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!reinforcement.hasPermission(player, CitadelPermissionHandler.getBypass())) {
            player.sendMessage(Component.text("You do not have permission to bypass this block!").color(NamedTextColor.RED));
            event.setCancelled(true);
            return;
        }
        ReinforcementBypassEvent bypassEvent = new ReinforcementBypassEvent(player, reinforcement);
        Bukkit.getPluginManager().callEvent(bypassEvent);
        if (event.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        reinforcement.setHealth(-1);
    }

    // prevent creating golems from reinforced blocks
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void spawn(CreatureSpawnEvent cse) {
        EntityType type = cse.getEntityType();
        if (type != EntityType.IRON_GOLEM && type != EntityType.SNOW_GOLEM && type != EntityType.WITHER
            && type != EntityType.SILVERFISH && type != EntityType.COPPER_GOLEM) {
            return;
        }
        for (Block block : getGolemBlocks(type, cse.getLocation().getBlock())) {
            Reinforcement reinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
            if (reinforcement != null) {
                cse.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void blockCopperGolem(ItemTransportingEntityValidateTargetEvent itevte) {
        Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(itevte.getBlock());
        if (rein != null && !rein.isInsecure()) {
            itevte.setAllowed(false);
        }
    }

    // ------------------------------------------------------------
    // Hanging Entities
    // ------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hangingPlaceEvent(HangingPlaceEvent event) {
        if (!Citadel.getInstance().getConfigManager().doHangersInheritReinforcements()) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(event.getBlock());
        if (reinforcement == null) {
            return;
        }
        if (reinforcement.isInsecure()) {
            return;
        }
        if (reinforcement.hasPermission(player, CitadelPermissionHandler.getHangingPlaceBreak())) {
            return;
        }
        player.sendMessage(ChatColor.RED + "You cannot place those on blocks you don't have permissions for.");
        event.setCancelled(true);
        Bukkit.getScheduler().runTaskLater(Citadel.getInstance(), player::updateInventory, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hangingEntityBreakEvent(HangingBreakByEntityEvent event) {
        if (!Citadel.getInstance().getConfigManager().doHangersInheritReinforcements()) {
            return;
        }
        switch (event.getCause()) {
            // Allow it to break if:
            // 1) The host block broke
            // 2) A block was placed over it
            // 3) A plugin broke it
            case OBSTRUCTION:
            case PHYSICS:
            case DEFAULT:
            case EXPLOSION:
                return;
            case ENTITY: {
                if (!(event.getRemover() instanceof final Player player)) {
                    break;
                }
                Hanging entity = event.getEntity();
                Block host = entity.getLocation().getBlock().getRelative(entity.getAttachedFace());
                Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(host);
                if (reinforcement == null) {
                    return;
                }
                if (reinforcement.isInsecure()) {
                    return;
                }
                if (reinforcement.hasPermission(player, CitadelPermissionHandler.getHangingPlaceBreak())) {
                    return;
                }
                player.sendMessage(ChatColor.RED + "The host block is protecting this.");
                //break;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void HangingBreakEvent(HangingBreakEvent event) {
        if (!Citadel.getInstance().getConfigManager().doHangersInheritReinforcements()) {
            return;
        }
        if (event.getCause() != HangingBreakEvent.RemoveCause.EXPLOSION) {
            return;
        }
        Hanging entity = event.getEntity();
        Block host = entity.getLocation().getBlock().getRelative(entity.getAttachedFace());
        Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(host);
        if (reinforcement == null) {
            return;
        }
        if (reinforcement.isInsecure()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void entityDamageEvent(EntityDamageByEntityEvent event) {
        if (!Citadel.getInstance().getConfigManager().doHangersInheritReinforcements()) {
            return;
        }
        if (!(event.getEntity() instanceof final Hanging entity)) {
            return;
        }
        if (!(event.getDamager() instanceof final Player player)) {
            event.setCancelled(true);
            return;
        }
        Block host = entity.getLocation().getBlock().getRelative(entity.getAttachedFace());
        Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(host);
        if (reinforcement == null) {
            return;
        }
        if (entity instanceof ItemFrame) {
            if (ItemUtils.isValidItem(((ItemFrame) entity).getItem())) {
                if (reinforcement.isInsecure()) {
                    return;
                }
                if (reinforcement.hasPermission(player, CitadelPermissionHandler.getItemFramePutTake())) {
                    return;
                }
            }
        }
        player.sendMessage(ChatColor.RED + "The host block is protecting this.");
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerEntityInteractEvent(PlayerInteractEntityEvent event) {
        if (!Citadel.getInstance().getConfigManager().doHangersInheritReinforcements()) {
            return;
        }
        if (!(event.getRightClicked() instanceof final Hanging entity)) {
            return;
        }
        Block host = entity.getLocation().getBlock().getRelative(entity.getAttachedFace());
        Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(host);
        if (reinforcement == null) {
            return;
        }
        Player player = event.getPlayer();
        if (entity instanceof ItemFrame) {
            if (reinforcement.isInsecure()) {
                return;
            }
            // If the Item Frame already has an item, then the only possible action is rotation
            if (ItemUtils.isValidItem(((ItemFrame) entity).getItem())) {
                if (reinforcement.hasPermission(player, CitadelPermissionHandler.getItemFrameRotate())) {
                    return;
                }
            }
            // If the Item Frame is empty, then the only possible action is placement
            if (reinforcement.hasPermission(player, CitadelPermissionHandler.getItemFramePutTake())) {
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "You do not have permission to alter that.");
        event.setCancelled(true);
    }

    // prevent editing signs without permission
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerEditSign(PlayerOpenSignEvent event) {
        Player player = event.getPlayer();

        Reinforcement reinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(event.getSign().getBlock());
        if (reinforcement == null) return;

        if (!reinforcement.hasPermission(player, CitadelPermissionHandler.getModifyBlocks())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to modify this block");
            event.setCancelled(true);
        }
    }
}
