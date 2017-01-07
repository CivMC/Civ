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
import java.util.WeakHashMap;
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
import com.aleksey.castlegates.types.BlockCoord;
import com.aleksey.castlegates.types.BlockState;
import com.aleksey.castlegates.types.Gearblock;
import com.aleksey.castlegates.types.GearblockForUpdate;
import com.aleksey.castlegates.types.GearblockLink;
import com.aleksey.castlegates.types.LinkForUpdate;
import com.aleksey.castlegates.types.TimerOperation;

public class DataWorker extends Thread implements Runnable {
	private static final int DRAW_CODE = 0;
	private static final int UNDRAW_CODE = 1;
	private static final int REVERT_CODE = 2;
	
	private SqlDatabase db;
	private GearblockSource gearblockSource;
	private LinkSource linkSource;
	private ReinforcementSource reinforcementSource;
	private ChangeLogger changeLogger;

	private Map<Gearblock, GearblockForUpdate> changedGearblocks = new WeakHashMap<Gearblock, GearblockForUpdate>();
	private Map<GearblockLink, LinkForUpdate> changedLinks = new WeakHashMap<GearblockLink, LinkForUpdate>();

	private ArrayList<GearblockForUpdate> localChangedGearblocks = new ArrayList<GearblockForUpdate>();
	private ArrayList<LinkForUpdate> localChangedLinks = new ArrayList<LinkForUpdate>();

	private long lastExecute = System.currentTimeMillis();
    private AtomicBoolean kill = new AtomicBoolean(false);

    public DataWorker(SqlDatabase db, boolean logChanges) {
    	this.db = db;
		this.gearblockSource = new GearblockSource(db);
		this.linkSource = new LinkSource(db);
		this.reinforcementSource = new ReinforcementSource(db);
		this.changeLogger = logChanges ? new ChangeLogger(): null;
	}
	
	public void close() {
		terminateThread();
		
		if(this.changeLogger != null) {
			this.changeLogger.close();
			this.changeLogger = null;
		}
	}
	
	public Map<BlockCoord, Gearblock> load() throws SQLException {
		Map<BlockCoord, Gearblock> gearblocks = new WeakHashMap<BlockCoord, Gearblock>();
		Map<Integer, Gearblock> gearblocksById = new WeakHashMap<Integer, Gearblock>();
		Map<Integer, GearblockLink> linksById = new WeakHashMap<Integer, GearblockLink>();
		
		loadGears(gearblocks, gearblocksById);
		loadLinks(linksById, gearblocksById);
		loadReinforcements(linksById);
		
		return gearblocks;
	}
	
	private void loadGears(Map<BlockCoord, Gearblock> gearblocks, Map<Integer, Gearblock> gearblocksById) throws SQLException {
		List<GearblockInfo> gearData = this.gearblockSource.selectAll();
		
		for(GearblockInfo info : gearData) {
			UUID world = UUID.fromString(info.location_worlduid);
			BlockCoord location = new BlockCoord(world, info.location_x, info.location_y, info.location_z);
			Gearblock gearblock = new Gearblock(location);
			
			TimerOperation timerOperation = null;
			
			if(info.timerOperation != null) {
				switch(info.timerOperation) {
				case DRAW_CODE:
					timerOperation = TimerOperation.DRAW;
					break;
				case UNDRAW_CODE:
					timerOperation = TimerOperation.UNDRAW;
					break;
				case REVERT_CODE:
					timerOperation = TimerOperation.REVERT;
					break;
				}
			}
			
			gearblock.setId(info.gearblock_id);
			gearblock.setTimer(info.timer, timerOperation);
			
			gearblocks.put(location, gearblock);
			gearblocksById.put(info.gearblock_id, gearblock);
		}
	}
	
	private void loadLinks(Map<Integer, GearblockLink> linksById, Map<Integer, Gearblock> gearblocksById) throws SQLException {
		List<LinkInfo> linkData = this.linkSource.selectAll();
		
		for(LinkInfo info : linkData) {
			Gearblock gearblock1 = info.gearblock1_id != null ? gearblocksById.get(info.gearblock1_id) : null;
			Gearblock gearblock2 = info.gearblock2_id != null ? gearblocksById.get(info.gearblock2_id) : null;
			List<BlockState> blocks = deserializeBlocks(info.blocks);
			GearblockLink link = new GearblockLink(gearblock1, gearblock2);
			
			link.setId(info.link_id);
			link.setBlocks(blocks);
			
			if(gearblock1 != null) {
				gearblock1.setLink(link);
			}

			if(gearblock2 != null) {
				gearblock2.setLink(link);
			}
			
			linksById.put(link.getId(), link);
		}
	}
	
	private void loadReinforcements(Map<Integer, GearblockLink> linksById) throws SQLException {
		List<ReinforcementInfo> reinforcementData = this.reinforcementSource.selectAll();
		GearblockLink link = null;
		
		for(ReinforcementInfo info : reinforcementData) {
			if(link == null || link.getId() != info.link_id) {
				link = linksById.get(info.link_id);
			}
			
			BlockState blockState = link.getBlocks().get(info.block_no);
			blockState.reinforcement = info;
		}
	}
	
    public void startThread() {
        setName("CastleGates DataWorker Thread");
        setPriority(Thread.MIN_PRIORITY);
        start();
        
        CastleGates.getPluginLogger().log(Level.INFO, "DataWorker thread started");
    }

    public void terminateThread() {
        this.kill.set(true);
    }
		
