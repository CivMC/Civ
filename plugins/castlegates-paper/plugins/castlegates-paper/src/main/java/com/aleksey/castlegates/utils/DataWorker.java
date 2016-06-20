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
import com.aleksey.castlegates.database.GearInfo;
import com.aleksey.castlegates.database.GearSource;
import com.aleksey.castlegates.database.LinkInfo;
import com.aleksey.castlegates.database.LinkSource;
import com.aleksey.castlegates.database.ReinforcementInfo;
import com.aleksey.castlegates.database.ReinforcementSource;
import com.aleksey.castlegates.database.SqlDatabase;
import com.aleksey.castlegates.types.BlockCoord;
import com.aleksey.castlegates.types.BlockState;
import com.aleksey.castlegates.types.GearLink;
import com.aleksey.castlegates.types.GearState;

public class DataWorker extends Thread implements Runnable {
	private static class GearForUpdate {
		public GearState original;
		public GearInfo info;
	}
	
	private static class LinkForUpdate {
		public GearLink original;
		public GearState gear1;
		public GearState gear2;
		public byte[] blocks;
		public List<ReinforcementInfo> reinforcements;
	}
	
	private GearSource gearSource;
	private LinkSource linkSource;
	private ReinforcementSource reinforcementSource;

	private Map<GearState, GearForUpdate> changedGears = new WeakHashMap<GearState, GearForUpdate>();
	private Map<GearLink, LinkForUpdate> changedLinks = new WeakHashMap<GearLink, LinkForUpdate>();

	private ArrayList<GearForUpdate> localChangedGears = new ArrayList<GearForUpdate>();
	private ArrayList<LinkForUpdate> localChangedLinks = new ArrayList<LinkForUpdate>();

	private long lastExecute = System.currentTimeMillis();
    private AtomicBoolean kill = new AtomicBoolean(false);

    public DataWorker(SqlDatabase db) {
		this.gearSource = new GearSource(db);
		this.linkSource = new LinkSource(db);
		this.reinforcementSource = new ReinforcementSource(db);
	}
	
	public void close() {
		terminateThread();
	}
	
	public Map<BlockCoord, GearState> load() throws SQLException {
		Map<BlockCoord, GearState> gears = new WeakHashMap<BlockCoord, GearState>();
		Map<Integer, GearState> gearsById = new WeakHashMap<Integer, GearState>();
		Map<Integer, GearLink> linksById = new WeakHashMap<Integer, GearLink>();
		
		loadGears(gears, gearsById);
		loadLinks(linksById, gearsById);
		loadReinforcements(linksById);
		
		return gears;
	}
	
	private void loadGears(Map<BlockCoord, GearState> gears, Map<Integer, GearState> gearsById) throws SQLException {
		List<GearInfo> gearData = this.gearSource.selectAll();
		
		for(GearInfo info : gearData) {
			UUID world = UUID.fromString(info.location_worlduid);
			BlockCoord location = new BlockCoord(world, info.location_x, info.location_y, info.location_z);
			GearState gear = new GearState(location);
			
			gear.setId(info.gear_id);
			
			gears.put(location, gear);
			gearsById.put(info.gear_id, gear);
		}
	}
	
	private void loadLinks(Map<Integer, GearLink> linksById, Map<Integer, GearState> gearsById) throws SQLException {
		List<LinkInfo> linkData = this.linkSource.selectAll();
		
		for(LinkInfo info : linkData) {
			GearState gear1 = info.gear1_id != null ? gearsById.get(info.gear1_id) : null;
			GearState gear2 = info.gear2_id != null ? gearsById.get(info.gear2_id) : null;
			List<BlockState> blocks = deserializeBlocks(info);
			GearLink link = new GearLink(gear1, gear2);
			
			link.setId(info.link_id);
			link.setBlocks(blocks);
			
			if(gear1 != null) {
				gear1.setLink(link);
			}

			if(gear2 != null) {
				gear2.setLink(link);
			}
			
			linksById.put(link.getId(), link);
		}
	}
	
