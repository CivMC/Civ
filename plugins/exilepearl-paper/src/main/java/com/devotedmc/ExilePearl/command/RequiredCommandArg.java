package com.devotedmc.ExilePearl.command;

final class RequiredCommandArg implements CommandArg {

	private final String name;
	private final AutoTab autoTab;
	
	public RequiredCommandArg(String name, AutoTab autoTab) {
		this.name = name;
		this.autoTab = autoTab;
	}
	
	public RequiredCommandArg(String name) {
		this(name, null);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isRequired() {
		return true;
	}

	@Override
	public boolean isAutoTab() {
		return autoTab != null;
	}

	@Override
	public AutoTab getAutoTab() {
		return autoTab;
	}
	
	@Override
	public String toString() {
		return "<" + name + ">";
	}
}
