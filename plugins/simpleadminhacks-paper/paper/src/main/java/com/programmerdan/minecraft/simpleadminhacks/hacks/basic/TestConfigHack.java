package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import net.md_5.bungee.api.ChatColor;

public final class TestConfigHack extends BasicHack {

	@AutoLoad
	private boolean boolValue;

	@AutoLoad
	private byte byteValue;

	@AutoLoad
	private short shortValue;

	@AutoLoad
	private int intValue;

	@AutoLoad
	private long longValue;

	@AutoLoad
	private long maybeLongValue;

	@AutoLoad
	private float floatValue;

	@AutoLoad
	private double doubleValue;

	@AutoLoad
	private char charValue;

	public TestConfigHack(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		// boolean
		if (this.boolValue) {
			this.plugin.info(ChatColor.GREEN + "Boolean value passes!");
		}
		else {
			this.plugin.info(ChatColor.RED + "Boolean value failed.");
		}
		// byte
		if (this.byteValue == (byte) 8) {
			this.plugin.info(ChatColor.GREEN + "Byte value passes!");
		}
		else {
			this.plugin.info(ChatColor.RED + "Byte value failed: " + this.byteValue);
		}
		// short
		if (this.shortValue == (short) 14646) {
			this.plugin.info(ChatColor.GREEN + "Short value passes!");
		}
		else {
			this.plugin.info(ChatColor.RED + "Short value failed: " + this.shortValue);
		}
		// int
		if (this.intValue == 33822776) {
			this.plugin.info(ChatColor.GREEN + "Int value passes!");
		}
		else {
			this.plugin.info(ChatColor.RED + "Int value failed: " + this.intValue);
		}
		// long
		if (this.longValue == 2486941267899177L) {
			this.plugin.info(ChatColor.GREEN + "Long value passes!");
		}
		else {
			this.plugin.info(ChatColor.RED + "Long value failed: " + this.longValue);
		}
		// long - second, ambiguous test
		if (this.maybeLongValue == 6543L) {
			this.plugin.info(ChatColor.GREEN + "Long (ambiguous) value passes!");
		}
		else {
			this.plugin.info(ChatColor.RED + "Long (ambiguous) value failed: " + this.maybeLongValue);
		}
		// float
		if (this.floatValue == 3847.1234f) {
			this.plugin.info(ChatColor.GREEN + "Float value passes!");
		}
		else {
			this.plugin.info(ChatColor.RED + "Float value failed: " + this.floatValue);
		}
		// double
		if (this.doubleValue == 345978623.178569873d) {
			this.plugin.info(ChatColor.GREEN + "Double value passes!");
		}
		else {
			this.plugin.info(ChatColor.RED + "Double value failed: " + this.doubleValue);
		}
		// char
		if (this.charValue == 'c') {
			this.plugin.info(ChatColor.GREEN + "Char value passes!");
		}
		else {
			this.plugin.info(ChatColor.RED + "Char value failed: " + this.charValue);
		}
	}

}
