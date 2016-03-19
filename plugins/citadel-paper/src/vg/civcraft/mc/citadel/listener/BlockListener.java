package vg.civcraft.mc.citadel.listener;

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
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
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
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import org.bukkit.material.PistonBaseMaterial;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.CitadelConfigManager;
import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.ReinforcementMode;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;
import vg.civcraft.mc.citadel.misc.ReinforcemnetFortificationCancelException;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.group.GroupType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class BlockListener implements Listener{
	
	public static final List<BlockFace> all_sides = Arrays.asList(
	        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
	        BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);
	
	public static final List<BlockFace> planar_sides = Arrays.asList(
	        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);
	
	private ReinforcementManager rm = Citadel.getReinforcementManager();
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onFortificationMode(BlockPlaceEvent event){
		Player p = event.getPlayer();
		Block b = event.getBlock();
		Inventory inv = p.getInventory();
		if (Material.AIR.equals(event.getBlockReplacedState().getType())) {
			Reinforcement rein = rm.getReinforcement(b.getLocation());
			if (rein != null && rein instanceof PlayerReinforcement){
				rm.deleteReinforcement(rein);
			}
			Location loc = event.getBlockPlaced().getLocation();
			ItemStack stack = event.getItemInHand();
			rein = isDroppedReinforcementBlock(p, stack, loc);
			if (rein != null){
				rm.saveInitialReinforcement(rein);
				return;
			}
		}
		PlayerState state = PlayerState.get(p);
		if (state.getMode() != ReinforcementMode.REINFORCEMENT_FORTIFICATION) {
			return;
		}
		if (!canPlace(b, p)){
			sendAndLog(p, ChatColor.RED, "Cancelled block place, mismatched reinforcement.");
			event.setCancelled(true);
			return;
		}
		ReinforcementType type = state.getReinforcementType();
		 // Don't allow double reinforcing reinforceable plants
        if (wouldPlantDoubleReinforce(b)) {
        	sendAndLog(p, ChatColor.RED, "Cancelled block place, crop would already be reinforced.");
            event.setCancelled(true);
            return;
        }
        int required = type.getRequiredAmount();
        if (type.getItemStack().isSimilar(event.getItemInHand())){
        	required++;
        }
		if (inv.containsAtLeast(type.getItemStack(), required)) {
			try {
				if (createPlayerReinforcement(p, state.getGroup(), b, type, event.getItemInHand()) == null) {
					sendAndLog(p, ChatColor.RED, String.format("%s is not a reinforcible material ", b.getType().name()));
				} else {
					state.checkResetMode();
				}	
			} catch(ReinforcemnetFortificationCancelException ex){
				Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementFortificationCancelException occured in BlockListener, BlockPlaceEvent ", ex);
			}
        } else {
        	sendAndLog(p, ChatColor.YELLOW, String.format("%s depleted, left fortification mode ",  
            		state.getReinforcementType().getMaterial().name()));
            state.reset();
            event.setCancelled(true);
        }
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void blockBreakEvent(BlockBreakEvent event){
		Block block = event.getBlock();
		Player player = event.getPlayer();	
		Block reinforcingBlock = null;
		Reinforcement rein = rm.getReinforcement(Utility.getRealBlock(block));
		
		//if block is a plant check reinforcement on soil block
		if(isPlant(block)){
        	reinforcingBlock = Utility.findPlantSoil(block);
        	Reinforcement plant = null;
        	if (reinforcingBlock != null)
        		plant = rm.getReinforcement(reinforcingBlock);
        	if (plant != null)
        		rein = plant;        		
		}

		if (rein == null){
			rein = createNaturalReinforcement(event.getBlock(), player);
			if (rein != null){
				ReinforcementDamageEvent e = new ReinforcementDamageEvent(rein, player, block);
				Bukkit.getPluginManager().callEvent(e);
				if (e.isCancelled()){
					event.setCancelled(true);
					return;
				}
				if(reinforcementDamaged(player, rein)) {
            		event.setCancelled(true);
            	}
            }
            return;
		}
		
		boolean is_cancelled = true;
        if (rein instanceof PlayerReinforcement) {
            PlayerReinforcement pr = (PlayerReinforcement) rein;
            PlayerState state = PlayerState.get(player);
            boolean admin_bypass = player.hasPermission("citadel.admin.bypassmode");   
            if (reinforcingBlock != null && isPlant(block) && (pr.isAccessible(player, PermissionType.CROPS) || admin_bypass)) {
                //player has CROPS access to the soil block, allow them to break without affecting reinforcement
            	is_cancelled = false;
            } else if (state.isBypassMode() && (pr.isBypassable(player) || admin_bypass) && !pr.getGroup().isDisciplined()) {
                if (admin_bypass) {
                	/*
                    Citadel.verbose(
                        VerboseMsg.AdminReinBypass,
                        player.getDisplayName(), pr.getBlock().getLocation().toString());
                        */
                } else {
                	/*
                    Citadel.verbose(
                        VerboseMsg.ReinBypass,
                        player.getDisplayName(), pr.getBlock().getLocation().toString());
                	*/
                }
                is_cancelled = reinforcementBroken(player, rein);
            } else {
            	
            	ReinforcementDamageEvent dre = new ReinforcementDamageEvent(rein, player, block);
            	
            	Bukkit.getPluginManager().callEvent(dre);
            	
            	if(dre.isCancelled()) {
            		is_cancelled = true;
            	}
            	else {
            		is_cancelled = reinforcementDamaged(player, rein);
            	}
            }
            if (!is_cancelled) {
                // The player reinforcement broke. Now check for natural
                is_cancelled = createNaturalReinforcement(block, player) != null;
            }
        } else {
        	ReinforcementDamageEvent dre = new ReinforcementDamageEvent(rein, player, block);
        	
        	Bukkit.getPluginManager().callEvent(dre);
        	
        	if(dre.isCancelled()) {
        		is_cancelled = reinforcementDamaged(player, rein);
        		return;
        	}
        	else {
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
        for (Block block : bpee.getBlocks())
        {
        	Reinforcement reinforcement = rm.getReinforcement(block.getLocation());
            if (reinforcement != null){
            	bpee.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void pistonRetract(BlockPistonRetractEvent bpre) {
        for (Block block : bpre.getBlocks())
        {
        	Reinforcement reinforcement = rm.getReinforcement(block.getLocation());
            if (reinforcement != null){
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
            if (block.getRelative(0,1,0).getType() == matfire) {block.getRelative(0,1,0).setTypeId(0);} // Essential
            // Extended fire protection (recommend)
            if (block.getRelative(1,0,0).getType() == matfire) {block.getRelative(1,0,0).setTypeId(0);}
            if (block.getRelative(-1,0,0).getType() == matfire) {block.getRelative(-1,0,0).setTypeId(0);}
            if (block.getRelative(0,-1,0).getType() == matfire) {block.getRelative(0,-1,0).setTypeId(0);}
            if (block.getRelative(0,0,1).getType() == matfire) {block.getRelative(0,0,1).setTypeId(0);}
            if (block.getRelative(0,0,-1).getType() == matfire) {block.getRelative(0,0,-1).setTypeId(0);}
            // Aggressive fire protection (would seriously reduce effectiveness of flint down to near the "you'd have to use it 25 times" mentality)
            /*
            if (block.getRelative(1,1,0).getType() == matfire) {block.getRelative(1,1,0).setTypeId(0);}
            if (block.getRelative(1,-1,0).getType() == matfire) {block.getRelative(1,-1,0).setTypeId(0);}
            if (block.getRelative(-1,1,0).getType() == matfire) {block.getRelative(-1,1,0).setTypeId(0);}
            if (block.getRelative(-1,-1,0).getType() == matfire) {block.getRelative(-1,-1,0).setTypeId(0);}
            if (block.getRelative(0,1,1).getType() == matfire) {block.getRelative(0,1,1).setTypeId(0);}
            if (block.getRelative(0,-1,1).getType() == matfire) {block.getRelative(0,-1,1).setTypeId(0);}
            if (block.getRelative(0,1,-1).getType() == matfire) {block.getRelative(0,1,-1).setTypeId(0);}
            if (block.getRelative(0,-1,-1).getType() == matfire) {block.getRelative(0,-1,-1).setTypeId(0);}
            */
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onBlockFromToEvent(BlockFromToEvent event) {
        Block reinforcementBlock = event.getToBlock();
        if(isPlant(reinforcementBlock)){
        	//block to is as plant check block under it for reinforcement
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
        //  like doors, from being opened by unauthorizied players.
        try {
            // NewCurrent <= 0 means the redstone wire is turning off, so the
            //  container is closing. Closing is good so just return. This also
            //  shaves off some time when dealing with sand generators.
            // OldCurrent > 0 means that the wire was already on, thus the
            //  container was already open by an authorized player. Now it's
            //  either staying open or closing. Just return.
            if (bre.getNewCurrent() <= 0 || bre.getOldCurrent() > 0) {
                return;
            }
            Block block = bre.getBlock();
            MaterialData blockData = block.getState().getData();
            if (!(blockData instanceof Openable)) {
                return;
            }
            Openable openable = (Openable)blockData;
            if (openable.isOpen()) {
                return;
            }
            Reinforcement generic_reinforcement =
                Citadel.getReinforcementManager().getReinforcement(block);
            if (generic_reinforcement == null ||
                !(generic_reinforcement instanceof PlayerReinforcement)) {
                return;
            }
            PlayerReinforcement reinforcement =
                (PlayerReinforcement)generic_reinforcement;
            Group group = reinforcement.getGroup();
            if (group.getType() == GroupType.PUBLIC) {
                return;
            }
            double redstoneDistance = CitadelConfigManager.getMaxRedstoneDistance();
            if (!isAuthorizedPlayerNear(reinforcement, redstoneDistance)) {
               // Citadel.Log( 
                //    reinforcement.getLocation().toString());
                bre.setNewCurrent(bre.getOldCurrent());
            }
        } catch(Exception e) {
        	Citadel.getInstance().getLogger().log(Level.WARNING, "Exception occured in BlockListener, BlockRedstoneEvent ", e);
        }
    }
	
	private boolean canPlace(Block block, Player player) {
        Material block_mat = block.getType();
        
        if (block_mat == Material.HOPPER || block_mat == Material.DROPPER){
            for (BlockFace direction : all_sides) {
                Block adjacent = block.getRelative(direction);
                if (!(adjacent.getState() instanceof ContainerBlock)) {
                    continue;
                }
                Reinforcement rein = rm.getReinforcement(adjacent);
                if (null != rein && rein instanceof PlayerReinforcement) {
                    PlayerReinforcement pr = (PlayerReinforcement)rein;
                    if (pr.isInsecure() && !pr.isAccessible(player, PermissionType.CHESTS)) {
                        return false;
                    }
                }
            }
        }
        if (block_mat == Material.CHEST || block_mat == Material.TRAPPED_CHEST){
            for (BlockFace direction : planar_sides) {
                Block adjacent = block.getRelative(direction);
                if (!(adjacent.getState() instanceof ContainerBlock)) {
                    continue;
                }
                Reinforcement rein = rm.getReinforcement(adjacent);
                if (null != rein && rein instanceof PlayerReinforcement) {
                    PlayerReinforcement pr = (PlayerReinforcement)rein;
                    if (!pr.isAccessible(player, PermissionType.CHESTS)) {
                        return false;
                    }
                }
            }
        }
        //stops players from modifying the reinforcement on a half slab by placing another block on top
        Reinforcement reinforcement_on_block = Citadel.getReinforcementManager().getReinforcement(block);
        if (reinforcement_on_block instanceof PlayerReinforcement) {
            PlayerReinforcement reinforcement = (PlayerReinforcement) reinforcement_on_block;
            if (!reinforcement.isBypassable(player)) {
                return false;
            }
        } else if (reinforcement_on_block != null) {
            return false; //not really sure when this could happen but just in case
        }

        return true;
    }
	
    @EventHandler(priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent pie) {
        try {
	        if (!pie.hasBlock()) return;
	
	        Player player = pie.getPlayer();
	        Block block = Utility.getRealBlock(pie.getClickedBlock());
	        Reinforcement generic_reinforcement = rm.getReinforcement(block);
	        PlayerReinforcement reinforcement = null;
	        if (generic_reinforcement instanceof PlayerReinforcement) {
	            reinforcement = (PlayerReinforcement)generic_reinforcement;
	        }
	
	        Action action = pie.getAction();
	        boolean access_reinforcement = 
	            action == Action.RIGHT_CLICK_BLOCK
	            && reinforcement != null
	            && reinforcement.isSecurable();
	        boolean normal_access_denied =
	            reinforcement != null
	            && !(reinforcement.isAccessible(player, PermissionType.CHESTS, PermissionType.DOORS));
	        boolean admin_can_access = player.hasPermission("citadel.admin");
	        if (access_reinforcement && normal_access_denied && !admin_can_access) {
	            /*Citadel.verbose(
	                VerboseMsg.ReinLocked,
	                player.getDisplayName(), block.getLocation().toString());
	            */
	            pie.getPlayer().sendMessage(ChatColor.RED + String.format("%s is locked", block.getType().name()));
	            pie.setCancelled(true);
	        }
	        // Not really sure what this is for.  Should come up in testing.
	        /*else if (action == Action.PHYSICAL) {
	            AccessDelegate aboveDelegate = AccessDelegate.getDelegate(block.getRelative(BlockFace.UP));
	            if (aboveDelegate instanceof CropAccessDelegate
	                    && aboveDelegate.isReinforced()) {
	                Citadel.verbose(
	                    VerboseMsg.CropTrample,
	                    block.getLocation().toString());
	                pie.setCancelled(true);
	            }
	        }
	        */
	        if (pie.isCancelled()) return;
	
	        PlayerState state = PlayerState.get(player);
	        ReinforcementMode placementMode = state.getMode();
	        switch (placementMode) {
	            case NORMAL:
	                if (access_reinforcement && normal_access_denied && admin_can_access) {
	                    /*
	                	Citadel.verbose(
	                        VerboseMsg.AdminReinLocked,
	                        player.getDisplayName(), block.getLocation().toString());
	                        */
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
	                    if (player.hasPermission("citadel.admin.ctinfodetails")) {
	                        sendAndLog(player, ChatColor.GREEN, String.format(
	                            "Loc[%s]", reinforcement.getLocation().toString()));
	                        String groupName = "!NULL!";
	                        if (group != null) {
	                        	groupName = String.format("[%s] (%s)", group.getName(),
	                        			group.getType().name());
	                        }
	                        sb = new StringBuilder();
	                        sb.append(String.format(" Group%s Durability[%d/%d]",
	                            groupName,
	                            reinforcement.getDurability(),
	                            ReinforcementType.getReinforcementType
	                            (reinforcement.getStackRepresentation()).getHitPoints()));
	                        int maturationTime = timeUntilMature(reinforcement);
	                        if (maturationTime != 0) {
	                            sb.append(" Immature[");
	                            sb.append(maturationTime);
	                            sb.append("]");
	                        }
							int acidTime = timeUntilAcidMature(reinforcement);
							if (CitadelConfigManager.getAcidBlock() == block.getType()) {
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
	                        
	                        sendAndLog(player, ChatColor.GREEN, sb.toString());
	                    } else if(reinforcement.isAccessible(player, PermissionType.BLOCKS, PermissionType.DOORS, PermissionType.CHESTS)){
	                        sb = new StringBuilder();
	                        boolean immature = timeUntilMature(reinforcement) != 0
	                            && CitadelConfigManager.isMaturationEnabled();
							boolean acid = timeUntilAcidMature(reinforcement) != 0 
								&& CitadelConfigManager.getAcidBlock() == block.getType();
	                        String groupName = "!NULL!";
	                        if (group != null) {
	                            groupName = group.getName();
	                        }
	                        sb.append(String.format("%s, %s, group: %s",
	                            reinforcementStatus, ageStatus, groupName));
	                        if(immature){
	                            sb.append(" (Hardening)");
	                        }
							if(acid){
								sb.append(" (Acid Maturing)");
							}
	                        if (reinforcement.isInsecure()) {
	                            sb.append(" (Insecure)");
	                        }
	                        sendAndLog(player, ChatColor.GREEN, sb.toString());
	                    } else {
	                    	sendAndLog(player, ChatColor.RED, reinforcementStatus + ", " + ageStatus);
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
	                    if (reinforcement.isBypassable(player)) {
	                        reinforcement.toggleInsecure();
	                        // Save the change
	                        Citadel.getReinforcementManager().saveReinforcement(reinforcement);
	                        if (reinforcement.isInsecure()) {
	                        	sendAndLog(player, ChatColor.YELLOW, "Reinforcement now insecure");
	                        } else {
	                        	sendAndLog(player, ChatColor.GREEN, "Reinforcement secured");
	                        }
	                    } else {
	                    	sendAndLog(player, ChatColor.RED, "Access denied");
	                    }
	                }
	                break;
	
	            case REINFORCEMENT:
	                // player is in reinforcement mode
	                if (reinforcement == null) {
	                	// set the reinforcemet material to what the player is holding
	                	ItemStack stack = player.getItemInHand();
	                	ReinforcementType type = ReinforcementType.getReinforcementType(stack);
	                	if (type == null){
	                		sendAndLog(player, ChatColor.RED, stack.getType().name() + " is not a reinforcable material.");
	                		sendAndLog(player, ChatColor.RED, "Left Reinforcement mode.");
	                		state.reset();
	                		return;
	                	}
	                	state.setFortificationItemStack(type.getItemStack());
	                    // Break any natural reinforcement before placing the player reinforcement
	                    if (generic_reinforcement != null) {
	                        reinforcementBroken(null, generic_reinforcement);
	                    }
	                    // Don't allow double reinforcing reinforceable plants
	                    if (wouldPlantDoubleReinforce(block)) {
	                    	sendAndLog(player, ChatColor.RED, "Cancelled reinforcement, crop would already be reinforced.");
	                    } else {
	                    	try {
	                        createPlayerReinforcement(player, state.getGroup(),
	                        		block, state.getReinforcementType(), null);
	                    	} catch(ReinforcemnetFortificationCancelException e) {
	                    		Citadel.getInstance().getLogger().log(Level.WARNING, "ReinforcementFortificationCancelException occured in BlockListener, PlayerInteractEvent ",e);
	                    	}
	                    }
	                } else if (reinforcement.isBypassable(player) || (player.isOp() || player.hasPermission("citadel.admin"))) {
	                    String message = "";
	                    Group group = state.getGroup();
	                    Group old_group = reinforcement.getGroup();
	                    if(!old_group.getName().equals(group.getName())) {
	                        reinforcement.setGroup(group);
	                    	ReinforcementCreationEvent event = new ReinforcementCreationEvent(reinforcement, block, player);
	                        Bukkit.getPluginManager().callEvent(event);
	                        if (!event.isCancelled()) {
	                            rm.saveReinforcement(reinforcement);
	                            message = "Group has been changed to: " + group.getName() + ".";
	                            sendAndLog(player, ChatColor.GREEN, message);
	                        }
	                        else
	                            reinforcement.setGroup(old_group);
	                    }
	                } else {
	                	sendAndLog(player, ChatColor.RED, "You are not permitted to modify this reinforcement");
	                }
	                pie.setCancelled(true);
	                state.checkResetMode();
	            default:
	            	break;
	        }

        } catch(Exception e) {
        	Citadel.getInstance().getLogger().log(Level.WARNING, "General Exception during player interaction", e);
        }
    }
    
// TODO: Come back and figure out why this is causing all the data to be re-written inplace with no change
/*    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void chunkLoadEvent(ChunkLoadEvent event) {
    	Chunk chunk = event.getChunk();
    	rm.loadReinforcementChunk(chunk);
    }*/
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void blockPhysEvent(BlockPhysicsEvent event){
    	Block block = event.getBlock();
    	if (block.getType().hasGravity()){
    		Reinforcement rein = rm.getReinforcement(Utility.getRealBlock(block));
    		if (rein != null){
    			event.setCancelled(true);
    		}
    	}
    	
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void liquidDumpEvent(PlayerBucketEmptyEvent event){
    	Block block = event.getBlockClicked().getRelative(event.getBlockFace());
    	if (block.getType().equals(Material.AIR) || block.getType().isSolid())
    		return;
    	
    	Reinforcement rein = rm.getReinforcement(Utility.getRealBlock(block));
    	if (rein != null){
    		event.setCancelled(true);
    	}
    }

	protected void sendAndLog(Player receiver, ChatColor color, String message) {
		receiver.sendMessage(color + message);
		if (CitadelConfigManager.shouldLogPlayerCommands()) {
			Citadel.getInstance().getLogger().log(Level.INFO, "Sent {0} reply {1}", new Object[]{receiver.getName(), message});
		}
	}
}
