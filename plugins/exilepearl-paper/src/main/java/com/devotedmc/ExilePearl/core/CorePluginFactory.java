package com.devotedmc.ExilePearl.core;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.devotedmc.ExilePearl.BorderHandler;
import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLoreProvider;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.PearlPlayer;
import com.devotedmc.ExilePearl.SuicideHandler;
import com.devotedmc.ExilePearl.config.Document;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.util.ExilePearlRunnable;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * Factory class for creating new core class instances
 * @author Gordon
 */
public final class CorePluginFactory implements PearlFactory {
	
	private final ExilePearlApi pearlApi;
	
	public static ExilePearlApi createCore(final Plugin plugin) {
		return new ExilePearlCore(plugin);
	}
	
	/**
	 * Creates a new ExilePearlFactory instance
	 * @param plugin The plugin instance
	 */
	public CorePluginFactory(final ExilePearlApi plugin) {
		Guard.ArgumentNotNull(plugin, "plugin");
		
		this.pearlApi = plugin;
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, Document doc) {
		Guard.ArgumentNotNull(uid, "uid");
		Guard.ArgumentNotNull(doc, "doc");
		
		try {
			UUID killedBy = UUID.fromString(doc.getString("killer_id"));
			int pearlId = doc.getInteger("pearl_id");
			Location loc = doc.getLocation("location");
			int health = doc.getInteger("health");
			Date pearledOn = doc.getDate("pearled_on");
			boolean freedOffline = doc.getBoolean("freed_offline");
			
			ExilePearl pearl = new CoreExilePearl(pearlApi, pearlApi.getStorageProvider().getStorage(), uid, killedBy, pearlId, new BlockHolder(loc.getBlock()));
			pearl.setHealth(health);
			pearl.setPearledOn(pearledOn);
			pearl.setFreedOffline(freedOffline);
			pearl.enableStorage();
			return pearl;
			
		} catch(Exception ex) {
			pearlApi.log(Level.SEVERE, "Failed to create pearl for ID=%s, ", uid.toString(), doc);
			return null;
		}
	}

	@Override
	public ExilePearl createExilePearl(UUID uid, Player killedBy, int pearlId) {
		Guard.ArgumentNotNull(uid, "uid");
		Guard.ArgumentNotNull(killedBy, "killedBy");
		
		ExilePearl pearl = new CoreExilePearl(pearlApi, pearlApi.getStorageProvider().getStorage(), uid, killedBy.getUniqueId(), pearlId, new PlayerHolder(killedBy));
		pearl.enableStorage();
		return pearl;
	}

	@Override
	public ExilePearl createdMigratedPearl(UUID uid, Document doc) {
		Guard.ArgumentNotNull(uid, "uid");
		Guard.ArgumentNotNull(doc, "doc");
		
		doc.append("health", pearlApi.getPearlConfig().getPearlHealthMaxValue() / 2); // set health to half max health
		return createExilePearl(uid, doc);
	}

	public PearlManager createPearlManager() {
		return new CorePearlManager(pearlApi, this, pearlApi.getStorageProvider());
	}

	public ExilePearlRunnable createPearlDecayWorker() {
		return new PearlDecayTask(pearlApi);
	}

	public SuicideHandler createSuicideHandler() {
		return new PlayerSuicideTask(pearlApi);
	}

	public BorderHandler createPearlBorderHandler() {
		return new PearlBoundaryTask(pearlApi);
	}

	public PearlPlayer createPearlPlayer(UUID uid) {
		return new CorePearlPlayer(uid, pearlApi, pearlApi);
	}

	public PearlLoreProvider createLoreGenerator() {
		return new CoreLoreGenerator(pearlApi.getPearlConfig());
	}

	public PearlConfig createPearlConfig() {
		return new CorePearlConfig(pearlApi);
	}
}
