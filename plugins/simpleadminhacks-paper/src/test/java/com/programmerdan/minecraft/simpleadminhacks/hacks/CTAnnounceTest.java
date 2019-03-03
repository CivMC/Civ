package com.programmerdan.minecraft.simpleadminhacks.hacks;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;
import net.minelink.ctplus.event.PlayerCombatTagEvent;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.programmerdan.minecraft.simpleadminhacks.BroadcastLevel;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacksConfig;
import com.programmerdan.minecraft.simpleadminhacks.configs.CTAnnounceConfig;

public class CTAnnounceTest {

	public CTAnnounce instance;
	public CTAnnounceConfig config;

	public SimpleAdminHacks plugin;
	public SimpleAdminHacksConfig pluginconfig;

	@Before
	public void setUp() throws Exception {
		config = mock(CTAnnounceConfig.class);
		plugin = mock(SimpleAdminHacks.class);
		pluginconfig = mock(SimpleAdminHacksConfig.class);
		when(plugin.serverHasPlugin("CombatTagPlus")).thenReturn(true);
		when(plugin.config()).thenReturn(pluginconfig);
		when(pluginconfig.getBroadcastPermission()).thenReturn("simpleadmin.broadcast");
	}

	@After
	public void tearDown() throws Exception {
		plugin = null;
		config = null;
		instance = null;
	}

	@Test
	public void testStatus() {
		instance = new CTAnnounce(plugin, config);
		when(config.isEnabled()).thenReturn(true, false);
		assertEquals("CombatTagPlus.PlayerCombatTagEvent monitoring active", instance.status());
		assertEquals("CombatTagPlus.PlayerCombatTagEvent monitoring not active", instance.status());
	}

	@Test
	public void testCTEventQuickFail() {
		instance = new CTAnnounce(plugin, config);
		when(config.isEnabled()).thenReturn(true);
		PlayerCombatTagEvent cte = new PlayerCombatTagEvent(null, null, 30);

		try {
			instance.CTEvent(cte);

			assertTrue(true);
		} catch( NullPointerException npe) {
			fail("Check failed to prevent NPE by fast-failing on null attacker/victim.");
		}
	}

	private List<BroadcastLevel> allLevels() {
		return Arrays.asList(BroadcastLevel.values());
	}

	private Set<OfflinePlayer> fakeOperators() {
		Set<OfflinePlayer> op = new HashSet<OfflinePlayer>();

		OfflinePlayer ofp = mock(OfflinePlayer.class);
		when(ofp.isOnline()).thenReturn(true);
		op.add(ofp);
		return op;
	}

	/**
	 * If I've done this right, should validate that all broadcast types (minus the general broadcast
	 *   method -- I'm not testing that Bukkit works, here) function, that {@link CTAnnounce#cleanMessage()} 
	 *   works, and that throttling works.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCTEvent() {
		instance = new CTAnnounce(plugin, config);
		instance.dataBootstrap();

		when(config.isEnabled()).thenReturn(true);
		when(config.getBroadcastDelay()).thenReturn(250l);
		when(config.getBroadcast()).thenReturn(allLevels());
		when(config.getBroadcastMessage()).thenReturn("%Victim% struck by %Attacker%");

		Set<OfflinePlayer> ops = fakeOperators();
		Set<SoftPlayer> sops = new HashSet<SoftPlayer>();
		for (OfflinePlayer op : ops) {
			SoftPlayer sfp = mock(SoftPlayer.class);
			when(op.getPlayer()).thenReturn(sfp);
			sops.add(sfp);
		}
		when(plugin.serverOperatorBroadcast(anyString())).thenReturn(ops.size());
		when(plugin.serverBroadcast(anyString())).thenReturn(4);

		/* This doubles as a check on hidden method {@link CTAnnounce#cleanMessage()} */
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				String toSend = invocation.getArgumentAt(0, String.class);
				assertEquals("Victim struck by Attacker", toSend);
				return null;
			}
		}).when(plugin).serverSendConsoleMessage(anyString());

		SoftPlayer vic = mock(SoftPlayer.class);
		String vicName = "Victim";
		when(vic.getName()).thenReturn(vicName);
		when(vic.getDisplayName()).thenReturn(vicName);
		UUID vicUUID = UUID.randomUUID();
		when(vic.getUniqueId()).thenReturn(vicUUID);
		when(vic.isOnline()).thenReturn(true);

		SoftPlayer att = mock(SoftPlayer.class);
		String attName = "Attacker";
		when(att.getName()).thenReturn(attName);
		when(att.getDisplayName()).thenReturn(attName);
		UUID attUUID = UUID.randomUUID();
		when(att.getUniqueId()).thenReturn(attUUID);
		when(att.isOnline()).thenReturn(true);

		PlayerCombatTagEvent cte = new PlayerCombatTagEvent(vic, att, 30);

		LinkedList online = new LinkedList();
		online.add(vic);
		online.add(att);
		when(plugin.serverOnlineBroadcast(anyString())).thenReturn(online.size());

		instance.CTEvent(cte);
		// Now we make sure everyone got notified, and only once.

		// OPs got notified
		verify(plugin).serverOperatorBroadcast(anyString());

		// Console got notified
		verify(plugin).serverSendConsoleMessage(anyString());

		// All Players got notified
		verify(plugin).serverOnlineBroadcast(anyString());

		// Broadcast holders got notified
		verify(plugin).serverBroadcast(anyString());

		try {
			Thread.sleep(10l);
		} catch (InterruptedException ie) {
		}

		// This one should get throttled right away.
		instance.CTEvent(cte);

		// verify that console was _not_ alerted again (e.g. still only one message)
		verify(plugin).serverSendConsoleMessage(anyString());

		try {
			Thread.sleep(400l);
		} catch (InterruptedException ie) {
		}

		// This one should not get throttled.
		instance.CTEvent(cte);

		// verify that console was alerted again (e.g. second throttled, third succeeded)
		verify(plugin, times(2)).serverSendConsoleMessage(anyString());
	}

	abstract interface SoftPlayer extends Player {}
}
