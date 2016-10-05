package com.devotedmc.ExilePearl.Util;

import org.bukkit.Server;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 * Test classes should inherit from this if they reference any of the static Bukkit methods
 * @author Gordon
 */
@Ignore
public class BukkitTestCase {
	
	private static Server testBukkit = null;

    @BeforeClass
    public static void setUpTestBukkit() {
    	if (testBukkit == null) {
    		testBukkit = TestBukkit.create();
    	}
    }
    
    public static Server getServer() {
    	return testBukkit;
    }
}
