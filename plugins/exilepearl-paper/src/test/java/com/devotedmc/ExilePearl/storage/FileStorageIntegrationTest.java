package com.devotedmc.ExilePearl.storage;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.Util.MockPearlLogger;
import com.devotedmc.ExilePearl.Util.TestBukkit;
import com.devotedmc.ExilePearl.core.MockPearl;
import com.devotedmc.ExilePearl.config.Document;

public class FileStorageIntegrationTest {

	private static File testDir = new File("bin/test/server/plugins/ExilePearl");
	private static File file = new File("bin/test/server/plugins/ExilePearl/pearls.yml");
	private static PearlFactory pearlFactory;
	private static MockPearlLogger logger;
	private static FileStorage storage;	
	private static World world;

	@BeforeClass
	public static void setUpClass() throws Exception {
		TestBukkit.create(true);
		logger = new MockPearlLogger(Bukkit.getServer().getLogger());

		world = Bukkit.getWorld("world");

		// Mock pearl factory for generating mock pearl instances
		pearlFactory = mock(PearlFactory.class);
		when(pearlFactory.createExilePearl(any(UUID.class), any(Document.class))).then(new Answer<ExilePearl>() {

			@Override
			public ExilePearl answer(InvocationOnMock invocation) throws Throwable {

				try {
					UUID uid1 = (UUID)invocation.getArguments()[0];
					Document doc = (Document)invocation.getArguments()[1];
					ExilePearl pearl = new MockPearl(mock(PlayerProvider.class), uid1, doc.getUUID("killer_id"), doc.getInteger("pearl_id"), doc.getLocation("location"));
					pearl.setHealth(doc.getInteger("health"));
					pearl.setPearledOn(doc.getDate("pearled_on"));
					pearl.setFreedOffline(doc.getBoolean("freed_offline"));
					return pearl;
					
				} catch (Exception ex) {
					return null;
				}
			}
		});
		
		testDir.mkdirs();
        assertTrue(testDir.exists());
        
        if (file.exists()) {
        	file.delete();
        }
        assertTrue(file.createNewFile());

		storage = new FileStorage(file, pearlFactory, logger);

		assertTrue(storage.isConnected());
		assertTrue(storage.connect());
		assertTrue(storage.isConnected());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		storage.disconnect();
	}

	@Test
	public void testFileStorage() {

		// Null arguments throw exceptions
		Throwable e = null;
		try { new FileStorage(null, pearlFactory, logger); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		e = null;
		try { new FileStorage(file, null, logger); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		e = null;
		try { new FileStorage(file, pearlFactory, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testPearls() {

		// Load initial pearl table and verify size is zero
		Collection<ExilePearl> loadedPearls = storage.loadAllPearls();
		assertEquals(0, loadedPearls.size());

		// Generate a bunch of pearls with a variety of values
		ArrayList<ExilePearl> pearlsToAdd = new ArrayList<ExilePearl>();
		Random rand = new Random(587);
		final int numPearlsToAdd = 100;
		for(int i = 0; i < numPearlsToAdd; i++) {
			ExilePearl toAdd = new MockPearl(mock(PlayerProvider.class), UUID.randomUUID(), UUID.randomUUID(), i, new Location(world, rand.nextInt(), rand.nextInt(), rand.nextInt()));
			toAdd.setPearledOn(new Date());
			toAdd.setHealth(rand.nextInt(100));

			if (i % 3 == 0) {
				toAdd.setFreedOffline(true);
			}

			pearlsToAdd.add(toAdd);
		}

		logger.log("Adding %d exile pearls to the file.", numPearlsToAdd);

		// Insert all the generated pearls to the database
		for(ExilePearl p : pearlsToAdd) {
			storage.pearlInsert(p);
		}

		logger.log("Loading all pearls from the file.");

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
