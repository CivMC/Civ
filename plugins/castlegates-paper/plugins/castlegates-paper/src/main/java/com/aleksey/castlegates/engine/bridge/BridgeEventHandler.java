package com.aleksey.castlegates.engine.bridge;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.config.ConfigManager;
import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.engine.PlayerStateManager;
import com.aleksey.castlegates.engine.StorageManager;
import com.aleksey.castlegates.types.*;
import com.aleksey.castlegates.utils.Helper;
import com.aleksey.castlegates.utils.ParticleHelper;
import com.aleksey.castlegates.utils.PowerResultHelper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Created by Aleksey Terzi
 */
public class BridgeEventHandler {
    private static class FindGearResult {
        public Gearblock gearblock;
        public int distance;

        public FindGearResult(Gearblock gearblock, int distance) {
            this.gearblock = gearblock;
            this.distance = distance;
        }
    }

    private StorageManager storage;
    private BridgeManager bridgeManager = new BridgeManager();
    private HashSet<Block> waitingBlocks = new HashSet<Block>();
    private HashSet<Block> processingBlocks = new HashSet<Block>();

    public void init(StorageManager storage) {
        this.storage = storage;

        this.bridgeManager.init(this.storage);
    }

    public void close() {
        if(this.bridgeManager != null) {
            this.bridgeManager.close();
        }
    }

    public boolean handleBlockClicked(PlayerInteractEvent event, PlayerStateManager.PlayerState state) {
        boolean interacted = false;
        ConfigManager configManager = CastleGates.getConfigManager();
        CommandMode mode = state != null ? state.mode: CommandMode.OFF;

        if(configManager.getAllowAutoCreate() && configManager.isCreationConsumeItem(event.getItem())) {
            interacted = createGearblockAndLink(event);
        }
        else if(mode == CommandMode.INFO) {
            showGearInfo(event);
            interacted = true;
        }
        else if(mode == CommandMode.TIMER) {
            interacted = setGearblockTimer(event, state);
        }
        else if(configManager.isStickItem(event.getItem())) {
            switch(mode) {
                case CREATE:
                    interacted = createGearblock(event);
                    break;
                case LINK:
                    interacted = linkGearblocks(event);
                    break;
                default:
                    interacted = false;
                    break;
            }
        }

        return interacted;
    }

    public void handleBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BridgeManager.RemoveResult result = this.bridgeManager.removeGear(new BlockCoord(block));

