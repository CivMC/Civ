package com.github.maxopoly.finale;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

import com.github.maxopoly.finale.external.CombatTagPlusManager;
import com.github.maxopoly.finale.listeners.DamageListener;
import com.github.maxopoly.finale.listeners.EnchantmentDisableListener;
import com.github.maxopoly.finale.listeners.PearlCoolDownListener;
import com.github.maxopoly.finale.listeners.PlayerListener;
import com.github.maxopoly.finale.listeners.PotionListener;
import com.github.maxopoly.finale.listeners.VelocityFixListener;
import com.github.maxopoly.finale.listeners.WeaponModificationListener;

import vg.civcraft.mc.civmodcore.ACivMod;

public class Finale extends ACivMod {

	private static Finale instance;

	public static Finale getPlugin() {
		return instance;
	}

	private FinaleManager manager;

	private CombatTagPlusManager ctpManager;

	private ConfigParser config;

	public CombatTagPlusManager getCombatTagPlusManager() {
		return ctpManager;
	}

	public FinaleManager getManager() {
		return manager;
	}

	@Override
	public String getPluginName() {
		return "Finale";
	}

	private void initExternalManagers() {
		if (!config.isPearlEnabled())
			return;
		// Only set up these managers if pearl cooldown change is in effect, otherwise
		// move on; better not to put hooks in that go unused.
		PluginManager plugins = Bukkit.getPluginManager();
		if (plugins.isPluginEnabled("CombatTagPlus")) {
			ctpManager = new CombatTagPlusManager();
		}
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		reload();
	}

	private void registerListener() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(manager), this);
		// So far the pearl listener, CTP manager only needed if pearl cooldown changes
		// are enabled.
		if (config.isPearlEnabled()) {
			Bukkit.getPluginManager()
					.registerEvents(new PearlCoolDownListener(config.getPearlCoolDown(), config.combatTagOnPearl(),
							ctpManager, config.setVanillaPearlCooldown(), config.useSideBarForPearlCooldown(), config.useActionBarForPearlCooldown()), this);
		}
		Bukkit.getPluginManager().registerEvents(new WeaponModificationListener(), this);
		Bukkit.getPluginManager().registerEvents(new EnchantmentDisableListener(config.getDisabledEnchants()), this);
		Bukkit.getPluginManager().registerEvents(new PotionListener(config.getPotionHandler()), this);
		Bukkit.getPluginManager().registerEvents(new VelocityFixListener(config.getVelocityHandler()), this);
		Bukkit.getPluginManager().registerEvents(new DamageListener(config.getDamageModifiers()), this);
	}

	public void reload() {
		onDisable();
		config = new ConfigParser(this);
		manager = config.parse();
		initExternalManagers();
		registerListener();
	}

}
