package com.devotedmc.ExilePearl.command;

final class OptionalCommandArg implements CommandArg {

	private final String name;
	private final String defValue;
	private final AutoTab autoTab;
	
	public OptionalCommandArg(String name, String defValue, AutoTab autoTab) {
		this.name = name;
		this.defValue = defValue;
		this.autoTab = autoTab;
	}
	
	public OptionalCommandArg(String name, AutoTab autoTab) {
		this(name, null, autoTab);
	}
	
	public OptionalCommandArg(String name, String defValue) {
		this(name, defValue, null);
	}
	
	public OptionalCommandArg(String name) {
		this(name, null, null);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isRequired() {
		return false;
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
		if (defValue != null) {
			return "[" + name + "=" + defValue + "]";
		}
		return "[" + name + "]";
	}
}
