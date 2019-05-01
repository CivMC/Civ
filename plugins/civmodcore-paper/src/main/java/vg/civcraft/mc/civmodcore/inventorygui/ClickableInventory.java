package vg.civcraft.mc.civmodcore.inventorygui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an inventory filled with Clickables. Whenever one of those is clicked by a player, the clickables specific
 * action is executed. To use those, either extend your main plugin class from ACivMod or register
 * ClickableInventoryListener as a listener in your plugin. DONT DO BOTH.
 * <p>
 * Also if you want to update players inventories with the given method, use setPlugin(JavaPlugin plugin) to specify
 * which plugin this runs on
 *
 * @author Maxopoly
 */
public class ClickableInventory {

	private static final Logger log = Bukkit.getLogger();

	private static HashMap<UUID, ClickableInventory> openInventories = new HashMap<>();

	private Inventory inventory;

	private IClickable[] clickables;

	/**
	 * Creates a new ClickableInventory
	 *
	 * @param type
	 *            type of the inventory, dont use CREATIVE here, it wont work
	 * @param name
	 *            name of the inventory which is shown at the top when a player has it open
	 */
	public ClickableInventory(InventoryType type, String name) {
		if (name != null && name.length() > 32) {
			log.warning("ClickableInventory title exceeds Bukkit limits: " + name);
			name = name.substring(0, 32);
		}
		inventory = Bukkit.createInventory(null, type, name);
		this.clickables = new IClickable[inventory.getSize() + 1];
	}

	/**
	 * Creates a new Clickable Inventory based on how many slots the inventory should have. The given size should always
	 * be dividable by 9 and smaller or equal to 54, which is the maximum possible size
	 *
	 * @param size
	 *            Size of the inventory to create, must be multiple of 9, bigger than 0 and smaller than 54
	 * @param name
	 *            name of the inventory which is shown at the top when a player has it open
	 */
	public ClickableInventory(int size, String name) {
		if (name != null && name.length() > 32) {
			log.warning("ClickableInventory title exceeds Bukkit limits: " + name);
			name = name.substring(0, 32);
		}
		inventory = Bukkit.createInventory(null, size, name);
		this.clickables = new IClickable[size + 1];
	}

	/**
	 * Sets a specific slot to use the given Clickable and also updates its item in the inventory. This will overwrite
	 * any existing clickable for this slot, but wont update player inventories on it's own, which means if this applied
	 * while a player has an inventory open, the visual for him isn't changed, but the functionality behind the scenes
	 * is, possibly resulting in something the player did not want to do. Either only use this if you are sure noone has
	 * this inventory open currently or update the inventories right away
	 *
	 * @param c
	 *            The new clickable for the given slot
	 * @param index
	 *            index of the slot in the inventory
	 */
	public void setSlot(IClickable c, int index) {
		inventory.setItem(index, c.getItemStack());
		clickables[index] = c;
		c.addedToInventory(this, index);
	}

	/**
	 * Gets which Clickable currently represents the given slot in this instance.
	 *
	 * @param index
	 *            index of the Clickable or the item representation in the inventory
	 * @return Either the clickable in the given slot or null if the slot is empty or if the given index is out of range
	 *         of the inventory indices
	 */
	public IClickable getSlot(int index) {
		return index < inventory.getSize() ? clickables[index] : null;
	}

	/**
	 * This puts the given clickable in the first empty slot of this instance. If there are no empty slots, this will
	 * fail quietly. Make sure to not add clickables, whichs item representation is identical to another already in this
	 * inventory existing clickable, because their item representation would stack in the inventory
	 *
	 * @param c
	 *            Clickable to add
	 */
	public void addSlot(IClickable c) {
		for (int i = 0; i < clickables.length; i++) {
			if (clickables[i] == null) {
				setSlot(c, i);
				break;
			}
		}
	}

