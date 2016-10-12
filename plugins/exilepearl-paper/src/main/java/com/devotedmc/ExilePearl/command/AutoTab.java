package com.devotedmc.ExilePearl.command;

final class AutoTab {
	
	private final String name;
	private final String help;
	
	public AutoTab(final String name, final String help) {
		this.name = name;
		this.help = help;
	}
	
	public AutoTab(final String name) {
		this(name, null);
	}
	
	public final String getName() {
		return name;
	}
	
	public String getHelp() {
		return help;
	}
	
	@Override
	public final String toString() {
		return name;
	}
}
