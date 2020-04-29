package vg.civcraft.mc.civmodcore.inventorygui.components;

import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class StaticDisplaySection extends InventoryComponent {
	
	private final IClickable [] clicks;
	private int additionCounter;

	public StaticDisplaySection(int size) {
		super(size);
		clicks = new IClickable [size];
	}
	
	public StaticDisplaySection(IClickable ... click) {
		this(click.length);
		for(int i = 0; i< click.length; i++) {
			this.clicks [i] = click [i];
		}
		additionCounter = click.length;
	}
	
	public StaticDisplaySection(int finalSize, IClickable ... click) {
		this(finalSize);
		for(int i = 0; i< click.length; i++) {
			this.clicks [i] = click [i];
		}
		additionCounter = click.length;
	}
	
	public void add(IClickable click) {
		clicks[additionCounter++] = click;
	}
	
	public void set(IClickable click, int slot) {
		clicks [slot] = click;
	}

	@Override
	void rebuild() {
		//NO OP
	}

}
