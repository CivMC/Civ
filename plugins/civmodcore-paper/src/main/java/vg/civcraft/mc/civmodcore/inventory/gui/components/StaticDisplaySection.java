package vg.civcraft.mc.civmodcore.inventory.gui.components;

import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;

public class StaticDisplaySection extends InventoryComponent {
	private int additionCounter;

	public StaticDisplaySection(int size) {
		super(size);
	}
	
	public StaticDisplaySection(IClickable ... click) {
		this(click.length);
		for(int i = 0; i< click.length; i++) {
			this.content.set(i, click [i]);
		}
		additionCounter = click.length;
	}
	
	public StaticDisplaySection(int finalSize, IClickable ... click) {
		this(finalSize);
		for(int i = 0; i< click.length; i++) {
			this.content.set(i, click [i]);
		}
		additionCounter = click.length;
	}
	
	public void add(IClickable click) {
		this.content.set(additionCounter++, click);
	}
	
	public void set(IClickable click, int slot) {
		this.content.set(slot, click);
	}

	@Override
	void rebuild() {
		//NO OP
	}

}
