package vg.civcraft.mc.civmodcore.inventory.gui.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public abstract class InventoryComponent {
	
	private final int size;
	private ComponableSection parent;
	protected final List<IClickable> content;
	
	public InventoryComponent(int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("Component size must be at least one");
		}
		if (size > 54) {
			throw new IllegalArgumentException("Component size must be at maximum 54");
		}
		this.size = size;
		content = new ArrayList<>(size);
		//ensure size
		for(int i = 0; i < size; i++) {
			content.add(null);
		}
	}
	
	public int getSize() {
		return size;
	}

	protected abstract void rebuild();
	
	List<IClickable> getContent() {
		return Collections.unmodifiableList(content);
	}
	
	public void update() {
		if (parent != null) {
			parent.updateComponent(this);
		}
	}
	
	void setParent(ComponableSection parent) {
		this.parent = parent;
	}
	
	public ComponableSection getParent() {
		return parent;
	}

}
