package com.untamedears.citadel.dao;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.NaturalReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.ReinforcementKey;

public class CitadelCachingDao extends CitadelDao {
    public static String MakeChunkId( Chunk chunk ) {
        return String.format("%s.%d.%d", chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    HashMap<String, ChunkCache> cachesByChunkId;
    PriorityQueue<ChunkCache> cachesByTime;
    long maxAge;
    long maxChunks;
    long counterChunkCacheLoads;
    long counterChunkUnloads;
    long counterChunkTimeouts;
    long counterReinforcementsSaved;
    long counterReinforcementsDeleted;
    long counterCacheHits;
    int counterPreventedThrashing;

    public CitadelCachingDao(JavaPlugin plugin){
        super(plugin);
        cachesByTime = new PriorityQueue<ChunkCache>();
        cachesByChunkId = new HashMap<String, ChunkCache>();
        setMaxAge(Citadel.getConfigManager().getCacheMaxAge());
        setMaxChunks(Citadel.getConfigManager().getCacheMaxChunks());
        counterChunkCacheLoads = 0;
        counterChunkUnloads = 0;
        counterChunkTimeouts = 0;
        counterReinforcementsSaved = 0;
        counterReinforcementsDeleted = 0;
        counterCacheHits = 0;
        counterPreventedThrashing = 0;
    }

    public ChunkCache getCacheOfBlock( Block block ) throws RefuseToPreventThrashingException {
        String chunkId = MakeChunkId(block.getChunk());
        ChunkCache cache = cachesByChunkId.get(chunkId);

        if( cache != null ){
            cachesByTime.remove(cache);
            cache.Access();
            cachesByTime.add(cache);
            ++counterCacheHits;
        }else{
            long removeCount = Math.max(5, cachesByTime.size() - maxChunks + 1);
            ChunkCache last = cachesByTime.peek();
            while (last != null && removeCount > 0 && last.getLastAccessed() + maxAge < System.currentTimeMillis()) {
                cachesByChunkId.remove(last.getChunkId());
                cachesByTime.poll();
                last.flush();
                last = cachesByTime.peek();
                --removeCount;
                ++counterChunkTimeouts;
            }
            if( cachesByTime.size() > maxChunks ){
                // WARNING: This WILL cause NaturalReinforcements to NOT be saved and thus lost.
                // TODO: Figure out why this needed to be added
                ++counterPreventedThrashing;
                throw new RefuseToPreventThrashingException();
            }
            ++counterChunkCacheLoads;
            cache = new ChunkCache( this, block.getChunk() );
            cachesByChunkId.put( cache.getChunkId(), cache );
            cachesByTime.add( cache );
        }
        return cache;
    }

    public void ForceCacheFlush(int flushCount) {
        // This is only forcing caches with pending updates to flush to DB. The
        //  ChunkCaches will remain intact.
        for (Map.Entry<String, ChunkCache> cursor : cachesByChunkId.entrySet()) {
            ChunkCache cache = cursor.getValue();
            if (cache.getTotalPendingCount() <= 0) {
                continue;
            }
            cache.flush();
            --flushCount;
            if (flushCount <= 0) {
                break;
            }
        }
    }

    @Override
    public IReinforcement findReinforcement( Location location ){
        return findReinforcement(location.getBlock());
    }

    @Override
    public IReinforcement findReinforcement( Block block ){
        try{
            ChunkCache cache = getCacheOfBlock( block );
            return cache.findReinforcement(block);
        }catch( RefuseToPreventThrashingException e ){
            Citadel.warning( "Bypassing RAM cache to prevent database thrashing.  Consider raising caching.max_chunks");
            return super.findReinforcement( block );
        }
    }

    @Override
    public void save(Object o){
        if( o instanceof IReinforcement ){
            IReinforcement r = (IReinforcement)o;
            try{
                getCacheOfBlock( r.getBlock() ).save( r );
            }catch( RefuseToPreventThrashingException e ){
                if ( !(r instanceof NaturalReinforcement) ){
                    Citadel.warning( "Bypassing RAM cache to prevent database thrashing.  Consider raising caching.max_chunks");
                    super.save( r );
                }
            }
        }else{
            super.save( o );
        }
    }
    
    @Override
    public void delete(Object o){
        if( o instanceof IReinforcement ){
            IReinforcement r = (IReinforcement)o;
            try{
                getCacheOfBlock( r.getBlock() ).delete( r );
            }catch( RefuseToPreventThrashingException e ){
                if ( !(r instanceof NaturalReinforcement) ){
                    Citadel.warning( "Bypassing RAM cache to prevent database thrashing.  Consider raising caching.max_chunks");
                    super.delete( r );
                }
            }
        }else{
            super.delete( o );
        }
    }

    //There should be some interface to implement that calls this automatically.
    public void shutDown(){
        while( !cachesByTime.isEmpty() ){
            ChunkCache cache = cachesByTime.poll();
            cachesByChunkId.remove(cache.getChunkId());
            cache.flush();
        }
    }

    public void unloadChunk(Chunk chunk) {
        // Flush any chunk data to the DB when the chunk unloads. Leave the
        //  ChunkCache intact in case the chunk reloads before it is removed
        //  from the ChunkCache cache.
        String chunkId = MakeChunkId(chunk);
        ChunkCache cache = cachesByChunkId.get(chunkId);
        if (cache != null) {
            cache.flush();
        }
        ++counterChunkUnloads;
    }

    public void setMaxAge(long age) {
        if (age > 1000) {  // 1 sec minimum
            maxAge = age;
        }
    }

    public void setMaxChunks(int count) {
        if (count > 0) {
            maxChunks = count;
        }
    }

    public void addCounterReinforcementsSaved(int delta) {
        counterReinforcementsSaved += delta;
    }

    public void addCounterReinforcementsDeleted(int delta) {
        counterReinforcementsDeleted += delta;
    }

    public long getCounterChunkCacheLoads()  { return counterChunkCacheLoads; }
    public long getCounterChunkUnloads()  { return counterChunkUnloads; }
    public long getCounterChunkTimeouts()  { return counterChunkTimeouts; }
    public long getCounterReinforcementsSaved()  { return counterReinforcementsSaved; }
    public long getCounterReinforcementsDeleted()  { return counterReinforcementsDeleted; }
    public long getCounterCacheHits()  { return counterCacheHits; }
    public int getCounterPreventedThrashing()  { return counterPreventedThrashing; }
    public int getChunkCacheSize()  { return cachesByChunkId.size(); }

    public Map<String, Long> getPendingUpdateCounts() {
        long totalCount = 0;
        long saveCount = 0;
        long deleteCount = 0;
        for (Map.Entry<String, ChunkCache> cursor : cachesByChunkId.entrySet()) {
            ChunkCache cache = cursor.getValue();
            int total = cache.getTotalPendingCount();
            int delete = cache.getPendingDeleteCount();
            int save = total - delete;
            saveCount += save;
            deleteCount += delete;
            totalCount += save + delete;
        }
        HashMap<String, Long> result = new HashMap<String, Long>();
        result.put("TotalPending", totalCount);
        result.put("PendingSaves", saveCount);
        result.put("PendingDeletes", deleteCount);
        return result;
    }

    private enum DBUpdateAction {
        SAVE,
        DELETE
    }

    private class ChunkCache implements
            Comparable<ChunkCache> {
        private CitadelCachingDao dao;
        private HashMap<PlayerReinforcement, DBUpdateAction> dbUpdates;
        private Set<PlayerReinforcement> freshRein = new TreeSet<PlayerReinforcement>();
        private TreeSet<IReinforcement> cache;//if RAM isn't a problem replace this with a HashSet.
        private String chunkId;
        private long lastAccessed;

        public ChunkCache( CitadelCachingDao dao, Chunk chunk ){
            this.dao = dao;
            this.chunkId = CitadelCachingDao.MakeChunkId(chunk);
            cache = new TreeSet<IReinforcement>( findReinforcementsInChunk( chunk ));
            dbUpdates = new HashMap<PlayerReinforcement, DBUpdateAction>();
            lastAccessed = System.currentTimeMillis();
        }

        public long getLastAccessed() { return lastAccessed; }
        public void Access() { lastAccessed = System.currentTimeMillis(); }

        public IReinforcement findReinforcement( Location l ){
            return findReinforcement(l.getBlock());
        }

        public IReinforcement findReinforcement( Block block ){
            IReinforcement key = new NaturalReinforcement();
            key.setId(new ReinforcementKey(block));
            IReinforcement r = cache.floor( key );

            if( r != null && r.equals(key) ){
                if ( r instanceof PlayerReinforcement){
                    dbUpdates.put((PlayerReinforcement)r, DBUpdateAction.SAVE);
    	        }
                return r;
            }else{
                return null;
            }
        }

        public void save( IReinforcement r ){
            if (r.getDurability() <= 0)
            {
                delete(r);
                return;
            }

            boolean inCache = cache.contains(r);
            if( inCache ){
                //Yes, this makes sense.
                //If we're editing the cache, then our new "r" will equal the old "r"
                //because reinforements are compared by their ReinforcementKeys, and the
                //new and old Reinforcements have the same key.  So we're removing the reinforcement
                //from the set that matches the new "r" (which the old "r" does) and then adding
                //the new "r".
                cache.remove( r );
                cache.add( r );
            }else{
                cache.add( r );
            }

            if ( r instanceof PlayerReinforcement){
                PlayerReinforcement pr = (PlayerReinforcement)r;
                if (!inCache) {
                    freshRein.add(pr);
                }
                dbUpdates.put(pr, DBUpdateAction.SAVE);
            }
        }

        public void delete( IReinforcement r ){
            if( cache.contains(r) ){
                cache.remove( r );
                if ( r instanceof PlayerReinforcement){
                    PlayerReinforcement pr = (PlayerReinforcement)r;
                    if (freshRein.contains(pr)) {
                        dbUpdates.remove(pr);
                        freshRein.remove(pr);
                    } else {
                        dbUpdates.put(pr, DBUpdateAction.DELETE);
                    }
                }
            }
        }

        public void flush(){
            LinkedList<PlayerReinforcement> toSave = new LinkedList<PlayerReinforcement>();
            LinkedList<PlayerReinforcement> toDelete = new LinkedList<PlayerReinforcement>();
            for (Map.Entry<PlayerReinforcement, DBUpdateAction> entry : dbUpdates.entrySet()) {
                if (entry.getValue() == DBUpdateAction.DELETE) {
                    toDelete.add(entry.getKey());
                } else {  // SAVE
                    toSave.add(entry.getKey());
                }
            }
            dbUpdates.clear();
            freshRein.clear();
            int size = toSave.size();
            if (size > 0) {
                int saveSuccess = getDatabase().save(toSave);
                dao.addCounterReinforcementsSaved(size);
            }
            size = toDelete.size();
            if (size > 0) {
                int deleteSuccess = getDatabase().delete(toDelete);
                dao.addCounterReinforcementsDeleted(size);
            }
        }

        public String getChunkId(){
            return chunkId;
        }

        public String toString(){
            return String.format("Cache (%s) has %d unsaved reinforcements.", chunkId, dbUpdates.size());
        }

        public int getTotalPendingCount() {
            return dbUpdates.size();
        }

        public int getPendingSaveCount() {
            int count = 0;
            for (Map.Entry<PlayerReinforcement, DBUpdateAction> entry : dbUpdates.entrySet()) {
                if (entry.getValue() == DBUpdateAction.SAVE) {
                    ++count;
                }
            }
            return count;
        }

        public int getPendingDeleteCount() {
            int count = 0;
            for (Map.Entry<PlayerReinforcement, DBUpdateAction> entry : dbUpdates.entrySet()) {
                if (entry.getValue() == DBUpdateAction.DELETE) {
                    ++count;
                }
            }
            return count;
        }

        public int compareTo(ChunkCache that) {
            if (this.lastAccessed < that.lastAccessed) {
                return -1;
            } else if (this.lastAccessed > that.lastAccessed) {
                return 1;
            }
            return 0;
        }
    }

    private class RefuseToPreventThrashingException extends Exception {}
}
