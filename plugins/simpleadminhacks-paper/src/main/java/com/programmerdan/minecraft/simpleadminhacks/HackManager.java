package com.programmerdan.minecraft.simpleadminhacks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;

import com.google.common.reflect.ClassPath;
import com.programmerdan.minecraft.simpleadminhacks.autoload.AutoLoad;

public class HackManager {

	private SimpleAdminHacks plugin;
	private List<SimpleHack<?>> hacks;

	public HackManager(SimpleAdminHacks plugin) {
		this.plugin = plugin;
		this.hacks = new LinkedList<>();
	}

	public void reloadHacks() {
		// Now load all the Hacks and register.
		ConfigurationSection hackConfigs = plugin.getConfig().getConfigurationSection("hacks");
		try {
			ClassPath getSamplersPath = ClassPath.from(plugin.exposeClassLoader());

			for (ClassPath.ClassInfo clsInfo : getSamplersPath
					.getTopLevelClassesRecursive("com.programmerdan.minecraft.simpleadminhacks.hacks")) {
				try {
					Class<?> clazz = clsInfo.load();
					if (clazz != null && SimpleHack.class.isAssignableFrom(clazz)) {
						plugin.log(Level.INFO,
								"Found a hack class {0}, attempting to find a generating method and constructor",
								clazz.getName());
						ConfigurationSection hackConfig = hackConfigs.getConfigurationSection(clazz.getSimpleName());
						SimpleHackConfig hackingConfig = null;
						if (hackConfig != null) {
							try {
								Method genBasic = clazz.getMethod("generate", SimpleAdminHacks.class,
										ConfigurationSection.class);
								hackingConfig = (SimpleHackConfig) genBasic.invoke(null, this, hackConfig);
							} catch (IllegalAccessException failure) {
								plugin.log(Level.WARNING,
										"Creating configuration for hack {0} failed, illegal access failure",
										clazz.getName());
							} catch (IllegalArgumentException failure) {
								plugin.log(Level.WARNING,
										"Creating configuration for hack {0} failed, illegal argument failure",
										clazz.getName());
							} catch (InvocationTargetException failure) {
								plugin.log(Level.WARNING,
										"Creating configuration for hack {0} failed, invocation target failure",
										clazz.getName());
							}
						} else {
							plugin.log(Level.INFO, "Hack for {0} found but no configuration, skipping.",
									clazz.getSimpleName());
						}

						if (hackingConfig != null) {
							plugin.log(Level.INFO, "Configuration for Hack {0} found, instance: {1}",
									clazz.getSimpleName(), hackingConfig.toString());
							SimpleHack<?> hack = null;
							try {
								Constructor<?> constructBasic = clazz.getConstructor(SimpleAdminHacks.class,
										hackingConfig.getClass());
								hack = (SimpleHack<?>) constructBasic.newInstance(this, hackingConfig);
								plugin.log(Level.INFO, "Created a new Hack of type {0}", clazz.getSimpleName());
							} catch (InvalidConfigException ice) {
								plugin.log(Level.WARNING, "Failed to activate {0} hack, configuration failed",
										clazz.getSimpleName());
							} catch (Exception e) {
								plugin.log(Level.WARNING, "Failed to activate {0} hack, configuration failed: {1}",
										clazz.getSimpleName(), e.getMessage());
							}

							if (hack == null) {
								plugin.log(Level.WARNING, "Failed to create a Hack of type {0}", clazz.getSimpleName());
							} else {
								register(hack);
								plugin.log(Level.INFO, "Registered a new hack: {0}", clazz.getSimpleName());
							}
						} else {
							plugin.log(Level.INFO, "Configuration generation for Hack {0} failed, skipping.",
									clazz.getSimpleName());
						}
					}
				} catch (NoClassDefFoundError e) {
					plugin.log(Level.INFO, "Unable to load discovered class {0} due to dependency failure",
							clsInfo.getName());
				} catch (Exception e) {
					plugin.log(Level.WARNING, "Failed to complete hack discovery {0}", clsInfo.getName());
				}
			}
		} catch (Exception e) {
			plugin.log(Level.WARNING, "Failed to complete hack registration");
		}
		// Warning if no hacks.
		if (hacks.isEmpty()) {
			plugin.log(Level.WARNING, "No hacks enabled.");
			return;
		}
		// Boot up the hacks.
		List<SimpleHack<?>> iterList = new ArrayList<>(hacks);
		for (SimpleHack<?> hack : iterList) {
			try {
				populateParameter(hack);
				hack.enable();
			} catch (NoClassDefFoundError err) {
				plugin.log(Level.WARNING, "Unable to activate hack {0}, missing dependency: {1}", hack.getName(),
						err.getMessage());
				unregister(hack);
			} catch (Exception e) {
				plugin.log(Level.WARNING, "Unable to activate hack {0}, unrecognized error: {1}", hack.getName(),
						e.getMessage());
				plugin.log(Level.WARNING, "Full stack trace: ", e);
				unregister(hack);
			}
		}
	}

	public void disableAllHacks() {
		for (SimpleHack<?> hack : hacks) {
			try {
				hack.disable();
			} catch (NoClassDefFoundError err) {
				plugin.log(Level.WARNING, "Unable to cleanly disable hack {0}, missing dependency: {1}", hack.getName(),
						err.getMessage());
			} catch (Exception e) {
				plugin.log(Level.WARNING, "Unable to cleanly disable hack {0}, unrecognized error: {1}", hack.getName(),
						e.getMessage());
				plugin.log(Level.WARNING, "Full stack trace: ", e);
			}
		}
		hacks.clear();
	}

	private void populateParameter(SimpleHack<? extends SimpleHackConfig> hack) {
		Class<?> hackClass = hack.getClass();
		ConfigurationSection config = hack.config().getBase();
		for (Field field : hackClass.getDeclaredFields()) {
			AutoLoad autoLoad = field.getAnnotation(AutoLoad.class);
			if (autoLoad == null) {
				continue;
			}
			String identifier;
			if (autoLoad.id().equals("")) {
				identifier = field.getName();
			} else {
				identifier = autoLoad.id();
			}
			Object value;
			try {
				value = config.getObject(identifier, field.getClass(), null);
			} catch (Exception e) {
				throw new IllegalArgumentException("Hack " + hackClass.getSimpleName() + " failed to read parameter "
						+ identifier, e);
			}
			field.setAccessible(true);
			try {
				field.set(hack, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new IllegalStateException(field.getClass().getSimpleName() + " in " + hackClass.getSimpleName()
						+ " could not be set " + e.toString());
			}
			plugin.log(Level.INFO, "Loaded '{0}' = '{1}'", identifier, value);
		}
	}

	/**
	 * Registers a new SimpleHack.
	 */
	void register(SimpleHack<?> hack) {
		hacks.add(hack);
	}

	/**
	 * Unregisters an existing SimpleHack.
	 */
	void unregister(SimpleHack<?> hack) {
		hacks.remove(hack);
	}

	/**
	 * Returns a wrapped version of hacks preventing external removal but allowing
	 * interaction with the hacks.
	 */
	public List<SimpleHack<? extends SimpleHackConfig>> getHacks() {
		return Collections.unmodifiableList(hacks);
	}

}
