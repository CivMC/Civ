package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.commons.lang3.ClassUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

public class EventDebugHack extends BasicHack {

	private Map<Class<?>, List<RegisteredListener>> classToListeners;
	private Map<Class<?>, HandlerList> classToHandler;

	public EventDebugHack(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
		classToListeners = new HashMap<>();
		classToHandler = new HashMap<>();
	}

	@Override
	public void registerCommands() {
		plugin().registerCommand("debugevent", new CommandExecutor() {

			@Override
			public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
				if (args.length == 0) {
					sender.sendMessage(ChatColor.RED + "You must give an event");
					return false;
				}
				if (untargetEvent(args[0])) {
					sender.sendMessage(ChatColor.GREEN + "Disabled tracking for " + args[0]);
					return true;
				}
				targetEvent(args[0], sender);
				return true;
			}
		});
	}

	public boolean untargetEvent(String eventName) {
		Class<? extends Event> eventClass = getEventClass(eventName);
		if (eventClass == null) {
			return false;
		}
		HandlerList handler = classToHandler.get(eventClass);
		List<RegisteredListener> listeners = classToListeners.get(eventClass);
		if (handler == null || listeners == null) {
			return false;
		}
		listeners.forEach(handler::unregister);
		classToHandler.remove(eventClass);
		classToListeners.remove(eventClass);
		return true;
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Event> getEventClass(String eventName) {
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
				if (clazz != eventClass) {
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
		sb.append("At stage " + prio.toString() + " " + event.getEventName() + " is as follows: \n");
		deepInspectObject(event, "event", sb, 0, new HashSet<>());
		SimpleAdminHacks.instance().getLogger().info(sb.toString());
	}

	private static void deepInspectObject(Object o, String fieldName, StringBuilder sb, int depth, Set<Object> seen) {
		String pre = new String(new char[depth * 2]).replace("\0", "-") + " ";
		if (o == null) {
			sb.append(pre + fieldName + ": null\n");
			return;
		}
		if (ClassUtils.isPrimitiveOrWrapper(o.getClass()) || straightPrint(o)) {
			sb.append(pre + o.getClass().getSimpleName() + " " + fieldName + " = " + o.toString() + '\n');
			return;
		}
		sb.append(pre + o.getClass().getSimpleName() + " " + fieldName + ":");
		if (seen.contains(o)) {
			sb.append(pre + "omitted\n");
			return;
		} else {
			sb.append('\n');
		}
		seen.add(o);
		for (Field field : getAllFields(new LinkedList<>(), o.getClass())) {
			field.setAccessible(true);
			Object member;
			try {
				member = field.get(o);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				SimpleAdminHacks.instance().getLogger()
						.severe("Failed to get member in deep inspection " + e.getMessage());
				continue;
			}
			deepInspectObject(member, field.getName(), sb, depth + 1, seen);
		}
	}

	public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));
		if (type.getSuperclass() != null) {
			getAllFields(fields, type.getSuperclass());
		}
		return fields;
	}

	private static boolean straightPrint(Object o) {
		return o instanceof Entity || o instanceof String || o instanceof World || o instanceof Plugin
				|| o instanceof Collection || o instanceof Map || o instanceof Server || o instanceof Location
				|| o instanceof Block || o.getClass().isEnum() || o instanceof HandlerList || o instanceof Random
				|| o instanceof Logger || o instanceof ItemStack;
	}

}
