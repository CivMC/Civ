package vg.civcraft.mc.civmodcore.inventory.gui.self;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.Inventory;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.util.Resettable;

public final class SelfGUIManager implements Resettable, Listener {

	private static SelfGUIManager instance;

	private final CivModCorePlugin plugin;
	private final Map<Inventory, SelfGUI<?>> guis;

	public SelfGUIManager(final CivModCorePlugin plugin) {
		if (instance != null) {
			throw new IllegalStateException("Cannot create more than one self GUI manager!");
		}
		instance = this;
		this.plugin = Objects.requireNonNull(plugin);
		this.guis = new Hashtable<>();
	}

	@Override
	public void init() {
		this.plugin.registerListener(this);
	}

	@Override
	public void reset() {
		for (final var gui : this.guis.values()) {
			gui.handleTrackingEnd();
		}
		this.guis.clear();
		HandlerList.unregisterAll(this);
	}

	public static <T extends Inventory> void registerInventory(final SelfGUI<T> gui) {
		if (instance == null) {
			throw new IllegalStateException(SelfGUIManager.class.getSimpleName() + " has not been constructed!");
		}
		if (gui == null) {
			return;
		}
		instance.guis.compute(gui.getInventory(), (inventory, existing) -> {
			if (existing != null) {
				existing.handleTrackingEnd();
			}
			gui.handleTrackingStart();
			return gui;
		});
	}

	public static <T extends Inventory> void unregisterInventory(final SelfGUI<T> gui) {
		if (instance == null) {
			throw new IllegalStateException(SelfGUIManager.class.getSimpleName() + " has not been constructed!");
		}
		if (gui == null) {
			return;
		}
		final var removed = instance.guis.remove(gui.getInventory());
		if (gui != removed) {
			removed.handleTrackingEnd();
		}
		gui.handleTrackingEnd();
	}

	// ------------------------------------------------------------
	// Event Listeners - Should be comprehensive
	// ------------------------------------------------------------

	public void relayInventoryEvent(final InventoryEvent event) {
		final var gui = this.guis.get(event.getInventory());
		if (gui == null) {
			return;
		}
		gui.handleEvent(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void relayInventoryOpen(final InventoryOpenEvent event) {
		relayInventoryEvent(event);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void relayInventoryClose(final InventoryCloseEvent event) {
		relayInventoryEvent(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void relayInventoryDrag(final InventoryDragEvent event) {
		relayInventoryEvent(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void relayTradeSelect(final TradeSelectEvent event) {
		relayInventoryEvent(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void relayCraftEvent(final CraftItemEvent event) {
		relayInventoryEvent(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void relayCreativeEvent(final InventoryCreativeEvent event) {
		relayInventoryEvent(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void relayAnvilDamage(final AnvilDamagedEvent event) {
		relayInventoryEvent(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void relayEnchantItem(final EnchantItemEvent event) {
		relayInventoryEvent(event);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void relayPrepareCrafting(final PrepareItemCraftEvent event) {
		relayInventoryEvent(event);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void relayPrepareEnchant(final PrepareItemEnchantEvent event) {
		relayInventoryEvent(event);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void relayPrepareResult(final PrepareResultEvent event) {
		relayInventoryEvent(event);
	}

}
