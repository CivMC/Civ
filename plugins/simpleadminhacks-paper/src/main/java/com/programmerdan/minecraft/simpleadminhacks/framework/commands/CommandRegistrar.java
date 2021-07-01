package com.programmerdan.minecraft.simpleadminhacks.framework.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import java.util.ArrayList;
import java.util.List;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public class CommandRegistrar extends CommandManager {

	public static final String ROOT_ALIAS = "hacks|hack|sah";
	public static final String PERMISSION_HACKS = "simpleadmin.hacks";

	private final SimpleAdminHacks plugin;

	public CommandRegistrar(final SimpleAdminHacks plugin) {
		super(plugin);
		this.plugin = plugin;
	}

	@Override
	public void registerCommands() {
		registerCommand(new HacksCommand(this.plugin));
	}

	@Override
	public void registerCompletions(final CommandCompletions<BukkitCommandCompletionContext> completions) {
		super.registerCompletions(completions);
		completions.registerAsyncCompletion("hacks", (context) -> {
			final List<String> names = new ArrayList<>();
			for (final SimpleHack<? extends SimpleHackConfig> hack : this.plugin.getHackManager().getHacks()) {
				names.add(hack.getName());
			}
			return names;
		});
	}

}
