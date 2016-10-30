package vg.civcraft.mc.namelayer;

import vg.civcraft.mc.namelayer.group.Group;

public abstract class RunnableOnGroup implements Runnable {
	
	private Group group;
	
	public void setGroup(Group group) {
		this.group = group;
	}
	
	public Group getGroup() {
		return this.group;
	}
	
	@Override
	public abstract void run();
}
