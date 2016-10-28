package com.devotedmc.ExilePearl.storage;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;

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
import com.devotedmc.ExilePearl.config.Document;
import com.devotedmc.ExilePearl.config.MySqlConfig;
import com.devotedmc.ExilePearl.core.MockPearl;
import com.devotedmc.testbukkit.TestServer;

import vg.civcraft.mc.civmodcore.dao.ConnectionPool;

public class MySqlStorageIntegrationTest {

	private static PearlFactory pearlFactory;
	private static MockPearlLogger logger;
	private static MySqlConfig config;
	private static MySqlStorage storage;	
	private static World world;

	private static ConnectionPool db;

	@BeforeClass
	public static void setUpClass() throws Exception {
		new TestServer(true);
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

		config = mock(MySqlConfig.class);
		when(config.getMySqlHost()).thenReturn("localhost");
		when(config.getMySqlDatabaseName()).thenReturn("exilepearltest");
		when(config.getMySqlPort()).thenReturn(3306);
		when(config.getMySqlUsername()).thenReturn("bukkit");
		when(config.getMySqlPassword()).thenReturn("");
		when(config.getMySqlPoolSize()).thenReturn(5);
		when(config.getMySqlConnectionTimeout()).thenReturn(5000);
		when(config.getMySqlIdleTimeout()).thenReturn(5000);
		when(config.getMySqlMaxLifetime()).thenReturn(5000);


		db = new ConnectionPool(logger.getLogger(), 
				config.getMySqlUsername(), 
				config.getMySqlPassword(), 
				config.getMySqlHost(), 
				config.getMySqlPort(), 
				config.getMySqlDatabaseName(), 
				config.getMySqlPoolSize(), 
				config.getMySqlConnectionTimeout(), 
				config.getMySqlIdleTimeout(), 
				config.getMySqlMaxLifetime());

		try (Connection connection = db.getConnection();) {
			try (PreparedStatement preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS ExilePearlPlugin;"); ) {
				preparedStatement.execute();
			}

			try (PreparedStatement preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS ExilePearls;"); ) {
				preparedStatement.execute();
			}

			try (PreparedStatement preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS PrisonPearls;"); ) {
				preparedStatement.execute();
			}
		}

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
	public void testPearls() throws SQLException {
		
		// Clear out the existing values
		try (Connection connection = db.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ExilePearls;"); ) {
			preparedStatement.execute();
		}

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
		storage.updatePearlHealth(updatePearl);
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
		storage.updatePearlLocation(updatePearl);
		loadedPearls = storage.loadAllPearls();
		assertTrue(loadedPearls.contains(updatePearl));



		// Change the freed offline value and verify that it no longer matches any loaded pearls
		updatePearl.setFreedOffline(!updatePearl.getFreedOffline());
		assertFalse(loadedPearls.contains(updatePearl));

		// Even after reloading, it does not match
		loadedPearls = storage.loadAllPearls();
		assertFalse(loadedPearls.contains(updatePearl));

		// Perform the update and verify it now exists
		storage.updatePearlFreedOffline(updatePearl);
		loadedPearls = storage.loadAllPearls();
		assertTrue(loadedPearls.contains(updatePearl));
	}

	private static String addPearlQuery = "insert into PrisonPearls(uuid, world, server, x, y, z, uq, motd, killer, pearlTime)"
			+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	private static final String createPrisonPearlTable = "create table if not exists PrisonPearls( "
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
			+ "primary key ids_id(uuid));";

	private static void addPrisonPearlData() throws SQLException {

		try (Connection connection = db.getConnection();
				PreparedStatement stmt = connection.prepareStatement(createPrisonPearlTable);) {
			stmt.execute();
		}

		Random rand = new Random(127);
		logger.log("Adding 50 dummy values to PrisonPearl table to test migration");

		for (int i = 0; i < 50; i++) {
			try (Connection connection = db.getConnection();
					PreparedStatement addPearl = connection.prepareStatement(addPearlQuery);) {
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
			} catch(SQLException e) {
				logger.log(Level.WARNING, "Failed to insert dummy pearl data.");
			}
		}

	}
}
