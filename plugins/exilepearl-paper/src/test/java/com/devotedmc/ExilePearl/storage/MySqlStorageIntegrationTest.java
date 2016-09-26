package com.devotedmc.ExilePearl.storage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlConfig;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLogger;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bukkit.class)
public class MySqlStorageIntegrationTest {
	
	private PearlFactory pearlFactory;
	private PearlLogger logger;
	private ExilePearlConfig config;
	private MySqlStorage storage;
	
	ExilePearl pearl1;
	ExilePearl pearl2;
	
	UUID p1Id = UUID.randomUUID();
	String p1Name = "Player1";
	UUID p2Id = UUID.randomUUID();
	String p2Name = "Player2";

	@Before
	public void setUp() throws Exception {
		
		World world = mock(World.class);
		when(world.getName()).thenReturn("world");
		
	    PowerMockito.mockStatic(Bukkit.class);
	    when(Bukkit.getWorld("world")).thenReturn(world);
		
		pearlFactory = mock(PearlFactory.class);
		pearl1 = mock(ExilePearl.class);
		when(pearl1.getUniqueId()).thenReturn(UUID.randomUUID());
		when(pearl1.getKillerUniqueId()).thenReturn(UUID.randomUUID());
		
		when(pearl1.getLocation()).thenReturn(new Location(world, 1, 2, 3));
		when(pearl1.getHealth()).thenReturn(10.0);
		when(pearl1.getPearledOn()).thenReturn(new Date());
		when(pearlFactory.createExilePearl(any(UUID.class), any(UUID.class), any(Location.class), anyDouble())).thenAnswer(new Answer<ExilePearl>() {

			@Override
			public ExilePearl answer(InvocationOnMock invocation) throws Throwable {
				UUID playerId;
				UUID killerId;
				Location loc;
				double health;
				
				try {
					playerId = (UUID)invocation.getArguments()[0];
					killerId = (UUID)invocation.getArguments()[1];
					loc = (Location)invocation.getArguments()[2];
					health = (double)invocation.getArguments()[3];
				} catch(Exception ex) {
					return null;
				}
				
				ExilePearl pearl = mock(ExilePearl.class);
				when(pearl.getUniqueId()).thenReturn(playerId);
				when(pearl.getKillerUniqueId()).thenReturn(killerId);
				when(pearl.getLocation()).thenReturn(loc);
				when(pearl.getHealth()).thenReturn(health);
				
				return null;
			}
		});
		
		logger = mock(PearlLogger.class);
		
		config = mock(ExilePearlConfig.class);
		when(config.getDbHost()).thenReturn("localhost");
		when(config.getDbName()).thenReturn("bukkittest");
		when(config.getDbPort()).thenReturn(3306);
		when(config.getDbUsername()).thenReturn("BukkitTest");
		when(config.getDbPassword()).thenReturn("test");
		
		storage = new MySqlStorage(pearlFactory, logger, config);
		
		assertFalse(storage.isConnected());
		assertTrue(storage.connect());
		assertTrue(storage.isConnected());
		
		// Clear the pearl table
		storage.deleteAllPearls();
	}
	
	@After
	public void tearDown() throws Exception {
		storage.disconnect();
	}

	@Test
	public void testMySqlStorage() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new MySqlStorage(null, logger, config); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new MySqlStorage(pearlFactory, null, config); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new MySqlStorage(pearlFactory, logger, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testPearls() {
		Collection<ExilePearl> pearls = storage.loadAllPearls();
		assertEquals(pearls.size(), 0);
		
		storage.pearlInsert(pearl1);
		
		pearls = storage.loadAllPearls();
		assertEquals(pearls.size(), 1);		
		assertTrue(pearls.contains(pearl1));
	}
}
