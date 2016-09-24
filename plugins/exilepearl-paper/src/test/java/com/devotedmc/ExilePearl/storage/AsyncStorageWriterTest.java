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
		try { new AsyncStorageWriter(null, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		try { new AsyncStorageWriter(storage, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		try { new AsyncStorageWriter(null, logger); } catch (Throwable ex) { e = ex; }
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
		
		Thread.sleep(10); // Wait for async writer to execute
		verify(storage).pearlInsert(pearl);;
		
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
		Thread.sleep(10); // Wait for async writer to execute
		verify(storage).pearlInsert(pearl);
	}

	@Test
	public void testPearlRemove() throws Exception {
		when(storage.connect()).thenReturn(true);
		when(storage.isConnected()).thenReturn(true);
		assertTrue(writer.connect());
		
		writer.pearlRemove(pearl);
		Thread.sleep(10); // Wait for async writer to execute
		verify(storage).pearlRemove(pearl);
	}

	@Test
	public void testPearlUpdateLocation() throws Exception {
		when(storage.connect()).thenReturn(true);
		when(storage.isConnected()).thenReturn(true);
		assertTrue(writer.connect());
		
		writer.pearlUpdateLocation(pearl);
		Thread.sleep(10); // Wait for async writer to execute
		verify(storage).pearlUpdateLocation(pearl);
	}

	@Test
	public void testPearlUpdateHealth() throws Exception {
		when(storage.connect()).thenReturn(true);
		when(storage.isConnected()).thenReturn(true);
		assertTrue(writer.connect());
		
		writer.pearlUpdateHealth(pearl);
		Thread.sleep(10); // Wait for async writer to execute
		verify(storage).pearlUpdateHealth(pearl);
	}
}
