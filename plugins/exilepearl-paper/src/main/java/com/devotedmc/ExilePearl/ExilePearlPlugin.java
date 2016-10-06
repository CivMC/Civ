package com.devotedmc.ExilePearl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.devotedmc.ExilePearl.command.PearlCommand;
import com.devotedmc.ExilePearl.core.CorePluginFactory;
import com.devotedmc.ExilePearl.command.BaseCommand;
import com.devotedmc.ExilePearl.command.CmdAutoHelp;
import com.devotedmc.ExilePearl.command.CmdExilePearl;
import com.devotedmc.ExilePearl.command.CmdLegacy;
import com.devotedmc.ExilePearl.command.CmdSuicide;
import com.devotedmc.ExilePearl.listener.BastionListener;
import com.devotedmc.ExilePearl.listener.CitadelListener;
import com.devotedmc.ExilePearl.listener.CivChatListener;
import com.devotedmc.ExilePearl.listener.ExileListener;
import com.devotedmc.ExilePearl.listener.JukeAlertListener;
import com.devotedmc.ExilePearl.listener.PlayerListener;
import com.devotedmc.ExilePearl.storage.PluginStorage;
import com.devotedmc.ExilePearl.storage.StorageProvider;
import com.devotedmc.ExilePearl.util.ExilePearlRunnable;
import com.devotedmc.ExilePearl.util.TextUtil;

import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.namelayer.NameAPI;

/**
 * An offshoot of Civcraft's PrisonPearl plugin
 * @author GordonFreemanQ
 *
 */
public class ExilePearlPlugin extends ACivMod implements ExilePearlApi {
	
	private final CorePluginFactory pearlFactory = new CorePluginFactory(this);
	private final PearlConfig pearlConfig = pearlFactory.createPearlConfig();
	private final StorageProvider storageProvider = new StorageProvider(this, pearlFactory);
	private final PluginStorage storage = storageProvider.createStorage();
	private final PearlManager pearlManager = pearlFactory.createPearlManager();
	private final PearlLoreProvider loreGenerator = pearlFactory.createLoreGenerator();
	private final ExilePearlRunnable pearlDecayWorker = pearlFactory.createPearlDecayWorker();
	private final ExilePearlRunnable pearlBordertask = pearlFactory.createPearlBorderTask();
	private final SuicideHandler suicideHandler = pearlFactory.createSuicideHandler();
	
	private final PlayerListener playerListener = new PlayerListener(this);
	private final ExileListener exileListener = new ExileListener(this);
	private final CitadelListener citadelListener = new CitadelListener(this);
	private final CivChatListener chatListener = new CivChatListener(this);
	private final BastionListener bastionListener = new BastionListener(this);
	private final JukeAlertListener jukeAlertListener = new JukeAlertListener(this);
	
	private final HashSet<BaseCommand<?>> commands = new HashSet<BaseCommand<?>>();
	private final CmdAutoHelp autoHelp = new CmdAutoHelp(this);
	
	private final HashMap<UUID, PearlPlayer> players = new HashMap<UUID, PearlPlayer>();