	/**
	 * Called whenever a player clicks an item in a clickable inventory. This executes the clicked items clickable and
	 * also closes the clickable inventory, unless the clicked object was a decoration stack
	 *
	 * @param p
	 *            Player who clicked
	 * @param index
	 *            index of the item in the inventory
	 */
	public void itemClick(Player p, int index) {
		if (index >= clickables.length || index < 0 || clickables[index] == null) {
			return;
		}
		clickables[index].clicked(p);
	}

	/**
	 * Gets the inventory shown to players by this instance. Do not modify the inventory object directly, use the
	 * methods provided by this class instead
	 *
	 * @return The inventory which represents this instance
	 */
	public Inventory getInventory() {
		return inventory;
	}

	/**
	 * Shows a player the inventory of this instance with all of its clickables
	 *
	 * @param p
	 *            Player to show the inventory to
	 */
	public void showInventory(Player p) {
		if (p != null) {
			p.openInventory(inventory);
			openInventories.put(p.getUniqueId(), this);
		}
	}

	/**
	 * Updates the inventories of this instance for all players who have it currently open and syncs it with the
	 * internal representation.
	 */
	public void updateInventory() {
		for (Map.Entry<UUID, ClickableInventory> c : openInventories.entrySet()) {
			if (c.getValue() == this) {
				Player p = Bukkit.getPlayer(c.getKey());
				p.updateInventory();
				showInventory(p);
			}
		}
	}

	/**
	 * Gets the index of any given Clickable in this instance
	 *
	 * @param c
	 *            Clickable to search for
	 * @return The index of the clickable if it exists in this inventory or -1 if it doesnt
	 */
	public int indexOf(IClickable c) {
		for (int i = 0; i < clickables.length; i++) {
			if (clickables[i] == c) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Sets a certain item slot, while bypassing the clickable structure
	 * 
	 * @param is
	 *            ItemStack to set to
	 * @param slot
	 *            Slot to be set
	 */
	void setItem(ItemStack is, int slot) {
		inventory.setItem(slot, is);
	}

	/**
	 * Closes a players clickable inventory if he has one open. This will also close any other inventory the player has
	 * possibly open, but no problems will occur if this is called while no clickable inventory was actually open
	 *
	 * @param p
	 *            Player whose inventory is closed
	 */
	public static void forceCloseInventory(Player p) {
		if (p != null) {
			p.closeInventory();
			openInventories.remove(p.getUniqueId());
		}
	}

	/**
	 * Called whenever a player closes a clickable inventory to update this in the internal tracking
	 *
	 * @param p
	 *            Player who closed the inventory
	 */
	public static void inventoryWasClosed(Player p) {
		if (p != null) {
			openInventories.remove(p.getUniqueId());
		}
	}

	/**
	 * Checks whether a player has a clickable inventory open currently
	 *
	 * @param p
	 *            Player to check
	 * @return true if the player has a clickable inventory open currently, false if not
	 */
	public static boolean hasClickableInventoryOpen(Player p) {
		return openInventories.get(p.getUniqueId()) != null;
	}

	/**
	 * Checks whether the player with this uuid has a clickable inventory open
	 *
	 * @param uuid
	 *            UUID of the player to check
	 * @return true if the player has a clickable inventory open currently, false if not
	 */
	public static boolean hasClickableInventoryOpen(UUID uuid) {
		return openInventories.get(uuid) != null;
	}

	/**
	 * Gets which clickable inventory the player with the given uuid has currently open
	 *
	 * @param uuid
	 *            UUID of the player
	 * @return The clickable inventory the player has currently open or null if the player has no inventory open
	 */
	public static ClickableInventory getOpenInventory(UUID uuid) {
		return openInventories.get(uuid);
	}

	/**
	 * Gets which clickable inventory the given player has currently open
	 *
	 * @param p
	 *            Player whose ClickableInventory is returned
	 * @return The ClickableInventory the player has currently open or null if the either the player has no inventory
	 *         open or if the given player object was null
	 */
	public static ClickableInventory getOpenInventory(Player p) {
		return p != null ? getOpenInventory(p.getUniqueId()) : null;
	}

}
