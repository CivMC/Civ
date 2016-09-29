package com.devotedmc.ExilePearl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.devotedmc.ExilePearl.core.CorePearlFactory;
import com.devotedmc.ExilePearl.listener.ExileListener;
import com.devotedmc.ExilePearl.listener.PlayerListener;
import com.devotedmc.ExilePearl.storage.AsyncStorageWriter;
import com.devotedmc.ExilePearl.storage.PluginStorage;
import com.devotedmc.ExilePearl.storage.RamStorage;
import com.devotedmc.ExilePearl.util.MockScheduler;
import com.devotedmc.ExilePearl.util.MockWorld;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExilePearlPlugin.class, PluginDescriptionFile.class })
public class ExilePearlPluginIntegrationTest {
	
    private static final File pluginDirectory = new File("bin/test/server/plugins/ExilePearl");
    public static final File serverDirectory = new File("bin/test/server");
    public static final File worldsDirectory = new File("bin/test/server");
    
    public static Logger logger;
	
    private static Server mockServer;
    private static ItemFactory mockItemFactory;
    private static MockScheduler mockScheduler;
    private static CommandSender commandSender;
    private static PluginManager mockPluginManager;
    private static ArrayList<MockWorld> worlds;
	private static ExilePearlPlugin plugin;

	@BeforeClass
	public static void setUpClass() throws Exception {
		setupLogger();
		
        worlds = new ArrayList<MockWorld>();
        
        // Create default worlds
        createMockWorld(new WorldCreator("world"));
        createMockWorld(new WorldCreator("world_nether"));
        createMockWorld(new WorldCreator("world_the_end"));
        
		plugin = createPlugin();
		
        // Init our command sender
        final Logger commandSenderLogger = Logger.getLogger("CommandSender");
        commandSenderLogger.setParent(logger);
        commandSender = mock(CommandSender.class);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                commandSenderLogger.info(ChatColor.stripColor((String) invocation.getArguments()[0]));
                return null;
            }}).when(commandSender).sendMessage(anyString());
        when(commandSender.getServer()).thenReturn(mockServer);
        when(commandSender.getName()).thenReturn("MockCommandSender");
        when(commandSender.isPermissionSet(anyString())).thenReturn(true);
        when(commandSender.isPermissionSet(Matchers.isA(Permission.class))).thenReturn(true);
        when(commandSender.hasPermission(anyString())).thenReturn(true);
        when(commandSender.hasPermission(Matchers.isA(Permission.class))).thenReturn(true);
        when(commandSender.addAttachment(plugin)).thenReturn(null);
        when(commandSender.isOp()).thenReturn(true);
        when(mockServer.getLogger()).thenReturn(logger);
        
        Bukkit.setServer(mockServer);
        
		plugin.onEnable();
	}
	
    /**
     * Tears down the test fixture
     * @return true if success
     * @throws Exception 
     */
	@AfterClass
    public static void tearDown() throws Exception {

        plugin.onDisable();
        
        try {
            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(Class.forName("org.bukkit.Bukkit"), null);
        } catch (Exception e) {
            log(Level.SEVERE, "Error while trying to unregister the server from Bukkit. Has Bukkit changed?");
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }

        FileUtils.deleteDirectory(serverDirectory);
        worlds.clear();
        
        log("TEAR DOWN COMPLETE");
    }
	
	private static void setupLogger() {
		logger = Logger.getLogger("ExilePearl");
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
	}
	
    /**
     * Creates the mock plugin instance
     * @return the new plugin instance
     */
	private static ExilePearlPlugin createPlugin() throws Exception {		
		// Mock the plugin loader
		PluginLoader pluginLoader = mock(PluginLoader.class);
		
		// Mock the PDF
        PluginDescriptionFile pdf = PowerMockito.spy(new PluginDescriptionFile("ExilePearl", "0.0.1", "com.devotedmc.ExilePearl.ExilePearlPlugin"));
        when(pdf.getAuthors()).thenReturn(new ArrayList<String>());
        when(pdf.getPermissions()).thenReturn(new ArrayList<Permission>());
        
        // Mock the item factory
        mockItemFactory = mock(ItemFactory.class);
        when(mockItemFactory.getItemMeta(any())).thenReturn(PowerMockito.mock(ItemMeta.class));
        
        ConsoleCommandSender consoleSender = mock(ConsoleCommandSender.class);
        
        mockPluginManager = mock(PluginManager.class);
        
        mockScheduler = MockScheduler.create();

        // Mock the server
        mockServer = mock(Server.class);
        when(mockServer.getItemFactory()).thenReturn(mockItemFactory);
        when(mockServer.getScheduler()).thenReturn(mockScheduler);
        when(mockServer.getLogger()).thenReturn(logger);
        when(mockServer.getConsoleSender()).thenReturn(consoleSender);
        when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
        when(mockServer.getName()).thenReturn("TestBukkit");
        when(mockServer.getVersion()).thenReturn("0.0.0");
        when(mockServer.getBukkitVersion()).thenReturn("0.0.0");
        
        // Create the plugin instance
        @SuppressWarnings("deprecation")
        ExilePearlPlugin plugin = PowerMockito.spy(new ExilePearlPlugin(pluginLoader, mockServer, pdf, pluginDirectory, new File(pluginDirectory, "testPluginFile")));
        
		// Put all files in bin/test
		doReturn(pluginDirectory).when(plugin).getDataFolder();
        
        when(mockPluginManager.getPlugin("ExilePearl")).thenReturn(plugin);
        when(mockPluginManager.getPlugins()).thenReturn(new JavaPlugin[] { plugin } );
        
        doReturn(true).when(plugin).isEnabled();
        doReturn(logger).when(plugin).getLogger();
        
        plugin.onLoad();
        
        // Override all the private fields with spy  instances
        ExilePearlConfig pearlConfig =  spy(new ExilePearlConfig(plugin));
        Field field = ExilePearlPlugin.class.getDeclaredField("pearlConfig");
        field.setAccessible(true);
        field.set(plugin, pearlConfig);
        
        PearlFactory pearlFactory =  spy(new CorePearlFactory(plugin));
        field = ExilePearlPlugin.class.getDeclaredField("pearlFactory");
        field.setAccessible(true);
        field.set(plugin, pearlFactory);
        
        PluginStorage storage =  spy(new AsyncStorageWriter(new RamStorage(), plugin));
        field = ExilePearlPlugin.class.getDeclaredField("storage");
        field.setAccessible(true);
        field.set(plugin, storage);
        
        PearlManager pearlManager =  spy(pearlFactory.createPearlManager());
        field = ExilePearlPlugin.class.getDeclaredField("pearlManager");
        field.setAccessible(true);
        field.set(plugin, pearlManager);
        
        PearlWorker pearlWorker =  spy(pearlFactory.createPearlWorker());
        field = ExilePearlPlugin.class.getDeclaredField("pearlWorker");
        field.setAccessible(true);
        field.set(plugin, pearlWorker);
        
        PearlLoreGenerator loreGenerator =  spy(pearlFactory.createLoreGenerator());
        field = ExilePearlPlugin.class.getDeclaredField("loreGenerator");
        field.setAccessible(true);
        field.set(plugin, loreGenerator);
        
        PlayerListener playerListener =  spy(new PlayerListener(plugin));
        field = ExilePearlPlugin.class.getDeclaredField("playerListener");
        field.setAccessible(true);
        field.set(plugin, playerListener);
        
        ExileListener exileListener =  spy(new ExileListener(plugin, pearlConfig));
        field = ExilePearlPlugin.class.getDeclaredField("exileListener");
        field.setAccessible(true);
        field.set(plugin, exileListener);
        
        return plugin;
	}
	
    /**
     * Creates a mock world
     * @param creator The world creator
     * @return The new world
     */
    public static World createMockWorld(WorldCreator creator) {
        File worldFile = new File(serverDirectory, creator.name());
        log("Creating world-folder: " + worldFile.getAbsolutePath());
        worldFile.mkdirs();
    	
    	MockWorld mockWorld = MockWorld.create(creator.name(), creator.environment(), creator.type());
        mockWorld.worldFolder = new File(serverDirectory, mockWorld.getName());
    	new File(worldsDirectory, mockWorld.getName()).mkdir();
    	worlds.add(mockWorld);
    	return mockWorld;
    }
    
    public static void log(String message) {
    	log(Level.INFO, message);
    }
    
    public static void log(Level level, String message) {
    	logger.log(level, message);
    }

	@Test
	public void test() {
		assertTrue(true);
	}

}
