package vg.civcraft.mc.namelayer.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.events.PromotePlayerEvent;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class MemberViewGUI extends GroupGUI {

	private boolean showMembers;
	private boolean showMods;
	private boolean showAdmins;
	private boolean showOwners;

	private int currentPage;
	private NameLayerGUI parentGUI;

	public MemberViewGUI(Player p, Group g, NameLayerGUI parentGUI) {
		super(g, p);
		this.parentGUI = parentGUI;
		showMembers = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MEMBERS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
		showMods = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MODS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
		showAdmins = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("ADMINS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
		showOwners = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("OWNER"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
		currentPage = 0;
		showScreen();
	}

	private void showScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		if (!hasPermissionToViewAnything()) {
			p.sendMessage(ChatColor.RED
					+ "You lost the required permissions to list members of this group");
			return;
		}
		ClickableInventory ci = new ClickableInventory(54, g.getName());
		final List<UUID> members = getToDisplay();
		if (members.size() < 45 * currentPage) {
			// would show an empty page, so go to previous
			currentPage--;
			showScreen();
		}
		// fill gui
		for (int i = 45 * currentPage; i < 45 * (currentPage + 1)
				&& i < members.size(); i++) {
			final UUID currentId = members.get(i);
			Clickable c;
			switch (g.getPlayerType(currentId)) {
			case MEMBERS:
				c = constructOverviewClickable(Material.LEATHER_CHESTPLATE,
						currentId, PlayerType.MEMBERS);
				break;
			case MODS:
				c = constructOverviewClickable(Material.GOLD_CHESTPLATE,
						currentId, PlayerType.MODS);
				break;
			case ADMINS:
				c = constructOverviewClickable(Material.IRON_CHESTPLATE,
						currentId, PlayerType.ADMINS);
				break;
			case OWNER:
				c = constructOverviewClickable(Material.DIAMOND_CHESTPLATE,
						currentId, PlayerType.OWNER);
				break;
			default:
				// should never happen
				continue;
			}
			ci.setSlot(c, i - (45 * currentPage));
		}
		// back button
		if (currentPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ISUtils.setName(back, ChatColor.GOLD + "Go to previous page");
			Clickable baCl = new Clickable(back) {

				@Override
				public void clicked(Player arg0) {
					if (currentPage > 0) {
						currentPage--;
					}
					showScreen();
				}
			};
			ci.setSlot(baCl, 45);
		}
		// next button
		if ((45 * (currentPage + 1)) <= members.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ISUtils.setName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= members.size()) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, 53);
		}

		// options
		ci.setSlot(setupMemberTypeToggle(PlayerType.MEMBERS, showMembers), 46);

		ci.setSlot(setupMemberTypeToggle(PlayerType.MODS, showMods), 47);

		ci.setSlot(setupMemberTypeToggle(PlayerType.ADMINS, showAdmins), 51);

		ci.setSlot(setupMemberTypeToggle(PlayerType.OWNER, showOwners), 52);

		// to previous gui
		ItemStack backToOverview = new ItemStack(Material.WOOD_DOOR);
		ISUtils.setName(backToOverview, ChatColor.GOLD + "Go back");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				parentGUI.showMemberManageScreen();
			}
		}, 49);
		ci.showInventory(p);
	}

	private Clickable setupMemberTypeToggle(final PlayerType pType,
			boolean initialState) {
		boolean canEdit = gm.hasAccess(g, p.getUniqueId(),
				getAccordingPermission(pType))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
		ItemStack is = MenuUtils.toggleButton(initialState, ChatColor.GOLD
				+ "Show " + getDirectRankName(pType) + "s", canEdit);
		Clickable c;
		if (canEdit) {
			c = new Clickable(is) {

				@Override
				public void clicked(Player arg0) {
					switch (pType) {
					case MEMBERS:
						showMembers = !showMembers;
						break;
					case MODS:
						showMods = !showMods;
						break;
					case ADMINS:
						showAdmins = !showAdmins;
						break;
					case OWNER:
						showOwners = !showOwners;
						break;
					}
					currentPage = 0;
					showScreen();
				}
			};
		} else {
			c = new DecorationStack(is);
		}

		return c;

	}

	private Clickable constructOverviewClickable(Material material,
			final UUID toDisplay, PlayerType rank) {
		Clickable c;
		ItemStack is = new ItemStack(Material.DIAMOND_CHESTPLATE);
		ItemMeta im = is.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		is.setItemMeta(im);
		ISUtils.setName(is, ChatColor.GOLD + NameAPI.getCurrentName(toDisplay));
		if (g.isOwner(toDisplay)) { // special case for primary owner
			ISUtils.addLore(is, ChatColor.AQUA + "Rank: Primary Owner");
			ISUtils.addLore(is, ChatColor.RED + "You don't have permission",
					ChatColor.RED + "to modify the rank of this player");
			c = new DecorationStack(is);

		} else {
			ISUtils.addLore(is, ChatColor.AQUA + "Rank: "
					+ getRankName(toDisplay));
			if (gm.hasAccess(g, p.getUniqueId(), getAccordingPermission(rank))) {
				ISUtils.addLore(is, ChatColor.GREEN
						+ "Click to modify this player's", ChatColor.GREEN
						+ "rank or to remove them");
				c = new Clickable(is) {

					@Override
					public void clicked(Player arg0) {
						showDetail(toDisplay);
					}
				};
			} else {
				ISUtils.addLore(is,
						ChatColor.RED + "You don't have permission",
						ChatColor.RED + "to modify the rank of this player");
				c = new DecorationStack(is);
			}
		}
		return c;
	}

	public void showDetail(final UUID uuid) {
		if (!validGroup()) {
			showScreen();
			return;
		}
		ClickableInventory.forceCloseInventory(p);
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		String playerName = NameAPI.getCurrentName(uuid);

		ItemStack info = new ItemStack(Material.PAPER);
		ISUtils.setName(info, ChatColor.GOLD + playerName);
		String rankName = getRankName(uuid);
		ISUtils.addLore(info, ChatColor.GOLD + "Current rank: " + rankName);
		ci.setSlot(new DecorationStack(info), 4);

		PlayerType rank = g.getCurrentRank(uuid);

		Clickable memberClick = setupDetailSlot(Material.LEATHER_CHESTPLATE,
				uuid, PlayerType.MEMBERS);
		ci.setSlot(memberClick, 10);
		Clickable modClick = setupDetailSlot(Material.GOLD_CHESTPLATE, uuid,
				PlayerType.MODS);
		ci.setSlot(modClick, 12);
		Clickable adminClick = setupDetailSlot(Material.IRON_CHESTPLATE, uuid,
				PlayerType.ADMINS);
		ci.setSlot(adminClick, 14);
		Clickable ownerClick = setupDetailSlot(Material.DIAMOND_CHESTPLATE,
				uuid, PlayerType.OWNER);
		ci.setSlot(ownerClick, 16);
		ci.showInventory(p);
	}

	private Clickable setupDetailSlot(Material slotMaterial,
			final UUID toChange, final PlayerType pType) {
		PlayerType rank = g.getCurrentRank(toChange);
		ItemStack mod = new ItemStack(slotMaterial);
		Clickable modClick;
		if (rank == pType) {
			ISUtils.setName(mod, ChatColor.GOLD + "Remove this player");
			if (!gm.hasAccess(g, p.getUniqueId(), getAccordingPermission(pType))) {
				ISUtils.addLore(mod, ChatColor.RED
						+ "You dont have permission to do this");
				modClick = new DecorationStack(mod);
			} else {
				modClick = new Clickable(mod) {

					@Override
					public void clicked(Player arg0) {
						if (gm.hasAccess(g, p.getUniqueId(),
								getAccordingPermission(g
										.getCurrentRank(toChange)))) {
							removeMember(toChange);
							showScreen();
						}
					}
				};
			}
		} else {
			ISUtils.setName(
					mod,
					ChatColor.GOLD
							+ demoteOrPromote(g.getPlayerType(toChange), pType,
									true) + " this player to "
							+ getDirectRankName(pType));
			if (!gm.hasAccess(g, p.getUniqueId(), getAccordingPermission(pType))) {
				ISUtils.addLore(mod, ChatColor.RED
						+ "You dont have permission to do this");
				modClick = new DecorationStack(mod);
			} else {
				modClick = new Clickable(mod) {

					@Override
					public void clicked(Player arg0) {
						changePlayerRank(toChange, pType);
						showDetail(toChange);
					}
				};
			}
		}
		return modClick;
	}

	private void removeMember(UUID toRemove) {
		if (gm.hasAccess(g, p.getUniqueId(),
				getAccordingPermission(g.getCurrentRank(toRemove)))) {
			if (!g.isMember(toRemove)) {
				p.sendMessage(ChatColor.RED
						+ "This player is no longer on the group and can't be removed");
				return;
			}
			if (g.isOwner(toRemove)) {
				p.sendMessage(ChatColor.RED
						+ "This player owns the group and can't be removed");
			}
			g.removeMember(toRemove);
			checkRecacheGroup();
			p.sendMessage(ChatColor.GREEN + NameAPI.getCurrentName(toRemove)
					+ " has been removed from the group");
		} else {
			p.sendMessage(ChatColor.RED
					+ "You have lost permission to remove this player");
		}
	}

	private void changePlayerRank(UUID toChange, PlayerType newRank) {
		if (gm.hasAccess(g, p.getUniqueId(),
				getAccordingPermission(g.getCurrentRank(toChange)))) {
			if (!g.isMember(toChange)) {
				p.sendMessage(ChatColor.RED
						+ "This player is no longer on the group and can't be "
						+ demoteOrPromote(g.getCurrentRank(toChange), newRank,
								false) + "d");
				return;
			}
			if (g.isOwner(toChange)) {
				p.sendMessage(ChatColor.RED
						+ "This player owns the group and can't be demoted");
			}
			OfflinePlayer prom = Bukkit.getOfflinePlayer(toChange);
			if (prom.isOnline()) {
				Player oProm = (Player) prom;
				PromotePlayerEvent event = new PromotePlayerEvent(oProm, g,
						g.getCurrentRank(toChange), newRank);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					p.sendMessage(ChatColor.RED
							+ "Could not change player rank, you should complain about this");
					return;
				}
				g.removeMember(toChange);
				g.addMember(toChange, newRank);
				oProm.sendMessage(ChatColor.GREEN
						+ "You have been promoted to " + getRankName(toChange)
						+ " in (Group) " + g.getName());
			} else {
				// player is offline change their perms
				g.removeMember(toChange);
				g.addMember(toChange, newRank);
			}
			checkRecacheGroup();
			p.sendMessage(ChatColor.GREEN
					+ NameAPI.getCurrentName(toChange)
					+ " has been "
					+ demoteOrPromote(g.getCurrentRank(toChange), newRank,
							false) + "d to " + getRankName(toChange));
		} else {
			p.sendMessage(ChatColor.RED
					+ "You have lost permission to remove this player");
		}
	}

	private List<UUID> getToDisplay() {
		List<UUID> res = new ArrayList<UUID>();
		for (UUID uuid : g.getAllMembers()) {
			switch (g.getPlayerType(uuid)) {
			case MEMBERS:
				if (showMembers) {
					res.add(uuid);
				}
				break;
			case MODS:
				if (showMods) {
					res.add(uuid);
				}
				break;
			case ADMINS:
				if (showAdmins) {
					res.add(uuid);
				}
				break;
			case OWNER:
				if (showOwners) {
					res.add(uuid);
				}
				break;
			default:
				// should never happen
				continue;
			}
		}
		return res;
	}

	/**
	 * Utility to get a "nice" version of the rank the given player has in the
	 * group this gui is based on
	 * 
	 * @param uuid
	 *            Player whose rank name should be checked
	 * @return Rankname of the given player ready to be put into the gui or null
	 *         if the given UUID was null or the player didn't have an explicit
	 *         rank in the group
	 */
	private String getRankName(UUID uuid) {
		if (uuid == null) {
			return null;
		}
		PlayerType pType = g.getPlayerType(uuid);
		if (pType == null) {
			return null;
		}
		return getDirectRankName(pType);
	}

	public static String getDirectRankName(PlayerType pType) {
		String res = pType.toString().toLowerCase();
		char[] chars = res.toCharArray();
		chars[0] = new String(new char[] { chars[0] }).toUpperCase()
				.toCharArray()[0];
		res = new String(chars);
		if (res.endsWith("s")) {
			return res.substring(0, res.length() - 1);
		}
		return res;
	}

	/**
	 * Gets the permission needed to make changes to the given PlayerType
	 * 
	 * @param pt
	 *            PlayerType to get permission for
	 * @return Permission belonging to the given PlayerType or null if the
	 *         permission was NOT_BLACKLISTED or null
	 */
	public static PermissionType getAccordingPermission(PlayerType pt) {
		switch (pt) {
		case MEMBERS:
			return PermissionType.getPermission("MEMBERS");
		case MODS:
			return PermissionType.getPermission("MODS");
		case ADMINS:
			return PermissionType.getPermission("ADMINS");
		case OWNER:
			return PermissionType.getPermission("OWNER");
		}
		return null;
	}

	/**
	 * Utility to determine whether the player is being promoted or demoted
	 */
	private static String demoteOrPromote(PlayerType oldRank,
			PlayerType newRank, boolean upperCaseFirstLetter) {
		int oldIndex = 0;
		int newIndex = 0;
		for (int i = 0; i < PlayerType.values().length; i++) {
			if (PlayerType.values()[i] == oldRank) {
				oldIndex = i;
			}
			if (PlayerType.values()[i] == newRank) {
				newIndex = i;
			}
		}
		String res = oldIndex <= newIndex ? "promote" : "demote";
		if (upperCaseFirstLetter) {
			return res.substring(0, 1).toUpperCase() + res.substring(1);
		}
		return res;
	}

	private boolean hasPermissionToViewAnything() {
		return gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MEMBERS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("MODS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("ADMINS"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("OWNER"))
				|| gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS"));
	}
}
