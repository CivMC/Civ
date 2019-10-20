package com.devotedmc.ExilePearl.command;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * A command argument item that is used to automatically generate
 * the tab list for an argument.
 * @author Gordon
 *
 */
final class AutoTab {

	private final String name;
	private final String help;

	public AutoTab(final String name, final String help) {
		Guard.ArgumentNotNull(name, "name");

		this.name = name;
		this.help = help;
	}

	public AutoTab(final String name) {
		this(name, null);
	}

	/**
	 * Gets the auto-tab name
	 * @return The auto-tab name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Gets the help string associated with the tab item
	 * @return The help string
	 */
	public String getHelp() {
		return help;
	}

	@Override
	public final String toString() {
		return name;
	}
}
