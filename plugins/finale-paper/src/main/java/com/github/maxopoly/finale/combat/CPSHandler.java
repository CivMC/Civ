package com.github.maxopoly.finale.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

public class CPSHandler {

	private Map<UUID, List<Long>> playerClicks = new ConcurrentHashMap<>();

	public int getCPS(UUID uuid) {
		 final long time = System.currentTimeMillis();
		 	List<Long> clicks = this.playerClicks.get(uuid);
		 	if (clicks == null) {
		 		return 0;
		 	}
	        final Iterator<Long> iterator = clicks.iterator();
	        while (iterator.hasNext()) {
	            if (iterator.next() + 1000L < time) {
	                iterator.remove();
	            }
	        }
	        return this.playerClicks.get(uuid).size();
	 }
	 
	 public void updateClicks(Player player) {
		 List<Long> clicks = playerClicks.get(player.getUniqueId());
		 if (clicks == null) {
			 clicks = new ArrayList<>();
		 }
		 clicks.add(System.currentTimeMillis());
		 playerClicks.put(player.getUniqueId(), clicks);
	 }
}
