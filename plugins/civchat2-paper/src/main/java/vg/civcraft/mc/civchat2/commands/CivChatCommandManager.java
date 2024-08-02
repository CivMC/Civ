package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.namelayer.command.NameLayerCommands;

public class CivChatCommandManager extends CommandManager {

    public CivChatCommandManager(Plugin plugin) {
        super(plugin);
        init();
    }

    @Override
    public void registerCommands() {
        registerCommand(new Afk());
        registerCommand(new Exit());
        registerCommand(new GlobalChat());
        registerCommand(new GlobalMute());
        registerCommand(new GroupChat());
        registerCommand(new Ignore());
        registerCommand(new IgnoreGroup());
        registerCommand(new IgnoreList());
        registerCommand(new LocalChat());
        registerCommand(new Reply());
        registerCommand(new Tell());
        registerCommand(new WhoAmI());
    }

    @Override
    public void registerCompletions(@NotNull CommandCompletions<BukkitCommandCompletionContext> completions) {
        super.registerCompletions(completions);
        NameLayerCommands.registerGroupCompletion("CC_Groups", completions);
    }
}
