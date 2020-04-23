package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ClassUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

import com.programmerdan.minecraft.simpleadminhacks.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;

public class EventDebugHack extends BasicHack {

	private Map<Class<?>, List<RegisteredListener>> classToListeners;
	private Map<Class<?>, HandlerList> classToHandler;

	public EventDebugHack(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
		classToListeners = new HashMap<>();
		classToHandler = new HashMap<>();
	}

	public static BasicHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

	public void untargetEvent(String eventName) {
		Class<? extends Event> eventClass = getEventClass(eventName);
		if (eventClass == null) {
			return;
		}
		HandlerList handler = classToHandler.get(eventClass);
		List<RegisteredListener> listeners = classToListeners.get(eventClass);
		if (handler == null || listeners == null) {
			return;
		}
		listeners.forEach(handler::unregister);
		classToHandler.remove(eventClass);
		classToListeners.remove(eventClass);
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Event> getEventClass(String eventName) {
		if (eventName.startsWith(".")) {
			eventName = "org.bukkit.event" + eventName;
		}
		try {
			return (Class<Event>) Class.forName(eventName);
		} catch (ClassCastException | ClassNotFoundException e) {
			return null;
		}
	}

	public void targetEvent(String eventName, CommandSender sender) {
		Class<? extends Event> eventClass = getEventClass(eventName);
		if (eventClass == null) {
			sender.sendMessage(ChatColor.RED + "No event class with that path exists");
			return;
		}
		if (classToHandler.containsKey(eventClass)) {
			sender.sendMessage(ChatColor.RED + "Event is already being investigated");
			return;
		}
		Method handlerGetter;
		try {
			handlerGetter = eventClass.getMethod("getHandlerList");
		} catch (NoSuchMethodException | SecurityException e) {
			sender.sendMessage(ChatColor.RED + "The class did not have a handler getter");
			return;
		}
		if (handlerGetter.getReturnType() != HandlerList.class) {
			sender.sendMessage(ChatColor.RED + "Getter had wrong return type");
			return;
		}
		HandlerList handlerList;
		try {
			handlerList = (HandlerList) handlerGetter.invoke(eventClass);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sender.sendMessage(ChatColor.RED + "Could not invoke handler getter");
			return;
		}
		sender.sendMessage(
				ChatColor.GOLD + "Currently registered listeners for " + eventClass.getSimpleName() + " are:");
		for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
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
				if (Event.class.isAssignableFrom(clazz)) {
					continue;
				}
				sender.sendMessage(String.format("[%s] on %s is %s", listener.getPlugin().getName(),
						listener.getPriority(), listener.getListener().getClass().getSimpleName()));
			}
		}
		Listener fakeListener = new Listener() {
		};
		// need to hack our listener in, because can't dynamically declare an actual
		// class with annotations etc.
		List<RegisteredListener> listeners = new LinkedList<>();
		for (EventPriority prio : EventPriority.values()) {
			RegisteredListener listener = new RegisteredListener(fakeListener, new EventExecutor() {

				@Override
				public void execute(Listener arg0, Event arg1) throws EventException {
					printEventState(prio, arg1);
				}
			}, prio, SimpleAdminHacks.instance(), false);
			listeners.add(listener);
			handlerList.register(listener);
		}
		classToHandler.put(eventClass, handlerList);
		classToListeners.put(eventClass, listeners);
	}

	private void printEventState(EventPriority prio, Event event) {
		StringBuilder sb = new StringBuilder();
		sb.append("At stage " + prio.toString() + " " + event.getEventName() + " is as follows: ");
		deepInspectObject(event, "event", sb, 0);
		SimpleAdminHacks.instance().getLogger().info(sb.toString());
	}

	private void deepInspectObject(Object o, String fieldName, StringBuilder sb, int depth) {
		String pre = new String(new char[depth]).replace("\0", "-") + " ";
		if (o == null) {
			sb.append(pre + fieldName + ": null");
			return;
		}
		if (ClassUtils.isPrimitiveOrWrapper(o.getClass()) || straightPrint(o)) {
			sb.append(pre + o.getClass().getSimpleName() + " " + fieldName + " = " + o.toString());
			return;
		}
		sb.append(pre + o.getClass().getSimpleName() + " " + fieldName + ":");
		for (Field field : this.getClass().getFields()) {
			Object member;
			try {
				member = field.get(o);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				SimpleAdminHacks.instance().getLogger()
						.severe("Failed to get member in deep inspection " + e.getMessage());
				continue;
			}
			deepInspectObject(member, field.getName(), sb, depth + 1);
		}
	}

	private static boolean straightPrint(Object o) {
		return o instanceof LivingEntity || o instanceof String || o instanceof World || o instanceof Plugin
				|| o instanceof Collection || o instanceof Map;
	}

}
