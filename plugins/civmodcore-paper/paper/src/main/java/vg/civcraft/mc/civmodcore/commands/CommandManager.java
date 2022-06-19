package vg.civcraft.mc.civmodcore.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.CommandCompletions.CommandCompletionHandler;
import co.aikar.commands.CommandContexts;
import com.google.common.base.Strings;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import javax.annotation.Nonnull;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

/**
 * Command registration class wrapper around {@link BukkitCommandManager}.
 */
public class CommandManager extends BukkitCommandManager {
	private final static List<String> allMaterials = Arrays.stream(Material.values()).map(Enum::name).toList();

	private final static List<String> itemMaterials = Arrays.stream(Material.values()).filter(ItemUtils::isValidItemMaterial).map(Enum::name).toList();

	// Track players to offer quick completion where necessary.
	private final Set<String> autocompletePlayerNames = new ConcurrentSkipListSet<>();

	private final CivLogger logger;

	/**
	 * Creates a new command manager for Aikar based commands and tab completions.
	 *
	 * @param plugin The plugin to bind this manager to.
	 */
	public CommandManager(@Nonnull final Plugin plugin) {
		super(Objects.requireNonNull(plugin));
		this.logger = CivLogger.getLogger(plugin.getClass(), getClass());
	}

	/**
	 * Will initialise the manager and register both commands and completions. You should only really use this if
	 * you've used {@link CommandManager#reset()} or both {@link #unregisterCommands()} and
	 * {@link #unregisterCompletions()}, otherwise there may be issues.
	 */
	public final void init() {
		// Prepare our list with player names on init.
		Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).forEach(autocompletePlayerNames::add);
		Bukkit.getPluginManager().registerEvents(new Listener() {
			// Players joining should be added to our list, just in case they are new.
			@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
			public void onLogin(PlayerLoginEvent ev) {
				autocompletePlayerNames.add(ev.getPlayer().getName());
			}
		}, plugin);

		registerCommands();
		registerCompletions(getCommandCompletions());
		registerContexts(getCommandContexts());
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
	public void registerCompletions(@Nonnull final CommandCompletions<BukkitCommandCompletionContext> completions) {
		completions.registerCompletion("none", (context) -> Collections.emptyList());
		completions.registerAsyncCompletion("allplayers", (context) -> new ArrayList<>(autocompletePlayerNames));
		completions.registerAsyncCompletion("materials", (context) -> new ArrayList<>(allMaterials));
		completions.registerAsyncCompletion("itemMaterials", (context) -> new ArrayList<>(itemMaterials));
	}

	/**
	 * This is called as part of {@link CommandManager#init()} and should be overridden by an extending class
	 * to register all (or as many) contexts at once.
	 *
	 * @param contexts The context manager is given. It is the same manager that can be reached via
	 *                 {@link #getCommandContexts()}.
	 */
	public void registerContexts(@Nonnull final CommandContexts<BukkitCommandExecutionContext> contexts) {
	}

	/**
	 * Registers a new command and any attached tab completions.
	 *
	 * @param command      The command instance to register.
	 * @param forceReplace Whether to force replace any existing command.
	 */
	@Override
	public final void registerCommand(@Nonnull final BaseCommand command, final boolean forceReplace) {
		super.registerCommand(Objects.requireNonNull(command), forceReplace);
		this.logger.info("Command [" + command.getClass().getSimpleName() + "] registered.");
		getTabCompletions(command.getClass()).forEach((method, annotation) -> {
			if (annotation.async()) {
				getCommandCompletions().registerAsyncCompletion(annotation.value(), (context) -> runCommandCompletion(context, command, annotation.value(), method));
			} else {
				getCommandCompletions().registerCompletion(annotation.value(), (context) -> runCommandCompletion(context, command, annotation.value(), method));
			}
			this.logger.info("Command Completer [" + annotation.value() + "] registered.");
		});
	}

	/**
	 * Deregisters a command and any attached tab completions.
	 *
	 * @param command The command instance to register.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final void unregisterCommand(@Nonnull final BaseCommand command) {
		super.unregisterCommand(Objects.requireNonNull(command));
		this.logger.info("Command [" + command.getClass().getSimpleName() + "] unregistered.");
		final Map<String, CommandCompletionHandler<BukkitCommandCompletionContext>> internal;
		try {
			internal = (HashMap<String, CommandCompletionHandler<BukkitCommandCompletionContext>>) FieldUtils.readField(getCommandCompletions(), "completionMap", true);
		} catch (final Throwable exception) {
			throw new UnsupportedOperationException("Could not get internal completion map.", exception);
		}
		for (final TabComplete complete : getTabCompletions(command.getClass()).values()) {
			internal.remove(complete.value().toLowerCase(Locale.ENGLISH));
			this.logger.info("Command Completer [" + complete.value() + "] unregistered.");
		}
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

	// ------------------------------------------------------------
	// Tab Completions
	// ------------------------------------------------------------

	@SuppressWarnings("unchecked")
	private List<String> runCommandCompletion(final BukkitCommandCompletionContext context, final BaseCommand command, final String id, final Method method) {
		try {
			method.setAccessible(true);
			return switch (method.getParameterCount()) {
				case 0 -> (List<String>) method.invoke(command);
				case 1 -> (List<String>) method.invoke(command, context);
				default -> throw new UnsupportedOperationException("Unsupported number of parameters.");
			};
		} catch (final Throwable exception) {
			this.logger.log(Level.WARNING, "Could not tab complete [@" + id + "]: an error with the handler!", exception);
			return Collections.emptyList();
		}
	}

	private static Map<Method, TabComplete> getTabCompletions(final Class<? extends BaseCommand> clazz) {
		final var completions = new HashMap<Method, TabComplete>();
		if (clazz == null) {
			return completions;
		}
		for (final Method method : clazz.getDeclaredMethods()) {
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			if (!List.class.isAssignableFrom(method.getReturnType())) {
				continue;
			}
			// TODO add a generic type check here when possible
			switch (method.getParameterCount()) {
				case 0:
					break;
				case 1:
					if (BukkitCommandCompletionContext.class.isAssignableFrom(method.getParameterTypes()[0])) {
						break;
					}
				default:
					continue;
			}
			final TabComplete tabComplete = method.getAnnotation(TabComplete.class);
			if (tabComplete == null) {
				continue;
			}
			if (Strings.isNullOrEmpty(tabComplete.value())) {
				continue;
			}
			completions.put(method, tabComplete);
		}
		return completions;
	}

}
