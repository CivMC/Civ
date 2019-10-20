package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import com.devotedmc.ExilePearl.util.Clock;

public class DamageLogTest {

	private UUID uid = UUID.randomUUID();
	private Clock clock;
	private DamageLog dut;
	private double maxAmount = 100;

	@Before
	public void setUp() throws Exception {
		clock = mock(Clock.class);
		when(clock.getCurrentTime()).thenReturn(0L);

		dut = new DamageLog(clock, uid);
		assertEquals(uid, dut.getPlayerId());
	}

	@Test
	public void testConstructor() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new DamageLog(null, uid); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);

		e = null;
		try { new DamageLog(clock, null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
	}

	@Test
	public void test() {
		UUID p1Id = UUID.randomUUID();
		UUID p2Id = UUID.randomUUID();
		UUID p3Id = UUID.randomUUID();

		Player p1 = mock(Player.class);
		Player p2 = mock(Player.class);
		Player p3 = mock(Player.class);

		when(p1.getUniqueId()).thenReturn(p1Id);
		when(p2.getUniqueId()).thenReturn(p2Id);
		when(p3.getUniqueId()).thenReturn(p3Id);

		when(clock.getCurrentTime()).thenReturn(100L);
		dut.recordDamage(p1, 10.0, maxAmount);

		when(clock.getCurrentTime()).thenReturn(101L);
		dut.recordDamage(p2, 15.0, maxAmount);

		when(clock.getCurrentTime()).thenReturn(102L);
		dut.recordDamage(p3, 5.0, maxAmount);

		// Sorted by time
		List<DamageRecord> recs = dut.getTimeSortedDamagers();
		assertEquals(3, recs.size());
		assertEquals(p3Id, recs.get(0).getDamager());
		assertEquals(p2Id, recs.get(1).getDamager());
		assertEquals(p1Id, recs.get(2).getDamager());

		// Sorted by damage
		recs = dut.getDamageSortedDamagers();
		assertEquals(3, recs.size());
		assertEquals(p2Id, recs.get(0).getDamager());
		assertEquals(p1Id, recs.get(1).getDamager());
		assertEquals(p3Id, recs.get(2).getDamager());

		// Kick off p3 
		assertTrue(dut.decayDamage(5));

		recs = dut.getTimeSortedDamagers();
		assertEquals(2, recs.size());
		assertEquals(p2Id, recs.get(0).getDamager());
		assertEquals(p1Id, recs.get(1).getDamager());

		recs = dut.getDamageSortedDamagers();
		assertEquals(2, recs.size());
		assertEquals(p2Id, recs.get(0).getDamager());
		assertEquals(p1Id, recs.get(1).getDamager());

		// Put p3 back on
		when(clock.getCurrentTime()).thenReturn(103L);
		dut.recordDamage(p3, 2, maxAmount);

		recs = dut.getTimeSortedDamagers();
		assertEquals(3, recs.size());
		assertEquals(p3Id, recs.get(0).getDamager());
		assertEquals(p2Id, recs.get(1).getDamager());
		assertEquals(p1Id, recs.get(2).getDamager());

		// Sorted by damage
		recs = dut.getDamageSortedDamagers();
		assertEquals(3, recs.size());
		assertEquals(p2Id, recs.get(0).getDamager());
		assertEquals(p1Id, recs.get(1).getDamager());
		assertEquals(p3Id, recs.get(2).getDamager());

		// Kick off p3 again
		assertTrue(dut.decayDamage(4));

		recs = dut.getTimeSortedDamagers();
		assertEquals(2, recs.size());
		assertEquals(p2Id, recs.get(0).getDamager());
		assertEquals(p1Id, recs.get(1).getDamager());

		recs = dut.getDamageSortedDamagers();
		assertEquals(2, recs.size());
		assertEquals(p2Id, recs.get(0).getDamager());
		assertEquals(p1Id, recs.get(1).getDamager());

		// Kick off p1
		assertTrue(dut.decayDamage(1));

		recs = dut.getTimeSortedDamagers();
		assertEquals(1, recs.size());
		assertEquals(p2Id, recs.get(0).getDamager());

		recs = dut.getDamageSortedDamagers();
		assertEquals(1, recs.size());
		assertEquals(p2Id, recs.get(0).getDamager());

		// Kick off p2
		assertFalse(dut.decayDamage(10));
		recs = dut.getTimeSortedDamagers();
		assertEquals(0, recs.size());		
		recs = dut.getDamageSortedDamagers();
		assertEquals(0, recs.size());
	}
}
