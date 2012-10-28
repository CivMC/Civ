package com.untamedears.citadel.listener;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.dao.CitadelCachingDao;

public class ChunkListener implements Listener {
    private CitadelCachingDao cdao_;

    public ChunkListener(CitadelCachingDao cdao) {
        this.cdao_ = cdao;
    }

    /**
     * This handles the ChunkUnloadEvent for the CitadelCachingDao to remove
     * chunks from the cache
     *
     * @param cue ChunkUnloadEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void chunkUnload(ChunkUnloadEvent cue) {
        Chunk chunk = cue.getChunk();
        this.cdao_.unloadChunk(chunk);
    }
}
