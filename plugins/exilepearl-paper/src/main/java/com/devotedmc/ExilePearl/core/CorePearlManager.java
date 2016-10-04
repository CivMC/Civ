package com.devotedmc.ExilePearl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.event.PlayerFreedEvent;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.devotedmc.ExilePearl.storage.PearlStorage;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * The prison pearl manager implementation
 * @author Gordon
 */
class CorePearlManager implements PearlManager {

	private final ExilePearlApi pearlApi;
	private final PearlFactory pearlFactory;
	private final PearlStorage storage;
	
	private final HashMap<UUID, ExilePearl> pearls;
	
	
	/**
	 * Creates a new PearlManager instance
	 * @param logger The logging instance
	 * @param factory The pearl factory
	 * @param storage The database storage
	 */
	public CorePearlManager(final ExilePearlApi pearlApi, final PearlFactory pearlFactory, final PearlStorage storage) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");
		Guard.ArgumentNotNull(pearlFactory, "pearlFactory");
		Guard.ArgumentNotNull(storage, "storage");
		
		this.pearlApi = pearlApi;
		this.pearlFactory = pearlFactory;
		this.storage = storage;
		
		this.pearls = new HashMap<UUID, ExilePearl>();
	}
	
	
	/**
	 * Loads all the pearls from the database
	 */
	public void loadPearls() {
		pearls.clear();
		for (ExilePearl p : storage.loadAllPearls()) {
			pearls.put(p.getPlayerId(), p);
		}
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
	public ExilePearl exilePlayer(UUID exiledId, String killedByName) {
		Guard.ArgumentNotNull(exiledId, "exiledId");
		Guard.ArgumentNotNull(killedByName, "killedByName");
		
		final PearlPlayer pPlayer = pearlApi.getPearlPlayer(exiledId);
		final PearlPlayer pKiller = pearlApi.getPearlPlayer(killedByName);
		
		if (isPlayerExiled(exiledId) && pKiller != null || pKiller.getPlayer() == null) {
			pKiller.msg(Lang.pearlAlreadyPearled, pPlayer.getName());
			return null;
		}
		
		final ExilePearl pearl = pearlFactory.createExilePearl(exiledId, pKiller.getPlayer(), createUniquePearlId());

		PlayerPearledEvent e = new PlayerPearledEvent(pearl);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			return null;
		}
		
		pearls.put(pearl.getPlayerId(), pearl);
		storage.pearlInsert(pearl);

		pearl.setHealth(pearlApi.getPearlConfig().getPearlHealthStartValue());
		
		return pearl;
	}
	
	@Override
	public ExilePearl exilePlayer(Player exiled, Player killer) {
		Guard.ArgumentNotNull(exiled, "exiled");
		Guard.ArgumentNotNull(killer, "killer");
		
		return exilePlayer(exiled.getUniqueId(), pearlApi.getPearlPlayer(killer).getName());
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
		
		PearlPlayer player = pearlApi.getPearlPlayer(pearl.getPlayerId());
		
		// If the player is online, do the full remove, otherwise mark the pearl
		// as free offline and it will be removed when they log in
		if (player != null && player.isOnline()) {
			pearls.remove(pearl.getPlayerId());
			storage.pearlRemove(pearl);
		} else {
			pearl.setFreedOffline(true);
		}
		pearlApi.log("Player %s was freed for reason %s.", player.getName(), reason.toString());
		
		return true;
	}

	@Override
	public ExilePearl getPearl(String name) {
		Guard.ArgumentNotNullOrEmpty(name, "name");
		
		for(ExilePearl pearl :pearls.values()) {
			if (pearl.getPlayerName().equalsIgnoreCase(name));
			return pearl;
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
		
		int pearlId = pearlApi.getLoreGenerator().getPearlIdFromItemStack(is);
		if (pearlId != 0) {
			return getPearlById(pearlId);
		}
		
		// Check if this is a legacy pearl
		UUID legacyId = pearlApi.getLoreGenerator().getPlayerIdFromLegacyPearl(is);
		if (legacyId == null) {
			return null;
		}
		
		// If an existing pearl is found, just use that
		ExilePearl pearl = pearls.get(legacyId);
		if (pearl != null) {
			return pearl;
		}

		// If no pearl record is found, then we need to migrate that pearl to the new plugin
		String killerName = pearlApi.getLoreGenerator().getKillerNameFromLegacyPearl(is);
		if (killerName == null) {
			killerName = "Unknown";
		}
		
		pearl = exilePlayer(legacyId, killerName);
		if (pearl == null) {
			pearlApi.log("Failed to convert Prison Pearl for player %s", legacyId.toString());
		} else {
			pearlApi.log("Converted Prison Pearl for player %s", pearl.getPlayerName());
		}
		return pearl;
	}


	@Override
	public void decayPearls() {
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
}
