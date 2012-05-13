package com.untamedears.citadel.listener;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.PluginConsumer;
import com.untamedears.citadel.entity.Reinforcement;
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
import org.bukkit.event.world.StructureGrowEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.untamedears.citadel.Utility.maybeReinforcementDamaged;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/21/12
 * Time: 9:56 PM
 */
public class EntityListener extends PluginConsumer implements Listener {

    public EntityListener(Citadel plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void explode(EntityExplodeEvent eee) {
        Iterator<Block> iterator = eee.blockList().iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (maybeReinforcementDamaged(block)) {
                block.getDrops().clear();
                iterator.remove();
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
        EntityType type = cse.getEntityType();
        if (type != EntityType.IRON_GOLEM && type != EntityType.SNOWMAN) return;

        for (Block block : getGolemBlocks(type, cse.getLocation().getBlock())) {
            Reinforcement reinforcement = plugin.dao.findReinforcement(block);
            if (reinforcement != null) {
                plugin.logVerbose("Reinforcement %s removed due to golem creation", reinforcement);
                plugin.dao.delete(reinforcement);
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

    @EventHandler(ignoreCancelled = true)
    public void grow(StructureGrowEvent sge) {
        Reinforcement reinforcement = plugin.dao.findReinforcement(sge.getLocation());
        if (reinforcement != null) {
            plugin.logVerbose("Reinforcement %s removed due to structure growth", reinforcement);
            plugin.dao.delete(reinforcement);
        }
    }
}
