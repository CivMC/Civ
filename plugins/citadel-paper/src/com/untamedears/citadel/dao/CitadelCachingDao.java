package com.untamedears.citadel.dao;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Reinforcement;
import com.untamedears.citadel.entity.ReinforcementKey;

public class CitadelCachingDao extends CitadelDao{
    
    HashMap<Chunk, ChunkCache> cachesByChunk;
    LinkedList<ChunkCache> cachesByTime;

    long maxAge;
    int maxChunks;
    
    public CitadelCachingDao(JavaPlugin plugin){
        super(plugin);
        cachesByTime = new LinkedList<ChunkCache>();
        cachesByChunk = new HashMap<Chunk, ChunkCache>();
        maxAge = Citadel.getConfigManager().getCacheMaxAge();
        maxChunks = Citadel.getConfigManager().getCacheMaxChunks();
    }
    
    public ChunkCache getCacheOfBlock( Block block ) throws RefuseToPreventThrashingException {
        ChunkCache cache = cachesByChunk.get( block.getChunk() );
        
        
        if( cache != null ){
            cachesByTime.remove(cache);
            cachesByTime.add(cache);
        }else{
            if( cachesByTime.size() > maxChunks ){
                throw new RefuseToPreventThrashingException();
            }
            cache = new ChunkCache( block.getChunk() );
            cachesByChunk.put( block.getChunk() , cache );
            cachesByTime.add( cache );
            
            ChunkCache last = cachesByTime.getLast();
            
            while( (last.getLastQueried() + maxAge < System.currentTimeMillis() ||
                   cachesByTime.size() > maxChunks) && 
                   !cachesByTime.isEmpty() ){
                last.flush();
                cachesByTime.remove(last);
                cachesByChunk.remove(last.getChunk());
            }
        }
        return cache;
    }
    
    @Override
    public Reinforcement findReinforcement( Location location ){
        return findReinforcement(location.getBlock());
    }
    
    @Override
    public Reinforcement findReinforcement( Block block ){
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
        if( o instanceof Reinforcement ){
            Reinforcement r = (Reinforcement)o;
            try{
                getCacheOfBlock( r.getBlock() ).save( r );
            }catch( RefuseToPreventThrashingException e ){
                Citadel.warning( "Bypassing RAM cache to prevent database thrashing.  Consider raising caching.max_chunks");
                super.save( r );
            }
        }else{
            super.save( o );
        }
    }
    
    @Override
    public void delete(Object o){
        if( o instanceof Reinforcement ){
            Reinforcement r = (Reinforcement)o;
            try{
                getCacheOfBlock( r.getBlock() ).save( r );
            }catch( RefuseToPreventThrashingException e ){
                Citadel.warning( "Bypassing RAM cache to prevent database thrashing.  Consider raising caching.max_chunks");
                super.delete( r );
            }
        }else{
            super.delete( o );
        }
    }
    
    //There should be some interface to implement that calls this automatically.
    public void shutDown(){
        while( !cachesByTime.isEmpty() ){
            cachesByTime.pop().flush();
        }
    }
    
    private class ChunkCache {
        TreeSet<Reinforcement> toSave;//if RAM isn't a problem replace this with a HashSet.
        TreeSet<Reinforcement> toDelete;//if RAM isn't a problem replace this with a HashSet.
        
        TreeSet<Reinforcement> cache;//if RAM isn't a problem replace this with a HashSet.
        
        Chunk chunk;
        long lastQueried;
        
        public ChunkCache( Chunk chunk ){
            this.chunk = chunk;
            cache = new TreeSet<Reinforcement>( findReinforcementsInChunk( chunk ));
            toSave = new TreeSet<Reinforcement>();
            toDelete = new TreeSet<Reinforcement>();
            lastQueried = System.currentTimeMillis();
        }
        
        public Reinforcement findReinforcement( Location l ){
            return findReinforcement(l.getBlock());
        }
        
        public Reinforcement findReinforcement( Block block ){
            lastQueried = System.currentTimeMillis();
            
            Reinforcement key = new Reinforcement();
            key.setId(new ReinforcementKey(block));
            Reinforcement r = cache.floor( key );
            
            if( r != null && r.equals(key) ){
                toSave.add(r);
                return r;
            }else{
                return null;
            }
        }
        
        public void save( Reinforcement r ){
            lastQueried = System.currentTimeMillis();
            
            if (r.getDurability() <= 0)
            {
                toSave.remove(r);
                cache.remove(r);
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
            
            toDelete.remove( r );
            if (toSave.contains(r)){
                toSave.remove(r);
                toSave.add( r );
            }else{
                toSave.add(r);
            }
        }
        
        public void delete( Reinforcement r ){
            lastQueried = System.currentTimeMillis();
            cache.remove( r );
            toSave.remove( r );
            toDelete.add( r );//Don't need to replace, merely include if not there already, since there's only one way to do a deletion.
        }
        
        public void flush(){
            int saveSuccess = getDatabase().save(toSave);
            int deleteSuccess = getDatabase().delete(toDelete);
        }
        
        public long getLastQueried(){
            return lastQueried;
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
            builder.append( " unsaved deletions.");
            
            return builder.toString();
        }
    }
    
    private class RefuseToPreventThrashingException extends Exception{
        
    }
}
