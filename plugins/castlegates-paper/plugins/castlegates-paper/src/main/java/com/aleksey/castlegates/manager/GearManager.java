/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.DeprecatedMethods;
import com.aleksey.castlegates.citadel.ICitadelManager;
import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.types.BlockCoord;
import com.aleksey.castlegates.types.BlockState;
import com.aleksey.castlegates.types.Gearblock;
import com.aleksey.castlegates.types.GearblockLink;
import com.aleksey.castlegates.types.PowerResult;
import com.aleksey.castlegates.utils.DataWorker;

public class GearManager {
	public static final BlockFace[] faces = new BlockFace[] {
			BlockFace.EAST,
			BlockFace.WEST,
			BlockFace.UP,
			BlockFace.DOWN,
			BlockFace.SOUTH,
			BlockFace.NORTH
	};
	
	public static enum CreateResult { NotCreated, AlreadyExist, Created }
	public static enum RemoveResult { NotExist, Removed, RemovedWithLink }
	public static enum SearchBridgeBlockResult { NotFound, Bridge, Gates }

	private Map<BlockCoord, Gearblock> gearblocks;
	private DataWorker dataWorker;
	
	public void init(SqlDatabase db) throws SQLException {
		this.dataWorker = new DataWorker(db,  CastleGates.getConfigManager().getLogChanges());
		this.gearblocks = this.dataWorker.load();
		
		CastleGates.getPluginLogger().log(Level.INFO, "Loaded " + this.gearblocks.size() + " gearblocks");
		
		this.dataWorker.startThread();
	}
	
	public void close() {
		if(this.dataWorker != null) {
			this.dataWorker.close();
		}
	}
	
	public SearchBridgeBlockResult searchBridgeBlock(BlockCoord coord) {
		int maxLen = CastleGates.getConfigManager().getMaxBridgeLength();
		
		for(BlockFace face : faces) {
			BlockCoord current = coord;
			
			for(int i = 0; i < maxLen; i++) {
				current.increment(face);
				
				Gearblock gearblock = this.gearblocks.get(current);
				
				if(gearblock != null) {
					if(isBridgeBlock(gearblock, face)) {
						return face == BlockFace.UP || face == BlockFace.DOWN
								? SearchBridgeBlockResult.Gates
								: SearchBridgeBlockResult.Bridge;
					}
					
					break;
				}
			}
		}
		
		return SearchBridgeBlockResult.NotFound;
	}
	
	private static boolean isBridgeBlock(Gearblock gearblock, BlockFace face) {
		GearblockLink link = gearblock.getLink();  
		
		if(link == null || link.isDrawn()) return false;
		
		BlockCoord coord1 = link.getGearblock1().getCoord();
		BlockCoord coord2 = link.getGearblock2().getCoord();
		
		switch(face) {
		case EAST:
			return coord1.getX() < gearblock.getCoord().getX() || coord2.getX() < gearblock.getCoord().getX();
		case WEST:
			return coord1.getX() > gearblock.getCoord().getX() || coord2.getX() > gearblock.getCoord().getX();
		case UP:
			return coord1.getY() < gearblock.getCoord().getY() || coord2.getY() < gearblock.getCoord().getY();
		case DOWN:
			return coord1.getY() > gearblock.getCoord().getY() || coord2.getY() > gearblock.getCoord().getY();
		case SOUTH:
			return coord1.getZ() < gearblock.getCoord().getZ() || coord2.getZ() < gearblock.getCoord().getZ();
		case NORTH:
			return coord1.getZ() > gearblock.getCoord().getZ() || coord2.getZ() > gearblock.getCoord().getZ();
		default:
			return false;
		}
	}
	
	public CreateResult createGear(Block block) {
		BlockCoord location = new BlockCoord(block);
		
		if(!CastleGates.getConfigManager().isGearBlockType(block)) return CreateResult.NotCreated;
		if(this.gearblocks.containsKey(location)) return CreateResult.AlreadyExist;
		
		Gearblock gear = new Gearblock(location);
		
		this.gearblocks.put(location, gear);
		
		this.dataWorker.addChangedGearblock(gear);
		
		return CreateResult.Created;
	}
	
