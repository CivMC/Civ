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
import com.aleksey.castlegates.types.GearState;
import com.aleksey.castlegates.utils.Helper;

public class CastleGatesManager {
	private static class FindGearResult {
		public GearState gear;
		public int distance;
		
		public FindGearResult(GearState gear, int distance) {
			this.gear = gear;
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
		if(!CastleGates.getConfigManager().isStickItem(event.getItem())) return;
		
		boolean interacted;
		
		switch(this.stateManager.getPlayerMode(event.getPlayer())) {
		case CREATE:
			interacted = createGear(event);
			break;
		case LINK:
			interacted = linkGears(event);
			break;
		case INFO:
			interacted = showGearInfo(event);
			break;
		default:
			interacted = false;
			break;
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
			
			if(this.gearManager.getGear(new BlockCoord(faceBlock)) != null) {
				this.waitingBlocks.add(faceBlock);
			}
        }
	}
	
	public void handleBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		
		if(!this.waitingBlocks.remove(block) || this.processingBlocks.contains(block)) return;
		
		GearState gear = this.gearManager.getGear(new BlockCoord(block));

		if(gear == null || gear.isPowered() == block.isBlockPowered()) return;
		
		this.processingBlocks.add(block);
			
		try
		{
			List<Player> players = Helper.getNearbyPlayers(block.getLocation());
			
			GearManager.PowerResult result = this.gearManager.processGear(
					block.getWorld(),
					gear,
					block.isBlockPowered(),
					players,
					block
					);
			
			String message;
			
			switch(result) {
			case Blocked:
				message = ChatColor.RED + "Undraw path is blocked";
				break;
			case Brocken:
				message = ChatColor.RED + "Bridge/gates is broken";
				break;
			case NotInCitadelGroup:
				message = ChatColor.RED + "Citadel prevent undrawing";
				break;
			case BastionBlocked:
				message = ChatColor.RED + "Bastion prevent drawing";
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
		} finally {
			this.processingBlocks.remove(block);
		}
	}
	
	private boolean createGear(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		
		if(!CastleGates.getCitadelManager().canChange(player, block.getLocation())) {
			player.sendMessage(ChatColor.RED + "Citadel preventing creation of gear.");
			return false;
		}
		
		ItemStack consumeItem = CastleGates.getConfigManager().getCreationConsumeItem();
		List<Integer> consumeSlots = Helper.getConsumeSlots(player, consumeItem);
		
		if(consumeSlots == null && consumeItem != null) {
			player.sendMessage(ChatColor.RED + "Not enough material to create gear.");
			return false;
		}
		
		GearManager.CreateResult result = this.gearManager.createGear(block); 
		
		if(result == GearManager.CreateResult.NotCreated) {
			player.sendMessage(ChatColor.RED + "This material cannot be used for gear.");
		} else if(result == GearManager.CreateResult.AlreadyExist) {
			player.sendMessage(ChatColor.RED + "Gear already exist.");
		} else {
			Helper.consumeItem(player, consumeItem, consumeSlots);
			
			player.sendMessage(ChatColor.GREEN + "Gear has been created.");
		}
		
		return true;
	}
	
	private boolean linkGears(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		
		if(!CastleGates.getCitadelManager().canChange(player, block.getLocation())) {
			player.sendMessage(ChatColor.RED + "Citadel preventing creation of link.");
			return false;
		}
		
		GearState gear1 = this.gearManager.getGear(new BlockCoord(block)); 
		
		if(gear1 == null) return false;
		
		FindGearResult result = findEndGear(block, event.getBlockFace());
		
		if(result == null) {
			event.getPlayer().sendMessage(ChatColor.RED + "End gear is not found. Link distance is limited to " + CastleGates.getConfigManager().getMaxBridgeLength() + " blocks");
		}
		else if(gear1.getLink() != null) {
			if(gear1.getLink().isDrawn()) {
				player.sendMessage(ChatColor.RED + "Link in drawn state cannot be removed.");
			} else {
				this.gearManager.removeLink(gear1.getLink());
				player.sendMessage(ChatColor.GREEN + "Gear's link has been removed.");
			}
		}
		else {
			Location loc = new Location(block.getWorld(), result.gear.getCoord().getX(), result.gear.getCoord().getY(), result.gear.getCoord().getZ());
			
			if(!CastleGates.getCitadelManager().canChange(player, loc)) {
				player.sendMessage(ChatColor.RED + "Citadel preventing creation of link.");
				return false;
			}

			if(this.gearManager.createLink(gear1, result.gear, result.distance)) {
				player.sendMessage(ChatColor.GREEN + "Gear has been linked with gear at x = " + result.gear.getCoord().getX() + ", y = " + result.gear.getCoord().getY() + ", z = " + result.gear.getCoord().getZ());
			}
			else {
				player.sendMessage(ChatColor.RED + "Link cannot be created.");
			}
		}
		
		return true;
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
			GearState gearState = this.gearManager.getGear(location);

			if(gearState != null) return new FindGearResult(gearState, i);
		}
		
		return null;
	}
	
	private boolean showGearInfo(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		GearState gear = this.gearManager.getGear(new BlockCoord(block));
		
		if(gear == null) return false;
		
		if(gear.getLink() == null) {
			event.getPlayer().sendMessage(ChatColor.GREEN + "Gear not linked");
			
			if(gear.getBrokenLink() != null) {
				event.getPlayer().sendMessage(ChatColor.GREEN + "But contains " + gear.getBrokenLink().getBlocks().size() + " drawn blocks");
			}
		} else {
			GearState gear2 = gear.getLink().getGear1() == gear ? gear.getLink().getGear2(): gear.getLink().getGear1();
			event.getPlayer().sendMessage(ChatColor.GREEN + "Gear linked to gear at x = " + gear2.getCoord().getX() + ", y = " +  + gear2.getCoord().getY() + ", z = " +  + gear2.getCoord().getZ());
			
			if(gear.getLink().isDrawn()) {
				event.getPlayer().sendMessage(ChatColor.GREEN + "Link is in drawn state");
			}
		}
		
		return true;
	}
}