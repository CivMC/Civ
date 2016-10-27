package com.devotedmc.ExilePearl.Util;

import org.bukkit.entity.Player;
import org.junit.BeforeClass;
import org.junit.Ignore;

import com.devotedmc.testbukkit.TestServer;

/**
 * Test classes should inherit from this if they reference any of the static Bukkit methods
 * @author Gordon
 */
@Ignore
public class BukkitTestCase {
	
	private static TestServer testServer = null;

    @BeforeClass
    public static void setUpTestBukkit() {
    	if (testServer == null) {
    		testServer = new TestServer(false);
    	}
    }
    
    public static TestServer getServer() {
    	return testServer;
    }
    
    public void addPlayer(Player p) {
    	testServer.addPlayer(p);
    }
    
}