	public RemoveResult removeGear(BlockCoord location) {
		Gearblock gear = this.gearblocks.get(location);
		
		if(gear == null) return RemoveResult.NotExist;
		
		RemoveResult result = RemoveResult.Removed;
		
		if(gear.getBrokenLink() != null) {
			removeLink(gear.getBrokenLink());
			result = RemoveResult.RemovedWithLink;
		}
		else if (gear.getLink() != null) {
			if(gear.getLink().isDrawn()) {
				gear.getLink().setBroken(gear);
				this.dataWorker.addChangedLink(gear.getBrokenLink());
			} else {
				removeLink(gear.getLink());
				result = RemoveResult.RemovedWithLink;
			}
		}
		
		this.gearblocks.remove(location);
		
		gear.setRemoved();
		
		this.dataWorker.addChangedGearblock(gear);
		
		return result;
	}
	
	public Gearblock getGearblock(BlockCoord location) {
		return this.gearblocks.get(location);
	}
	
	public void removeLink(GearblockLink link) {
		if(link == null) return;
		
		link.setRemoved();
		
		if(link.getGearblock1() != null) {
			link.getGearblock1().setLink(null);
		}
		
		if(link.getGearblock2() != null) {
			link.getGearblock2().setLink(null);
		}

		this.dataWorker.addChangedLink(link);
	}
	
	public boolean createLink(Gearblock gearblock1, Gearblock gearblock2, int distance) {
		if(!removeGearblocksLinks(gearblock1, gearblock2, distance)) return false;
		
		if(gearblock1.getLink() != null) return true;
		
		GearblockLink link = new GearblockLink(gearblock1, gearblock2);
		
		if(gearblock1.getBrokenLink() != null) {
			link = gearblock1.getBrokenLink();
			link.setRestored(gearblock2);
		}
		else if(gearblock2.getBrokenLink() != null) {
			link = gearblock2.getBrokenLink();
			link.setRestored(gearblock1);
		}
		else {
			link = new GearblockLink(gearblock1, gearblock2);
		}
		
		gearblock1.setLink(link);
		gearblock2.setLink(link);
		
		this.dataWorker.addChangedLink(link);
		
		return true;
	}
	
	private boolean removeGearblocksLinks(Gearblock gearblock1, Gearblock gearblock2, int distance) {
		if(gearblock1.getBrokenLink() != null && gearblock2.getBrokenLink() != null
				|| gearblock1.getBrokenLink() != null && distance != gearblock1.getBrokenLink().getBlocks().size()
				|| gearblock2.getBrokenLink() != null && distance != gearblock2.getBrokenLink().getBlocks().size()
				)
		{
			return false;
		}

		GearblockLink link1 = gearblock1.getLink();
		GearblockLink link2 = gearblock2.getLink();
		
		if(link1 != null && link1.equals(link2)) return true;

		if(link1 != null && link1.isDrawn() || link2 != null && link2.isDrawn()) return false;
		
		if(link1 != null) {
			link1.setRemoved();
			this.dataWorker.addChangedLink(link1);
		}
		
		if(link2 != null) {
			link2.setRemoved();
			this.dataWorker.addChangedLink(link1);
		}
		
		return true;
	}
	
	public PowerResult processGearblock(World world, Gearblock gearblock, boolean isPowered, List<Player> players) {
		if(gearblock.isPowered() == isPowered) return PowerResult.Unchanged;
		
		if(!isPowered) {
			gearblock.setPowered(false);
			return PowerResult.Unpowered;
		}
		
		if(System.currentTimeMillis() - gearblock.getLastSwitchTime() < CastleGates.getConfigManager().getSwitchTimeout()) {
			return PowerResult.Unchanged;
		}
		
		if(!canAccessDoors(players, world, gearblock.getCoord())) {
			return PowerResult.NotInCitadelGroup;
		}
		
		gearblock.setPowered(true);
		
		if(gearblock.getLink() != null)
		{
			PowerResult result;
			
			if(!gearblock.getLink().isDrawn()) {
				result = canDraw(world, gearblock.getLink(), players);
			} else {
				result = canUndraw(world, gearblock.getLink(), players);			
			}
			
			if(result != PowerResult.Allowed) return result;
		}
		
		PowerResult result = powerGear(world, gearblock, isPowered, players);
		
		gearblock.setLastSwitchTime();
		
		return result;
	}
	