        if(result == BridgeManager.RemoveResult.Removed || result == BridgeManager.RemoveResult.RemovedWithLink) {
            Helper.putItemToInventoryOrDrop(event.getPlayer(), block.getLocation(), CastleGates.getConfigManager().getCreationConsumeItem());
        }
    }

    public void handleBlockRedstone(BlockRedstoneEvent event) {
        if ((event.getOldCurrent() != 0) == (event.getNewCurrent() != 0)) return;

        Block block = event.getBlock();

        for (BlockFace face : BridgeManager.faces) {
            Block faceBlock = block.getRelative(face);

            if(this.storage.getGearblock(new BlockCoord(faceBlock)) != null) {
                this.waitingBlocks.add(faceBlock);
            }
        }
    }

    public void handleBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();

        if(!this.waitingBlocks.remove(block) || this.processingBlocks.contains(block)) return;

        Gearblock gearblock = this.storage.getGearblock(new BlockCoord(block));

        if(gearblock == null || gearblock.isPowered() == block.isBlockPowered()) return;

        this.processingBlocks.add(block);

        try
        {
            List<Player> players = Helper.getNearbyPlayers(block.getLocation());

            PowerResult result = this.bridgeManager.processGearblock(
                    block.getWorld(),
                    gearblock,
                    block.isBlockPowered(),
                    players
            );

            if(result.status == PowerResult.Status.Locked && gearblock.getTimerMode() == TimerMode.DOOR) {
                result.status = PowerResult.Status.Unchanged;
            }

            PowerResultHelper.showStatus(block.getLocation(), players, result);
        } finally {
            this.processingBlocks.remove(block);
        }
    }

    public void handleEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            this.bridgeManager.removeGear(new BlockCoord(block));
        }
    }

    public void handleEntityChangeBlock(EntityChangeBlockEvent event) {
        this.bridgeManager.removeGear(new BlockCoord(event.getBlock()));
    }

    public void handlePistonEvent(List<Block> blocks) {
        ItemStack dropItem = CastleGates.getConfigManager().getCreationConsumeItem();

        for (Block block : blocks) {
            BridgeManager.RemoveResult result = this.bridgeManager.removeGear(new BlockCoord(block));

            if(result == BridgeManager.RemoveResult.Removed || result == BridgeManager.RemoveResult.RemovedWithLink) {
                Helper.putItemToInventoryOrDrop(null, block.getLocation(), dropItem);
            }
        }
    }

    private boolean createGearblockAndLink(PlayerInteractEvent event) {
        if(!createGearblock(event)) return false;

        Block block = event.getClickedBlock();
        Gearblock gearblock1 = this.storage.getGearblock(new BlockCoord(block));

        if(gearblock1.getLink() != null) return true;

        for(BlockFace face : BridgeManager.faces) {
            FindGearResult result = findEndGear(block, face);

            if(result != null
                    && result.gearblock.getLink() == null
                    && linkGearblocks(event.getPlayer(), gearblock1, result, false)
                    )
            {
                break;
            }
        }

        return true;
    }

    private boolean createGearblock(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if(!CastleGates.getCitadelManager().canBypass(player, block.getLocation())) {
            player.sendMessage(ChatColor.RED + "Citadel has prevented the creation of a gearblock.");
            return false;
        }

        ItemStack consumeItem = CastleGates.getConfigManager().getCreationConsumeItem();
        List<Integer> consumeSlots = Helper.getConsumeSlots(player, consumeItem);

        if(consumeSlots == null && consumeItem != null) {
            player.sendMessage(ChatColor.RED + "Not enough material left to create a gearblock.");
            return false;
        }

        BridgeManager.CreateResult result = this.bridgeManager.createGear(block);

        if(result == BridgeManager.CreateResult.NotCreated) {
            player.sendMessage(ChatColor.RED + block.getType().toString() + " cannot be used as a gearblock.");
            return false;
        } else if(result == BridgeManager.CreateResult.AlreadyExist) {
            player.sendMessage(ChatColor.RED + "That block is already a gearblock.");
            return true;
        }

        Helper.consumeItem(player, consumeItem, consumeSlots);

        player.sendMessage(ChatColor.GREEN + "A gearblock has been created.");

        ParticleHelper.spawn(block, ParticleHelper.Type.Info);

        return true;
    }

    private boolean linkGearblocks(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        Gearblock gearblock1 = this.storage.getGearblock(new BlockCoord(block));

        if(gearblock1 == null) return false;

        if(!CastleGates.getCitadelManager().canBypass(player, block.getLocation())) {
            player.sendMessage(ChatColor.RED + "Citadel has prevented the creation of a link.");
            return false;
        }

        if(gearblock1.getLink() != null) {
            if(gearblock1.getLink().isDrawn()) {
                player.sendMessage(ChatColor.RED + "Cannot unlink gearblocks in a drawn bridge/gate.");
            } else {
                this.bridgeManager.removeLink(gearblock1.getLink());
                player.sendMessage(ChatColor.GREEN + "The gearblocks have been unlinked.");
            }

            return true;
        }

        FindGearResult result = findEndGear(block, event.getBlockFace());

        if(result == null) {
            event.getPlayer().sendMessage(ChatColor.RED + "End gearblock not found. Link distance is limited to " + CastleGates.getConfigManager().getMaxBridgeLength() + " blocks.");
        } else {
            linkGearblocks(player, gearblock1, result, true);
        }

        return true;
    }

    private boolean linkGearblocks(Player player, Gearblock gearblock1, FindGearResult result, boolean showError) {
        Location loc = new Location(player.getWorld(), result.gearblock.getCoord().getX(), result.gearblock.getCoord().getY(), result.gearblock.getCoord().getZ());

        if(!CastleGates.getCitadelManager().canBypass(player, loc)) {
            if(showError) {
                player.sendMessage(ChatColor.RED + "Citadel has prevented the creation of a link.");
            }

            return false;
        }

        if(result.gearblock.getLink() != null) {
            if(showError) {
                player.sendMessage(ChatColor.RED + "The gearblock at [" + result.gearblock.getCoord().getX() + " " + result.gearblock.getCoord().getY() + " " + result.gearblock.getCoord().getZ() + "] already has a link. Remove it before creating a new one.");
                ParticleHelper.spawn(player, result.gearblock, ParticleHelper.Type.Warning);
            }

            return false;
        }

        if(this.bridgeManager.createLink(gearblock1, result.gearblock, result.distance)) {
            player.sendMessage(ChatColor.GREEN + "The gearblock has been linked with the gearblock at [" + result.gearblock.getCoord().getX() + " " + result.gearblock.getCoord().getY() + " " + result.gearblock.getCoord().getZ() + "].");
            ParticleHelper.spawn(player, gearblock1, ParticleHelper.Type.Info);
            ParticleHelper.spawn(player, result.gearblock, ParticleHelper.Type.Info);
            return true;
        }

        if(showError) {
            player.sendMessage(ChatColor.RED + "The gearblock at [" + result.gearblock.getCoord().getX() + " " + result.gearblock.getCoord().getY() + " " + result.gearblock.getCoord().getZ() + "] has a broken link which cannot be restored by using the clicked block. Click the block where the other gearblock used to be.");
            ParticleHelper.spawn(player, result.gearblock, ParticleHelper.Type.Warning);
        }

        return false;
    }

    private FindGearResult findEndGear(Block startGearBlock, BlockFace blockFace) {
        UUID worldUID = startGearBlock.getWorld().getUID();
        int x = startGearBlock.getX();
        int y = startGearBlock.getY();
        int z = startGearBlock.getZ();

        for(int i = 0; i < CastleGates.getConfigManager().getMaxBridgeLength(); i++) {
            x += blockFace.getModX();
            y += blockFace.getModY();
            z += blockFace.getModZ();

            BlockCoord location = new BlockCoord(worldUID, x, y, z);
            Gearblock gearblock = this.storage.getGearblock(location);

            if(gearblock != null) {
                return i > 0 ? new FindGearResult(gearblock, i): null;
            }
        }

        return null;
    }

    private void showGearInfo(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        BlockCoord blockCoord = new BlockCoord(block);
        Gearblock gearblock = this.storage.getGearblock(blockCoord);

        if(gearblock == null) {
            BridgeManager.SearchBridgeBlockResult searchResult = this.bridgeManager.searchBridgeBlock(blockCoord);

            switch(searchResult) {
                case Bridge:
                    player.sendMessage("Bridge block");
                    break;
                case Gates:
                    player.sendMessage("Gate block");
                    break;
                default:
                    break;
            }

            return;
        }

        if(!CastleGates.getCitadelManager().canViewInformation(player, block.getLocation())) {
            player.sendMessage(ChatColor.RED + "Gearblock");
        }
        else {
            if(gearblock.getLink() == null) {
                player.sendMessage(ChatColor.GREEN + "The gearblock is not linked.");

                if(gearblock.getBrokenLink() != null) {
                    player.sendMessage(ChatColor.GREEN + "But contains " + gearblock.getBrokenLink().getBlocks().size() + " drawn blocks.");
                }
            }
            else {
                Gearblock gearblock2 = gearblock.getLink().getGearblock1() == gearblock ? gearblock.getLink().getGearblock2(): gearblock.getLink().getGearblock1();
                player.sendMessage(ChatColor.GREEN + "The gearblock has been linked to the gearblock at [" + gearblock2.getCoord().getX() + " " + gearblock2.getCoord().getY() + " " + gearblock2.getCoord().getZ() + "].");

                if(gearblock.getLink().isDrawn()) {
                    player.sendMessage(ChatColor.GREEN + "The bridge/gate is drawn.");
                }

                ParticleHelper.spawn(player, gearblock2, ParticleHelper.Type.Info);
            }

            if(gearblock.getTimer() != null) {
                String message = "Timer: " + gearblock.getTimer() + " sec to process operation " + gearblock.getTimerOperation() + " in " + gearblock.getTimerMode() + " mode.";
                player.sendMessage(ChatColor.GREEN + message);
            }

            if(gearblock.getLockedGearblocks() != null || gearblock.getLockGearblock() != null) {
                player.sendMessage(ChatColor.YELLOW + "Locked");
            }
        }
    }

    private boolean setGearblockTimer(PlayerInteractEvent event, PlayerStateManager.PlayerState state) {
        Player player = event.getPlayer();

        if(!CastleGates.getConfigManager().isTimerEnabled()) {
            player.sendMessage(ChatColor.RED + "The timer function is disabled on this server.");
            return true;
        }

        Block block = event.getClickedBlock();
        BlockCoord blockCoord = new BlockCoord(block);
        Gearblock gearblock = this.storage.getGearblock(blockCoord);

        if(gearblock == null) {
            player.sendMessage(ChatColor.RED + "That block is not a gearblock.");
            return true;
        }

        if(!CastleGates.getCitadelManager().canBypass(player, block.getLocation())) {
            player.sendMessage(ChatColor.RED + "Citadel has prevented that operation.");
            return true;
        }

        String message;

        if(gearblock.getTimer() == null) {
            this.storage.setGearblockTimer(gearblock, state.timer, state.timerOperation, state.timerMode);
            String modeText = state.timerMode == TimerMode.DEFAULT ? "DEFAULT" : "DOOR";
            message = ChatColor.GREEN + "The gearblock's timer has been set to " + state.timer + " sec to process operation " + state.timerOperation + " in " + modeText + " mode.";
        } else {
            this.storage.clearGearblockTimer(gearblock);
            message = ChatColor.YELLOW + "The gearblock's timer has been removed.";
        }

        player.sendMessage(message);

        ParticleHelper.spawn(player, gearblock, ParticleHelper.Type.Info);

        return true;
    }
}