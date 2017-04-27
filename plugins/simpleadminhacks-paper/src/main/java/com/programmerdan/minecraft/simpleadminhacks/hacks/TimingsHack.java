package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.logging.Level;

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

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.TimingsHackConfig;

public class TimingsHack extends SimpleHack<TimingsHackConfig> implements Listener, CommandExecutor {

	public static final String NAME = "TimingsHack";
	
	private long lastTick = 0l;
	
	private int tickRecord = 0;
	private long[] ticks = null; 
	
	private int tickTask = 0;

	TimingsMap tickVisualize = null;
	
	public TimingsHack(SimpleAdminHacks plugin, TimingsHackConfig config) {
		super(plugin, config);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!config.isEnabled()) return true;
		
		if (!(sender instanceof Player)) {
			sender.sendMessage("No Map visualization support on console.");
			return true;
		}
		Player player = (Player) sender;
		MapView view = Bukkit.createMap(player.getWorld());
		view.getRenderers().forEach(view::removeRenderer);
		view.addRenderer(this.tickVisualize);
		
		ItemStack viewMap = new ItemStack(Material.MAP, 1, view.getId());
		
		player.getInventory().addItem(viewMap);
		
		player.sendMessage("Check your inventory for a TPS visualization Map");
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
	}

	@Override
	public void dataBootstrap() {
		if (!config.isEnabled()) return;
		tickRecord = 0;
		ticks = new long[12000]; // 20 ticks per second, 1200 ticks per minute, 12000 ticks in 10 minutes;
		
		lastTick = System.nanoTime();
		tickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(SimpleAdminHacks.getPlugin(SimpleAdminHacks.class),
				new Runnable() {
			@Override
			public void run(){
				tick();
			}
		}, 0l, 1l);
		
		tickVisualize = new TimingsMap();
	}
	
	private void tick() {
		long newTick = System.nanoTime();
		long tickTime = newTick - lastTick;
		
		ticks[tickRecord++] = tickTime;
		
		if (tickRecord >= 12000) {
			tickRecord = 0;
		}
		
		if (tickRecord % 1000 == 999) {
			SimpleAdminHacks.instance().log("Recorded 1000 ticks so far");
		}
		
		lastTick = newTick;
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		Bukkit.getScheduler().cancelTask(tickTask);
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
			int storeStart = tickRecord;
			storeStart = storeStart - (storeStart % 20); // pin it to the nearest second
			if (storeStart < 0) storeStart += 12000;
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
				if (activeIdx < 0) activeIdx += 12000;
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
					//SimpleAdminHacks.instance().log(Level.INFO, "R{0} C{1}", newRow, nextCol);
				}
			}
			
		}
		
	}

}
	
