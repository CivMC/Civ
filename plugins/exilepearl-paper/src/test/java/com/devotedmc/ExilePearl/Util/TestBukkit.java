package com.devotedmc.ExilePearl.Util;

import static org.mockito.Mockito.*;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

public class TestBukkit {
	
	public static Logger logger;
	public static final List<MockWorld> worlds = new ArrayList<MockWorld>();
	
    public static final File pluginDirectory = new File("bin/test/server/plugins/Sabre");
    public static final File serverDirectory = new File("bin/test/server");
    public static final File worldsDirectory = new File("bin/test/server");

	public static Server create(final String name, final boolean useLogger) {
		
		logger = Logger.getLogger(name);
		
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
        
		World world = createMockWorld(new WorldCreator("world"));
		World worldNether = createMockWorld(new WorldCreator("world_nether"));
		World worldEnd = createMockWorld(new WorldCreator("world_the_end"));
		
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
	
    /**
     * Creates a mock world
     * @param creator The world creator
     * @return The new world
     */
    public static World createMockWorld(WorldCreator creator) {
        File worldFile = new File(serverDirectory, creator.name());
        worldFile.mkdirs();
    	
    	MockWorld mockWorld = MockWorld.create(creator.name(), creator.environment(), creator.type());
        mockWorld.worldFolder = new File(serverDirectory, mockWorld.getName());
    	new File(worldsDirectory, mockWorld.getName()).mkdir();
    	worlds.add(mockWorld);
    	return mockWorld;
    }
	
}
