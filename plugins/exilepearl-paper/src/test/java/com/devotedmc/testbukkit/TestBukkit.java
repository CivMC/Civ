package com.devotedmc.testbukkit;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

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
     * Creates a new player instance and adds it to the server
     * @param name The player name
     * @param uid The player UUID
     * @return The player instance
     */
    public static TestPlayer createOnlinePlayer(String name, UUID uid) {    	
    	TestPlayer p = TestPlayer.create(name);
    	
    	final InetSocketAddress address = new InetSocketAddress("localhost", 25565);
    	final AsyncPlayerPreLoginEvent preLoginEvent = new AsyncPlayerPreLoginEvent(name, address.getAddress(), uid);
    	getPluginManager().callEvent(preLoginEvent);
    	if (preLoginEvent.getLoginResult() != Result.ALLOWED) {
    		return null;
    	}
    	
    	final PlayerLoginEvent loginEvent = new PlayerLoginEvent(p, "localhost", address.getAddress());
    	getPluginManager().callEvent(loginEvent);

    	if (loginEvent.getResult() != PlayerLoginEvent.Result.ALLOWED) {
    		return null;
    	}
    	
    	final PlayerJoinEvent joinEvent = new PlayerJoinEvent(p, "");
    	getPluginManager().callEvent(joinEvent);
    	
    	p.goOnline();
    	return p;
    }
    

    /**
     * Creates a new player instance and adds it to the server
     * @param name The player name
     * @return The player instance
     */
    public static TestPlayer createOnlinePlayer(String name) {    
    	return createOnlinePlayer(name, UUID.randomUUID());
    }
    
    /**
     * Executes a console command
     * @param commandLine The command
     * @return The command result
     */
    public static boolean consoleCommand(String commandLine) {
    	server.getLogger().log(Level.INFO, String.format("Running console command '%s'", commandLine));
    	return getServer().dispatchCommand(getServer().getConsoleSender(), commandLine);
    }
    
    /**
     * Gets the plugin manager
     * @return The plugin manager
     */
    public static TestPluginManager getPluginManager() {
    	return server.getPluginManager();
    }
    
    
}
