package com.devotedmc.ExilePearl.storage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlConfig;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.PearlLogger;
import com.devotedmc.ExilePearl.PlayerProvider;
import com.devotedmc.ExilePearl.Util.BukkitTestCase;
import com.devotedmc.ExilePearl.Util.MockPearlLogger;
import com.devotedmc.ExilePearl.Util.TestBukkit;
import com.devotedmc.ExilePearl.core.MockPearl;
import com.devotedmc.ExilePearl.core.MockPearlFactory;

public class MySqlStorageIntegrationTest {
	
	private static PearlFactory pearlFactory;
	private static MockPearlLogger logger;
	private static PearlConfig config;
	private static MySqlStorage storage;	
	private static World world;
	
	private static MySqlConnection db;

	@BeforeClass
	public static void setUp() throws Exception {
		TestBukkit.create(true);
		logger = new MockPearlLogger(Bukkit.getServer().getLogger());
		
		world = Bukkit.getWorld("world");
		
	    // Mock pearl factory for generating mock pearl instances
		pearlFactory = new MockPearlFactory(mock(PlayerProvider.class));
		
		config = mock(PearlConfig.class);
		when(config.getDbHost()).thenReturn("localhost");
		when(config.getDbName()).thenReturn("bukkittest");
		when(config.getDbPort()).thenReturn(3306);
		when(config.getDbUsername()).thenReturn("BukkitTest");
		when(config.getDbPassword()).thenReturn("test");
		
		db = new MySqlConnection(config.getDbHost(), config.getDbPort(), config.getDbName(), config.getDbUsername(), config.getDbPassword(), mock(PearlLogger.class));
		db.connect();
		db.execute("DELETE FROM exilepearlplugin;");
		
		// Add some fake PP data
		addPrisonPearlData();
		
		storage = new MySqlStorage(pearlFactory, logger, config);
		
		assertFalse(storage.isConnected());
		assertTrue(storage.connect());
		assertTrue(storage.isConnected());
		

	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		storage.disconnect();
		db.close();
	}

	@Test
	public void testMySqlStorage() {
		db.execute("DELETE FROM exilepearls;");
		
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
			ExilePearl toAdd = new MockPearl(mock(PlayerProvider.class), UUID.randomUUID(), UUID.randomUUID(), i, new Location(world, rand.nextInt(), rand.nextInt(), rand.nextInt()));
			toAdd.setPearledOn(new Date());
			toAdd.setHealth(rand.nextInt(100));
			
			if (i % 3 == 0) {
				toAdd.setFreedOffline(true);
			}
			
			pearlsToAdd.add(toAdd);
		}
		
		logger.log("Inserting %d exile pearls to the database.", numPearlsToAdd);
		
		// Insert all the generated pearls to the database
		for(ExilePearl p : pearlsToAdd) {
			storage.pearlInsert(p);
		}
		
		logger.log("Loading all pearls from the database.");
		
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
	
	private static String addPearlQuery = "insert into PrisonPearls(uuid, world, server, x, y, z, uq, motd, killer, pearlTime)"
			+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	
	
	private static void addPrisonPearlData() throws SQLException {

		db.execute("DELETE FROM PrisonPearls;");
		
		db.execute("create table if not exists PrisonPearls( "
				+ "uuid varchar(36) not null,"
				+ "world varchar(36) not null,"
				+ "server varchar(255) not null,"
				+ "x int not null,"
				+ "y int not null," 
				+ "z int not null,"
				+ "uq int not null,"
				+ "motd varchar(255)," 
				+ "killer varchar(36)," 
				+ "pearlTime bigint,"
				+ "primary key ids_id(uuid));");

		Random rand = new Random(127);
		logger.log("Adding 50 dummy values to PrisonPearl table to test migration");
		
		for (int i = 0; i < 50; i++) {
			PreparedStatement addPearl = db.prepareStatement(addPearlQuery);
			
			addPearl.setString(1, UUID.randomUUID().toString());
			addPearl.setString(2, world.getName());
			addPearl.setString(3, "bukkit");
			addPearl.setInt(4, rand.nextInt());
			addPearl.setInt(5, rand.nextInt());
			addPearl.setInt(6, rand.nextInt());
			addPearl.setInt(7, rand.nextInt());
			addPearl.setString(8, "motd");
			addPearl.setString(9, UUID.randomUUID().toString());
			addPearl.setLong(10, new Date().getTime());
			addPearl.execute();
		}
		
	}
}
