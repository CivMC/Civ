package com.devotedmc.ExilePearl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ExilePearlPluginTest {
	
	private ExilePearlPlugin plugin;

	@Before
	public void setUp() throws Exception {
		plugin = mock(ExilePearlPlugin.class, Mockito.CALLS_REAL_METHODS);
	}

	@Test
	public void testGetPluginName() {
		assertEquals(plugin.getPluginName(), "ExilePearl");
	}
}
