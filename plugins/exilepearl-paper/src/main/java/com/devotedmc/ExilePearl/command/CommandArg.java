package com.devotedmc.ExilePearl.command;

public class CommandArg {

	private final String name;
	private final String defValue;
	private final AutoTab tab;
	
	public CommandArg(String name, String defValue, AutoTab tab) {
		this.name = name;
		this.defValue = defValue;
		this.tab = tab;
	}
	
	public CommandArg(String name, AutoTab tab) {
		this(name, null, tab);
	}
	
	public CommandArg(String name, String defValue) {
		this(name, defValue, AutoTab.DEFAULT);
	}
	
	public CommandArg(String name) {
		this(name, null, AutoTab.DEFAULT);
	}
	
	public String getName() {
		return name;
	}
	
	public AutoTab getAutoTab() {
		return tab;
	}
	
	public boolean isAutoTab() {
		return tab != AutoTab.DEFAULT;
	}
	
	@Override
	public String toString() {
		if (defValue != null) {
			return name + "=" + defValue;
		}
		return name;
	}
}
