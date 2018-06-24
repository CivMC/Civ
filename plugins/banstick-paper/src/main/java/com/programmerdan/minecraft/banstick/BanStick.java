package com.programmerdan.minecraft.banstick;

import com.programmerdan.minecraft.banstick.data.BSLog;
import com.programmerdan.minecraft.banstick.handler.BanStickCommandHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickDatabaseHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickEventHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickIPDataHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickIPHubHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickImportHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickProxyHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickScrapeHandler;
import com.programmerdan.minecraft.banstick.handler.BanStickTorUpdater;
import vg.civcraft.mc.civmodcore.ACivMod;

public class BanStick extends ACivMod {
	private static BanStick instance;
	private BanStickCommandHandler commandHandler;
	private BanStickEventHandler eventHandler;
	private BanStickDatabaseHandler databaseHandler;
	private BanStickTorUpdater torUpdater;
	private BanStickProxyHandler proxyHandler;
	private BanStickIPDataHandler ipdataUpdater;
	private BanStickIPHubHandler ipHubUpdater;
	private BanStickScrapeHandler scrapeHandler;
	private BanStickImportHandler importHandler;
	private BSLog logHandler;

	private boolean slaveMode = false;

	@Override
	public void onEnable() {
		super.onEnable();

		saveDefaultConfig();
		reloadConfig();

		BanStick.instance = this;
		connectDatabase();
		if (!this.isEnabled()) return;

		if (getConfig().getBoolean("slaveMode", false)) {
			slaveMode = true;
		}

		registerEventHandler();
		registerCommandHandler();
		registerTorHandler();
		registerProxyHandler();
		registerIPDataHandler();
		registerIPHubHandler();
		registerScrapeHandler();
		registerImportHandler();
		registerLogHandler();
	}

	@Override
	public void onDisable() {
		super.onDisable();

		if (this.eventHandler != null) this.eventHandler.shutdown();
		if (this.proxyHandler != null) this.proxyHandler.shutdown();
		if (this.scrapeHandler != null) this.scrapeHandler.shutdown();
		if (this.ipdataUpdater != null) this.ipdataUpdater.end();
		if (this.ipHubUpdater != null) this.ipHubUpdater.end();
		if (this.torUpdater != null) this.torUpdater.shutdown();
		if (this.importHandler != null) this.importHandler.shutdown();
		if (this.logHandler != null) this.logHandler.disable();
		if (this.databaseHandler != null) this.databaseHandler.doShutdown();
	}

	private void connectDatabase() {
		try {
			this.databaseHandler = new BanStickDatabaseHandler(getConfig());
		} catch (Exception e) {
			this.severe("Failed to establish database", e);
			this.setEnabled(false);
		}
	}

	public BanStickIPDataHandler getIPDataHandler() {
		return this.ipdataUpdater;
	}

	public BanStickIPHubHandler getIPHubHandler() {
		return this.ipHubUpdater;
	}

	public BanStickEventHandler getEventHandler() {
		return this.eventHandler;
	}

	private void registerCommandHandler() {
		if (!this.isEnabled()) return;
		try {
			this.commandHandler = new BanStickCommandHandler(getConfig());
		} catch (Exception e) {
			this.severe("Failed to set up command handling", e);
			this.setEnabled(false);
		}
	}

	private void registerEventHandler() {
		if (!this.isEnabled()) return;
		try {
			this.eventHandler = new BanStickEventHandler(getConfig());
		} catch (Exception e) {
			this.severe("Failed to set up event capture / handling", e);
			this.setEnabled(false);
		}
	}

	private void registerTorHandler() {
		if (!this.isEnabled()) return;
		try {
			this.torUpdater = new BanStickTorUpdater(getConfig());
		} catch (Exception e) {
			this.severe("Failed to set up TOR updater!", e);
		}
	}

	private void registerProxyHandler() {
		if (!this.isEnabled()) return;
		try {
			this.proxyHandler = new BanStickProxyHandler(getConfig(), getPlugin().getClassLoader());
		} catch (Exception e) {
			this.severe("Failed to set up Proxy updaters!", e);
		}
	}

	private void registerIPDataHandler() {
		if (!this.isEnabled()) return;
		try {
			this.ipdataUpdater = new BanStickIPDataHandler(getConfig());
		} catch (Exception e) {
			this.severe("Failed to set up dynamic IPData updater!", e);
		}
	}

	private void registerIPHubHandler() {
		if (!this.isEnabled()) return;
		try {
			this.ipHubUpdater = new BanStickIPHubHandler(getConfig());
		} catch (Exception e) {
			this.severe("Failed to set up dynamic IPHub.info updater!", e);
		}
	}

	private void registerScrapeHandler() {
		if (!this.isEnabled()) return;
		try {
			this.scrapeHandler = new BanStickScrapeHandler(getConfig(), getPlugin().getClassLoader());
		} catch (Exception e) {
			this.severe("Failed to set up anonymous proxy scrapers", e);
		}
	}

	private void registerImportHandler() {
		if (!this.isEnabled()) return;
		try {
			this.importHandler = new BanStickImportHandler(getConfig(), getPlugin().getClassLoader());
		} catch (Exception e) {
			this.severe("Failed to set up data imports", e);
		}
	}

	private void registerLogHandler() {
		if (!this.isEnabled()) return;
		try {
			this.logHandler = new BSLog(getConfig());
			this.logHandler.runTaskTimerAsynchronously(this, this.logHandler.getDelay(), this.logHandler.getPeriod());
		} catch (Exception e) {
			this.severe("Failed to set up ban log handler", e);
		}
	}

	public BSLog getLogHandler() {
		return this.logHandler;
	}

	/**
	 *
	 * @return the static global instance. Not my fav pattern, but whatever.
	 */
	public static BanStick getPlugin() {
		return BanStick.instance;
	}

	/**
	 *
	 * @return the name of this plugin.
	 */
	@Override
	protected String getPluginName() {
		return "BanStick";
	}

	public void saveCache() {
		this.databaseHandler.doShutdown();
	}

	public static boolean slave() {
		return BanStick.instance.slaveMode;
	}
}
