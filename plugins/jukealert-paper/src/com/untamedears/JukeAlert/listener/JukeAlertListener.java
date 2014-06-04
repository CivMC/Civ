package com.untamedears.JukeAlert.listener;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.external.VanishNoPacket;
import com.untamedears.JukeAlert.manager.PlayerManager;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

import static com.untamedears.JukeAlert.util.Utility.doesSnitchExist;
import static com.untamedears.JukeAlert.util.Utility.isOnSnitch;
import static com.untamedears.JukeAlert.util.Utility.isPartialOwnerOfSnitch;
import static com.untamedears.JukeAlert.util.Utility.isDebugging;
import static com.untamedears.JukeAlert.util.Utility.notifyGroup;

import com.untamedears.citadel.SecurityLevel;
import com.untamedears.citadel.Utility;
import com.untamedears.citadel.access.AccessDelegate;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.IReinforcement;
import com.untamedears.citadel.entity.PlayerReinforcement;
import com.untamedears.citadel.events.CreateReinforcementEvent;
import com.untamedears.citadel.events.GroupChangeEvent;
import com.untamedears.citadel.events.GroupChangeType;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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

public class JukeAlertListener implements Listener {

    private final JukeAlert plugin = JukeAlert.getInstance();
    SnitchManager snitchManager = plugin.getSnitchManager();
    PlayerManager playerManager = plugin.getPlayerManager();
    private final Map<String, Set<Snitch>> playersInSnitches = new TreeMap<String, Set<Snitch>>();
	private final ArrayList<Location> previousLocations = new ArrayList<Location>();
    private final VanishNoPacket vanishNoPacket = new VanishNoPacket();

    private boolean checkProximity(Snitch snitch, String playerName) {
        Set<Snitch> inList = playersInSnitches.get(playerName);
        if (inList == null) {
            inList = new TreeSet<Snitch>();
            playersInSnitches.put(playerName, inList);
        }
        return inList.contains(snitch);
    }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerJoinEvent(PlayerJoinEvent event) {
		Player player = event.getPlayer();
        if (vanishNoPacket.isPlayerInvisible(player)) {
            return;
        }
		String playerName = player.getName();
		Set<Snitch> inList = new TreeSet<Snitch>();
		playersInSnitches.put(player.getName(), inList);

		Location location = player.getLocation();
		World world = location.getWorld();
		Set<Snitch> snitches = snitchManager.findSnitches(world, location);
		for (Snitch snitch : snitches) {
			if (!isOnSnitch(snitch, playerName)) {
                snitch.imposeSnitchTax();
				inList.add(snitch);
                notifyGroup(
                    snitch,
                    ChatColor.AQUA + " * " + playerName + " logged in to snitch at " + snitch.getName()
                    + " [" + snitch.getX() + " " + snitch.getY() + " " + snitch.getZ() + "]");
                if (snitch.shouldLog()) {
                    plugin.getJaLogger().logSnitchLogin(snitch, location, player);
                }
			}
		}
	}

