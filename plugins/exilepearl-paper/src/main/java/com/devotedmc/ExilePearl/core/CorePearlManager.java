package com.devotedmc.ExilePearl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.StorageProvider;
import com.devotedmc.ExilePearl.event.PearlDecayEvent;
import com.devotedmc.ExilePearl.event.PearlReturnEvent;
import com.devotedmc.ExilePearl.event.PearlSummonEvent;
import com.devotedmc.ExilePearl.event.PlayerFreedEvent;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.util.SpawnUtil;
import com.programmerdan.minecraft.banstick.data.BSPlayer;

import vg.civcraft.mc.civmodcore.util.Guard;
import vg.civcraft.mc.civmodcore.util.cooldowns.ICoolDownHandler;
import vg.civcraft.mc.civmodcore.util.cooldowns.MilliSecCoolDownHandler;

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

	private ICoolDownHandler<UUID> summonRequests = new MilliSecCoolDownHandler<UUID>(120000);


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
	@Override
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
	public ExilePearl exilePlayer(final UUID exiledId, final UUID killerId, Location location) {
		Guard.ArgumentNotNull(exiledId, "exiledId");
		Guard.ArgumentNotNull(killerId, "killerId");
		Guard.ArgumentNotNull(location, "location");

		final Block block = location.getBlock();
		final BlockState bs = block.getState();
		if (bs == null || (!(bs instanceof InventoryHolder))) {
			return null;
		}

		return exilePlayer(exiledId, killerId, new BlockHolder(block));
	}

	@Override
	public ExilePearl exilePlayer(UUID exiledId, Player killer) {
		Guard.ArgumentNotNull(exiledId, "exiledId");
		Guard.ArgumentNotNull(killer, "killer");

		if (!killer.isOnline()) {
			return null;
		}

		return exilePlayer(exiledId, killer.getUniqueId(), new PlayerHolder(killer));
	}

	@Override
	public ExilePearl exilePlayer(UUID exiledId, UUID killerId, PearlHolder holder) {
		Guard.ArgumentNotNull(exiledId, "exiledId");
		Guard.ArgumentNotNull(killerId, "killerId");
		Guard.ArgumentNotNull(holder, "holder");

		if (pearlApi.isPlayerExiled(exiledId)) {
			ExilePearl pearl = pearlApi.getPearl(exiledId);
			if(!(pearl.getPearlType() == PearlType.PRISON && pearlApi.getPearlConfig().allowPearlStealing())) {
				return null;
			}
		}

		final ExilePearl pearl = pearlFactory.createExilePearl(exiledId, killerId, createUniquePearlId(), holder);

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
			if(pearl.getPearlType() == PearlType.PRISON) {
				dropInventory(player);
				if(pearlApi.getPearlConfig().getShouldFreeTeleport() && (reason == PearlFreeReason.FREED_BY_PLAYER || reason == PearlFreeReason.PEARL_THROWN)) {
					player.teleport(pearl.getLocation().add(0, 0.5, 0));
				} else {
					SpawnUtil.spawnPlayer(player, pearlApi.getPearlConfig().getMainWorld());
				}
			}
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
		pearlApi.log("Performing pearl decay.");
		long startTime = System.currentTimeMillis();

		final Collection<ExilePearl> pearls = getPearls();
		final int decayAmount = pearlApi.getPearlConfig().getPearlHealthDecayAmount();
		final int decayTimeout = pearlApi.getPearlConfig().getPearlHealthDecayTimeout();
		final Set<String> disallowedWorlds = pearlApi.getPearlConfig().getDisallowedWorlds();
		final HashSet<ExilePearl> pearlsToFree = new HashSet<>();

		// Iterate through all the pearls and reduce the health
		for (ExilePearl pearl : pearls) {

			// Ignore freed offline pearls
			if (pearl.getFreedOffline()) {
				continue;
			}

			PearlHolder holder = pearl.getHolder();
			if (decayTimeout > 0 && pearl.getPlayer() != null) {
				// player is online now!
				pearl.setLastOnline(new Date());
			}

			// convert timeout to milliseconds and compare against last time online.
			if (decayTimeout == 0 || (new Date()).getTime() - pearl.getLastOnline().getTime() < (decayTimeout * 60 * 1000)) {
				PearlDecayEvent e = new PearlDecayEvent(pearl, decayAmount);
				Bukkit.getPluginManager().callEvent(e);
				if (!e.isCancelled() && e.getDamageAmount() > 0) {
					int oldHealth = pearl.getHealth();
					int newHealth = oldHealth - decayAmount;
					pearl.setHealth(newHealth);
					pearlApi.log("Set pearl for player %s health from %s to %s", pearl.getPlayerName(), oldHealth, newHealth);
				}
			}

			if (pearl.getHealth() == 0) {
				pearlApi.log("Freeing pearl for player %s because the health reached 0.", pearl.getPlayerName());
				pearlsToFree.add(pearl);
			}

			boolean permitLocationVerification = true;
			if (holder != null && holder.isBlock()) {
				if (disallowedWorlds.contains(holder.getLocation().getWorld().getName())) {
					pearlApi.log("Freeing pearl for player %s because the pearl is stored in disallowed world %s.", pearl.getPlayerName(),holder.getLocation().getWorld().getName());
					pearlsToFree.add(pearl);
				}
				permitLocationVerification = holder.inLoadedChunk();
			}

			if (permitLocationVerification) {
				if (!pearl.verifyLocation()) {
					pearlApi.log("Freeing pearl for player %s because the verification failed.", pearl.getPlayerName());
					pearlsToFree.add(pearl);
				}
			} else {
				pearlApi.log("Skipping verification of block holder for player %s in unloaded chunk at %s.", pearl.getPlayerName(), holder.getLocation());
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
		Set<UUID> toRemove = new HashSet<>();

		for(Entry<UUID, ExilePearl> entry : bcastRequests.entrySet()) {
			if (entry.getValue().equals(pearl)) {
				toRemove.add(entry.getKey());
			}
		}

		for(UUID uid : toRemove) {
			bcastRequests.remove(uid);
		}
	}

	@Override
	public boolean summonPearl(ExilePearl pearl, Player summoner) {
		if(pearl.getPearlType() == PearlType.PRISON && pearl.getPlayer().isOnline()
				&& !pearl.getPlayer().isDead() && !pearl.isSummoned()) {
			PearlSummonEvent event = new PearlSummonEvent(pearl, summoner);
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled()) {
				dropInventory(pearl.getPlayer());
				pearl.setReturnLocation(pearl.getPlayer().getLocation());
				pearl.setSummoned(true);
				summonRequests.removeCooldown(pearl.getPlayerId());
				return pearl.getPlayer().teleport(summoner);
			}
		}
		return false;
	}

	@Override
	public boolean returnPearl(ExilePearl pearl) {
		if(pearl.isSummoned() && pearl.getPlayer().isOnline() && !pearl.getPlayer().isDead()) {
			PearlReturnEvent event = new PearlReturnEvent(pearl);
			Bukkit.getPluginManager().callEvent(event);
			if(!event.isCancelled()) {
				Location returnLoc = pearl.getReturnLocation();
				if(returnLoc == null) returnLoc = pearlApi.getPearlConfig().getPrisonWorld().getSpawnLocation().add(0, 0.5, 0);
				pearl.getPlayer().teleport(returnLoc);
				pearl.setSummoned(false);
				pearl.setReturnLocation(null);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean requestSummon(ExilePearl pearl) {
		if(summonRequests.onCoolDown(pearl.getPlayerId())) {
			return false;
		} else {
			summonRequests.putOnCoolDown(pearl.getPlayerId());
			return true;
		}
	}

	@Override
	public boolean awaitingSummon(ExilePearl pearl) {
		return summonRequests.onCoolDown(pearl.getPlayerId());
	}

	private void dropInventory(Player player) {
		Inventory inv = player.getInventory();
		final Location loc = player.getLocation();
		final World world = loc.getWorld();
		for(int i = 0; i < inv.getSize(); i++) {
			final ItemStack item = inv.getItem(i);
			if(item == null) continue;
			if(item.getType() == Material.PLAYER_HEAD) continue;
			inv.clear(i);
			Bukkit.getScheduler().runTask(pearlApi, new Runnable() {

				@Override
				public void run() {
					world.dropItemNaturally(loc, item);
				}

			});
		}
	}


	@Override
	public int getExiledAlts(UUID uuidPlayer, boolean includeSelf) {
		if (pearlApi.isBanStickEnabled()) {
			BSPlayer bsPlayer = BSPlayer.byUUID(uuidPlayer);
	        if (bsPlayer == null) {
	            if (includeSelf && isPlayerExiled(uuidPlayer)) {
	            	return 1;
	            }
	            return 0;
	        }
	        int pearledAlts = 0;
	        for (BSPlayer alt : bsPlayer.getTransitiveSharedPlayers(true)) {
	            ExilePearl altPearl = pearlApi.getPearl(alt.getUUID());
	            if (altPearl != null) {
	            	if (includeSelf || !alt.getUUID().equals(bsPlayer.getUUID())) {
	            		pearledAlts++;
	            	}
	            }
	        }
	        return pearledAlts;
		}
        if (includeSelf && isPlayerExiled(uuidPlayer)) {
        	return 1;
        }
        return 0;
	}
}
