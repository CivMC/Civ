package com.untamedears.JukeAlert.listener;

import static com.untamedears.JukeAlert.util.Utility.doesSnitchExist;
import static com.untamedears.JukeAlert.util.Utility.isDebugging;
import static com.untamedears.JukeAlert.util.Utility.immuneToSnitch;
import static com.untamedears.JukeAlert.util.Utility.notifyGroup;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.Lever;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.events.PlayerHitSnitchEvent;
import com.untamedears.JukeAlert.events.PlayerLoginSnitchEvent;
import com.untamedears.JukeAlert.events.PlayerLogoutSnitchEvent;
import com.untamedears.JukeAlert.external.Mercury;
import com.untamedears.JukeAlert.external.VanishNoPacket;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.events.GroupDeleteEvent;
import vg.civcraft.mc.namelayer.events.GroupInvalidationEvent;
import vg.civcraft.mc.namelayer.events.GroupMergeEvent;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class JukeAlertListener implements Listener {

	private ReinforcementManager rm = Citadel.getReinforcementManager();

	private final JukeAlert plugin = JukeAlert.getInstance();

	SnitchManager snitchManager = plugin.getSnitchManager();

	private final Map<UUID, Set<Snitch>> playersInSnitches = new TreeMap<UUID, Set<Snitch>>();

	private final VanishNoPacket vanishNoPacket = new VanishNoPacket();

	private final Mercury mercury = new Mercury();

	private boolean checkProximity(Snitch snitch, UUID accountId) {

		Set<Snitch> inList = playersInSnitches.get(accountId);
		if (inList == null) {
			inList = new TreeSet<Snitch>();
			playersInSnitches.put(accountId, inList);
		}
		return inList.contains(snitch);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerJoinEvent(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		if (vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish")) {
			return;
		}
		UUID accountId = player.getUniqueId();
		Set<Snitch> inList = new TreeSet<Snitch>();
		playersInSnitches.put(accountId, inList);

		Location location = player.getLocation();
		World world = location.getWorld();
		Set<Snitch> snitches = snitchManager.findSnitches(world, location);
		for (Snitch snitch : snitches) {
			if (!immuneToSnitch(snitch, accountId)) {
				Bukkit.getPluginManager().callEvent(new PlayerLoginSnitchEvent(snitch, player));
				snitch.imposeSnitchTax();
				inList.add(snitch);
				try {
					TextComponent message = new TextComponent(ChatColor.AQUA + " * " + player.getDisplayName()
						+ " logged in to snitch at "
						+ snitch.getName() + " [" + snitch.getLoc().getWorld().getName() + " " + snitch.getX() +
						" " + snitch.getY() + " " + snitch.getZ() + "]");
					String hoverText = snitch.getHoverText(null, null);
					message.setHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
					notifyGroup(snitch, message);

					if (mercury.isEnabled() && plugin.getConfigManager().getBroadcastAllServers()) {
						mercury.sendMessage(snitch.getGroup().getName() + " " + message, "jukealert-login");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if (snitch.shouldLog()) {
					plugin.getJaLogger().logSnitchLogin(snitch, location, player);

					Location north = new Location(world, snitch.getX(), snitch.getY(), snitch.getZ() - 1);
					toggleLeverIfApplicable(snitch, north, true);
				}
			}
		}
	}

	public void handlePlayerExit(PlayerEvent event) {

		Player player = event.getPlayer();

		if (vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish")) {
			return;
		}
		UUID accountId = player.getUniqueId();
		playersInSnitches.remove(accountId);

		Location location = player.getLocation();
		World world = location.getWorld();
		Set<Snitch> snitches = snitchManager.findSnitches(world, location);
		for (Snitch snitch : snitches) {
			if (!immuneToSnitch(snitch, accountId)) {
				Bukkit.getPluginManager().callEvent(new PlayerLogoutSnitchEvent(snitch, player));
				snitch.imposeSnitchTax();
				try {
					TextComponent message = new TextComponent(ChatColor.AQUA + " * " + player.getDisplayName()
						+ " logged out in snitch at "
						+ snitch.getName() + " [" + snitch.getLoc().getWorld().getName() + " " + snitch.getX() +
						" " + snitch.getY() + " " + snitch.getZ() + "]");
					String hoverText = snitch.getHoverText(null, null);
					message.setHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
					notifyGroup(snitch, message);

					if (mercury.isEnabled() && plugin.getConfigManager().getBroadcastAllServers()) {
						mercury.sendMessage(snitch.getGroup().getName() + " " + message, "jukealert-logout");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				if (snitch.shouldLog()) {
					plugin.getJaLogger().logSnitchLogout(snitch, location, player);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void playerKickEvent(PlayerKickEvent event) {

		handlePlayerExit(event);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void playerQuitEvent(PlayerQuitEvent event) {

		handlePlayerExit(event);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void placeSnitchBlock(BlockPlaceEvent event) {

		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		Player player = event.getPlayer();
		Location loc = block.getLocation();
		if (block.getType().equals(Material.JUKEBOX)) {
			if (!rm.isReinforced(loc)) {
				player.sendMessage(
					ChatColor.YELLOW + "You've placed a Jukebox; reinforce it to register it as a snitch.");
			}
		} else if (block.getType().equals(Material.NOTE_BLOCK)) {
			if (!rm.isReinforced(loc)) {
				player.sendMessage(
					ChatColor.YELLOW + "You've placed a Noteblock; reinforce it to register it as an entry snitch.");
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void reinforceSnitchBlock(ReinforcementCreationEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		if (block == null || block.getType() == null) {
			return;
		}
		boolean isJukebox = block.getType().equals(Material.JUKEBOX);
		boolean isNoteblock = block.getType().equals(Material.NOTE_BLOCK);
		if (!isJukebox && !isNoteblock) {
			return;
		}
		Player player = event.getPlayer();
		if (player == null) {
			return;
		}
		Location loc = block.getLocation();
		if (loc == null) {
			return;
		}
		Reinforcement rei = event.getReinforcement();
		if (rei == null || !(rei instanceof PlayerReinforcement)) {
			return;
		}
		PlayerReinforcement reinforcement = (PlayerReinforcement) rei;
		Group owner = reinforcement.getGroup();
		if (owner == null) {
			JukeAlert.getInstance().log(String.format(
				"No group on rein (%s)", reinforcement.getLocation().toString()));
		}
		Snitch snitch;
		if (snitchManager.getSnitch(loc.getWorld(), loc) != null) {
			snitch = snitchManager.getSnitch(loc.getWorld(), loc);
			plugin.getJaLogger().updateSnitchGroup(
				snitchManager.getSnitch(loc.getWorld(), loc), owner.getName());
			snitchManager.removeSnitch(snitch);
			snitch.setGroup(owner);
		} else {
			snitch = new Snitch(loc, owner, isJukebox, false);
			plugin.getJaLogger().logSnitchPlace(
				player.getWorld().getName(), owner.getName(), "", loc.getBlockX(), loc.getBlockY(),
				loc.getBlockZ(), isJukebox);
			snitch.setId(plugin.getJaLogger().getLastSnitchID());
			plugin.getJaLogger().increaseLastSnitchID();
		}
		snitchManager.addSnitch(snitch);

		String snitchGroupName = "";
		if (snitch.getGroup() != null && snitch.getGroup().getName() != null) {
			snitchGroupName = snitch.getGroup().getName();
		}
		String message;
		if (isJukebox) {
			message = (ChatColor.AQUA + "You've created a snitch registered to the group " + snitchGroupName
				+ ". To name it, type /janame.");
		} else {
			message = (ChatColor.AQUA + "You've created an entry snitch registered to the group " + snitchGroupName
				+ ". To name it, type /janame.");
		}
		TextComponent lineText = new TextComponent(message);
		String hoverText = snitch.getHoverText(null, null);
		lineText.setHoverEvent(
			new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
		player.spigot().sendMessage(lineText);
	}

	@EventHandler(ignoreCancelled = true)
	public void onGroupDeletion(GroupDeleteEvent event) {

		String groupName = event.getGroup().getName();
		Set<Snitch> removeSet = new TreeSet<Snitch>();
		for (Snitch snitch : snitchManager.getAllSnitches()) {
			final Group snitchGroup = snitch.getGroup();
			String snitchGroupName = null;
			if (snitchGroup != null) {
				snitchGroupName = snitchGroup.getName();
			}
			if (snitchGroupName != null && snitchGroupName.equalsIgnoreCase(groupName)) {
				removeSet.add(snitch);
			}
		}
		for (Snitch snitch : removeSet) {
			final Location loc = snitch.getLoc();
			if (snitch.shouldLog()) {
				plugin.getJaLogger().logSnitchBreak(
					loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			}
			snitchManager.removeSnitch(snitch);
		}
	}

	@EventHandler
	public void onGroupEvent(GroupInvalidationEvent event) {

		String reason = event.getReason();
		if (reason.equalsIgnoreCase("delete")) {
			String groupName = event.getParameter() [0];
			Set<Snitch> removeSet = new TreeSet<Snitch>();
			for (Snitch snitch : snitchManager.getAllSnitches()) {
				final Group snitchGroup = snitch.getGroup();
				String snitchGroupName = null;
				if (snitchGroup != null) {
					snitchGroupName = snitchGroup.getName();
				}
				if (snitchGroupName != null && snitchGroupName.equalsIgnoreCase(groupName)) {
					removeSet.add(snitch);
				}
			}
			for (Snitch snitch : removeSet) {
				final Location loc = snitch.getLoc();
				if (snitch.shouldLog()) {
					plugin.getJaLogger().logSnitchBreak(
						loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
				}
				snitchManager.removeSnitch(snitch);
			}
		} else if (reason.equalsIgnoreCase("merge")) {
			String group1 = event.getParameter() [0];
			String group2 = event.getParameter() [1];
			Group g1 = GroupManager.getGroup(group1);
			Set<Snitch> mergeSet = new TreeSet<Snitch>();
			for (Snitch snitch : snitchManager.getAllSnitches()) {
				final Group snitchGroup = snitch.getGroup();
				String snitchGroupName = null;
				if (snitchGroup != null) {
					snitchGroupName = snitchGroup.getName();
				}
				if (snitchGroupName != null && snitchGroupName.equalsIgnoreCase(group2)) {
					mergeSet.add(snitch);
				}
			}
			for (Snitch snitch : mergeSet) {
				snitch.setGroup(g1);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onGroupMergeEvent(GroupMergeEvent event) {

		Group g1 = event.getMergingInto();
		Group g2 = event.getToBeMerged();
		String groupName = g2.getName();
		Set<Snitch> mergeSet = new TreeSet<Snitch>();
		for (Snitch snitch : snitchManager.getAllSnitches()) {
			final Group snitchGroup = snitch.getGroup();
			String snitchGroupName = null;
			if (snitchGroup != null) {
				snitchGroupName = snitchGroup.getName();
			}
			if (snitchGroupName != null && snitchGroupName.equalsIgnoreCase(groupName)) {
				mergeSet.add(snitch);
			}
		}
		for (Snitch snitch : mergeSet) {
			snitch.setGroup(g1);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void breakSnitchBlock(BlockBreakEvent event) {

		Block block = event.getBlock();
		if (block == null) {
			return;
		}
		if (block.getType().equals(Material.JUKEBOX) || block.getType().equals(Material.NOTE_BLOCK)) {
			Snitch snitch = snitchManager.getSnitch(block.getWorld(), block.getLocation());
			if (snitch == null) {
				return;
			}
			if (snitch.getGroup().isMember(event.getPlayer().getUniqueId())) {
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				boolean playerHasPerm = NameAPI.getGroupManager().hasAccess(
					snitch.getGroup(), player.getUniqueId(), PermissionType.getPermission("READ_SNITCHLOG"));
				if (playerHasPerm) {
					String snitchGroup = "";
					if (snitch.getGroup() != null && snitch.getGroup().getName() != null) {
						snitchGroup = snitch.getGroup().getName();
					}
					TextComponent playerSnitchInfoMessage;
					if (snitch.shouldLog()) {
						playerSnitchInfoMessage = new TextComponent(ChatColor.AQUA
							+ "You've broken a snitch registered to the group " + snitchGroup + ".");
					} else {
						playerSnitchInfoMessage = new TextComponent(ChatColor.AQUA
							+ "You've broken an entry snitch registered to the group " + snitchGroup + ".");
					}
					String hoverText = snitch.getHoverText(null, null);
					playerSnitchInfoMessage.setHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
					player.spigot().sendMessage(playerSnitchInfoMessage);
				}
			} else {
				if (plugin.getConfigManager().isDisplayOwnerOnBreak()) {
					Location loc = snitch.getLoc();
					event.getPlayer().sendMessage(ChatColor.AQUA + "Snitch at [" + loc.getWorld().getName()
						+ " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + "] was on group "
						+ snitch.getGroup().getName()
						+ " which is owned by " + NameAPI.getCurrentName(snitch.getGroup().getOwner()));
				}
			}
		}
		if (vanishNoPacket.isPlayerInvisible(event.getPlayer())
				|| event.getPlayer().hasPermission("jukealert.vanish")) {
			return;
		}
		Location loc = block.getLocation();
		if (snitchManager.getSnitch(loc.getWorld(), loc) != null) {
			Snitch snitch = snitchManager.getSnitch(loc.getWorld(), loc);
			plugin.getJaLogger().logSnitchBreak(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(),
				loc.getBlockZ());
			snitchManager.removeSnitch(snitch);
		}
	}

	private long failureReportDelay = 10000l;

	private long lastNotifyMoveFailure = System.currentTimeMillis() - failureReportDelay;

	@EventHandler(priority = EventPriority.HIGH)
	public void enterSnitchProximity(PlayerMoveEvent event) {

		try {
			Location from = event.getFrom();
			Location to = event.getTo();
			if (from == null || to == null) {
				if (System.currentTimeMillis() - lastNotifyMoveFailure > failureReportDelay) {
					JukeAlert.getInstance().getLogger().log(
						Level.WARNING, "MoveEvent called without valid from and to.");
					lastNotifyMoveFailure = System.currentTimeMillis();
				}
				return;
			}

			if (from.getBlockX() == to.getBlockX()
					&& from.getBlockY() == to.getBlockY()
					&& from.getBlockZ() == to.getBlockZ()
					&& from.getWorld().equals(to.getWorld())) {
				// Player didn't move by at least one block
				return;
			}
			Player player = event.getPlayer();
			if (player == null) {
				if (System.currentTimeMillis() - lastNotifyMoveFailure > failureReportDelay) {
					JukeAlert.getInstance().getLogger().log(Level.WARNING, "MoveEvent called without valid player.");
					lastNotifyMoveFailure = System.currentTimeMillis();
				}
				return;
			}
			handleSnitchEntry(player);
		} catch (NullPointerException npe) {
			if (System.currentTimeMillis() - lastNotifyMoveFailure > failureReportDelay) {
				JukeAlert.getInstance().getLogger().log(Level.SEVERE, "MoveEvent generated an NPE", npe);
				lastNotifyMoveFailure = System.currentTimeMillis();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onVehicleMovement(VehicleMoveEvent event) {
			Entity e = event.getVehicle().getPassenger();
			// TODO: apparently there's no way to get the second passenger? wtf, bukkit
			if (e instanceof Player) {
				enterSnitchProximity(new PlayerMoveEvent((Player) e, event.getFrom(), event.getTo()));
			}
		}

	// Because teleporting doesn't trigger a movement event :/
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {

		Player player = event.getPlayer();
		handleSnitchEntry(player);
	}

	private void handleSnitchEntry(Player player) {

		if (player.hasMetadata("NPC")) {
			return;
		}

		if (vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish")) {
			return;
		}
		UUID accountId = player.getUniqueId();
		Location location = player.getLocation();
		World world = location.getWorld();
		Set<Snitch> inList = playersInSnitches.get(accountId);
		if (inList == null) {
			inList = new TreeSet<Snitch>();
			playersInSnitches.put(accountId, inList);
		}
		Set<Snitch> snitches = snitchManager.findSnitches(world, location);
		for (Snitch snitch : snitches) {
			if (doesSnitchExist(snitch, true)) {
				try {
					// Refresh cull timer of snitch
					if (NameAPI.getGroupManager().hasAccess(snitch.getGroup(), player.getUniqueId(),
							PermissionType.getPermission("LIST_SNITCHES"))) {
						if (!inList.contains(snitch)) {
							inList.add(snitch);
							plugin.getJaLogger().logSnitchVisit(snitch);
						}
					}
				} catch (NullPointerException npe) {
					if (System.currentTimeMillis() - lastNotifyMoveFailure > failureReportDelay) {
						JukeAlert.getInstance().getLogger().log(Level.SEVERE,
							"handleSnitchEntry isPartialOwnerOfSnitch generated an exception", npe);
						lastNotifyMoveFailure = System.currentTimeMillis();
					}
				}

				try {
					if ((!immuneToSnitch(snitch, accountId) || isDebugging())) {
						if (!inList.contains(snitch)) {
							snitch.imposeSnitchTax();
							inList.add(snitch);
							Bukkit.getPluginManager().callEvent(new PlayerHitSnitchEvent(snitch, player));
							if ((plugin.getConfigManager().getInvisibilityEnabled()
									&& player.hasPotionEffect(PotionEffectType.INVISIBILITY)) && !snitch.shouldLog()) {
								continue;
							} else {
								try {
									TextComponent message = new TextComponent(ChatColor.AQUA + " * "
										+ player.getDisplayName() + " entered snitch at "
										+ snitch.getName() + " [" + snitch.getLoc().getWorld().getName() + " "
										+ snitch.getX() + " " + snitch.getY() + " " + snitch.getZ() + "]");
									String hoverText = snitch.getHoverText(null, null);
									message.setHoverEvent(
										new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
									notifyGroup(snitch, message);

									if (mercury.isEnabled() && plugin.getConfigManager().getBroadcastAllServers()) {
										Group g = snitch.getGroup();
										if (g != null) {
											mercury.sendMessage(g.getName() + " " + message, "jukealert-entry");
										} else {
											if (System.currentTimeMillis() - lastNotifyMoveFailure
													> failureReportDelay) {
												JukeAlert.getInstance().getLogger().log(
													Level.WARNING,
													"Null group encountered when constructing Mercury Message: {0}",
													message);
												lastNotifyMoveFailure = System.currentTimeMillis();
											}
										}
									}
								} catch (SQLException | NullPointerException e) {
									if (System.currentTimeMillis() - lastNotifyMoveFailure > failureReportDelay) {
										JukeAlert.getInstance().getLogger().log(
											Level.SEVERE, "handleSnitchEntry generated an exception", e);
										lastNotifyMoveFailure = System.currentTimeMillis();
									}
								}
							}
							if (snitch.shouldLog()) {
								plugin.getJaLogger().logSnitchEntry(snitch, location, player);
								Location north = new Location(world, snitch.getX(), snitch.getY(), snitch.getZ() - 1);
								toggleLeverIfApplicable(snitch, north, true);
							}
						}
					}
				} catch (NullPointerException npe) {
					if (System.currentTimeMillis() - lastNotifyMoveFailure > failureReportDelay) {
						JukeAlert.getInstance().getLogger().log(Level.SEVERE,
							"handleSnitchEntry isOnSnitch or contained generated an exception", npe);
						lastNotifyMoveFailure = System.currentTimeMillis();
					}
				}
			}
		}
		snitches = snitchManager.findSnitches(world, location, true);
		Set<Snitch> rmList = new TreeSet<Snitch>();
		for (Snitch snitch : inList) {
			if (snitches.contains(snitch)) {
				continue;
			}
			rmList.add(snitch);
		}
		inList.removeAll(rmList);
	}

	// Exceptions:  No exceptions must be raised from this for any reason
	private void toggleLeverIfApplicable(final Snitch snitch, final Location blockToPossiblyToggle,
			final Boolean leverShouldEnable) {

		try {
			if (!JukeAlert.getInstance().getConfigManager().getAllowTriggeringLevers()) {
				return;
			}
			if (null == snitch) {
				return;
			}

			World world = snitch.getLoc().getWorld();
			if (snitch.shouldToggleLevers()) {
				if (world.getBlockAt(blockToPossiblyToggle).getType() == Material.LEVER) {
					BlockState leverState = world.getBlockAt(blockToPossiblyToggle).getState();
					Lever lever = ((Lever) leverState.getData());

					if (leverShouldEnable && !lever.isPowered()) {
						lever.setPowered(true);
						leverState.setData(lever);
						leverState.update();
					} else if (!leverShouldEnable && lever.isPowered()) {
						lever.setPowered(false);
						leverState.setData(lever);
						leverState.update();
					}

					if (leverShouldEnable) {
						BukkitScheduler scheduler = plugin.getServer().getScheduler();
						scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								toggleLeverIfApplicable(snitch, blockToPossiblyToggle, false);
							}
						}, 15L);
					}
				}
			}
		} catch (Exception ex) {
			// eat.
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryOpenEvent(InventoryOpenEvent e) {

		Player player = (Player) e.getPlayer();
		if (vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish")) {
			return;
		}
		Block block;
		if (e.getInventory().getHolder() instanceof Chest) {
			Chest chest = (Chest) e.getInventory().getHolder();
			block = chest.getBlock();
		} else if (e.getInventory().getHolder() instanceof DoubleChest) {
			DoubleChest chest = (DoubleChest) e.getInventory().getHolder();
			block = chest.getLocation().getBlock();
		} else {
			return;
		}
		UUID accountId = player.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(player.getWorld(), player.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.shouldLog()) {
				continue;
			}
			if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				if (checkProximity(snitch, accountId)) {
					plugin.getJaLogger().logUsed(snitch, player, block);

					Location south = new Location(block.getWorld(), snitch.getX(), snitch.getY(), snitch.getZ() + 1);
					toggleLeverIfApplicable(snitch, south, true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerKillEntity(EntityDeathEvent event) {

		LivingEntity entity = event.getEntity();
		LivingEntity killer = entity.getKiller();
		if (entity instanceof Player) {
			return;
		}
		if (!(killer instanceof Player)) {
			return;
		}
		if (vanishNoPacket.isPlayerInvisible((Player) killer) || ((Player) killer).hasPermission("jukealert.vanish")) {
			return;
		}
		Player player = (Player) killer;
		UUID accountId = player.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(player.getWorld(), player.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.shouldLog()) {
				continue;
			}
			if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				if (checkProximity(snitch, accountId)) {
					plugin.getJaLogger().logSnitchEntityKill(snitch, player, entity);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerDestroyCart(VehicleDestroyEvent event) {

		Vehicle vehicle = event.getVehicle();
		Entity killer = event.getAttacker();
		if (killer == null || !(killer instanceof Player)) {
			return;
		}
		if (vanishNoPacket.isPlayerInvisible((Player) killer) || ((Player) killer).hasPermission("jukealert.vanish")) {
			return;
		}
		Player player = (Player) killer;
		UUID accountId = player.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(player.getWorld(), player.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.shouldLog()) {
				continue;
			}
			if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				if (checkProximity(snitch, accountId)) {
					plugin.getJaLogger().logSnitchCartDestroyed(snitch, player, vehicle);
				}
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void playerMountEntity(EntityMountEvent e) {

		if (e.getEntityType() != EntityType.PLAYER) {
			return;
		}
		Player p = (Player) e.getEntity();
		Entity mount = e.getMount();
		if (vanishNoPacket.isPlayerInvisible(p) || p.hasPermission("jukealert.vanish")) {
			return;
		}
		UUID accountId = p.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(p.getWorld(), p.getLocation());
		 for (Snitch snitch : snitches) {
			 if (!snitch.shouldLog()) {
				 continue;
			 }
			 if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				 if (checkProximity(snitch, accountId)) {
					 plugin.getJaLogger().logSnitchMount(snitch, p, mount);
				 }
			 }
		 }
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void playerDismountEntity(EntityDismountEvent e) {

		if (e.getEntityType() != EntityType.PLAYER) {
			return;
		}
		Player p = (Player) e.getEntity();
		Entity mount = e.getDismounted();
		if (vanishNoPacket.isPlayerInvisible(p) || p.hasPermission("jukealert.vanish")) {
			return;
		}
		UUID accountId = p.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(p.getWorld(), p.getLocation());
		 for (Snitch snitch : snitches) {
			 if (!snitch.shouldLog()) {
				 continue;
			 }
			 if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				 if (checkProximity(snitch, accountId)) {
					 plugin.getJaLogger().logSnitchDismount(snitch, p, mount);
				 }
			 }
		 }
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerKillPlayer(PlayerDeathEvent event) {

		if (!(event.getEntity().getKiller() instanceof Player)) {
			return;
		}
		Player killed = event.getEntity();
		Player killer = killed.getKiller();
		if (vanishNoPacket.isPlayerInvisible(killer) || killer.hasPermission("jukealert.vanish")) {
			return;
		}
		UUID accountId = killer.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(killed.getWorld(), killed.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.shouldLog()) {
				continue;
			}
			if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				if (checkProximity(snitch, accountId) || checkProximity(snitch, accountId)) {
					plugin.getJaLogger().logSnitchPlayerKill(snitch, killer, killed);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockIgniteEvent(BlockIgniteEvent event) {

		if (event.isCancelled()) {
			return;
		}
		if (event.getPlayer() == null) {
			return;
		}
		Player player = event.getPlayer();
		if (vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish")) {
			return;
		}
		Block block = event.getBlock();
		UUID accountId = player.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.shouldLog()) {
				continue;
			}
			if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				if (checkProximity(snitch, accountId)) {
					plugin.getJaLogger().logSnitchIgnite(snitch, player, block);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBurnEvent(BlockBurnEvent event) {

		if (event.isCancelled()) {
			return;
		}
		Block block = event.getBlock();
		Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.shouldLog()) {
				continue;
			}
			if (snitch.getGroup() != null) {
				continue;
			}
			plugin.getJaLogger().logSnitchBlockBurn(snitch, block);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerBreakBlock(BlockBreakEvent event) {

		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		if (vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish")) {
			return;
		}
		Block block = event.getBlock();
		UUID accountId = player.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.shouldLog()) {
				continue;
			}
			if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				if (checkProximity(snitch, accountId)) {
					plugin.getJaLogger().logSnitchBlockBreak(snitch, player, block);
					Location west = new Location(block.getWorld(), snitch.getX() - 1, snitch.getY(), snitch.getZ());
					toggleLeverIfApplicable(snitch, west, true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerPlaceBlock(BlockPlaceEvent event) {

		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		if (vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish")) {
			return;
		}
		Block block = event.getBlock();
		UUID accountId = player.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.shouldLog()) {
				continue;
			}
			if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				if (checkProximity(snitch, accountId)) {
					plugin.getJaLogger().logSnitchBlockPlace(snitch, player, block);

					Location east = new Location(block.getWorld(), snitch.getX() + 1, snitch.getY(), snitch.getZ());
					toggleLeverIfApplicable(snitch, east, true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerFillBucket(PlayerBucketFillEvent event) {

		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		if (vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish")) {
			return;
		}
		Block block = event.getBlockClicked();
		UUID accountId = player.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.shouldLog()) {
				continue;
			}
			if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				if (checkProximity(snitch, accountId)) {
					plugin.getJaLogger().logSnitchBucketFill(snitch, player, block, event.getBucket());
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void playerEmptyBucket(PlayerBucketEmptyEvent event) {

		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		if (vanishNoPacket.isPlayerInvisible(player) || player.hasPermission("jukealert.vanish")) {
			return;
		}
		Block block = event.getBlockClicked();
		UUID accountId = player.getUniqueId();
		Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
		for (Snitch snitch : snitches) {
			if (!snitch.shouldLog()) {
				continue;
			}
			if (!immuneToSnitch(snitch, accountId) || isDebugging()) {
				if (checkProximity(snitch, accountId)) {
					plugin.getJaLogger().logSnitchBucketEmpty(snitch, player, block.getLocation(), event.getBucket());
				}
			}
		}
	}
}