    public void run() {
        while (!this.isInterrupted() && !this.kill.get()) {
            try {
                long timeWait = lastExecute + CastleGates.getConfigManager().getDataWorkerRate() - System.currentTimeMillis();
                lastExecute = System.currentTimeMillis();
                if (timeWait > 0) {
                    Thread.sleep(timeWait);
                }
                
                synchronized (this.changedGearblocks) {
                	for(GearblockForUpdate gearForUpdate : this.changedGearblocks.values()) {
                		this.localChangedGearblocks.add(gearForUpdate);
                	}

                	this.changedGearblocks.clear();
                }
                
                synchronized (this.changedLinks) {
                	for(LinkForUpdate linkForUpdate : this.changedLinks.values()) {
                		this.localChangedLinks.add(linkForUpdate);
                	}

                	this.changedLinks.clear();
                }
                
                if(this.localChangedGearblocks.size() > 0 || this.localChangedLinks.size() > 0) {
                	if(this.db.checkConnection()) {
		                try {
			                updateGears();
			                updateLinks();
			                
			                if(this.changeLogger != null) {
			                	this.changeLogger.flush();
			                }
		                } finally {
			                this.localChangedGearblocks.clear();
			                this.localChangedLinks.clear();
		                }
                	}
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    }
    
    private void updateGears() throws SQLException {
    	for(GearblockForUpdate gearForUpdate : this.localChangedGearblocks) {
    		if(this.changeLogger != null) {
    			this.changeLogger.write(gearForUpdate);
    		}
    		
    		if(gearForUpdate.original.isRemoved()) {
    			if(gearForUpdate.original.getId() != 0) {
    				this.gearblockSource.delete(gearForUpdate.original.getId());
    			}
    		}
    		else if(gearForUpdate.original.getId() != 0) {
    			this.gearblockSource.update(gearForUpdate.info);
    		}
    		else {
    			this.gearblockSource.insert(gearForUpdate.info);
    			gearForUpdate.original.setId(gearForUpdate.info.gearblock_id);
    		}
    	}
    }
    
    private void updateLinks() throws SQLException {
    	for(LinkForUpdate linkForUpdate : this.localChangedLinks) {
    		if(this.changeLogger != null) {
    			this.changeLogger.write(linkForUpdate);
    		}
    		
    		if(linkForUpdate.original.isRemoved()) {
    			if(linkForUpdate.original.getId() != 0) {
    				this.reinforcementSource.deleteByLinkId(linkForUpdate.original.getId());
    				this.linkSource.delete(linkForUpdate.original.getId());
    			}
    		} else {
	    		LinkInfo info = new LinkInfo();
	    		info.link_id = linkForUpdate.original.getId();
	    		info.gearblock1_id = linkForUpdate.gearblock1 != null ? linkForUpdate.gearblock1.getId(): null;
	    		info.gearblock2_id = linkForUpdate.gearblock2 != null ? linkForUpdate.gearblock2.getId(): null;
	    		info.blocks = linkForUpdate.blocks;
	    		
	    		if(info.link_id != 0) {
	    			this.linkSource.update(info);
	    			
	    			if(info.blocks == null) {
	    				this.reinforcementSource.deleteByLinkId(info.link_id);
	    			}
	    		} else {
		    		this.linkSource.insert(info);
		    		linkForUpdate.original.setId(info.link_id);
	    		}
	    		
	    		if(linkForUpdate.reinforcements != null) {
		    		for(ReinforcementInfo reinforcement : linkForUpdate.reinforcements) {
		    			if(reinforcement.link_id != 0) continue;
		    			
		    			reinforcement.link_id = info.link_id;
		    			
		    			this.reinforcementSource.insert(reinforcement);
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
			info.gearblock_id = gearblock.getId();
			info.location_worlduid = location.getWorldUID().toString();
			info.location_x = location.getX();
			info.location_y = location.getY();
			info.location_z = location.getZ();
			info.timer = gearblock.getTimer();
			
			if(gearblock.getTimerOperation() != null) {
				switch(gearblock.getTimerOperation()) {
				case DRAW:
					info.timerOperation = DRAW_CODE;
					break;
				case UNDRAW:
					info.timerOperation = UNDRAW_CODE;
					break;
				case REVERT:
					info.timerOperation = REVERT_CODE;
					break;
				}
			}
		}
		
		GearblockForUpdate gearForUpdate = new GearblockForUpdate();
		gearForUpdate.original = gearblock;
		gearForUpdate.info = info;

		synchronized(this.changedGearblocks) {
			this.changedGearblocks.put(gearblock, gearForUpdate);
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
				linkForUpdate.reinforcements = new ArrayList<ReinforcementInfo>();
				
				for(int i = 0; i < link.getBlocks().size(); i++) {
					ReinforcementInfo reinforcement = link.getBlocks().get(i).reinforcement;
					
					if(reinforcement != null && reinforcement.link_id == 0) {
						reinforcement.block_no = i;
						linkForUpdate.reinforcements.add(reinforcement);
					}
				}
			}
		}
		
		synchronized(this.changedLinks) {
			this.changedLinks.put(link, linkForUpdate);
		}
	}
	
	private static byte[] serializeBlocks(GearblockLink link) {
		if(link.getBlocks() == null) return null;
		
		byte[] data = new byte[BlockState.BytesPerBlock * link.getBlocks().size()];
		int offset = 0;
		
		for(BlockState block : link.getBlocks()) {
			offset = block.serialize(data, offset);
		}
		
		return data;
	}
	
	public static List<BlockState> deserializeBlocks(byte[] blockBytes) {
		if(blockBytes == null || blockBytes.length == 0) return null;
		
		List<BlockState> blocks = new ArrayList<BlockState>();
		int offset = 0;
			
		while(offset < blockBytes.length) {
			BlockState block = new BlockState();
			
			offset = BlockState.deserialize(blockBytes, offset, block);
			
			blocks.add(block);
		}
		
		return blocks;
	}
}