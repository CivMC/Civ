package com.devotedmc.ExilePearl.storage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlConfig;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.core.MockPearl;
import com.devotedmc.ExilePearl.core.MockPearlFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Bukkit.class)
public class MySqlStorageIntegrationTest {
	
	private PearlFactory pearlFactory;
	private PearlLogger logger;
	private PearlConfig config;
	private MySqlStorage storage;	
	private World world;

	@Before
	public void setUp() throws Exception {
		
		world = mock(World.class);
		when(world.getName()).thenReturn("world");
		
	    PowerMockito.mockStatic(Bukkit.class);
	    when(Bukkit.getWorld("world")).thenReturn(world);
		
	    // Mock pearl factory for generating mock pearl instances
		pearlFactory = new MockPearlFactory(mock(PlayerProvider.class));
		
		logger = mock(PearlLogger.class);
		
		config = mock(PearlConfig.class);
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
		// Load initial pearl table and verify size is zero
		Collection<ExilePearl> loadedPearls = storage.loadAllPearls();
		assertEquals(loadedPearls.size(), 0);
		
		// Generate a bunch of pearls with a variety of values
		ArrayList<ExilePearl> pearlsToAdd = new ArrayList<ExilePearl>();
		Random rand = new Random(587);
		final int numPearlsToAdd = 100;
		for(int i = 0; i < numPearlsToAdd; i++) {
			ExilePearl toAdd = new MockPearl(mock(PlayerProvider.class), UUID.randomUUID(), "Killer" + i, 100000 + i, new Location(world, rand.nextInt(), rand.nextInt(), rand.nextInt()));
			toAdd.setPearledOn(new Date());
			toAdd.setHealth(rand.nextInt(100));
			
			if (i % 3 == 0) {
				toAdd.setFreedOffline(true);
			}
			
			pearlsToAdd.add(toAdd);
		}
		
		// Insert all the generated pearls to the database
		for(ExilePearl p : pearlsToAdd) {
			storage.pearlInsert(p);
		}
		
		// Perform a load operation of all pearls and verify the size is correct
		loadedPearls = storage.loadAllPearls();
		assertEquals(numPearlsToAdd, loadedPearls.size());
		
		// Verify that all the generated pearls exist in the loaded pearl collection
		for(ExilePearl p : pearlsToAdd) {
			assertTrue(loadedPearls.contains(p));
		}
		
		// Now we pick a pearl to update.
		// Verify that it exists in the collection
		ExilePearl updatePearl = pearlsToAdd.get(0);
		assertTrue(loadedPearls.contains(updatePearl));
		
		
		
		// Change the health and verify that it no longer matches any loaded pearls
		updatePearl.setHealth(updatePearl.getHealth() + 1);
		assertFalse(loadedPearls.contains(updatePearl));
		
		// Even after reloading, it does not match
		loadedPearls = storage.loadAllPearls();
		assertFalse(loadedPearls.contains(updatePearl));
		
		// Perform the update and verify it now exists
		storage.pearlUpdateHealth(updatePearl);
		loadedPearls = storage.loadAllPearls();
		assertTrue(loadedPearls.contains(updatePearl));
		

		
		
		// Change the location and verify that it no longer matches any loaded pearls
		Location l = new Location(world, rand.nextInt(), rand.nextInt(), rand.nextInt());
		Item item = mock(Item.class);
		when(item.getLocation()).thenReturn(l);
		updatePearl.setHolder(item);
		assertFalse(loadedPearls.contains(updatePearl));
		
		// Even after reloading, it does not match
		loadedPearls = storage.loadAllPearls();
		assertFalse(loadedPearls.contains(updatePearl));
		
		// Perform the update and verify it now exists
		storage.pearlUpdateLocation(updatePearl);
		loadedPearls = storage.loadAllPearls();
		assertTrue(loadedPearls.contains(updatePearl));
		
		
		
		// Change the freed offline value and verify that it no longer matches any loaded pearls
		updatePearl.setFreedOffline(!updatePearl.getFreedOffline());
		assertFalse(loadedPearls.contains(updatePearl));
		
		// Even after reloading, it does not match
		loadedPearls = storage.loadAllPearls();
		assertFalse(loadedPearls.contains(updatePearl));
		
		// Perform the update and verify it now exists
		storage.pearlUpdateFreedOffline(updatePearl);
		loadedPearls = storage.loadAllPearls();
		assertTrue(loadedPearls.contains(updatePearl));
	}
}
