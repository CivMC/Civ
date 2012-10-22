package com.untamedears.citadel.listener;

import static com.untamedears.citadel.Utility.createNaturalReinforcement;
import static com.untamedears.citadel.Utility.createPlayerReinforcement;
import static com.untamedears.citadel.Utility.maybeReinforcementDamaged;
import static com.untamedears.citadel.Utility.reinforcementBroken;
import static com.untamedears.citadel.Utility.reinforcementDamaged;
import static com.untamedears.citadel.Utility.sendMessage;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.Effect;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.PlayerState;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.NaturalReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.ReinforcementMaterial;

public class BlockListener implements Listener {

    /**
     * This handles the BlockPlaceEvent for Fortification mode (all placed blocks are reinforced)
     *
     * @param bpe BlockPlaceEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void placeFortifiedBlock(BlockPlaceEvent bpe) {
        Player player = bpe.getPlayer();
        PlayerState state = PlayerState.get(player);

        if (state.getMode() != PlacementMode.FORTIFICATION) {
            // if we are not in fortification mode
            // cancel event if we are not in normal mode
            if (state.getMode() == PlacementMode.REINFORCEMENT || state.getMode() == PlacementMode.REINFORCEMENT_SINGLE_BLOCK)
                bpe.setCancelled(true);
            return;
        }

        Block block = bpe.getBlockPlaced();
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
            reinforcement = createNaturalReinforcement(block);
            if (reinforcement != null && reinforcementDamaged(reinforcement)) {
                bbe.setCancelled(true);
                block.getDrops().clear();
            }
	        return;
	    }

        if (reinforcement instanceof PlayerReinforcement) {
            PlayerReinforcement pr = (PlayerReinforcement)reinforcement;
            PlayerState state = PlayerState.get(player);
            if (state.isBypassMode() && pr.isBypassable(player)) {
		    	Citadel.info(player.getDisplayName() + " bypassed reinforcement %s at " 
		    			+ pr.getBlock().getLocation().toString());
                bbe.setCancelled(reinforcementBroken(reinforcement));
            } else {
                bbe.setCancelled(reinforcementDamaged(reinforcement));
            }
        } else {
            bbe.setCancelled(reinforcementDamaged(reinforcement));
        }
        if (bbe.isCancelled()) {
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
    public void blockPhysics(BlockPhysicsEvent bpe) {
       Material changedType = bpe.getChangedType();
       if (Material.LAVA == changedType) {
           Block block = bpe.getBlock();
           // Protection for reinforced rails types from lava. Similar to water, transform surrounding blocks in cobblestone to stop the lava effect.
           if (Material.RAILS == block.getType() || Material.POWERED_RAIL == block.getType() || Material.DETECTOR_RAIL == block.getType()) {
               boolean isReinforced = maybeReinforcementDamaged(block);
               if (isReinforced) {
                   for (BlockFace blockFace : new BlockFace[]{BlockFace.DOWN, BlockFace.UP, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH}) {
                       Block otherBlock = block.getRelative(blockFace);
                       if (Material.LAVA == otherBlock.getType()) {
                           otherBlock.setType(Material.COBBLESTONE);
                           otherBlock.getWorld().playEffect(otherBlock.getLocation(), Effect.EXTINGUISH, 0);
                       }
                   }
               }
           }
       }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void redstonePower(BlockRedstoneEvent bre) {
    	try {
        Block block = bre.getBlock();
        
        if (!(block.getState().getData() instanceof Openable)) return;
        
        Openable openable = (Openable) block.getState().getData();
        if (openable.isOpen()) return;
        
        IReinforcement generic_reinforcement = Citadel.getReinforcementManager().getReinforcement(block);
        if (generic_reinforcement == null ||
                !(generic_reinforcement instanceof PlayerReinforcement))
            return;

        PlayerReinforcement reinforcement = (PlayerReinforcement)generic_reinforcement;
        if (reinforcement.getSecurityLevel() == SecurityLevel.PUBLIC)
            return;

        //Set<Player> onlinePlayers = new HashSet<Player>(Citadel.getMemberManager().getOnlinePlayers());
        Player[] onlinePlayers = Citadel.getPlugin().getServer().getOnlinePlayers();
		boolean isAuthorizedPlayerNear = false;
		try {
			double redstoneDistance = Citadel.getConfigManager().getRedstoneDistance();
			for(Player player : onlinePlayers){
				if(reinforcement.isAccessible(player)){
					Location playerLocation = player.getLocation();
					Location blockLocation = block.getLocation();
					if(playerLocation.getWorld() == blockLocation.getWorld()){
						double distanceSquared = playerLocation.distance(blockLocation);
						if(distanceSquared <= redstoneDistance){
							isAuthorizedPlayerNear = true;
							break;
						}
					}
				}
			}
		} catch (ConcurrentModificationException e){
			Citadel.warning("ConcurrentModificationException at redstonePower() in BlockListener");
		}

        if (!isAuthorizedPlayerNear) {
			Citadel.info("Prevented redstone from opening reinforcement %s at " 
					+ reinforcement.getBlock().getLocation().toString());
            bre.setNewCurrent(bre.getOldCurrent());
        }

        }
        catch(Exception e)
        {
          Citadel.printStackTrace(e);
        }
    }
}
