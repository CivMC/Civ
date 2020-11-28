package com.programmerdan.minecraft.simpleadminhacks.hacks.basic.EventHandlerList;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

final class _HandlersList implements Listener {

	private final Map<Class<? extends Event>, Map<Plugin, Set<Class<? extends Listener>>>> handlers = new HashMap<>();

	private boolean handlersCollected = false;

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
