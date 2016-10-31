package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.devotedmc.ExilePearl.config.Document;
import com.devotedmc.testbukkit.TestBukkit;
import com.devotedmc.testbukkit.TestBukkitRunner;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.TagManager;

@RunWith(TestBukkitRunner.class)
public class ExilePearlCoreTest {
	
	private Document configDoc; 
	private FileConfiguration fileConfig;
	private Plugin plugin;
	private PluginManager pMan;
	private BukkitScheduler scheduler;
	private ExilePearlCore core;

	@Before
	public void setUp() throws Exception {
		configDoc = new Document();
		configDoc.append("storage.type", 2); // use RAM storage
		
		// Hack into the configuration
		fileConfig = mock(FileConfiguration.class);
		when(fileConfig.getValues(false)).thenReturn(configDoc);
		
		plugin = mock(Plugin.class);
		when(plugin.getConfig()).thenReturn(fileConfig);
		when(plugin.getLogger()).thenReturn(mock(Logger.class));
		when(plugin.getServer()).thenReturn(TestBukkit.getServer());
		
		core = new ExilePearlCore(plugin);
		
		pMan = TestBukkit.getServer().getPluginManager();
		scheduler = TestBukkit.getServer().getScheduler();
	}
	
	@Test
	public void testExilePearlCore() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new ExilePearlCore(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testListenerHooks() throws Exception {
		core.onEnable();
		
		Field f = core.getClass().getDeclaredField("playerListener");
		f.setAccessible(true);
		Listener playerListener = (Listener) f.get(core);
		
		f = core.getClass().getDeclaredField("exileListener");
		f.setAccessible(true);
		Listener exileListener = (Listener) f.get(core);
		
		f = core.getClass().getDeclaredField("borderHandler");
		f.setAccessible(true);
		Listener borderHandler = (Listener) f.get(core);
		
		f = core.getClass().getDeclaredField("suicideHandler");
		f.setAccessible(true);
		Listener suicideHandler = (Listener) f.get(core);
		
		f = core.getClass().getDeclaredField("damageLogger");
		f.setAccessible(true);
		Listener damageLogger = (Listener) f.get(core);
		
		verify(pMan, times(1)).registerEvents(playerListener, core);
		verify(pMan, times(1)).registerEvents(exileListener, core);
		verify(pMan, times(1)).registerEvents(borderHandler, core);
		verify(pMan, times(1)).registerEvents(suicideHandler, core);
		verify(pMan, times(1)).registerEvents(damageLogger, core);
		
		core.onDisable();
	}
	
	@Test
	public void testCitadelHook() throws Exception {
		core.onEnable();
		
		Field f = core.getClass().getDeclaredField("citadelListener");
		f.setAccessible(true);
		Listener listener = (Listener) f.get(core);
		
		verify(pMan, times(0)).registerEvents(listener, core);
		
		when(pMan.isPluginEnabled("Citadel")).thenReturn(true);
		core.onEnable();
		verify(pMan, times(1)).registerEvents(listener, core);
	}
	
	@Test
	public void testCivChatHook() throws Exception {
		core.onEnable();
		
		Field f = core.getClass().getDeclaredField("chatListener");
		f.setAccessible(true);
		Listener listener = (Listener) f.get(core);
		
		verify(pMan, times(0)).registerEvents(listener, core);
		
		when(pMan.isPluginEnabled("CivChat2")).thenReturn(true);
		core.onEnable();
		verify(pMan, times(1)).registerEvents(listener, core);
	}
	
	@Test
	public void testBastionHook() throws Exception {
		core.onEnable();
		
		Field f = core.getClass().getDeclaredField("bastionListener");
		f.setAccessible(true);
		Listener listener = (Listener) f.get(core);
		
		verify(pMan, times(0)).registerEvents(listener, core);
		
		when(pMan.isPluginEnabled("Bastion")).thenReturn(true);
		core.onEnable();
		verify(pMan, times(1)).registerEvents(listener, core);
	}
	
	@Test
	public void testJukeAlertHook() throws Exception {
		core.onEnable();
		
		Field f = core.getClass().getDeclaredField("jukeAlertListener");
		f.setAccessible(true);
		Listener listener = (Listener) f.get(core);
		
		verify(pMan, times(0)).registerEvents(listener, core);
		
		when(pMan.isPluginEnabled("JukeAlert")).thenReturn(true);
		core.onEnable();
		verify(pMan, times(1)).registerEvents(listener, core);
	}
	
	@Test
	public void testRandomSpawnHook() throws Exception {
		core.onEnable();
		
		Field f = core.getClass().getDeclaredField("randomSpawnListener");
		f.setAccessible(true);
		Listener listener = (Listener) f.get(core);
		
		verify(pMan, times(0)).registerEvents(listener, core);
		
		when(pMan.isPluginEnabled("RandomSpawn")).thenReturn(true);
		core.onEnable();
		verify(pMan, times(1)).registerEvents(listener, core);
	}
	
	@Test
	public void testWorldBorderHook() throws Exception {
		core.onEnable();
		
		Field f = core.getClass().getDeclaredField("worldBorderListener");
		f.setAccessible(true);
		Listener listener = (Listener) f.get(core);
		
		verify(pMan, times(0)).registerEvents(listener, core);
		
		when(pMan.isPluginEnabled("WorldBorder")).thenReturn(true);
		core.onEnable();
		verify(pMan, times(1)).registerEvents(listener, core);
	}
	
	@Ignore
	@Test
	public void testCombatTagHook() throws Exception {
		core.onEnable();
		
		Field f = core.getClass().getDeclaredField("tagManager");
		f.setAccessible(true);
		TagManager tagManager = (TagManager) f.get(core);
		
		assertNull(tagManager);
		assertFalse(core.isCombatTagEnabled());
		
		CombatTagPlus mockCombatPlugin = new CombatTagPlus();
		TagManager mockCombatManager = mock(TagManager.class);
		when(mockCombatPlugin.getTagManager()).thenReturn(mockCombatManager);
		when(pMan.getPlugin("CombatTagPlus")).thenReturn(mockCombatPlugin);
		
		core.onEnable();
		tagManager = (TagManager) f.get(core);
		assertEquals(mockCombatManager, tagManager);
		assertTrue(core.isCombatTagEnabled());
	}
	
	@Test
	public void testStartTasks() throws Exception {
		Field f = core.getClass().getDeclaredField("pearlDecayWorker");
		f.setAccessible(true);
		ExilePearlTask pearlDecayWorker = (ExilePearlTask) f.get(core);
		
		f = core.getClass().getDeclaredField("borderHandler");
		f.setAccessible(true);
		ExilePearlTask borderHandler = (ExilePearlTask) f.get(core);
		
		f = core.getClass().getDeclaredField("suicideHandler");
		f.setAccessible(true);
		ExilePearlTask suicideHandler = (ExilePearlTask) f.get(core);
		
		f = core.getClass().getDeclaredField("damageLogger");
		f.setAccessible(true);
		ExilePearlTask damageLogger = (ExilePearlTask) f.get(core);
		
		configDoc.append("damage_log.enabled", false);

		core.onEnable();
		verify(scheduler, times(1)).scheduleSyncRepeatingTask(core, pearlDecayWorker, pearlDecayWorker.getTickInterval(), pearlDecayWorker.getTickInterval());
		verify(scheduler, times(1)).scheduleSyncRepeatingTask(core, borderHandler, borderHandler.getTickInterval(), borderHandler.getTickInterval());
		verify(scheduler, times(1)).scheduleSyncRepeatingTask(core, suicideHandler, suicideHandler.getTickInterval(), suicideHandler.getTickInterval());
		verify(scheduler, times(0)).scheduleSyncRepeatingTask(core, damageLogger, damageLogger.getTickInterval(), damageLogger.getTickInterval());
		
		configDoc.append("damage_log.enabled", true);
		core.onEnable();
		verify(scheduler, times(1)).scheduleSyncRepeatingTask(core, damageLogger, damageLogger.getTickInterval(), damageLogger.getTickInterval());
	}
}
