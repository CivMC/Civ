package vg.civcraft.mc.civmodcore.inventory.gui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public class ComponableSection extends InventoryComponent {

	protected final List<InventoryComponent> containedComponents;
	protected int componentSumSize;
	protected final InventoryComponent[] occupiedSlots;

	public ComponableSection(int size) {
		super(size);
		this.occupiedSlots = new InventoryComponent[size];
		this.containedComponents = new ArrayList<>();
	}

	public void addComponent(InventoryComponent component, IntPredicate slotSelector) {
		componentSumSize += component.getSize();
		if (componentSumSize > getSize()) {
			throw new IllegalArgumentException("Adding component " + component.toString()
					+ " would exceed maximum size allocated " + componentSumSize + "/" + getSize());
		}
		int slotsToOccupy = component.getSize();
		for (int i = 0; i < occupiedSlots.length; i++) {
			if (occupiedSlots[i] != null) {
				continue;
			}
			if (slotSelector.test(i)) {
				occupiedSlots[i] = component;
				slotsToOccupy--;
				if (slotsToOccupy <= 0) {
					break;
				}
			}
		}
		if (slotsToOccupy > 0) {
			throw new IllegalArgumentException("Component " + component.toString() + " did not occupy enough slots");
		}
		component.setParent(this);
		containedComponents.add(component);
	}

	public void removeComponent(InventoryComponent component) {
		for (int i = 0; i < occupiedSlots.length; i++) {
			if (occupiedSlots[i] == component) {
				occupiedSlots[i] = null;
			}
			if (containedComponents.remove(component)) {
				componentSumSize -= component.getSize();
			}
		}
	}

	/**
	 * Updates the displayed clickables for one specific contained component
	 * @param component Component to update
	 */
	void updateComponent(InventoryComponent component) {
		int offSet = 0;
		component.rebuild();
		List <IClickable> componentContent = component.getContent();
		for(int i = 0; i < occupiedSlots.length; i++) {
			if (occupiedSlots [i] == component) {
				this.content.set(i, componentContent.get(offSet++));
			}
		}
		if (getParent() != null) {
			getParent().updateComponent(this);
		}
	}
	
	/**
	 * Removes all content
	 */
	public void clear() {
		containedComponents.clear();
		componentSumSize = 0;
		for(int i = 0; i < occupiedSlots.length; i++) {
			occupiedSlots [i] = null;
		}
	}

	@Override
	void rebuild() {
		// we use lists instead of maps here, because we expect the amount of components
		// to be very low, rarely to never above 5
		List<List<IClickable>> builds = new ArrayList<>();
		List<Integer> offsets = new ArrayList<>();
		for (InventoryComponent component : containedComponents) {
			component.rebuild();
			builds.add(component.getContent());
			offsets.add(0);
		}
		for (int i = 0; i < occupiedSlots.length; i++) {
			if (occupiedSlots [i] == null) {
				content.set(i, null);
				continue;
			}
			int index = containedComponents.indexOf(occupiedSlots[i]);
			int offSet = offsets.get(index);
			offsets.set(index, offSet + 1);
			content.set(i, builds.get(index).get(offSet));
		}
	}

	@Override
	public void update() {
		// this component does not have an own state that could change, so the only
		// interpretation of an update is rebuilding all components
		rebuild();
		ComponableSection parent = getParent();
		if (parent != null) {
			parent.updateComponent(this);
		}
	}

}
