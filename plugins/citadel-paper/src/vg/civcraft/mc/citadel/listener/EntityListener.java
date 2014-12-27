package vg.civcraft.mc.citadel.listener;

import static vg.civcraft.mc.citadel.Utility.explodeReinforcement;
import static vg.civcraft.mc.citadel.Utility.maybeReinforcementDamaged;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

public class EntityListener implements Listener{
    @EventHandler(ignoreCancelled = true)
    public void explode(EntityExplodeEvent eee) {
        Iterator<Block> iterator = eee.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            try {
	            if (explodeReinforcement(block)) {
	                block.getDrops().clear();
	                iterator.remove();
	            }
            } catch (NoClassDefFoundError e){
            	Citadel.Log("NoClassDefFoundError");
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

    @EventHandler(ignoreCancelled = true)
    public void spawn(CreatureSpawnEvent cse) {
    	ReinforcementManager reinforcementManager = Citadel.getReinforcementManager();
        EntityType type = cse.getEntityType();
        if (type != EntityType.IRON_GOLEM && type != EntityType.SNOWMAN) return;

        for (Block block : getGolemBlocks(type, cse.getLocation().getBlock())) {
            Reinforcement reinforcement = reinforcementManager.getReinforcement(block);
            if (reinforcement != null) {
            	/*
            	Citadel.verbose(
                        VerboseMsg.GolemCreated,
            			reinforcement.getBlock().getLocation().toString());
            			*/
                reinforcementManager.deleteReinforcement(reinforcement);
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
}
