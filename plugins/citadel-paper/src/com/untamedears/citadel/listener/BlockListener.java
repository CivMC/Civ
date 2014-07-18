package com.untamedears.citadel.listener;

import static com.untamedears.citadel.Utility.createNaturalReinforcement;
import static com.untamedears.citadel.Utility.createPlayerReinforcement;
import static com.untamedears.citadel.Utility.isAuthorizedPlayerNear;
import static com.untamedears.citadel.Utility.isPlant;
import static com.untamedears.citadel.Utility.isRail;
import static com.untamedears.citadel.Utility.maybeReinforcementDamaged;
import static com.untamedears.citadel.Utility.reinforcementBroken;
import static com.untamedears.citadel.Utility.reinforcementDamaged;
import static com.untamedears.citadel.Utility.sendMessage;
import static com.untamedears.citadel.Utility.sendThrottledMessage;
import static com.untamedears.citadel.Utility.wouldPlantDoubleReinforce;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import org.bukkit.material.PistonBaseMaterial;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.Citadel.VerboseMsg;
import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.access.CropAccessDelegate;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.PlayerState;
import com.untamedears.citadel.entity.ReinforcementMaterial;
import com.untamedears.citadel.events.PlayerDamageReinforcementEvent;

public class BlockListener implements Listener {

