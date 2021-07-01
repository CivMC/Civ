package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.google.common.base.Strings;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import vg.civcraft.mc.civmodcore.commands.CommandManager;
import vg.civcraft.mc.civmodcore.commands.TabComplete;

public final class EventHandlerList extends BasicHack {

	private final CommandManager commands;
	private final HandlersList handlers;

	public EventHandlerList(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		this.handlers = new HandlersList();
		this.commands = new CommandManager(plugin) {
			@Override
			public void registerCommands() {
				registerCommand(new HandlersCommand(handlers));
			}
		};
	}

	@Override
	public void onEnable() {
		super.onEnable();
		this.commands.init();
		this.plugin.registerListener(this.handlers);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this.handlers);
		this.commands.reset();
		super.onDisable();
	}

	// ------------------------------------------------------------
	// Handlers Command
	// ------------------------------------------------------------

	@CommandPermission("simpleadmin.eventdebug")
	private static final class HandlersCommand extends BaseCommand {

		private final HandlersList handlers;

		public HandlersCommand(final HandlersList handlers) {
			this.handlers = handlers;
		}

		@CommandAlias("handlers|handler|listeners|listener")
		@Description("Gets the details for a handler.")
		@Syntax("<handler>")
		@CommandCompletion("@handlers")
		public void onListHandlers(final CommandSender sender, @Single final String handler) {
			if (Strings.isNullOrEmpty(handler)) {
				throw new InvalidCommandArgument("Please enter an event handler.");
			}
			sender.sendMessage(ChatColor.GOLD + "Getting handlers for: " + ChatColor.WHITE + handler);
			for (final Map.Entry<Class<? extends Event>, Map<Plugin, Set<Class<? extends Listener>>>> entry :
					this.handlers.getHandlerCache().entrySet()) {
				if (!StringUtils.equals(entry.getKey().getName(), handler)) {
					continue;
				}
				for (final Map.Entry<Plugin, Set<Class<? extends Listener>>> details : entry.getValue().entrySet()) {
					sender.sendMessage(ChatColor.LIGHT_PURPLE + details.getKey().getName() + ":");
					for (final Class<? extends Listener> clazz : details.getValue()) {
						sender.sendMessage(" - " + clazz.getName());
					}
				}
			}
			sender.sendMessage(ChatColor.AQUA + "End of handlers.");
		}

		@TabComplete("handlers")
		public List<String> tabCompleteEvents(final BukkitCommandCompletionContext context) {
			final List<String> results = new ArrayList<>();
			for (final Class<? extends Event> clazz : this.handlers.getHandlerCache().keySet()) {
				final String path = clazz.getName();
				if (!StringUtils.startsWith(path, context.getInput())) {
					continue;
				}
				results.add(path);
			}
			return results;
		}

	}

	// ------------------------------------------------------------
	// Handlers List
	// ------------------------------------------------------------

	private static final class HandlersList implements Listener {

		private final Map<Class<? extends Event>, Map<Plugin, Set<Class<? extends Listener>>>> handlers;
		private boolean handlersCollected;

		public HandlersList() {
			this.handlers = new HashMap<>();
			this.handlersCollected = false;
		}

		@SuppressWarnings("unchecked")
		public Map<Class<? extends Event>, Map<Plugin, Set<Class<? extends Listener>>>> getHandlerCache() {
			if (!this.handlersCollected) {
				this.handlers.clear();
				for (final HandlerList handlers : HandlerList.getHandlerLists()) {
					for (final RegisteredListener listener : handlers.getRegisteredListeners()) {
						for (final Method method : listener.getListener().getClass().getMethods()) {
							if (method.isBridge() || method.isSynthetic()) {
								continue;
							}
							if (Modifier.isStatic(method.getModifiers())) {
								continue;
							}
							if (!method.isAnnotationPresent(EventHandler.class)) {
								continue;
							}
							if (method.getParameterCount() != 1) {
								continue;
							}
							final Class<?> clazz = method.getParameterTypes()[0];
							if (!Event.class.isAssignableFrom(clazz)) {
								continue;
							}
							this.handlers.
									computeIfAbsent((Class<? extends Event>) clazz, (k) -> new HashMap<>()).
									computeIfAbsent(listener.getPlugin(), (k) -> new HashSet<>()).
									add(listener.getListener().getClass());
						}
					}
				}
				this.handlersCollected = true;
			}
			return this.handlers;
		}

		@EventHandler
		public void onPluginEnable(final PluginEnableEvent event) {
			this.handlersCollected = false;
		}

		@EventHandler
		public void onPluginDisable(final PluginDisableEvent event) {
			this.handlersCollected = false;
		}

	}

}
