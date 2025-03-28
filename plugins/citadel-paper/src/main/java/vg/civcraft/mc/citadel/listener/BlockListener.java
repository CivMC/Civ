package vg.civcraft.mc.citadel.listener;

import com.destroystokyo.paper.MaterialTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.SculkBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.Brushable;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SculkBloomEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.utilities.DoubleInteractFixer;
import vg.civcraft.mc.civmodcore.world.WorldUtils;
import vg.civcraft.mc.namelayer.group.Group;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BlockListener implements Listener {

    private static final Material matfire = Material.FIRE;
    private final Map<Block, Block> mossSpreadingDispensers = new HashMap<>();

    private DoubleInteractFixer interactFixer;

    public BlockListener(Citadel plugin) {
        this.interactFixer = new DoubleInteractFixer(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void blockBreakEvent(BlockBreakEvent event) {
        Citadel.getInstance().getStateManager().getState(event.getPlayer()).handleBreakBlock(event);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void blockPlaceEvent(BlockPlaceEvent event) {
        Citadel.getInstance().getStateManager().getState(event.getPlayer()).handleBlockPlace(event);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void plantExploitFix(BlockPlaceEvent event) {
        Block placed = event.getBlockPlaced();
        Block placedAgainst = event.getBlockAgainst();

        Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(placed);
        if (reinforcement == null) {
            return;
        }
        if (reinforcement.hasPermission(event.getPlayer(), CitadelPermissionHandler.getCrops())) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("You cannot place this without the permission " + CitadelPermissionHandler.getCrops().getName() + " on it's group!", NamedTextColor.RED));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void blockBurn(BlockBurnEvent bbe) {
        Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(bbe.getBlock());
        if (reinforcement == null) {
            return;
        }
        bbe.setCancelled(true);
        Block block = bbe.getBlock();
        // Basic essential fire protection
        if (block.getRelative(0, 1, 0).getType() == matfire) {
            block.getRelative(0, 1, 0).setType(Material.AIR);
        } // Essential
        // Extended fire protection (recommend)
        if (block.getRelative(1, 0, 0).getType() == matfire) {
            block.getRelative(1, 0, 0).setType(Material.AIR);
        }
        if (block.getRelative(-1, 0, 0).getType() == matfire) {
            block.getRelative(-1, 0, 0).setType(Material.AIR);
        }
        if (block.getRelative(0, -1, 0).getType() == matfire) {
            block.getRelative(0, -1, 0).setType(Material.AIR);
        }
        if (block.getRelative(0, 0, 1).getType() == matfire) {
            block.getRelative(0, 0, 1).setType(Material.AIR);
        }
        if (block.getRelative(0, 0, -1).getType() == matfire) {
            block.getRelative(0, 0, -1).setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }

        Block block = event.getBlock();
        Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(block);

        if (rein != null) {
            event.setCancelled(true);
        }
    }

    // Stop comparators from being placed unless the reinforcement is insecure
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void comparatorPlaceCheck(BlockPlaceEvent event) {
        // We only care if they are placing a comparator
        if (event.getBlockPlaced().getType() != Material.COMPARATOR) {
            return;
        }
        Comparator comparator = (Comparator) event.getBlockPlaced().getBlockData();
        Block block = event.getBlockPlaced().getRelative(comparator.getFacing().getOppositeFace());
        // Check if the comparator is placed against something with an inventory
        if (ReinforcementLogic.isPreventingBlockAccess(event.getPlayer(), block)) {
            event.setCancelled(true);
            CitadelUtility.sendAndLog(event.getPlayer(), ChatColor.RED,
                "You can not place this because it'd allow bypassing a nearby reinforcement",
                block.getLocation());
            return;
        }
        // Comparators can also read through a single opaque block
        if (block.getType().isOccluding()) {
            if (ReinforcementLogic.isPreventingBlockAccess(event.getPlayer(),
                block.getRelative(comparator.getFacing().getOppositeFace()))) {
                event.setCancelled(true);
                CitadelUtility.sendAndLog(event.getPlayer(), ChatColor.RED,
                    "You can not place this because it'd allow bypassing a nearby reinforcement",
                    block.getLocation());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void interact(PlayerInteractEvent pie) {
        if (pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (interactFixer.checkInteracted(pie.getPlayer(), pie.getClickedBlock())) {
                return;
            }
        } else if (pie.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Citadel.getInstance().getStateManager().getState(pie.getPlayer()).handleInteractBlock(pie);
    }

    // prevent placing water inside of reinforced blocks
    @EventHandler(priority = EventPriority.LOW)
    public void liquidDumpEvent(PlayerBucketEmptyEvent event) {
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        if (block.getType() == Material.AIR || block.getType().isSolid()) {
            return;
        }
        Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(block);
        if (rein != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockFromToEvent(BlockFromToEvent event) {
        // prevent water/lava from spilling reinforced blocks away
        if (event.getToBlock().getY() < event.getToBlock().getWorld().getMinHeight()) {
            return;
        }
        Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(event.getToBlock());
        if (rein != null) {
            event.setCancelled(true);
        }
    }

    // prevent "enemy" grass spreading to reinforced dirt
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onGrassSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() != Material.GRASS_BLOCK) return;

        Reinforcement destRein = ReinforcementLogic.getReinforcementProtecting(event.getBlock());
        if (destRein == null) {
            return;
        }

        Reinforcement sourceRein = ReinforcementLogic.getReinforcementProtecting(event.getSource());
        if (sourceRein == null || sourceRein.getGroupId() != destRein.getGroupId()) {
            event.setCancelled(true);
        }
    }

    // prevent breaking reinforced blocks through plant growth
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        for (BlockState block_state : event.getBlocks()) {
            if (ReinforcementLogic.getReinforcementProtecting(block_state.getBlock()) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // prevent sculk spread if block is reinforced
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSculkSpread(BlockSpreadEvent event) {
        if (event.getSource().getType() != Material.SCULK_CATALYST) return;

        if (ReinforcementLogic.getReinforcementProtecting(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    // prevent drying mud to clay if the mud is reinforced
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMudDry(BlockFormEvent event) {
        if (event.getNewState().getType() != Material.CLAY &&
            event.getBlock().getType() != Material.MUD) return;

        if (ReinforcementLogic.getReinforcementProtecting(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    // prevent concrete hardening if the concrete powder is reinforced
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onConcreteHarden(BlockFormEvent event) {
        if (!(MaterialTags.CONCRETES.isTagged(event.getNewState().getType()) &&
            MaterialTags.CONCRETE_POWDER.isTagged(event.getBlock().getType()))) return;

        if (ReinforcementLogic.getReinforcementProtecting(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    // prevent dirt to mud if dirt is reinforced
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMudCreated(EntityChangeBlockEvent event) {
        if (!Tag.CONVERTABLE_TO_MUD.isTagged(event.getBlock().getType()) ||
            event.getTo() != Material.MUD) return;

        Reinforcement reinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(event.getBlock().getLocation());
        if (reinforcement == null) return;

        Entity eventEntity = event.getEntity();

        if (!(eventEntity instanceof Player player)) return;

        if (!reinforcement.hasPermission(player, CitadelPermissionHandler.getModifyBlocks())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to modify this block");
            event.setCancelled(true);
        }
    }

    // prevent opening reinforced things
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void openContainer(PlayerInteractEvent e) {
        if (!e.hasBlock()) {
            return;
        }
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(e.getClickedBlock());
        if (rein == null) {
            return;
        }
        Player player = e.getPlayer();

        // Logic copied from ServerPlayerGameMode#useItemOn
        // This will let the event happen if the player is not going to actually open/interact with the block
        boolean flag = player.getInventory().getItemInMainHand().getType() != Material.AIR || player.getInventory().getItemInOffHand().getType() != Material.AIR;
        if (player.isSneaking() && flag) {
            return;
        }

        // Iron trapdoors and doors cannot be opened by right clicking on them
        if (e.getClickedBlock().getType() == Material.IRON_TRAPDOOR || e.getClickedBlock().getType() == Material.IRON_DOOR) {
            return;
        }

        if (e.getClickedBlock().getState() instanceof Container) {
            if (!rein.hasPermission(player, CitadelPermissionHandler.getChests())) {
                e.setCancelled(true);
                String msg = String.format("%s is locked with %s%s", e.getClickedBlock().getType().name(),
                    ChatColor.AQUA, rein.getType().getName());
                CitadelUtility.sendAndLog(player, ChatColor.RED, msg, e.getClickedBlock().getLocation());
            }
            return;
        }
        if (e.getClickedBlock().getBlockData() instanceof Openable) {
            if (!rein.hasPermission(player, CitadelPermissionHandler.getDoors())) {
                e.setCancelled(true);
                String msg = String.format("%s is locked with %s%s", e.getClickedBlock().getType().name(),
                    ChatColor.AQUA, rein.getType().getName());
                CitadelUtility.sendAndLog(player, ChatColor.RED, msg, e.getClickedBlock().getLocation());
            }
        }
    }

    // prevent players from upgrading a chest into a double chest to bypass the
    // single chests reinforcement
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void preventBypassChestAccess(BlockPlaceEvent e) {
        Material mat = e.getBlock().getType();
        if (mat != Material.CHEST && mat != Material.TRAPPED_CHEST) {
            return;
        }
        for (Block rel : WorldUtils.getPlanarBlockSides(e.getBlock(), true)) {
            if (rel.getType() == mat && ReinforcementLogic.isPreventingBlockAccess(e.getPlayer(), rel)) {
                e.setCancelled(true);
                CitadelUtility.sendAndLog(e.getPlayer(), ChatColor.RED,
                    "You can not place this because it'd allow bypassing a nearby reinforcement", rel.getLocation());
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void removeReinforcedAir(BlockPlaceEvent e) {
        if (!MaterialUtils.isAir(e.getBlockReplacedState().getType())) {
            return;
        }
        Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(e.getBlock());
        if (rein != null) {
            rein.setHealth(-1);
        }
    }

    @EventHandler(
        priority = EventPriority.LOW,
        ignoreCancelled = true
    )
    private void preventStrippingLogs(
        final @NotNull PlayerInteractEvent event
    ) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Block block = event.getClickedBlock();
        if (block == null) { // This shouldn't happen, but just in case
            return;
        }
        switch (block.getType()) {
            case ACACIA_LOG:
            case BIRCH_LOG:
            case CHERRY_LOG:
            case DARK_OAK_LOG:
            case JUNGLE_LOG:
            case MANGROVE_LOG:
            case OAK_LOG:
            case SPRUCE_LOG:
                break;
            default:
                return;
        }
        final Player player = event.getPlayer();
        final ItemStack tool = switch (event.getHand()) {
            case HAND -> player.getInventory().getItemInMainHand();
            case OFF_HAND -> player.getInventory().getItemInOffHand();
            case null, default -> null;
        };
        if (ItemUtils.isEmptyItem(tool) || !MaterialTags.AXES.isTagged(tool)) {
            return;
        }
        final Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
        if (rein == null || rein.hasPermission(player, CitadelPermissionHandler.getModifyBlocks())) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(Component.text(
            "You do not have permission to modify this block",
            NamedTextColor.RED
        ));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void rightClickCaveVines(PlayerHarvestBlockEvent event) {
        Block harvestedBlock = event.getHarvestedBlock();
        Reinforcement reinforcement = ReinforcementLogic.getReinforcementProtecting(harvestedBlock);
        if (reinforcement == null) {
            return;
        }
        if (!reinforcement.hasPermission(event.getPlayer(), CitadelPermissionHandler.getCrops())) {
            event.getPlayer().sendMessage(Component.text("You do not have permission to harvest this crop").color(NamedTextColor.RED));
            event.setCancelled(true);
        }
    }

    /*
    For some stupid reason, Waxing / Stripping copper blocks calls a BlockPlaceEvent instead of PlayerInteractEvent,
    this obviously might change in future so heres a warning note
    Reminder: This is retarded
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void preventWaxingCopper(BlockPlaceEvent event) {
        if (!MaterialTags.COPPER_BLOCKS.isTagged(event.getBlockPlaced().getType())) {
            return;
        }
        Reinforcement reinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(event.getBlockPlaced());
        if (reinforcement == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!reinforcement.hasPermission(player, CitadelPermissionHandler.getModifyBlocks())) {
            player.sendMessage(ChatColor.RED + "You do not have permission to modify this block");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void preventTilingGrass(PlayerInteractEvent pie) {
        if (!pie.hasBlock()) {
            return;
        }
        if (pie.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = pie.getClickedBlock();
        if (block.getType() != Material.GRASS_BLOCK) {
            return;
        }
        EquipmentSlot hand = pie.getHand();
        if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
            return;
        }
        ItemStack relevant;
        Player p = pie.getPlayer();
        if (hand == EquipmentSlot.HAND) {
            relevant = p.getInventory().getItemInMainHand();
        } else {
            relevant = p.getInventory().getItemInOffHand();
        }
        if (!MaterialTags.SHOVELS.isTagged(relevant.getType())) {
            return;
        }
        Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
        if (rein == null) {
            return;
        }
        if (!rein.hasPermission(p, CitadelPermissionHandler.getModifyBlocks())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to modify this block");
            pie.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void preventTillingDirtIntoFarmland(PlayerInteractEvent pie) {
        if (!pie.hasBlock()) {
            return;
        }
        if (pie.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = pie.getClickedBlock();
        Material type = block.getType();
        if (type != Material.GRASS_BLOCK && type != Material.DIRT && type != Material.COARSE_DIRT
            && type != Material.DIRT_PATH) {
            return;
        }
        EquipmentSlot hand = pie.getHand();
        if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
            return;
        }
        ItemStack relevant;
        Player p = pie.getPlayer();
        if (hand == EquipmentSlot.HAND) {
            relevant = p.getInventory().getItemInMainHand();
        } else {
            relevant = p.getInventory().getItemInOffHand();
        }
        if (!MaterialTags.HOES.isTagged(relevant.getType())) {
            return;
        }
        Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
        if (rein == null) {
            return;
        }
        if (!rein.hasPermission(p, CitadelPermissionHandler.getModifyBlocks())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to modify this block");
            pie.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void preventHarvestingHoney(PlayerInteractEvent pie) {
        if (!pie.hasBlock()) {
            return;
        }
        if (pie.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = pie.getClickedBlock();
        Material type = block.getType();
        if (type != Material.BEE_NEST && type != Material.BEEHIVE) {
            return;
        }
        EquipmentSlot hand = pie.getHand();
        if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
            return;
        }
        ItemStack relevant;
        Player p = pie.getPlayer();
        if (hand == EquipmentSlot.HAND) {
            relevant = p.getInventory().getItemInMainHand();
        } else {
            relevant = p.getInventory().getItemInOffHand();
        }
        if (relevant.getType() != Material.SHEARS && relevant.getType() != Material.GLASS_BOTTLE) {
            return;
        }
        Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
        if (rein == null) {
            return;
        }
        if (!rein.hasPermission(p, CitadelPermissionHandler.getModifyBlocks())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to harvest this block");
            pie.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void preventLightingCandles(PlayerInteractEvent pie) {
        if (!pie.hasBlock()) {
            return;
        }
        if (pie.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = pie.getClickedBlock();
        Material type = block.getType();
        if (!Tag.CANDLES.isTagged(type) && !Tag.CANDLE_CAKES.isTagged(type)) {
            return;
        }
        if (!pie.hasItem()) {
            Lightable candles = (Lightable) block.getBlockData();
            if (candles.isLit()) {
                Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
                if (rein == null) {
                    return;
                }
                if (!rein.hasPermission(pie.getPlayer(), CitadelPermissionHandler.getModifyBlocks())) {
                    pie.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to modify this block");
                    pie.setCancelled(true);
                    return;
                }
            }
        }
        EquipmentSlot hand = pie.getHand();
        if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
            return;
        }
        ItemStack relevant;
        Player p = pie.getPlayer();
        if (hand == EquipmentSlot.HAND) {
            relevant = p.getInventory().getItemInMainHand();
        } else {
            relevant = p.getInventory().getItemInOffHand();
        }
        if (relevant.getType() != Material.FLINT_AND_STEEL) {
            return;
        }
        Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
        if (rein == null) {
            return;
        }
        if (!rein.hasPermission(p, CitadelPermissionHandler.getModifyBlocks())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to modify this block");
            pie.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
    public void openBeacon(PlayerInteractEvent pie) {
        if (!pie.hasBlock()) {
            return;
        }
        if (pie.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (pie.getClickedBlock().getType() != Material.BEACON) {
            return;
        }
        Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(pie.getClickedBlock());
        if (rein == null) {
            return;
        }
        if (!rein.hasPermission(pie.getPlayer(), CitadelPermissionHandler.getBeacon())) {
            pie.setCancelled(true);
            String msg = String.format("%s is locked with %s%s", pie.getClickedBlock().getType().name(), ChatColor.AQUA,
                rein.getType().getName());
            CitadelUtility.sendAndLog(pie.getPlayer(), ChatColor.RED, msg, pie.getClickedBlock().getLocation());
        }
    }

    // ------------------------------------------------------------
    // Lecterns
    // ------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void preventLecternPutBook(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final var clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.LECTERN) {
            return;
        }
        // if the lectern has a book, then the person is right clicking to read that book, not place in a new book
        final var clickedBlockData = (Lectern) clickedBlock.getBlockData();
        if (clickedBlockData.hasBook()) {
            return;
        }
        INTERNAL_checkLecternModificationPermission(event, event.getPlayer(), clickedBlock.getLocation());
    }

    @EventHandler(ignoreCancelled = true)
    public void preventLecternTakeBook(final PlayerTakeLecternBookEvent event) {
        INTERNAL_checkLecternModificationPermission(event, event.getPlayer(), event.getLectern().getLocation());
    }

    private void INTERNAL_checkLecternModificationPermission(final Cancellable event,
                                                             final Player clicker,
                                                             final Location lecternLocation) {
        // We already know that a lectern is a single block, thus the additional logic
        // of ReinforcementLogic.getReinforcementProtecting(block) is unnecessary
        final var reinforcement = ReinforcementLogic.getReinforcementAt(lecternLocation);
        if (reinforcement == null
            || reinforcement.isInsecure()
            || reinforcement.hasPermission(clicker.getUniqueId(), CitadelPermissionHandler.getChests())) {
            return;
        }
        event.setCancelled(true);
        CitadelUtility.sendAndLog(clicker, ChatColor.RED, "You cannot modify that lectern.", lecternLocation);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMossSpread(BlockFertilizeEvent event) {
        Player player = event.getPlayer();
        Iterator<BlockState> iterator = event.getBlocks().iterator();
        Block dispenser = mossSpreadingDispensers.remove(event.getBlock());
        Reinforcement dispenserRein = dispenser == null ? null
            : Citadel.getInstance()
            .getReinforcementManager()
            .getReinforcement(dispenser);
        Group dispenserGroup = dispenserRein == null ? null :
            dispenserRein.getGroup();
        while (iterator.hasNext()) {
            BlockState block = iterator.next();
            Reinforcement reinforcement = Citadel.getInstance()
                .getReinforcementManager()
                .getReinforcement(block.getBlock());
            if (reinforcement == null) {
                continue;
            }
            if (player != null && reinforcement.hasPermission(player, CitadelPermissionHandler.getCrops())) {
                continue;
            }
            if (dispenserGroup != null && dispenserGroup == reinforcement.getGroup()) {
                continue;
            }
            iterator.remove();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDispenserGrowMoss(BlockDispenseEvent event) {
        Block dispenserBlock = event.getBlock();
        ItemStack bonemeal = event.getItem();
        Dispenser dispenser = (Dispenser) event.getBlock().getBlockData();
        if (bonemeal.getType() != Material.BONE_MEAL) {
            return;
        }
        Block moss = dispenserBlock.getRelative(dispenser.getFacing());
        if (moss.getType() != Material.MOSS_BLOCK) {
            return;
        }
        mossSpreadingDispensers.put(moss, dispenserBlock);
    }

    @EventHandler(ignoreCancelled = true)
    public void preventHostileAnvilUsage(
        final PlayerInteractEvent event
    ) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || !Tag.ANVIL.isTagged(clickedBlock.getType())) {
            return;
        }
        final Player clicker = event.getPlayer();
        // We already know that an anvil is a single block, thus the additional logic
        // of ReinforcementLogic.getReinforcementProtecting(block) is unnecessary
        final Reinforcement reinforcement = ReinforcementLogic.getReinforcementAt(clickedBlock.getLocation());
        if (reinforcement == null
            || reinforcement.isInsecure()
            || reinforcement.hasPermission(clicker.getUniqueId(), CitadelPermissionHandler.getChests())) {
            return;
        }
        event.setCancelled(true);
        CitadelUtility.sendAndLog(clicker, ChatColor.RED, "You cannot use that anvil.", clickedBlock.getLocation());
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void preventTakingBooks(PlayerInteractEvent pie) {
        if (!pie.hasBlock()) {
            return;
        }
        if (pie.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = pie.getClickedBlock();
        Material type = block.getType();
        if (type != Material.CHISELED_BOOKSHELF) {
            return;
        }
        EquipmentSlot hand = pie.getHand();
        if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
            return;
        }
        ItemStack relevant;
        Player p = pie.getPlayer();
        if (hand == EquipmentSlot.HAND) {
            relevant = p.getInventory().getItemInMainHand();
        } else {
            relevant = p.getInventory().getItemInOffHand();
        }
        if (relevant.getType() != Material.AIR && !Tag.ITEMS_BOOKSHELF_BOOKS.isTagged(relevant.getType())) {
            return;
        }
        Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
        if (rein == null) {
            return;
        }
        if (!rein.hasPermission(p, CitadelPermissionHandler.getModifyBlocks())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to " +
                (Tag.ITEMS_BOOKSHELF_BOOKS.isTagged(relevant.getType()) ? "place books in this shelf" : "take books from this shelf"));
            pie.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void preventDusting(PlayerInteractEvent pie) { // Maybe this should be changed to brushing? The two terms seem to be used interchangeably.
        if (!pie.hasBlock()) {
            return;
        }
        if (pie.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = pie.getClickedBlock();
        Material type = block.getType();
        if (!(block.getBlockData() instanceof Brushable)) {
            return;
        }
        EquipmentSlot hand = pie.getHand();
        if (hand != EquipmentSlot.HAND && hand != EquipmentSlot.OFF_HAND) {
            return;
        }
        ItemStack relevant;
        Player p = pie.getPlayer();
        if (hand == EquipmentSlot.HAND) {
            relevant = p.getInventory().getItemInMainHand();
        } else {
            relevant = p.getInventory().getItemInOffHand();
        }
        if (relevant.getType() != Material.BRUSH) {
            return;
        }
        Reinforcement rein = Citadel.getInstance().getReinforcementManager().getReinforcement(block);
        if (rein == null) {
            return;
        }
        if (!rein.hasPermission(p, CitadelPermissionHandler.getModifyBlocks())) {
            p.sendMessage(ChatColor.RED + "You do not have permission to dust this block");
            pie.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void preventFlowerTheft(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null || !Tag.FLOWER_POTS.isTagged(block.getType())) {
            return;
        }
        Player player = event.getPlayer();

        Reinforcement reinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(block);

        if (reinforcement == null
            || reinforcement.isInsecure()
            || reinforcement.hasPermission(player.getUniqueId(), CitadelPermissionHandler.getModifyBlocks())) {
            return;
        }
        event.setCancelled(true);
        player.sendMessage(ChatColor.RED + "You do not have permission to modify this block");
    }
}
