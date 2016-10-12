package com.devotedmc.ExilePearl.core;

import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.devotedmc.ExilePearl.config.DocumentConfig;
import com.devotedmc.ExilePearl.config.Document;

class CoreConfiguration implements DocumentConfig {
	
	protected final Plugin plugin;
	
	protected Document doc;

	public CoreConfiguration(Plugin plugin) {
		this.plugin = plugin;
		
		doc = configurationSectionToDocument(plugin.getConfig());
	}

	@Override
	public Document getDocument() {
		return this.doc;
	}

	@Override
	public DocumentConfig reloadFile() {
		plugin.reloadConfig();
		doc = configurationSectionToDocument(plugin.getConfig());
		return this;
	}

	@Override
	public void saveToFile(Document doc) {
		documentToConfigurationSection(plugin.getConfig(), doc);
		
		plugin.saveConfig();
	}
	
	@Override
	public void saveToFile() {
		saveToFile(doc);
	}
	
	/**
	 * Recursively converts Bukkit configuration sections into documents
	 * @param mem The bukkit configuration section
	 * @return A document containing all the configuration data
	 */
	private Document configurationSectionToDocument(ConfigurationSection mem) {
		Document doc = new Document();
		
		for(Entry<String, Object> e : mem.getValues(false).entrySet()) {
			String k = e.getKey();
			Object o = e.getValue();
			if (o instanceof ConfigurationSection) {
				doc.append(k, configurationSectionToDocument((ConfigurationSection)o));
			}
			else {
				doc.append(k, o);
			}
		}
		
		return doc;
	}
	
	/**
	 * Recursively adds all document data to a Bukkit configuration section
	 * @param mem The bukkit configuration section
	 * @param doc The document object
	 * @return The resulting configuration section
	 */
	private ConfigurationSection documentToConfigurationSection(ConfigurationSection mem, Document doc) {
		for(Entry<String, Object> e : doc.entrySet()) {
			String k = e.getKey();
			Object o = e.getValue();
			if (o instanceof Document) {
				documentToConfigurationSection(mem.createSection(k), (Document)o);
			}
			else {
				mem.set(k, o);
			}
		}
		
		return mem;
	}
}
