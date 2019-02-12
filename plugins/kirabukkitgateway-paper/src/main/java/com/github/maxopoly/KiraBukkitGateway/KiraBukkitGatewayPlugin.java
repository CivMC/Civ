package com.github.maxopoly.KiraBukkitGateway;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;

import com.github.maxopoly.KiraBukkitGateway.auth.AuthcodeManager;
import com.github.maxopoly.KiraBukkitGateway.command.KiraCommandHandler;
import com.github.maxopoly.KiraBukkitGateway.listener.CivChatListener;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitCommands;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitHandler;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class KiraBukkitGatewayPlugin extends ACivMod {

	private static KiraBukkitGatewayPlugin instance;

	private VaultAPI vault;
	private RabbitHandler rabbit;
	private RabbitCommands rabbitCommands;
	private AuthcodeManager authcodeManager;
	private ConfigParser config;

	public void onEnable() {
		handle = new KiraCommandHandler();
		handle.registerCommands();
		super.onEnable();
		instance = this;
		authcodeManager = new AuthcodeManager(12);
		vault = new VaultAPI();
		config = new ConfigParser(this);
		rabbit = new RabbitHandler(config.getRabbitConfig(), config.getIncomingQueueName(), config.getOutgoingQueueName(), getLogger());
		if (!rabbit.setup()) {
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		List<PlayerType> owners = new LinkedList<>();
		owners.add(PlayerType.OWNER);
		PermissionType.registerPermission("KIRA_MANAGE_CHANNEL", owners);
		rabbit.beginAsyncListen();
		rabbitCommands = new RabbitCommands(rabbit);
		getServer().getPluginManager().registerEvents(new CivChatListener(), this);
		getLogger().info("Successfully enabled " + getName());
	}

	public void onDisable() {
		rabbit.shutdown();
	}

	public VaultAPI getVault() {
		return vault;
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

	@Override
	protected String getPluginName() {
		return "KiraBukkitGateway";
	}

}
