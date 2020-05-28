package vg.civcraft.mc.civmodcore.command;

import static vg.civcraft.mc.civmodcore.command.AikarCommand.TabComplete;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.CommandCompletions.CommandCompletionHandler;
import co.aikar.commands.CommandContexts;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.reflect.FieldUtils;
import org.bukkit.Material;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.util.TextUtil;

/**
 * Command registration class wrapper around {@link BukkitCommandManager}.
 */
public class AikarCommandManager {

    private final ACivMod plugin;

    private CustomBukkitManager manager;

    /**
	 * Creates a new command manager for Aikar based commands and tab completions.
	 *
	 * @param plugin The plugin to bind this manager to.
	 */
    public AikarCommandManager(ACivMod plugin) {
    	this(plugin, true);
    }

    /**
	 * Creates a new command manager for Aikar based commands and tab completions.
	 *
	 * @param plugin The plugin to bind this manager to.
	 * @param autoRegister If true will automatically register all commands and completions as defined within
	 *     {@link AikarCommandManager#registerCommands()} and
	 *     {@link AikarCommandManager#registerCompletions(CommandCompletions)}.
	 */
	public AikarCommandManager(ACivMod plugin, boolean autoRegister) {
		this.plugin = plugin;
		if (autoRegister) {
			register();
		}
	}

	/**
	 * Will register both commands and completions. You should only really use this if you've used
	 * {@link AikarCommandManager#reset()} or both {@link AikarCommandManager#deregisterCommands()} and
	 * {@link AikarCommandManager#deregisterCompletions()}, otherwise there may be issues.
	 */
    public final void register() {
		this.manager = new CustomBukkitManager(plugin);
		registerCommands();
		registerCompletions(this.manager.getCommandCompletions());
		registerContexts(this.manager.getCommandContexts());
	}

	/**
	 * This is called as part of {@link AikarCommandManager#register()} and should be overridden by an extending class
	 * to register all (or as many) commands at once.
	 */
    public void registerCommands() { }

    /**
	 * This is called as part of {@link AikarCommandManager#register()} and should be overridden by an extending class
	 * to register all (or as many) completions at once, though make sure to call super.
	 *
	 * @param completions The completion manager is given. It is the same manager that can be reached via
	 *     {@link CustomBukkitManager#getCommandCompletions()}.
	 */
    public void registerCompletions(CommandCompletions<BukkitCommandCompletionContext> completions) {
		completions.registerAsyncCompletion("materials", context ->
				Arrays.stream(Material.values()).
						map(Enum::name).
						filter((name) -> TextUtil.startsWith(name, context.getInput())).
						collect(Collectors.toCollection(ArrayList::new)));
		completions.registerAsyncCompletion("itemMaterials", context ->
				Arrays.stream(Material.values()).
						filter(MaterialAPI::isValidItemMaterial).
						map(Enum::name).
						filter((name) -> TextUtil.startsWith(name, context.getInput())).
						collect(Collectors.toCollection(ArrayList::new)));
	}

	/**
	 * This is called as part of {@link AikarCommandManager#register()} and should be overridden by an extending class
	 * to register all (or as many) contexts at once.
	 *
	 * @param contexts The context manager is given. It is the same manager that can be reached via
	 *     {@link CustomBukkitManager#getCommandContexts()}.
	 */
	public void registerContexts(CommandContexts<BukkitCommandExecutionContext> contexts) { }