	/**
	 * Spigot enable method
	 */
	@Override
	public void onEnable() {
		log("=== ENABLE START ===");
		long timeEnableStart = System.currentTimeMillis();
		super.onEnable();
		
		// Storage connect and load
		if (storage.connect()) {
			pearlManager.loadPearls();
		} else {
			log(Level.SEVERE, "Failed to connect to database.");
		}
		
		// Add commands
		commands.add(new CmdExilePearl(this));
		commands.add(new CmdLegacy(this));
		commands.add(new CmdSuicide(this));
		
		playerListener.setupRecipes();
		
		// Register events
		this.getServer().getPluginManager().registerEvents(suicideHandler, this);
		this.getServer().getPluginManager().registerEvents(playerListener, this);
		this.getServer().getPluginManager().registerEvents(exileListener, this);
		if (isCitadelEnabled()) {
			this.getServer().getPluginManager().registerEvents(citadelListener, this);
		} else {
			log(Level.WARNING, "Ignoring hooks for Citadel since it's not enabled.");
		}
		if (isCivChatEnabled()) {
			this.getServer().getPluginManager().registerEvents(chatListener, this);
		} else {
			log(Level.WARNING, "Ignoring hooks for CivChat since it's not enabled.");
		}
		if (isBastionEnabled()) {
			this.getServer().getPluginManager().registerEvents(bastionListener, this);
		} else {
			log(Level.WARNING, "Ignoring hooks for Bastion since it's not enabled.");
		}
		if (isJukeAlertEnabled()) {
			this.getServer().getPluginManager().registerEvents(jukeAlertListener, this);
		} else {
			log(Level.WARNING, "Ignoring hooks for JukeAlert since it's not enabled.");
		}
		
		// Start tasks
		pearlDecayWorker.start();
		pearlBordertask.start();
		suicideHandler.start();
		
		log("=== ENABLE DONE (Took "+(System.currentTimeMillis() - timeEnableStart)+"ms) ===");
	}

	/**
	 * Spigot disable method
	 */
	@Override
	public void onDisable() {
		super.onDisable();
		
		pearlDecayWorker.stop();
		pearlBordertask.stop();
		suicideHandler.stop();
		storage.disconnect();
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
	 * Gets the plugin storage
	 * @return The storage instance
	 */
	public PluginStorage getStorage() {
		return storage;
	}
	
	/**
	 * Gets the pearl manager
	 * @return The pearl manager instance
	 */
	public PearlManager getPearlManager() {
		return pearlManager;
	}
	
	/**
	 * Gets the auto-help command
	 * @return The auto-help command
	 */
	public PearlCommand getAutoHelp() {
		return autoHelp;
	}
	
	/**
	 * Gets the suicide handler
	 * @return The suicide handler
	 */
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

	/**
	 * Gets the plugin name
	 */
	@Override
	public final String getPluginName() {
		return "ExilePearl";
	}
	
	public void log(Level level, String msg, Object... args) {
		logInternal(level, String.format(msg, args));
	}
	
	public void log(String msg, Object... args) {
		logInternal(Level.INFO, String.format(msg, args));
	}
	
	private void logInternal(Level level, String msg) {
		getLogger().log(level, msg);
	}
	
	public String formatText(String text, Object... args) {
		return TextUtil.instance().parse(text, args);
	}

	@Override
	public ExilePearl exilePlayer(UUID exiledId, UUID killer) {
		return pearlManager.exilePlayer(exiledId, killer);
	}
	
	@Override
	public ExilePearl exilePlayer(Player exiled, Player killer) {
		return pearlManager.exilePlayer(exiled, killer);
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
	public PearlPlayer getPearlPlayer(final UUID uid) {
		PearlPlayer p = players.get(uid);
		if (p == null) {
			p = pearlFactory.createPearlPlayer(uid);
			players.put(uid, p);
		}
		
		return p;
	}
	
	@Override
	public PearlPlayer getPearlPlayer(final String name) {
		return getPearlPlayer(getUniqueId(name));
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
		return Bukkit.getOfflinePlayer(uid).getName();
	}
	

	@SuppressWarnings("deprecation")
	@Override
	public UUID getUniqueId(String name) {
		if (isNameLayerEnabled()) {
			return NameAPI.getUUID(name);
		}
		return Bukkit.getOfflinePlayer(name).getUniqueId();
	}
	
	private boolean isNameLayerEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("NameLayer");
	}
	
	private boolean isCitadelEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("Citadel");
	}
	
	private boolean isCivChatEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("CivChat2");
	}
	
	private boolean isBastionEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("Bastion");
	}
	
	private boolean isJukeAlertEnabled() {
		return Bukkit.getPluginManager().isPluginEnabled("JukeAlert");
	}

	@Override
	public PearlLoreProvider getLoreProvider() {
		return loreGenerator;
	}

	@Override
	public void decayPearls() {
		pearlManager.decayPearls();
	}

	@Override
	public Plugin getPlugin() {
		return this;
	}
}
