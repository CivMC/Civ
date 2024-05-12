/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import com.aleksey.castlegates.CastleGates;
import com.aleksey.castlegates.database.GearblockInfo;
import com.aleksey.castlegates.database.GearblockSource;
import com.aleksey.castlegates.database.LinkInfo;
import com.aleksey.castlegates.database.LinkSource;
import com.aleksey.castlegates.database.ReinforcementInfo;
import com.aleksey.castlegates.database.ReinforcementSource;
import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.types.*;

public class DataWorker extends Thread implements Runnable {
	private static final int DRAW_CODE = 0;
	private static final int UNDRAW_CODE = 1;
	private static final int REVERT_CODE = 2;
	private static final int OPERATION_MASK = 3;
	private static final int MODE_MASK = 4;

	private final SqlDatabase _db;
	private final GearblockSource _gearblockSource;
	private final LinkSource _linkSource;
	private final ReinforcementSource _reinforcementSource;

	private final Map<Gearblock, GearblockForUpdate> _changedGearblocks = new HashMap<>();
	private final Map<GearblockLink, LinkForUpdate> _changedLinks = new HashMap<>();

	private final ArrayList<GearblockForUpdate> _localChangedGearblocks = new ArrayList<>();
	private final ArrayList<LinkForUpdate> _localChangedLinks = new ArrayList<>();

	private long _lastExecute = System.currentTimeMillis();
    private final AtomicBoolean _kill = new AtomicBoolean(false);
	private final AtomicBoolean _run = new AtomicBoolean(false);

    public DataWorker(SqlDatabase db) {
    	_db = db;
		_gearblockSource = new GearblockSource(db);
		_linkSource = new LinkSource(db);
		_reinforcementSource = new ReinforcementSource(db);
	}

	public void close() {
		terminateThread();
	}

	public Map<BlockCoord, Gearblock> load() throws SQLException {
		Map<BlockCoord, Gearblock> gearblocks = new HashMap<>();
		Map<Integer, Gearblock> gearblocksById = new HashMap<>();
		Map<Integer, GearblockLink> linksById = new HashMap<>();

		loadGears(gearblocks, gearblocksById);
		loadLinks(linksById, gearblocksById);
		loadReinforcements(linksById);

		return gearblocks;
	}

	private void loadGears(Map<BlockCoord, Gearblock> gearblocks, Map<Integer, Gearblock> gearblocksById) throws SQLException {
    	int gearblockCount = _gearblockSource.countAll();

		CastleGates.getPluginLogger().log(Level.INFO, "Gearblock count: " + gearblockCount);

		List<GearblockInfo> gearData = _gearblockSource.selectAll();
		int timerCount = 0;

		for(GearblockInfo info : gearData) {
			UUID world = UUID.fromString(info.WorldId);
			BlockCoord location = new BlockCoord(world, info.X, info.Y, info.Z);
			Gearblock gearblock = new Gearblock(location);

			TimerOperation timerOperation = null;
			TimerMode timerMode = null;

			if(info.TimerOperation != null) {
				timerMode = (info.TimerOperation & MODE_MASK) != 0 ? TimerMode.DOOR : TimerMode.DEFAULT;

				switch(info.TimerOperation & OPERATION_MASK) {
					case DRAW_CODE -> timerOperation = TimerOperation.DRAW;
					case UNDRAW_CODE -> timerOperation = TimerOperation.UNDRAW;
					case REVERT_CODE -> timerOperation = TimerOperation.REVERT;
				}

				timerCount++;
			}

			gearblock.setId(info.GearblockId);
			gearblock.setTimer(info.Timer, timerOperation, timerMode);

			gearblocks.put(location, gearblock);
			gearblocksById.put(info.GearblockId, gearblock);
		}

		CastleGates.getPluginLogger().log(Level.INFO, "Loaded gearblocks: " + gearblocks.size());
		CastleGates.getPluginLogger().log(Level.INFO, "Loaded gearblocksById: " + gearblocksById.size());
		CastleGates.getPluginLogger().log(Level.INFO, "Timers: " + timerCount);
	}

