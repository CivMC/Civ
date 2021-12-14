package vg.civcraft.mc.namelayer.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.StringUtil;
import vg.civcraft.mc.civmodcore.chat.dialog.Dialog;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.events.PromotePlayerEvent;
import vg.civcraft.mc.namelayer.group.BlackList;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.listeners.PlayerListener;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class MainGroupGUI extends AbstractGroupGUI {

	private boolean showInheritedMembers;
	private boolean showBlacklist;
	private boolean showInvites;
	private boolean showMembers;
	private boolean showMods;
	private boolean showAdmins;
	private boolean showOwners;

	private int currentPage;
	private boolean[] savedToggleState;

	public MainGroupGUI(Player p, Group g) {
		super(g, p);
		showBlacklist = false;
		showInvites = false;
		showInheritedMembers = false;
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

	/**
	 * Shows the main gui overview for a specific group based on the properties
	 * of this class
	 */
	public void showScreen() {
		if (!validGroup()) {
			return;
		}
		ClickableInventory ci = new ClickableInventory(54, g.getName());
		final List<Clickable> clicks = constructClickables();
		if (clicks.size() < 45 * currentPage) {
			// would show an empty page, so go to previous
			currentPage--;
			showScreen();
		}
		// fill gui
		for (int i = 36 * currentPage; i < 36 * (currentPage + 1)
				&& i < clicks.size(); i++) {
			ci.setSlot(clicks.get(i), 9 + i - (36 * currentPage));
		}
		// back button
		if (currentPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(back, ChatColor.GOLD + "Go to previous page");
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
		if ((45 * (currentPage + 1)) <= clicks.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= clicks.size()) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, 53);
		}

		// options

		ci.setSlot(getSuperMenuClickable(), 45);
		ci.setSlot(createInheritedMemberToggle(), 46);
		ci.setSlot(createInviteToggle(), 47);
		ci.setSlot(setupMemberTypeToggle(PlayerType.MEMBERS, showMembers), 48);

		ci.setSlot(setupMemberTypeToggle(PlayerType.MODS, showMods), 50);

		ci.setSlot(setupMemberTypeToggle(PlayerType.ADMINS, showAdmins), 51);

		ci.setSlot(setupMemberTypeToggle(PlayerType.OWNER, showOwners), 52);

		// exit button
		ItemStack backToOverview = goBackStack(); 
		ItemUtils.setDisplayName(backToOverview, ChatColor.GOLD + "Close");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				ClickableInventory.forceCloseInventory(arg0);
			}
		}, 49);

		// options at the top
		ci.setSlot(getInvitePlayerClickable(), 0);
		ci.setSlot(createBlacklistToggle(), 1);
		ci.setSlot(getAddBlackListClickable(), 2);
		ci.setSlot(getLeaveGroupClickable(), 3);
		ci.setSlot(getInfoStack(), 4);
		ci.setSlot(getDefaultGroupStack(), 5);
		ci.setSlot(getPasswordClickable(), 6);
		ci.setSlot(getPermOptionClickable(), 7);
		ci.setSlot(getAdminStuffClickable(), 8);
		ci.showInventory(p);
	}

	/**
	 * Creates a list of all Clickables representing members, invitees and
	 * blacklisted players, if they are supposed to be displayed. This is whats
	 * directly fed into the middle of the gui
	 */
	private List<Clickable> constructClickables() {
		List<Clickable> clicks = new ArrayList<>();
		if (showInheritedMembers) {
			if (g.hasSuperGroup()) {
				clicks.addAll(getRecursiveInheritedMembers(g.getSuperGroup()));
			}
		}
		if (showBlacklist) {
			final BlackList black = NameLayerPlugin.getBlackList();
			for (final UUID uuid : black.getBlacklist(g)) {
				ItemStack is = new ItemStack(Material.LEATHER_CHESTPLATE);
				LeatherArmorMeta meta = (LeatherArmorMeta) is.getItemMeta();
				meta.setColor(Color.BLACK);
				meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				is.setItemMeta(meta);
				ItemUtils.setDisplayName(is, NameAPI.getCurrentName(uuid));
				Clickable c;
				if (gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("BLACKLIST"))) {
					ItemUtils.addLore(is, ChatColor.GREEN + "Click to remove "
							+ NameAPI.getCurrentName(uuid), ChatColor.GREEN
							+ "from the blacklist");
					c = new Clickable(is) {

						@Override
						public void clicked(Player arg0) {
							if (gm.hasAccess(g, p.getUniqueId(),
									PermissionType.getPermission("BLACKLIST"))) {
								NameLayerPlugin.log(
										Level.INFO,
										arg0.getName() + " removed "
												+ NameAPI.getCurrentName(uuid)
												+ " from the blacklist of "
												+ g.getName() + " via the gui");
								black.removeBlacklistMember(g, uuid, true);
								p.sendMessage(ChatColor.GREEN + "You removed "
										+ NameAPI.getCurrentName(uuid)
										+ " from the blacklist");
							} else {
								p.sendMessage(ChatColor.RED
										+ "You lost permission to remove this player from the blacklist");
							}
							showScreen();
						}
					};
				} else {
					ItemUtils.addLore(is, ChatColor.RED
							+ "You don't have permission to remove",
							ChatColor.RED + NameAPI.getCurrentName(uuid)
						  + "from the blacklist");
					c = new DecorationStack(is);
				}
				clicks.add(c);
			}

		}
		if (showInvites) {
			Map<UUID, PlayerType> invites = NameLayerPlugin
					.getGroupManagerDao().getInvitesForGroup(g.getName());
			for (Entry<UUID, PlayerType> entry : invites.entrySet()) {
				ItemStack is = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
				ItemMeta im = is.getItemMeta();
				im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				is.setItemMeta(im);
				final String playerName = NameAPI
						.getCurrentName(entry.getKey());
				ItemUtils.setDisplayName(is, ChatColor.GOLD + playerName);
				boolean canRevoke = false;
				switch (entry.getValue()) {
				case MEMBERS:
					ItemUtils.addLore(is, ChatColor.AQUA + "Invited as: Member");
					if (gm.hasAccess(g, p.getUniqueId(),
							PermissionType.getPermission("MEMBERS"))) {
						canRevoke = true;
					}
					break;
				case MODS:
					ItemUtils.addLore(is, ChatColor.AQUA + "Invited as: Mod");
					if (gm.hasAccess(g, p.getUniqueId(),
							PermissionType.getPermission("MODS"))) {
						canRevoke = true;
					}
					break;
				case ADMINS:
					ItemUtils.addLore(is, ChatColor.AQUA + "Invited as: Admin");
					if (gm.hasAccess(g, p.getUniqueId(),
							PermissionType.getPermission("ADMINS"))) {
						canRevoke = true;
					}
					break;
				case OWNER:
					ItemUtils.addLore(is, ChatColor.AQUA + "Invited as: Owner");
					if (gm.hasAccess(g, p.getUniqueId(),
							PermissionType.getPermission("OWNER"))) {
						canRevoke = true;
					}
					break;
				default:
					continue;
				}
				Clickable c = null;
				if (canRevoke) {
					ItemUtils.addLore(is, ChatColor.GREEN
							+ "Click to revoke this invite");
					c = new Clickable(is) {

						@Override
						public void clicked(Player arg0) {
							UUID invitedUUID = NameAPI.getUUID(playerName);
							PlayerType pType = g.getInvite(invitedUUID);
							if (pType == null) {
								p.sendMessage(ChatColor.RED
										+ "Failed to revoke invite for "
										+ playerName
										+ ". This player isn't invited currently.");
								showScreen();
							}
							// make sure the player still has permission to do
							// this
							boolean allowed = false;
							switch (pType) {
								case MEMBERS:
									allowed = gm.hasAccess(g, p.getUniqueId(),
											PermissionType.getPermission("MEMBERS"));
									break;
								case MODS:
									allowed = gm.hasAccess(g, p.getUniqueId(),
											PermissionType.getPermission("MODS"));
									break;
								case ADMINS:
									allowed = gm.hasAccess(g, p.getUniqueId(),
											PermissionType.getPermission("ADMINS"));
									break;
								case OWNER:
									allowed = gm.hasAccess(g, p.getUniqueId(),
											PermissionType.getPermission("OWNER"));
									break;
								default:
									allowed = false;
									break;
							}
							if (!allowed) {
								p.sendMessage(ChatColor.RED
										+ "You don't have permission to revoke this invite");
							} else {
								NameLayerPlugin.log(Level.INFO, arg0.getName()
										+ " revoked an invite for " + NameAPI.getCurrentName(invitedUUID)
										+ " for group " + g.getName() + " via the gui");
								g.removeInvite(invitedUUID, true);
								PlayerListener.removeNotification(invitedUUID, g);

								p.sendMessage(ChatColor.GREEN + playerName
										+ "'s invitation has been revoked.");
							}
							showScreen();
						}
					};
				} else {
					ItemUtils.addLore(is, ChatColor.RED
							+ "You don't have permission to revoke this invite");
					c = new DecorationStack(is);
				}
				if (c != null) {
					clicks.add(c);
				}
			}
		}
		for (UUID uuid : g.getAllMembers()) {
			Clickable c = null;
			switch (g.getPlayerType(uuid)) {
				case MEMBERS:
					if (showMembers) {
						c = constructMemberClickable(Material.LEATHER_CHESTPLATE,
								uuid, PlayerType.MEMBERS);
					}
					break;
				case MODS:
					if (showMods) {
						c = constructMemberClickable(modMat(),
								uuid, PlayerType.MODS);
					}
					break;
				case ADMINS:
					if (showAdmins) {
						c = constructMemberClickable(Material.IRON_CHESTPLATE,
								uuid, PlayerType.ADMINS);
					}
					break;
				case OWNER:
					if (showOwners) {
						c = constructMemberClickable(Material.DIAMOND_CHESTPLATE,
								uuid, PlayerType.OWNER);
					}
					break;
				default:
					// should never happen
			}
			if (c != null) {
				clicks.add(c);
			}
		}

		return clicks;
	}

	/**
	 * Convenience method to create the toggles for the displaying of different
	 * group member types
	 */
	private Clickable setupMemberTypeToggle(final PlayerType pType,
											boolean initialState) {
		boolean canEdit = gm.hasAccess(g, p.getUniqueId(),
				getAccordingPermission(pType))
				|| gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("GROUPSTATS"));
		ItemStack is = MenuUtils.toggleButton(initialState, ChatColor.GOLD
				+ "Show " + PlayerType.getNiceRankName(pType) + "s", canEdit);
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

	/**
	 * Convenience method used when constructing clickables in the middle of the
	 * gui, which represent members
	 */
	private Clickable constructMemberClickable(Material material,
											   final UUID toDisplay, PlayerType rank) {
		Clickable c;
		ItemStack is = new ItemStack(material);
		ItemMeta im = is.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		is.setItemMeta(im);
		ItemUtils.setDisplayName(is, ChatColor.GOLD + NameAPI.getCurrentName(toDisplay));
		if (g.isOwner(toDisplay)) { // special case for primary owner
			ItemUtils.addLore(is, ChatColor.AQUA + "Rank: Primary Owner");
			ItemUtils.addLore(is, ChatColor.RED + "You don't have permission",
					ChatColor.RED + "to modify the rank of this player");
			c = new DecorationStack(is);

		} else {
			ItemUtils.addLore(is, ChatColor.AQUA + "Rank: "
					+ getRankName(toDisplay));
			if (gm.hasAccess(g, p.getUniqueId(), getAccordingPermission(rank))) {
				ItemUtils.addLore(is, ChatColor.GREEN
						+ "Click to modify this player's", ChatColor.GREEN
						+ "rank or to remove them");
				c = new Clickable(is) {

					@Override
					public void clicked(Player arg0) {
						showDetail(toDisplay);
					}
				};
			} else {
				ItemUtils.addLore(is,
						ChatColor.RED + "You don't have permission",
						ChatColor.RED + "to modify the rank of this player");
				c = new DecorationStack(is);
			}
		}
		return c;
	}

	/**
	 * Called when the icon representing a member in the middle of the gui is
	 * clicked, this opens up a detailed view where you can select what to do
	 * (promoting/removing)
	 *
	 * @param uuid the UUID to show the inventory to
	 */
	public void showDetail(final UUID uuid) {
		if (!validGroup()) {
			showScreen();
			return;
		}
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		String playerName = NameAPI.getCurrentName(uuid);

		ItemStack info = new ItemStack(Material.PAPER);
		ItemUtils.setDisplayName(info, ChatColor.GOLD + playerName);
		String rankName = getRankName(uuid);
		ItemUtils.addLore(info, ChatColor.GOLD + "Current rank: " + rankName);
		ci.setSlot(new DecorationStack(info), 4);

		Clickable memberClick = setupDetailSlot(Material.LEATHER_CHESTPLATE,
				uuid, PlayerType.MEMBERS);
		ci.setSlot(memberClick, 10);
		Clickable modClick = setupDetailSlot(modMat(), uuid,
				PlayerType.MODS);
		ci.setSlot(modClick, 12);
		Clickable adminClick = setupDetailSlot(Material.IRON_CHESTPLATE, uuid,
				PlayerType.ADMINS);
		ci.setSlot(adminClick, 14);
		Clickable ownerClick = setupDetailSlot(Material.DIAMOND_CHESTPLATE,
				uuid, PlayerType.OWNER);
		ci.setSlot(ownerClick, 16);

		ItemStack backToOverview = goBackStack(); 
		ItemUtils.setDisplayName(backToOverview, ChatColor.GOLD + "Back to overview");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				showScreen();
			}
		}, 22);
		ci.showInventory(p);
	}

	/**
	 * Used by the gui that allows selecting an action for a specific member to
	 * easily construct the clickables needed
	 */
	private Clickable setupDetailSlot(Material slotMaterial,
									  final UUID toChange, final PlayerType pType) {
		final PlayerType rank = g.getCurrentRank(toChange);
		ItemStack mod = new ItemStack(slotMaterial);
		ItemMeta im = mod.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		mod.setItemMeta(im);
		Clickable modClick;
		if (rank == pType) {
			ItemUtils.setDisplayName(mod, ChatColor.GOLD + "Remove this player");
			if (!gm.hasAccess(g, p.getUniqueId(), getAccordingPermission(pType))) {
				ItemUtils.addLore(mod, ChatColor.RED
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
			ItemUtils.setDisplayName(
					mod,
					ChatColor.GOLD
							+ demoteOrPromote(g.getPlayerType(toChange), pType,
							true) + " this player to "
							+ PlayerType.getNiceRankName(pType));
			if (!gm.hasAccess(g, p.getUniqueId(), getAccordingPermission(pType))) {
				ItemUtils.addLore(mod, ChatColor.RED
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
			NameLayerPlugin.log(Level.INFO,
					p.getName() + " kicked " + NameAPI.getCurrentName(toRemove)
							+ " from " + g.getName() + " via the gui");
			g.removeMember(toRemove);
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
			NameLayerPlugin.log(
					Level.INFO,
					p.getName() + " changed player rank for "
							+ NameAPI.getCurrentName(toChange) + " from "
							+ g.getCurrentRank(toChange).toString() + " to "
							+ newRank.toString() + " for group " + g.getName()
							+ " via the gui");
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

	private Clickable createBlacklistToggle() {
		ItemStack is = MenuUtils.toggleButton(
				showBlacklist,
				ChatColor.GOLD + "Show blacklisted players",
				gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("GROUPSTATS")));
		Clickable c;
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("GROUPSTATS"))) {
			c = new Clickable(is) {

				@Override
				public void clicked(Player arg0) {
					if (!showBlacklist) {
						// currently showing members, so save state
						savedToggleState = new boolean[5];
						savedToggleState[0] = showInvites;
						savedToggleState[1] = showMembers;
						savedToggleState[2] = showMods;
						savedToggleState[3] = showAdmins;
						savedToggleState[4] = showOwners;
						showInvites = false;
						showMembers = false;
						showMods = false;
						showAdmins = false;
						showOwners = false;
						showBlacklist = true;
					} else {
						// load state
						showInvites = savedToggleState[0];
						showMembers = savedToggleState[1];
						showMods = savedToggleState[2];
						showAdmins = savedToggleState[3];
						showOwners = savedToggleState[4];
						showBlacklist = false;
					}
					showScreen();
				}
			};
		} else {
			c = new DecorationStack(is);
		}
		return c;
	}

	private Clickable createInheritedMemberToggle() {
		boolean canToggle = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("GROUPSTATS"));
		ItemStack is = MenuUtils.toggleButton(showInheritedMembers,
				ChatColor.GOLD + "Show inherited members", canToggle);
		Clickable c;
		if (canToggle) {
			c = new Clickable(is) {

				@Override
				public void clicked(Player p) {
					showInheritedMembers = !showInheritedMembers;
					showScreen();

				}
			};
		} else {
			c = new DecorationStack(is);
		}
		return c;
	}

	private Clickable createInviteToggle() {
		ItemStack is = MenuUtils.toggleButton(showInvites, ChatColor.GOLD
				+ "Show invited players", true);
		return new Clickable(is) {

			@Override
			public void clicked(Player arg0) {
				showInvites = !showInvites;
				showScreen();
			}
		};
	}

	private Clickable getAddBlackListClickable() {
		Clickable c;
		ItemStack is = blacklistStack();
		ItemUtils.setDisplayName(is, ChatColor.GOLD + "Add player to blacklist");
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("BLACKLIST"))) {
			c = new Clickable(is) {

				@Override
				public void clicked(final Player p) {
					p.sendMessage(ChatColor.GOLD + "Enter the name of the player to blacklist or \"cancel\" to exit this prompt");
					ClickableInventory.forceCloseInventory(p);
					new Dialog(p, NameLayerPlugin.getInstance()) {

						@Override
						public List<String> onTabComplete(String word, String[] msg) {
							List<String> players = Bukkit.getOnlinePlayers().stream()
									.filter(p -> !g.isMember(p.getUniqueId()))
									.map(Player::getName)
									.collect(Collectors.toList());
							players.add("cancel");

							return StringUtil.copyPartialMatches(word, players, new ArrayList<>());
						}

						@Override
						public void onReply(String[] message) {
							if (message[0].equalsIgnoreCase("cancel")) {
								showScreen();
								return;
							}
							if (gm.hasAccess(g, p.getUniqueId(),
									PermissionType.getPermission("BLACKLIST"))) {
								for (String playerName : message) {
									UUID blackUUID = NameAPI
											.getUUID(playerName);
									if (blackUUID == null) {
										p.sendMessage(ChatColor.RED
												+ playerName + " doesn't exist");
										continue;
									}
									if (g.isMember(blackUUID)) {
										p.sendMessage(ChatColor.RED
												+ NameAPI.getCurrentName(blackUUID)
												+ " is currently a member of this group and can't be blacklisted");
										continue;
									}
									BlackList bl = NameLayerPlugin
											.getBlackList();
									if (bl.isBlacklisted(g, blackUUID)) {
										p.sendMessage(ChatColor.RED
												+ NameAPI.getCurrentName(blackUUID)
												+ " is already blacklisted");
										continue;
									}
									NameLayerPlugin
											.log(Level.INFO,
													p.getName()
															+ " blacklisted "
															+ NameAPI.getCurrentName(blackUUID)
															+ " for group "
															+ g.getName()
															+ " via the gui");
									bl.addBlacklistMember(g, blackUUID, true);
									p.sendMessage(ChatColor.GREEN
											+ NameAPI.getCurrentName(blackUUID)
											+ " was successfully blacklisted");
								}
							} else {
								p.sendMessage(ChatColor.RED
										+ "You lost permission to do this");
							}
							showScreen();
						}
					};

				}
			};
		} else {
			ItemUtils.addLore(is, ChatColor.RED
					+ "You don't have permission to do this");
			c = new DecorationStack(is);
		}
		return c;
	}

	private Clickable getPasswordClickable() {
		Clickable c;
		ItemStack is = new ItemStack(Material.OAK_SIGN);
		ItemUtils.setDisplayName(is, ChatColor.GOLD + "Add or change password");
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("PASSWORD"))) {
			String pass = g.getPassword();
			if (pass == null) {
				ItemUtils.addLore(is, ChatColor.AQUA
						+ "This group doesn't have a password currently");
			} else {
				ItemUtils.addLore(is, ChatColor.AQUA
						+ "The current password is: " + ChatColor.YELLOW + pass);
			}
			c = new Clickable(is) {

				@Override
				public void clicked(final Player p) {
					if (gm.hasAccess(g, p.getUniqueId(),
							PermissionType.getPermission("PASSWORD"))) {
						p.sendMessage(ChatColor.GOLD
								+ "Enter the new password for "
								+ g.getName()
								+ ". Enter \" delete\" to remove an existing password or \"cancel\" to exit this prompt");
						ClickableInventory.forceCloseInventory(p);
						new Dialog(p, NameLayerPlugin.getInstance()) {

							@Override
							public List<String> onTabComplete(
									String wordCompleted, String[] fullMessage) {
								return Collections.emptyList();
							}

							@Override
							public void onReply(String[] message) {
								if (message.length == 0) {
									p.sendMessage(ChatColor.RED
											+ "You entered nothing, no password was set");
									return;
								}
								if (message.length > 1) {
									p.sendMessage(ChatColor.RED
											+ "Your password may not contain spaces");
									return;
								}
								String newPassword = message[0];
								if (newPassword.equals("cancel")) {
									p.sendMessage(ChatColor.GREEN
											+ "Left password unchanged");
									return;
								}
								if (newPassword.equals("delete")) {
									g.setPassword(null);
									p.sendMessage(ChatColor.GREEN
											+ "Removed the password from the group");
									NameLayerPlugin.log(Level.INFO, p.getName()
											+ " removed password "
											+ " for group " + g.getName()
											+ " via the gui");
								} else {
									NameLayerPlugin.log(Level.INFO, p.getName()
											+ " set password to " + newPassword
											+ " for group " + g.getName()
											+ " via the gui");
									g.setPassword(newPassword);
									p.sendMessage(ChatColor.GREEN
											+ "Set new password: "
											+ ChatColor.YELLOW + newPassword);
								}
								showScreen();
							}
						};
					} else {
						p.sendMessage(ChatColor.RED
								+ "You lost permission to do this");
						showScreen();
					}
				}
			};
		} else {
			ItemUtils.addLore(is, ChatColor.RED
					+ "You don't have permission to do this");
			c = new DecorationStack(is);
		}
		return c;
	}

	private Clickable getPermOptionClickable() {
		ItemStack permStack = permsStack();
		ItemUtils.setDisplayName(permStack, ChatColor.GOLD
				+ "View and manage group permissions");
		Clickable permClickable;
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("LIST_PERMS"))) {
			permClickable = new Clickable(permStack) {
				@Override
				public void clicked(Player arg0) {
					PermissionManageGUI pmgui = new PermissionManageGUI(g, p,
							MainGroupGUI.this);
					pmgui.showScreen();
				}
			};
		} else {
			ItemUtils.addLore(permStack, ChatColor.RED
					+ "You don't have permission", ChatColor.RED + "to do this");
			permClickable = new DecorationStack(permStack);
		}
		return permClickable;
	}

	private Clickable getInvitePlayerClickable() {
		ItemStack inviteStack = new ItemStack(Material.COOKIE);
		ItemUtils.setDisplayName(inviteStack, ChatColor.GOLD + "Invite new member");
		return new Clickable(inviteStack) {

			@Override
			public void clicked(Player arg0) {
				new InvitationGUI(g, p, MainGroupGUI.this);
			}
		};
	}

	private Clickable getDefaultGroupStack() {
		Clickable c;
		ItemStack is = defaultStack();
		ItemUtils.setDisplayName(is, ChatColor.GOLD + "Default group");
		final String defGroup = gm.getDefaultGroup(p.getUniqueId());
		if (defGroup != null && defGroup.equals(g.getName())) {
			ItemUtils.addLore(is, ChatColor.AQUA
					+ "This group is your current default group");
			c = new DecorationStack(is);
		} else {
			ItemUtils.addLore(is, ChatColor.AQUA
					+ "Click to make this group your default group");
			if (defGroup != null) {
				ItemUtils.addLore(is, ChatColor.BLUE
						+ "Your current default group is : " + defGroup);
			}
			c = new Clickable(is) {

				@Override
				public void clicked(Player p) {
					NameLayerPlugin.log(Level.INFO, p.getName()
							+ " set default group to " + g.getName()
							+ " via the gui");
					if (defGroup == null) {
						g.setDefaultGroup(p.getUniqueId());
						p.sendMessage(ChatColor.GREEN
								+ "You have set your default group to "
								+ g.getName());
					} else {
						g.changeDefaultGroup(p.getUniqueId());
						p.sendMessage(ChatColor.GREEN
								+ "You changed your default group from "
								+ defGroup + " to " + g.getName());
					}
					showScreen();
				}
			};
		}
		return c;
	}
	
	private Clickable getSuperMenuClickable() {
		ItemStack is = new ItemStack(Material.DIAMOND);
		ItemUtils.setDisplayName(is, ChatColor.GOLD + "Return to overview for all your groups");
		return new Clickable(is) {

			@Override
			public void clicked(Player p) {
				GUIGroupOverview gui = new GUIGroupOverview(p);
				gui.showScreen();
			}
		};
	}

	private Clickable getAdminStuffClickable() {
		ItemStack is = new ItemStack(Material.DIAMOND);
		ItemUtils.setDisplayName(is, ChatColor.GOLD + "Owner functions");
		return new Clickable(is) {

			@Override
			public void clicked(Player p) {
				AdminFunctionsGUI subGui = new AdminFunctionsGUI(p, g,
						MainGroupGUI.this);
				subGui.showScreen();
			}
		};
	}

	/**
	 * Constructs the icon used in the gui for leaving a group
	 */
	private Clickable getLeaveGroupClickable() {
		Clickable c;
		ItemStack is = new ItemStack(Material.IRON_DOOR);
		ItemUtils.setDisplayName(is, ChatColor.GOLD + "Leave group");
		if (g.isOwner(p.getUniqueId())) {
			ItemUtils.addLore(is, ChatColor.RED + "You cant leave this group,",
					ChatColor.RED + "because you own it");
			c = new DecorationStack(is);
		} else {
			c = new Clickable(is) {

				@Override
				public void clicked(Player p) {
					ClickableInventory confirmInv = new ClickableInventory(27,
							g.getName());
					ItemStack info = new ItemStack(Material.PAPER);
					ItemUtils.setDisplayName(info, ChatColor.GOLD + "Leave group");
					ItemUtils.addLore(info, ChatColor.RED
							+ "Are you sure that you want to", ChatColor.RED
							+ "leave this group? You can not undo this!");
					ItemStack yes = yesStack();
					ItemUtils.setDisplayName(yes,
							ChatColor.GOLD + "Yes, leave " + g.getName());
					ItemStack no = noStack();
					ItemUtils.setDisplayName(no,
							ChatColor.GOLD + "No, stay in " + g.getName());
					confirmInv.setSlot(new Clickable(yes) {

						@Override
						public void clicked(Player p) {
							if (!g.isMember(p.getUniqueId())) {
								p.sendMessage(ChatColor.RED
										+ "You are not a member of this group.");
								showScreen();
								return;
							}
							if (g.isDisciplined()) {
								p.sendMessage(ChatColor.RED
										+ "This group is disciplined.");
								showScreen();
								return;
							}
							NameLayerPlugin.log(Level.INFO, p.getName()
									+ " left " + g.getName() + " via the gui");
							g.removeMember(p.getUniqueId());
							p.sendMessage(ChatColor.GREEN + "You have left "
									+ g.getName());
						}
					}, 11);
					confirmInv.setSlot(new Clickable(no) {

						@Override
						public void clicked(Player p) {
							showScreen();
						}
					}, 15);
					confirmInv.setSlot(new DecorationStack(info), 4);
					confirmInv.showInventory(p);
				}
			};
		}
		return c;
	}

	private Clickable getInfoStack() {
		Clickable c;
		ItemStack is = new ItemStack(Material.PAPER);
		ItemUtils.setDisplayName(is, ChatColor.GOLD + "Stats for " + g.getName());
		ItemUtils.addLore(is,
				ChatColor.DARK_AQUA + "Your current rank: " + ChatColor.YELLOW
						+ PlayerType.getNiceRankName(g.getPlayerType(p.getUniqueId())));
		boolean hasGroupStatsPerm = gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("GROUPSTATS"));
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MEMBERS"))
				|| hasGroupStatsPerm) {
			ItemUtils.addLore(
					is,
					ChatColor.AQUA
							+ String.valueOf(g
							.getAllMembers(PlayerType.MEMBERS).size())
							+ " members");
		}
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MODS"))
				|| hasGroupStatsPerm) {
			ItemUtils.addLore(
					is,
					ChatColor.AQUA
							+ String.valueOf(g.getAllMembers(PlayerType.MODS)
							.size()) + " mods");
		}
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("ADMINS"))
				|| hasGroupStatsPerm) {
			ItemUtils.addLore(
					is,
					ChatColor.AQUA
							+ String.valueOf(g.getAllMembers(PlayerType.ADMINS)
							.size()) + " admins");
		}
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("OWNER"))
				|| hasGroupStatsPerm) {
			ItemUtils.addLore(
					is,
					ChatColor.AQUA
							+ String.valueOf(g.getAllMembers(PlayerType.OWNER)
							.size()) + " owner");
		}
		if (hasGroupStatsPerm) {
			ItemUtils.addLore(
					is,
					ChatColor.DARK_AQUA
							+ String.valueOf(g.getAllMembers().size())
							+ " total group members");
			ItemUtils.addLore(is, ChatColor.DARK_AQUA + "Group owner: "
					+ ChatColor.YELLOW + NameAPI.getCurrentName(g.getOwner()));
		}
		c = new DecorationStack(is);
		return c;
	}

	/**
	 * Utility to get a "nice" version of the rank the given player has in the
	 * group this gui is based on
	 *
	 * @param uuid Player whose rank name should be checked
	 * @return Rankname of the given player ready to be put into the gui or null
	 * if the given UUID was null or the player didn't have an explicit
	 * rank in the group
	 */
	private String getRankName(UUID uuid) {
		if (uuid == null) {
			return null;
		}
		PlayerType pType = g.getPlayerType(uuid);
		if (pType == null) {
			return null;
		}
		return PlayerType.getNiceRankName(pType);
	}

	/**
	 * Gets the permission needed to make changes to the given PlayerType
	 *
	 * @param pt PlayerType to get permission for
	 * @return Permission belonging to the given PlayerType or null if the
	 * permission was NOT_BLACKLISTED or null
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
		String res = PlayerType.getID(oldRank) <= PlayerType.getID(newRank) ? "promote"
				: "demote";
		if (upperCaseFirstLetter) {
			return res.substring(0, 1).toUpperCase() + res.substring(1);
		}
		return res;
	}

	private List<Clickable> getRecursiveInheritedMembers(Group g) {
		List<Clickable> clicks = new ArrayList<>();
		if (g.hasSuperGroup()) {
			clicks.addAll(getRecursiveInheritedMembers(g.getSuperGroup()));
		}
		for (UUID uuid : g.getAllMembers()) {
			ItemStack is;
			switch (g.getPlayerType(uuid)) {
				case MEMBERS:
					is = new ItemStack(Material.LEATHER_CHESTPLATE);
					break;
				case MODS:
					is = modStack();
					break;
				case ADMINS:
					is = new ItemStack(Material.IRON_CHESTPLATE);
					break;
				case OWNER:
					is = new ItemStack(Material.DIAMOND_CHESTPLATE);
					break;
				default:
					continue;
			}
			ItemUtils.setDisplayName(is, NameAPI.getCurrentName(uuid));
			ItemUtils.addLore(is, ChatColor.AQUA + "Inherited "
					+ getRankName(uuid) + " from " + g.getName());
			clicks.add(new DecorationStack(is));
		}
		return clicks;
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
