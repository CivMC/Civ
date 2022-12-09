package com.github.maxopoly.finale;

import com.comphenix.protocol.ProtocolLibrary;
import com.github.maxopoly.finale.command.AllyCommand;
import com.github.maxopoly.finale.command.CardinalCommand;
import com.github.maxopoly.finale.command.CombatConfigCommand;
import com.github.maxopoly.finale.command.FinaleCommand;
import com.github.maxopoly.finale.command.GammaBrightCommand;
import com.github.maxopoly.finale.command.ReloadFinaleCommand;
import com.github.maxopoly.finale.command.ShowCpsCommand;
import com.github.maxopoly.finale.external.CombatTagPlusManager;
import com.github.maxopoly.finale.external.FinaleSettingManager;
import com.github.maxopoly.finale.listeners.BlockListener;
import com.github.maxopoly.finale.listeners.CrossbowListener;
import com.github.maxopoly.finale.listeners.DamageListener;
import com.github.maxopoly.finale.listeners.EnchantmentDisableListener;
import com.github.maxopoly.finale.listeners.ExtraDurabilityListener;
import com.github.maxopoly.finale.listeners.GappleListener;
import com.github.maxopoly.finale.listeners.PearlCoolDownListener;
import com.github.maxopoly.finale.listeners.PlayerListener;
import com.github.maxopoly.finale.listeners.PotionListener;
import com.github.maxopoly.finale.listeners.ShieldListener;
import com.github.maxopoly.finale.listeners.ToolProtectionListener;
import com.github.maxopoly.finale.listeners.TridentListener;
import com.github.maxopoly.finale.listeners.VelocityFixListener;
import com.github.maxopoly.finale.listeners.WarpFruitListener;
import com.github.maxopoly.finale.listeners.WeaponModificationListener;
import com.github.maxopoly.finale.overlay.ScoreboardHUD;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.commands.CommandManager;

public class Finale extends ACivMod {

	private static Finale instance;

	public static Finale getPlugin() {
		return instance;
	}
	
	private FinaleManager manager;
	private CombatTagPlusManager ctpManager;
	private ConfigParser config;
	private FinaleSettingManager settingsManager;
	private CommandManager commandManager;

	public CombatTagPlusManager getCombatTagPlusManager() {
		return ctpManager;
	}

	public FinaleManager getManager() {
		return manager;
	}
	
	public FinaleSettingManager getSettingsManager() {
		return settingsManager;
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
		if (manager != null) {
			manager.getAllyHandler().save();
			manager.getAllyHandler().shutdown();
		}

		HandlerList.unregisterAll(this);
		ProtocolLibrary.getProtocolManager().removePacketListeners(this);
		ProtocolLibrary.getProtocolManager().getAsynchronousManager().unregisterAsyncHandlers(this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		instance = this;
		commandManager = new CommandManager(this);
		commandManager.init();
		reload();
	}

	private void registerListener() {
		Bukkit.getPluginManager().registerEvents(new PlayerListener(manager), this);
		// So far the pearl listener, CTP manager only needed if pearl cooldown changes
		// are enabled.
		if (config.isPearlEnabled()) {
			Bukkit.getPluginManager()
					.registerEvents(new PearlCoolDownListener(config.getPearlCoolDown(), config.combatTagOnPearl(),
							ctpManager), this);
		}
		Bukkit.getPluginManager().registerEvents(new WeaponModificationListener(), this);
		Bukkit.getPluginManager().registerEvents(new ExtraDurabilityListener(), this);
		Bukkit.getPluginManager().registerEvents(new EnchantmentDisableListener(config.getDisabledEnchants()), this);
		Bukkit.getPluginManager().registerEvents(new PotionListener(config.getPotionHandler()), this);
		Bukkit.getPluginManager().registerEvents(new VelocityFixListener(config.getVelocityHandler()), this);
		Bukkit.getPluginManager().registerEvents(new DamageListener(config.getDamageModifiers()), this);
		Bukkit.getPluginManager().registerEvents(new ScoreboardHUD(settingsManager), this);
		Bukkit.getPluginManager().registerEvents(new ToolProtectionListener(settingsManager), this);
		Bukkit.getPluginManager().registerEvents(new WarpFruitListener(), this);
		Bukkit.getPluginManager().registerEvents(new TridentListener(), this);
		Bukkit.getPluginManager().registerEvents(new ShieldListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
		Bukkit.getPluginManager().registerEvents(new CrossbowListener(), this);
		Bukkit.getPluginManager().registerEvents(new GappleListener(), this);
	}

	private void registerCommands() {
		commandManager.registerCommand(new CardinalCommand());
		commandManager.registerCommand(new CombatConfigCommand());
		commandManager.registerCommand(new GammaBrightCommand());
		commandManager.registerCommand(new ReloadFinaleCommand());
		commandManager.registerCommand(new ShowCpsCommand());
		commandManager.registerCommand(new FinaleCommand());
		commandManager.registerCommand(new AllyCommand());
	}

	public void reload() {
		onDisable();
		config = new ConfigParser(this);
		manager = config.parse();
		settingsManager = new FinaleSettingManager();
		initExternalManagers();
		registerListener();
		registerCommands();
	}

}
