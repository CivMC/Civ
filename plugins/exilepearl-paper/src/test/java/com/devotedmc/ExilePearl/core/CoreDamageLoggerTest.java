package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static com.devotedmc.testbukkit.TestBukkit.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.devotedmc.ExilePearl.util.Clock;
import com.devotedmc.testbukkit.TestBukkitRunner;
import com.devotedmc.testbukkit.TestPlayer;

import net.minelink.ctplus.compat.api.NpcIdentity;

@RunWith(TestBukkitRunner.class)
public class CoreDamageLoggerTest {
	
	private static final int INTERVAL = 20;
	
	private ExilePearlApi pearlApi;
	private PearlConfig config;
	private Clock clock;
	private CoreDamageLogger dut;
	
	private TestPlayer player = createOnlinePlayer("player");
	private TestPlayer d1 = createOnlinePlayer("d1");
	private TestPlayer d2 = createOnlinePlayer("d2");
	private TestPlayer d3 = createOnlinePlayer("d3");

	@Before
	public void setUp() throws Exception {
		clock = mock(Clock.class);
		when(clock.getCurrentTime()).thenReturn(100L);

		config = mock(PearlConfig.class);
		when(config.getDamageLogInterval()).thenReturn(INTERVAL);
		when(config.getDamageLogAlgorithm()).thenReturn(0);
		when(config.getDamageLogDecayAmount()).thenReturn(1d);
		when(config.getDamageLogMaxDamage()).thenReturn(10d);
		when(config.getDamageLogPotionDamage()).thenReturn(2d);
		
		pearlApi = mock(ExilePearlApi.class);
		when(pearlApi.getClock()).thenReturn(clock);
		when(pearlApi.getPearlConfig()).thenReturn(config);
		
		dut = new CoreDamageLogger(pearlApi);
		dut.loadConfig(config);
	}
	
	@Test
	public void testCoreDamageLogger() {
		// Null arguments throw exceptions
		Throwable e = null;
		try { new CoreDamageLogger(null); } catch (Throwable ex) { e = ex; }
		assertTrue(e instanceof NullArgumentException);
		
		assertEquals("Damage Logger", dut.getTaskName());
	}
	
	@Test
	public void testStartStop() {
		final BukkitScheduler scheduler = getServer().getScheduler();
		reset(scheduler);
		
		assertFalse(dut.isRunning());
		
		dut.start();
		verify(scheduler).scheduleSyncRepeatingTask(pearlApi, dut, INTERVAL, INTERVAL);
		assertTrue(dut.isRunning());
		
		dut.start();
		verify(scheduler).scheduleSyncRepeatingTask(pearlApi, dut, INTERVAL, INTERVAL);
		assertTrue(dut.isRunning());
		
		dut.stop();
		verify(scheduler).cancelTask(anyInt());
		assertFalse(dut.isRunning());
	}
	
	@Test
	public void testRestart() {
		final BukkitScheduler scheduler = getServer().getScheduler();
		reset(scheduler);
		
		when(config.getDamageLogInterval()).thenReturn(40);
		dut.loadConfig(config);
		
		dut.restart();
		verify(scheduler).scheduleSyncRepeatingTask(pearlApi, dut, 40, 40);
		assertTrue(dut.isRunning());
		
		dut.restart();
		verify(scheduler).cancelTask(anyInt());
		assertTrue(dut.isRunning());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testEntityDamage() {
		dut.start();

		List<Player> damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		// t = 100
		// d1 = 5
		// d2 = 0
		// d3 = 0
		EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(d1, player, null, 5);
		dut.onEntityDamageByEntity(e);
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));

		// t = 101
		// d1 = 5
		// d2 = 10
		// d3 = 0
		when(clock.getCurrentTime()).thenReturn(101L);
		e = new EntityDamageByEntityEvent(d2, player, null, 10);
		dut.onEntityDamageByEntity(e);
		damagers = dut.getSortedDamagers(player);
		assertEquals(2, damagers.size());
		assertEquals(d2, damagers.get(0));
		assertEquals(d1, damagers.get(1));
		
		// t = 102
		// d1 = 6
		// d2 = 10
		// d3 = 0
		when(clock.getCurrentTime()).thenReturn(102L);
		e = new EntityDamageByEntityEvent(d1, player, null, 1);
		dut.onEntityDamageByEntity(e);
		damagers = dut.getSortedDamagers(player);
		assertEquals(2, damagers.size());
		assertEquals(d1, damagers.get(0));
		assertEquals(d2, damagers.get(1));
		
		// t = 103
		// d1 = 6
		// d2 = 10
		// d3 = 8
		when(clock.getCurrentTime()).thenReturn(103L);
		e = new EntityDamageByEntityEvent(d3, player, null, 8);
		dut.onEntityDamageByEntity(e);
		damagers = dut.getSortedDamagers(player);
		assertEquals(3, damagers.size());
		assertEquals(d3, damagers.get(0));
		assertEquals(d1, damagers.get(1));
		assertEquals(d2, damagers.get(2));

		// Switch algorithm
		when(config.getDamageLogAlgorithm()).thenReturn(1);
		dut.loadConfig(config);

		// t = 103
		// d1 = 6
		// d2 = 10
		// d3 = 8
		damagers = dut.getSortedDamagers(player);
		assertEquals(3, damagers.size());
		assertEquals(d2, damagers.get(0));
		assertEquals(d3, damagers.get(1));
		assertEquals(d1, damagers.get(2));
		
		// Offline players are ignored
		when(d2.isOnline()).thenReturn(false);
		damagers = dut.getSortedDamagers(player);
		assertEquals(2, damagers.size());
		assertEquals(d3, damagers.get(0));
		assertEquals(d1, damagers.get(1));
		
