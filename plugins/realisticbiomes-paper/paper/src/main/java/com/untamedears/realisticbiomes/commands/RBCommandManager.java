package com.untamedears.realisticbiomes.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import java.util.Arrays;
import javax.annotation.Nonnull;
import org.bukkit.block.Biome;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public class RBCommandManager extends CommandManager {

	public RBCommandManager(@Nonnull Plugin plugin) {
		super(plugin);
		init();
	}

	@Override
	public void registerCommands() {
		registerCommand(new Menu());
	}

	@Override
	public void registerCompletions(@Nonnull CommandCompletions<BukkitCommandCompletionContext> completions) {
		super.registerCompletions(completions);
		completions.registerCompletion("RB_Biomes", (context) -> Arrays.stream(Biome.values()).map(Biome::name).toList());
	}
}
