package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.CommandContexts;
import java.util.Objects;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

/**
 * Command registration class wrapper around {@link BukkitCommandManager}.
 */
public class CommandManager extends BukkitCommandManager {

    private final CivLogger logger;

    /**
     * Creates a new command manager for Aikar based commands and tab completions.
     *
     * @param plugin The plugin to bind this manager to.
     */
    public CommandManager(@NotNull final Plugin plugin) {
        super(Objects.requireNonNull(plugin));
        this.logger = CivLogger.getLogger(plugin.getClass(), getClass());
    }

    /**
     * Will initialise the manager and register both commands and completions. You should only really use this if
     * you've used {@link CommandManager#reset()} or both {@link #unregisterCommands()} and
     * {@link #unregisterCompletions()}, otherwise there may be issues.
     */
    public final void init() {
        registerContexts(getCommandContexts());
        registerCompletions(getCommandCompletions());
        registerCommands();
    }

    /**
     * This is called as part of {@link CommandManager#init()} and should be overridden by an extending class to
     * register all (or as many) commands at once.
     */
    public void registerCommands() {
    }


    /**
     * This is called as part of {@link CommandManager#init()} and should be overridden by an extending class to
     * register all (or as many) completions at once, though make sure to call super.
     *
     * @param completions The completion manager is given. It is the same manager that can be reached via
     *                    {@link #getCommandCompletions()}.
     */
    public void registerCompletions(@NotNull final CommandCompletions<BukkitCommandCompletionContext> completions) {
        CommandHelpers.registerNoneCompletion(completions);
        CommandHelpers.registerMaterialsCompletion(completions);
        CommandHelpers.registerItemMaterialsCompletion(completions);
        CommandHelpers.registerKnownPlayersCompletion(completions);
        CommandHelpers.registerCustomItemsCompletion(completions);
    }

    /**
     * This is called as part of {@link CommandManager#init()} and should be overridden by an extending class
     * to register all (or as many) contexts at once.
     *
     * @param contexts The context manager is given. It is the same manager that can be reached via
     *                 {@link #getCommandContexts()}.
     */
    public void registerContexts(@NotNull final CommandContexts<BukkitCommandExecutionContext> contexts) {
        CommandHelpers.registerConsoleSenderContext(contexts);
    }

    /**
     * Registers a new command and any attached tab completions.
     *
     * @param command      The command instance to register.
     * @param forceReplace Whether to force replace any existing command.
     */
    @Override
    public final void registerCommand(@NotNull final BaseCommand command, final boolean forceReplace) {
        super.registerCommand(Objects.requireNonNull(command), forceReplace);
        this.logger.info("Command [" + command.getClass().getSimpleName() + "] registered.");
    }

    /**
     * Deregisters a command and any attached tab completions.
     *
     * @param command The command instance to register.
     */
    @Override
    public final void unregisterCommand(@NotNull final BaseCommand command) {
        super.unregisterCommand(Objects.requireNonNull(command));
        this.logger.info("Command [" + command.getClass().getSimpleName() + "] unregistered.");
    }

    /**
     * Resets all command completions.
     */
    public final void unregisterCompletions() {
        this.completions = null;
    }

    /**
     * Resets the manager, resetting all commands and completions.
     */
    public final void reset() {
        unregisterCommands();
        unregisterCompletions();
    }

}
