package com.devotedmc.ExilePearl.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExilePearlConfig;
import com.devotedmc.ExilePearl.Lang;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.event.ExilePearlEvent;
import com.devotedmc.ExilePearl.storage.PearlStorage;
import com.devotedmc.ExilePearl.util.Guard;
import com.devotedmc.ExilePearl.util.PearlLoreUtil;

/**
 * The prison pearl manager implementation
 * @author Gordon
 */
class CorePearlManager implements PearlManager {

	private final ExilePearlApi pearlApi;
	private final PearlFactory pearlFactory;
	private final PearlStorage storage;
	private final ExilePearlConfig config;
	
	private final HashMap<UUID, ExilePearl> pearls;
	
	
	/**
	 * Creates a new PearlManager instance
	 * @param logger The logging instance
	 * @param factory The pearl factory
	 * @param storage The database storage
	 */
	public CorePearlManager(final ExilePearlApi pearlApi, final PearlFactory pearlFactory, final PearlStorage storage, final ExilePearlConfig config) {
		Guard.ArgumentNotNull(pearlApi, "pearlApi");
		Guard.ArgumentNotNull(pearlFactory, "pearlFactory");
		Guard.ArgumentNotNull(storage, "storage");
		Guard.ArgumentNotNull(config, "config");
		
		this.pearlApi = pearlApi;
		this.pearlFactory = pearlFactory;
		this.storage = storage;
		this.config = config;
		
		this.pearls = new HashMap<UUID, ExilePearl>();
	}
	
	
	/**
	 * Loads all the pearls from the database
	 */
	public void loadPearls() {
		pearls.clear();
		for (ExilePearl p : storage.loadAllPearls()) {
			pearls.put(p.getUniqueId(), p);
		}
	}
	
	
	/**
	 * Gets the pearled players
	 * @return The collection of pearled players
	 */
	@Override
	public Collection<ExilePearl> getPearls() {
		return Collections.unmodifiableCollection(pearls.values());
	}
	
	
	/**
	 * Imprisons a player
	 * @param pearl The pearl instance
	 */
	@Override
	public ExilePearl exilePlayer(final Player exiled, final Player killedBy) {
		
		final PearlPlayer pPlayer = pearlApi.getPearlPlayer(exiled.getUniqueId());
		final PearlPlayer pKilledBy = pearlApi.getPearlPlayer(killedBy.getUniqueId());
		
		if (pearls.containsKey(pPlayer.getUniqueId())) {
			throw new RuntimeException(String.format("Tried to exile player %s, but he was already exiled.", pPlayer.getName()));
		}
		
		if (isPlayerExiled(exiled)) {
			pKilledBy.msg(Lang.pearlAlreadyPearled, exiled.getName());
			return null;
		}
		
		final ExilePearl pearl = pearlFactory.createExilePearl(exiled.getUniqueId(), killedBy, config.getPearlHealthStartValue());

		ExilePearlEvent e = new ExilePearlEvent(pearl, ExilePearlEvent.Type.NEW, killedBy);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			return null;
		}
		
		pearls.put(pearl.getUniqueId(), pearl);
		storage.pearlInsert(pearl);
		
		return pearl;
	}
	
	
	/**
	 * Frees a pearl's player
	 * @param pearl The pearl to free
	 */
	@Override
	public boolean freePearl(ExilePearl pearl) {
		Guard.ArgumentNotNull(pearl, "pearl");
		
		ExilePearlEvent e = new ExilePearlEvent(pearl, ExilePearlEvent.Type.FREED, null);
		Bukkit.getPluginManager().callEvent(e);
		
		if (!e.isCancelled()) {
			pearls.remove(pearl.getUniqueId());
			storage.pearlRemove(pearl);
			return true;
		}
		return false;
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
    

	/**
	 * Gets a list of pearls located in an inventory
	 * @param inv The inventory to search
	 * @return The list of contained pearls
	 */
	public List<ExilePearl> getInventoryExilePearls(Inventory inv) {
		Guard.ArgumentNotNull(inv, "inv");
		
		List<ExilePearl> pearls = new ArrayList<ExilePearl>();
		
		for (ItemStack is : inv.all(Material.ENDER_PEARL).values()) {
			if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
				UUID id = PearlLoreUtil.getIDFromItemStack(is);
				if (id != null) {
					ExilePearl pearl = this.getPearl(id);
					if (pearl != null) {
						pearls.add(pearl);
					}
				}
			}
		}
		
		return pearls;
	}
	
	
	/**
	 * Gets the prison pearl stacks located in an inventory
	 * @param inv The inventory to search
	 * @return The list of contained pearls
	 */
	public List<ItemStack> getInventoryPearlStacks(Inventory inv) {
		List<ItemStack> pearls = new ArrayList<ItemStack>();
		
		for (ItemStack is : inv.all(Material.ENDER_PEARL).values()) {
			if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
				pearls.add(is);
			}
		}
		
		return pearls;
	}


	@Override
	public boolean isPlayerExiled(Player player) {
		Guard.ArgumentNotNull(player, "player");
		return pearls.get(player.getUniqueId()) != null;
	}


	@Override
	public boolean isPlayerExiled(UUID uid) {
		Guard.ArgumentNotNull(uid, "uid");
		return pearls.get(uid) != null;
	}


	@Override
	public ExilePearl getPearlFromItemStack(ItemStack is) {
		Guard.ArgumentNotNull(is, "is");
		
		UUID id = PearlLoreUtil.getIDFromItemStack(is);
		if (id != null) {
			return pearls.get(id);
		}
		
		return null;
	}


	@Override
	public void decayPearls() {
		pearlApi.log("Performing pearl decay.");
		long startTime = System.currentTimeMillis();

		final Collection<ExilePearl> pearls = getPearls();
		final int decayAmount = config.getPearlHealthDecayAmount();
		int numFreed = 0;

		// Iterate through all the pearls and reduce the strength
		// This will free any pearls that reach zero strength
		for (ExilePearl pearl : pearls) {
			if (pearl.verifyLocation()) {
				
				int updatedHealth = pearl.getHealth() - decayAmount;
				if (updatedHealth > 0) {
					pearl.setHealth(updatedHealth);
				} else {
					pearlApi.log("Freeing pearl for player %s because the strength reached 0.", pearl.getPlayerName());
					freePearl(pearl);
				}
			}
		}
		
		pearlApi.log("Pearl decay completed in %dms. Processed %d and freed %d." , System.currentTimeMillis() - startTime, pearls.size(), numFreed);
	}
}
