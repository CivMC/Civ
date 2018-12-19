package vg.civcraft.mc.citadel.listener;

import static vg.civcraft.mc.citadel.Utility.canPlace;
import static vg.civcraft.mc.citadel.Utility.createNaturalReinforcement;
import static vg.civcraft.mc.citadel.Utility.createPlayerReinforcement;
import static vg.civcraft.mc.citadel.Utility.isAuthorizedPlayerNear;
import static vg.civcraft.mc.citadel.Utility.isDroppedReinforcementBlock;
import static vg.civcraft.mc.citadel.Utility.isPlant;
import static vg.civcraft.mc.citadel.Utility.maybeReinforcementDamaged;
import static vg.civcraft.mc.citadel.Utility.reinforcementBroken;
import static vg.civcraft.mc.citadel.Utility.reinforcementDamaged;
import static vg.civcraft.mc.citadel.Utility.timeUntilMature;
import static vg.civcraft.mc.citadel.Utility.timeUntilAcidMature;
import static vg.civcraft.mc.citadel.Utility.wouldPlantDoubleReinforce;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Comparator;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;
import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.ReinforcementMode;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.events.ReinforcementChangeTypeEvent;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;
import vg.civcraft.mc.citadel.misc.ReinforcemnetFortificationCancelException;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;

public class BlockListener implements Listener {

	public static final List<BlockFace> all_sides = Arrays.asList(BlockFace.UP,
			BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST,
			BlockFace.EAST);

