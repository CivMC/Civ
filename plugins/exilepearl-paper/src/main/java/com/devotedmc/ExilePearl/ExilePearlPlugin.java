package com.devotedmc.ExilePearl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import com.devotedmc.ExilePearl.command.PearlCommand;
import com.devotedmc.ExilePearl.holder.BlockHolder;
import com.devotedmc.ExilePearl.holder.LocationHolder;
import com.devotedmc.ExilePearl.holder.PearlHolder;
import com.devotedmc.ExilePearl.holder.PlayerHolder;
import com.devotedmc.ExilePearl.command.BaseCommand;
import com.devotedmc.ExilePearl.command.CmdAutoHelp;
import com.devotedmc.ExilePearl.command.CmdExilePearl;
import com.devotedmc.ExilePearl.listener.PlayerListener;
import com.devotedmc.ExilePearl.storage.MySqlStorage;
import com.devotedmc.ExilePearl.storage.PluginStorage;
import com.devotedmc.ExilePearl.util.TextUtil;

import vg.civcraft.mc.civmodcore.ACivMod;

/**
 * An offshoot of Civcraft's PrisonPearl plugin
 * @author GordonFreemanQ
 *
 */
public class ExilePearlPlugin extends ACivMod implements ExilePearlApi, PearlLogging, ExilePearlFactory {
	
	private final ExilePearlConfig pearlConfig = new ExilePearlConfig(this);
	private final PluginStorage storage = new MySqlStorage(this);
	private final PearlManager pearlManager = new PearlManager(this, storage);
	private final PlayerListener playerListener = new PlayerListener(this, pearlManager);
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
		
		// Register events
		this.getServer().getPluginManager().registerEvents(playerListener, this);
		
		// Add commands
		commands.add(new CmdExilePearl(this));
		
		log("=== ENABLE DONE (Took "+(System.currentTimeMillis() - timeEnableStart)+"ms) ===");
	}

	/**
	 * Spigot disable method
	 */
	@Override
	public void onDisable() {
		super.onDisable();
	}
	
	/**
	 * Gets a player instance by UUID
	 * @param uniqueId The player UUID
	 * @return The player instance
	 */
	public PearlPlayer getPearlPlayer(final UUID uid) {
		Player p = Bukkit.getPlayer(uid);
		return new PearlPlayer(p, p.getName());  // TODO Namelayer
	}
	
	/**
	 * Gets a player instance by name
	 * @param uniqueId The player name
	 * @return The player instance
	 */
	public PearlPlayer getPearlPlayer(final String name) {
		Player p = Bukkit.getPlayer(name);
		return new PearlPlayer(p, p.getName());  // TODO Namelayer
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
	public boolean isPlayerExiled(Player player) {
		return pearlManager.isExiled(player);
	}

	@Override
	public boolean isPlayerExiled(UUID uid) {
		return pearlManager.isExiled(uid);
	}


	@Override
	public ExilePearl createExilePearl(UUID uid, Location location, int strength) {
		PearlHolder holder;
		
		if (location.getBlock().getState() instanceof InventoryHolder) {
			holder = new BlockHolder(location.getBlock());
		} else {
			holder = new LocationHolder(location);
		}

		return new ExilePearl(this, storage, uid, holder, strength);
	}


	@Override
	public ExilePearl createExilePearl(UUID uid, Player player, int strength) {
		return new ExilePearl(this, storage, uid, new PlayerHolder(player), strength);
	}
}
