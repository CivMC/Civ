package com.programmerdan.minecraft.banstick;

import com.programmerdan.minecraft.banstick.data.BSLog;
import com.programmerdan.minecraft.banstick.data.BSRegistrars;
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

	@SuppressWarnings("unused")
	private BanStickCommandHandler commandHandler;
	private BanStickEventHandler eventHandler;
	private BanStickDatabaseHandler databaseHandler;
	private BanStickTorUpdater torUpdater;
	private BanStickProxyHandler proxyHandler;
	private BanStickIPDataHandler ipDataUpdater;
	private BanStickIPHubHandler ipHubUpdater;
	private BanStickScrapeHandler scrapeHandler;
	private BanStickImportHandler importHandler;
	private BSLog logHandler;
	private BSRegistrars bannedRegistrars;

	private boolean slaveMode;

	@Override
	public void onEnable() {
		instance = this;
		super.onEnable();

		saveDefaultConfig();

		connectDatabase();
		if (!isEnabled()) {
			return;
		}

		if (getConfig().getBoolean("slaveMode", false)) {
			this.slaveMode = true;
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
		registerRegistrarHandler();
	}

	@Override
	public void onDisable() {
		if (this.eventHandler != null) {
			this.eventHandler.shutdown();
		}
		if (this.proxyHandler != null) {
			this.proxyHandler.shutdown();
		}
		if (this.scrapeHandler != null) {
			this.scrapeHandler.shutdown();
		}
		if (this.ipDataUpdater != null) {
			this.ipDataUpdater.end();
		}
		if (this.ipHubUpdater != null) {
			this.ipHubUpdater.end();
		}
		if (this.torUpdater != null) {
			this.torUpdater.shutdown();
		}
		if (this.importHandler != null) {
			this.importHandler.shutdown();
		}
		if (this.logHandler != null) {
			this.logHandler.disable();
		}
		if (this.databaseHandler != null) {
			this.databaseHandler.doShutdown();
		}

		super.onDisable();
	}

	private void connectDatabase() {
		try {
			this.databaseHandler = new BanStickDatabaseHandler(getConfig());
		} catch (Exception e) {
			severe("Failed to establish database", e);
			setEnabled(false);
		}
	}

	public BanStickIPDataHandler getIPDataHandler() {
		return this.ipDataUpdater;
	}

	public BanStickIPHubHandler getIPHubHandler() {
		return this.ipHubUpdater;
	}

	public BanStickEventHandler getEventHandler() {
		return this.eventHandler;
	}

	private void registerCommandHandler() {
		if (!isEnabled()) {
			return;
		}
		try {
			this.commandHandler = new BanStickCommandHandler(getConfig());
		} catch (Exception e) {
			severe("Failed to set up command handling", e);
			setEnabled(false);
		}
	}

	private void registerEventHandler() {
		if (!isEnabled()) {
			return;
		}
		try {
			this.eventHandler = new BanStickEventHandler(getConfig());
		} catch (Exception e) {
			severe("Failed to set up event capture / handling", e);
			setEnabled(false);
		}
	}

	private void registerTorHandler() {
		if (!isEnabled()) {
			return;
		}
		try {
			this.torUpdater = new BanStickTorUpdater(getConfig());
		} catch (Exception e) {
			severe("Failed to set up TOR updater!", e);
		}
	}

	private void registerProxyHandler() {
		if (!isEnabled()) {
			return;
		}
		try {
			this.proxyHandler = new BanStickProxyHandler(getConfig(), getClassLoader());
		} catch (Exception e) {
			severe("Failed to set up Proxy updaters!", e);
		}
	}

	private void registerIPDataHandler() {
		if (!isEnabled()) {
			return;
		}
		try {
			this.ipDataUpdater = new BanStickIPDataHandler(getConfig());
		} catch (Exception e) {
			severe("Failed to set up dynamic IPData updater!", e);
		}
	}

	private void registerIPHubHandler() {
		if (!isEnabled()) {
			return;
		}
		try {
			this.ipHubUpdater = new BanStickIPHubHandler(getConfig());
		} catch (Exception e) {
			severe("Failed to set up dynamic IPHub.info updater!", e);
		}
	}

	private void registerScrapeHandler() {
		if (!isEnabled()) {
			return;
		}
		try {
			this.scrapeHandler = new BanStickScrapeHandler(getConfig(), getClassLoader());
		} catch (Exception e) {
			severe("Failed to set up anonymous proxy scrapers", e);
		}
	}

	private void registerImportHandler() {
		if (!isEnabled()) {
			return;
		}
		try {
			this.importHandler = new BanStickImportHandler(getConfig(), getClassLoader());
		} catch (Exception e) {
			severe("Failed to set up data imports", e);
		}
	}

	private void registerLogHandler() {
		if (!isEnabled()) {
			return;
		}
		try {
			this.logHandler = new BSLog(getConfig());
			this.logHandler.runTaskTimerAsynchronously(this, this.logHandler.getDelay(), this.logHandler.getPeriod());
		} catch (Exception e) {
			severe("Failed to set up ban log handler", e);
		}
	}
	
	private void registerRegistrarHandler() {
		if (!isEnabled()) {
			return;
		}
		try {
			this.bannedRegistrars = new BSRegistrars();
		} catch (Exception e) {
			severe("Failed to set up registrar ban handler", e);
		}
	}
	
	public BSRegistrars getRegistrarHandler() {
		return bannedRegistrars;
	}

	public BSLog getLogHandler() {
		return this.logHandler;
	}

	/**
	 * @return the static global instance. Not my fav pattern, but whatever.
	 */
	public static BanStick getPlugin() {
		return instance;
	}

	public void saveCache() {
		this.databaseHandler.doShutdown();
	}

	public static boolean slave() {
		return instance.slaveMode;
	}

}
