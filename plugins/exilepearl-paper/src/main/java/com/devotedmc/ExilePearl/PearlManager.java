package com.devotedmc.ExilePearl;

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

import com.devotedmc.ExilePearl.event.ExilePearlEvent;
import com.devotedmc.ExilePearl.storage.PearlStorage;
import com.devotedmc.ExilePearl.util.Guard;

/**
 * The prison pearl manager
 * @author GFQ
 */
public class PearlManager {

	private final PearlLogging logger;
	private final ExilePearlFactory factory;
	private final PearlStorage storage;
	
	private final HashMap<UUID, ExilePearl> pearls;
	
	
	/**
	 * Creates a new PearlManager instance
	 * @param logger The logging instance
	 * @param factory The pearl factory
	 * @param storage The database storage
	 */
	public PearlManager(final PearlLogging logger, final ExilePearlFactory factory, final PearlStorage storage) {
		Guard.ArgumentNotNull(logger, "logger");
		Guard.ArgumentNotNull(factory, "factory");
		Guard.ArgumentNotNull(storage, "storage");
		
		this.logger = logger;
		this.factory = factory;
		this.storage = storage;
		
		this.pearls = new HashMap<UUID, ExilePearl>();
	}
	
	
	/**
	 * Loads all the pearls from the database
	 */
	public void load() {
		pearls.clear();
		for (ExilePearl p : storage.loadAllPearls()) {
			pearls.put(p.getPlayerID(), p);
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
	public ExilePearl imprisonPlayer(PearlPlayer exiled, PearlPlayer imprisoner) {
		
		if (this.isExiled(exiled)) {
			imprisoner.msg(Lang.pearlAlreadyPearled, exiled.getName());
			return null;
		}
		
		// set up the imprisoner's inventory
		Inventory inv = imprisoner.getBukkitPlayer().getInventory();
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
		Location l = imprisoner.getBukkitPlayer().getLocation();
		if (dropStack != null) {
			imprisoner.getBukkitPlayer().getWorld().dropItem(l, dropStack);
			logger.log(l + ", " + dropStack.getAmount());
		}
		
		
		final ExilePearl pearl = factory.createExilePearl(exiled.getUniqueId(), imprisoner.getBukkitPlayer());
		pearl.markMove();

		ExilePearlEvent e = new ExilePearlEvent(pearl, ExilePearlEvent.Type.NEW, imprisoner);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			return null;
		}
		
		inv.setItem(pearlnum, pearl.createItemStack());
		pearls.put(pearl.getPlayerID(), pearl);
		storage.pearlInsert(pearl);
		
		return pearl;
	}
	
	
	/**
	 * Frees a pearl's player
	 * @param pearl The pearl to free
	 */
	public boolean freePearl(ExilePearl pearl) {
		
		ExilePearlEvent e = new ExilePearlEvent(pearl, ExilePearlEvent.Type.FREED, pearl.getPlayer());
		Bukkit.getPluginManager().callEvent(e);
		
		if (!e.isCancelled()) {
			pearls.remove(pearl.getPlayerID());
			storage.pearlRemove(pearl);
			return true;
		}
		return false;
	}
	
	
	/**
	 * Removes a player
	 * @param pearl The player instance to free
	 */
	public void freePlayer(PearlPlayer p) {
		ExilePearl pearl = pearls.remove(p.getUniqueId());
		storage.pearlRemove(pearl);
	}
	
	
	/**
	 * Gets a pearl by ID
	 * @param id The ID to match
	 * @return The prison pearl instance if it exists
	 */
	public ExilePearl getById(UUID id) {
		return pearls.get(id);
	}
	
	
	/**
	 * Gets a pearl by an item stack
	 * @param is
	 */
	public ExilePearl getPearlByItem(ItemStack is) {
		UUID id = ExilePearl.getIDFromItemStack(is);
		if (id != null) {
			return pearls.get(id);
		}
		
		return null;
	}
	
	
	/**
	 * Gets whether a player is exiled
	 * @param p The player to check
	 * @return true if the player is exiled
	 */
	public boolean isExiled(Player p) {
		return pearls.get(p.getUniqueId()) != null;
	}
	
	
	/**
	 * Gets whether a player is exiled
	 * @param p The player UUID to check
	 * @return true if the player is exiled
	 */
	public boolean isExiled(UUID uid) {
		return pearls.get(uid) != null;
	}
	
	
	/**
	 * Gets whether a player is exiled
	 * @param p The player to check
	 * @return true if the player is exiled
	 */
	public boolean isExiled(PearlPlayer p) {
		return pearls.get(p.getUniqueId()) != null;
	}
    

	/**
	 * Gets a list of pearls located in an inventory
	 * @param inv The inventory to search
	 * @return The list of contained pearls
	 */
	public List<ExilePearl> getInventoryExilePearls(Inventory inv) {
		List<ExilePearl> pearls = new ArrayList<ExilePearl>();
		
		for (ItemStack is : inv.all(Material.ENDER_PEARL).values()) {
			if (is.hasItemMeta() && is.getItemMeta().hasLore()) {
				UUID id = ExilePearl.getIDFromItemStack(is);
				if (id != null) {
					ExilePearl pearl = this.getById(id);
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
}
