package com.github.igotyou.FactoryMod.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import javax.annotation.Nonnull;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public class FMCommandManager extends CommandManager {

	public FMCommandManager(Plugin plugin) {
		super(plugin);
		init();
	}

	@Override
	public void registerCommands() {
		registerCommand(new CheatOutput());
		registerCommand(new Create());
		registerCommand(new FactoryMenu());
		registerCommand(new ItemUseMenu());
		registerCommand(new RunAmountSetterCommand());
	}

	@Override
	public void registerCompletions(@Nonnull CommandCompletions<BukkitCommandCompletionContext> completions) {
		super.registerCompletions(completions);
		completions.registerCompletion("FM_Factories", (context) -> FactoryMod.getInstance().getManager().getAllFactoryEggs().stream().map(
				IFactoryEgg::getName).toList());
	}
}