		// The damager still exists when he comes back online
		when(d2.isOnline()).thenReturn(true);
		damagers = dut.getSortedDamagers(player);
		assertEquals(3, damagers.size());
		assertEquals(d2, damagers.get(0));
		assertEquals(d3, damagers.get(1));
		assertEquals(d1, damagers.get(2));
		
		// t = 103
		// d1 = 6
		// d2 = 10
		// d3 = 8
		dut.run();
		dut.run();
		dut.run();
		dut.run();
		dut.run();
		damagers = dut.getSortedDamagers(player);
		assertEquals(3, damagers.size());
		assertEquals(d2, damagers.get(0));
		assertEquals(d3, damagers.get(1));
		assertEquals(d1, damagers.get(2));
		
		// This will kick off d1
		dut.run();
		damagers = dut.getSortedDamagers(player);
		assertEquals(2, damagers.size());
		assertFalse(damagers.contains(d1));
		assertEquals(d2, damagers.get(0));
		assertEquals(d3, damagers.get(1));
		
		// t = 104
		// d1 = 5
		// d2 = 4
		// d3 = 2
		when(clock.getCurrentTime()).thenReturn(104L);
		e = new EntityDamageByEntityEvent(d1, player, null, 5);
		dut.onEntityDamageByEntity(e);
		damagers = dut.getSortedDamagers(player);
		assertEquals(3, damagers.size());
		assertEquals(d1, damagers.get(0));
		assertEquals(d2, damagers.get(1));
		assertEquals(d3, damagers.get(2));

		// This will kick off d3
		dut.run();
		dut.run();
		damagers = dut.getSortedDamagers(player);
		assertEquals(2, damagers.size());
		assertEquals(d1, damagers.get(0));
		assertEquals(d2, damagers.get(1));
		
		// This will kick off d2
		dut.run();
		dut.run();
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));
		
		// This will kick off d1
		dut.run();
		damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testEntityDeath() {
		dut.start();

		List<Player> damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(d1, player, null, 5);
		dut.onEntityDamageByEntity(damageEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));
		
		EntityDeathEvent deathEvent = new EntityDeathEvent(player, null);
		dut.onEntityDeath(deathEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testPlayerQuit() {
		dut.start();

		List<Player> damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(d1, player, null, 5);
		dut.onEntityDamageByEntity(damageEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));
		
		PlayerQuitEvent quitEvent = new PlayerQuitEvent(player, null);
		dut.onPlayerQuit(quitEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testPlayerPearled() {
		dut.start();

		List<Player> damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(d1, player, null, 5);
		dut.onEntityDamageByEntity(damageEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));
		
		ExilePearl pearl = mock(ExilePearl.class);
		when(pearl.getPlayerId()).thenReturn(player.uid);
		
		PlayerPearledEvent pearlEvent = new PlayerPearledEvent(pearl);
		dut.onPlayerPearled(pearlEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testWolfDamage() {
		dut.start();

		List<Player> damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		Wolf wolf = mock(Wolf.class);
		
		EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(wolf, player, null, 5);
		dut.onEntityDamageByEntity(damageEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		when(wolf.getOwner()).thenReturn(d1);
		dut.onEntityDamageByEntity(damageEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testArrowDamage() {
		dut.start();

		List<Player> damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		Arrow arrow = mock(Arrow.class);
		
		EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(arrow, player, null, 5);
		dut.onEntityDamageByEntity(damageEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		when(arrow.getShooter()).thenReturn(d1);
		dut.onEntityDamageByEntity(damageEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testNpcDamage() {
		dut.start();

		List<Player> damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		Player npcPlayer = mock(Player.class);
		NpcIdentity npcIdentity = new NpcIdentity(player);
		when(pearlApi.getPlayerAsTaggedNpc(npcPlayer)).thenReturn(npcIdentity);
		
		EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(d1, npcPlayer, null, 5);
		dut.onEntityDamageByEntity(damageEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testSnowballDamage() {
		dut.start();

		List<Player> damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		Snowball snowball = mock(Snowball.class);
		
		EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(snowball, player, null, 5);
		dut.onEntityDamageByEntity(damageEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		when(snowball.getShooter()).thenReturn(d1);
		dut.onEntityDamageByEntity(damageEvent);
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));
	}
	
	@Test
	public void testPotionDamage() {
		dut.start();
		when(config.getDamageLogPotionDamage()).thenReturn(2d);

		List<Player> damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		ThrownPotion potion = mock(ThrownPotion.class);
		when(potion.getShooter()).thenReturn(d1);
		Collection<PotionEffect> effects = new ArrayList<PotionEffect>();
		when(potion.getEffects()).thenReturn(effects);
		
		HashMap<LivingEntity, Double> affected = new HashMap<LivingEntity, Double>();
		affected.put(player, 1.0);
		
		// No effect so no damage
		PotionSplashEvent e = new PotionSplashEvent(potion, affected);
		dut.onPotionSplashEvent(e);
		damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
		
		// Add an effect
		effects.add(new PotionEffect(PotionEffectType.POISON, 10, 1));
		ItemStack is = new ItemStack(Material.SPLASH_POTION, 1);
		is.setDurability((short)(1 << 5)); // This makes it a poison II potion
		
		when(potion.getItem()).thenReturn(is);
		affected.put(player, 0.75); // 75% strength
		
		e = new PotionSplashEvent(potion, affected);
		dut.onPotionSplashEvent(e);
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));
		
		// The damage should be at 2 * 2 * 0.75 = 3
		dut.run();
		dut.run();
		damagers = dut.getSortedDamagers(player);
		assertEquals(1, damagers.size());
		assertEquals(d1, damagers.get(0));

		dut.run();
		damagers = dut.getSortedDamagers(player);
		assertEquals(0, damagers.size());
	}
}
