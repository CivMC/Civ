package com.devotedmc.ExilePearl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.devotedmc.ExilePearl.core.CorePearlFactory;
import com.devotedmc.ExilePearl.listener.ExileListener;
import com.devotedmc.ExilePearl.listener.PlayerListener;
import com.devotedmc.ExilePearl.storage.AsyncStorageWriter;
import com.devotedmc.ExilePearl.storage.MySqlStorage;
import com.devotedmc.ExilePearl.storage.PluginStorage;
import com.devotedmc.ExilePearl.util.MockScheduler;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, ExilePearlPlugin.class, PluginDescriptionFile.class, Server.class, PluginManager.class })
public class ExilePearlPluginIntegrationTest {
	
    private static final File pluginDirectory = new File("bin/test/server/plugins/ExilePearl");
    
    public static final Logger logger = Logger.getLogger("ExilePearl-Test");
	
    private static Server mockServer;
    private static ItemFactory mockItemFactory;
    private static MockScheduler mockScheduler;
    private static CommandSender commandSender;
    private static PluginManager mockPluginManager;
	private static ExilePearlPlugin plugin;

	@BeforeClass
	public static void setUpClass() throws Exception {
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
        
        Bukkit.setServer(mockServer);
	}
	
    /**
     * Creates the mock plugin instance
     * @return the new plugin instance
     */
	private static ExilePearlPlugin createPlugin() throws Exception {
		
		Logger logger = Logger.getLogger("ExilePearl-Test");
		
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
        
        // Create the plugin instance
        @SuppressWarnings("deprecation")
        ExilePearlPlugin plugin = new ExilePearlPlugin(pluginLoader, mockServer, pdf, pluginDirectory, new File(pluginDirectory, "testPluginFile"));
        
        when(mockPluginManager.getPlugin("ExilePearl")).thenReturn(plugin);
        when(mockPluginManager.getPlugins()).thenReturn(new JavaPlugin[] { plugin } );
        
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
        
        PluginStorage storage =  spy(new AsyncStorageWriter(new MySqlStorage(pearlFactory, plugin, pearlConfig), plugin));
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

	@Test
	public void test() {
		plugin.onEnable();
	}

}