	/**
	 * Registers a new command and any attached tab completions.
	 *
	 * @param command The command instance to register.
	 */
    public final void registerCommand(AikarCommand command) {
		Preconditions.checkArgument(command != null, "Could not register that command: the command was null.");
        this.manager.registerCommand(command);
		this.plugin.info("Command [" + command.getClass().getSimpleName() + "] registered.");
		for (Map.Entry<Method, TabComplete> entry : getTabCompletions(command.getClass()).entrySet()) {
			if (entry.getValue().async()) {
				this.manager.getCommandCompletions().registerAsyncCompletion(entry.getValue().value(), (context) ->
						runCommandCompletion(context, command, entry.getValue().value(), entry.getKey()));
			}
			else {
				this.manager.getCommandCompletions().registerCompletion(entry.getValue().value(), (context) ->
						runCommandCompletion(context, command, entry.getValue().value(), entry.getKey()));
			}
			this.plugin.info("Command Completer [" + entry.getValue().value() + "] registered.");
		}
    }

    /**
	 * Deregisters a command and any attached tab completions.
	 *
	 * @param command The command instance to register.
	 */
	@SuppressWarnings("unchecked")
    public final void deregisterCommand(AikarCommand command) {
		Preconditions.checkArgument(command != null, "Could not deregister that command: the command was null.");
		this.manager.unregisterCommand(command);
		this.plugin.info("Command [" + command.getClass().getSimpleName() + "] deregistered.");
		Map<String, CommandCompletionHandler<BukkitCommandCompletionContext>> internal;
		try {
			internal = (HashMap<String, CommandCompletionHandler<BukkitCommandCompletionContext>>)
					FieldUtils.readField(this.manager.getCommandCompletions(), "completionMap", true);
		}
		catch (Exception exception) {
			throw new UnsupportedOperationException("Could not get internal completion map.", exception);
		}
		for (TabComplete complete : getTabCompletions(command.getClass()).values()) {
			internal.remove(complete.value().toLowerCase(Locale.ENGLISH));
			this.plugin.info("Command Completer [" + complete.value() + "] deregistered.");
		}
	}

	/**
	 * Deregisters all commands.
	 */
	public final void deregisterCommands() {
		this.manager.unregisterCommands();
	}

	/**
	 * Deregisters all command completions.
	 */
	public final void deregisterCompletions() {
		this.manager.unregisterCompletions();
	}

	/**
	 * Resets the manager, resetting all commands and completions.
	 */
    public final void reset() {
        this.manager.unregisterCommands();
		this.manager.unregisterCompletions();
		this.manager = null;
	}

	/**
	 * Retrieves the internal manager this class wraps.
	 *
	 * @return Returns the internal manager.
	 */
    public final CustomBukkitManager getInternalManager() {
        return this.manager;
    }

	// ------------------------------------------------------------
	// Utilities
	// ------------------------------------------------------------

	@SuppressWarnings("unchecked")
	private List<String> runCommandCompletion(BukkitCommandCompletionContext context, AikarCommand command,
											  String id, Method method) {
		try {
			method.setAccessible(true);
			switch (method.getParameterCount()) {
				case 0:
					return (List<String>) method.invoke(command);
				case 1:
					return (List<String>) method.invoke(command, context);
				default:
					throw new UnsupportedOperationException("Unsupported number of parameters.");
			}
		}
		catch (Exception exception) {
			this.plugin.warning("Could not tab complete [@" + id + "]: an error with the handler!", exception);
			return Collections.emptyList();
		}
	}

    private static Map<Method, TabComplete> getTabCompletions(Class<? extends AikarCommand> clazz) {
		Map<Method, TabComplete> completions = Maps.newHashMap();
    	if (clazz == null) {
    		return completions;
		}
		for (Method method : clazz.getDeclaredMethods()) {
			if (!Modifier.isPublic(method.getModifiers())) {
				continue;
			}
			if (!List.class.isAssignableFrom(method.getReturnType())) {
				continue;
			}
			if (method.getParameterCount() > 1) {
				continue;
			}
			TabComplete tabComplete = method.getAnnotation(TabComplete.class);
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

	public static class CustomBukkitManager extends BukkitCommandManager {

		public CustomBukkitManager(ACivMod plugin) {
			super(plugin);
		}

		public void unregisterCompletions() {
			this.completions = null;
		}

	}

}