    public static final List<BlockFace> all_sides = Arrays.asList(
        BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
        BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

    public static final List<BlockFace> planar_sides = Arrays.asList(
        BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST);

    private boolean canPlace(Block block, Player player) {
        Material block_mat = block.getType();
        // See if block is a sign over a chest indicating it's a shop.
        // Check to see if we need to worry about physical shop interaction.
        if (block_mat == Material.WALL_SIGN
                && Bukkit.getPluginManager().isPluginEnabled("PhysicalShop")) {
            Block below = block.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.CHEST || block_mat == Material.TRAPPED_CHEST) {
                IReinforcement rein = AccessDelegate.getDelegate(below).getReinforcement();
                if (null != rein
                        && rein instanceof PlayerReinforcement
                        && !((PlayerReinforcement)rein).isAccessible(player)) {
                    // Don't allow another player to access the chest by creating a shop
                    return false;
                }
            }
        }
        if (block_mat == Material.HOPPER || block_mat == Material.DROPPER){
            for (BlockFace direction : all_sides) {
                Block adjacent = block.getRelative(direction);
                if (!(adjacent.getState() instanceof ContainerBlock)) {
                    continue;
                }
                IReinforcement rein = AccessDelegate.getDelegate(adjacent).getReinforcement();
                if (null != rein && rein instanceof PlayerReinforcement) {
                    PlayerReinforcement pr = (PlayerReinforcement)rein;
                    if (!pr.isInsecure() && !pr.isAccessible(player)) {
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
                IReinforcement rein = AccessDelegate.getDelegate(adjacent).getReinforcement();
                if (null != rein && rein instanceof PlayerReinforcement) {
                    PlayerReinforcement pr = (PlayerReinforcement)rein;
                    if (!pr.isAccessible(player)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * This handles the BlockPlaceEvent for Fortification mode (all placed blocks are reinforced)
     *
     * @param bpe BlockPlaceEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void placeFortifiedBlock(BlockPlaceEvent bpe) {
        Block block = bpe.getBlockPlaced();
        if (bpe.getBlockReplacedState().getType().equals(Material.AIR)) {
            IReinforcement existingReinforcement = Citadel.getReinforcementManager().getReinforcement(block);
            if (existingReinforcement != null && existingReinforcement instanceof PlayerReinforcement) {
                Citadel.getReinforcementManager().removeReinforcement(existingReinforcement);
            }
        }
        Player player = bpe.getPlayer();
        if (!canPlace(block, player)) {
            sendThrottledMessage(player, ChatColor.RED, "Cancelled block place, mismatched reinforcement.");
            bpe.setCancelled(true);
            return;
        }
        PlayerState state = PlayerState.get(player);
        Faction group = state.getFaction();
        if (group != null && group.isDisciplined()) {
            sendThrottledMessage(player, ChatColor.RED, Faction.kDisciplineMsg);
            bpe.setCancelled(true);
            return;
        }
        if (state.getMode() != PlacementMode.FORTIFICATION) {
            // if we are not in fortification mode
            // cancel event if we are not in normal mode
            if (state.getMode() == PlacementMode.REINFORCEMENT || state.getMode() == PlacementMode.REINFORCEMENT_SINGLE_BLOCK)
                bpe.setCancelled(true);
            return;
        }
        // Don't allow double reinforcing reinforceable plants
        if (wouldPlantDoubleReinforce(block)) {
            sendThrottledMessage(player, ChatColor.RED, "Cancelled block place, crop would already be reinforced.");
            bpe.setCancelled(true);
            return;
        }
        PlayerInventory inventory = player.getInventory();
        ReinforcementMaterial material = state.getReinforcementMaterial();
        ItemStack required = material.getRequiredMaterials();
        if (inventory.contains(material.getMaterial(), required.getAmount())) {
            if (createPlayerReinforcement(player, block) == null) {
                sendMessage(player, ChatColor.RED, "%s is not a reinforcible material", block.getType().name());
            } else {
                state.checkResetMode();
            }
        } else {
            sendMessage(player, ChatColor.YELLOW, "%s depleted, left fortification mode", material.getMaterial().name());
            state.reset();
            bpe.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void blockBreak(BlockBreakEvent bbe) {
        Block block = bbe.getBlock();
        Player player = bbe.getPlayer();

        AccessDelegate delegate = AccessDelegate.getDelegate(block);
        IReinforcement reinforcement = delegate.getReinforcement();
        if (reinforcement == null) {
            reinforcement = createNaturalReinforcement(block, player);
            if (reinforcement != null) {
            	PlayerDamageReinforcementEvent dre = new PlayerDamageReinforcementEvent(reinforcement, block, player);
            	
            	Bukkit.getPluginManager().callEvent(dre);
            	
            	if(dre.isCancelled()) {
            		bbe.setCancelled(true);
            		block.getDrops().clear();
            		
            		return;
            	}

            	if(reinforcementDamaged(reinforcement)) {
            		bbe.setCancelled(true);
            		block.getDrops().clear();
            	}
            }
            return;
        }

        boolean is_cancelled = true;
        if (reinforcement instanceof PlayerReinforcement) {
            PlayerReinforcement pr = (PlayerReinforcement)reinforcement;
            PlayerState state = PlayerState.get(player);
            boolean admin_bypass = player.hasPermission("citadel.admin.bypassmode");
            if ((delegate instanceof CropAccessDelegate)
                    && (pr.isAccessible(player) || admin_bypass)) {
                // If this is a delegated reinforcement for a crop which the
                //  player has access to, allow the player to break the crop
                //  without effecting the reinforcement.
                is_cancelled = false;
            } else if (state.isBypassMode() && (pr.isBypassable(player) || admin_bypass)) {
                if (admin_bypass) {
                    Citadel.verbose(
                        VerboseMsg.AdminReinBypass,
                        player.getDisplayName(), pr.getBlock().getLocation().toString());
                } else {
                    Citadel.verbose(
                        VerboseMsg.ReinBypass,
                        player.getDisplayName(), pr.getBlock().getLocation().toString());
                }
                is_cancelled = reinforcementBroken(reinforcement);
            } else {
            	PlayerDamageReinforcementEvent dre = new PlayerDamageReinforcementEvent(reinforcement, block, player);
            	
            	Bukkit.getPluginManager().callEvent(dre);
            	
            	if(dre.isCancelled()) {
            		is_cancelled = true;
            	}
            	else {
            		is_cancelled = reinforcementDamaged(reinforcement);
            	}
            }
            if (!is_cancelled) {
                // The player reinforcement broke. Now check for natural
                is_cancelled = createNaturalReinforcement(block, player) != null;
            }
        } else {
        	PlayerDamageReinforcementEvent dre = new PlayerDamageReinforcementEvent(reinforcement, block, player);
        	
        	Bukkit.getPluginManager().callEvent(dre);
        	
        	if(dre.isCancelled()) {
        		is_cancelled = reinforcementDamaged(reinforcement);
        		return;
        	}
        	else {
        		is_cancelled = reinforcementDamaged(reinforcement);
        	}
        }

        if (is_cancelled) {
            bbe.setCancelled(true);
            block.getDrops().clear();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void pistonExtend(BlockPistonExtendEvent bpee) {
        Block piston = bpee.getBlock();
        BlockState state = piston.getState();
        MaterialData data = state.getData();
        BlockFace direction = null;

        if (data instanceof PistonBaseMaterial) {
            direction = ((PistonBaseMaterial) data).getFacing();
        }

        // if no direction was found, no point in going on
        if (direction == null)
            return;

        // Check the affected blocks
        for (int i = 1; i < bpee.getLength() + 2; i++) {
            Block block = piston.getRelative(direction, i);

            if (block.getType() == Material.AIR){
                break;
            }

            AccessDelegate delegate = AccessDelegate.getDelegate(block);
            IReinforcement reinforcement = delegate.getReinforcement();

            if (reinforcement != null){
                bpee.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void pistonRetract(BlockPistonRetractEvent bpre) {
        Block piston = bpre.getBlock();
        BlockState state = piston.getState();
        MaterialData data = state.getData();
        BlockFace direction = null;

        // Check the block it pushed directly
        if (data instanceof PistonBaseMaterial) {
            direction = ((PistonBaseMaterial) data).getFacing();
        }

        if (direction == null)
            return;

        // the block that the piston moved
        Block moved = piston.getRelative(direction, 2);

        AccessDelegate delegate = AccessDelegate.getDelegate(moved);
        IReinforcement reinforcement = delegate.getReinforcement();

        if (reinforcement != null) {
            bpre.setCancelled(true);
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
        Block to_block = event.getToBlock();
        if (!isRail(to_block) && !isPlant(to_block)) {
            return;
        }
        AccessDelegate delegate = AccessDelegate.getDelegate(to_block);
        if (delegate.isReinforced()) {
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
            IReinforcement generic_reinforcement =
                Citadel.getReinforcementManager().getReinforcement(block);
            if (generic_reinforcement == null ||
                !(generic_reinforcement instanceof PlayerReinforcement)) {
                return;
            }
            PlayerReinforcement reinforcement =
                (PlayerReinforcement)generic_reinforcement;
            if (reinforcement.getSecurityLevel() == SecurityLevel.PUBLIC) {
                return;
            }
            double redstoneDistance = Citadel.getConfigManager().getRedstoneDistance();
            if (!isAuthorizedPlayerNear(reinforcement, redstoneDistance)) {
                Citadel.verbose(
                    VerboseMsg.RedstoneOpen, 
                    reinforcement.getBlock().getLocation().toString());
                bre.setNewCurrent(bre.getOldCurrent());
            }
        } catch(Exception e) {
            Citadel.printStackTrace(e);
        }
    }
}
