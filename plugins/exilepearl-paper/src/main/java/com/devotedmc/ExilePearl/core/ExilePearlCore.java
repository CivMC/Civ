package com.devotedmc.ExilePearl.core;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;

import com.avaje.ebean.EbeanServer;
import com.devotedmc.ExilePearl.BorderHandler;
import com.devotedmc.ExilePearl.DamageLogger;
import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.LoreProvider;
import com.devotedmc.ExilePearl.PearlManager;
import com.devotedmc.ExilePearl.StorageProvider;
import com.devotedmc.ExilePearl.SuicideHandler;
import com.devotedmc.ExilePearl.command.BaseCommand;
import com.devotedmc.ExilePearl.command.CmdAutoHelp;
import com.devotedmc.ExilePearl.command.CmdExilePearl;
import com.devotedmc.ExilePearl.command.CmdLegacy;
import com.devotedmc.ExilePearl.command.CmdSuicide;
import com.devotedmc.ExilePearl.command.PearlCommand;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.listener.BastionListener;
import com.devotedmc.ExilePearl.listener.CitadelListener;
import com.devotedmc.ExilePearl.listener.CivChatListener;
import com.devotedmc.ExilePearl.listener.ExileListener;
import com.devotedmc.ExilePearl.listener.JukeAlertListener;
import com.devotedmc.ExilePearl.listener.PlayerListener;
import com.devotedmc.ExilePearl.listener.RandomSpawnListener;
import com.devotedmc.ExilePearl.listener.WorldBorderListener;
import com.devotedmc.ExilePearl.storage.CoreStorageProvider;
import com.devotedmc.ExilePearl.storage.PluginStorage;
import com.devotedmc.ExilePearl.util.Clock;
import com.devotedmc.ExilePearl.util.ExilePearlRunnable;
import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.WorldBorder;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.manager.BastionBlockManager;
import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.TagManager;
import vg.civcraft.mc.civmodcore.locations.QTBox;
import vg.civcraft.mc.civmodcore.util.Guard;
import vg.civcraft.mc.namelayer.NameAPI;

/**
 * The implementation class for the ExilPearlApi.
 * <p>
 * The reason for putting this in a separate class is two-fold.
 * 1. It forces any development to reference the API instead of the implementation.
 * 2. It's much easier for testing because creating instances of JavaPlugin inside
 * 		a test case isn't trivial.
 * 
 * 
 * @author Gordon
 */
final class ExilePearlCore implements ExilePearlApi {
	
	private final Plugin plugin;
	private final CorePluginFactory pearlFactory;
	private final PearlConfig pearlConfig;
	private final CoreStorageProvider storageProvider;
	private final PearlManager pearlManager;
	private final LoreProvider loreGenerator;
	private final ExilePearlRunnable pearlDecayWorker;;
	private final BorderHandler borderHandler;
	private final SuicideHandler suicideHandler;
	private final DamageLogger damageLogger;
	
	private final PlayerListener playerListener;
	private final ExileListener exileListener;
	private final CitadelListener citadelListener;
	private final CivChatListener chatListener;
	private final BastionListener bastionListener;
	private final JukeAlertListener jukeAlertListener;
	private final RandomSpawnListener randomSpawnListener;
	private final WorldBorderListener worldBorderListener;
	
	private final HashSet<BaseCommand<?>> commands;
	private final CmdAutoHelp autoHelp;
	private CoreClock clock;
	
	private PluginStorage storage;
	private TagManager tagManager;
	
