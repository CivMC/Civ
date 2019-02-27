package com.github.maxopoly.KiraBukkitGateway;

import org.bukkit.configuration.file.FileConfiguration;

import com.rabbitmq.client.ConnectionFactory;

public class ConfigParser {

	private KiraBukkitGatewayPlugin plugin;
	private FileConfiguration config;

	public ConfigParser(KiraBukkitGatewayPlugin plugin) {
		this.plugin = plugin;
		reload();
	}

	public void reload() {
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		config = plugin.getConfig();
	}

	public ConnectionFactory getRabbitConfig() {
		ConnectionFactory connFac = new ConnectionFactory();
		String user = config.getString("rabbitmq.user", null);
		if (user != null) {
			connFac.setUsername(user);
		}
		String password = config.getString("rabbitmq.password", null);
		if (password != null) {
			connFac.setPassword(password);
		}
		String host = config.getString("rabbitmq.host", null);
		if (host != null) {
			connFac.setHost(host);
		}
		int port = config.getInt("rabbitmq.port", -1);
		if (port != -1) {
			connFac.setPort(port);
		}
		return connFac;
	}

	public String getIncomingQueueName() {
		return config.getString("rabbitmq.incomingQueue");
	}

	public String getOutgoingQueueName() {
		return config.getString("rabbitmq.outgoingQueue");
	}

}
