package com.devotedmc.ExilePearl.core;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.Util.BukkitTestCase;
import com.devotedmc.ExilePearl.config.PearlConfig;
import com.devotedmc.ExilePearl.event.PlayerFreedEvent;
import com.devotedmc.ExilePearl.event.PlayerPearledEvent;
import com.devotedmc.ExilePearl.holder.PearlHolder;

public class PearlBoundaryTaskTest extends BukkitTestCase {
	
	private static int RADIUS = 100;
	private static int RADIUS_KNOCK = RADIUS + PearlBoundaryTask.KNOCKBACK;
	
	private ExilePearlApi pearlApi;
	private PearlConfig config;
	private PearlBoundaryTask dut;
	private UUID playerId = UUID.randomUUID();
	private Player player;

	@Before
	public void setUp() throws Exception {
		
		pearlApi = mock(ExilePearlApi.class);
		
		config = mock(PearlConfig.class);
		when(config.getRulePearlRadius()).thenReturn(RADIUS);
		
		dut = new PearlBoundaryTask(pearlApi);
		dut.loadConfig(config);
		
		player = mock(Player.class);
		when(player.getUniqueId()).thenReturn(playerId);
		when(player.getName()).thenReturn("Player");
	}
	
	@Test
	public void testStartStop() {
		final BukkitScheduler scheduler = getServer().getScheduler();
		reset(scheduler);
		
		assertFalse(dut.isRunning());
		
		dut.start();
		verify(scheduler).scheduleSyncRepeatingTask(pearlApi, dut, PlayerSuicideTask.TICKS_PER_SECOND, PlayerSuicideTask.TICKS_PER_SECOND);
		assertTrue(dut.isRunning());
		
		dut.start();
		verify(scheduler).scheduleSyncRepeatingTask(pearlApi, dut, PlayerSuicideTask.TICKS_PER_SECOND, PlayerSuicideTask.TICKS_PER_SECOND);
		assertTrue(dut.isRunning());
		
		dut.stop();
		verify(scheduler).cancelTask(anyInt());
		assertFalse(dut.isRunning());
	}

	@Test
	public void testEvents() {
		assertFalse(dut.isPlayerTracked(player));
		
		// Normal players are ignored when they join
		PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, null);
		dut.onPlayerJoin(joinEvent);
		assertFalse(dut.isPlayerTracked(player));
		
		// Pearled players are tracked when they join
		when(pearlApi.isPlayerExiled(player)).thenReturn(true);
		dut.onPlayerJoin(joinEvent);
		assertTrue(dut.isPlayerTracked(player));
		
		// Players are removed when they quit
		PlayerQuitEvent quitEvent = new PlayerQuitEvent(player, null);
		dut.onPlayerQuit(quitEvent);
		assertFalse(dut.isPlayerTracked(player));
		
		// mock pearl instance
		ExilePearl pearl = mock(ExilePearl.class);
		when(pearl.getPlayer()).thenReturn(player);
		when(pearl.getPlayerId()).thenReturn(playerId);
		
		// Pearl events are ignored if the player is not online
		PlayerPearledEvent pearlEvent = new PlayerPearledEvent(pearl);
		dut.onPlayerPearled(pearlEvent);
		assertFalse(dut.isPlayerTracked(player));
		
		// Pearl events are tracked when the player is online
		when(player.isOnline()).thenReturn(true);
		dut.onPlayerPearled(pearlEvent);
		assertTrue(dut.isPlayerTracked(player));
		
		// Freed players are removed
		PlayerFreedEvent freeEvent = new PlayerFreedEvent(pearl, PearlFreeReason.FREED_BY_PLAYER);
		dut.onPlayerFreed(freeEvent);
		assertFalse(dut.isPlayerTracked(player));
	}
	
	@Test
	public void testRun() {
		
		ExilePearl pearl = mock(ExilePearl.class);
		when(pearl.getPlayer()).thenReturn(player);
		when(pearl.getPlayerId()).thenReturn(playerId);
		when(pearlApi.getPlayer(playerId)).thenReturn(player);
		when(pearlApi.getPearl(playerId)).thenReturn(pearl);
		when(pearlApi.isPlayerExiled(player)).thenReturn(true);
		
		PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, null);
		dut.onPlayerJoin(joinEvent);
		assertTrue(dut.isPlayerTracked(player));
		
		Chunk chunk = mock(Chunk.class);
		when(chunk.isLoaded()).thenReturn(true);
		
		World world = getServer().getWorld("world");
		when(world.getChunkAt(anyInt(), anyInt())).thenReturn(chunk);
		
		Location pearlLocation = new Location(world, 0, 64, 0);
		when(pearl.getLocation()).thenReturn(pearlLocation);
		
		PearlHolder holder = mock(PearlHolder.class);
		when(holder.isBlock()).thenReturn(true);
		when(pearl.getHolder()).thenReturn(holder);
		
		Location playerLocation = new Location(world, 0, 64, 0);
		when(player.getLocation()).thenReturn(playerLocation);
		
		dut.start();
		dut.run();
		verifyTeleport(0);
		
		when(player.isOnline()).thenReturn(true);
		
		dut.run();
		verifyTeleport(1);
		
		// Verify game modes
		when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
		dut.run();
		verifyTeleport(1);
		when(player.getGameMode()).thenReturn(GameMode.SPECTATOR);
		dut.run();
		verifyTeleport(1);
		when(player.getGameMode()).thenReturn(GameMode.SURVIVAL);
		dut.run();
		verifyTeleport(2);
		
		// Verify worlds
		when(player.getLocation()).thenReturn(new Location(mock(World.class), 0, 64, 0));
		dut.run();
		verifyTeleport(2);
		when(player.getLocation()).thenReturn(playerLocation);
		dut.run();
		verifyTeleport(3);
		
		// Verify radius
		when(player.getLocation()).thenReturn(new Location(world, 100, 64, 0));
		dut.run();
		verifyTeleport(3);
		when(player.getLocation()).thenReturn(new Location(world, -100, 64, 0));
		dut.run();
		verifyTeleport(3);
		when(player.getLocation()).thenReturn(new Location(world, 0, 64, 100));
		dut.run();
		verifyTeleport(3);
		when(player.getLocation()).thenReturn(new Location(world, 0, 64, -100));
		dut.run();
		verifyTeleport(3);
		when(player.getLocation()).thenReturn(new Location(world, 0, 0, 99));
		dut.run();
		verifyTeleport(4);
		
		// Verify holder must be block
		when(holder.isBlock()).thenReturn(false);
		dut.run();
		verifyTeleport(4);
		when(holder.isBlock()).thenReturn(true);
		dut.run();
		verifyTeleport(5);
		
		// Verify the player gets teleported outside the radius
		when(player.getLocation()).thenReturn(new Location(world, 0, 64, 50));
		ArgumentCaptor<Location> arg = ArgumentCaptor.forClass(Location.class);
		dut.run();
		verify(player, times(6)).teleport(arg.capture(), any(TeleportCause.class));
		Location teleportLoc = arg.getValue();
		assertEquals(RADIUS_KNOCK, teleportLoc.getBlockZ());

		// Player is not moved again
		when(player.getLocation()).thenReturn(teleportLoc);
		dut.run();
		verifyTeleport(6);
	}
	
	private void verifyTeleport(int num) {
		verify(player, times(num)).teleport(any(Location.class), any(TeleportCause.class));
	}
}
