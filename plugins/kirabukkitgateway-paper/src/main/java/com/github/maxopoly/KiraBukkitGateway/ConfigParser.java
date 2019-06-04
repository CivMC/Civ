package com.github.maxopoly.KiraBukkitGateway;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.github.maxopoly.KiraBukkitGateway.log.KiraLogAppender;
import com.rabbitmq.client.ConnectionFactory;

public class ConfigParser {

	private KiraBukkitGatewayPlugin plugin;
	private FileConfiguration config;
	private Logger logger;

	public ConfigParser(KiraBukkitGatewayPlugin plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();
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
	
	public List<KiraLogAppender> getConsoleProcessors() {
		List <KiraLogAppender> result = new LinkedList<>();
		ConfigurationSection section = config.getConfigurationSection("console");
		if (section == null) {
			return result;
		}
		for(String key : section.getKeys(false)) {
			if (!section.isConfigurationSection(key)) {
				logger.warning("Ignoring invalid entry " + key + " at " + section.getCurrentPath());
				continue;
			}
			ConfigurationSection current = section.getConfigurationSection(key);
			if (!current.isString("regex")) {
				logger.warning("Console processor " + key  + " has no regex and was ignored");
				continue;
			}
			String regex = current.getString("regex");
			try {
				Pattern.compile(regex);
			}
			catch (PatternSyntaxException e) {
				logger.warning("Regex at " + key  + " is missformatted and was ignored");
				continue;
			}
			if (!current.isString("key")) {
				logger.warning("Console processor " + key  + " has no key and was ignored");
				continue;
			}
			String sectionKey = current.getString("key");
			result.add(new KiraLogAppender(sectionKey, regex));
		}
		return result;
	}

}
