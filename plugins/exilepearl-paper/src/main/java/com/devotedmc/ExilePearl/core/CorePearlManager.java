package com.devotedmc.ExilePearl.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
 * The prison pearl manager
 * @author Gordon
 */
public class CorePearlManager implements PearlManager {

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
	public void load() {
		pearls.clear();
		for (ExilePearl p : storage.loadAllPearls()) {
			pearls.put(p.getUniqueId(), p);
		}
	}
	
	
	/**
	 * Gets the pearled players
	 * @return The collection of pearled players
	 */
	public Collection<ExilePearl> getPearls() {
		return pearls.values();
	}
	
	
	/**
	 * Imprisons a player
	 * @param pearl The pearl instance
	 */
	@Override
	public ExilePearl exilePlayer(final Player exiled, final Player killedBy) {
		
		final PearlPlayer pKilledBy = pearlApi.getPearlPlayer(killedBy.getUniqueId());
		
		if (isPlayerExiled(exiled)) {
			pKilledBy.msg(Lang.pearlAlreadyPearled, exiled.getName());
			return null;
		}
		
		// set up the imprisoner's inventory
		Inventory inv = killedBy.getInventory();
		ItemStack stack = null;
		int stacknum = -1;
		
		// Scan for the smallest stack of normal ender pearls
		for (Entry<Integer, ? extends ItemStack> entry :
				inv.all(Material.ENDER_PEARL).entrySet()) {
			ItemStack newstack = entry.getValue();
			int newstacknum = entry.getKey();
			if (newstack.getDurability() == 0) {
				if (stack != null) {
					// don't keep a stack bigger than the previous one
					if (newstack.getAmount() > stack.getAmount()) {
						continue;
					}
					// don't keep an identical sized stack in a higher slot
					if (newstack.getAmount() == stack.getAmount() &&
							newstacknum > stacknum) {
						continue;
					}
				}

				stack = newstack;
				stacknum = entry.getKey();
			}
		}
		
		int pearlnum;
		ItemStack dropStack = null;
		if (stacknum == -1) { // no pearl (admin command)
			// give him a new one at the first empty slot
			pearlnum = inv.firstEmpty();
		} else if (stack.getAmount() == 1) { // if he's just got one pearl
			pearlnum = stacknum; // put the prison pearl there
		} else {
			// otherwise, put the prison pearl in the first empty slot
			pearlnum = inv.firstEmpty();
			if (pearlnum > 0) {
				// and reduce his stack of pearls by one
				stack.setAmount(stack.getAmount() - 1);
				inv.setItem(stacknum, stack);
			} else { // no empty slot?
				dropStack = new ItemStack(Material.ENDER_PEARL, stack.getAmount() - 1);
				pearlnum = stacknum; // then overwrite his stack of pearls
			}
		}
		
		// Drop pearls that otherwise would be deleted
		Location l = killedBy.getLocation();
		if (dropStack != null) {
			killedBy.getWorld().dropItem(l, dropStack);
			pearlApi.log(l + ", " + dropStack.getAmount());
		}
		
		
		final ExilePearl pearl = pearlFactory.createExilePearl(exiled.getUniqueId(), killedBy, config.getPearlStartStrength());
		pearl.updateLastMoved();

		ExilePearlEvent e = new ExilePearlEvent(pearl, ExilePearlEvent.Type.NEW, killedBy);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			return null;
		}
		
		inv.setItem(pearlnum, pearl.createItemStack());
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
	
	
	/**
	 * Gets a pearl by ID
	 * @param uid The ID to match
	 * @return The prison pearl instance if it exists
	 */
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
}
