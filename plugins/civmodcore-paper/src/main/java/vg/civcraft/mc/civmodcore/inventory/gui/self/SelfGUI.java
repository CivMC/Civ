package vg.civcraft.mc.civmodcore.inventory.gui.self;

import java.util.Objects;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;

public abstract class SelfGUI<T extends Inventory> {

	private final T inventory;

	public SelfGUI(final T inventory) {
		this.inventory = Objects.requireNonNull(inventory);
	}

	/**
	 * @return Returns the wrapped inventory this GUI represents.
	 */
	public final T getInventory() {
		return this.inventory;
	}

	protected void handleTrackingStart() {

	}

	protected void handleTrackingEnd() {

	}

	protected void handleEvent(final InventoryEvent event) {

	}

}
