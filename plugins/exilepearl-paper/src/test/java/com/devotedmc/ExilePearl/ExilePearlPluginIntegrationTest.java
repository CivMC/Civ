package com.devotedmc.ExilePearl;

import static org.junit.Assert.*;
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
import com.devotedmc.testbukkit.TestServer;
import com.devotedmc.testbukkit.v1_10_R1.TestServer_v1_10_R1;

@RunWith(TestBukkitRunner.class)
@TestOptions(useLogger = true, server = TestServer_v1_10_R1.class)
public class ExilePearlPluginIntegrationTest {
	
	private static TestServer server;
	
	private TestPlayer player1;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		server = getServer();
		
		// Use MySQL for the integration test
		server.addPlugin(ExilePearlPlugin.class)
			.appendConfig("storage.type", 1) // MySQL type
			.appendConfig("storage.mysql.host", "localhost")
			.appendConfig("storage.mysql.dbname", "exilepearl")
			.appendConfig("storage.mysql.username", "bukkit")
		;
		
		server.loadPlugins();
		server.enablePlugins();
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

	@Ignore
	@Test
	public void testPlayerCommands() {
		assertTrue(player1.runCommand("ep"));
		
		assertFalse(player1.runCommand("ep reload"));
		assertFalse(player1.runCommand("ep check"));
		assertFalse(player1.runCommand("ep decay"));
		assertFalse(player1.runCommand("ep exileany"));
		assertFalse(player1.runCommand("ep freeany"));
		assertFalse(player1.runCommand("ep list"));
		assertFalse(player1.runCommand("ep sethealth"));
		assertFalse(player1.runCommand("ep setkiller"));
		assertFalse(player1.runCommand("ep settype"));
		
		assertFalse(player1.runCommand("ep config"));
		assertFalse(player1.runCommand("ep config list"));
		assertFalse(player1.runCommand("ep config load"));
		assertFalse(player1.runCommand("ep config save"));
		assertFalse(player1.runCommand("ep config set"));
		
		assertTrue(player1.runCommand("ep broadcast"));
		assertTrue(player1.runCommand("ep confirm"));
		assertTrue(player1.runCommand("ep silence"));
		assertTrue(player1.runCommand("ep free"));
		assertTrue(player1.runCommand("ep locate"));
		
		assertTrue(player1.runCommand("suicide"));
	}
}
