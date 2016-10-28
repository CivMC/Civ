package com.devotedmc.testbukkit;

import java.util.logging.Level;

import org.bukkit.entity.Player;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import com.devotedmc.testbukkit.v1_10_R1.TestServer_v1_10_R1;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

public class TestBukkitRunner extends BlockJUnit4ClassRunner {
	
	private static TestServer testServer = null;

	public TestBukkitRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
		
		rewireJavaPlugin();
		
    	if (testServer == null) {
    		testServer = new TestServer_v1_10_R1(true);
    	}
	}

	
	/**
	 * Modifies the code for JavaPlugin so that it can actually 
	 * be invoked outside of a Bukkit instance
	 * @throws InitializationError
	 */
    private void rewireJavaPlugin() throws InitializationError {
    	try {
	    	ClassPool cp = new ClassPool(true);
	    	CtClass ctJavaPlugin = cp.get("org.bukkit.plugin.java.JavaPlugin");
	    	
	    	// Change the default constructor
	    	CtConstructor constructor = ctJavaPlugin.getConstructors()[0];
	    	constructor.setBody("{"
	    			+ "com.devotedmc.testbukkit.TestServer server = com.devotedmc.testbukkit.TestBukkitRunner.getServer();"
	    			+ "this.server = server;"
	    			+ "this.loader = server.getPluginLoader();"
	    			+ "this.classLoader = getClass().getClassLoader();"
	    			+ "this.description = server.getDescription(this);"
	    			+ "this.dataFolder = server.getTestPluginDataFolder(this);"
	    			+ "this.file = server.getTestPluginFile(this);"
	    			+ "this.configFile = server.getTestPluginConfigFile(this);"
	    			+ "this.logger = new org.bukkit.plugin.PluginLogger(this);"
	    			+ "}");
	    	
	    	// Don't save anything to disk
	    	CtMethod ctSaveResource = ctJavaPlugin.getDeclaredMethod("saveResource");
	    	ctSaveResource.setBody("{ }");
	    	
	    	CtMethod ctSaveConfig = ctJavaPlugin.getDeclaredMethod("saveConfig");
	    	ctSaveConfig.setBody("{ }");
	    	
	    	CtMethod ctSaveDefaultConfig = ctJavaPlugin.getDeclaredMethod("saveDefaultConfig");
	    	ctSaveDefaultConfig.setBody("{ }");
	    	
	    	// Configuration gets pulled from the test server
	    	CtMethod ctReloadConfig = ctJavaPlugin.getDeclaredMethod("reloadConfig");
	    	ctReloadConfig.setBody("{ newConfig = com.devotedmc.testbukkit.TestBukkitRunner.getServer().getPluginConfig(this); }");
	    	
	    	// Compile
	    	ctJavaPlugin.toClass();
	    	
    	} catch (Exception ex) {
    		throw new InitializationError(ex);
    	}
    }
    
    public static TestServer getServer() {
    	return testServer;
    }
    
    public static void addPlayer(Player p) {
    	testServer.addPlayer(p);
    }
    
    public static void runCommand(String commandLine) {
    	testServer.getLogger().log(Level.INFO, String.format("Running console command '%s'", commandLine));
    	getServer().dispatchCommand(getServer().getConsoleSender(), commandLine);
    }
}