    public void handlePlayerExit(PlayerEvent event) {
    	Player player = event.getPlayer();
    	
        if (vanishNoPacket.isPlayerInvisible(player)) {
            return;
        }
		String playerName = player.getName();
		playersInSnitches.remove(playerName);

		Location location = player.getLocation();
		World world = location.getWorld();
		Set<Snitch> snitches = snitchManager.findSnitches(world, location);
		for (Snitch snitch : snitches) {
			if (!isOnSnitch(snitch, playerName)) {
                snitch.imposeSnitchTax();
                notifyGroup(
                    snitch,
                    ChatColor.AQUA + " * " + playerName + " logged out in snitch at " + snitch.getName()
                    + " [" + snitch.getX() + " " + snitch.getY() + " " + snitch.getZ() + "]");
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

    @EventHandler(ignoreCancelled = true)
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
            if (!Utility.isReinforced(loc)) {
                player.sendMessage(ChatColor.YELLOW + "You've placed a jukebox; reinforce it to register it as a snitch.");
            }
        } else if (block.getType().equals(Material.NOTE_BLOCK)) {
            if (!Utility.isReinforced(loc)) {
                player.sendMessage(ChatColor.YELLOW + "You've placed a noteblock; reinforce it to register it as an entry snitch.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void reinforceSnitchBlock(CreateReinforcementEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (block.getType().equals(Material.JUKEBOX)) {

            Player player = event.getPlayer();
            Location loc = block.getLocation();
            AccessDelegate access = AccessDelegate.getDelegate(block);
            IReinforcement rei = event.getReinforcement();
            if (rei instanceof PlayerReinforcement) {
                PlayerReinforcement reinforcement = (PlayerReinforcement) rei;
                Faction owner = reinforcement.getOwner();
                if (owner == null) {
                    JukeAlert.getInstance().log(String.format(
                        "No group on rein (%s): %s", reinforcement.getId(), reinforcement.getOwnerName()));
                }
                if (reinforcement.getSecurityLevel().equals(SecurityLevel.GROUP)) {
                    Snitch snitch;
                    if (snitchManager.getSnitch(loc.getWorld(), loc) != null) {
                        snitch = snitchManager.getSnitch(loc.getWorld(), loc);
                        plugin.getJaLogger().updateSnitchGroup(snitchManager.getSnitch(loc.getWorld(), loc), owner.getName());
                        snitchManager.removeSnitch(snitch);
                        snitch.setGroup(owner);
                    } else {
                        snitch = new Snitch(loc, owner, true);
                        plugin.getJaLogger().logSnitchPlace(player.getWorld().getName(), owner.getName(), "", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), true);
                        snitch.setId(plugin.getJaLogger().getLastSnitchID());
                        plugin.getJaLogger().increaseLastSnitchID();
                    }
                    snitchManager.addSnitch(snitch);

                    player.sendMessage(ChatColor.AQUA + "You've created a snitch block registered to the group " + owner.getName() + ".  To name your snitch, type /janame.");
                } else {
                    Snitch snitch;
                    if (snitchManager.getSnitch(loc.getWorld(), loc) != null) {
                        snitch = snitchManager.getSnitch(loc.getWorld(), loc);
                        plugin.getJaLogger().updateSnitchGroup(snitchManager.getSnitch(loc.getWorld(), loc), owner.getFounder());
                        snitchManager.removeSnitch(snitch);
                        snitch.setGroup(owner);
                    } else {
                        snitch = new Snitch(loc, owner, true);
                        plugin.getJaLogger().logSnitchPlace(player.getWorld().getName(), owner.getFounder(), "", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), true);
                        snitch.setId(plugin.getJaLogger().getLastSnitchID());
                        plugin.getJaLogger().increaseLastSnitchID();
                    }
                    snitchManager.addSnitch(snitch);

                    player.sendMessage(ChatColor.AQUA + "You've created a private snitch block; reinforce it with a group to register members.  To name your snitch, type /janame.");
                }
            }
        } else if (block.getType().equals(Material.NOTE_BLOCK)) {
            Player player = event.getPlayer();
            Location loc = block.getLocation();
            AccessDelegate access = AccessDelegate.getDelegate(block);
            IReinforcement rei = event.getReinforcement();
            if (rei instanceof PlayerReinforcement) {
                PlayerReinforcement reinforcement = (PlayerReinforcement) rei;
                Faction owner = reinforcement.getOwner();
                if (owner == null) {
                    JukeAlert.getInstance().log(String.format(
                        "No group on rein (%s): %s", reinforcement.getId(), reinforcement.getOwnerName()));
                }
                if (reinforcement.getSecurityLevel().equals(SecurityLevel.GROUP)) {
                    Snitch snitch;
                    if (snitchManager.getSnitch(loc.getWorld(), loc) != null) {
                        snitch = snitchManager.getSnitch(loc.getWorld(), loc);
                        plugin.getJaLogger().updateSnitchGroup(snitchManager.getSnitch(loc.getWorld(), loc), owner.getName());
                        snitchManager.removeSnitch(snitch);
                        snitch.setGroup(owner);
                    } else {
                        snitch = new Snitch(loc, owner, false);
                        plugin.getJaLogger().logSnitchPlace(player.getWorld().getName(), owner.getName(), "", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), false);
                        snitch.setId(plugin.getJaLogger().getLastSnitchID());
                        plugin.getJaLogger().increaseLastSnitchID();
                    }
                    snitchManager.addSnitch(snitch);

                    player.sendMessage(ChatColor.AQUA + "You've created an entry snitch registered to the group " + owner.getName() + ".  To name your entry snitch, type /janame.");
                } else {
                    Snitch snitch;
                    if (snitchManager.getSnitch(loc.getWorld(), loc) != null) {
                        snitch = snitchManager.getSnitch(loc.getWorld(), loc);
                        plugin.getJaLogger().updateSnitchGroup(snitchManager.getSnitch(loc.getWorld(), loc), owner.getFounder());
                        snitchManager.removeSnitch(snitch);
                        snitch.setGroup(owner);
                    } else {
                        snitch = new Snitch(loc, owner, false);
                        plugin.getJaLogger().logSnitchPlace(player.getWorld().getName(), owner.getFounder(), "", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), false);
                        snitch.setId(plugin.getJaLogger().getLastSnitchID());
                        plugin.getJaLogger().increaseLastSnitchID();
                    }
                    snitchManager.addSnitch(snitch);

                    player.sendMessage(ChatColor.AQUA + "You've created a private entry snitch; reinforce it with a group to register members.  To name your entry snitch, type /janame.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGroupDeletion(GroupChangeEvent event) {
        if (event.getType() != GroupChangeType.DELETE) {
            return;
        }
        String groupName = event.getFactionName();
        Set<Snitch> removeSet = new TreeSet<Snitch>();
        for (Snitch snitch : snitchManager.getAllSnitches()) {
            final Faction snitchGroup = snitch.getGroup();
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
                plugin.getJaLogger().logSnitchBreak(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            }
            snitchManager.removeSnitch(snitch);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void breakSnitchBlock(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (!block.getType().equals(Material.JUKEBOX)) {
            return;
        }
        if (vanishNoPacket.isPlayerInvisible(event.getPlayer())) {
            return;
        }
        Location loc = block.getLocation();
        if (snitchManager.getSnitch(loc.getWorld(), loc) != null) {
            Snitch snitch = snitchManager.getSnitch(loc.getWorld(), loc);
            if (snitch.shouldLog()) {
                plugin.getJaLogger().logSnitchBreak(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            }
            snitchManager.removeSnitch(snitch);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void enterSnitchProximity(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()
                && from.getWorld().equals(to.getWorld())) {
            // Player didn't move by at least one block.
            return;
        }
        Player player = event.getPlayer();
        if (vanishNoPacket.isPlayerInvisible(player)) {
            return;
        }
        String playerName = player.getName();
        Location location = player.getLocation();
        World world = location.getWorld();
        Set<Snitch> inList = playersInSnitches.get(playerName);
        if (inList == null) {
            inList = new TreeSet<Snitch>();
            playersInSnitches.put(player.getName(), inList);
        }
        Set<Snitch> snitches = snitchManager.findSnitches(world, location);
        for (Snitch snitch : snitches) {
        	if (doesSnitchExist(snitch, true)) {
        		
        		if (isPartialOwnerOfSnitch(snitch, playerName)) {
        			if (!inList.contains(snitch)) {
	                	inList.add(snitch);
                        plugin.getJaLogger().logSnitchVisit(snitch);
        			}
        		}
	            
        		if ((!isOnSnitch(snitch, playerName) || isDebugging())) {
	                if (!inList.contains(snitch)) {
						snitch.imposeSnitchTax();
	                	inList.add(snitch);
						
						notifyGroup(
							snitch,
							ChatColor.AQUA + " * " + playerName + " entered snitch at " + snitch.getName()
							+ " [" + snitch.getX() + " " + snitch.getY() + " " + snitch.getZ() + "]");
							
	                    if (snitch.shouldLog()) {
	                        plugin.getJaLogger().logSnitchEntry(snitch, location, player);
	                    }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpenEvent(InventoryOpenEvent e) {
        Player player = (Player) e.getPlayer();
        if (e.isCancelled()) {
            return;
        }
        if (vanishNoPacket.isPlayerInvisible(player)) {
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
        	Set<Snitch> snitches = snitchManager.findSnitches(player.getWorld(), player.getLocation());
        	for (Snitch snitch : snitches) {
                if (!snitch.shouldLog()) {
                    continue;
                }
                
        		if (!isOnSnitch(snitch, player.getName()) || isDebugging()) {
        			if (checkProximity(snitch, player.getName())) {
        				plugin.getJaLogger().logUsed(snitch, player, block);
        			}
        		}
        	}
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerKillEntity(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        LivingEntity killer = entity.getKiller();
        // TODO: This should never be true, bug?
        if (entity instanceof Player) {
            return;
        }
        if (!(killer instanceof Player)) {
            return;
        }
        if (vanishNoPacket.isPlayerInvisible((Player) killer)) {
            return;
        }
        Player player = (Player) killer;
        Set<Snitch> snitches = snitchManager.findSnitches(player.getWorld(), player.getLocation());
        for (Snitch snitch : snitches) {
            if (!snitch.shouldLog()) {
                continue;
            }
            if (!isOnSnitch(snitch, player.getName()) || isDebugging()) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchEntityKill(snitch, player, entity);
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
        if (vanishNoPacket.isPlayerInvisible(killer)) {
            return;
        }
        Set<Snitch> snitches = snitchManager.findSnitches(killed.getWorld(), killed.getLocation());
        for (Snitch snitch : snitches) {
            if (!snitch.shouldLog()) {
                continue;
            }
            if (!isOnSnitch(snitch, killer.getName()) || isDebugging()) {
                if (checkProximity(snitch, killed.getName()) || checkProximity(snitch, killer.getName())) {
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
        if (vanishNoPacket.isPlayerInvisible(player)) {
            return;
        }
        Block block = event.getBlock();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (!snitch.shouldLog()) {
                continue;
            }
            if (!isOnSnitch(snitch, player.getName()) || isDebugging()) {
                if (checkProximity(snitch, player.getName())) {
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
        if (vanishNoPacket.isPlayerInvisible(player)) {
            return;
        }
        Block block = event.getBlock();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (!snitch.shouldLog()) {
                continue;
            }
            if (!isOnSnitch(snitch, player.getName()) || isDebugging()) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchBlockBreak(snitch, player, block);
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
        if (vanishNoPacket.isPlayerInvisible(player)) {
            return;
        }
        Block block = event.getBlock();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (!snitch.shouldLog()) {
                continue;
            }
            if (!isOnSnitch(snitch, player.getName()) || isDebugging()) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchBlockPlace(snitch, player, block);
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
        if (vanishNoPacket.isPlayerInvisible(player)) {
            return;
        }
        Block block = event.getBlockClicked();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (!snitch.shouldLog()) {
                continue;
            }
            if (!isOnSnitch(snitch, player.getName()) || isDebugging()) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchBucketFill(snitch, player, block);
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
        if (vanishNoPacket.isPlayerInvisible(player)) {
            return;
        }
        Block block = event.getBlockClicked();
        Set<Snitch> snitches = snitchManager.findSnitches(block.getWorld(), block.getLocation());
        for (Snitch snitch : snitches) {
            if (!snitch.shouldLog()) {
                continue;
            }
            if (!isOnSnitch(snitch, player.getName()) || isDebugging()) {
                if (checkProximity(snitch, player.getName())) {
                    plugin.getJaLogger().logSnitchBucketEmpty(snitch, player, block.getLocation(), player.getItemInHand());
                }
            }
        }
    }
}