	public static final List<BlockFace> planar_sides = Arrays.asList(
			BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

	private ReinforcementManager rm = Citadel.getReinforcementManager();

	//Stop comparators from being placed unless the reinforcement is insecure
	@EventHandler(priority = EventPriority.HIGH,ignoreCancelled = true)
	public void comparatorPlaceCheck(BlockPlaceEvent event)
	{
		//We only care if they are placing a comparator
		if(event.getBlockPlaced().getType() != Material.REDSTONE_COMPARATOR_OFF) return;

		Comparator comparator = (Comparator)event.getBlockPlaced().getState().getData();
		Block block = event.getBlockPlaced().getRelative(comparator.getFacing().getOppositeFace());
		//We only care if the comparator is going placed against something with an inventory
		if(block.getState() instanceof InventoryHolder) {
			Reinforcement rein = rm.getReinforcement(Utility.getRealBlock(block));
			if (rein != null && rein instanceof PlayerReinforcement) {
				PlayerReinforcement playerReinforcement = (PlayerReinforcement) rein;
				if (!playerReinforcement.isInsecure()) { //Only let them place against /ctinsecure
					Player player = event.getPlayer();
					if (player != null) {
						if (playerReinforcement.canAccessChests(player)) {
							return; // We also allow players to place against chests they can access
						}
						Utility.sendAndLog(player, ChatColor.RED, "You cannot place that next to a container you do not own.");
					}
					event.setCancelled(true);
				}
			}
			return;
		}

		// So apparently read-through state can pass through an intermediary block, so lets check that too.
		block = block.getRelative(comparator.getFacing().getOppositeFace());
		if(block.getState() instanceof InventoryHolder) {
			Reinforcement rein = rm.getReinforcement(Utility.getRealBlock(block));
			if (rein != null && rein instanceof PlayerReinforcement) {
				PlayerReinforcement playerReinforcement = (PlayerReinforcement) rein;
				if (!playerReinforcement.isInsecure()) { //Only let them place against /ctinsecure
					Player player = event.getPlayer();
					if (player != null) {
						if (playerReinforcement.canAccessChests(player)) {
							return; // We also allow players to place against chests they can access
						}
						Utility.sendAndLog(player, ChatColor.RED, "You cannot place that next to a container you do not own.");
					}
					event.setCancelled(true);
				}
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onFortificationMode(BlockPlaceEvent event){
		Player p = event.getPlayer();
		Block b = event.getBlock();
		GroupManager gm = NameAPI.getGroupManager();
		Location loc = b.getLocation();
		Inventory inv = p.getInventory();
		Reinforcement rein = rm.getReinforcement(b.getLocation());
		if (Material.AIR.equals(event.getBlockReplacedState().getType())) {
			if (rein != null && rein instanceof PlayerReinforcement){
				//Would be nice to find a more performant way to detect the entity w/o checking every entity.
				for (Entity e : loc.getChunk().getEntities()){
					if (e instanceof Hanging){
						Location eloc = e.getLocation().getBlock().getLocation();
						if (eloc.getBlockX() == loc.getBlockX() && eloc.getBlockY() == loc.getBlockY()
								&& eloc.getBlockZ() == loc.getBlockZ()){
							event.setCancelled(true);
							return;
						}
					}
				}

				rm.deleteReinforcement(rein);
			}
			ItemStack stack = event.getItemInHand();
			rein = isDroppedReinforcementBlock(p, stack, loc);
			if (rein != null){
				rm.saveInitialReinforcement(rein);
				return;
			}
		}
		PlayerState state = PlayerState.get(p);
		ReinforcementType type = null;
		Group groupToReinforceTo = null;
		if (state.getMode() == ReinforcementMode.REINFORCEMENT_FORTIFICATION) {
			type = state.getReinforcementType();
			if (type == null) {
				Utility.sendAndLog(p, ChatColor.RED, "Something went wrong, you dont seem to have a reinforcement material selected?");
				state.reset();
				event.setCancelled(true);
				return;
			}
			groupToReinforceTo = state.getGroup();
		}else if(state.getMode() == ReinforcementMode.NORMAL) {
			if (!state.getEasyMode()) {
				return;
			}
				type =  ReinforcementType.getReinforcementType(p.getInventory().getItemInOffHand());
				if (type == null) {
					return;
				}
				String gName = gm.getDefaultGroup(p.getUniqueId());
				if (gName != null) {
					groupToReinforceTo = GroupManager.getGroup(gName);
				}
				if (groupToReinforceTo == null) {
					return;
				}
			}
			else {
				return;
		}

		if (!canPlace(b, p)){
			Utility.sendAndLog(p, ChatColor.RED, "Cancelled block place, mismatched reinforcement.");
			event.setCancelled(true);
			return;
		}
		 // Don't allow double reinforcing reinforceable plants
        if (wouldPlantDoubleReinforce(b)) {
        	Utility.sendAndLog(p, ChatColor.RED, "Cancelled block place, crop would already be reinforced.");
            event.setCancelled(true);
            return;
        }
        // Don't allow incorrect reinforcement with exclusive reinforcement types
        if (!type.canBeReinforced(b.getType())) {
            Utility.sendAndLog(p, ChatColor.RED, "That material cannot reinforce that type of block. Try a different reinforcement material.");
            event.setCancelled(true);
            return;
        }
        int required = type.getRequiredAmount();
        if (type.getItemStack().isSimilar(event.getItemInHand())){
        	required++;
        }
		if (inv.containsAtLeast(type.getItemStack(), required)) {
			try {
				if (createPlayerReinforcement(p, groupToReinforceTo, b, type, event.getItemInHand()) == null) {
					Utility.sendAndLog(p, ChatColor.RED, String.format("%s is not a reinforcible material ", b.getType().name()));
				} else {
					state.checkResetMode();
				}
			} catch(ReinforcemnetFortificationCancelException ex){
				Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementFortificationCancelException occured in BlockListener, BlockPlaceEvent ", ex);
			}
        } else {
        	if (state.getMode() == ReinforcementMode.REINFORCEMENT_FORTIFICATION) {
	        	Utility.sendAndLog(p, ChatColor.YELLOW, String.format("%s depleted, left fortification mode ",
	            		state.getReinforcementType().getMaterial().name()));
	            state.reset();
	            event.setCancelled(true);
        	}
        }
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockBreakEvent(BlockBreakEvent event) {
		Block block = event.getBlock();
		Player player = event.getPlayer();
		Block reinforcingBlock = null;
		Reinforcement rein = rm.getReinforcement(Utility.getRealBlock(block));

		// if block is a plant check reinforcement on soil block
		if (isPlant(block)) {
			reinforcingBlock = Utility.findPlantSoil(block);
			Reinforcement plant = null;
			if (reinforcingBlock != null)
				plant = rm.getReinforcement(reinforcingBlock);
			if (plant != null)
				rein = plant;
		}

		if (rein == null) {
			rein = createNaturalReinforcement(event.getBlock(), player);
			if (rein != null) {
				ReinforcementDamageEvent e = new ReinforcementDamageEvent(rein,
						player, block);
				Bukkit.getPluginManager().callEvent(e);
				if (e.isCancelled()) {
					event.setCancelled(true);
					return;
				}
				if (reinforcementDamaged(player, rein)) {
					event.setCancelled(true);
				}
			}
			return;
		}

		boolean is_cancelled = true;
		if (rein instanceof PlayerReinforcement) {
			PlayerReinforcement pr = (PlayerReinforcement) rein;
			PlayerState state = PlayerState.get(player);
			boolean admin_bypass = player
					.hasPermission("citadel.admin.bypassmode");
			if (reinforcingBlock != null && isPlant(block)
					&& (pr.canAccessCrops(player) || admin_bypass)) {
				// player has CROPS access to the soil block, allow them to
				// break without affecting reinforcement
				is_cancelled = false;
			} else if (state.isBypassMode()
					&& (pr.canBypass(player) || admin_bypass)
					&& !pr.getGroup().isDisciplined()) {
				if (admin_bypass) {
					/*
					 * Citadel.verbose( VerboseMsg.AdminReinBypass,
					 * player.getDisplayName(),
					 * pr.getBlock().getLocation().toString());
					 */
				} else {
					/*
					 * Citadel.verbose( VerboseMsg.ReinBypass,
					 * player.getDisplayName(),
					 * pr.getBlock().getLocation().toString());
					 */
				}
				is_cancelled = reinforcementBroken(player, rein);
			} else {
				if (!state.isBypassMode() && pr.canBypass(player)) {
					player.sendMessage(ChatColor.RED + "Enable bypass mode with \"/ctb\" to break this reinforcement");
				}
				ReinforcementDamageEvent dre = new ReinforcementDamageEvent(
						rein, player, block);

				Bukkit.getPluginManager().callEvent(dre);

				if (dre.isCancelled()) {
					is_cancelled = true;
				} else {
					is_cancelled = reinforcementDamaged(player, rein);
				}
			}
			if (!is_cancelled) {
				// The player reinforcement broke. Now check for natural
				is_cancelled = createNaturalReinforcement(block, player) != null;
			}
		} else {
			ReinforcementDamageEvent dre = new ReinforcementDamageEvent(rein,
					player, block);

			Bukkit.getPluginManager().callEvent(dre);

			if (dre.isCancelled()) {
				is_cancelled = reinforcementDamaged(player, rein);
				return;
			} else {
				is_cancelled = reinforcementDamaged(player, rein);
			}
		}

		if (is_cancelled) {
			event.setCancelled(true);
			block.getDrops().clear();
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void pistonExtend(BlockPistonExtendEvent bpee) {
		for (Block block : bpee.getBlocks()) {
			Block realBlock = Utility.getRealBlock(block);
			Reinforcement reinforcement = rm.getReinforcement(realBlock
					.getLocation());
			if (reinforcement != null) {
				bpee.setCancelled(true);
				break;
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void pistonRetract(BlockPistonRetractEvent bpre) {
		for (Block block : bpre.getBlocks()) {
			Block realBlock = Utility.getRealBlock(block);
			Reinforcement reinforcement = rm.getReinforcement(realBlock
					.getLocation());
			if (reinforcement != null) {
				bpre.setCancelled(true);
				break;
			}
		}
	}

	private static final Material matfire = Material.FIRE;

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void blockBurn(BlockBurnEvent bbe) {
		boolean wasprotected = maybeReinforcementDamaged(bbe.getBlock());
		if (wasprotected) {
			bbe.setCancelled(wasprotected);
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
			// Aggressive fire protection (would seriously reduce effectiveness
			// of flint down to near the "you'd have to use it 25 times"
			// mentality)
			/*
			 * if (block.getRelative(1,1,0).getType() == matfire)
			 * {block.getRelative(1,1,0).setTypeId(0);} if
			 * (block.getRelative(1,-1,0).getType() == matfire)
			 * {block.getRelative(1,-1,0).setTypeId(0);} if
			 * (block.getRelative(-1,1,0).getType() == matfire)
			 * {block.getRelative(-1,1,0).setTypeId(0);} if
			 * (block.getRelative(-1,-1,0).getType() == matfire)
			 * {block.getRelative(-1,-1,0).setTypeId(0);} if
			 * (block.getRelative(0,1,1).getType() == matfire)
			 * {block.getRelative(0,1,1).setTypeId(0);} if
			 * (block.getRelative(0,-1,1).getType() == matfire)
			 * {block.getRelative(0,-1,1).setTypeId(0);} if
			 * (block.getRelative(0,1,-1).getType() == matfire)
			 * {block.getRelative(0,1,-1).setTypeId(0);} if
			 * (block.getRelative(0,-1,-1).getType() == matfire)
			 * {block.getRelative(0,-1,-1).setTypeId(0);}
			 */
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
	public void onBlockFromToEvent(BlockFromToEvent event) {
		Block reinforcementBlock = event.getToBlock();
		if (isPlant(reinforcementBlock)) {
			// block to is as plant check block under it for reinforcement
			reinforcementBlock = Utility.findPlantSoil(reinforcementBlock);
		}
		Reinforcement rein = rm.getReinforcement(reinforcementBlock);
		if (rein != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void redstonePower(BlockRedstoneEvent bre) {
		// This currently only protects against reinforced openable objects,
		// like doors, from being opened by unauthorizied players.
		try {
			// NewCurrent <= 0 means the redstone wire is turning off, so the
			// container is closing. Closing is good so just return. This also
			// shaves off some time when dealing with sand generators.
			// OldCurrent > 0 means that the wire was already on, thus the
			// container was already open by an authorized player. Now it's
			// either staying open or closing. Just return.
			if (bre.getNewCurrent() <= 0 || bre.getOldCurrent() > 0) {
				return;
			}
			Block block = bre.getBlock();
			MaterialData blockData = block.getState().getData();
			if (!(blockData instanceof Openable)) {
				return;
			}
			Openable openable = (Openable) blockData;
			if (openable.isOpen()) {
				return;
			}
			Reinforcement generic_reinforcement = Citadel
					.getReinforcementManager().getReinforcement(block);
			if (generic_reinforcement == null
					|| !(generic_reinforcement instanceof PlayerReinforcement)) {
				return;
			}
			PlayerReinforcement reinforcement = (PlayerReinforcement) generic_reinforcement;
			double redstoneDistance = CitadelConfigManager
					.getMaxRedstoneDistance();
			if (!isAuthorizedPlayerNear(reinforcement, redstoneDistance)) {
				// Citadel.Log(
				// reinforcement.getLocation().toString());
				bre.setNewCurrent(bre.getOldCurrent());
			}
		} catch (Exception e) {
			Citadel.getInstance()
					.getLogger()
					.log(Level.WARNING,
							"Exception occured in BlockListener, BlockRedstoneEvent ",
							e);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void interact(PlayerInteractEvent pie) {
		try {
			if (!pie.hasBlock() || (pie.getHand() != EquipmentSlot.HAND && pie.getHand() != EquipmentSlot.OFF_HAND))
				return;

			Player player = pie.getPlayer();
			Block block = Utility.getRealBlock(pie.getClickedBlock());
			Reinforcement generic_reinforcement = rm.getReinforcement(block);
			PlayerReinforcement reinforcement = null;
			if (generic_reinforcement instanceof PlayerReinforcement) {
				reinforcement = (PlayerReinforcement) generic_reinforcement;
			}

			Action action = pie.getAction();
			boolean access_reinforcement = action == Action.RIGHT_CLICK_BLOCK
					&& reinforcement != null && reinforcement.isSecurable();
			boolean normal_access_denied = reinforcement != null
					&& ((reinforcement.isDoor() && !reinforcement
							.canAccessDoors(player)) || (reinforcement
							.isContainer() && !reinforcement
							.canAccessChests(player)));
			boolean admin_can_access = player.hasPermission("citadel.admin");
			if (access_reinforcement && normal_access_denied
					&& !admin_can_access) {
				/*
				 * Citadel.verbose( VerboseMsg.ReinLocked,
				 * player.getDisplayName(), block.getLocation().toString());
				 */
				//Prevents double broadcasts
				if(pie.getHand() == EquipmentSlot.HAND) {
					pie.getPlayer().sendMessage(
							ChatColor.RED
									+ String.format("%s is locked", block.getType()
									.name()));
				}
				pie.setCancelled(true);
			}
			// Not really sure what this is for. Should come up in testing.
			/*
			 * else if (action == Action.PHYSICAL) { AccessDelegate
			 * aboveDelegate =
			 * AccessDelegate.getDelegate(block.getRelative(BlockFace.UP)); if
			 * (aboveDelegate instanceof CropAccessDelegate &&
			 * aboveDelegate.isReinforced()) { Citadel.verbose(
			 * VerboseMsg.CropTrample, block.getLocation().toString());
			 * pie.setCancelled(true); } }
			 */
			if (pie.isCancelled() || pie.getHand() != EquipmentSlot.HAND)
				return;

			PlayerState state = PlayerState.get(player);
			ReinforcementMode placementMode = state.getMode();
			GroupManager gm = NameAPI.getGroupManager();
			switch (placementMode) {
			case NORMAL:
				if (!state.getEasyMode()) {
					return;
				}
				if (pie.getAction() == Action.LEFT_CLICK_BLOCK && generic_reinforcement == null) {
					ItemStack stack = player.getInventory().getItemInMainHand();
					ReinforcementType type = ReinforcementType
							.getReinforcementType(stack);
					if (type != null) {
						 // Don't allow double reinforcing reinforceable plants
				        if (wouldPlantDoubleReinforce(block)) {
				        	Utility.sendAndLog(player, ChatColor.RED, "Cancelled block place, crop would already be reinforced.");
				            return;
				        }
				        // Don't allow incorrect reinforcement with exclusive reinforcement types
				        if (!type.canBeReinforced(block.getType())) {
				            Utility.sendAndLog(player, ChatColor.RED, "That material cannot reinforce that type of block. Try a different reinforcement material.");
				            return;
				        }
						if (!canPlace(block, player)){
							Utility.sendAndLog(player, ChatColor.RED, "Cancelled interact easymode rein, mismatched reinforcement.");
							return;
						}

						String gName = gm.getDefaultGroup(player.getUniqueId());
						Group g = null;
						if (gName != null) {
							g = GroupManager.getGroup(gName);
						}
						if (g != null) {
							if (createPlayerReinforcement(player, g, block, type, null) == null && CitadelConfigManager.shouldLogReinforcement()) {
								// someone else's job to tell the player what went wrong, but let's do log it.
								Citadel.getInstance().getLogger().log(Level.INFO, "Create Reinforcement by {0} at {1} cancelled by plugin",
										new Object[] {player.getName(), block.getLocation()});
							}
						}
					}
				}
				return;
			case REINFORCEMENT_FORTIFICATION:
				return;
			case REINFORCEMENT_INFORMATION:
				// did player click on a reinforced block?
				if (reinforcement != null) {
					String reinforcementStatus = reinforcement.getStatus();
					String ageStatus = reinforcement.getAgeStatus();
					Group group = reinforcement.getGroup();
					StringBuilder sb;
					Location blockLoc = reinforcement.getLocation();
					String blockName = reinforcement.getLocation().getBlock().getType().toString();
					String hoverMessage = String.format(
						"Block: %s\nLocation: [%s %d %d %d]",
						blockName, blockLoc.getWorld().getName(), (int)blockLoc.getX(), (int)blockLoc.getY(), (int)blockLoc.getZ());
					if (player.hasPermission("citadel.admin.ctinfodetails")) {
						Utility.sendAndLog(player, ChatColor.GREEN, String.format(
								"Loc[%s]", reinforcement.getLocation()
										.toString()));
						String groupName = "!NULL!";
						if (group != null) {
							groupName = String.format("[%s]", group.getName());
						}
						sb = new StringBuilder();
						sb.append(String.format(
								" Group%s Durability[%d/%d]",
								groupName,
								reinforcement.getDurability(),
								ReinforcementType.getReinforcementType(
										reinforcement.getStackRepresentation())
										.getHitPoints()));
						int maturationTime = timeUntilMature(reinforcement);
						if (maturationTime != 0) {
							sb.append(" Immature[");
							sb.append(maturationTime);
							sb.append("]");
						}
						int acidTime = timeUntilAcidMature(reinforcement);
						if (CitadelConfigManager.getAcidBlock() == block
								.getType()) {
							sb.append(" Acid ");
							if (acidTime != 0) {
								sb.append("Immature[");
								sb.append(acidTime);
								sb.append("]");
							} else {
								sb.append("Mature");
							}
						}
						if (reinforcement.isInsecure()) {
							sb.append(" (Insecure)");
						}
						if (group.isDisciplined()) {
							sb.append(" (Disciplined)");
						}
						sb.append("\nGroup id: " + reinforcement.getGroupId());

						Utility.sendAndLog(player, ChatColor.GREEN, sb.toString());
					} else if (reinforcement.canViewInformation(player)) {
						sb = new StringBuilder();
						boolean immature = timeUntilMature(reinforcement) != 0
								&& CitadelConfigManager.isMaturationEnabled();
						boolean acid = timeUntilAcidMature(reinforcement) != 0
								&& CitadelConfigManager.getAcidBlock() == block
										.getType();
						String groupName = "!NULL!";
						if (group != null) {
							groupName = group.getName();
						}
						sb.append(String.format("%s, %s, group: %s",
								reinforcementStatus, ageStatus, groupName));
						if (immature) {
							sb.append(" (Hardening)");
						}
						if (acid) {
							sb.append(" (Acid Maturing)");
						}
						if (reinforcement.isInsecure()) {
							sb.append(" (Insecure)");
						}
						Utility.sendAndLog(player, ChatColor.GREEN, sb.toString(), hoverMessage);
					} else {
						Utility.sendAndLog(player, ChatColor.RED,
								reinforcementStatus + ", " + ageStatus, hoverMessage);
					}
					if (player.getGameMode() == GameMode.CREATIVE) {
						pie.setCancelled(true);
					}
				}
				break;

			case INSECURE:
				// did player click on a reinforced block?
				pie.setCancelled(true);
				if (reinforcement != null) {
					if (reinforcement.canMakeInsecure(player)) {
						reinforcement.toggleInsecure();
						// Save the change
						/*Citadel.getReinforcementManager().saveReinforcement(reinforcement);*/
						if (reinforcement.isInsecure()) {
							Utility.sendAndLog(player, ChatColor.YELLOW,
									"Reinforcement now insecure");
						} else {
							Utility.sendAndLog(player, ChatColor.GREEN,
									"Reinforcement secured");
						}
					} else {
						Utility.sendAndLog(player, ChatColor.RED, "Access denied");
					}
				}
				break;

			case REINFORCEMENT:
				// player is in reinforcement mode
				if (reinforcement == null) {
					// set the reinforcemet material to what the player is
					// holding
					ItemStack stack = player.getInventory().getItemInMainHand();
					ReinforcementType type = ReinforcementType
							.getReinforcementType(stack);
					if (type == null) {
						Utility.sendAndLog(player, ChatColor.RED, stack.getType()
								.name() + " is not a reinforcable material.");
						Utility.sendAndLog(player, ChatColor.RED,
								"Left Reinforcement mode.");
						state.reset();
						return;
					}
					// Don't allow incorrect reinforcement with exclusive reinforcement types
					if (!type.canBeReinforced(block.getType())) {
						Utility.sendAndLog(player, ChatColor.RED, "That material cannot reinforce that type of block. Try a different reinforcement material.");
						Utility.sendAndLog(player, ChatColor.RED,"Left Reinforcement mode.");
						state.reset();
						return;
					}
					state.setFortificationItemStack(type.getItemStack());
					// Break any natural reinforcement before placing the player
					// reinforcement
					if (generic_reinforcement != null) {
						reinforcementBroken(null, generic_reinforcement);
					}
					// Don't allow double reinforcing reinforceable plants
					if (wouldPlantDoubleReinforce(block)) {
						Utility.sendAndLog(player, ChatColor.RED,
								"Cancelled reinforcement, crop would already be reinforced.");
					} else {
						if (createPlayerReinforcement(player, state.getGroup(), block, state.getReinforcementType(), null) == null &&
								CitadelConfigManager.shouldLogReinforcement()) {
							// someone else's job to tell the player what went wrong, but let's do log it.
							Citadel.getInstance().getLogger().log(Level.INFO, "Create Reinforcement by {0} at {1} cancelled by plugin",
									new Object[] {player.getName(), block.getLocation()});
						}
					}
				} else if (reinforcement.canBypass(player)
						|| (player.isOp() || player
								.hasPermission("citadel.admin"))) {
					String message = "";
					Group group = state.getGroup();
					Group old_group = reinforcement.getGroup();
					if (!old_group.getName().equals(group.getName())) {
						reinforcement.setGroup(group);
						ReinforcementCreationEvent event = new ReinforcementCreationEvent(
								reinforcement, block, player);
						Bukkit.getPluginManager().callEvent(event);
						if (!event.isCancelled()) {
							//rm.saveReinforcement(reinforcement);
							message = "Group has been changed to: "
									+ group.getName() + ".";
							Utility.sendAndLog(player, ChatColor.GREEN, message);
						} else {
							reinforcement.setGroup(old_group);
						}
					}
					ItemStack stack = player.getInventory().getItemInMainHand();
					ReinforcementType type = ReinforcementType.getReinforcementType(stack);
					if (type != null && !reinforcement.getStackRepresentation().isSimilar(type.getItemStack())) {
						//hit with different rein material, so switch material
						if (!type.canBeReinforced(block.getType())) {
							Utility.sendAndLog(player, ChatColor.RED, "That material cannot reinforce that type of block. Try a different reinforcement material.");
						}
						else {
							ReinforcementChangeTypeEvent e = new ReinforcementChangeTypeEvent(reinforcement, type, player);
							Bukkit.getPluginManager().callEvent(e);
							if (!e.isCancelled()) {
								reinforcementBroken(player, reinforcement);
								ReinforcementCreationEvent event = new ReinforcementCreationEvent(reinforcement, block, player);
								Bukkit.getPluginManager().callEvent(event);
								if (!event.isCancelled()) {
									if(createPlayerReinforcement(player, state.getGroup(),	block, type, null) != null) {
										Utility.sendAndLog(player, ChatColor.GREEN, "Changed reinforcement type");
									} else if (CitadelConfigManager.shouldLogReinforcement()) {
										Citadel.getInstance().getLogger().log(Level.INFO, "Change Reinforcement by {0} at {1} cancelled by plugin",
												new Object[] {player.getName(), block.getLocation()});
									}
								}
							}
						}
					}
				} else {
					Utility.sendAndLog(player, ChatColor.RED,
							"You are not permitted to modify this reinforcement");
				}
				pie.setCancelled(true);
				state.checkResetMode();
			default:
				break;
			}

		} catch (Exception e) {
			Citadel.getInstance()
					.getLogger()
					.log(Level.WARNING,
							"General Exception during player interaction", e);
		}
	}

	// TODO: Come back and figure out why this is causing all the data to be
	// re-written inplace with no change
	/*
	 * @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	 * public void chunkLoadEvent(ChunkLoadEvent event) { Chunk chunk =
	 * event.getChunk(); rm.loadReinforcementChunk(chunk); }
	 */

	@EventHandler(priority = EventPriority.HIGHEST)
	public void blockPhysEvent(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		if (block.getType().hasGravity()) {
			Reinforcement rein = rm.getReinforcement(Utility
					.getRealBlock(block));
			if (rein != null) {
				event.setCancelled(true);
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void liquidDumpEvent(PlayerBucketEmptyEvent event) {
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		if (block.getType().equals(Material.AIR) || block.getType().isSolid())
			return;

		Reinforcement rein = rm.getReinforcement(Utility.getRealBlock(block));
		if (rein != null) {
			event.setCancelled(true);
		}
	}
}
