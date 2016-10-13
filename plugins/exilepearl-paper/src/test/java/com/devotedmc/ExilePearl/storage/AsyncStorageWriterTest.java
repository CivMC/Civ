package com.devotedmc.ExilePearl.storage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.nio.channels.NotYetConnectedException;

import org.apache.commons.lang.NullArgumentException;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.PearlLogger;

public class AsyncStorageWriterTest {
	
	private PluginStorage storage;
	private PearlLogger logger;
	private AsyncStorageWriter writer;
	private ExilePearl pearl;
	
	
	@Before
	public void setUp() throws Exception {
		storage = mock(PluginStorage.class);
		logger = mock(PearlLogger.class);
		writer = new AsyncStorageWriter(storage, logger);
		pearl = mock(ExilePearl.class);
	}

	@Test
	public void testAsyncStorageWriter() {
		
		// Null arguments throw exceptions
		Throwable e = null;
		try { new AsyncStorageWriter(null, logger); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new AsyncStorageWriter(storage, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void testConnectDisconnect() throws Exception {
		assertFalse(writer.isConnected());
		
		when(storage.connect()).thenReturn(false);
		assertFalse(writer.connect());
		verify(storage).connect();
		
		Throwable e = null;
		try { writer.pearlInsert(pearl); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NotYetConnectedException);
		
		when(storage.connect()).thenReturn(true);
		assertTrue(writer.connect());
		when(storage.isConnected()).thenReturn(true);
		assertTrue(writer.isConnected());
		
		e = null;
		try { writer.pearlInsert(pearl); } catch (Throwable ex) { e = ex; }
		assertNull(e);
		
		verify(storage, timeout(5000)).pearlInsert(pearl);;
		
		writer.disconnect();
		assertFalse(writer.isConnected());
		verify(storage).disconnect();
		
		e = null;
		try { writer.pearlInsert(pearl); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NotYetConnectedException);
	}

	@Test
	public void testLoadAllPearls() throws Exception {
		when(storage.connect()).thenReturn(true);
		when(storage.isConnected()).thenReturn(true);
		assertTrue(writer.connect());
		
		writer.loadAllPearls();
		verify(storage).loadAllPearls();
	}

	@Test
	public void testPearlInsert() throws Exception {
		when(storage.connect()).thenReturn(true);
		when(storage.isConnected()).thenReturn(true);
		assertTrue(writer.connect());
		
		writer.pearlInsert(pearl);
		verify(storage, timeout(5000)).pearlInsert(pearl);
	}

	@Test
	public void testPearlRemove() throws Exception {
		when(storage.connect()).thenReturn(true);
		when(storage.isConnected()).thenReturn(true);
		assertTrue(writer.connect());
		
		writer.pearlRemove(pearl);
		verify(storage, timeout(5000)).pearlRemove(pearl);
	}

	@Test
	public void testPearlUpdateLocation() throws Exception {
		when(storage.connect()).thenReturn(true);
		when(storage.isConnected()).thenReturn(true);
		assertTrue(writer.connect());
		
		writer.updatePearlLocation(pearl);
		verify(storage, timeout(5000)).updatePearlLocation(pearl);
	}

	@Test
	public void testPearlUpdateHealth() throws Exception {
		when(storage.connect()).thenReturn(true);
		when(storage.isConnected()).thenReturn(true);
		assertTrue(writer.connect());
		
		writer.updatePearlHealth(pearl);
		verify(storage, timeout(5000)).updatePearlHealth(pearl);
	}

	@Test
	public void testPearlUpdateFreedOffline() throws Exception {
		when(storage.connect()).thenReturn(true);
		when(storage.isConnected()).thenReturn(true);
		assertTrue(writer.connect());
		
		writer.updatePearlFreedOffline(pearl);
		verify(storage, timeout(5000)).updatePearlFreedOffline(pearl);
	}
}
