package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

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
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import vg.civcraft.mc.civmodcore.command.AikarCommand;
import vg.civcraft.mc.civmodcore.command.AikarCommandManager;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class EventHandlerList extends BasicHack {

	private AikarCommandManager commands;

	public EventHandlerList(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerCommands() {
		this.commands = new AikarCommandManager(plugin()) {
			@Override
			public void registerCommands() {
				registerCommand(new HandlersCommand());
			}
		};
	}

	@Override
	public void unregisterCommands() {
		if (this.commands != null) {
			this.commands.reset();
			this.commands = null;
		}
	}

	@Override
	public String status() {
		return EventHandlerList.class.getSimpleName() + " is " + (isEnabled() ? "enabled" : "disabled") + ".";
	}

	@CommandPermission("simpleadmin.eventdebug")
	public static class HandlersCommand extends AikarCommand {

		@CommandAlias("handlers|handler|listeners|listener")
		@Description("Gets the details for a handler.")
		@Syntax("<handler>")
		@CommandCompletion("@handlers")
		public void onListHandlers(CommandSender sender, @Single String handler) {
			if (Strings.isNullOrEmpty(handler)) {
				throw new InvalidCommandArgument("Please enter an event handler.");
			}
			sender.sendMessage(ChatColor.GOLD + "Getting handlers for: " + ChatColor.WHITE + handler);
			for (Map.Entry<Class<? extends Event>, Map<Plugin, Set<Class<? extends Listener>>>> entry :
					getPluginEventHandlers().entrySet()) {
				if (!TextUtil.stringEquals(entry.getKey().getName(), handler)) {
					continue;
				}
				for (Map.Entry<Plugin, Set<Class<? extends Listener>>> details : entry.getValue().entrySet()) {
					sender.sendMessage(ChatColor.LIGHT_PURPLE + details.getKey().getName() + ":");
					for (Class<? extends Listener> clazz : details.getValue()) {
						sender.sendMessage(" - " + clazz.getName());
					}
				}
			}
			sender.sendMessage(ChatColor.AQUA + "End of handlers.");
		}

		@TabComplete("handlers")
		public List<String> tabCompleteEvents(BukkitCommandCompletionContext context) {
			List<String> results = new ArrayList<>();
			for (Class<? extends Event> clazz : getPluginEventHandlers().keySet()) {
				String path = clazz.getName();
				if (!TextUtil.startsWith(path, context.getInput())) {
					continue;
				}
				results.add(path);
			}
			return results;
		}

	}

	@SuppressWarnings("unchecked")
	private static Map<Class<? extends Event>, Map<Plugin, Set<Class<? extends Listener>>>> getPluginEventHandlers() {
		Map<Class<? extends Event>, Map<Plugin, Set<Class<? extends Listener>>>> results = new HashMap<>();
		for (HandlerList handlers : HandlerList.getHandlerLists()) {
			for (RegisteredListener listener : handlers.getRegisteredListeners()) {
				for (Method method : listener.getListener().getClass().getMethods()) {
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
					Class<?> clazz = method.getParameterTypes()[0];
					if (!Event.class.isAssignableFrom(clazz)) {
						continue;
					}
					results.computeIfAbsent((Class<? extends Event>) clazz, (k) -> new HashMap<>()).
							computeIfAbsent(listener.getPlugin(), (k) -> new HashSet<>()).
							add(listener.getListener().getClass());
				}
			}
		}
		return results;
	}

	public static BasicHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

}