	private void loadLinks(Map<Integer, GearblockLink> linksById, Map<Integer, Gearblock> gearblocksById) throws SQLException {
    	int linkCount = _linkSource.countAll();

		CastleGates.getPluginLogger().log(Level.INFO, "Link count: " + linkCount);

		List<LinkInfo> linkData = _linkSource.selectAll();
		List<LinkInfo> invalidLinks = new ArrayList<>();
		int drawnLinkCount = 0, brokenLinkCount = 0;

		for(LinkInfo info : linkData) {
			Gearblock gearblock1 = info.StartGearblockId != null ? gearblocksById.get(info.StartGearblockId) : null;
			Gearblock gearblock2 = info.EndGearblockId != null ? gearblocksById.get(info.EndGearblockId) : null;
			List<BlockState> blocks = deserializeBlocks(info.Blocks);
			GearblockLink link = new GearblockLink(gearblock1, gearblock2);

			link.setId(info.LinkId);
			link.setBlocks(blocks);

			if(gearblock1 != null) {
				gearblock1.setLink(link);
			}

			if(gearblock2 != null) {
				gearblock2.setLink(link);
			}

			linksById.put(link.getId(), link);

			if(gearblock1 == null && gearblock2 == null) {
				invalidLinks.add(info);
			}

			if(link.isDrawn()) {
				drawnLinkCount++;
			}

			if(link.isBroken()) {
				brokenLinkCount++;
			}
		}

		CastleGates.getPluginLogger().log(Level.INFO, "Loaded links: " + linkData.size());

		if(drawnLinkCount > 0) {
			CastleGates.getPluginLogger().log(Level.INFO, "Links in DRAWN state: " + drawnLinkCount);
		}

		if(brokenLinkCount > 0) {
			CastleGates.getPluginLogger().log(Level.INFO, "Links in BROKEN state: " + brokenLinkCount);
		}

		if(invalidLinks.size() > 0) {
			CastleGates.getPluginLogger().log(Level.WARNING, "Invalid links (i.e. BUG): " + invalidLinks.size());

			String filePath = new InvalidLinkLogger().write(invalidLinks);

			CastleGates.getPluginLogger().log(Level.INFO, "List of invalid links saved to: " + filePath);
		}
	}

	private void loadReinforcements(Map<Integer, GearblockLink> linksById) throws SQLException {
		List<ReinforcementInfo> reinforcementData = _reinforcementSource.selectAll();
		GearblockLink link = null;

		for(ReinforcementInfo info : reinforcementData) {
			if(link == null || link.getId() != info.LinkId) {
				link = linksById.get(info.LinkId);
			}

			BlockState blockState = link.getBlocks().get(info.BlockSequence);
			blockState.setReinforcement(info);
		}
	}

    public void startThread() {
		_kill.set(false);

        setName("CastleGates DataWorker Thread");
        setPriority(Thread.MIN_PRIORITY);
        start();

        CastleGates.getPluginLogger().log(Level.INFO, "DataWorker thread started");
    }

    public void terminateThread() {
        _kill.set(true);

		while (_run.get()) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		saveData();

		CastleGates.getPluginLogger().log(Level.INFO, "DataWorker thread stopped");
    }

