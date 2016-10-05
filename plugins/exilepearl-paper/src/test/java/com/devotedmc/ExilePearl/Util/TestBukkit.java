package com.devotedmc.ExilePearl.Util;

import static org.mockito.Mockito.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

public class TestBukkit {

	public static Server create(final String name, final boolean useLogger) {
		
		Logger logger = Logger.getLogger(name);
		
		Formatter formatter = new Formatter() {
			
			private final DateFormat df = new SimpleDateFormat("hh:mm:ss");
			
			@Override
			public String format(LogRecord record) {
				return String.format("[%s %s]: %s\n", df.format(new Date(record.getMillis())), record.getLevel().getLocalizedName(), formatMessage(record));
			}
		};
		
		logger.setUseParentHandlers(false);
		ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        
		World world = mock(World.class);
		when(world.getName()).thenReturn("world");
		
		World worldNether = mock(World.class);
		when(worldNether.getName()).thenReturn("world_nether");
		
		World worldEnd = mock(World.class);
		when(worldEnd.getName()).thenReturn("world_the_end");
		
		ItemFactory itemFactory = mock(ItemFactory.class);
		ItemMeta im = mock(ItemMeta.class);
	    when(itemFactory.getItemMeta(any(Material.class))).thenReturn(im);

	    PluginManager pluginManager = mock(PluginManager.class);
	    
	    BukkitScheduler scheduler = mock(BukkitScheduler.class);
		
		Server mockServer = mock(Server.class);
		when(mockServer.getWorld("world")).thenReturn(world);
		when(mockServer.getWorld("world_nether")).thenReturn(worldNether);
		when(mockServer.getWorld("world_the_end")).thenReturn(worldEnd);
		
		when(mockServer.getName()).thenReturn(name);
		when(mockServer.getVersion()).thenReturn("0.0.0");
		when(mockServer.getBukkitVersion()).thenReturn("0.0.0");		
		when(mockServer.getItemFactory()).thenReturn(itemFactory);
		when(mockServer.getPluginManager()).thenReturn(pluginManager);
		when(mockServer.getScheduler()).thenReturn(scheduler);
		
		if (useLogger) {
			when(mockServer.getLogger()).thenReturn(logger);
		} else {
			when(mockServer.getLogger()).thenReturn(mock(Logger.class));
		}
		
	    Bukkit.setServer(mockServer);
	    
	    return mockServer;
	}
	
	public static Server create() {
		return create("TestBukkit", false);
	}
	
	public static Server create(final boolean useLogger) {
		return create("TestBukkit", useLogger);
	}
	
}
