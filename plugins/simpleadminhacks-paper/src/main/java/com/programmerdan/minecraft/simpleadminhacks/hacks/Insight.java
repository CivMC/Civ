package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.InsightConfig;

/**
 * Config example:
 * <code>
 *   Insight:
 *     enabled: true
 *     into:
 *     - org.bukkit.event.player
 *     - org.bukkit.event.block
 *     - org.bukkit.event.inventory
 * 
 * This "into" list defines event classpaths. Events found in that classpath are 
 * instrumented; listeners are reordered to put _this_ hack's listeners as first and last to run.
 * 
 * These listeners bypass safe bukkit convention, Timings, and can handle
 * async or sync events.
 * 
 * Can monitor not only bukkit events but also plugin / custom events; just give the
 * valid classpath.
 * 
 * This bypasses the "safe" bukkit way of registering listeners for events. So it
 * also bypasses Timings.
 * 
 * This is the only warning I'll give. Be careful with this and prefer to leave it off.
 * 
 * Wireups only happen on restart, I'm dubious about injecting live for now.
 *
 * @author ProgrammerDan
 */
public class Insight extends SimpleHack<InsightConfig> implements CommandExecutor, Listener {

	public static final String NAME = "Insight";

	private Map<String, InsightStat> tracking;
	private List<Class<? extends Event>> rebounders;

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


	@SuppressWarnings("unchecked")
	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering Insight reordering hack");
			plugin().registerListener(this);

			plugin().log("Registering Insight event instrumentations");

			if (config.getInsightOn() == null) {
				plugin().log("No instrumentations defined, disabling.");
				this.softDisable();
				return;
			}
			try {
				ClassPath getSamplersPath = ClassPath.from(plugin().exposeClassLoader());

				for (String clsPackage : config.getInsightOn()) {
					ImmutableSet<ClassPath.ClassInfo> clsInfoSet = getSamplersPath.getTopLevelClasses(clsPackage);
					if (clsInfoSet == null || clsInfoSet.isEmpty()) {
						plugin().log(Level.INFO, "Package {0} doesn't exist or is empty.", clsPackage);
						continue;
					}
					plugin().log(Level.INFO, "Scanning package {0} for events.", clsPackage);
					for (ClassPath.ClassInfo clsInfo : clsInfoSet) {
						try {
							Class<?> clazz = clsInfo.load();
							if (clazz != null && Event.class.isAssignableFrom(clazz)) {
								plugin().log(Level.INFO, "...Found event {0}, registering", clazz.getName());
								// This is some advanced shit. It also bypasses the timings, so, be careful, ok?
								plugin().getServer().getPluginManager().registerEvent((Class<? extends Event>) clazz, this, EventPriority.LOWEST, new EventExecutor() {
									@Override
									public void execute(Listener arg0, Event arg1) throws EventException {
										try {
											((Insight) arg0).start(arg1);
										} catch (Exception e) {
											// no-op, we were never here
										}
									}
								}, plugin(), true);
								plugin().getServer().getPluginManager().registerEvent((Class<? extends Event>) clazz, this, EventPriority.MONITOR, new EventExecutor() {
									@Override
									public void execute(Listener arg0, Event arg1) throws EventException {
										try {
											((Insight) arg0).end(arg1);
										} catch (Exception e) {
											// no-op, we were never here
										}
									}
								}, plugin(), true);
								rebounders.add((Class<? extends Event>) clazz);
							}
						} catch (NoClassDefFoundError e) {
							plugin().log(Level.INFO, "...Unable to register event {0} due to dependency failure", clsInfo.getName());
						} catch (IllegalPluginAccessException ipae) {
							plugin().log(Level.INFO, "...Unable to register event {0}; abstract or uninstanced event", clsInfo.getName());
						} catch (Exception e) {
							plugin().log(Level.WARNING, "...Failed to complete event registration for " + clsInfo.getName(), e);
						}
					}
				}
			} catch (IOException ioe) {
				plugin().log(Level.WARNING, "Failed to register event classes", ioe);
			}
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
				//event.block
				rebounders.forEach(e -> reboundHandlers(e));
			}
		}, 60l); // 3 seconds
	}

	/**
	 * For tracked events we hijack priority ordering insertions
	 * We to grab the registeredlistener array, remove all listeners
	 *   then readd, forcing _this_ plugin for _that_ event to be first(s) and last(s) in the list.
	 *   
	 * @param clazz The Event class whose handlers we want to reorder
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
			this.rebounders = new ArrayList<Class<? extends Event>>();
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
			this.rebounders = null;
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
