package com.github.maxopoly.KiraBukkitGateway;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;

import com.github.maxopoly.KiraBukkitGateway.auth.AuthcodeManager;
import com.github.maxopoly.KiraBukkitGateway.impersonation.KiraLuckPermsWrapper;
import com.github.maxopoly.KiraBukkitGateway.listener.CivChatListener;
import com.github.maxopoly.KiraBukkitGateway.listener.JukeAlertListener;
import com.github.maxopoly.KiraBukkitGateway.listener.SkynetListener;
import com.github.maxopoly.KiraBukkitGateway.log.KiraLogAppender;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitCommands;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitHandler;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class KiraBukkitGatewayPlugin extends ACivMod {

	private static KiraBukkitGatewayPlugin instance;

	private RabbitHandler rabbit;
	private RabbitCommands rabbitCommands;
	private AuthcodeManager authcodeManager;
	private KiraLuckPermsWrapper permsWrapper;
	private ConfigParser config;
	private List <KiraLogAppender> logAppenders;

	public void onEnable() {
		super.onEnable();
		instance = this;
		authcodeManager = new AuthcodeManager(12);
		reload();
		setupPermissions();
		this.permsWrapper = new KiraLuckPermsWrapper();
		getServer().getPluginManager().registerEvents(new CivChatListener(), this);
		getServer().getPluginManager().registerEvents(new JukeAlertListener(), this);
		getServer().getPluginManager().registerEvents(new SkynetListener(), this);
		getLogger().info("Successfully enabled " + getName());
	}

	public void reload() {
		config = new ConfigParser(this);
		if (rabbit != null) {
			rabbit.shutdown();
		}
		rabbit = new RabbitHandler(config.getRabbitConfig(), config.getIncomingQueueName(),
				config.getOutgoingQueueName(), getLogger());
		if (!rabbit.setup()) {
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		rabbit.beginAsyncListen();
		rabbitCommands = new RabbitCommands(rabbit);
		Logger logger = (Logger) LogManager.getRootLogger();
		if (logAppenders != null) {
			for (KiraLogAppender appender : logAppenders) {
				logger.removeAppender(appender);
				appender.stop();
			}
			logAppenders.clear();
		}
		logAppenders = config.getConsoleProcessors();
		for (KiraLogAppender appender : config.getConsoleProcessors()) {
			appender.start();
			logger.addAppender(appender);
		}
	}
	
	private void setupPermissions() {
		List<PlayerType> owners = new LinkedList<>();
		owners.add(PlayerType.OWNER);
		PermissionType.registerPermission("KIRA_MANAGE_CHANNEL", owners);
	}

	@Override
	public void onDisable() {
		rabbit.shutdown();
	}

	public static KiraBukkitGatewayPlugin getInstance() {
		return instance;
	}

	public AuthcodeManager getAuthcodeManager() {
		return authcodeManager;
	}

	public RabbitCommands getRabbit() {
		return rabbitCommands;
	}
	
	public KiraLuckPermsWrapper getPermsWrapper()  {
		return permsWrapper;
	}

}
