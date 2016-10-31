package com.devotedmc.ExilePearl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static com.devotedmc.testbukkit.TestBukkit.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.devotedmc.testbukkit.TestBukkitRunner;
import com.devotedmc.testbukkit.TestOptions;
import com.devotedmc.testbukkit.TestPlayer;
import com.devotedmc.testbukkit.TestPlugin;
import com.devotedmc.testbukkit.TestServer;
import com.devotedmc.testbukkit.v1_10_R1.TestServer_v1_10_R1;

import vg.civcraft.mc.civmodcore.util.TextUtil;

@SuppressWarnings("unused")
@RunWith(TestBukkitRunner.class)
@TestOptions(useLogger = true, server = TestServer_v1_10_R1.class)
public class ExilePearlPluginIntegrationTest {
	
	private static TestServer server;
	private static TestPlugin<ExilePearlPlugin> testPlugin;
	
	private TestPlayer player1;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		server = getServer();
		
		// Use MySQL for the integration test
		testPlugin = server.addPlugin(ExilePearlPlugin.class)
			.appendConfig("storage.type", 1) // MySQL type
			.appendConfig("storage.mysql.host", "localhost")
			.appendConfig("storage.mysql.dbname", "exilepearl")
			.appendConfig("storage.mysql.username", "bukkit");
		
		server.loadPlugins();
		server.enablePlugins();
		
		ExilePearlPlugin plugin = testPlugin.getInstance();
		
		
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		server.disablePlugins();
	}
	
	@Before
	public void setUp() throws Exception {
		server.getOnlinePlayers().clear();
		player1 = createOnlinePlayer("player1");
	}

	@Ignore
	@Test
	public void testConsoleCommands() {
		assertTrue(consoleCommand("ep"));
		
		assertTrue(consoleCommand("ep reload"));
		assertTrue(consoleCommand("ep check"));
		assertTrue(consoleCommand("ep decay"));
		assertTrue(consoleCommand("ep exileany"));
		assertTrue(consoleCommand("ep freeany"));
		assertTrue(consoleCommand("ep list"));
		assertTrue(consoleCommand("ep sethealth"));
		assertTrue(consoleCommand("ep setkiller"));
		assertTrue(consoleCommand("ep settype"));
		
		assertTrue(consoleCommand("ep config"));
		assertTrue(consoleCommand("ep config list"));
		assertTrue(consoleCommand("ep config load"));
		assertTrue(consoleCommand("ep config save"));
		assertTrue(consoleCommand("ep config set"));
		
		assertTrue(consoleCommand("ep broadcast"));
		assertTrue(consoleCommand("ep confirm"));
		assertTrue(consoleCommand("ep silence"));
		assertTrue(consoleCommand("ep free"));
		assertTrue(consoleCommand("ep locate"));
		
		assertTrue(consoleCommand("suicide"));
	}
	
	/**
	 * The command will always return true because it's being psuedo-executed
	 */
	@Test
	public void testPlayerCommands() {
		assertTrue(player1.runCommand("ep reload"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "reload"));
		
		assertTrue(player1.runCommand("ep check"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "check"));
		
		assertTrue(player1.runCommand("ep decay"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "decay"));
		
		assertTrue(player1.runCommand("ep exileany"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "exileany"));
		
		assertTrue(player1.runCommand("ep freeany"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "freeany"));
		
		assertTrue(player1.runCommand("ep list"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "list"));
		
		assertTrue(player1.runCommand("ep sethealth"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "sethealth"));
		
		assertTrue(player1.runCommand("ep setkiller"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "setkiller"));
		
		assertTrue(player1.runCommand("ep settype"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "settype"));
		
		assertTrue(player1.runCommand("ep config"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "config"));
		
		assertTrue(player1.runCommand("ep config list"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "config list"));
		
		assertTrue(player1.runCommand("ep config load"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "config load"));
		
		assertTrue(player1.runCommand("ep config save"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "config save"));
		
		assertTrue(player1.runCommand("ep config set"));
		verify(player1).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "config set"));
		
		
		// These should pass
		assertTrue(player1.runCommand("ep broadcast"));
		verify(player1, times(0)).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "broadcast"));
		
		assertTrue(player1.runCommand("ep confirm"));
		verify(player1, times(0)).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "broadcast"));
		
		assertTrue(player1.runCommand("ep silence"));
		verify(player1, times(0)).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "broadcast"));
		
		assertTrue(player1.runCommand("ep free"));
		verify(player1, times(0)).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "broadcast"));
		
		assertTrue(player1.runCommand("ep locate"));
		verify(player1, times(0)).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "suicide"));
		
		assertTrue(player1.runCommand("suicide"));
		verify(player1, times(0)).sendMessage(TextUtil.parse(Lang.commandToManyArgs, "broadcast"));
	}
}
