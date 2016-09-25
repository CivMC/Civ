package com.devotedmc.ExilePearl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.command.PearlCommand;
import com.devotedmc.ExilePearl.core.CorePearlFactory;
import com.devotedmc.ExilePearl.command.BaseCommand;
import com.devotedmc.ExilePearl.command.CmdAutoHelp;
import com.devotedmc.ExilePearl.command.CmdExilePearl;
import com.devotedmc.ExilePearl.listener.ExileListener;
import com.devotedmc.ExilePearl.listener.PlayerListener;
import com.devotedmc.ExilePearl.storage.MySqlStorage;
import com.devotedmc.ExilePearl.storage.AsyncStorageWriter;
import com.devotedmc.ExilePearl.storage.PluginStorage;
import com.devotedmc.ExilePearl.util.TextUtil;

import vg.civcraft.mc.civmodcore.ACivMod;

/**
 * An offshoot of Civcraft's PrisonPearl plugin
 * @author GordonFreemanQ
 *
 */
public class ExilePearlPlugin extends ACivMod implements ExilePearlApi, PlayerNameProvider {
	
	private final ExilePearlConfig pearlConfig = new ExilePearlConfig(this);
	private final PearlFactory pearlFactory = new CorePearlFactory(this);
	private final PluginStorage storage = new AsyncStorageWriter(new MySqlStorage(pearlFactory), this);
	private final PearlManager pearlManager = pearlFactory.createPearlManager();
	private final PearlWorker pearlWorker = pearlFactory.createPearlWorker();
	
	private final PlayerListener playerListener = new PlayerListener(this);
	private final ExileListener exileListener = new ExileListener(this, pearlConfig);
	
	private final HashSet<PearlCommand> commands = new HashSet<PearlCommand>();
	private final CmdAutoHelp autoHelp = new CmdAutoHelp(this);
	
	
	/**
	 * 
	 * ProgrammerDan - Today at 2:00 PM
	 * my "wish list" would be 
	 * 		(1) supports reload 
	 * 		(2) exposes command to self-reload 
	 * 		(3) individual configuration options can be changed at runtime and saved to config 
	 * 		(4) admins can exile players or de-exile them either in game or on console; 
	 * 			if on console exiling a player requires giving coordinates to a chest / InventoryHolder tile entity or player name 
	 * 		(5)easy admin command to see which players are exiled 
	 * 		(6) Throw bukkit events for when players are exiled or freed from exile, and when they attempt to breach their exile, 
	 * 			and when an ExilePearl is placed in an InventoryHolder tile entity or picked up / in a player inventory (for tracking).
	 * 
	 */

	/**
	 * Spigot enable method
	 */
	@Override
	public void onEnable() {
		log("=== ENABLE START ===");
		long timeEnableStart = System.currentTimeMillis();
		super.onEnable();
		
		// Storage connect and load
		if (!storage.connect()) {
			log(Level.SEVERE, "Failed to connect to database.");
			return;
		}
		pearlManager.loadPearls();
		
		// Add commands
		commands.add(new CmdExilePearl(this));
		
		// Register events
		this.getServer().getPluginManager().registerEvents(playerListener, this);
		this.getServer().getPluginManager().registerEvents(exileListener, this);
		
		// Start tasks
		pearlWorker.start();
		
		log("=== ENABLE DONE (Took "+(System.currentTimeMillis() - timeEnableStart)+"ms) ===");
	}

	/**
	 * Spigot disable method
	 */
	@Override
	public void onDisable() {
		super.onDisable();
		
		pearlWorker.stop();
		storage.disconnect();
	}
	
	/**
	 * Gets the pearl configuration
	 * @return The pearl configuration
	 */
	public ExilePearlConfig getPearlConfig() {
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
	 * Handles a bukkit command event
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		for (BaseCommand<? extends ExilePearlPlugin> c : commands) {
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
		for (BaseCommand<? extends ExilePearlPlugin> c : commands) {
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
	protected final String getPluginName() {
		return "ExilePearl";
	}
	
	public void log(Level level, String msg, Object... args) {
		getLogger().log(level, String.format(msg, args));
	}
	
	public void log(String msg, Object... args) {
		log(Level.INFO, msg, args);
	}
	
	public String formatText(String text, Object... args) {
		return TextUtil.instance().parse(text, args);
	}

	@Override
	public ExilePearl exilePlayer(Player exiled, Player killedBy) {
		return pearlManager.exilePlayer(exiled, killedBy);
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
	public boolean freePearl(ExilePearl pearl) {
		return pearlManager.freePearl(pearl);
	}
	
	@Override
	public PearlPlayer getPearlPlayer(final UUID uid) {
		Player player = Bukkit.getPlayer(uid);
		if (player == null) {
			return null;
		}
		
		return pearlFactory.createPearlPlayer(player);
	}
	
	@Override
	public PearlPlayer getPearlPlayer(final String name) {
		return getPearlPlayer(getUniqueId(name));
	}

	@Override
	public String getName(UUID uid) {
		// TODO Namelayer
		return Bukkit.getOfflinePlayer(uid).getName();
	}
	

	@SuppressWarnings("deprecation")
	@Override
	public UUID getUniqueId(String name) {
		// TODO Namelayer
		return Bukkit.getOfflinePlayer(name).getUniqueId();
	}
}