    public void run() {
		_run.set(true);

		try {
			while (!isInterrupted() && !_kill.get()) {
				try {
					long timeWait = _lastExecute + CastleGates.getConfigManager().getDataWorkerRate() - System.currentTimeMillis();
					_lastExecute = System.currentTimeMillis();
					if (timeWait > 0) {
						Thread.sleep(timeWait);
					}

					saveData();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} finally {
			_run.set(false);
		}
    }

    private void saveData() {
		try {
			synchronized (_changedGearblocks) {
				_localChangedGearblocks.addAll(_changedGearblocks.values());
				_changedGearblocks.clear();
			}

			synchronized (_changedLinks) {
				_localChangedLinks.addAll(_changedLinks.values());
				_changedLinks.clear();
			}

			if (_localChangedGearblocks.size() > 0 || _localChangedLinks.size() > 0) {
				if (_db.checkConnection()) {
					try {
						updateGears();
						updateLinks();
					} finally {
						_localChangedGearblocks.clear();
						_localChangedLinks.clear();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private void updateGears() throws SQLException {
    	for(GearblockForUpdate gearForUpdate : _localChangedGearblocks) {
    		if(gearForUpdate.original.isRemoved()) {
    			if(gearForUpdate.original.getId() != 0) {
    				_gearblockSource.delete(gearForUpdate.original.getId());
    			}
    		}
    		else if(gearForUpdate.original.getId() != 0) {
    			_gearblockSource.update(gearForUpdate.info);
    		}
    		else {
    			_gearblockSource.insert(gearForUpdate.info);
    			gearForUpdate.original.setId(gearForUpdate.info.GearblockId);
    		}
    	}
    }

    private void updateLinks() throws SQLException {
    	for(LinkForUpdate linkForUpdate : _localChangedLinks) {
    		if(linkForUpdate.original.isRemoved()) {
    			if(linkForUpdate.original.getId() != 0) {
    				_reinforcementSource.deleteByLinkId(linkForUpdate.original.getId());
    				_linkSource.delete(linkForUpdate.original.getId());
    			}
    		} else {
	    		LinkInfo info = new LinkInfo();
	    		info.LinkId = linkForUpdate.original.getId();
	    		info.StartGearblockId = linkForUpdate.gearblock1 != null ? linkForUpdate.gearblock1.getId(): null;
	    		info.EndGearblockId = linkForUpdate.gearblock2 != null ? linkForUpdate.gearblock2.getId(): null;
	    		info.Blocks = linkForUpdate.blocks;

	    		if(info.LinkId != 0) {
	    			_linkSource.update(info);

	    			if(info.Blocks == null) {
	    				_reinforcementSource.deleteByLinkId(info.LinkId);
	    			}
	    		} else {
		    		_linkSource.insert(info);
		    		linkForUpdate.original.setId(info.LinkId);
	    		}

	    		if(linkForUpdate.reinforcements != null) {
		    		for(ReinforcementInfo reinforcement : linkForUpdate.reinforcements) {
		    			if(reinforcement.LinkId != 0) continue;

		    			reinforcement.LinkId = info.LinkId;

		    			_reinforcementSource.insert(reinforcement);
		    		}
	    		}
    		}
    	}
    }

    public void addChangedGearblock(Gearblock gearblock) {
		GearblockInfo info;

		if(gearblock.isRemoved()) {
			info = null;
		} else {
			info = new GearblockInfo();

			BlockCoord location = gearblock.getCoord();
			info.GearblockId = gearblock.getId();
			info.WorldId = location.getWorldUID().toString();
			info.X = location.getX();
			info.Y = location.getY();
			info.Z = location.getZ();
			info.Timer = gearblock.getTimer();

			if(gearblock.getTimerOperation() != null) {
				switch(gearblock.getTimerOperation()) {
					case DRAW -> info.TimerOperation = DRAW_CODE;
					case UNDRAW -> info.TimerOperation = UNDRAW_CODE;
					case REVERT -> info.TimerOperation = REVERT_CODE;
				}

				if(gearblock.getTimerMode() == TimerMode.DOOR) {
					info.TimerOperation |= MODE_MASK;
				}
			}
		}

		GearblockForUpdate gearForUpdate = new GearblockForUpdate();
		gearForUpdate.original = gearblock;
		gearForUpdate.info = info;

		synchronized(_changedGearblocks) {
			_changedGearblocks.put(gearblock, gearForUpdate);
		}
	}

	public void addChangedLink(GearblockLink link) {
		LinkForUpdate linkForUpdate = new LinkForUpdate();
		linkForUpdate.original = link;

		if(!link.isRemoved()) {
			linkForUpdate.gearblock1 = link.getGearblock1();
			linkForUpdate.gearblock2 = link.getGearblock2();
			linkForUpdate.blocks = serializeBlocks(link);

			if(link.getBlocks() != null) {
				linkForUpdate.reinforcements = new ArrayList<>();

				for(int i = 0; i < link.getBlocks().size(); i++) {
					ReinforcementInfo reinforcement = link.getBlocks().get(i).getReinforcement();

					if(reinforcement != null && reinforcement.LinkId == 0) {
						reinforcement.BlockSequence = i;
						linkForUpdate.reinforcements.add(reinforcement);
					}
				}
			}
		}

		synchronized(_changedLinks) {
			_changedLinks.put(link, linkForUpdate);
		}
	}

	private static String serializeBlocks(GearblockLink link) {
		if(link.getBlocks() == null)
			return null;

		int length = 0;
		for(BlockState block : link.getBlocks())
			length += block.getBlockDataLen();

		StringBuilder blockSerialized = new StringBuilder(length);
		for(BlockState block : link.getBlocks())
			block.serialize(blockSerialized);

		return blockSerialized.toString();
	}

	public static List<BlockState> deserializeBlocks(String blockDataList) {
		if(blockDataList == null || blockDataList.length() == 0)
			return null;

		List<BlockState> blocks = new ArrayList<>();
		int len = blockDataList.length();
		int startIndex = 0;
		int index = 0;

		while (index < len) {
			if (blockDataList.charAt(index) == BlockState.Separator) {
				String blockData = blockDataList.substring(startIndex, index);
				BlockState block = BlockState.deserialize(blockData);

				blocks.add(block);

				startIndex = index + 1;
			}

			index++;
		}

		return blocks;
	}
}
