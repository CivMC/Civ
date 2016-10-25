package com.devotedmc.ExilePearl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.StorageProvider;
import com.devotedmc.ExilePearl.event.PearlDecayEvent;
import com.devotedmc.ExilePearl.event.PearlDecayEvent.DecayAction;
import com.devotedmc.ExilePearl.event.PlayerFreedEvent;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * The prison pearl manager implementation
 * @author Gordon
 */
final class CorePearlManager implements PearlManager {

	private final ExilePearlApi pearlApi;
	private final PearlFactory pearlFactory;
	private final StorageProvider storage;
	
	private final Map<UUID, ExilePearl> pearls = new HashMap<UUID, ExilePearl>();
	private final Map<UUID, ExilePearl> bcastRequests = new HashMap<UUID, ExilePearl>();
	
	
	/**
	 * Creates a new PearlManager instance
	 * @param logger The logging instance
	 * @param factory The pearl factory
	 * @param storage The database storage provider
	 */
	public CorePearlManager(final ExilePearlApi pearlApi, final PearlFactory pearlFactory, final StorageProvider storage) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");
		Guard.ArgumentNotNull(pearlFactory, "pearlFactory");
		Guard.ArgumentNotNull(storage, "storage");
		
		this.pearlApi = pearlApi;
		this.pearlFactory = pearlFactory;
		this.storage = storage;
	}
	
	
	/**
	 * Loads all the pearls from the database
	 */
	public void loadPearls() {
		pearls.clear();
		for (ExilePearl p : storage.getStorage().loadAllPearls()) {
			pearls.put(p.getPlayerId(), p);
		}
		pearlApi.log("Loaded %d pearls from storage.", pearls.size());
	}
	
	
	/**
	 * Gets the pearled players
	 * @return The collection of pearled players
	 */
	@Override
	public Collection<ExilePearl> getPearls() {
		return Collections.unmodifiableCollection(pearls.values().stream().filter(p -> !p.getFreedOffline()).collect(Collectors.toSet()));
	}
	
	
	@Override
	public ExilePearl exilePlayer(final UUID exiledId, final UUID killedById) {
		Guard.ArgumentNotNull(exiledId, "exiledId");
		Guard.ArgumentNotNull(killedById, "killedById");
		
		final Player pKilledBy = pearlApi.getPlayer(killedById);
		
		if (pearlApi.isPlayerExiled(exiledId)) {
			return null;
		}
		
		final ExilePearl pearl = pearlFactory.createExilePearl(exiledId, pKilledBy, createUniquePearlId());

		PlayerPearledEvent e = new PlayerPearledEvent(pearl);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			return null;
		}
		
		pearls.put(pearl.getPlayerId(), pearl);
		storage.getStorage().pearlInsert(pearl);

		pearl.setHealth(pearlApi.getPearlConfig().getPearlHealthStartValue());
		
		return pearl;
	}
	
	@Override
	public ExilePearl exilePlayer(Player exiled, Player killer) {
		Guard.ArgumentNotNull(exiled, "exiled");
		Guard.ArgumentNotNull(killer, "killer");
		
		return exilePlayer(exiled.getUniqueId(), killer.getUniqueId());
	}
	
	
	/**
	 * Frees a pearl's player
	 * @param pearl The pearl to free
	 */
	@Override
	public boolean freePearl(ExilePearl pearl, PearlFreeReason reason) {
		Guard.ArgumentNotNull(pearl, "pearl");
		Guard.ArgumentNotNull(reason, "reason");
		
		// Don't call the event if the pearl was already freed while they were offline
		if (!pearl.getFreedOffline()) {
			PlayerFreedEvent e = new PlayerFreedEvent(pearl, reason);
			Bukkit.getPluginManager().callEvent(e);
			
			if (e.isCancelled()) {
				return false; // The event was cancelled
			}
		}
		
		Player player = pearlApi.getPlayer(pearl.getPlayerId());
		
		// If the player is online, do the full remove, otherwise mark the pearl
		// as free offline and it will be removed when they log in
		if ((player != null && player.isOnline()) || reason == PearlFreeReason.FORCE_FREED_BY_ADMIN) {
			pearls.remove(pearl.getPlayerId());
			clearPearlBroadcasts(pearl);
			storage.getStorage().pearlRemove(pearl);
		} else {
			pearl.setFreedOffline(true);
		}
		pearlApi.log("Player %s was freed for reason %s.", pearl.getPlayerName(), reason.toString());
		
		return true;
	}

	@Override
	public ExilePearl getPearl(String name) {
		Guard.ArgumentNotNullOrEmpty(name, "name");
		
		for(ExilePearl pearl :pearls.values()) {
			if (pearl.getPlayerName().equalsIgnoreCase(name)) {
				return pearl;
			}
		}
		return null;
	}
	
	
	@Override
	public ExilePearl getPearl(UUID uid) {
		Guard.ArgumentNotNull(uid, "uid");
		return pearls.get(uid);
	}


	@Override
	public boolean isPlayerExiled(Player player) {
		Guard.ArgumentNotNull(player, "player");
		ExilePearl pearl = pearls.get(player.getUniqueId());
		return pearl != null && !pearl.getFreedOffline();
	}


	@Override
	public boolean isPlayerExiled(UUID uid) {
		Guard.ArgumentNotNull(uid, "uid");
		return pearls.get(uid) != null;
	}


	@Override
	public ExilePearl getPearlFromItemStack(ItemStack is) {
		
		ExilePearl pearl = null;
		int pearlId = pearlApi.getLoreProvider().getPearlIdFromItemStack(is);
		if (pearlId != 0) {
			pearl = getPearlById(pearlId);
			if (pearl == null || pearl.getFreedOffline()) {
				return null;
			}
			return pearl;
		}
		
		// Check if this is a legacy pearl
		UUID legacyId = pearlApi.getLoreProvider().getPlayerIdFromLegacyPearl(is);
		if (legacyId == null) {
			return null;
		}
		
		// If an existing pearl is found, just use that
		pearl = pearls.get(legacyId);
		if (pearl == null) {
			pearlApi.log(Level.SEVERE, "Found legacy PrisonPearl item for player %s but no pearl was found.", legacyId.toString());
			return null;
		}
		
		return pearl;
	}


	@Override
	public void decayPearls() {
		PearlDecayEvent e = new PearlDecayEvent(DecayAction.START);
		Bukkit.getPluginManager().callEvent(e);
		
		if (e.isCancelled()) {
			return;
		}
		
		pearlApi.log("Performing pearl decay.");
		long startTime = System.currentTimeMillis();

		final Collection<ExilePearl> pearls = getPearls();
		final int decayAmount = pearlApi.getPearlConfig().getPearlHealthDecayAmount();
		final HashSet<ExilePearl> pearlsToFree = new HashSet<ExilePearl>();

		// Iterate through all the pearls and reduce the health
		for (ExilePearl pearl : pearls) {
			
			// Ignore freed offline pearls
			if (pearl.getFreedOffline()) {
				continue;
			}
			
			pearl.setHealth(pearl.getHealth() - decayAmount);
			
			if (pearl.getHealth() == 0) {
				pearlApi.log("Freeing pearl for player %s because the health reached 0.", pearl.getPlayerName());
				pearlsToFree.add(pearl);
			}
			
			if (!pearl.verifyLocation()) {
				pearlApi.log("Freeing pearl for player %s because the verification failed.", pearl.getPlayerName());
				pearlsToFree.add(pearl);
			}
		}
		
		// Free the pending pearls
		for (ExilePearl pearl : pearlsToFree) {
			freePearl(pearl, PearlFreeReason.HEALTH_DECAY);
		}
		
		pearlApi.log("Pearl decay completed in %dms. Processed %d and freed %d." , System.currentTimeMillis() - startTime, pearls.size(), pearlsToFree.size());
		
		e = new PearlDecayEvent(DecayAction.COMPLETE);
		Bukkit.getPluginManager().callEvent(e);
	}
	
	
	/**
	 * Gets a unique pearlId
	 * @return The unique pearl ID
	 */
	private int createUniquePearlId() {
		Random rand = new Random();
		int pearlId = 0;
		
		while(pearlId == 0) {
			pearlId = rand.nextInt(Integer.MAX_VALUE >> 1);
			if (getPearlById(pearlId) != null) {
				pearlId = 0;
				continue;
			}
		}
		
		return pearlId;
	}
	
	private ExilePearl getPearlById(int pearlId) {
		for(ExilePearl p : pearls.values()) {
			if (p.getPearlId() == pearlId) {
				return p;
			}
		}
		return null;
	}


	@Override
	public void addBroadcastRequest(Player player, ExilePearl pearl) {
		bcastRequests.put(player.getUniqueId(), pearl);
	}


	@Override
	public ExilePearl getBroadcastRequest(Player player) {
		return bcastRequests.get(player.getUniqueId());
	}


	@Override
	public void removeBroadcastRequest(Player player) {
		bcastRequests.remove(player.getUniqueId());
	}
	
	private void clearPearlBroadcasts(ExilePearl pearl) {
		Set<UUID> toRemove = new HashSet<UUID>();
		
		for(Entry<UUID, ExilePearl> entry : bcastRequests.entrySet()) {
			if (entry.getValue().equals(pearl)) {
				toRemove.add(entry.getKey());
			}
		}
		
		for(UUID uid : toRemove) {
			bcastRequests.remove(uid);
		}
	}
}
