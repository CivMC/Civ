package vg.civcraft.mc.citadel.listener;

import static vg.civcraft.mc.citadel.Utility.explodeReinforcement;
import static vg.civcraft.mc.citadel.Utility.maybeReinforcementDamaged;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.PlayerState;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.ReinforcementMode;
import vg.civcraft.mc.citadel.Utility;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.events.PromotePlayerEvent;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;


public class EntityListener implements Listener{
	protected GroupManager gm = NameAPI.getGroupManager();	
	
    @EventHandler(ignoreCancelled = true)
    public void explode(EntityExplodeEvent eee) {
        Iterator<Block> iterator = eee.blockList().iterator();
        List<Block> blocks = new ArrayList<Block>();
        while (iterator.hasNext()) {
            Block b = iterator.next();
            Block block = Utility.getRealBlock(b);
            //if it's a plant we want to check the reinforcement of the soil block
            if(Utility.isPlant(block)) {
            	Block soilBlock = block.getRelative(BlockFace.DOWN);
            	if(Citadel.getReinforcementManager().isReinforced(soilBlock)) {
            		block.getDrops().clear();
            		iterator.remove();
            	}
            }
            // getRealBlock should return the block we care about so if its already in the list we know it is a double block and was already handled.
            if (blocks.contains(block)){
            	block.getDrops().clear();
                iterator.remove();
            	continue;
            }
            blocks.add(block);
            try {
	            if (explodeReinforcement(block)) {
	                block.getDrops().clear();
	                iterator.remove();
	            }
            } catch (NoClassDefFoundError e){
            	Citadel.getInstance().getLogger().log(Level.WARNING, "Class Definition not found in explode", e);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void breakDoor(EntityBreakDoorEvent ebde) {
        ebde.setCancelled(maybeReinforcementDamaged(ebde.getBlock()));
    }

    @EventHandler(ignoreCancelled = true)
    public void changeBlock(EntityChangeBlockEvent ecbe) {
        ecbe.setCancelled(maybeReinforcementDamaged(ecbe.getBlock()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void spawn(CreatureSpawnEvent cse) {
    	ReinforcementManager reinforcementManager = Citadel.getReinforcementManager();
        EntityType type = cse.getEntityType();
        if (type != EntityType.IRON_GOLEM && type != EntityType.SNOWMAN && type != EntityType.WITHER && type != EntityType.SILVERFISH) return;

        for (Block block : getGolemBlocks(type, cse.getLocation().getBlock())) {
            Reinforcement reinforcement = reinforcementManager.getReinforcement(block);
            if (reinforcement != null) {
            	cse.setCancelled(true);
            }
        }
    }
    
    private List<Block> getGolemBlocks(EntityType type, Block base) {
        ArrayList<Block> blocks = new ArrayList<Block>();
        blocks.add(base);
        base = base.getRelative(BlockFace.UP);
        blocks.add(base);
        if (type == EntityType.IRON_GOLEM) {
            for (BlockFace face : new BlockFace[]{ BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST }) {
                Block arm = base.getRelative(face);
                if (arm.getType() == Material.IRON_BLOCK)
                    blocks.add(arm);
            }
        }
        base = base.getRelative(BlockFace.UP);
        blocks.add(base);
        
        return blocks;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerQuitEvent(PlayerQuitEvent event){
    	Player p = event.getPlayer();
    	PlayerState state = PlayerState.get(p);
    	state.reset();
    }
    
    

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerPromoteEvent(PromotePlayerEvent event){
    	Player p = event.getPlayer();
    	Group g = event.getGroup();
    	PlayerType currentType = event.getCurrentPlayerType();
    	PlayerType futureType = event.getFuturePlayerType();
    	PlayerState state = PlayerState.get(p);
    	GroupPermission gPerm = gm.getPermissionforGroup(g);
    	if(state.getMode() == ReinforcementMode.NORMAL){
    		//player is in NORMAL mode don't need to do anything
    		return;
    	}
    	if(gPerm.isAccessible(currentType, PermissionType.BLOCKS)){
    		//see if they can access blocks current state
    		if (!gPerm.isAccessible(futureType, PermissionType.BLOCKS)){
    			//if they will no longer be able to access blocks
    			Group citadelGroup = state.getGroup();
    			if(citadelGroup.getName() == g.getName()){
    				//Player is Actively Reinforcing in that group
    				state.reset();
        			String msg = "Your PlayerType in group (" + g.getName() + ") changed you no longer have access to reinforce/bypass";
        			p.sendMessage(ChatColor.RED + msg);
        			p.sendMessage(ChatColor.GREEN + "Your mode has been set to " + 
        					ReinforcementMode.REINFORCEMENT.name() + ".");
    			}
    			
    			
    		}
    	}
    }   	
 
}
