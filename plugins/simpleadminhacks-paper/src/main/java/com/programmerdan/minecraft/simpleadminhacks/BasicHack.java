package com.programmerdan.minecraft.simpleadminhacks;

import java.lang.reflect.Field;
import java.util.logging.Level;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * Utility automating a lot of the boiler plate required by SimpleHack and thus allowing easier creation
 * of small hacks
 *
 */
public abstract class BasicHack extends SimpleHack<BasicHackConfig> implements Listener {

	public BasicHack(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerListeners() {
		SimpleAdminHacks.instance().registerListener(this);
	}

	@Override
	public void registerCommands() {
		//override in subclass if needed
	}

	@Override
	public void dataBootstrap() {
		//override in subclass if needed
	}

	@Override
	public void unregisterListeners() {
		HandlerList.unregisterAll(this);
		
	}

	@Override
	public void unregisterCommands() {
		//override in subclass if needed
	}

	@Override
	public void dataCleanup() {
		//override in subclass if needed
		
	}

	@Override
	public String status() {
		StringBuilder genStatus = new StringBuilder();
		genStatus.append(this.getClass().getSimpleName());
		genStatus.append(" is ");
		if (config == null || !config.isEnabled()) {
			genStatus.append("disabled");
			return genStatus.toString();
		}
		genStatus.append("enabled\n");
		for(Field field : this.getClass().getDeclaredFields()) {
			genStatus.append(field.getName());
			genStatus.append(" = ");
			field.setAccessible(true);
			try {
				genStatus.append(field.get(this));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				plugin().log(Level.WARNING, "Failed to read field", e);
			}
			genStatus.append('\n');
		}
		return genStatus.toString();
	}

}
