package vg.civcraft.mc.citadel.command;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import javax.annotation.Nonnull;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;

public class CitadelCommandManager extends CommandManager {

	public CitadelCommandManager(Plugin plugin) {
		super(plugin);
		init();
	}

	@Override
	public void registerCommands() {
		registerCommand(new Acid());
		registerCommand(new AdvancedFortification());
		registerCommand(new AreaReinforce());
		registerCommand(new Bypass());
		registerCommand(new EasyMode());
		registerCommand(new Fortification());
		registerCommand(new Information());
		registerCommand(new Insecure());
		registerCommand(new Off());
		registerCommand(new PatchMode());
		registerCommand(new Reinforce());
		registerCommand(new ReinforcementsGUI());
		registerCommand(new Reload());
	}

	@Override
	public void registerCompletions(@Nonnull CommandCompletions<BukkitCommandCompletionContext> completions) {
		super.registerCompletions(completions);
		completions.registerCompletion("CT_Groups", (context) -> GroupTabCompleter.complete(context.getInput(), null, context.getPlayer()));
	}
}
