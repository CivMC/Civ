package com.devotedmc.testbukkit;

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
	    	
	    	CtMethod ctReloadConfig = ctJavaPlugin.getDeclaredMethod("reloadConfig");
	    	ctReloadConfig.setBody("{ newConfig = com.devotedmc.testbukkit.TestBukkitRunner.getServer().getPluginConfig(this); }");
	    	
	    	ctJavaPlugin.toClass();
	    	
	    	// Change the YamlConfiguration to just load a blank instance
	    	CtClass ctYaml = cp.get("org.bukkit.configuration.file.YamlConfiguration");
	    	CtMethod ctLoadConfiguration = ctYaml.getMethod("loadConfiguration", "(Ljava/io/File;)Lorg/bukkit/configuration/file/YamlConfiguration;");
	    	ctLoadConfiguration.setBody("{ return new org.bukkit.configuration.file.YamlConfiguration(); }");
	    	ctYaml.toClass();
	    	
    	} catch (Exception ex) {
    		throw new InitializationError(ex);
    	}
    }
    
    public static TestServer getServer() {
    	return testServer;
    }
    
    public void addPlayer(Player p) {
    	testServer.addPlayer(p);
    }
}
