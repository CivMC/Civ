package com.devotedmc.ExilePearl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.devotedmc.testbukkit.TestServer;

@Ignore
public class ExilePearlPluginIntegrationTest {
	
	private static TestServer server;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		server = new TestServer(true);
		
		server.addPlugin("CombatTagPlus", "1.2.4", "net.minelink.ctplus.CombatTagPlus");
		server.loadPlugins();
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
	}
}
