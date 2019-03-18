package vg.civcraft.mc.citadel.listener;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import vg.civcraft.mc.citadel.Citadel;

public class ChunkLoadListener implements Listener {
	
	public ChunkLoadListener() {
		for(World world : Bukkit.getWorlds())  {
			for(Chunk chunk : world.getLoadedChunks()) {
				Citadel.getInstance().getReinforcementManager().loadChunkData(chunk);
			}
		}
	}
	
	@EventHandler
	public void chunkLoad(ChunkLoadEvent e) {
		Citadel.getInstance().getReinforcementManager().loadChunkData(e.getChunk());
	}
	
	@EventHandler
	public void chunkUnload(ChunkUnloadEvent e) {
		Citadel.getInstance().getReinforcementManager().unloadChunkData(e.getChunk());
	}
	
	

}
