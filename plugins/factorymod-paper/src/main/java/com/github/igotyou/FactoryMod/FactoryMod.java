package com.github.igotyou.FactoryMod;

import com.github.igotyou.FactoryMod.commands.FMCommandManager;
import com.github.igotyou.FactoryMod.compaction.CompactedConfigItemModifier;
import com.github.igotyou.FactoryMod.compaction.CompactedItemListener;
import com.github.igotyou.FactoryMod.listeners.CitadelListener;
import com.github.igotyou.FactoryMod.listeners.FactoryModListener;
import com.github.igotyou.FactoryMod.utility.FactoryModPermissionManager;
import vg.civcraft.mc.civmodcore.ACivMod;

public class FactoryMod extends ACivMod {
	private FactoryModManager manager;
	private static FactoryMod plugin;
	private FactoryModPermissionManager permissionManager;
	private FMCommandManager commandManager;

	@Override
	public void onEnable() {
		super.onEnable();
		plugin = this;
		registerConfigClass(CompactedConfigItemModifier.class);
		ConfigParser cp = new ConfigParser(this);
		manager = cp.parse();
		manager.loadFactories();
		if (manager.isCitadelEnabled()) {
			permissionManager = new FactoryModPermissionManager();
		}
		commandManager = new FMCommandManager(this);
		registerListeners();
		info("Successfully enabled");
	}

	@Override
	public void onDisable() {
		manager.shutDown();
		plugin.info("Shutting down");
	}

	public FactoryModManager getManager() {
		return manager;
	}

	public static FactoryMod getInstance() {
		return plugin;
	}
	
	public FactoryModPermissionManager getPermissionManager() {
		return permissionManager;
	}

	private void registerListeners() {
		registerListener(new FactoryModListener(this.manager));
		registerListener(new CompactedItemListener());
		if (this.manager.isCitadelEnabled()) {
			registerListener(new CitadelListener());
		}
	}
}