	private void loadReinforcements(Map<Integer, GearLink> linksById) throws SQLException {
		List<ReinforcementInfo> reinforcementData = this.reinforcementSource.selectAll();
		GearLink link = null;
		
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
                
                synchronized (this.changedGears) {
                	for(GearForUpdate gearForUpdate : this.changedGears.values()) {
                		this.localChangedGears.add(gearForUpdate);
                	}

                	this.changedGears.clear();
                }
                
                synchronized (this.changedLinks) {
                	for(LinkForUpdate linkForUpdate : this.changedLinks.values()) {
                		this.localChangedLinks.add(linkForUpdate);
                	}

                	this.changedLinks.clear();
                }
                
                try {
	                updateGears();
	                updateLinks();
                } finally {
	                this.localChangedGears.clear();
	                this.localChangedLinks.clear();
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    }
    
    private void updateGears() throws SQLException {
    	for(GearForUpdate gearForUpdate : this.localChangedGears) {
    		if(gearForUpdate.original.isRemoved()) {
    			if(gearForUpdate.original.getId() != 0) {
    				this.gearSource.delete(gearForUpdate.original.getId());
    			}
    		}
    		else if(gearForUpdate.original.getId() != 0) {
    			this.gearSource.update(gearForUpdate.info);
    		}
    		else {
    			this.gearSource.insert(gearForUpdate.info);
    			gearForUpdate.original.setId(gearForUpdate.info.gear_id);
    		}
    	}
    }
    
    private void updateLinks() throws SQLException {
    	for(LinkForUpdate linkForUpdate : this.localChangedLinks) {
    		if(linkForUpdate.original.isRemoved()) {
    			if(linkForUpdate.original.getId() != 0) {
    				this.reinforcementSource.deleteByLinkId(linkForUpdate.original.getId());
    				this.linkSource.delete(linkForUpdate.original.getId());
    			}
    		} else {
	    		LinkInfo info = new LinkInfo();
	    		info.link_id = linkForUpdate.original.getId();
	    		info.gear1_id = linkForUpdate.gear1 != null ? linkForUpdate.gear1.getId(): null;
	    		info.gear2_id = linkForUpdate.gear2 != null ? linkForUpdate.gear2.getId(): null;
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

    public void addChangedGear(GearState gear) {
		GearInfo info;
		
		if(gear.isRemoved()) {
			info = null;
		} else {
			info = new GearInfo();

			BlockCoord location = gear.getCoord();
			info.gear_id = gear.getId();
			info.location_worlduid = location.getWorldUID().toString();
			info.location_x = location.getX();
			info.location_y = location.getY();
			info.location_z = location.getZ();
		}
		
		GearForUpdate gearForUpdate = new GearForUpdate();
		gearForUpdate.original = gear;
		gearForUpdate.info = info;

		synchronized(this.changedGears) {
			this.changedGears.put(gear, gearForUpdate);
		}
	}
	
	public void addChangedLink(GearLink link) {
		LinkForUpdate linkForUpdate = new LinkForUpdate();
		linkForUpdate.original = link;
		
		if(!link.isRemoved()) {
			linkForUpdate.gear1 = link.getGear1();
			linkForUpdate.gear2 = link.getGear2();
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
	
	private static byte[] serializeBlocks(GearLink link) {
		if(link.getBlocks() == null) return null;
		
		byte[] data = new byte[BlockState.BytesPerBlock * link.getBlocks().size()];
		int offset = 0;
		
		for(BlockState block : link.getBlocks()) {
			offset = block.serialize(data, offset);
		}
		
		return data;
	}
	
	private static List<BlockState> deserializeBlocks(LinkInfo info) {
		if(info.blocks == null || info.blocks.length == 0) return null;
		
		List<BlockState> blocks = new ArrayList<BlockState>();
		int offset = 0;
			
		while(offset < info.blocks.length) {
			BlockState block = new BlockState();
			
			offset = BlockState.deserialize(info.blocks, offset, block);
			
			blocks.add(block);
		}
		
		return blocks;
	}
}