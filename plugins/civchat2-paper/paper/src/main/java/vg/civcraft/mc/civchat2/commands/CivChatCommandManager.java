package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import javax.annotation.Nonnull;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;

public class CivChatCommandManager extends CommandManager {

	public CivChatCommandManager(Plugin plugin) {
		super(plugin);
		init();
	}

	@Override
	public void registerCommands() {
		registerCommand(new Afk());
		registerCommand(new Exit());
		registerCommand(new GlobalMute());
		registerCommand(new GroupChat());
		registerCommand(new Ignore());
		registerCommand(new IgnoreGroup());
		registerCommand(new IgnoreList());
		registerCommand(new Reply());
		registerCommand(new Tell());
		registerCommand(new WhoAmI());
	}

	@Override
	public void registerCompletions(@Nonnull CommandCompletions<BukkitCommandCompletionContext> completions) {
		super.registerCompletions(completions);
		completions.registerCompletion("CC_Groups", (context) -> GroupTabCompleter
				.complete(context.getInput(), null, context.getPlayer()));
	}
}
