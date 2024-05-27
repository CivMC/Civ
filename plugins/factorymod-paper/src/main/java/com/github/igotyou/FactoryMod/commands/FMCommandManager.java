package com.github.igotyou.FactoryMod.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.CommandContexts;
import co.aikar.commands.InvalidCommandArgument;
import com.github.igotyou.FactoryMod.FactoryMod;
import com.github.igotyou.FactoryMod.eggs.IFactoryEgg;
import com.github.igotyou.FactoryMod.recipes.IRecipe;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public class FMCommandManager extends CommandManager {

	public FMCommandManager(
			final @NotNull FactoryMod plugin
	) {
		super(plugin);
		init();
	}

	public @NotNull FactoryMod getPlugin() {
		return (FactoryMod) this.plugin;
	}

	@Override
	public void registerCommands() {
		registerCommand(new CheatOutput());
		registerCommand(new Create());
		registerCommand(new FactoryMenu());
		registerCommand(new ItemUseMenu());
		registerCommand(new RunAmountSetterCommand());
		registerCommand(new TestCommand());
	}

	@Override
	public void registerContexts(
			final @NotNull CommandContexts<BukkitCommandExecutionContext> contexts
	) {
		super.registerContexts(contexts);

		contexts.registerContext(IFactoryEgg.class, (ctx) -> {
			final String input = ctx.joinArgs();
			final IFactoryEgg matchedEgg = getPlugin().getManager().getEgg(input);
			if (matchedEgg == null) {
				throw new InvalidCommandArgument("Could not find factory with name [" + input + "]!");
			}
			return matchedEgg;
		});

		contexts.registerContext(IRecipe.class, (ctx) -> {
			final String input = ctx.popFirstArg();
			final IRecipe matchedRecipe = getPlugin().getManager().getRecipe(input);
			if (matchedRecipe == null) {
				throw new InvalidCommandArgument("Could not find factory recipe with identifier [" + input + "]!");
			}
			return matchedRecipe;
		});
	}

	@Override
	public void registerCompletions(@Nonnull CommandCompletions<BukkitCommandCompletionContext> completions) {
		super.registerCompletions(completions);
		completions.registerCompletion("FM_Factories", (context) -> {
			return FactoryMod.getInstance()
					.getManager()
					.getAllFactoryEggs()
					.stream()
					.map(IFactoryEgg::getName)
					.toList();
		});
		completions.setDefaultCompletion("FM_Factories", IFactoryEgg.class);

		completions.registerCompletion("FM_Recipes", (context) -> {
			return FactoryMod.getInstance()
					.getManager()
					.getAllRecipesIdentifiers();
		});
		completions.setDefaultCompletion("FM_Recipes", IRecipe.class);
	}
}
