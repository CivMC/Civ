package vg.civcraft.mc.namelayer.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

	private boolean canRemoveMembers;
	private boolean canRemoveMods;
	private boolean canRemoveAdmins;
	private boolean canRemoveOwners;
	private boolean canSeeAll;

	private UUID memberViewed;
	private int currentPage;
	private NameLayerGUI parentGUI;

	public MemberViewGUI(Player p, Group g, NameLayerGUI parentGUI) {
		super(g, p);
		this.parentGUI = parentGUI;
		loadPermission();
		showMembers = (canRemoveMembers || canSeeAll);
		showMods = (canRemoveMods || canSeeAll);
		showAdmins = (canRemoveAdmins || canSeeAll);
		showOwners = (canRemoveOwners || canSeeAll);
		currentPage = 0;
		showScreen();
	}

	private void showScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		loadPermission();
		if (!canRemoveMembers && !canRemoveMods && !canRemoveAdmins
				&& !canRemoveOwners && !canSeeAll) {
			p.sendMessage(ChatColor.RED
					+ "You lost the required permissions to list members of this group");
			return;
		}
		ClickableInventory ci = new ClickableInventory(54, g.getName());
		final List<UUID> members = getToDisplay();
		if (members.size() < 45 * currentPage) {
			currentPage--;
			showScreen();
		}
		for (int i = 45 * currentPage; i < 45 * (currentPage + 1)
				&& i < members.size(); i++) {
			ItemStack is;
			final UUID currentId = members.get(i);
			Clickable c;
			switch (g.getPlayerType(currentId)) {
			case MEMBERS:
				is = new ItemStack(Material.LEATHER_CHESTPLATE);
				ISUtils.addLore(is, ChatColor.AQUA + "Rank: Member");
				if (canRemoveMembers) {
					ISUtils.addLore(is, ChatColor.GREEN
							+ "Click to modify this players", ChatColor.GREEN
							+ "rank or to remove him");
					c = new Clickable(is) {

						@Override
						public void clicked(Player arg0) {
							showDetail(currentId);
							showScreen();
						}
					};
				} else {
					ISUtils.addLore(is, ChatColor.RED
							+ "You don't have permission", ChatColor.RED
							+ "to modify the rank of this player");
					c = new DecorationStack(is);
				}
				break;
			case MODS:
				is = new ItemStack(Material.GOLD_CHESTPLATE);
				ISUtils.addLore(is, ChatColor.AQUA + "Rank: Mod");
				if (canRemoveMods) {
					ISUtils.addLore(is, ChatColor.GREEN
							+ "Click to modify this players", ChatColor.GREEN
							+ "rank or to remove him");
					c = new Clickable(is) {

						@Override
						public void clicked(Player arg0) {
							showDetail(currentId);
							showScreen();
						}
					};
				} else {
					ISUtils.addLore(is, ChatColor.RED
							+ "You don't have permission", ChatColor.RED
							+ "to modify the rank of this player");
					c = new DecorationStack(is);
				}
				break;
			case ADMINS:
				is = new ItemStack(Material.IRON_CHESTPLATE);
				ISUtils.addLore(is, ChatColor.AQUA + "Rank: Admin");
				if (canRemoveAdmins) {
					ISUtils.addLore(is, ChatColor.GREEN
							+ "Click to modify this players", ChatColor.GREEN
							+ "rank or to remove him");
					c = new Clickable(is) {

						@Override
						public void clicked(Player arg0) {
							showDetail(currentId);
							showScreen();
						}
					};
				} else {
					ISUtils.addLore(is, ChatColor.RED
							+ "You don't have permission", ChatColor.RED
							+ "to modify the rank of this player");
					c = new DecorationStack(is);
				}
				break;
			case OWNER:
				is = new ItemStack(Material.DIAMOND_CHESTPLATE);
				if (g.isOwner(currentId)) {
					ISUtils.addLore(is, ChatColor.AQUA + "Rank: Primary Owner");
					ISUtils.addLore(is, ChatColor.RED
							+ "You don't have permission", ChatColor.RED
							+ "to modify the rank of this player");
					c = new DecorationStack(is);

				} else {
					ISUtils.addLore(is, ChatColor.AQUA + "Rank: Owner");
					if (canRemoveOwners) {
						ISUtils.addLore(is, ChatColor.GREEN
								+ "Click to modify this players",
								ChatColor.GREEN + "rank or to remove him");
						c = new Clickable(is) {

							@Override
							public void clicked(Player arg0) {
								showDetail(currentId);
								showScreen();
							}
						};
					} else {
						ISUtils.addLore(is, ChatColor.RED
								+ "You don't have permission", ChatColor.RED
								+ "to modify the rank of this player");
						c = new DecorationStack(is);
					}
				}
				break;
			default:
				// should never happen
				continue;
			}
			ISUtils.setName(is,
					ChatColor.GOLD + NameAPI.getCurrentName(currentId));
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
		ci.setSlot(
				new Clickable(MenuUtils.toggleButton(showMembers,
						ChatColor.GOLD + "Show members",
						(canRemoveMembers || canSeeAll))) {

					@Override
					public void clicked(Player arg0) {
						showMembers = !showMembers;
						currentPage = 0;
						showScreen();
					}
				}, 45);

		ci.setSlot(
				new Clickable(MenuUtils.toggleButton(showMods, ChatColor.GOLD
						+ "Show mods", (canRemoveMods || canSeeAll))) {

					@Override
					public void clicked(Player arg0) {
						showMods = !showMods;
						currentPage = 0;
						showScreen();
					}
				}, 46);

		ci.setSlot(
				new Clickable(MenuUtils.toggleButton(showAdmins, ChatColor.GOLD
						+ "Show admins", (canRemoveAdmins || canSeeAll))) {

					@Override
					public void clicked(Player arg0) {
						showAdmins = !showAdmins;
						currentPage = 0;
						showScreen();
					}
				}, 47);

		ci.setSlot(
				new Clickable(MenuUtils.toggleButton(showOwners, ChatColor.GOLD
						+ "Show owners", (canRemoveOwners || canSeeAll))) {

					@Override
					public void clicked(Player arg0) {
						showOwners = !showOwners;
						currentPage = 0;
						showScreen();
					}
				}, 48);

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

	public void showDetail(final UUID uuid) {
		loadPermission();
		if (!validGroup()) {
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

		Clickable memberClick = setupDetailSlot(Material.LEATHER_CHESTPLATE, uuid, PlayerType.MEMBERS);
		ci.setSlot(memberClick, 10);
		Clickable modClick = setupDetailSlot(Material.GOLD_CHESTPLATE, uuid, PlayerType.MODS);
		ci.setSlot(modClick, 12);
		Clickable adminClick = setupDetailSlot(Material.IRON_CHESTPLATE, uuid, PlayerType.ADMINS);
		ci.setSlot(adminClick, 14);
		Clickable ownerClick = setupDetailSlot(Material.DIAMOND_CHESTPLATE, uuid, PlayerType.OWNER);
		ci.setSlot(ownerClick, 16);
		ci.showInventory(p);
	}
	
	private Clickable setupDetailSlot(Material slotMaterial, final UUID toChange, final PlayerType pType) {
		PlayerType rank = g.getCurrentRank(toChange);
		ItemStack mod = new ItemStack(slotMaterial);
		Clickable modClick;
		if (rank == pType) {
			ISUtils.setName(mod, ChatColor.GOLD + "Remove this player");
			if (!gm.hasAccess(g, p.getUniqueId(),
					getAccordingPermission(pType))) {
				ISUtils.addLore(mod, ChatColor.RED
						+ "You dont have permission to do this");
				modClick = new DecorationStack(mod);
			} else {
				modClick = new Clickable(mod) {

					@Override
					public void clicked(Player arg0) {
						if (gm.hasAccess(g, p.getUniqueId(),
								getAccordingPermission(g.getCurrentRank(toChange)))) {
							removeMember(toChange);
						}
					}
				};
			}
		} else {
			ISUtils.setName(mod, ChatColor.GOLD
					+ demoteOrPromote(g.getPlayerType(toChange), pType, true) +" this player to " + getDirectRankName(pType));
			if (!gm.hasAccess(g, p.getUniqueId(),
					getAccordingPermission(pType))) {
				ISUtils.addLore(mod, ChatColor.RED
						+ "You dont have permission to do this");
				modClick = new DecorationStack(mod);
			} else {
				modClick = new Clickable(mod) {

					@Override
					public void clicked(Player arg0) {
						changePlayerRank(toChange, pType);
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
						+ "This player is no longer on the group and can't be " + demoteOrPromote(g.getCurrentRank(toChange), newRank, false)+"d");
				return;
			}
			if (g.isOwner(toChange)) {
				p.sendMessage(ChatColor.RED
						+ "This player owns the group and can't be demoted");
			}
			OfflinePlayer prom = Bukkit.getOfflinePlayer(toChange);
			if(prom.isOnline()){
				Player oProm = (Player) prom;
				PromotePlayerEvent event = new PromotePlayerEvent(oProm, g, g.getCurrentRank(toChange), newRank);
				Bukkit.getPluginManager().callEvent(event);
				if(event.isCancelled()){
					p.sendMessage(ChatColor.RED + "Could not change player rank, you should complain about this");
					return;
				}
				g.removeMember(toChange);
				g.addMember(toChange, newRank);
				oProm.sendMessage(ChatColor.GREEN + "You have been promoted to " + getRankName(toChange) + " in (Group) " + g.getName());
			}
			else{
				//player is offline change their perms
				g.removeMember(toChange);
				g.addMember(toChange, newRank);
			}
			checkRecacheGroup();
			p.sendMessage(ChatColor.GREEN + NameAPI.getCurrentName(toChange)
					+ " has been " + demoteOrPromote(g.getCurrentRank(toChange), newRank, false)+"d to " + getRankName(toChange));
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

	private void loadPermission() {
		canRemoveMembers = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MEMBERS"));
		canRemoveMods = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MODS"));
		canRemoveAdmins = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("ADMINS"));
		canRemoveOwners = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("OWNER"));
		canSeeAll = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("GROUPSTATS"));
	}

	private String getRankName(UUID uuid) {
		PlayerType pType = g.getPlayerType(uuid);
		if (pType == null) {
			return null;
		}
		return getDirectRankName(pType);
	}
	
	private String getDirectRankName(PlayerType pType) {
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

	private PermissionType getAccordingPermission(PlayerType pt) {
		switch (pt) {
		case MEMBERS:
			return PermissionType.getPermission("MEMBERS");
		case MODS:
			return PermissionType.getPermission("MODS");
		case ADMINS:
			return PermissionType.getPermission("ADMINS");
		case OWNER:
			return PermissionType.getPermission("MEMBERS");
		}
		return null;
	}

	/**
	 * Utility to determine whether the player is being promoted or demoted
	 */
	private String demoteOrPromote(PlayerType oldRank, PlayerType newRank, boolean upperCaseFirstLetter) {
		int oldIndex  = 0;
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
}
