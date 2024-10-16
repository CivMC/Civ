package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.CommandCompletions;
import co.aikar.commands.CommandContexts;
import co.aikar.commands.InvalidCommandArgument;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.players.PlayerNames;

/**
 * This is a separate helper class that allows plugins to use ACF without needing to commit to {@link CommandManager}
 * to get all the additional features it provides. It also allows some degree of optimisation since not all plugins
 * need their own item-material completions (like CivChat2).
 */
public final class CommandHelpers {

    /**
     * ACF has an automatic help generator, but it's currently considered an unstable API.
     * <a href="https://github.com/aikar/commands/wiki/Command-Help">Read more</a>.
     *
     * <pre><code>
     * // Somewhere in your plugin enable process
     * CommandHelpers.enableCommandHelp(this.commandManager);
     *
     * // Example Command
     * &#64;CommandAlias("example")
     * public class ExampleCommand extends BaseCommand {
     *     &#64;Default
     *     public void showHelp(final &#64;NotNull Player sender) {
     *         throw new ShowCommandHelp(); // Sends a help page to the player
     *     }
     * }
     * </code></pre>
     */
    @SuppressWarnings("deprecation")
    public static void enableCommandHelp(
        final @NotNull co.aikar.commands.CommandManager<?, ?, ?, ?, ?, ?> manager
    ) {
        manager.enableUnstableAPI("help");
    }

    // ============================================================
    // Completions
    // ============================================================

    /**
     * ACF already has the "@nothing" completion, but that seems less intuitive than "@none", so this adds "@none".
     */
    public static void registerNoneCompletion(
        final @NotNull CommandCompletions<?> completions
    ) {
        completions.registerStaticCompletion(
            "none",
            List.of()
        );
    }

    /**
     * Completion for all Bukkit materials.
     */
    public static void registerMaterialsCompletion(
        final @NotNull CommandCompletions<?> completions
    ) {
        completions.registerStaticCompletion(
            "materials",
            Arrays.stream(Material.values())
                .map(Enum::name)
                .toList()
        );
    }

    /**
     * Completion for all Bukkit materials that are items, excluding Air.
     */
    public static void registerItemMaterialsCompletion(
        final @NotNull CommandCompletions<?> completions
    ) {
        completions.registerStaticCompletion(
            "itemMaterials",
            Arrays.stream(Material.values())
                .filter(ItemUtils::isValidItemMaterial)
                .map(Enum::name)
                .toList()
        );
    }

    /**
     * Completion for all known player names.
     */
    public static void registerKnownPlayersCompletion(
        final @NotNull CommandCompletions<?> completions
    ) {
        completions.registerCompletion(
            "allplayers",
            (context) -> PlayerNames.getPlayerNames()
        );
        completions.setDefaultCompletion(
            "allplayers",
            OfflinePlayer.class
        );
    }

    // ============================================================
    // Contexts
    // ============================================================

    /**
     * Registers a context that requires the command-sender to be the console.
     */
    public static void registerConsoleSenderContext(
        final @NotNull CommandContexts<?> contexts
    ) {
        contexts.registerIssuerAwareContext(ConsoleCommandSender.class, (context) -> {
            if (context.getIssuer().getIssuer() instanceof final ConsoleCommandSender console) {
                return console;
            }
            throw new InvalidCommandArgument("Command can only be called from console!", false);
        });
    }
}
