package com.devotedmc.ExilePearl;

import static com.devotedmc.testbukkit.TestBukkitRunner.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.devotedmc.testbukkit.TestBukkitRunner;
import com.devotedmc.testbukkit.TestServer;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;

@RunWith(TestBukkitRunner.class)
public class ExilePearlPluginIntegrationTest {
	
	private TestServer server;
	
	@Before
	public void setUp() throws Exception {
		server = getServer();
		
		server.addPlugin(CivModCorePlugin.class);
		
		server.addPlugin(ExilePearlPlugin.class)
			.appendConfig("storage.type", 2); // Use RAM storage
		
		server.loadPlugins();
		server.enablePlugins();
	}

	@Test
	public void test() {
	}
}