	private PowerResult powerGear(World world, Gearblock gearblock, boolean isPowered, List<Player> players) {
		HashSet<Gearblock> gearblocks = new HashSet<Gearblock>();
		gearblocks.add(gearblock);
		
		PowerResult result = transferPower(world, gearblock, isPowered, players, gearblocks);
		
		if(result != PowerResult.Allowed) return result;

		Boolean draw = null;

		for(Gearblock gearFromList : gearblocks) {
			GearblockLink linkFromList = gearFromList.getLink();
			
			if(linkFromList == null) continue;
			
			if(draw == null) {
				draw = !linkFromList.isDrawn();
			}
			
			if(!linkFromList.isDrawn()) {
				draw(world, linkFromList);
			} else {
				undraw(world, linkFromList);
			}
			
			this.dataWorker.addChangedLink(linkFromList);
		}
		
		if(draw == null) return PowerResult.Unchanged;
		
		return draw ? PowerResult.Drawn : PowerResult.Undrawn;
	}
	
	private PowerResult transferPower(
			World world,
			Gearblock gearblock,
			boolean isPowered,
			List<Player> players,
			HashSet<Gearblock> gearblocks
			)
	{
		ConfigManager configManager = CastleGates.getConfigManager();
		BlockCoord gearLoc = gearblock.getCoord();

		for(int i = 0; i < faces.length && gearblocks.size() <= configManager.getMaxPowerTransfers(); i++) {
			BlockFace face = faces[i];
			BlockCoord loc = new BlockCoord(world.getUID(), gearLoc.getX() + face.getModX(), gearLoc.getY() + face.getModY(), gearLoc.getZ() + face.getModZ());
			Gearblock to = this.gearblocks.get(loc);
			
			if(to == null || to.isPowered() == isPowered || gearblocks.contains(to)) continue;
			
			if(!canAccessDoors(players, world, loc)) return PowerResult.NotInCitadelGroup;
			
			if(isPowered) {
				GearblockLink link = to.getLink();
				
				if(link != null) {
					PowerResult result;
					
					if(!link.isDrawn()) {
						result = canDraw(world, link, players);
					} else {
						result = canUndraw(world, link, players);
					}
					
					if(result != PowerResult.Allowed) return result;
				}
				
				gearblocks.add(to);
			}
			
			PowerResult result = transferPower(world, to, isPowered, players, gearblocks);
			
			if(result != PowerResult.Allowed) return result;
		}
		
		return PowerResult.Allowed;
	}
	
	private BlockFace getLinkFace(GearblockLink link) {
		BlockCoord loc1 = link.getGearblock1().getCoord();
		BlockCoord loc2 = link.getGearblock2().getCoord();
		
		if(loc1.getX() != loc2.getX()) {
			return loc1.getX() < loc2.getX() ? BlockFace.EAST: BlockFace.WEST;
		}
		else if(loc1.getY() != loc2.getY()) {
			return loc1.getY() < loc2.getY() ? BlockFace.UP: BlockFace.DOWN;
		}
		else if(loc1.getZ() != loc2.getZ()) {
			return loc1.getZ() < loc2.getZ() ? BlockFace.SOUTH: BlockFace.NORTH;
		}

		return null;
	}
	
	private PowerResult canDraw(World world, GearblockLink link, List<Player> players) {
		ConfigManager configManager = CastleGates.getConfigManager();
		
		BlockFace blockFace = getLinkFace(link);;
		BlockCoord loc1 = link.getGearblock1().getCoord();
		BlockCoord loc2 = link.getGearblock2().getCoord();
		
		int x1 = loc1.getX(), y1 = loc1.getY(), z1 = loc1.getZ();
		int x2 = loc2.getX(), y2 = loc2.getY(), z2 = loc2.getZ();
		
		x1 += blockFace.getModX();
		y1 += blockFace.getModY();
		z1 += blockFace.getModZ();
		
		while(x1 != x2 || y1 != y2 || z1 != z2) {
			Block block = world.getBlockAt(x1, y1, z1);
			
			if(!configManager.isBridgeMaterial(block)) return new PowerResult(PowerResult.Status.Broken, block);
			
			if(this.gearblocks.containsKey(new BlockCoord(block))) return new PowerResult(PowerResult.Status.CannotDrawGear, block);
			
			if(!canAccessDoors(players, block.getLocation())) return PowerResult.NotInCitadelGroup;
			
			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}
		
		return PowerResult.Allowed;
	}

