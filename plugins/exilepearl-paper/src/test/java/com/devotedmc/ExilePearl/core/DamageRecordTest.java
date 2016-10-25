package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.util.Clock;

public class DamageRecordTest {
	
	private UUID uid = UUID.randomUUID();
	private Clock clock;
	private DamageRecord dut;

	@Before
	public void setUp() throws Exception {
		clock = mock(Clock.class);
		when(clock.getCurrentTime()).thenReturn(0L);
		
		dut = new DamageRecord(clock, uid);
		assertEquals(0, dut.getAmount(), 0);
		assertEquals(0, dut.getTime());
	}
	
	@Test
	public void testConstructor() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new DamageRecord(null, uid); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		e = null;
		try { new DamageRecord(clock, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}
	

	@Test
	public void testAmountTime() {
		assertEquals(uid, dut.getDamager());
		assertEquals(0, dut.getAmount(), 0);
		assertEquals(0, dut.getTime());
		
		dut.setAmount(5.1);
		assertEquals(5.1, dut.getAmount(), 0);
		assertEquals(0, dut.getTime());
		
		dut.setAmount(6.2);
		assertEquals(6.2, dut.getAmount(), 0);
		assertEquals(0, dut.getTime());
		
		dut.setTime(10);
		assertEquals(10, dut.getTime());
		assertEquals(6.2, dut.getAmount(), 0);
		
		dut.setTime(20);
		assertEquals(20, dut.getTime());
		assertEquals(6.2, dut.getAmount(), 0);
	}
	
	@Test
	public void testRecordDecayDamage() {
		when(clock.getCurrentTime()).thenReturn(50L);
		dut.recordDamage(5, 10);
		assertEquals(50, dut.getTime());
		assertEquals(5, dut.getAmount(), 0);

		when(clock.getCurrentTime()).thenReturn(51L);
		dut.recordDamage(5, 10);
		assertEquals(51, dut.getTime());
		assertEquals(10, dut.getAmount(), 0);

		when(clock.getCurrentTime()).thenReturn(52L);
		dut.recordDamage(5, 10);
		assertEquals(52, dut.getTime());
		assertEquals(10, dut.getAmount(), 0);
		
		assertTrue(dut.decayDamage(5));
		assertTrue(dut.decayDamage(1));
		assertTrue(dut.decayDamage(1));
		assertTrue(dut.decayDamage(1));
		assertTrue(dut.decayDamage(1));
		assertFalse(dut.decayDamage(1));
	}
}
