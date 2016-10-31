package com.devotedmc.ExilePearl;

import static org.mockito.Mockito.*;

import java.util.UUID;
import static com.devotedmc.testbukkit.TestBukkit.*;

import org.bukkit.entity.Player;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.devotedmc.testbukkit.TestBukkitRunner;
import com.devotedmc.testbukkit.TestOptions;
import com.devotedmc.testbukkit.TestServer;

@RunWith(TestBukkitRunner.class)
@TestOptions(useLogger = true)
public class ExilePearlPluginIntegrationTest {
	
	private TestServer server;
	
	@Before
	public void setUp() throws Exception {
		server = getServer();
		
		server.addPlugin(ExilePearlPlugin.class)
			.appendConfig("storage.type", 2) // Use RAM storage
			.appendConfig("storage.mysql.host", "localhost")
			.appendConfig("storage.mysql.dbname", "exilepearl")
			.appendConfig("storage.mysql.username", "bukkit")
			.appendConfig("storage.mysql.migrate_pp", "false")
			.appendConfig("storage.mysql.migrate_dbname", "prisonpearl")
		;
		
		server.loadPlugins();
		server.enablePlugins();
	}
	
	@After
	public void tearDown() throws Exception {
		server.disablePlugins();
	}

	@Test
	public void testCommands() {
		Player player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(UUID.randomUUID());
		when(player.getName()).thenReturn("Player");
		runCommand("ep check player");
	}
}
