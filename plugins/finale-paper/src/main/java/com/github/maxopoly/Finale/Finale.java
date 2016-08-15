package com.github.maxopoly.finale;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import com.github.maxopoly.finale.external.CombatTagPlusManager;
import com.github.maxopoly.finale.external.ProtocolLibManager;
import com.github.maxopoly.finale.listeners.PearlCoolDownListener;
import com.github.maxopoly.finale.listeners.PlayerListener;
import com.github.maxopoly.finale.listeners.WeaponModificationListener;

import vg.civcraft.mc.civmodcore.ACivMod;

public class Finale extends ACivMod {

	private static Finale instance;
	private static FinaleManager manager;
	private static CombatTagPlusManager ctpManager;
	private static ProtocolLibManager protocolLibManager;
	
	private ConfigParser config;

	public void onEnable() {
		instance = this;
		config = new ConfigParser(this);
		manager = config.parse();
		initExternalManagers();
		registerListener();
	}

	public void onDisable() {

	}

	public static Finale getPlugin() {
		return instance;
	}

	public static FinaleManager getManager() {
		return manager;
	}

	public static CombatTagPlusManager getCombatTagPlusManager() {
		return ctpManager;
	}
	
	public static ProtocolLibManager getProtocolLibManager() {
		return protocolLibManager;
	}

	public String getPluginName() {
		return "Finale";
	}

	private void registerListener() {
		// So far the player listener is only needed if regen or attack speed is enabled.
		if (manager.isAttackSpeedEnabled() || manager.isRegenHandlerEnabled()) {
			Bukkit.getPluginManager().registerEvents(new PlayerListener(manager), this);
		}
		// So far the pearl listener, CTP manager and ProtocolLib manager are only needed if pearl cooldown changes are enabled.
		if (config.isPearlEnabled()) {
			Bukkit.getPluginManager().registerEvents(
					new PearlCoolDownListener(config.getPearlCoolDown(), config.combatTagOnPearl(), ctpManager), this);
		}
		Bukkit.getPluginManager().registerEvents(new WeaponModificationListener(), this);
	}

	private void initExternalManagers() {
		if (!config.isPearlEnabled())  return;
		// Only set up these managers if pearl cooldown change is in effect, otherwise move on; better not to put hooks in that go unused.
		PluginManager plugins = Bukkit.getPluginManager();
		if (plugins.isPluginEnabled("CombatTagPlus")) {
			ctpManager = new CombatTagPlusManager();
		}
		if (plugins.isPluginEnabled("ProtocolLib")) {
			protocolLibManager = new ProtocolLibManager();
		}
	}

}