	private void draw(World world, GearblockLink link) {
		BlockFace blockFace = getLinkFace(link);
		BlockCoord loc1 = link.getGearblock1().getCoord();
		BlockCoord loc2 = link.getGearblock2().getCoord();
		
		int x1 = loc1.getX(), y1 = loc1.getY(), z1 = loc1.getZ();
		int x2 = loc2.getX(), y2 = loc2.getY(), z2 = loc2.getZ();
		
		x1 += blockFace.getModX();
		y1 += blockFace.getModY();
		z1 += blockFace.getModZ();
		
		ICitadelManager citadelManager = CastleGates.getCitadelManager();
		ArrayList<BlockState> blockStates = new ArrayList<BlockState>();
		ArrayList<Location> locations = new ArrayList<Location>();
		
		while(x1 != x2 || y1 != y2 || z1 != z2) {
			Block block = world.getBlockAt(x1, y1, z1);
			Location location = block.getLocation();
			
			locations.add(location);
			
			BlockState blockState = new BlockState(block);
			blockState.reinforcement = citadelManager.removeReinforcement(location);
			
			blockStates.add(blockState);
			
			DeprecatedMethods.setTypeIdAndData(block, Material.AIR, (byte)0);
			
			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}
		
		CastleGates.getOrebfuscatorManager().update(locations);
				
		link.setBlocks(blockStates);
	}

	private PowerResult canUndraw(World world, GearblockLink link, List<Player> players) {
		BlockFace blockFace = getLinkFace(link);
		BlockCoord loc1 = link.getGearblock1().getCoord();
		BlockCoord loc2 = link.getGearblock2().getCoord();
		
		int x1 = loc1.getX(), y1 = loc1.getY(), z1 = loc1.getZ();
		int x2 = loc2.getX(), y2 = loc2.getY(), z2 = loc2.getZ();
		
		x1 += blockFace.getModX();
		y1 += blockFace.getModY();
		z1 += blockFace.getModZ();
		
		List<Block> bridgeBlocks = new ArrayList<Block>();

		while(x1 != x2 || y1 != y2 || z1 != z2) {
			Block block = world.getBlockAt(x1, y1, z1); 
			
			if(block.getType() != Material.AIR) return new PowerResult(PowerResult.Status.Blocked, block);
			
			bridgeBlocks.add(block);
			
			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}
		
		return CastleGates.getBastionManager().canUndraw(players, bridgeBlocks)
				? PowerResult.Allowed
				: PowerResult.BastionBlocked; 
	}
	
	private void undraw(World world, GearblockLink link) {
		BlockFace blockFace = getLinkFace(link);
		BlockCoord loc1 = link.getGearblock1().getCoord();
		BlockCoord loc2 = link.getGearblock2().getCoord();
		
		int x1 = loc1.getX(), y1 = loc1.getY(), z1 = loc1.getZ();
		int x2 = loc2.getX(), y2 = loc2.getY(), z2 = loc2.getZ();
		
		x1 += blockFace.getModX();
		y1 += blockFace.getModY();
		z1 += blockFace.getModZ();
		
		ICitadelManager citadelManager = CastleGates.getCitadelManager();
		List<BlockState> blocks = link.getBlocks();
		int i = 0;
		
		while(x1 != x2 || y1 != y2 || z1 != z2) {
			BlockState blockState = blocks.get(i++);
			
			Block block = world.getBlockAt(x1, y1, z1);
			DeprecatedMethods.setTypeIdAndData(block, blockState.id, blockState.meta);
			citadelManager.createReinforcement(blockState.reinforcement, block.getLocation());
			
			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}
		
		link.setBlocks(null);
	}
	
	private static boolean canAccessDoors(List<Player> players, Location location) {
		return CastleGates.getCitadelManager().canAccessDoors(players, location);
	}
	
	private static boolean canAccessDoors(List<Player> players, World world, BlockCoord coord) {
		Location location = new Location(world, coord.getX(), coord.getY(), coord.getZ());
		return CastleGates.getCitadelManager().canAccessDoors(players, location);
	}
}