package com.programmerdan.minecraft.simpleadminhacks.hacks.basic.EventHandlerList;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import vg.civcraft.mc.civmodcore.command.AikarCommand;
import vg.civcraft.mc.civmodcore.util.TextUtil;

@CommandPermission("simpleadmin.eventdebug")
final class _HandlersCommand extends AikarCommand {

	private final _HandlersList handlers;

	public _HandlersCommand(final _HandlersList handlers) {
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
			if (!TextUtil.stringEquals(entry.getKey().getName(), handler)) {
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
			if (!TextUtil.startsWith(path, context.getInput())) {
				continue;
			}
			results.add(path);
		}
		return results;
	}

}
