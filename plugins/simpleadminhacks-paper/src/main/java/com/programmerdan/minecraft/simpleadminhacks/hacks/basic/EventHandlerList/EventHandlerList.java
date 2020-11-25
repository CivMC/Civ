package com.programmerdan.minecraft.simpleadminhacks.hacks.basic.EventHandlerList;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;

public final class EventHandlerList extends BasicHack {

	private final AikarCommandManager commands;

	private final _HandlersList handlers;

	public EventHandlerList(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		this.handlers = new _HandlersList();
		this.commands = new AikarCommandManager(plugin(), false) {
			@Override
			public void registerCommands() {
				registerCommand(new _HandlersCommand(handlers));
			}
		};
	}

	@Override
	public void onEnable() {
		super.onEnable();
		this.commands.init();
		plugin().registerListener(this.handlers);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this.handlers);
		this.commands.reset();
		super.onDisable();
	}

	public static BasicHackConfig generate(final SimpleAdminHacks plugin, final ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

}
