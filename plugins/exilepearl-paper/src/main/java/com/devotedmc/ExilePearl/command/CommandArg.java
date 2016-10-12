package com.devotedmc.ExilePearl.command;

/**
 * Represents a command argument
 * @author Gordon
 *
 */
interface CommandArg {

	/**
	 * Gets the argument name
	 * @return The argument name
	 */
	String getName();
	
	/**
	 * Gets whether the argument is required
	 * @return true if the argument is required
	 */
	boolean isRequired();
	
	/**
	 * Gets whether the argument supports auto-tab complete
	 * @return true if it supports auto-tab
	 */
	boolean isAutoTab();
	
	/**
	 * Gets the auto-tab if it exists
	 * @return The auto-tab instance
	 */
	AutoTab getAutoTab();
	
}
