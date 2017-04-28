package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Sets;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.TimingsHackConfig;

import net.md_5.bungee.api.ChatColor;

public class TimingsHack extends SimpleHack<TimingsHackConfig> implements Listener, CommandExecutor {

	public static final String NAME = "TimingsHack";
	private static final int DEPTH_MAX = 30;
	private static final int HQ_CYCLES = 120000;
	private static final int LQ_CYCLES = 12000;
	
	/**
	 * Nanosecond time of last tick start.
	 */
	private long lastTick = 0l;
	/**
	 * Circular index pointer into <code>ticks</code>.
	 */
	private int tickRecord = 0;
	/**
	 * Circular array storing millisecond lengths of ticks.
	 */
	private long[] ticks = null; 
	
	private long sumTicks = 0l;
	private long cntTicks = 0l;
	private double avgTick = 0.0d;
	
	/**
	 * Nanosecond time of last HQ tick start.
	 */
	private long hqLastTick = 0l;
	/**
	 * Nanosecond record of "real" cpu allocation time for monitored thread, stored circularly
	 */
	private long[] hqCpuTime = null;
	/**
	 * Nanosecond record of "elapsed" time -- wallclock style, stored circularly
	 */
	private long[] hqElapsedTime = null;
	/**
	 * Circular index pointer into <code>hqCpuTime</code> and <code>hqElapsedTime</code>
	 */
	private int hqTickRecord = 0;
	/**
	 * Are we storing high resolution data right now?
	 */
	private boolean hqActive = false;

	/**
	 * This stores the starting <code>hqTickRecord</code> circular index point for the current game tick
	 * being tracked, to reduce hq cpu and elapsed data into "real" game tick data.
	 */
	private int hqToLqTickRecord = 0;
	/**
	 * Aggregated data from hq cpu time; circular array indexed by <code>tickRecord</code>.
	 */
	private long[] hqToLqCpuTime = null;
	/**
	 * Aggregated data from hq elapsed time; circular array indexed by <code>tickRecord</code>.
	 */
	private long[] hqToLqElapsedTime = null;
	
	/**
	 * Stores the breakdown of "where time is spent" inside each tick, leveraging a high resolution
	 * timer task. This high resolution timer task data is aggregated into the hqTickMap on a per class/method basis.
	 * Then, the data can be displayed over time.
	 */
	private ConcurrentHashMap<String, ClassMethod>[] hqTickMap = null;

	private int tickTask = 0;
	
	private BukkitTask listTask = null;
	private BukkitTask thresholdTask = null;

	TimingsMap tickVisualize = null;
	
	private long rootThread = -1;
	
	private ThreadMXBean threadBean = null;
	
	private ScheduledExecutorService executor = null;
	
	private ScheduledFuture<?> hqTask = null;
	
	public TimingsHack(SimpleAdminHacks plugin, TimingsHackConfig config) {
		super(plugin, config);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!config.isEnabled()) return true;
		
