package com.untamedears.citadel.dao;

import java.util.HashMap;
import java.util.List;
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
    HashMap<Chunk, ChunkCache> cachesByChunk;
    PriorityQueue<ChunkCache> cachesByTime;

    long maxAge;
    int maxChunks;
    
    public CitadelCachingDao(JavaPlugin plugin){
        super(plugin);
        cachesByTime = new PriorityQueue<ChunkCache>();
        cachesByChunk = new HashMap<Chunk, ChunkCache>();
        maxAge = Citadel.getConfigManager().getCacheMaxAge();
        maxChunks = Citadel.getConfigManager().getCacheMaxChunks();
    }

    public ChunkCache getCacheOfBlock( Block block ) throws RefuseToPreventThrashingException {
        ChunkCache cache = cachesByChunk.get( block.getChunk() );

        if( cache != null ){
            cachesByTime.remove(cache);
            cache.Access();
            cachesByTime.add(cache);
        }else{
            ChunkCache last = cachesByTime.peek();
            while (last != null && last.getLastAccessed() + maxAge < System.currentTimeMillis()) {
                cachesByChunk.remove(last);
                cachesByTime.poll();
                last.flush();
                last = cachesByTime.peek();
            }
            if( cachesByTime.size() > maxChunks ){
                // WARNING: This WILL cause NaturalReinforcements to NOT be saved and thus lost.
                // TODO: Figure out why this needed to be added
                throw new RefuseToPreventThrashingException();
            }
            cache = new ChunkCache( block.getChunk() );
            cachesByChunk.put( block.getChunk() , cache );
            cachesByTime.add( cache );
        }
        return cache;
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
            cachesByChunk.remove(cache.getChunk());
            cache.flush();
        }
    }

    public void unloadChunk(Chunk chunk) {
        ChunkCache cache = cachesByChunk.get(chunk);
        if (cache != null) {
            cachesByTime.remove(cache);
            cache.flush();
        }
        cachesByChunk.remove(chunk);
    }

    private class ChunkCache implements
            Comparable<ChunkCache> {
        TreeSet<PlayerReinforcement> toSave;//if RAM isn't a problem replace this with a HashSet.
        TreeSet<PlayerReinforcement> toDelete;//if RAM isn't a problem replace this with a HashSet.

        TreeSet<IReinforcement> cache;//if RAM isn't a problem replace this with a HashSet.

        Chunk chunk;
        long lastAccessed;

        public ChunkCache( Chunk chunk ){
            this.chunk = chunk;
            cache = new TreeSet<IReinforcement>( findReinforcementsInChunk( chunk ));
            toSave = new TreeSet<PlayerReinforcement>();
            toDelete = new TreeSet<PlayerReinforcement>();
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
                    toSave.add((PlayerReinforcement)r);
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

            if( cache.contains(r) ){
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
                toDelete.remove(pr);
                if (toSave.contains(pr)){
                    toSave.remove(pr);
	            }
                toSave.add(pr);
            }
        }

        public void delete( IReinforcement r ){
            cache.remove( r );
            if ( r instanceof PlayerReinforcement){
                PlayerReinforcement pr = (PlayerReinforcement)r;
                toSave.remove( pr );
                toDelete.add( pr );//Don't need to replace, merely include if not there already, since there's only one way to do a deletion.
            }
        }

        public void flush(){
            int saveSuccess = getDatabase().save(toSave);
            int deleteSuccess = getDatabase().delete(toDelete);
        }
        
        public Chunk getChunk(){
            return chunk;
        }

        public String toString(){
            StringBuilder builder = new StringBuilder();
            builder.append( "Cache at (");
            builder.append( chunk.getX());
            builder.append( ",");
            builder.append( chunk.getZ());
            builder.append( "), has ");
            builder.append( toSave.size() );
            builder.append( " unsaved saves and ");
            builder.append( toDelete.size() );
            builder.append( " unsaved deletions.");
            
            return builder.toString();
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
