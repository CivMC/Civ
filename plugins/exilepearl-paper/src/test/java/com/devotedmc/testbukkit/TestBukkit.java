package com.devotedmc.testbukkit;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Server;

public final class TestBukkit {
    private static TestServer server;

    /**
     * Static class cannot be initialized.
     */
    private TestBukkit() { }

    /**
     * Gets the current {@link TestServer} singleton
     *
     * @return TestServer instance being ran
     */
    public static TestServer getServer() {
        return server;
    }
    
    /**
     * Attempts to set the {@link Server} singleton.
     * <p>
     * This cannot be done if the Server is already set.
     *
     * @param server Server instance
     */
    public static void setServer(TestServer server) {
        Bukkit.setServer(server);
        TestBukkit.server = server;
    }
    
    /**
     * Adds a test player to the server
     * @param p The test player
     */
    public static void addPlayer(TestPlayer p) {
    	server.addPlayer(p);
    }
    
    /**
     * Adds a test player to the server
     * @param p The test player
     */
    public static TestPlayer createOnlinePlayer(String name) {    	
    	TestPlayer p = TestPlayer.create(name);
    	p.goOnline();
    	return p;
    }
    
    public static boolean consoleCommand(String commandLine) {
    	server.getLogger().log(Level.INFO, String.format("Running console command '%s'", commandLine));
    	return getServer().dispatchCommand(getServer().getConsoleSender(), commandLine);
    }
}
