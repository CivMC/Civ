/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.engine.bridge;

import java.util.*;
import java.util.logging.Level;

import com.aleksey.castlegates.config.ConfigManager;
import com.aleksey.castlegates.database.ReinforcementInfo;
import com.aleksey.castlegates.engine.StorageManager;
import com.aleksey.castlegates.plugins.citadel.ICitadel;
import com.aleksey.castlegates.types.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.plugins.citadel.ICitadelManager;
import com.aleksey.castlegates.events.CastleGatesDrawGateEvent;
import com.aleksey.castlegates.events.CastleGatesUndrawGateEvent;
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

	public enum CreateResult { NotCreated, AlreadyExist, Created }
	public enum RemoveResult { NotExist, Removed, RemovedWithLink }
	public enum SearchBridgeBlockResult { NotFound, Bridge, Gates }
	public enum DoorAccess { None, Partial, Full }

	private StorageManager _storage;
	private Map<Gearblock, TimerBatch> _pendingTimerBatches;
	private TimerWorker _timerWorker;

	public void init(StorageManager storage) {
		_storage = storage;
		_pendingTimerBatches = new HashMap<>();

		_timerWorker = new TimerWorker(this);
		_timerWorker.startThread();
	}

	public void close() {
		if(_timerWorker != null) {
			_timerWorker.terminateThread();
			_timerWorker = null;
		}
	}

	public SearchBridgeBlockResult searchBridgeBlock(BlockCoord coord) {
		int maxLen = CastleGates.getConfigManager().getMaxBridgeLength();

		for(BlockFace face : faces) {
			BlockCoord current = coord.clone();

			for(int i = 0; i < maxLen; i++) {
				current.increment(face);

				Gearblock gearblock = _storage.getGearblock(current);

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

		return switch(face) {
			case EAST -> coord1.getX() < gearblock.getCoord().getX() || coord2.getX() < gearblock.getCoord().getX();
			case WEST -> coord1.getX() > gearblock.getCoord().getX() || coord2.getX() > gearblock.getCoord().getX();
			case UP -> coord1.getY() < gearblock.getCoord().getY() || coord2.getY() < gearblock.getCoord().getY();
			case DOWN -> coord1.getY() > gearblock.getCoord().getY() || coord2.getY() > gearblock.getCoord().getY();
			case SOUTH -> coord1.getZ() < gearblock.getCoord().getZ() || coord2.getZ() < gearblock.getCoord().getZ();
			case NORTH -> coord1.getZ() > gearblock.getCoord().getZ() || coord2.getZ() > gearblock.getCoord().getZ();
			default -> false;
		};
	}

	public CreateResult createGear(Block block) {
		BlockCoord location = new BlockCoord(block);

		if(!CastleGates.getConfigManager().isGearBlockType(block)) return CreateResult.NotCreated;
		if(_storage.hasGearblock(location)) return CreateResult.AlreadyExist;

		_storage.addGearblock(location);

		return CreateResult.Created;
	}

	public RemoveResult removeGear(BlockCoord location) {
		Gearblock gear = _storage.getGearblock(location);

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
				_storage.setLinkBroken(gear);
			} else {
				removeLink(gear.getLink());
				result = RemoveResult.RemovedWithLink;
			}
		}

		_storage.removeGearblock(gear);

		return result;
	}

	public void removeLink(GearblockLink link) {
		if(link == null)
			return;

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

		_storage.removeLink(link);
	}

	public boolean createLink(Gearblock gearblock1, Gearblock gearblock2, int distance) {
		if(!removeGearblocksLinks(gearblock1, gearblock2, distance)) return false;

		if(gearblock1.getLink() != null) return true;

		_storage.addLink(gearblock1, gearblock2);

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
			_storage.removeLink(link1);
		}

		if(link2 != null) {
			_storage.removeLink(link2);
		}

		return true;
	}

	public PowerResult processGearblock(World world, Gearblock gearblock, boolean isPowered, List<Player> players) {
		if(gearblock.isPowered() == isPowered)
			return PowerResult.Unchanged;

		if(!isPowered) {
			unlock(gearblock);
			gearblock.setPowered(false);
			addPendingTimerBatch(gearblock);
			return PowerResult.Unpowered;
		}

		if(System.currentTimeMillis() - gearblock.getLastSwitchTime() < CastleGates.getConfigManager().getSwitchTimeout()) {
			return PowerResult.Unchanged;
		}

		ICitadel citadel = getCitadel(world, gearblock, players);
		DoorAccess doorAccess = canAccessDoors(world, gearblock, citadel);

		switch(doorAccess) {
			case None: return PowerResult.NotInCitadelGroup;
			case Partial: return PowerResult.DifferentCitadelGroup;
		}

		if(extendDoorTimerBatch(gearblock))
			return PowerResult.Unchanged;

		if(!unlock(gearblock))
			return PowerResult.Locked;

		gearblock.setPowered(true);

		if(gearblock.getLink() != null)
		{
			PowerResult result = !gearblock.getLink().isDrawn()
				? canDraw(world, gearblock.getLink(), citadel)
				: canUndraw(world, gearblock.getLink(), players, citadel);

			if(result != PowerResult.Allowed)
				return result;
		}

		PowerResult result = powerGear(world, gearblock, players, citadel);

		gearblock.setLastSwitchTime();

		return result;
	}

	private void addPendingTimerBatch(Gearblock gearblock) {
		TimerBatch timerBatch = _pendingTimerBatches.get(gearblock);

		if(timerBatch != null && timerBatch.resetRunTime() && !timerBatch.isInvalid()) {
			_timerWorker.addBatch(timerBatch);
		}
	}

	private PowerResult powerGear(World world, Gearblock gearblock, List<Player> players, ICitadel citadel) {
		HashSet<Gearblock> gearblocks = new HashSet<>();
		gearblocks.add(gearblock);

		PowerResult transferResult = transferPower(world, gearblock, true, players, citadel, gearblocks);

		if(transferResult != PowerResult.Allowed)
			return transferResult;

		if(isLocked(gearblocks))
			return PowerResult.Locked;

		TimerBatch timerBatch = gearblock.getTimer() != null ? new TimerBatch(world, gearblock, gearblocks) : null;
		Boolean draw = powerGearList(world, gearblocks, timerBatch);

		lock(gearblock, gearblocks);

		if(draw == null)
			return PowerResult.Unchanged;

		PowerResult result = draw ? PowerResult.Drawn : PowerResult.Undrawn;

		if(timerBatch != null) {
			timerBatch.setProcessStatus(result.status);

			if(gearblock.getTimerMode() == TimerMode.DEFAULT) {
				_timerWorker.addBatch(timerBatch);
			} else {
				_pendingTimerBatches.put(gearblock, timerBatch);
			}
		}

		return result;
	}

	private Boolean powerGearList(World world, HashSet<Gearblock> gearblocks, TimerBatch timerBatch) {
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
		List<Gearblock> lockedGearblocks = new ArrayList<>();

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

	private boolean extendDoorTimerBatch(Gearblock gearblock) {
		if(gearblock.getTimer() == null
			|| gearblock.getTimerMode() != TimerMode.DOOR
			|| gearblock.getTimerBatch() == null
			|| gearblock.getTimerBatch().getTimerMode() != TimerMode.DOOR
			|| gearblock.getTimerBatch().isInvalid()
			)
		{
			return false;
		}

		TimerBatch originalTimerBatch = gearblock.getTimerBatch();
		boolean canLock = true;

		for(Gearblock current : originalTimerBatch.getAllGearblocks()) {
			if(current.getLockedGearblocks() != null && current != originalTimerBatch.getGearblock()
					|| current.getLockGearblock() != null && current.getLockGearblock() != originalTimerBatch.getGearblock()
				)
			{
				canLock = false;
				break;
			}
		}

		if(canLock) {
			gearblock.setPowered(true);

			originalTimerBatch.invalidate();

			unlock(originalTimerBatch.getGearblock());
			lock(gearblock, originalTimerBatch.getAllGearblocks());

			_pendingTimerBatches.put(gearblock, originalTimerBatch.clone(gearblock));
		}

		return true;
	}

	public boolean processTimerBatch(TimerBatch timerBatch) {
		timerBatch.clearTimerBatchForAllGearblocks();

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
			ICitadel citadel,
			HashSet<Gearblock> gearblocks
			)
	{
		ConfigManager configManager = CastleGates.getConfigManager();
		BlockCoord gearLoc = gearblock.getCoord();

		for(int i = 0; i < faces.length && gearblocks.size() <= configManager.getMaxPowerTransfers(); i++) {
			BlockFace face = faces[i];
			BlockCoord loc = new BlockCoord(world.getUID(), gearLoc.getX() + face.getModX(), gearLoc.getY() + face.getModY(), gearLoc.getZ() + face.getModZ());
			Gearblock to = _storage.getGearblock(loc);

			if(to == null || to.isPowered() == isPowered || gearblocks.contains(to)) continue;

			DoorAccess access = canAccessDoors(world, to, citadel);

			if(access == DoorAccess.Partial) {
				return PowerResult.DifferentCitadelGroup;
			} else if(access == DoorAccess.None) {
				continue;
			}

			if(isPowered) {
				GearblockLink link = to.getLink();

				if(link != null) {
					PowerResult result;

					if(!link.isDrawn()) {
						result = canDraw(world, link, citadel);
					} else {
						result = canUndraw(world, link, players, citadel);
					}

					if(result != PowerResult.Allowed) return result;
				}

				gearblocks.add(to);
			}

			PowerResult result = transferPower(world, to, isPowered, players, citadel, gearblocks);

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

	private PowerResult canDraw(World world, GearblockLink link, ICitadel citadel) {
		ConfigManager configManager = CastleGates.getConfigManager();

		BlockFace blockFace = getLinkFace(link);
		if (blockFace == null)
			return PowerResult.Locked;

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

			if(_storage.hasGearblock(new BlockCoord(block))) return new PowerResult(PowerResult.Status.CannotDrawGear, block);

			if(!citadel.canAccessDoors(block.getLocation())) return PowerResult.DifferentCitadelGroup;

			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}

		return PowerResult.Allowed;
	}

	private void draw(World world, GearblockLink link) {
		BlockFace blockFace = getLinkFace(link);
		if (blockFace == null)
			return;

		BlockCoord loc1 = link.getGearblock1().getCoord();
		BlockCoord loc2 = link.getGearblock2().getCoord();

		int x1 = loc1.getX(), y1 = loc1.getY(), z1 = loc1.getZ();
		int x2 = loc2.getX(), y2 = loc2.getY(), z2 = loc2.getZ();

		x1 += blockFace.getModX();
		y1 += blockFace.getModY();
		z1 += blockFace.getModZ();

		ICitadelManager citadelManager = CastleGates.getCitadelManager();
		ArrayList<BlockState> blockStates = new ArrayList<>();
		ArrayList<Location> locations = new ArrayList<>();
		ArrayList<org.bukkit.block.BlockState> states = new ArrayList<>();

		while(x1 != x2 || y1 != y2 || z1 != z2) {
			Block block = world.getBlockAt(x1, y1, z1);
			Location location = block.getLocation();

			locations.add(location);

			ReinforcementInfo reinforcement = citadelManager.removeReinforcement(location);
			BlockState blockState = new BlockState(block, reinforcement);

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
		
		_storage.setLinkBlocks(link, blockStates);
	}

	private PowerResult canUndraw(World world, GearblockLink link, List<Player> players, ICitadel citadel) {
		BlockFace blockFace = getLinkFace(link);
		if (blockFace == null)
			return PowerResult.Locked;

		BlockCoord loc1 = link.getGearblock1().getCoord();
		BlockCoord loc2 = link.getGearblock2().getCoord();

		int x1 = loc1.getX(), y1 = loc1.getY(), z1 = loc1.getZ();
		int x2 = loc2.getX(), y2 = loc2.getY(), z2 = loc2.getZ();

		x1 += blockFace.getModX();
		y1 += blockFace.getModY();
		z1 += blockFace.getModZ();

		List<Block> bridgeBlocks = new ArrayList<>();

		while(x1 != x2 || y1 != y2 || z1 != z2) {
			Block block = world.getBlockAt(x1, y1, z1);

			if(!Helper.isEmptyBlock(block))
				return new PowerResult(PowerResult.Status.Blocked, block);

			bridgeBlocks.add(block);

			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}

		return CastleGates.getBastionManager().canUndraw(players, bridgeBlocks, citadel)
				? PowerResult.Allowed
				: PowerResult.BastionBlocked;
	}

	private void undraw(World world, GearblockLink link) {
		BlockFace blockFace = getLinkFace(link);

		if (blockFace == null)
			return;

		BlockCoord loc1 = link.getGearblock1().getCoord();
		BlockCoord loc2 = link.getGearblock2().getCoord();

		int x1 = loc1.getX(), y1 = loc1.getY(), z1 = loc1.getZ();
		int x2 = loc2.getX(), y2 = loc2.getY(), z2 = loc2.getZ();

		x1 += blockFace.getModX();
		y1 += blockFace.getModY();
		z1 += blockFace.getModZ();

		ICitadelManager citadelManager = CastleGates.getCitadelManager();
		List<BlockState> linkBlocks = link.getBlocks();
		List<Block> worldBlocks = new ArrayList<>();
		ArrayList<Location> locations = new ArrayList<>();

		while(x1 != x2 || y1 != y2 || z1 != z2) {
			Block block = world.getBlockAt(x1, y1, z1);
			worldBlocks.add(block);

			Location location = block.getLocation();
			locations.add(location);

			x1 += blockFace.getModX();
			y1 += blockFace.getModY();
			z1 += blockFace.getModZ();
		}

		Bukkit.getPluginManager().callEvent(new CastleGatesUndrawGateEvent(locations));

		for (int i = 0; i < linkBlocks.size(); i++) {
			BlockState blockState = linkBlocks.get(i);
			BlockData blockData = Bukkit.createBlockData(blockState.getBlockData());
			Block block = worldBlocks.get(i);

			block.setBlockData(blockData, false);

			citadelManager.createReinforcement(blockState.getReinforcement(), block.getLocation());
		}

		// Apply physics
		for (Block block : worldBlocks)
			block.setBlockData(block.getBlockData(), true);

		_storage.setLinkBlocks(link, null);
	}

	private static DoorAccess canAccessDoors(World world, Gearblock gearblock, ICitadel citadel) {
		boolean hasAccess = citadel.canAccessDoors(getLocation(world, gearblock.getCoord()));

		if(!hasAccess) return DoorAccess.None;

		GearblockLink link = gearblock.getLink();

		if(link == null) return DoorAccess.Full;

		Gearblock secondGearblock = link.getGearblock1() == gearblock
				? link.getGearblock2()
				: link.getGearblock1();

		return citadel.canAccessDoors(getLocation(world, secondGearblock.getCoord())) ? DoorAccess.Full : DoorAccess.Partial;
	}

	private static Location getLocation(World world, BlockCoord coord) {
		return new Location(world, coord.getX(), coord.getY(), coord.getZ());
	}

	private static ICitadel getCitadel(World world, Gearblock gearblock, List<Player> players) {
		BlockCoord coord = gearblock.getCoord();
		Location location = new Location(world, coord.getX(), coord.getY(), coord.getZ());

		return CastleGates.getCitadelManager().getCitadel(players, location);
	}

	public boolean isNotSimpleGearblock(Gearblock gearblock, Player player) {
		World world = player.getWorld();
		BlockCoord coord = gearblock.getCoord();
		List<Player> players = new ArrayList<>();

		players.add(player);

		ICitadel citadel = getCitadel(world, gearblock, players);

		return hasAccessibleGearblock(world, coord.getForward(), players, citadel)
			|| hasAccessibleGearblock(world, coord.getBackward(), players, citadel)
			|| hasAccessibleGearblock(world, coord.getRight(), players, citadel)
			|| hasAccessibleGearblock(world, coord.getLeft(), players, citadel)
			|| hasAccessibleGearblock(world, coord.getTop(), players, citadel)
			|| hasAccessibleGearblock(world, coord.getBottom(), players, citadel);
	}

	private boolean hasAccessibleGearblock(World world, BlockCoord coord, List<Player> players, ICitadel originalCitadel) {
		Gearblock gearblock = _storage.getGearblock(coord);
		if(gearblock == null)
			return false;

		ICitadel citadel = getCitadel(world, gearblock, players);

		return originalCitadel.getGroupName() == null
				? citadel.getGroupName() == null
				: originalCitadel.getGroupName().equalsIgnoreCase(citadel.getGroupName());
	}
}