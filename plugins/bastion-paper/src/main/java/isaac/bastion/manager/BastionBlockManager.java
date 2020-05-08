package isaac.bastion.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.Permissions;
import isaac.bastion.event.BastionDamageEvent;
import isaac.bastion.event.BastionDamageEvent.Cause;
import isaac.bastion.storage.BastionBlockStorage;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.locations.QTBox;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class BastionBlockManager {
	private static Random rng = new Random();

	private HashMap<UUID, HashMap<String, Long>> cooldowns = new HashMap<>();
	private BastionBlockStorage storage;

	public BastionBlockManager() {
		storage = Bastion.getBastionStorage();
	}

	// For block places
	public void erodeFromPlace(Player player, Set<BastionBlock> blocking) {
		erodeFromAction(player, blocking, Cause.BLOCK_PLACED);
	}

	// For pearls
	public void erodeFromTeleport(Player player, Set<BastionBlock> blocking) {
		erodeFromAction(player, blocking, Cause.PEARL);
	}

	/**
	 * Common handler for erosion.
	 */
	private void erodeFromAction(Player player, Set<BastionBlock> blocking, Cause cause) {
		HashMap<BastionType, Set<BastionBlock>> typeMap = new HashMap<>();
		for (BastionBlock block : blocking) {
			if (cause == Cause.PEARL && !block.getType().isBlockPearls())
				continue;
			Set<BastionBlock> set = typeMap.get(block.getType());
			if (set == null) {
				set = new HashSet<>();
				typeMap.put(block.getType(), set);
			}
			set.add(block);
		}

		for (BastionType type : typeMap.keySet()) {
			if (onCooldown(player.getUniqueId(), type))
				continue;
			Set<BastionBlock> bastions = typeMap.get(type);
			Set<BastionBlock> otherNearby = shouldStopBlockByBlockingBastion(null,
					bastions.stream().map(b -> b.getLocation().getBlock()).collect(Collectors.toSet()),
					player.getUniqueId());
			//dont damage those already directly damaged
			otherNearby.removeAll(bastions);
			for (BastionBlock bastion : otherNearby) {
				if (bastion.getType() != type) {
					continue;
				}
				boolean closeEnough = false;
				for (BastionBlock comparesBas : bastions) {
					if (Math.abs(comparesBas.getLocation().getBlockX() - bastion.getLocation().getBlockX()) > type
							.getProximityDamageRange()) {
						continue;
					}
					if (Math.abs(comparesBas.getLocation().getBlockX() - bastion.getLocation().getBlockX()) > type
							.getProximityDamageRange()) {
						continue;
					}
					closeEnough = true;
					break;
				}
				if (closeEnough) {
					double damage = cause == Cause.BLOCK_PLACED ? bastion.getErosionFromBlock() : bastion.getErosionFromPearl();
					damage *= type.getProximityDamageFactor();
					BastionDamageEvent event = new BastionDamageEvent(bastion, player, cause, damage);
					Bukkit.getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						continue;
					}
					bastion.erode(damage);
				}
			}

			if (type.getBlocksToErode() < 0) { // erode all
				for (BastionBlock bastion : bastions) {
					double damage = cause == Cause.BLOCK_PLACED ? bastion.getErosionFromBlock()
							: bastion.getErosionFromPearl();
					BastionDamageEvent event = new BastionDamageEvent(bastion, player, cause, damage);
					Bukkit.getPluginManager().callEvent(event);
					if (event.isCancelled())
						continue;
					bastion.erode(damage);
				}
			} else if (type.getBlocksToErode() > 0) { // erode some
				List<BastionBlock> ordered = new LinkedList<>(bastions);
				for (int i = 0; i < ordered.size() && i < type.getBlocksToErode(); i++) {
					int erode = rng.nextInt(ordered.size());
					BastionBlock bastion = ordered.get(erode);
					double damage = cause == Cause.BLOCK_PLACED ? bastion.getErosionFromBlock()
							: bastion.getErosionFromPearl();
					BastionDamageEvent event = new BastionDamageEvent(bastion, player, cause, damage);
					Bukkit.getPluginManager().callEvent(event);
					if (event.isCancelled())
						continue;
					bastion.erode(damage);
					ordered.remove(erode);
				}
			}
		}
	}

	public boolean onCooldown(UUID player, BastionType type) {
		if (!cooldowns.containsKey(player)) {
			cooldowns.put(player, new HashMap<String, Long>());
			return false;
		}
		if (!cooldowns.get(player).containsKey(type.getName())) {
			cooldowns.get(player).put(type.getName(), System.currentTimeMillis());
			return false;
		}
		long last_placed = cooldowns.get(player).get(type.getName());

		if ((System.currentTimeMillis() - last_placed) < type.getPlacementCooldown()) {
			return true;
		} else {
			cooldowns.get(player).put(type.getName(), System.currentTimeMillis());
		}

		return false;
	}

	/**
	 * handles all block based events in a general way
	 * 
	 * @param origin
	 * @param result
	 * @param player
	 * @return
	 */
	public Set<BastionBlock> shouldStopBlock(Block origin, Set<Block> result, UUID player) {
		if (player != null) {
			Player playerB = Bukkit.getPlayer(player);
			if (playerB != null && playerB.hasPermission("Bastion.bypass"))
				return new CopyOnWriteArraySet<>();
		}

		Set<BastionBlock> toReturn = new HashSet<>();
		Set<UUID> accessors = new HashSet<>();
		if (player != null) {
			accessors.add(player);
		}

		if (origin != null) {
			Reinforcement reinforcement = Citadel.getInstance().getReinforcementManager().getReinforcement(origin);
			if (reinforcement != null) {
				accessors.add(reinforcement.getGroup().getOwner());

				for (BastionBlock bastion : this.getBlockingBastions(origin.getLocation())) {
					accessors.add(bastion.getOwner());
				}
			}
		}

		for (Block block : result) {
			toReturn.addAll(getBlockingBastions(block.getLocation(), accessors));
		}

		return toReturn;
	}

	/**
	 * handles all block based events in a general way
	 * 
	 * @param origin
	 * @param result
	 * @param player
	 * @return
	 */
	public Set<BastionBlock> shouldStopBlockByBlockingBastion(Block origin, Set<Block> result, UUID player) {
		Set<BastionBlock> preblocking = shouldStopBlock(origin, result, player);

		// Clear non-blocking

		if (preblocking.size() == 0)
			return preblocking; // don't allocate if nothing to do.

		Set<BastionBlock> blocking = new HashSet<>();

		for (BastionBlock bastion : preblocking) {
			if (!bastion.getType().isOnlyDirectDestruction()) {
				blocking.add(bastion);
			}
		}

		return blocking;
	}

	// TODO: This is potentially inefficient: new LL, plus shuffle, all to
	// "random-choose" a bastion?
	// Evaluable if forLocation returns a new Set; if so, just directly mess with
	// the set.
	private BastionBlock getBlockingBastion(Location loc, Player player) {
		Set<? extends QTBox> possible = storage.forLocation(loc);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom = new LinkedList<>((Set<BastionBlock>) possible);
		Collections.shuffle(possibleRandom);

		for (BastionBlock bastion : possibleRandom) {
			if (!bastion.canPlace(player) && bastion.inField(loc)) {
				return bastion;
			}
		}
		return null;
	}

	public BastionBlock getBlockingBastion(Location loc) {
		Set<? extends QTBox> possible = storage.forLocation(loc);

		@SuppressWarnings("unchecked")
		List<BastionBlock> possibleRandom = new LinkedList<>((Set<BastionBlock>) possible);
		Collections.shuffle(possibleRandom);

		for (BastionBlock bastion : possibleRandom) {
			if (bastion.inField(loc)) {
				return bastion;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Set<BastionBlock> getBlockingBastions(Location loc) {
		Set<? extends QTBox> boxes = storage.forLocation(loc);
		Set<BastionBlock> bastions = null;

		if (boxes.size() > 0 && boxes.iterator().next() instanceof BastionBlock) {
			bastions = (Set<BastionBlock>) boxes;
		}

		if (bastions == null) {
			return new CopyOnWriteArraySet<>();
		}

		Iterator<BastionBlock> i = bastions.iterator();

		while (i.hasNext()) {
			BastionBlock bastion = i.next();
			if (!bastion.inField(loc)) {
				i.remove();
			}
		}
		;
		return bastions;
	}

	@SuppressWarnings("unchecked")
	public Set<BastionBlock> getBlockingBastions(Location loc, Player player, PermissionType perm) {
		Set<? extends QTBox> boxes = storage.forLocation(loc);
		Set<BastionBlock> bastions = null;

		if (boxes.size() > 0 && boxes.iterator().next() instanceof BastionBlock) {
			bastions = (Set<BastionBlock>) boxes;
		}

		if (bastions == null) {
			return new CopyOnWriteArraySet<>();
		}

		Iterator<BastionBlock> i = bastions.iterator();
		while (i.hasNext()) {
			BastionBlock bastion = i.next();
			if (!bastion.inField(loc) || bastion.permAccess(player, perm)) {
				i.remove();
			}
		}

		return bastions;
	}

	@SuppressWarnings("unchecked")
	private Set<BastionBlock> getBlockingBastions(Location loc, Set<UUID> players) {
		Set<? extends QTBox> boxes = storage.forLocation(loc);
		Set<BastionBlock> bastions = null;

		if (boxes.size() != 0) {
			bastions = (Set<BastionBlock>) boxes;
		}

		if (bastions == null) {
			return new CopyOnWriteArraySet<>();
		}

		Iterator<BastionBlock> i = bastions.iterator();
		while (i.hasNext()) {
			BastionBlock bastion = i.next();
			if (!bastion.inField(loc) || bastion.oneCanPlace(players)) {
				i.remove();
			}
		}

		return bastions;
	}

	public boolean canList(Player player, Integer groupId) {
		if (player == null || groupId == null)
			return false;

		Group group = GroupManager.getGroup(groupId);

		PermissionType permission = PermissionType.getPermission(Permissions.BASTION_LIST);

		return group != null && NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(), permission);
	}

	public TextComponent bastionDeletedMessageComponent(BastionBlock bastion) {
		TextComponent component = new TextComponent(ChatColor.GREEN + "Bastion deleted");

		String hoverText = bastion.getHoverText();
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));

		return component;
	}

	public TextComponent bastionCreatedMessageComponent(Location location) {
		BastionBlock bastion = storage.getBastionBlock(location);

		TextComponent component = new TextComponent(ChatColor.GREEN + "Bastion block created");

		String hoverText = bastion.getHoverText();
		component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));

		return component;
	}

	public TextComponent infoMessageComponent(boolean dev, Block block, Block clicked, Player player) {
		BastionBlock bastion = storage.getBastionBlock(clicked.getLocation()); // Get the bastion at the location
																				// clicked.

		if (bastion == null)
			return new TextComponent(infoMessage(dev, block, clicked, player));

		TextComponent component = new TextComponent(bastion.infoMessage(dev));

		if (canList(player, bastion.getListGroupId())) {
			String hoverText = bastion.getHoverText();
			component.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
		}

		return component;
	}

	public String infoMessage(boolean dev, Block block, Block clicked, Player player) {
		BastionBlock bastion = storage.getBastionBlock(clicked.getLocation()); // Get the bastion at the location
																				// clicked.

		if (bastion != null) { // See if anything was found
			return bastion.infoMessage(dev); // If there is actually something there tell the player about it.
		}

		StringBuilder sb = new StringBuilder();

		bastion = getBlockingBastion(block.getLocation(), player);
		if (bastion == null) {
			bastion = getBlockingBastion(block.getLocation());
			if (bastion != null) {
				sb.append(ChatColor.GREEN).append("A Bastion Block prevents others from building");
			} else {
				sb.append(ChatColor.YELLOW).append("No Bastion Block");
			}
		} else {
			if (bastion.getType().isOnlyDirectDestruction()) {
				sb.append(ChatColor.BLUE).append("Bastion ignores blocks");
			} else {
				Group allowedGroup = Bastion.getGroupManager().findFirstAllowedGroup(player, bastion);

				if (allowedGroup != null) {
					sb.append(ChatColor.YELLOW)
							.append("A Bastion Block allows you to build using group [" + allowedGroup.getName() + "]");
				} else {
					sb.append(ChatColor.RED).append("A Bastion Block prevents you building");
				}
			}
		}

		if (dev && bastion != null) {
			sb.append(ChatColor.GRAY).append("\n").append(bastion.toString());
		}

		return sb.toString();
	}

	public Boolean changeBastionGroup(Player player, Reinforcement reinforcement, Location location) {
		BastionBlock bastion = storage.getBastionBlock(location);

		if (bastion == null)
			return null;

		Reinforcement oldReinf = bastion.getReinforcement();

		if (NameAPI.getGroupManager().hasAccess(reinforcement.getGroup(), player.getUniqueId(),
				PermissionType.getPermission(Permissions.BASTION_PLACE))
				&& NameAPI.getGroupManager().hasAccess(oldReinf.getGroup(), player.getUniqueId(),
						PermissionType.getPermission(Permissions.BASTION_PLACE))) {
			storage.changeBastionGroup(bastion);
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}

	public void getBastionsByGroupIds(List<Integer> groupIds, List<BastionBlock> result) {
		storage.getBastionsByGroupIds(groupIds, result);
	}
}
