package vg.civcraft.mc.namelayer.gui;

import io.papermc.paper.registry.data.dialog.input.DialogInput;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.chat.dialog.DialogManager;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.commands.InvitePlayer;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InvitationGUI extends AbstractGroupGUI {

    private PlayerType selectedType;
    private MainGroupGUI parent;

    public InvitationGUI(Group g, Player p, MainGroupGUI parent) {
        super(g, p);
        this.parent = parent;
        showScreen();
    }

    private void showScreen() {
        ClickableInventory ci = new ClickableInventory(27, g.getName());

        ItemStack explain = new ItemStack(Material.PAPER);
        ItemUtils.setDisplayName(explain, ChatColor.GOLD + "Select an option");
        ItemUtils.addLore(explain, ChatColor.AQUA + "Please select the rank ", ChatColor.AQUA + "you want the invited player to have");
        ci.setSlot(new DecorationStack(explain), 4);
        ci.setSlot(produceOptionStack(Material.LEATHER_CHESTPLATE, "member", PlayerType.MEMBERS, PermissionType.getPermission("MEMBERS")), 10);
        ci.setSlot(produceOptionStack(modMat(), "mod", PlayerType.MODS, PermissionType.getPermission("MODS")), 12);
        ci.setSlot(produceOptionStack(Material.IRON_CHESTPLATE, "admin", PlayerType.ADMINS, PermissionType.getPermission("ADMINS")), 14);
        ci.setSlot(produceOptionStack(Material.DIAMOND_CHESTPLATE, "owner", PlayerType.OWNER, PermissionType.getPermission("OWNER")), 16);
        ci.showInventory(p);
    }

    private Clickable produceOptionStack(Material item, String niceRankName, final PlayerType pType, PermissionType perm) {
        ItemStack is = new ItemStack(item);
        ItemMeta im = is.getItemMeta();
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        is.setItemMeta(im);
        ItemUtils.setDisplayName(is, ChatColor.GOLD + "Invite as " + niceRankName);
        Clickable c;
        if (gm.hasAccess(g, p.getUniqueId(), perm)) {
            c = new Clickable(is) {

                @Override
                public void clicked(Player arg0) {
                    final String PLAYER_ID = "player_name";
                    DialogManager.showDialog(
                        arg0,
                        Component.text("Group Invite"),
                        List.of(),
                        List.of(
                            DialogInput.text(PLAYER_ID, Component.text("Player name:", NamedTextColor.GOLD))
                                .maxLength(16)
                                .build()
                        ),
                        (view) -> {
                            if (!gm.hasAccess(g, p.getUniqueId(), MainGroupGUI.getAccordingPermission(selectedType))) {
                                p.sendMessage(ChatColor.RED + "You do not have permission to invite a player to this rank");
                                parent.showScreen();
                                return;
                            }
                            final String s = view.getText(PLAYER_ID);
                            UUID inviteUUID = NameLayerAPI.getUUID(s);
                            if (inviteUUID == null) {
                                p.sendMessage(ChatColor.RED + "The player " + s + " doesn't exist");
                                parent.showScreen();
                                return;
                            }
                            if (g.isMember(inviteUUID)) { // So a player can't demote someone who is above them.
                                p.sendMessage(ChatColor.RED + NameLayerAPI.getCurrentName(inviteUUID) + " is already a member of " + g.getName());
                                parent.showScreen();
                                return;
                            }
                            if (NameLayerPlugin.getBlackList().isBlacklisted(g, inviteUUID)) {
                                p.sendMessage(ChatColor.RED + NameLayerAPI.getCurrentName(inviteUUID) + " is currently blacklisted, you have to unblacklist him before inviting him to the group");
                                parent.showScreen();
                                return;
                            }
                            NameLayerPlugin.log(Level.INFO,
                                p.getName() + " invited "
                                    + NameLayerAPI.getCurrentName(inviteUUID)
                                    + " to group " + g.getName()
                                    + " via the gui");

                            InvitePlayer.sendInvitation(g, pType, inviteUUID, p.getUniqueId(), false, result -> {
                                if (result.success()) {
                                    p.sendMessage(ChatColor.GREEN + "Invited " + NameLayerAPI.getCurrentName(inviteUUID) + " as " + PlayerType.getNiceRankName(pType));
                                } else {
                                    p.sendMessage(ChatColor.RED + result.message());
                                }
                                parent.showScreen();
                            });
                        }
                    );
                }
            };
        } else {
            ItemUtils.addLore(is, ChatColor.RED + "You don't have permission to invite " + niceRankName);
            c = new DecorationStack(is);
        }
        return c;
    }
}
