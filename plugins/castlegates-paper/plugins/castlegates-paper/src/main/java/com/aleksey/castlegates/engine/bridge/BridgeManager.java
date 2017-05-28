/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.engine.bridge;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import com.aleksey.castlegates.config.ConfigManager;
import com.aleksey.castlegates.engine.StorageManager;
import com.aleksey.castlegates.types.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.DeprecatedMethods;
import com.aleksey.castlegates.plugins.citadel.ICitadelManager;
import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.events.CastleGatesDrawGateEvent;
import com.aleksey.castlegates.events.CastleGatesUndrawGateEvent;
import com.aleksey.castlegates.utils.DataWorker;
import com.aleksey.castlegates.utils.Helper;
import com.aleksey.castlegates.utils.TimerWorker;

public class BridgeManager {
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

	private StorageManager storage;
	private Map<Gearblock, TimerBatch> pendingTimerBatches;
	private TimerWorker timerWorker;

	public void init(StorageManager storage) {
		this.storage = storage;
		this.pendingTimerBatches = new WeakHashMap<>();

		this.timerWorker = new TimerWorker(this);
		this.timerWorker.startThread();
	}

	public void close() {
		if(this.timerWorker != null) {
			this.timerWorker.terminateThread();
		}
	}

	public SearchBridgeBlockResult searchBridgeBlock(BlockCoord coord) {
		int maxLen = CastleGates.getConfigManager().getMaxBridgeLength();

		for(BlockFace face : faces) {
			BlockCoord current = coord;

			for(int i = 0; i < maxLen; i++) {
				current.increment(face);

				Gearblock gearblock = this.storage.getGearblock(current);

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
		if(this.storage.hasGearblock(location)) return CreateResult.AlreadyExist;

		this.storage.addGearblock(location);

		return CreateResult.Created;
	}

	public RemoveResult removeGear(BlockCoord location) {
		Gearblock gear = this.storage.getGearblock(location);

		if(gear == null) return RemoveResult.NotExist;

		RemoveResult result = RemoveResult.Removed;

		if(gear.getLockGearblock() != null) {
			unlock(gear.getLockGearblock());
		} else {
			unlock(gear);
		}

		if(gear.getBrokenLink() != null) {
			removeLink(gear.getBrokenLink());
			result = RemoveResult.RemovedWithLink;
		}
		else if (gear.getLink() != null) {
			if(gear.getLink().isDrawn()) {
				this.storage.setLinkBroken(gear);
			} else {
				removeLink(gear.getLink());
				result = RemoveResult.RemovedWithLink;
			}
		}

		this.storage.removeGearblock(gear);

		return result;
	}

	public void removeLink(GearblockLink link) {
		if(link == null) return;

		Gearblock gearblock1 = link.getGearblock1();

		if(gearblock1 != null) {
			if(gearblock1.getLockGearblock() != null) {
				unlock(gearblock1.getLockGearblock());
			} else {
				unlock(gearblock1);
			}
		}

		Gearblock gearblock2 = link.getGearblock2();

		if(gearblock2 != null) {
			if(gearblock2.getLockGearblock() != null) {
				unlock(gearblock2.getLockGearblock());
			} else {
				unlock(gearblock2);
			}
		}

		this.storage.removeLink(link);
	}

	public boolean createLink(Gearblock gearblock1, Gearblock gearblock2, int distance) {
		if(!removeGearblocksLinks(gearblock1, gearblock2, distance)) return false;

		if(gearblock1.getLink() != null) return true;

		this.storage.addLink(gearblock1, gearblock2);

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
			this.storage.removeLink(link1);
		}

		if(link2 != null) {
			this.storage.removeLink(link2);
		}

		return true;
	}

	public PowerResult processGearblock(World world, Gearblock gearblock, boolean isPowered, List<Player> players) {
		if(gearblock.isPowered() == isPowered) return PowerResult.Unchanged;

		if(!isPowered) {
			unlock(gearblock);
			gearblock.setPowered(false);
			addPendingTimerBatch(gearblock);
			return PowerResult.Unpowered;
		}

		if(System.currentTimeMillis() - gearblock.getLastSwitchTime() < CastleGates.getConfigManager().getSwitchTimeout()) {
			return PowerResult.Unchanged;
		}

		if(!canAccessDoors(players, world, gearblock.getCoord())) {
			return PowerResult.NotInCitadelGroup;
		}

		if(!unlock(gearblock)) {
			return PowerResult.Locked;
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

	private void addPendingTimerBatch(Gearblock gearblock) {
		TimerBatch timerBatch = this.pendingTimerBatches.get(gearblock);

		if(timerBatch != null) {
			if(timerBatch.resetRunTime()) {
				this.timerWorker.addBatch(timerBatch);
			}
		}
	}

	private PowerResult powerGear(World world, Gearblock gearblock, boolean isPowered, List<Player> players) {
		HashSet<Gearblock> gearblocks = new HashSet<Gearblock>();
		gearblocks.add(gearblock);

		PowerResult transferResult = transferPower(world, gearblock, isPowered, players, gearblocks);

		if(transferResult != PowerResult.Allowed) return transferResult;

		if(isLocked(gearblocks)) return PowerResult.Locked;

		TimerBatch timerBatch = gearblock.getTimer() != null ? new TimerBatch(world, gearblock) : null;
		Boolean draw = powerGearList(world, gearblocks, isPowered, players, timerBatch);

		lock(gearblock, gearblocks);

		if(draw == null) return PowerResult.Unchanged;

		PowerResult result = draw ? PowerResult.Drawn : PowerResult.Undrawn;

		if(timerBatch != null) {
			timerBatch.setProcessStatus(result.status);

			if(gearblock.getTimerMode() == TimerMode.DEFAULT) {
				this.timerWorker.addBatch(timerBatch);
			} else {
				this.pendingTimerBatches.put(gearblock, timerBatch);
			}
		}

		return result;
	}

	private Boolean powerGearList(
			World world,
			HashSet<Gearblock> gearblocks,
			boolean isPowered,
			List<Player> players,
			TimerBatch timerBatch)
	{
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

			if(timerBatch != null) {
				timerBatch.addLink(linkFromList);
			}
		}

		return draw;
	}

	private boolean unlock(Gearblock gearblock) {
		if(gearblock.getLockGearblock() != null) {
			return false;
		}

		if(gearblock.getLockedGearblocks() == null) {
			return true;
		}

		for(Gearblock lockedGearblock : gearblock.getLockedGearblocks()) {
			lockedGearblock.setLockGearblock(null);
		}

		gearblock.setLockedGearblocks(null);

		return true;
	}

	private void lock(Gearblock gearblock, HashSet<Gearblock> gearblocks) {
		List<Gearblock> lockedGearblocks = new ArrayList<Gearblock>();

		for(Gearblock lockedGearblock : gearblocks) {
			GearblockLink link = lockedGearblock.getLink();

			if(lockedGearblock != gearblock) {
				if(link != null) {
					lockedGearblocks.add(link.getGearblock1());
					lockedGearblocks.add(link.getGearblock2());

					link.getGearblock1().setLockGearblock(gearblock);
					link.getGearblock2().setLockGearblock(gearblock);
				} else {
					lockedGearblocks.add(lockedGearblock);
					lockedGearblock.setLockGearblock(gearblock);
				}
			} else if(link != null) {
				if(link.getGearblock1() != lockedGearblock) {
					lockedGearblocks.add(link.getGearblock1());
					link.getGearblock1().setLockGearblock(gearblock);
				} else {
					lockedGearblocks.add(link.getGearblock2());
					link.getGearblock2().setLockGearblock(gearblock);
				}
			}
		}

		gearblock.setLockedGearblocks(lockedGearblocks);
	}

	private boolean isLocked(HashSet<Gearblock> gearblocks) {
		for(Gearblock lockedGearblock : gearblocks) {
			if(lockedGearblock.getLockGearblock() != null) {
				return true;
			}
		}

		return false;
	}

	public boolean processTimerBatch(TimerBatch timerBatch) {
		boolean result = false;
		World world = timerBatch.getWorld();

		for(TimerLink timerLink : timerBatch.getLinks()) {
			GearblockLink link = timerLink.getLink();

			if(link.isRemoved() || link.isBroken() || link.isDrawn() == timerLink.isMustDraw()) {
				continue;
			}

			if(!link.isDrawn()) {
				draw(world, link);
			} else {
				undraw(world, link);
			}

			result = true;
		}

		return result;
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
			Gearblock to = this.storage.getGearblock(loc);

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

			if(this.storage.hasGearblock(new BlockCoord(block))) return new PowerResult(PowerResult.Status.CannotDrawGear, block);

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
		ArrayList<org.bukkit.block.BlockState> states = new ArrayList<org.bukkit.block.BlockState>();

		while(x1 != x2 || y1 != y2 || z1 != z2) {
			Block block = world.getBlockAt(x1, y1, z1);
			Location location = block.getLocation();

			locations.add(location);

			BlockState blockState = new BlockState(block);
			blockState.reinforcement = citadelManager.removeReinforcement(location);

			blockStates.add(blockState);
			
			org.bukkit.block.BlockState state = block.getState();
			
			state.setType(Material.AIR);
			states.add(state);

			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}

		// Call our event before committing world changes.
		Bukkit.getPluginManager().callEvent(new CastleGatesDrawGateEvent(locations));

		// commit world changes
		for (org.bukkit.block.BlockState state : states) {
			state.update(true, true);
		}
		
		CastleGates.getOrebfuscatorManager().update(locations);

		this.storage.setLinkBlocks(link, blockStates);
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

			if(!Helper.isEmptyBlock(block)) return new PowerResult(PowerResult.Status.Blocked, block);

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

		ArrayList<Location> locations = new ArrayList<Location>();
		ArrayList<org.bukkit.block.BlockState> states = new ArrayList<org.bukkit.block.BlockState>();

		while(x1 != x2 || y1 != y2 || z1 != z2) {
			BlockState blockState = blocks.get(i++);

			Block block = world.getBlockAt(x1, y1, z1);
			Location location = block.getLocation();

			locations.add(location);

			// stage world changes
			states.add(DeprecatedMethods.toCraftBukkit(block, blockState));
			citadelManager.createReinforcement(blockState.reinforcement, location);

			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}
		Bukkit.getPluginManager().callEvent(new CastleGatesUndrawGateEvent(locations));
		
		// post-event, commit world changes.
		for (org.bukkit.block.BlockState state : states) {
			DeprecatedMethods.commitCraftBukkit(state);
		}

		this.storage.setLinkBlocks(link, null);
	}

	private static boolean canAccessDoors(List<Player> players, Location location) {
		return CastleGates.getCitadelManager().canAccessDoors(players, location);
	}

	private static boolean canAccessDoors(List<Player> players, World world, BlockCoord coord) {
		Location location = new Location(world, coord.getX(), coord.getY(), coord.getZ());
		return CastleGates.getCitadelManager().canAccessDoors(players, location);
	}
}