package vg.civcraft.mc.citadel.listener;

import com.destroystokyo.paper.MaterialTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.block.data.type.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelPermissionHandler;
import vg.civcraft.mc.citadel.CitadelUtility;
import vg.civcraft.mc.citadel.ReinforcementLogic;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MoreTags;
import vg.civcraft.mc.civmodcore.utilities.DoubleInteractFixer;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class BlockListener implements Listener {

	private static final Material matfire = Material.FIRE;

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
	public void blockPhysEvent(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		if (block.getType().hasGravity()) {
			Reinforcement rein = ReinforcementLogic.getReinforcementProtecting(block);
			if (rein != null) {
				event.setCancelled(true);
			}
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
					"You can not place this because it'd allow bypassing a nearby reinforcement");
			return;
		}
		// Comparators can also read through a single opaque block
		if (block.getType().isOccluding()) {
			if (ReinforcementLogic.isPreventingBlockAccess(event.getPlayer(),
					block.getRelative(comparator.getFacing().getOppositeFace()))) {
				event.setCancelled(true);
				CitadelUtility.sendAndLog(event.getPlayer(), ChatColor.RED,
						"You can not place this because it'd allow bypassing a nearby reinforcement");
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
		if (e.getClickedBlock().getState() instanceof Container) {
			if (!rein.hasPermission(e.getPlayer(), CitadelPermissionHandler.getChests())) {
				e.setCancelled(true);
				String msg = String.format("%s is locked with %s%s", e.getClickedBlock().getType().name(),
						ChatColor.AQUA, rein.getType().getName());
				CitadelUtility.sendAndLog(e.getPlayer(), ChatColor.RED, msg);
			}
			return;
		}
		if (e.getClickedBlock().getBlockData() instanceof Openable) {
			if (!rein.hasPermission(e.getPlayer(), CitadelPermissionHandler.getDoors())) {
				e.setCancelled(true);
				String msg = String.format("%s is locked with %s%s", e.getClickedBlock().getType().name(),
						ChatColor.AQUA, rein.getType().getName());
				CitadelUtility.sendAndLog(e.getPlayer(), ChatColor.RED, msg);
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
						"You can not place this because it'd allow bypassing a nearby reinforcement");
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

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void preventStrippingLogs(PlayerInteractEvent pie) {
		if (!pie.hasBlock()) {
			return;
		}
		if (pie.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		Block block = pie.getClickedBlock();
		if (!MoreTags.LOGS.isTagged(block.getType())) {
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
		if (!MaterialTags.AXES.isTagged(relevant.getType())) {
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
		if (!MoreTags.COPPER_BLOCKS.isTagged(event.getBlockPlaced().getType())) {
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
		if (!MoreTags.LIGHTABLE_CANDLES.isTagged(type)) {
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
			CitadelUtility.sendAndLog(pie.getPlayer(), ChatColor.RED, msg);
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
		CitadelUtility.sendAndLog(clicker, ChatColor.RED, "You cannot modify that lectern.");
	}

	@EventHandler(ignoreCancelled = true)
	public void onMossSpread(BlockFertilizeEvent event) {
		for (BlockState block : event.getBlocks()) {
			if (Citadel.getInstance().getReinforcementManager().getReinforcement(block.getBlock()) != null) {
				event.getPlayer().sendMessage(Component.text("You can't do that while their are reinforced blocks around!").color(NamedTextColor.RED));
				event.setCancelled(true);
				return;
			}
		}
	}
}
