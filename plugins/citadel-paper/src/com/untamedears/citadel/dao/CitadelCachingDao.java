package com.untamedears.citadel.dao;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import com.avaje.ebean.EbeanServer;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.DbUpdateAction;
import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.NaturalReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.entity.ReinforcementKey;

// Wonderfully pleasent and happy thought section.
//
// Rules:
// 1. If a PlayerReinforcement was created by the DB layer, it can't be
//    deleted and MUST be reused for any updates to that reinforcement
//    until it has been deleted from the DB.
// 2. NaturalReinforcements cannot be saved to the DB (currently).
//
// Scenarios:
// 1. Player hits a naturally reinforced block
//   a. Reinforcement lookup returns null
//   b. NaturalReinforcement created and decremeted
//   c. Reinforcement inserted into the cache
// 2. Player hits a damaged naturally reinforced block
//   a. Reinforcement lookup returns the NaturalReinforcement
//   b. Durability decremented by 1
// 3. Player destroys a naturally reinforced block
//   a. Reinforcement lookup returns the NaturalReinforcement
//   b. Durability decremented to 0
//   c. NaturalReinforcement deleted from the cache
// 4. Player creates a reinforcement on a naturally reinforced block
//   a. Utility.createPlayerReinforcement rejects the request and returns null
// 5. Player creates a reinforcement on reinforced block
//   a. Utility.createPlayerReinforcement rejects the request and returns null
// 6. Player creates a reinforcement on normal block
//   a. Utility.createPlayerReinforcement creates and returns reinforcement
// 7. Player deletes via ctbypass a natural reinforcement
//   a. BlockListener.blockBreak sees that its a natural reinforcement
//   b. It only calls Utility.reinforcementDamaged and not Utility.reinforcementBroken.
// 8. Player deletes an unmodified DB backed reinforcement
//   a. Delete sees state is NONE thus it is stored in the DB
//   b. Reinforcement DB state changed to DELETED
//   c. Reinforcement moved into pendingDbUpdate
//   d. Reinforcement removed from the cache
// 9. Player deletes a modified DB backed reinforcement
//   a. Delete sees state is SAVED thus it is stored in the DB
//   b. Reinforcement DB state changed to DELETED
//   c. Reinforcement is already in pendingDbUpdate
//   d. Reinforcement removed from the cache
// A. Player deletes an unsaved non-DB backed reinforcement
//   a. Delete sees its state is INSERT thus it was not loaded from the DB and
//      has no been saved to the DB yet
//   b. Delete just removes it from the cache
// B. Player creates a reinforcement on previously deleted reinforcement
//   a. Prior reinforcement found in pendingDbUpdate with state DELETE
//   b. Properties copied from the new into prior reinforcement
//   c. State changed to SAVE
//   d. Reinforcement removed from pendingDbUpdate and added to cache
//   e. The prior reinforcement is returned while the new one is deleted
// C. Player creates a natural reinforcement on previously deleted reinforcement
//   a. Reinforcement not found in cache
//   b. Natural reinforcement added to cache and returned


public class CitadelCachingDao extends CitadelDao {
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
    int counterAttemptedDbSaveRecoveries;
    int counterAttemptedDbDeleteRecoveries;
    int counterDbSaveFailures;
    int counterDbDeleteFailures;
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
        counterAttemptedDbSaveRecoveries = 0;
        counterAttemptedDbDeleteRecoveries = 0;
        counterDbSaveFailures = 0;
        counterDbDeleteFailures = 0;
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

    public boolean ForceChunkFlush(String chunk_id) {
        ChunkCache cache = cachesByChunkId.get(chunk_id);
        if (cache == null) {
            return false;
        }
        if (cache.getTotalPendingCount() > 0) {
            cache.flush();
        }
        return true;
    }

