package com.programmerdan.minecraft.simpleadminhacks.framework;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.reflect.ClassPath;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.exceptions.InvalidConfigException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

public final class HackManager {

	private static final String HACKS_PATH = "com.programmerdan.minecraft.simpleadminhacks.hacks";

	private final SimpleAdminHacks plugin;
	private final List<SimpleHack<?>> hacks;

	public HackManager(final SimpleAdminHacks plugin) {
		this.plugin = plugin;
		this.hacks = new LinkedList<>();
	}

	@SuppressWarnings({"UnstableApiUsage", "unchecked"})
	public void loadAllHacks() {
		// Now load all the Hacks and register.
		final ConfigurationSection hackConfigs = plugin.getConfig().getConfigurationSection("hacks");
		if (hackConfigs == null) {
			this.plugin.warning("There are no hacks defined under 'hacks' the config. Is this right?");
			return;
		}
		try {
			final ClassPath getSamplersPath = ClassPath.from(plugin.exposeClassLoader());
			for (final var classInfo : getSamplersPath.getTopLevelClassesRecursive(HACKS_PATH)) {
				try {
					final Class<?> clazz = classInfo.load();
					if (clazz == null || !SimpleHack.class.isAssignableFrom(clazz)) {
						continue;
					}
					this.plugin.info("Found hack class [" + clazz.getName() + "]");
					loadHack((Class<SimpleHack<?>>) clazz, hackConfigs.getConfigurationSection(clazz.getSimpleName()));
				}
				catch (final NoClassDefFoundError exception) {
					this.plugin.warning("Unable to load hack \"" + classInfo.getSimpleName() + "\" probably due to a " +
							"dependency / import error.", exception);
					//continue;
				}
				catch (final Exception exception) {
					this.plugin.warning("Failed to complete hack discovery of: " + classInfo.getName(), exception);
					//continue;
				}
			}
		}
		catch (final Exception exception) {
			this.plugin.warning("Failed to complete hack registration");
			exception.printStackTrace();
			return;
		}
		if (this.hacks.isEmpty()) {
			this.plugin.warning("No hacks have been loaded.");
			//return;
		}
	}

	public SimpleHack<?> loadHack(final Class<SimpleHack<?>> hackClass, final ConfigurationSection config) {
		Preconditions.checkNotNull(hackClass, "Hack class cannot be null!");
		if (config == null) {
			this.plugin.warning("Config for \"" + hackClass.getSimpleName() + "\" not defined, skipping.");
			return null;
		}
		final String hackName = hackClass.getSimpleName();
		SimpleHackConfig hackConfig;
		try {
			hackConfig = (SimpleHackConfig) MethodUtils.invokeExactStaticMethod(hackClass,"generate",
					new Object[] { this.plugin, config },
					new Class[] { SimpleAdminHacks.class, ConfigurationSection.class });
		}
		catch (final NoSuchMethodException exception) {
			this.plugin.warning("Could not find config generator for \"" + hackName + "\", skipping.");
			return null;
		}
		catch (final IllegalAccessException exception) {
			this.plugin.warning("Could not access config generator for \"" + hackName + "\", skipping.");
			exception.printStackTrace();
			return null;
		}
		catch (final IllegalArgumentException exception) {
			this.plugin.warning("Config generator for \"" + hackName + "\" threw an argument exception, skipping.");
			exception.printStackTrace();
			return null;
		}
		catch (final InvocationTargetException exception) {
			this.plugin.warning("Config generator for \"" + hackName + "\" threw an invocation target exception, " +
					"skipping.");
			exception.printStackTrace();
			return null;
		}
		if (hackConfig == null) {
			this.plugin.warning("Config generator for \"" + hackName + "\" returned nothing, skipping.");
			return null;
		}
		final var configClass = hackConfig.getClass();
		if (configClass.isAnonymousClass()) {
			this.plugin.warning("Config for \"" + hackName + "\" is anonymous, skipping.");
			return null;
		}
		this.plugin.info("Config for \"" + hackName + "\" found; instance [" + hackConfig.toString() + "]");
		SimpleHack<?> hack;
		try {
			hack = hackClass.getConstructor(SimpleAdminHacks.class, configClass).newInstance(this.plugin, hackConfig);
		}
		catch (final InvalidConfigException exception) {
			this.plugin.warning("Failed to initialise \"" + hackName + "\"'s configuration", exception);
			return null;
		}
		catch (final Exception exception) {
			this.plugin.warning("Failed to initialise \"" + hackName + "\"", exception);
			return null;
		}
		register(hack);
		this.plugin.info("Registered hack: " + hackName);
		interactWithHack(hack, (finalHack) -> {
			finalHack.onLoad();
			if (finalHack.shouldEnable()) {
				finalHack.enable();
			}
		});
		return hack;
	}

	public void enableAllHacks() {
		for (final SimpleHack<?> hack : hacks) {
			enableHack(hack);
		}
	}

	public void enableHack(final SimpleHack<?> hack) {
		Preconditions.checkNotNull(hack, "Hack cannot be null!");
		interactWithHack(hack, (ignored) -> {
			if (!hack.isEnabled()) {
				hack.enable();
			}
		});
	}

	public void disableAllHacks() {
		for (final SimpleHack<?> hack : hacks) {
			disableHack(hack);
		}
	}

	public void disableHack(final SimpleHack<?> hack) {
		Preconditions.checkNotNull(hack, "Hack cannot be null!");
		interactWithHack(hack, (ignored) -> {
			if (hack.isEnabled()) {
				hack.disable();
			}
		});
	}

	/**
	 * Registers a new SimpleHack.
	 */
	void register(final SimpleHack<?> hack) {
		this.hacks.add(hack);
	}

	/**
	 * Returns a wrapped version of hacks preventing external removal but allowing
	 * interaction with the hacks.
	 */
	public List<SimpleHack<? extends SimpleHackConfig>> getHacks() {
		return Collections.unmodifiableList(this.hacks);
	}

	public SimpleHack<? extends SimpleHackConfig> getHack(final String name) {
		if (Strings.isNullOrEmpty(name)) {
			return null;
		}
		for (final SimpleHack<? extends SimpleHackConfig> candidate : this.hacks) {
			if (StringUtils.equalsIgnoreCase(name, candidate.getClass().getSimpleName())) {
				return candidate;
			}
		}
		return null;
	}

	private void interactWithHack(final SimpleHack<?> hack, final Consumer<SimpleHack<?>> processor) {
		Preconditions.checkNotNull(hack, "Cannot interact with a null hack!");
		Preconditions.checkNotNull(processor, "Cannot interact with a null processor!");
		try {
			processor.accept(hack);
		}
		catch (final NoClassDefFoundError exception) {
			this.plugin.warning("Unable to interact with hack \"" + hack.getClass().getSimpleName() + "\" probably " +
					"due to a dependency / import error.", exception);
		}
		catch (final Exception exception) {
			this.plugin.warning("Unable to deactivate hack \"" + hack.getClass().getSimpleName() + "\"", exception);
		}
	}

}
