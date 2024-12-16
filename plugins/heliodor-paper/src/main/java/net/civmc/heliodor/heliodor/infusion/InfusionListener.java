package net.civmc.heliodor.heliodor.infusion;

import net.civmc.heliodor.heliodor.HeliodorGem;
import net.civmc.heliodor.heliodor.infusion.chunkmeta.CauldronInfuseData;
import net.civmc.heliodor.heliodor.infusion.chunkmeta.CauldronInfusion;
import net.civmc.heliodor.meteoriciron.MeteoricIron;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableStorageEngine;

public class InfusionListener implements Listener {

    private final InfusionManager infusionManager;
    private BlockBasedChunkMetaView<CauldronInfuseData, TableBasedDataObject, TableStorageEngine<CauldronInfusion>> chunkMetaView;


    public InfusionListener(InfusionManager infusionManager, BlockBasedChunkMetaView<CauldronInfuseData, TableBasedDataObject, TableStorageEngine<CauldronInfusion>> chunkMetaView) {
        this.infusionManager = infusionManager;
        this.chunkMetaView = chunkMetaView;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockPlaceEvent event) {
        ItemStack hand = event.getItemInHand();
        Integer charge = HeliodorGem.getCharge(hand);
        Integer maxCharge = HeliodorGem.getMaxCharge(hand);
        if (charge == null || maxCharge == null) {
            return;
        }

        CauldronInfusion infusion = new CauldronInfusion(event.getBlock().getLocation(), true, charge, maxCharge);
        chunkMetaView.put(infusion);
        infusionManager.addInfusion(infusion);
    }

    @EventHandler
    public void onFinishedPlace(BlockPlaceEvent event) {
        ItemStack hand = event.getItemInHand();
        if (HeliodorGem.isFinished(hand) || MeteoricIron.isIngot(hand)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (chunkMetaView.get(block) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (chunkMetaView.get(block) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(chunkMetaView.get(event.getBlock()) instanceof CauldronInfusion infusion)) {
            return;
        }
        chunkMetaView.remove(event.getBlock());
        event.setDropItems(false);
        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), HeliodorGem.createHeliodorGem(infusion.getCharge(), infusion.getMaxCharge()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplosion(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> chunkMetaView.get(block) != null);
    }
}