    public boolean ForceChunkUnload(String chunk_id) {
        ChunkCache cache = cachesByChunkId.get(chunk_id);
        if (cache == null) {
            return false;
        }
        if (cache.getTotalPendingCount() > 0) {
            cache.flush();
        }
        cachesByChunkId.remove(chunk_id);
        cachesByTime.remove(cache);
        return true;
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
    public Object save(Object o){
        if( o instanceof IReinforcement ){
            IReinforcement r = (IReinforcement)o;
            try{
                return (Object)getCacheOfBlock( r.getBlock() ).save( r );
            }catch( RefuseToPreventThrashingException e ){
                if ( !(r instanceof NaturalReinforcement) ){
                    Citadel.warning( "Bypassing RAM cache to prevent database thrashing.  Consider raising caching.max_chunks");
                    super.save( r );
                }
            }
        }else{
            super.save( o );
        }
        return o;
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
        Citadel.warning(formatCacheStats());
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
    public int getCounterAttemptedDbSaveRecoveries()  { return counterAttemptedDbSaveRecoveries; }
    public int getCounterAttemptedDbDeleteRecoveries()  { return counterAttemptedDbDeleteRecoveries; }
    public int getCounterDbSaveFailures()  { return counterDbSaveFailures; }
    public int getCounterDbDeleteFailures()  { return counterDbDeleteFailures; }
    public int getCounterPreventedThrashing()  { return counterPreventedThrashing; }
    public int getChunkCacheSize()  { return cachesByChunkId.size(); }

    public String formatCacheStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Cache Stats ---\n");
        sb.append(String.format("ChunkCacheSize = %d\n", getChunkCacheSize()));
        sb.append(String.format("ChunkCacheLoads = %d\n", getCounterChunkCacheLoads()));
        sb.append(String.format("CacheHits = %d\n", getCounterCacheHits()));
        sb.append(String.format("ChunkUnloads = %d\n", getCounterChunkUnloads()));
        sb.append(String.format("ChunkTimeouts = %d\n", getCounterChunkTimeouts()));
        sb.append(String.format("ReinforcementsSaved = %d\n", getCounterReinforcementsSaved()));
        sb.append(String.format("ReinforcementsDeleted = %d\n", getCounterReinforcementsDeleted()));
        sb.append(String.format("AttemptedDbSaveRecoveries = %d\n", getCounterAttemptedDbSaveRecoveries()));
        sb.append(String.format("AttemptedDbDeleteRecoveries = %d\n", getCounterAttemptedDbDeleteRecoveries()));
        sb.append(String.format("DbSaveFailures = %d\n", getCounterDbSaveFailures()));
        sb.append(String.format("DbDeleteFailures = %d\n", getCounterDbDeleteFailures()));
        sb.append(String.format("PreventedThrashing = %d", getCounterPreventedThrashing()));
        return sb.toString();
    }

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

    private class ChunkCache implements
            Comparable<ChunkCache> {
        private CitadelCachingDao dao;
        // If RAM isn't a problem replace these with HashSets.
        private TreeSet<PlayerReinforcement> pendingDbUpdate;
        private TreeSet<IReinforcement> cache;
        private String chunkId;
        private long lastAccessed;

        public ChunkCache( CitadelCachingDao dao, Chunk chunk ){
            this.dao = dao;
            this.chunkId = CitadelCachingDao.MakeChunkId(chunk);
            this.pendingDbUpdate = new TreeSet<PlayerReinforcement>();
            this.cache = dao.findReinforcementsInChunk(chunk);
            this.lastAccessed = System.currentTimeMillis();
        }

        public long getLastAccessed() { return lastAccessed; }
        public void Access() { lastAccessed = System.currentTimeMillis(); }

        public IReinforcement findReinforcement( Location l ){
            return findReinforcement(l.getBlock());
        }

        public IReinforcement findReinforcement( Block block ){
            NaturalReinforcement rein = new NaturalReinforcement();
            rein.setId(new ReinforcementKey(block));
            IReinforcement key = (IReinforcement)rein;
            IReinforcement r = cache.floor( key );

            if( r != null && r.equals(key) ){
                return r;
            }else{
                return null;
            }
        }

        public IReinforcement save( IReinforcement r ){
            if (r.getDurability() <= 0)
            {
                delete(r);
                return null;
            }
            PlayerReinforcement pr = null;
            if (r instanceof PlayerReinforcement) {
                pr = (PlayerReinforcement)r;
            }
            IReinforcement old_rein = null;
            if (cache.contains(r)) {
                old_rein = cache.floor(r);
                cache.remove(old_rein);
            } else if (pr != null && pendingDbUpdate.contains(pr)) {
                // When the PlayerReinforcement is marked for delete, it is
                //  removed from the cache but kept in pendingDbUpdate. This
                //  must reuse the old object if at all possible.
                old_rein = (IReinforcement)pendingDbUpdate.floor(pr);
            }
            if (pr == null || old_rein == null) {
                cache.add(r);
                if (pr != null) {
                    pendingDbUpdate.add(pr);  // DB INSERT
                }
                return r;
            }
            // pr != null && old_rein != null
            ((PlayerReinforcement)old_rein).updateFrom(pr);
            cache.add(old_rein);
            pendingDbUpdate.add((PlayerReinforcement)old_rein);  // DB UPDATE
            return old_rein;
        }

        public void delete(IReinforcement r) {
            if (cache.contains(r)) {
                // NaturalReinforcements aren't stored in the DB, only cached.
                cache.remove(r);
                if (!(r instanceof PlayerReinforcement)) {
                    return;
                }
                PlayerReinforcement pr = (PlayerReinforcement)r;
                if (pr.getDbAction() == DbUpdateAction.DELETE) {
                    // Just a WTF safety net
                    Citadel.warning("PlayerReinforcement already pending delete, bad: " + pr.toString());
                    return;
                }
                if (pr.getDbAction() == DbUpdateAction.INSERT) {
                    // Inserts don't exist in the DB yet so this only has
                    //  to be removed from the caches.
                    cache.remove(r);
                    pendingDbUpdate.remove(pr);
                    return;
                }
                pr.setDbAction(DbUpdateAction.DELETE);
                pendingDbUpdate.add(pr);  // DB DELETE
            }
        }

        public void flush(){
            LinkedList<PlayerReinforcement> toSave = new LinkedList<PlayerReinforcement>();
            LinkedList<PlayerReinforcement> toDelete = new LinkedList<PlayerReinforcement>();
            for (PlayerReinforcement pr : pendingDbUpdate) {
                if (pr.getDbAction() == DbUpdateAction.DELETE) {
                    toDelete.add(pr);
                } else {  // INSERT or SAVE
                    toSave.add(pr);
                }
                pr.setDbAction(DbUpdateAction.NONE);
            }
            pendingDbUpdate.clear();
            if (toSave.size() > 0) {
                dao.addCounterReinforcementsSaved(
                    AttemptSave(toSave));
            }
            if (toDelete.size() > 0) {
                dao.addCounterReinforcementsDeleted(
                    AttemptDelete(toDelete));
            }
        }

        private int AttemptSave(LinkedList<PlayerReinforcement> toSave) {
            boolean attemptRecovery = false;
            try {
                getDatabase().save(toSave);
            } catch (Exception ex) {
                attemptRecovery = true;
                Citadel.warning("DB mass save failure: " + ex.toString());
            }
            if (!attemptRecovery) {
                return toSave.size();
            }

            // Attempt to recover from any exception by individually saving
            //  each reinforcement.
            int successfulSaves = 0;
            ++counterAttemptedDbSaveRecoveries;
            EbeanServer db = getDatabase();
            for (PlayerReinforcement pr : toSave) {
                try {
                    db.save(pr);
                    ++successfulSaves;
                } catch (Exception ex) {
                    ++counterDbSaveFailures;
                    Citadel.severe("Attempted DB Save recovery failed for: "
                        + pr.toString());
                    Citadel.printStackTrace(ex);
                }
            }
            return successfulSaves;
        }

        private int AttemptDelete(LinkedList<PlayerReinforcement> toDelete) {
            boolean attemptRecovery = false;
            try {
                getDatabase().delete(toDelete);
            } catch (Exception ex) {
                attemptRecovery = true;
                Citadel.warning("DB mass delete failure: " + ex.toString());
            }
            if (!attemptRecovery) {
                return toDelete.size();
            }

            // Attempt to recover from any exception by individually saving
            //  each reinforcement.
            ++counterAttemptedDbDeleteRecoveries;
            int successfulDeletes = 0;
            EbeanServer db = getDatabase();
            for (PlayerReinforcement pr : toDelete) {
                try {
                    db.delete(pr);
                    ++successfulDeletes;
                } catch (Exception ex) {
                    ++counterDbDeleteFailures;
                    Citadel.severe("Attempted DB Delete recovery failed for: "
                        + pr.toString());
                    Citadel.printStackTrace(ex);
                }
            }
            return successfulDeletes;
        }

        public String getChunkId(){
            return chunkId;
        }

        public String toString(){
            return String.format("Cache (%s) has %d unsaved reinforcements.", chunkId, pendingDbUpdate.size());
        }

        public int getTotalPendingCount() {
            return pendingDbUpdate.size();
        }

        public int getPendingSaveCount() {
            int count = 0;
            for (PlayerReinforcement pr : pendingDbUpdate) {
                DbUpdateAction action = pr.getDbAction();
                if (action == DbUpdateAction.INSERT || action == DbUpdateAction.SAVE) {
                    ++count;
                }
            }
            return count;
        }

        public int getPendingDeleteCount() {
            int count = 0;
            for (PlayerReinforcement pr : pendingDbUpdate) {
                if (pr.getDbAction() == DbUpdateAction.DELETE) {
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

    private class RefuseToPreventThrashingException extends Exception {
        private static final long serialVersionUID = 763853564799286588L;
    }
}
