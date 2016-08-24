/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.manager;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.types.BlockCoord;
import com.aleksey.castlegates.types.CommandMode;
import com.aleksey.castlegates.types.Gearblock;
import com.aleksey.castlegates.utils.Helper;

public class CastleGatesManager {
	private static class FindGearResult {
		public Gearblock gearblock;
		public int distance;
		
		public FindGearResult(Gearblock gearblock, int distance) {
			this.gearblock = gearblock;
			this.distance = distance;
		}
	}
	
	private SqlDatabase db;
	private PlayerStateManager stateManager = new PlayerStateManager();
	private GearManager gearManager = new GearManager();
	private HashSet<Block> waitingBlocks = new HashSet<Block>();
	private HashSet<Block> processingBlocks = new HashSet<Block>();
	
	public boolean init(SqlDatabase db) {
		this.db = db;
		
		try {
			this.gearManager.init(db);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void close() {
		this.gearManager.close();
		
		if(this.db != null) {
			this.db.close();
		}
	}
	
	public void setPlayerMode(Player player, CommandMode mode) {
		stateManager.setPlayerMode(player, mode);
	}
	
	public void handlePlayerJoin(PlayerJoinEvent event) {
		this.stateManager.clearPlayerMode(event.getPlayer());
	}
	
	public void handlePlayerQuit(PlayerQuitEvent event) {
		this.stateManager.clearPlayerMode(event.getPlayer());
	}
	
	public void handleBlockClicked(PlayerInteractEvent event) {
		boolean interacted = false;
		ConfigManager configManager = CastleGates.getConfigManager();
		CommandMode mode = this.stateManager.getPlayerMode(event.getPlayer());
		
		if(configManager.getAllowAutoCreate()
				&& configManager.isCreationConsumeItem(event.getItem()))
		{
			interacted = createGearblockAndLink(event);
		}
		else if(mode == CommandMode.INFO) {
			showGearInfo(event);
			interacted = true;
		}
		else if(configManager.isStickItem(event.getItem())) {
			switch(this.stateManager.getPlayerMode(event.getPlayer())) {
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
		
		if(interacted) {
			this.stateManager.interact(event.getPlayer());
		}
	}
	
	public void handleBlockBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		GearManager.RemoveResult result = this.gearManager.removeGear(new BlockCoord(block));
		
		if(result == GearManager.RemoveResult.Removed || result == GearManager.RemoveResult.RemovedWithLink) {
			Helper.putItemToInventoryOrDrop(event.getPlayer(), block.getLocation(), CastleGates.getConfigManager().getCreationConsumeItem());
		}
	}
	
	public void handleBlockRedstone(BlockRedstoneEvent event) {
        if ((event.getOldCurrent() != 0) == (event.getNewCurrent() != 0)) return;
        
		Block block = event.getBlock();
		
		for (BlockFace face : GearManager.faces) {
			Block faceBlock = block.getRelative(face);
			
			if(this.gearManager.getGearblock(new BlockCoord(faceBlock)) != null) {
				this.waitingBlocks.add(faceBlock);
			}
        }
	}
	
	public void handleBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		
		if(!this.waitingBlocks.remove(block) || this.processingBlocks.contains(block)) return;
		
		Gearblock gearblock = this.gearManager.getGearblock(new BlockCoord(block));

		if(gearblock == null || gearblock.isPowered() == block.isBlockPowered()) return;
		
		this.processingBlocks.add(block);
			
		try
		{
			List<Player> players = Helper.getNearbyPlayers(block.getLocation());
			
			GearManager.PowerResult result = this.gearManager.processGearblock(
					block.getWorld(),
					gearblock,
					block.isBlockPowered(),
					players
					);
			
			String message;
			
			switch(result) {
			case Blocked:
				message = ChatColor.RED + "Undraw path is blocked";
				break;
			case Brocken:
				message = ChatColor.RED + "Bridge/gates is broken";
				break;
			case CannotDrawGear:
				message = ChatColor.RED + "Gearblock cannot be drawn";
				break;
			case NotInCitadelGroup:
				message = ChatColor.RED + "Citadel prevent this operation";
				break;
			case BastionBlocked:
				message = ChatColor.RED + "Bastion prevent undrawing";
				break;
			default:
				message = null;
				break;
			}
			
			if(message != null) {
				for(Player player : players) {
					player.sendMessage(message);
				}
			}
			else if(result == GearManager.PowerResult.Drawn || result == GearManager.PowerResult.Undrawn){
				Sound sound = result == GearManager.PowerResult.Drawn
						? Sound.BLOCK_PISTON_CONTRACT
						: Sound.BLOCK_PISTON_EXTEND;
				
				block.getWorld().playSound(block.getLocation(), sound, 0.7f, 1);
			}
		} finally {
			this.processingBlocks.remove(block);
		}
	}
	
	private boolean createGearblockAndLink(PlayerInteractEvent event) {
		if(!createGearblock(event)) return false;
		
		Block block = event.getClickedBlock();
		Gearblock gearblock1 = this.gearManager.getGearblock(new BlockCoord(block));
		
		if(gearblock1.getLink() != null) return true;
		
		for(BlockFace face : GearManager.faces) {
			FindGearResult result = findEndGear(block, face);
			
			if(result == null || result.gearblock.getLink() != null) continue;
			
			linkGearblocks(event.getPlayer(), gearblock1, result);
			
			break;
		}
		
		return true;
	}
	
	private boolean createGearblock(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		
		if(!CastleGates.getCitadelManager().canBypass(player, block.getLocation())) {
			player.sendMessage(ChatColor.RED + "Citadel preventing creation of gearblock.");
			return false;
		}
		
		ItemStack consumeItem = CastleGates.getConfigManager().getCreationConsumeItem();
		List<Integer> consumeSlots = Helper.getConsumeSlots(player, consumeItem);
		
		if(consumeSlots == null && consumeItem != null) {
			player.sendMessage(ChatColor.RED + "Not enough material to create gearblock.");
			return false;
		}
		
		GearManager.CreateResult result = this.gearManager.createGear(block); 
		
		if(result == GearManager.CreateResult.NotCreated) {
			player.sendMessage(ChatColor.RED + "This material cannot be used for gearblock.");
			return false;
		} else if(result == GearManager.CreateResult.AlreadyExist) {
			player.sendMessage(ChatColor.RED + "Gearblock already exist.");
			return true;
		}
		
		Helper.consumeItem(player, consumeItem, consumeSlots);
			
		player.sendMessage(ChatColor.GREEN + "Gearblock has been created.");
		
		return true;
	}
	
	private boolean linkGearblocks(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		
		Gearblock gearblock1 = this.gearManager.getGearblock(new BlockCoord(block)); 
		
		if(gearblock1 == null) return false;

		if(!CastleGates.getCitadelManager().canBypass(player, block.getLocation())) {
			player.sendMessage(ChatColor.RED + "Citadel preventing creation of link.");
			return false;
		}

		if(gearblock1.getLink() != null) {
			if(gearblock1.getLink().isDrawn()) {
				player.sendMessage(ChatColor.RED + "Link in drawn state cannot be removed.");
			} else {
				this.gearManager.removeLink(gearblock1.getLink());
				player.sendMessage(ChatColor.GREEN + "Gearblock's link has been removed.");
			}
			
			return true;
		}
		
		FindGearResult result = findEndGear(block, event.getBlockFace());

		if(result == null) {
			event.getPlayer().sendMessage(ChatColor.RED + "End gearblock is not found. Link distance is limited to " + CastleGates.getConfigManager().getMaxBridgeLength() + " blocks");
		} else {
			linkGearblocks(player, gearblock1, result);
		}
		
		return true;
	}
	
	private boolean linkGearblocks(Player player, Gearblock gearblock1, FindGearResult result) {
		Location loc = new Location(player.getWorld(), result.gearblock.getCoord().getX(), result.gearblock.getCoord().getY(), result.gearblock.getCoord().getZ());
		
		if(!CastleGates.getCitadelManager().canBypass(player, loc)) {
			player.sendMessage(ChatColor.RED + "Citadel preventing creation of link.");
			return false;
		}

		if(this.gearManager.createLink(gearblock1, result.gearblock, result.distance)) {
			player.sendMessage(ChatColor.GREEN + "Gearblock has been linked with gearblock at x = " + result.gearblock.getCoord().getX() + ", y = " + result.gearblock.getCoord().getY() + ", z = " + result.gearblock.getCoord().getZ());
			return true;
		}

		player.sendMessage(ChatColor.RED + "Link cannot be created.");
		
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
			Gearblock gearblock = this.gearManager.getGearblock(location);

			if(gearblock != null) {
				return i > 0 ? new FindGearResult(gearblock, i): null;
			}
		}
		
		return null;
	}
	
	private void showGearInfo(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		BlockCoord blockCoord = new BlockCoord(block);
		Gearblock gearblock = this.gearManager.getGearblock(blockCoord);
		
		if(gearblock == null) {
			GearManager.SearchBridgeBlockResult searchResult = this.gearManager.searchBridgeBlock(blockCoord);
			
			switch(searchResult) {
			case Bridge:
				event.getPlayer().sendMessage("Bridge block");
				break;
			case Gates:
				event.getPlayer().sendMessage("Gates block");
				break;
			default:
				break;
			}
			
			return;
		}
		
		if(!CastleGates.getCitadelManager().canViewInformation(event.getPlayer(), block.getLocation())) {
			event.getPlayer().sendMessage(ChatColor.RED + "Gearblock");
		}
		else if(gearblock.getLink() == null) {
			event.getPlayer().sendMessage(ChatColor.GREEN + "Gearblock not linked");
			
			if(gearblock.getBrokenLink() != null) {
				event.getPlayer().sendMessage(ChatColor.GREEN + "But contains " + gearblock.getBrokenLink().getBlocks().size() + " drawn blocks");
			}
		}
		else {
			Gearblock gearblock2 = gearblock.getLink().getGearblock1() == gearblock ? gearblock.getLink().getGearblock2(): gearblock.getLink().getGearblock1();
			event.getPlayer().sendMessage(ChatColor.GREEN + "Gearblock linked to gearblock at x = " + gearblock2.getCoord().getX() + ", y = " +  + gearblock2.getCoord().getY() + ", z = " +  + gearblock2.getCoord().getZ());
			
			if(gearblock.getLink().isDrawn()) {
				event.getPlayer().sendMessage(ChatColor.GREEN + "Link is in drawn state");
			}
		}
	}
}