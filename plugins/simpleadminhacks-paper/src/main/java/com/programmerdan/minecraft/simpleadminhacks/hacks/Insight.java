package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.NotePlayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitTask;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.InsightConfig;

public class Insight extends SimpleHack<InsightConfig> implements CommandExecutor, Listener {

	public static final String NAME = "Insight";
	
	private Map<String, InsightStat> tracking;
	
	public Insight(SimpleAdminHacks plugin, InsightConfig config) {
		super(plugin, config);
	}

	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if (args.length < 1) { // show help
			return false;
		}

		return true;
	}
	

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering Insight listeners");
			plugin().registerListener(this);
		}
	}

	private BukkitTask remap = null;
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void postPluginLoad(PluginEnableEvent e) {
		if (!config.isEnabled()) {
			return;
		}
		if (remap != null) {
			remap.cancel();
		}
		
		remap = Bukkit.getScheduler().runTaskLater(plugin(), new Runnable(){
			@Override
			public void run() {
				reboundHandlers(BlockBreakEvent.class);
				reboundHandlers(BlockBurnEvent.class);
				reboundHandlers(BlockCanBuildEvent.class);
				reboundHandlers(BlockDamageEvent.class);
				reboundHandlers(BlockExpEvent.class);
				reboundHandlers(BlockExplodeEvent.class);
				reboundHandlers(BlockFadeEvent.class);
				reboundHandlers(BlockFormEvent.class);
				reboundHandlers(BlockFromToEvent.class);
				reboundHandlers(BlockGrowEvent.class);
				reboundHandlers(BlockIgniteEvent.class);
				reboundHandlers(BlockMultiPlaceEvent.class);
				reboundHandlers(BlockPhysicsEvent.class);
				reboundHandlers(BlockPistonExtendEvent.class);
				reboundHandlers(BlockPistonRetractEvent.class);
				reboundHandlers(BlockPlaceEvent.class);
				reboundHandlers(BlockRedstoneEvent.class);
				reboundHandlers(BlockSpreadEvent.class);
				reboundHandlers(CauldronLevelChangeEvent.class);
				reboundHandlers(EntityBlockFormEvent.class);
				reboundHandlers(LeavesDecayEvent.class);
				reboundHandlers(NotePlayEvent.class);
				reboundHandlers(SignChangeEvent.class);
			}
		}, 60l); // 3 seconds
	}
	
	/**
	 * TODO for tracked events consider hijacking priority ordering insertions... not sure how
	 * but it'd be hella cool if these handlers could be injected to bound the listeners.
	 *   one thought would be to grab the registeredlistener array, remove all listeners
	 *   then readd, forcing _this_ plugin for _that_ even to be first and last in the list.
	 * @param handler
	 */
	private <T extends Event> void reboundHandlers(Class<T> clazz) {
		try {
			plugin().log(Level.INFO, "Rebound handlers for {0}", clazz.getName());
			Method method = clazz.getDeclaredMethod("getHandlerList");
			HandlerList handler = (HandlerList) method.invoke(null);
			RegisteredListener[] listeners = handler.getRegisteredListeners();
			ArrayList<RegisteredListener> newOrdering = new ArrayList<RegisteredListener>(listeners.length);
			ArrayList<RegisteredListener> thisLow = new ArrayList<RegisteredListener>();
			ArrayList<RegisteredListener> thisHigh = new ArrayList<RegisteredListener>();
			for (RegisteredListener l : listeners) {
				handler.unregister(l);
				if (l.getPriority().equals(EventPriority.LOWEST)) {
					if (l.getListener().equals(this)) {
						thisLow.add(l);
						continue;
					} 
				} else if (l.getPriority().equals(EventPriority.MONITOR)) {
					if (l.getListener().equals(this)) {
						thisHigh.add(l);
						continue;
					}
				}
				newOrdering.add(l);
			}
			if (thisLow.isEmpty()) {
				plugin().log("Failed to find low-order handler!");
			}
			thisLow.addAll(newOrdering);
			if (thisHigh.isEmpty()) {
				plugin().log("Failed to find high-order handler!");
			} else {
				thisLow.addAll(thisHigh);
			}
			try {
				handler.registerAll(thisLow);
			} catch (Exception e) {
				plugin().log(Level.SEVERE, "reboundHandler failure to reassert listeners for " + clazz.getName(), e);
				handler.registerAll(Arrays.asList(listeners));
			}
		} catch (NoSuchMethodException nsme) {
			plugin().log(Level.WARNING, "This event does not have independent handlers, skipping " + clazz.getName());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException m) {
			plugin().log(Level.SEVERE, "Unable to retrieve handlers for " + clazz.getName(), m);
		}
	}

	@Override
	public void registerCommands() {
		if (config.isEnabled()) {
			plugin().log("Registering Insight's insight command");
			plugin().registerCommand("insight", this);
		}
	}

	@Override
	public void dataBootstrap() {
		if (config.isEnabled()) {
			this.tracking = new ConcurrentHashMap<String, InsightStat>();
		}
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		if (config.isEnabled() || this.tracking != null) {
			this.tracking.clear();
			this.tracking = null;
		}
	}

	@Override
	public String status() {
		if (config.isEnabled()) {
			StringBuffer sb = new StringBuffer( "Passively providing insight into events:\n");
			
			if (this.tracking != null) {
				sb.append(ChatColor.WHITE).append(String.format("  %10s %10s %10s %7s %7s %7s %7s Event\n", "Count", "Canceled", "Handlers", "Min", "Avg", "Max", "StDev"));
				this.tracking.forEach((k, v) -> {
					sb.append(String.format("  %s%10d %10d %10d %s%7d %s%7.0f %s%7d %s%7.2f ", 
							ChatColor.WHITE, v.getSamples(), v.getCancels(), v.getHandlers(),
							color(v.getMin()), v.getMin(),
							color(v.getAverage()), v.getAverage(),
							color(v.getMax()), v.getMax(),
							ChatColor.WHITE, v.getStdev()));
					sb.append(ChatColor.AQUA).append(k).append("\n");
				}); 
			}
			
			return sb.toString();
		} else {
			return "Insights fully disabled.";
		}
	}
	
	/**
	 * 10% of tick? Green.
	 * 15% of tick? Dark Green.
	 * 20% of tick? Yellow
	 * 30% of tick? Orange
	 * 50% of tick? Dark Red
	 * Above that? Red
	 * 
	 * @param microseconds
	 * @return
	 */
	private ChatColor color(long microseconds) {
		if (microseconds <= 5000) {
			return ChatColor.GREEN;
		} else if (microseconds <= 7500) {
			return ChatColor.DARK_GREEN;
		} else if (microseconds <= 10000) {
			return ChatColor.YELLOW;
		} else if (microseconds <= 15000) {
			return ChatColor.GOLD;
		} else if (microseconds <= 25000) {
			return ChatColor.DARK_RED;
		} else {
			return ChatColor.RED;
		}
	}
	
	private ChatColor color(double microseconds) {
		return color((long) microseconds);
	}

	// -- LISTENERS --
	
	// --- BLOCKS ---
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockBreakEvent(BlockBreakEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockBreakEvent(BlockBreakEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockBurnEvent(BlockBurnEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockBurnEvent(BlockBurnEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockCanBuildEvent(BlockCanBuildEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockCanBuildEvent(BlockCanBuildEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockDamageEvent(BlockDamageEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockDamageEvent(BlockDamageEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockDispenseEvent(BlockDispenseEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockDispenseEvent(BlockDispenseEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockExpEvent(BlockExpEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockExpEvent(BlockExpEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockExplodeEvent(BlockExplodeEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockExplodeEvent(BlockExplodeEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockFadeEvent(BlockFadeEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockFadeEvent(BlockFadeEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockFormEvent(BlockFormEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockFormEvent(BlockFormEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockFromToEvent(BlockFromToEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockFromToEvent(BlockFromToEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockGrowEvent(BlockGrowEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockGrowEvent(BlockGrowEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockIgniteEvent(BlockIgniteEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockIgniteEvent(BlockIgniteEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockMultiPlaceEvent(BlockMultiPlaceEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockMultiPlaceEvent(BlockMultiPlaceEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockPhysicsEvent(BlockPhysicsEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockPhysicsEvent(BlockPhysicsEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockPistonExtendEvent(BlockPistonExtendEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockPistonExtendEvent(BlockPistonExtendEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockPistonRetractEvent(BlockPistonRetractEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockPistonRetractEent(BlockPistonRetractEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockPlaceEvent(BlockPlaceEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockPlaceEvent(BlockPlaceEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockRedstoneEvent(BlockRedstoneEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockRedstoneEvent(BlockRedstoneEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowBlockSpreadEvent(BlockSpreadEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highBlockSpreadEvent(BlockSpreadEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowCauldronLevelChangeEvent(CauldronLevelChangeEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highCauldronLevelChangeEvent(CauldronLevelChangeEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowEntityBlockFormEvent(EntityBlockFormEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highEntityBlockFormEvent(EntityBlockFormEvent event) {
		end(event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowLeavesDecayEvent(LeavesDecayEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highLeavesDecayEvent(LeavesDecayEvent event) {
		end(event);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowNotePlayEvent(NotePlayEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highNotePlayEvent(NotePlayEvent event) {
		end(event);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void lowSignChangeEvent(SignChangeEvent event) {
		start(event);
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void highSignChangeEvent(SignChangeEvent event) {
		end(event);
	}

	
	// -- SUPPORT
	
	public void start(Event event) {
		if (config.isEnabled()) {
			InsightStat insight = this.tracking.compute(event.getEventName(), (key, stat) -> 
				(stat == null) ? new InsightStat() : stat
			);
			insight.begin(event.getHandlers().getRegisteredListeners().length);
		}
	}
	
	public void end(Event event) {
		if (config.isEnabled()) {
			InsightStat insight = this.tracking.get(event.getEventName());
			if (insight != null) {
				if (event instanceof Cancellable) {
					insight.end(((Cancellable) event).isCancelled());
				} else {
					insight.end(false);
				}
			}
		}
	}
	
	public static InsightConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new InsightConfig(plugin, config);
	}
	
	private static class InsightStat {
		// count of events
		private long count;
		// count of cancels
		private long cancels;
		// avg length of event (based on LOWEST vs. MONITOR)
		private double avg;
		// max length of event (based on LOWEST vs. MONITOR)
		private long max;
		// min length of event
		private long min;
		// variance of lengths
		private double varsum;
		private double var;
		// rolling stdev of lengths
		private double std;
		// last execution handler list size
		private long handlers;
		
		private long lastStart;
		
		public InsightStat() {
			count = 0l;
			cancels = 0l;
			avg = 0l;
			max = 0l;
			min = 0l;
			var = 0l;
			std = 0l;
			handlers = 0l;
			lastStart = 0l;
		}
		
		/**
		 * Starts a specific capture for this; assumes runs in LOWEST for same-event.
		 * Will safely skip events that had no end() called.
		 * 
		 * @param handlers
		 */
		public synchronized void begin(long handlers) {
			this.lastStart = System.nanoTime() / 1000l;
			this.handlers = handlers;
		}
		
		/**
		 * Ends a specific capture for this; assumes runs in MONITOR for same-event.
		 */
		public synchronized void end(boolean cancelled) {
			long len = (System.nanoTime() / 1000l) - this.lastStart;
			if (cancelled) {
				cancels ++;
			}
			
			if (count == 0) {
				avg = len;
				max = len;
				min = len;
				varsum = 0d;
				var = 0d;
				std = 0d;
			} else {
				double oavg = avg;
				avg = avg + ( len - avg )/ (double) (count + 1);
				// estimate...
				varsum += Math.pow(len - avg, 2);
				var = varsum / count;  // (pre-inc, see Bessel's correction)
				std = Math.sqrt(var);
				if (len > max) {
					max = len;
				}
				if (len < min) {
					min = len;
				}
			}
			count++; // n+1
		}
		
		public long getHandlers() {
			return handlers;
		}
		
		public double getAverage() {
			return avg;
		}
		
		public long getSamples() {
			return count;
		}
		
		public double getStdev() {
			return std;
		}
		
		public long getMin() {
			return min;
		}
		
		public long getMax() {
			return max;
		}
		
		public long getCancels() {
			return cancels;
		}
	}
}
