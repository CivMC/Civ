package com.untamedears.citadel.listener;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.PlacementMode;
import com.untamedears.citadel.PluginConsumer;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.PlayerState;
import com.untamedears.citadel.entity.Reinforcement;
import com.untamedears.citadel.entity.ReinforcementMaterial;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Bed;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

import static com.untamedears.citadel.Utility.*;

public class BlockListener extends PluginConsumer implements Listener {

    public BlockListener(Citadel plugin) {
        super(plugin);
    }

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
            if (createReinforcement(player, block) == null) {
                sendMessage(player, ChatColor.RED, "%s is not a reinforcible material", block.getType().name());
            }
        } else {
            sendMessage(player, ChatColor.YELLOW, "%s depleted, left fortification mode", material.getMaterial().name());
            state.reset();
            bpe.setCancelled(true);
        }
    }

    /**
     * This handles the BlockPlaceEvent for chests which will attach to a registered chest.
     *
     * @param bpe BlockPlaceEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void placeDoubleChest(BlockPlaceEvent bpe) {
        Block block = bpe.getBlock();
        Material material = block.getType();

        MaterialData data = block.getState().getData();
        if (data instanceof Door || data instanceof Bed || block.getType() == Material.CHEST) {
            plugin.logVerbose("Placed %s", block);
        }

        if (material == Material.CHEST) {
            // if the placed block is a chest, check all around the chest for a connecting one
            Block attachedChest = getAttachedChest(block);
            if (attachedChest == null) return;

            Reinforcement reinforcement = plugin.dao.findReinforcement(attachedChest);
            if (reinforcement == null) return;

            if (reinforcement.isAccessible(bpe.getPlayer())) {
                // if the player has access
                // add the same reinforcement to the new chest
                plugin.dao.save(reinforcement.clone(block));
            } else {
                // do not allow chest placement
                sendMessage(bpe.getPlayer(), ChatColor.RED, "The attached %s" + reinforcement.getStatus());
                bpe.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockBreak(BlockBreakEvent bbe) {
        Block block = bbe.getBlock();
        Player player = bbe.getPlayer();

        AccessDelegate delegate = AccessDelegate.getDelegate(block);
        Reinforcement reinforcement = delegate.getReinforcement();
        if (reinforcement == null) return;

        PlayerState state = PlayerState.get(player);
        if (state.isBypassMode() && reinforcement.isAccessible(player)) {
            plugin.logVerbose("Player %s bypassed reinforcement %s", player.getDisplayName(), reinforcement);

            bbe.setCancelled(reinforcementBroken(reinforcement));
        } else {
            bbe.setCancelled(reinforcementDamaged(reinforcement));
        }
        if (bbe.isCancelled()) {
            block.getDrops().clear();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void blockBurn(BlockBurnEvent bbe) {
        bbe.setCancelled(maybeReinforcementDamaged(bbe.getBlock()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void redstonePower(BlockRedstoneEvent bre) {
        Block block = bre.getBlock();
        
        if (!(block.getState().getData() instanceof Openable)) return;
        
        Openable openable = (Openable) block.getState().getData();
        if (openable.isOpen()) return;
        
        Reinforcement reinforcement = plugin.dao.findReinforcement(block);
        if (reinforcement == null || reinforcement.getSecurityLevel() == SecurityLevel.PUBLIC) return;

        boolean isAuthorizedPlayerNear = false;
        for (Entity entity : block.getChunk().getEntities()) {
            if (entity instanceof Player) {
                if (entity.getLocation().distanceSquared(block.getLocation()) < plugin.redstoneDistance
                        && reinforcement.isAccessible((Player) entity)) {
                    isAuthorizedPlayerNear = true;
                }
                if (isAuthorizedPlayerNear) break;
            }
        }

        if (!isAuthorizedPlayerNear) {
            plugin.logVerbose("Prevented redstone from opening reinforcement %s", reinforcement);
            bre.setNewCurrent(bre.getOldCurrent());
        }
    }
}