		if (command.getName().equalsIgnoreCase("thresholdtimings")) {
			if (args.length < 1) {
				sender.sendMessage("Please specify a threshold - a factor * against avg tick which if a particular tick exceeds it, outputs the worst offending classes");
				return true;
			}
			
			startHq();
			
			double factor = 1.0d;
			
			try {
				factor = Double.parseDouble(args[0]);
			} catch (NumberFormatException e) {
				sender.sendMessage("Cannot recognize " + args[0] + " as a factor -- should be something like 1.5 or 1.25");
				return true;
			}
			
			sender.sendMessage("Request for Threshold Timings at " + factor + " received.");

			if (this.thresholdTask != null) {
				try {
					this.thresholdTask.cancel();
				} catch (Exception e) {
					// ignore? TODO
				}
			}

			ThresholdTask thresholdTask = new ThresholdTask(sender, factor);
			this.thresholdTask = thresholdTask.runTaskTimerAsynchronously(SimpleAdminHacks.instance(), 1l, 1l);
			return true;
		} else if (command.getName().equalsIgnoreCase("listtimings")) {
			// do open sampling for a little while until bindtimings is called or nothing new is found.
			startHq();
			
			sender.sendMessage("Request to list timings received.");
			// Start monitor task a little later that accumulates and prints Classes to bind to
			if (this.listTask != null) {
				try {
					this.listTask.cancel();
				} catch (Exception e) {
					// ignore? TODO
				}
			}
			ListTask listTask = new ListTask(sender);
			this.listTask = listTask.runTaskTimerAsynchronously(SimpleAdminHacks.instance(), 20l, 1l);
			return true;
		} else if (command.getName().equalsIgnoreCase("stoptimings")) {
			stopHq();
			
			sender.sendMessage("Advanced timings stopped");
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage("No Map visualization support on console.");
			return true;
		}
		Player player = (Player) sender;
		if (command.getName().equalsIgnoreCase("showtimings")) {
			MapView view = Bukkit.createMap(player.getWorld());
			view.getRenderers().forEach(view::removeRenderer);
			view.addRenderer(this.tickVisualize);
			
			ItemStack viewMap = new ItemStack(Material.MAP, 1, view.getId());
			
			player.getInventory().addItem(viewMap);
			
			player.sendMessage("Check your inventory for a TPS visualization Map");
		} else if (command.getName().equalsIgnoreCase("bindtimings")) {
			if (args.length < 1) {
				player.sendMessage("You need to tell us what to bind to. Use listtimings if you don't know what is possible.");
				return true;
			}
			// show map with graph for a specific limited element
			startHq();
			
			// Delay a few ticks, then give into inventory a map that shows the class % of TPS use
		}
		return true;
	}
	
	@Override
	public void registerListeners() {
	}

	@Override
	public void registerCommands() {
		if (!config.isEnabled()) return;

		plugin().log("Registering showtimings commands");
		plugin().registerCommand("showtimings", this);
		plugin().registerCommand("bindtimings", this);
		plugin().registerCommand("thresholdtimings", this);
		plugin().registerCommand("listtimings", this);
		plugin().registerCommand("stoptimings", this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void dataBootstrap() {
		if (!config.isEnabled()) return;
		tickRecord = 0;
		ticks = new long[LQ_CYCLES]; // 20 ticks per second, 1200 ticks per minute, 12000 ticks in 10 minutes;
		hqToLqCpuTime = new long[LQ_CYCLES];
		hqToLqElapsedTime = new long[LQ_CYCLES];
		hqTickMap = new ConcurrentHashMap[LQ_CYCLES];
		hqActive = false;
		
		for (int i = 0; i < LQ_CYCLES; i++) {
			hqTickMap[i] = new ConcurrentHashMap<String, ClassMethod>(100);
		}

		lastTick = System.nanoTime();
		tickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(SimpleAdminHacks.getPlugin(SimpleAdminHacks.class),
				new Runnable() {
			@Override
			public void run(){
				tick();
			}
		}, 0l, 1l);
		
		tickVisualize = new TimingsMap();
		
		rootThread = Thread.currentThread().getId();
		threadBean = ManagementFactory.getThreadMXBean();
		executor = Executors.newSingleThreadScheduledExecutor();
	}
	
	private void startHq() {
		if (hqActive) return; // already alive.
		SimpleAdminHacks.instance().log("Starting HQ timer sequence");
		hqCpuTime = new long[HQ_CYCLES]; // 2 * 60 * 1000 -- two minutes of milliseconds.
		hqElapsedTime = new long[HQ_CYCLES];
		hqTickRecord = 0;
		hqToLqTickRecord = 0; // tails behind hqTickRecord
		
		hqCpuTime[0] = threadBean.getThreadCpuTime(rootThread);
		hqLastTick = System.nanoTime();
		hqActive = true;
		
		SimpleAdminHacks.instance().log("Starting actual HQ timer");
		hqTask = executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					hqTick();
				} catch (Exception e) {
					SimpleAdminHacks.instance().log(Level.WARNING, "HQTick Failure:", e);
					stopHq();
				}
			}
		}, 1l, 1l, TimeUnit.MILLISECONDS);
	}
	
	private void stopHq() {
		if (!hqActive) return; // already paused
		SimpleAdminHacks.instance().log("Stopping HQ timer");
		hqActive = false;
		
		hqTask.cancel(true);
	}
	
	private void tick() {
		long newTick = System.nanoTime();
		long tickTime = newTick - lastTick;
		int tempTickRecord = tickRecord;
		int ePoint = hqTickRecord; // ignore _current_ as its unfilled.
		if (++tickRecord >= LQ_CYCLES) { // TODO race condition fault state, time between inc and reset could result in HQ Index bound error
			tickRecord = 0;
		}
		hqTickMap[tickRecord].clear(); // prepare future tickmap.
		
		ticks[tempTickRecord] = tickTime;
		
		sumTicks += tickTime - ticks[tickRecord]; // new record - outgoing record.
		cntTicks = (cntTicks == LQ_CYCLES) ? LQ_CYCLES : cntTicks + 1;
		avgTick = (double) sumTicks / (double) cntTicks;
		
		if (hqActive) {
			// now do Hq tick time summary
			hqToLqCpuTime[tempTickRecord] = 0l;
			hqToLqElapsedTime[tempTickRecord] = 0l;
			for (;hqToLqTickRecord != ePoint; hqToLqTickRecord++) {
				if (hqToLqTickRecord >= HQ_CYCLES) {
					hqToLqTickRecord = 0;
					if (hqToLqTickRecord == ePoint) break;
				}
				hqToLqCpuTime[tempTickRecord] += hqCpuTime[hqToLqTickRecord]; // sum up CPU time from hq slices into tick slices.
				hqToLqElapsedTime[tempTickRecord] += hqElapsedTime[hqToLqTickRecord];
			}
		}
		
		if (tickRecord % 1000 == 999) {
			SimpleAdminHacks.instance().log(Level.INFO, "Recorded 1000 ticks so far, avg: {0}", avgTick);
		}
		
		lastTick = newTick;
	}
	
	private void hqTick() {
		long newTick = System.nanoTime();
		long tickTime = newTick - hqLastTick;
		int nextHqTick = hqTickRecord >= HQ_CYCLES - 1 ? 0 : hqTickRecord + 1;
		
		hqCpuTime[nextHqTick] = threadBean.getThreadCpuTime(rootThread); // "start" of next record stored now.
		hqCpuTime[hqTickRecord] = hqCpuTime[nextHqTick] - hqCpuTime[hqTickRecord]; // diff against old start.
		hqElapsedTime[hqTickRecord] = tickTime; // tick over tick time too, not just cpu time. Evals drift / co-sharing issues.
		if (hqCpuTime[hqTickRecord] == 0) {
			hqCpuTime[hqTickRecord] = tickTime;
		}
		
		ThreadInfo threadInfo = threadBean.getThreadInfo(rootThread, DEPTH_MAX);
		
		StackTraceElement[] stack = threadInfo.getStackTrace(); 
		
		//SimpleAdminHacks.instance().log(Level.INFO, "HQTick: {0} {1} {2}", hqTickRecord, tickTime, stack.length);
		int tmpTickRecord = tickRecord;
		if (tmpTickRecord >= LQ_CYCLES) tmpTickRecord = 0;
		for (StackTraceElement frame: stack) {
			String className = frame.getClassName();
			String methodName = frame.getMethodName();
			hqTickMap[tmpTickRecord].compute(className, (clz, clzMethod) -> { // If we have one, atomically increment the corresponding method.
				if (clzMethod == null) {
					clzMethod = new ClassMethod(clz);
				}
				clzMethod.inc(methodName, hqCpuTime[hqTickRecord]);
				return clzMethod;
			});
		}
		
		
		hqTickRecord++;
		if (hqTickRecord >= HQ_CYCLES) {
			hqTickRecord = 0;
		}
		
		if (hqTickRecord % 10000 == 9999) {
			SimpleAdminHacks.instance().log("Recorded 10000 hq ticks so far");
		}
		
		hqLastTick = newTick;
	}
	
	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		if (tickTask > 0) {
			Bukkit.getScheduler().cancelTask(tickTask);
		}
		
		if (listTask != null) {
			try {
				listTask.cancel();
			} catch (Exception e) {
				
			}
		}
		if (thresholdTask != null) {
			try {
				thresholdTask.cancel();
			} catch (Exception e) {
				
			}
		}
		stopHq();
	}

	@Override
	public String status() {
		if (!config.isEnabled()) {
			return "Timings Hack disabled.";
		} else {
			return "Timings Hack enabled.";
		}
	}
	
	public static TimingsHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new TimingsHackConfig(plugin, config);
	}

	/**
	 * Turns out maps are super easy.
	 * 
	 * Thanks to some inspiration from LagMonitor, here we have a nice TPS monitor for in-game
	 * operators. All operators are sent the same map. The map object they are using becomes a 
	 * normal map on reload. They can get a new active one by issuing <code>/showtimings</code>
	 * 
	 * On top is color AND size stacked ticks; each vertical stack is 20 ticks (theoretic "second").
	 * Up to 128 stacks horizontally will be displayed. Large vertical displacement on top indicates
	 * significant tick overload. 
	 * 
	 * On bottom is color differentiated only; greens are relatively good (within factor 1.5 of 50ms target)
	 * purples / reds are not good (within factor 2-3 of 50ms target) white is very bad (4x or more of 50ms target).
	 * These colors apply below and above.
	 * 
	 * @author ProgrammerDan
	 *
	 */
	class TimingsMap extends MapRenderer {

		@SuppressWarnings("deprecation")
		private byte resolveColor(long tickLength) {
			if (tickLength <= 10000l) {
				return MapPalette.TRANSPARENT;
			} else if (tickLength < 50000000l) {
				return MapPalette.matchColor(0, 255, 0); // bright green
			} else if (tickLength < 75600000l) {
				int greenShade = (int) Math.floorDiv((tickLength - 50000000l), 2000000l);
				return MapPalette.matchColor(0, 255-greenShade, 0); // bright to midshade green
			} else if (tickLength < 88400000l) {
				int purpleShade = (int) Math.floorDiv((tickLength - 75600000l), 500000l);
				return MapPalette.matchColor(purpleShade, 127 - (purpleShade / 2), purpleShade); // midshade green to bright purple
			} else if (tickLength < 114000000l) {
				int redShade = (int) Math.floorDiv((tickLength - 88400000l), 1000000l);
				return MapPalette.matchColor(255, 0, 255 - redShade);
			} else if (tickLength < 216400000l) {
				int whiteShade = (int) Math.floorDiv((tickLength - 114000000l), 4000000l);
				return MapPalette.matchColor(255, whiteShade, whiteShade);
			} else {
				return MapPalette.WHITE;
			}
		}
		
		private int resolveWidth(long tickLength) {
			if (tickLength <= 50000000l){
				return 1;
			} else if (tickLength <= 75600000l) {
				return 2;
			} else if (tickLength <= 88400000l) {
				return 3;
			} else if (tickLength <= 114000000l) {
				return 4;
			} else {
				return 5;
			}
		}
		
		@Override
		public void render(MapView view, MapCanvas canvas, Player player) {
			if (!isEnabled()) return;
			try {
				int storeStart = tickRecord;
				storeStart = storeStart - (storeStart % 20); // pin it to the nearest second
				if (storeStart < 0) storeStart += LQ_CYCLES;
				int lastRow = 128;
				int newRow = 127;
				int nextCol = 100;
				int downCol = 102;
				for (int displace = 0; displace < 2560; displace++) {
					if (newRow != lastRow) {
						// clear row
						for (int y = 0; y <= 127; y++) {
							canvas.setPixel(newRow, y, (byte) 0);
						}
						lastRow = newRow;
					}
					int activeIdx = storeStart - displace;
					if (activeIdx < 0) activeIdx += LQ_CYCLES;
					long recorded = ticks[activeIdx];
					byte color = resolveColor(recorded);
					for (int j = 0; j < resolveWidth(recorded);j++,nextCol--) {
						canvas.setPixel(newRow, nextCol, color);
					}
					canvas.setPixel(newRow, 101, MapPalette.DARK_GRAY);
					canvas.setPixel(newRow, downCol++, color);
					if (displace % 20 == 19) {
						newRow--;
						nextCol = 100;
						downCol = 101;
					}
				}
			} catch (Exception e) {
				SimpleAdminHacks.instance().log(Level.WARNING, "TimingsMap Render failure ", e);
			}
		}
	}
	
	class BindTimingMap extends MapRenderer {

		private String regex;
		
		public BindTimingMap(String regex) {
			super();
			this.regex = regex;
		}
		
		private int resolveY(long avgSec, long avgTick) {
			if (avgSec == 0) {
				return 22;
			} else if (avgSec < avgTick) {
				return (int) Math.floor(((double) avgSec / (double) avgTick) * 22d);
			} else { //if (avgSec > avgTick) {
				return 23 + (int) Math.floor(1d - ((double) avgTick / (double) avgSec) * 11d);
			}
		}
		
		/**
		 * Deals with color at 10% to 40% of total tick -- above 40% will be full white; 10% green, and so on.
		 * 
		 * @param tickLength How long did this take to run
		 * @return Color code byte
		 */
		@SuppressWarnings("deprecation")
		private byte resolveColor(long tickLength) {
			if (tickLength <= 10000l) {
				return MapPalette.TRANSPARENT;
			} else if (tickLength < 5000000l) {
				return MapPalette.matchColor(0, 255, 0); // bright green
			} else if (tickLength < 7560000l) {
				int greenShade = (int) Math.floorDiv((tickLength - 5000000l), 200000l);
				return MapPalette.matchColor(0, 255-greenShade, 0); // bright to midshade green
			} else if (tickLength < 8840000l) {
				int purpleShade = (int) Math.floorDiv((tickLength - 7560000l), 50000l);
				return MapPalette.matchColor(purpleShade, 127 - (purpleShade / 2), purpleShade); // midshade green to bright purple
			} else if (tickLength < 11400000l) {
				int redShade = (int) Math.floorDiv((tickLength - 8840000l), 100000l);
				return MapPalette.matchColor(255, 0, 255 - redShade);
			} else if (tickLength < 21640000l) {
				int whiteShade = (int) Math.floorDiv((tickLength - 11400000l), 400000l);
				return MapPalette.matchColor(255, whiteShade, whiteShade);
			} else {
				return MapPalette.WHITE;
			}
		}
		
		private int resolveWidth(long tickLength) {
			if (tickLength <= 50000000l){
				return 1;
			} else if (tickLength <= 75600000l) {
				return 2;
			} else if (tickLength <= 88400000l) {
				return 3;
			} else if (tickLength <= 114000000l) {
				return 4;
			} else {
				return 5;
			}
		}
		
		@Override
		public void render(MapView view, MapCanvas canvas, Player player) {
			if (!isEnabled()) return;
			try {
				int storeStart = tickRecord;
				storeStart = storeStart - (storeStart % 20); // pin it to the nearest second
				if (storeStart < 0) storeStart += LQ_CYCLES;
				int lastRow = 128;
				int newRow = 127;
				int nextCol = 100;
				int downCol = 102;
				for (int displace = 0; displace < 2560; displace++) {
					if (newRow != lastRow) {
						// clear row
						for (int y = 0; y <= 127; y++) {
							canvas.setPixel(newRow, y, (byte) 0);
						}
						lastRow = newRow;
					}
					int activeIdx = storeStart - displace;
					if (activeIdx < 0) activeIdx += LQ_CYCLES;
					long recorded = ticks[activeIdx];
					byte color = resolveColor(recorded);
					for (int j = 0; j < resolveWidth(recorded);j++,nextCol--) {
						canvas.setPixel(newRow, nextCol, color);
					}
					canvas.setPixel(newRow, 101, MapPalette.DARK_GRAY);
					canvas.setPixel(newRow, downCol++, color);
					if (displace % 20 == 19) {
						newRow--;
						nextCol = 100;
						downCol = 101;
					}
				}
			} catch (Exception e) {
				SimpleAdminHacks.instance().log(Level.WARNING, "TimingsMap Render failure ", e);
			}
		}
	}

	/**
	 * Holds timing data per class and per method in the class, based on thread dumps.
	 * 
	 * @author ProgrammerDan
	 *
	 */
	class ClassMethod implements Comparable<ClassMethod>{
		String clazz = null;
		ConcurrentHashMap<String, Long> method = null;
		
		long max = 0l; //nanoseconds
		
		public ClassMethod(String clazz) {
			this.clazz = clazz;
			this.method = new ConcurrentHashMap<String, Long>();
			this.max = 0l;
		}
		
		public void inc(String method, long elapsed) {
			Long newm = this.method.merge(method, elapsed, (x, y) -> x + y); // insert, or merge if already present.
			if (newm > max) {
				max = newm;
			}
		}
		
		public String getClazz() {
			return clazz;
		}
		
		public long max() {
			return max;
		}
		
		public int methodCount() {
			return this.method.size();
		}
		
		public double fraction(String method) {
			Long num = timeIn(method);
			if (max <= 0l) return 0d;
			if (num <= 0l) return 0d;
			return (double) num / (double) max;
		}
		
		public long timeIn(String method) {
			return this.method.getOrDefault(method, 0L);
		}

		@Override
		public int compareTo(ClassMethod o) {
			return clazz.compareTo(o.clazz);
		}
		
		@Override
		public int hashCode() {
			return clazz.hashCode();
		}
		
		public boolean matches(String regex) {
			return clazz.matches(regex);
		}
	}
	
	class ListTask extends BukkitRunnable {

		public static final long CANCEL_AFTER = 1000l;
		
		Set<String> classOptions = Sets.newConcurrentHashSet();
		
		long lastAdd = 0l;;
		int lastTickInternal = -1;
		
		WeakReference<CommandSender> sender = null;
		
		public ListTask(CommandSender sender) {
			super();
			this.sender = new WeakReference<CommandSender>(sender);
		}
		
		@Override
		public void run() {
			if (lastTickInternal == tickRecord) return;
					
			if (lastAdd == 0l) {
				lastAdd = System.currentTimeMillis();
			}
			
			int checkTick = tickRecord - 1;
			if (checkTick < 0) {
				checkTick += TimingsHack.LQ_CYCLES;
			}
			
			//SimpleAdminHacks.instance().log(Level.INFO, "Checking new class options {0}, {1}", checkTick, hqTickMap[checkTick].size());
			
			ArrayList<String> keys = new ArrayList<String>(hqTickMap[checkTick].keySet());
			
			keys.removeAll(classOptions);
			
			if (keys.isEmpty()) {
				if (System.currentTimeMillis() - lastAdd > CANCEL_AFTER) {
					this.cancel();
				}
			} else {
				classOptions.addAll(keys);
				lastAdd = System.currentTimeMillis();
				CommandSender toSend = sender.get();
				if (toSend != null) {
					for (String key : keys) {
						toSend.sendMessage("Usable: /bindtimings " + key);
					}
				} else {
					this.cancel();
				}
			}
			
			lastTickInternal = tickRecord;
		}
		
	}

	class ThresholdTask extends BukkitRunnable {

		Set<String> classOptions = Sets.newConcurrentHashSet();
		
		int lastTickInternal = -1;
		
		double threshold = 1.5d;
		
		WeakReference<CommandSender> sender = null;
		
		public ThresholdTask(CommandSender sender, double threshold) {
			super();
			this.sender = new WeakReference<CommandSender>(sender);
			this.threshold = threshold;
		}
		
		@Override
		public void run() {
			if (lastTickInternal == tickRecord) return;
					
			int checkTick = tickRecord - 1;
			if (checkTick < 0) {
				checkTick += TimingsHack.LQ_CYCLES;
			}
			
			if ( (double) ticks[checkTick] <= avgTick * threshold ) {
				return;
			}
			
			StringBuilder badTick = new StringBuilder();
			badTick.append(ChatColor.AQUA).append("Typical tick is ").append(ChatColor.GREEN).append(String.format("%.2f", avgTick)).append("ns");
			badTick.append(ChatColor.AQUA).append(" this tick was ").append(ChatColor.RED).append(String.format("%d", ticks[checkTick])).append("ns");
			badTick.append(ChatColor.AQUA).append('\n').append("   CPU Time: \n").append(ChatColor.BLUE)
					.append(String.format(" %13d", hqToLqCpuTime[checkTick])).append(ChatColor.AQUA).append("ns")
					.append(ChatColor.AQUA).append("\n   Elapsed Time: \n").append(ChatColor.BLUE)
					.append(String.format(" %13d", hqToLqElapsedTime[checkTick])).append(ChatColor.AQUA).append("ns");
			
			TreeMap<Long, String> reveals = new TreeMap<Long, String>();
			
			for (ClassMethod cm : hqTickMap[checkTick].values()) {
					reveals.compute(cm.max(), (k, S) -> {
						if (S != null) {
							return String.format("%s\n%s %13d%sns: %s (%d methods)", S, ChatColor.BLUE, cm.max(), ChatColor.WHITE, cm.getClazz(), cm.methodCount());
						} else {
							return String.format("\n%s %13d%sns: %s (%d methods)", ChatColor.BLUE, cm.max(), ChatColor.WHITE, cm.getClazz(), cm.methodCount());
						}
					});
			}
			
			for (Long key : reveals.descendingKeySet()) {
				badTick.append(reveals.get(key));
			}
			ArrayList<String> keys = new ArrayList<String>(hqTickMap[checkTick].keySet());
			
			keys.removeAll(classOptions);
			
			CommandSender toSend = sender.get();
			if (toSend != null) {
				toSend.sendMessage(badTick.toString());
			} else {
				this.cancel();
			}
			
			lastTickInternal = tickRecord;
		}
		
	}

}
	