	public ExilePearlCore(final Plugin plugin) {
		Guard.ArgumentNotNull(plugin, "plugin");
		
		this.plugin = plugin;
		
		pearlFactory = new CorePluginFactory(this);
		pearlConfig = pearlFactory.createPearlConfig();
		storageProvider = new CoreStorageProvider(this, pearlFactory);
		pearlManager = pearlFactory.createPearlManager();
		loreGenerator = pearlFactory.createLoreGenerator();
		pearlDecayWorker = pearlFactory.createPearlDecayWorker();
		borderHandler = pearlFactory.createPearlBorderHandler();
		suicideHandler = pearlFactory.createSuicideHandler();
		damageLogger = pearlFactory.createDamageLogger();
		
		playerListener = new PlayerListener(this);
		exileListener = new ExileListener(this);
		citadelListener = new CitadelListener(this);
		chatListener = new CivChatListener(this);
		bastionListener = new BastionListener(this);
		jukeAlertListener = new JukeAlertListener(this);
		randomSpawnListener = new RandomSpawnListener(this);
		worldBorderListener = new WorldBorderListener(this);
		
		commands = new HashSet<BaseCommand<?>>();
		autoHelp = new CmdAutoHelp(this);
		clock = new CoreClock();
	}

	@Override
	public void onLoad() {
	}

	/**
	 * Spigot enable method
	 */
	@Override
	public void onEnable() {
		log("=== ENABLE START ===");
		long timeEnableStart = System.currentTimeMillis();
		
		pearlConfig.addConfigurable(playerListener);
		pearlConfig.addConfigurable(exileListener);
		pearlConfig.addConfigurable(pearlDecayWorker);
		pearlConfig.addConfigurable(borderHandler);
		pearlConfig.addConfigurable(suicideHandler);
		pearlConfig.addConfigurable(damageLogger);
		
		saveDefaultConfig();
		pearlConfig.reload();
		
		// Storage connect and load
		if (storageProvider.getStorage() == null) {
			storage = storageProvider.createStorage();
		}
		if (storage.connect()) {
			pearlManager.loadPearls();
		} else {
			log(Level.SEVERE, "Failed to connect to storage.");
		}
		
		// Add commands
		commands.add(new CmdExilePearl(this));
		commands.add(new CmdLegacy(this));
		commands.add(new CmdSuicide(this));
		
		// Register events
		getServer().getPluginManager().registerEvents(playerListener, this);
		getServer().getPluginManager().registerEvents(suicideHandler, this);
		getServer().getPluginManager().registerEvents(borderHandler, this);
		getServer().getPluginManager().registerEvents(exileListener, this);
		if (isCitadelEnabled()) {
			this.getServer().getPluginManager().registerEvents(citadelListener, this);
		} else {
			logIgnoringHooks("Citadel");
		}
		if (isCivChatEnabled()) {
			this.getServer().getPluginManager().registerEvents(chatListener, this);
		} else {
			logIgnoringHooks("CivChat");
		}
		if (isBastionEnabled()) {
			this.getServer().getPluginManager().registerEvents(bastionListener, this);
		} else {
			logIgnoringHooks("Bastion");
		}
		if (isJukeAlertEnabled()) {
			this.getServer().getPluginManager().registerEvents(jukeAlertListener, this);
		} else {
			logIgnoringHooks("JukeAlert");
		}
		if (isRandomSpawnEnabled()) {
			this.getServer().getPluginManager().registerEvents(randomSpawnListener, this);
		} else {
			logIgnoringHooks("RandomSpawn");
		}
		if (isWorldBorderEnabled()) {
			this.getServer().getPluginManager().registerEvents(worldBorderListener, this);
		} else {
			logIgnoringHooks("WorldBorder");
		}
		
		
		// Start tasks
		pearlDecayWorker.start();
		borderHandler.start();
		suicideHandler.start();
		if (pearlConfig.getDamageLogEnabled()) {
			damageLogger.start();
			this.getServer().getPluginManager().registerEvents(damageLogger, this);
		} else {
			logIgnoringTask(damageLogger);
		}
		
		Plugin combatPlugin = Bukkit.getPluginManager().getPlugin("CombatTagPlus");
		if(combatPlugin != null) {
			CombatTagPlus combat = (CombatTagPlus)combatPlugin;
			tagManager = combat.getTagManager();
		}
		
		log("=== ENABLE DONE (Took "+(System.currentTimeMillis() - timeEnableStart)+"ms) ===");
	}

