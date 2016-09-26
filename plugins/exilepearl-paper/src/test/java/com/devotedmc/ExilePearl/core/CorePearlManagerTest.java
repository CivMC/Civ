package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.NullArgumentException;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExilePearlConfig;
import com.devotedmc.ExilePearl.PearlFactory;
import com.devotedmc.ExilePearl.storage.PearlStorage;

public class CorePearlManagerTest {
	
	private ExilePearlApi pearlApi;
	private PearlFactory pearlFactory;
	private PearlStorage storage;
	private ExilePearlConfig config;
	private CorePearlManager manager;
	
	
	@Before
	public void setUp() throws Exception {
		pearlApi = mock(ExilePearlApi.class);
		pearlFactory = mock(PearlFactory.class);
		storage = mock(PearlStorage.class);
		config = mock(ExilePearlConfig.class);
		manager = new CorePearlManager(pearlApi, pearlFactory, storage, config);
	}

	@Test
	public void testCorePearlManager() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new CorePearlManager(null, pearlFactory, storage, config); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CorePearlManager(pearlApi, null, storage, config); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CorePearlManager(pearlApi, pearlFactory, null, config); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new CorePearlManager(pearlApi, pearlFactory, storage, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testLoadPearls() {		
		Collection<ExilePearl> pearls = manager.getPearls();
		assertEquals(pearls.size(), 0);
		
		// Collection should be unmodifiable
		Throwable e = null;
		try { pearls.clear(); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof UnsupportedOperationException);
		
		Collection<ExilePearl> pearlsToLoad = new HashSet<ExilePearl>();
		when(storage.loadAllPearls()).thenReturn(pearlsToLoad);
		
		manager.loadPearls();
		verify(storage).loadAllPearls();
		assertEquals(pearls.size(), 0);
		
		pearlsToLoad.add(mock(ExilePearl.class));

		manager.loadPearls();
		pearls = manager.getPearls();
		assertEquals(pearls.size(), 1);
	}

	@Test
	public void testExilePlayer() {
		fail("Not yet implemented");
	}

	@Test
	public void testFreePearl() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPearlString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPearlUUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInventoryExilePearls() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetInventoryPearlStacks() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsPlayerExiledPlayer() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsPlayerExiledUUID() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPearlFromItemStack() {
		fail("Not yet implemented");
	}

	@Test
	public void testDecayPearls() {
		fail("Not yet implemented");
	}

}
