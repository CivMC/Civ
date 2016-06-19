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
import com.aleksey.castlegates.types.GearLink;
import com.aleksey.castlegates.types.GearState;
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
	public static enum PowerResult { NotLinked, Unchanged, Unpowered, Blocked, Brocken, Drawn, Undrawn, CannotDrawGear, NotInCitadelGroup, BastionBlocked, Allowed }

	private Map<BlockCoord, GearState> gears;
	private DataWorker dataWorker;
	
	public void init(SqlDatabase db) throws SQLException {
		this.dataWorker = new DataWorker(db);
		this.gears = this.dataWorker.load();
		
		CastleGates.getPluginLogger().log(Level.INFO, "Loaded " + this.gears.size() + " gears");
		
		this.dataWorker.startThread();
	}
	
	public void close() {
		if(this.dataWorker != null) {
			this.dataWorker.close();
		}
	}
	
	public CreateResult createGear(Block block) {
		BlockCoord location = new BlockCoord(block);
		
		if(!CastleGates.getConfigManager().isGearBlockType(block)) return CreateResult.NotCreated;
		if(this.gears.containsKey(location)) return CreateResult.AlreadyExist;
		
		GearState gear = new GearState(location);
		
		this.gears.put(location, gear);
		
		this.dataWorker.addChangedGear(gear);
		
		return CreateResult.Created;
	}
	
	public RemoveResult removeGear(BlockCoord location) {
		GearState gear = this.gears.get(location);
		
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
		
		this.gears.remove(location);
		
		gear.setRemoved();
		
		this.dataWorker.addChangedGear(gear);
		
		return result;
	}
	
	public GearState getGear(BlockCoord location) {
		return this.gears.get(location);
	}
	
	public void removeLink(GearLink link) {
		if(link == null) return;
		
		link.setRemoved();
		
		if(link.getGear1() != null) {
			link.getGear1().setLink(null);
		}
		
		if(link.getGear2() != null) {
			link.getGear2().setLink(null);
		}

		this.dataWorker.addChangedLink(link);
	}
	
	public boolean createLink(GearState gear1, GearState gear2, int distance) {
		if(!removeGearsLinks(gear1, gear2, distance)) return false;
		
		if(gear1.getLink() != null) return true;
		
		GearLink link = new GearLink(gear1, gear2);
		
		if(gear1.getBrokenLink() != null) {
			link = gear1.getBrokenLink();
			link.setRestored(gear2);
		}
		else if(gear2.getBrokenLink() != null) {
			link = gear2.getBrokenLink();
			link.setRestored(gear1);
		}
		else {
			link = new GearLink(gear1, gear2);
		}
		
		gear1.setLink(link);
		gear2.setLink(link);
		
		this.dataWorker.addChangedLink(link);
		
		return true;
	}
	
	private boolean removeGearsLinks(GearState gear1, GearState gear2, int distance) {
		if(gear1.getBrokenLink() != null && gear2.getBrokenLink() != null
				|| gear1.getBrokenLink() != null && distance != gear1.getBrokenLink().getBlocks().size()
				|| gear2.getBrokenLink() != null && distance != gear2.getBrokenLink().getBlocks().size()
				)
		{
			return false;
		}

		GearLink link1 = gear1.getLink();
		GearLink link2 = gear2.getLink();
		
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
	
	public PowerResult processGear(World world, GearState gear, boolean isPowered, List<Player> players, Block mainGearBlock) {
		if(gear.isPowered() == isPowered) return PowerResult.Unchanged;
		
		if(!isPowered) {
			gear.setPowered(false);
			return PowerResult.Unpowered;
		}
		
		if(System.currentTimeMillis() - gear.getLastSwitchTime() < CastleGates.getConfigManager().getSwitchTimeout()) {
			return PowerResult.Unchanged;
		}
		
		if(!canInteract(players, world, gear.getCoord())) {
			return PowerResult.NotInCitadelGroup;
		}
		
		gear.setPowered(true);
		
		if(gear.getLink() != null)
		{
			PowerResult result;
			
			if(!gear.getLink().isDrawn()) {
				result = canDraw(world, gear.getLink(), players);
			} else {
				result = canUndraw(world, gear.getLink(), players);			
			}
			
			if(result != PowerResult.Allowed) return result;
		}
		
		PowerResult result = powerGear(world, gear, isPowered, players);
		
		gear.setLastSwitchTime();
		
		return result;
	}
	
	private PowerResult powerGear(World world, GearState gear, boolean isPowered, List<Player> players) {
		HashSet<GearState> gears = new HashSet<GearState>();
		gears.add(gear);
		
		PowerResult result = transferPower(world, gear, isPowered, players, gears);
		
		if(result != PowerResult.Allowed) return result;

		Boolean draw = null;

		for(GearState gearFromList : gears) {
			GearLink linkFromList = gearFromList.getLink();
			
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
		
		return draw == null || (boolean)draw ? PowerResult.Drawn : PowerResult.Undrawn;
	}
	
	private PowerResult transferPower(
			World world,
			GearState gear,
			boolean isPowered,
			List<Player> players,
			HashSet<GearState> gears
			)
	{
		ConfigManager configManager = CastleGates.getConfigManager();
		BlockCoord gearLoc = gear.getCoord();

		for(int i = 0; i < faces.length && gears.size() <= configManager.getMaxPowerTransfers(); i++) {
			BlockFace face = faces[i];
			BlockCoord loc = new BlockCoord(world.getUID(), gearLoc.getX() + face.getModX(), gearLoc.getY() + face.getModY(), gearLoc.getZ() + face.getModZ());
			GearState to = this.gears.get(loc);
			
			if(to == null || to.isPowered() == isPowered || gears.contains(to)) continue;
			
			if(!canInteract(players, world, loc)) return PowerResult.NotInCitadelGroup;
			
			if(isPowered) {
				GearLink link = to.getLink();
				
				if(link != null) {
					PowerResult result;
					
					if(!link.isDrawn()) {
						result = canDraw(world, link, players);
					} else {
						result = canUndraw(world, link, players);
					}
					
					if(result != PowerResult.Allowed) return result;
				}
				
				gears.add(to);
			}
			
			PowerResult result = transferPower(world, to, isPowered, players, gears);
			
			if(result != PowerResult.Allowed) return result;
		}
		
		return PowerResult.Allowed;
	}
	
	private BlockFace getLinkFace(GearLink link) {
		BlockCoord loc1 = link.getGear1().getCoord();
		BlockCoord loc2 = link.getGear2().getCoord();
		
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
	
	private PowerResult canDraw(World world, GearLink link, List<Player> players) {
		ConfigManager configManager = CastleGates.getConfigManager();
		
		BlockFace blockFace = getLinkFace(link);;
		BlockCoord loc1 = link.getGear1().getCoord();
		BlockCoord loc2 = link.getGear2().getCoord();
		
		int x1 = loc1.getX(), y1 = loc1.getY(), z1 = loc1.getZ();
		int x2 = loc2.getX(), y2 = loc2.getY(), z2 = loc2.getZ();
		
		x1 += blockFace.getModX();
		y1 += blockFace.getModY();
		z1 += blockFace.getModZ();
		
		while(x1 != x2 || y1 != y2 || z1 != z2) {
			Block block = world.getBlockAt(x1, y1, z1);
			
			if(!configManager.isBridgeMaterial(block)) return PowerResult.Brocken;
			
			if(this.gears.containsKey(new BlockCoord(block))) return PowerResult.CannotDrawGear;
			
			if(!canInteract(players, block.getLocation())) return PowerResult.NotInCitadelGroup;
			
			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}
		
		return PowerResult.Allowed;
	}

	private void draw(World world, GearLink link) {
		BlockFace blockFace = getLinkFace(link);
		BlockCoord loc1 = link.getGear1().getCoord();
		BlockCoord loc2 = link.getGear2().getCoord();
		
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

	private PowerResult canUndraw(World world, GearLink link, List<Player> players) {
		BlockFace blockFace = getLinkFace(link);
		BlockCoord loc1 = link.getGear1().getCoord();
		BlockCoord loc2 = link.getGear2().getCoord();
		
		int x1 = loc1.getX(), y1 = loc1.getY(), z1 = loc1.getZ();
		int x2 = loc2.getX(), y2 = loc2.getY(), z2 = loc2.getZ();
		
		x1 += blockFace.getModX();
		y1 += blockFace.getModY();
		z1 += blockFace.getModZ();
		
		HashSet<Block> bridgeBlocks = new HashSet<Block>();

		while(x1 != x2 || y1 != y2 || z1 != z2) {
			Block block = world.getBlockAt(x1, y1, z1); 
			
			if(block.getType() != Material.AIR) return PowerResult.Blocked;
			
			bridgeBlocks.add(block);
			
			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}
		
		return CastleGates.getBastionManager().canUndraw(players, bridgeBlocks)
				? PowerResult.Allowed
				: PowerResult.BastionBlocked; 
	}
	
	private void undraw(World world, GearLink link) {
		BlockFace blockFace = getLinkFace(link);
		BlockCoord loc1 = link.getGear1().getCoord();
		BlockCoord loc2 = link.getGear2().getCoord();
		
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
	
	private static boolean canInteract(List<Player> players, Location location) {
		return CastleGates.getCitadelManager().canInteract(players, location);
	}
	
	private static boolean canInteract(List<Player> players, World world, BlockCoord coord) {
		Location location = new Location(world, coord.getX(), coord.getY(), coord.getZ());
		return CastleGates.getCitadelManager().canInteract(players, location);
	}
}