	/**
	 * Spigot disable method
	 */
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		pearlDecayWorker.stop();
		borderHandler.stop();
		suicideHandler.stop();
		storage.disconnect();
	}
	
	private void logIgnoringHooks(String pluginName) {
		log(Level.WARNING, "Ignoring hooks for '%s' since it's not enabled.", pluginName);
	}
	
	private void logIgnoringTask(ExilePearlRunnable task) {
		log(Level.WARNING, "Ignoring the task '%s' since it's not enabled.", task.getTaskName());
	}
	
	/**
	 * Gets the pearl configuration
	 * @return The pearl configuration
	 */
	@Override
	public PearlConfig getPearlConfig() {
		return pearlConfig;
	}
	
	/**
	 * Gets the plugin storage provider
	 * @return The storage instance provider
	 */
	@Override
	public StorageProvider getStorageProvider() {
		return storageProvider;
	}
	
	/**
	 * Gets the pearl manager
	 * @return The pearl manager instance
	 */
	@Override
	public PearlManager getPearlManager() {
		return pearlManager;
	}

	@Override
	public DamageLogger getDamageLogger() {
		return damageLogger;
	}
	
	/**
	 * Gets the auto-help command
	 * @return The auto-help command
	 */
	@Override
	public PearlCommand getAutoHelp() {
		return autoHelp;
	}
	
	/**
	 * Gets the suicide handler
	 * @return The suicide handler
	 */
	@Override
	public SuicideHandler getSuicideHandler() {
		return suicideHandler;
	}
	
	/**
	 * Handles a bukkit command event
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		for (BaseCommand<?> c : commands) {
			List<String> aliases = c.getAliases();
			if (aliases.contains(cmd.getLabel())) {

				// Set the label to the default alias
				cmd.setLabel(aliases.get(0));
				
				c.execute(sender, new ArrayList<String>(Arrays.asList(args)));
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * Handles a tab complete event
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		for (BaseCommand<?> c : commands) {
			List<String> aliases = c.getAliases();
			if (aliases.contains(cmd.getLabel())) {

				// Set the label to the default alias
				cmd.setLabel(aliases.get(0));
				
				return c.getTabList(sender, new ArrayList<String>(Arrays.asList(args)));
			}
		}
		return null;
	}

	@Override
	public void log(Level level, String msg, Object... args) {
		logInternal(level, String.format(msg, args));
	}

	@Override
	public void log(String msg, Object... args) {
		logInternal(Level.INFO, String.format(msg, args));
	}

	@Override
	public Logger getPluginLogger() {
		return plugin.getLogger();
	}
	
	private void logInternal(Level level, String msg) {
		getLogger().log(level, msg);
	}

	@Override
	public ExilePearl exilePlayer(UUID exiledId, UUID killerId, PearlHolder holder) {
		return pearlManager.exilePlayer(exiledId, killerId, holder);
	}

	@Override
	public ExilePearl exilePlayer(UUID exiledId, UUID killerId, Location location) {
		return pearlManager.exilePlayer(exiledId, killerId, location);
	}
	
	@Override
	public ExilePearl exilePlayer(UUID exiledId, Player killer) {
		return pearlManager.exilePlayer(exiledId, killer);
	}

	@Override
	public ExilePearl getPearl(String name) {
		return pearlManager.getPearl(name);
	}

	@Override
	public ExilePearl getPearl(UUID uid) {
		return pearlManager.getPearl(uid);
	}

	@Override
	public Collection<ExilePearl> getPearls() {
		return pearlManager.getPearls();
	}

	@Override
	public boolean isPlayerExiled(Player player) {
		return pearlManager.isPlayerExiled(player);
	}

	@Override
	public boolean isPlayerExiled(UUID uid) {
		return pearlManager.isPlayerExiled(uid);
	}

	@Override
	public ExilePearl getPearlFromItemStack(ItemStack is) {
		return pearlManager.getPearlFromItemStack(is);
	}

	@Override
	public boolean freePearl(ExilePearl pearl, PearlFreeReason reason) {
		return pearlManager.freePearl(pearl, reason);
	}

	@Override
	public Player getPlayer(UUID uid) {
		return Bukkit.getPlayer(uid);
	}

	@Override
	public Player getPlayer(String name) {
		return Bukkit.getPlayer(name);
	}

	@Override
	public String getRealPlayerName(UUID uid) {
		if (isNameLayerEnabled()) {
			return NameAPI.getCurrentName(uid);
		}
		OfflinePlayer player = Bukkit.getOfflinePlayer(uid);
		if (player == null) {
			return null;
		}
		return player.getName();
	}
	

	@SuppressWarnings("deprecation")
	@Override
	public UUID getUniqueId(String name) {
		if (isNameLayerEnabled()) {
			return NameAPI.getUUID(name);
		}
		OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
		if (offline != null)  {
			return offline.getUniqueId();
		}
		return null;
	}

	@Override
	public boolean isNameLayerEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("NameLayer");
	}

	@Override
	public boolean isCitadelEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("Citadel");
	}

	@Override
	public boolean isCivChatEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("CivChat2");
	}

	@Override
	public boolean isBastionEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("Bastion");
	}

	@Override
	public boolean isJukeAlertEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("JukeAlert");
	}

	@Override
	public boolean isRandomSpawnEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("RandomSpawn");
	}

	@Override
	public boolean isWorldBorderEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("WorldBorder");
	}

	@Override
	public boolean isCombatTagEnabled() {
		return tagManager != null;
	}

	@Override
	public LoreProvider getLoreProvider() {
		return loreGenerator;
	}

	@Override
	public boolean isPlayerTagged(UUID uid) {
		if (isCombatTagEnabled()) {
			return tagManager.isTagged(uid);
		}
		return false;
	}

	@Override
	public FileConfiguration getConfig() {
		return plugin.getConfig();
	}

	@Override
	public File getDataFolder() {
		return plugin.getDataFolder();
	}

	@Override
	public EbeanServer getDatabase() {
		return plugin.getDatabase();
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String arg0, String arg1) {
		return plugin.getDefaultWorldGenerator(arg0, arg1);
	}

	@Override
	public PluginDescriptionFile getDescription() {
		return plugin.getDescription();
	}

	@Override
	public Logger getLogger() {
		return plugin.getLogger();
	}

	@Override
	public String getName() {
		return plugin.getName();
	}

	@Override
	public PluginLoader getPluginLoader() {
		return plugin.getPluginLoader();
	}

	@Override
	public InputStream getResource(String arg0) {
		return plugin.getResource(arg0);
	}

	@Override
	public Server getServer() {
		return plugin.getServer();
	}

	@Override
	public boolean isEnabled() {
		return plugin.isEnabled();
	}

	@Override
	public boolean isNaggable() {
		return plugin.isNaggable();
	}

	@Override
	public void reloadConfig() {
		plugin.reloadConfig();
	}

	@Override
	public void saveConfig() {
		plugin.saveConfig();
	}

	@Override
	public void saveDefaultConfig() {
		plugin.saveDefaultConfig();
	}

	@Override
	public void saveResource(String arg0, boolean arg1) {
		plugin.saveResource(arg0, arg1);
	}

	@Override
	public void setNaggable(boolean arg0) {
		plugin.setNaggable(arg0);
	}

	@Override
	public boolean isLocationInsideBorder(Location location) {
		if (isWorldBorderEnabled()) {
			WorldBorder wb = WorldBorder.plugin;
			BorderData bd = wb.getWorldBorder(location.getWorld().getName());
			return bd.insideBorder(location);
		}
		return true;
	}

	@Override
	public boolean isPlayerInUnpermittedBastion(Player player) {
		if (!isBastionEnabled()) {
			return false;
		}
		try {
			final BastionBlockManager manager = Bastion.getBastionManager();
			
			Set<? extends QTBox> possible  = manager.getBlockingBastions(player.getLocation());
			@SuppressWarnings("unchecked")
			List<BastionBlock> bastions = new LinkedList<BastionBlock>((Set<BastionBlock>)possible);
			for (BastionBlock bastion : bastions) {
				if (!bastion.canPlace(player)) {
					return true;
				}
			}
		} catch(Exception ex) { }
		return false;
	}

	@Override
	public Clock getClock() {
		return clock;
	}
}
