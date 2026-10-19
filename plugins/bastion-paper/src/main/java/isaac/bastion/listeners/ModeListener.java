package isaac.bastion.listeners;

import isaac.bastion.Bastion;
import isaac.bastion.BastionBlock;
import isaac.bastion.BastionType;
import isaac.bastion.Permissions;
import isaac.bastion.utils.BastionSettingManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;
import vg.civcraft.mc.civmodcore.players.settings.SettingChangeListener;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;
import vg.civcraft.mc.namelayer.NameLayerAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ModeListener implements Listener {

    private BottomLine bsiBottomLine;
    private CivScoreBoard bsiBoard;
    private BastionSettingManager settingMan;

    private PermissionType placePerm;

    public ModeListener() {
        this.bsiBoard = ScoreBoardAPI.createBoard("bsiDisplay");
        this.bsiBottomLine = BottomLineAPI.createBottomLine("bsiDisplay", 3);
        this.settingMan = Bastion.getSettingManager();
        settingMan.getBsiOverlay().registerListener(new SettingChangeListener<Boolean>() {
            @Override
            public void handle(UUID player, PlayerSetting<Boolean> setting, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    updateDisplayedInformation(Bukkit.getPlayer(player), Bukkit.getPlayer(player).getLocation());
                    return;
                }
                Player p = Bukkit.getPlayer(player);
                if (p != null) {
                    hideAll(p);
                }
            }
        });
        settingMan.getBsiLocation().registerListener(new SettingChangeListener<String>() {
            @Override
            public void handle(UUID player, PlayerSetting<String> setting, String oldValue, String newValue) {
                updateDisplayedInformation(Bukkit.getPlayer(player), Bukkit.getPlayer(player).getLocation());
            }
        });
        this.placePerm = PermissionType.getPermission(Permissions.BASTION_PLACE);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent pje) {
        updateDisplayedInformation(pje.getPlayer(), pje.getPlayer().getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent pme) {
        Location to = pme.getTo();
        if (to == null) {
            return;
        }
        Location from = pme.getFrom();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
            && from.getBlockZ() == to.getBlockZ() && from.getWorld().equals(to.getWorld())) {
            return;
        }
        updateDisplayedInformation(pme.getPlayer(), to);
    }

    /**
     * This method handles the updating of the scoreboard/action bar as to whether
     * the player is in a bastion field or not.
     *
     * @param player   to update bastion status to
     * @param location to check for bastions
     */
    private void updateDisplayedInformation(Player player, Location location) {
        if (player == null || location == null) {
            return;
        }
        if (!settingMan.getBsiOverlay().getValue(player.getUniqueId())) {
            hideAll(player);
            return;
        }
        Set<BastionBlock> bastionBlocks = Bastion.getBastionManager().getBlockingBastions(location);
        if (bastionBlocks.isEmpty()) {
            if (settingMan.getShowNoBastion(player.getUniqueId())) {
                updateDisplaySetting(player, ChatColor.WHITE + "No Bastion");
            } else {
                hideAll(player);
            }
            return;
        }

        SortedMap<BastionType, Boolean> alliedBastions = new TreeMap<>();
        SortedMap<BastionType, Boolean> enemyBastions = new TreeMap<>();
        for (BastionBlock bastion : bastionBlocks) {
            if (NameLayerAPI.getGroupManager().hasAccess(bastion.getGroup(), player.getUniqueId(), placePerm)) {
                alliedBastions.merge(bastion.getType(), bastion.isMature(), Boolean::logicalAnd);
            } else {
                enemyBastions.merge(bastion.getType(), bastion.isMature(), Boolean::logicalAnd);
            }
        }
        if (!alliedBastions.isEmpty() && !enemyBastions.isEmpty()) {
            updateDisplaySetting(player,
                String.format("%s%sConflict Bastion %s[%s] %s[%s]", ChatColor.YELLOW, ChatColor.BOLD,
                    ChatColor.GREEN, buildBastionTypeList(alliedBastions, ChatColor.GREEN),
                    ChatColor.RED, buildBastionTypeList(enemyBastions, ChatColor.RED)));
            return;
        }
        if (!alliedBastions.isEmpty()) {
            updateDisplaySetting(player,
                String.format("%sAlly Bastion [%s]", ChatColor.GREEN, buildBastionTypeList(alliedBastions, ChatColor.GREEN)));
            return;
        }
        updateDisplaySetting(player, String.format("%s%sHostile Bastion [%s]", ChatColor.RED, ChatColor.BOLD,
            buildBastionTypeList(enemyBastions, ChatColor.RED)));
    }

    private String buildBastionTypeList(SortedMap<BastionType, Boolean> types, ChatColor baseColor) {
        List<String> identifiers = new ArrayList<>();
        for (Map.Entry<BastionType, Boolean> entry : types.entrySet()) {
            String name = entry.getKey().getOverlayName();
            if (!entry.getValue()) {
                name = ChatColor.ITALIC + name + baseColor;
            }
            identifiers.add(name);
        }
        return String.join(",", identifiers);
    }

    /**
     * This method simply updates the players information
     *
     * @param player     to update scoreboard/action bar on
     * @param locSetting PlayerSetting of where to display information
     * @param state      Value of the PlayerSetting
     * @param text       Text to display
     */
    private void updateDisplaySetting(Player player, String text) {
        DisplayLocationSetting locSetting = settingMan.getBsiLocation();
        if (locSetting.showOnActionbar(player.getUniqueId())) {
            bsiBottomLine.updatePlayer(player, text);
        }
        if (locSetting.showOnSidebar(player.getUniqueId()) && !text.equals(bsiBoard.get(player))) {
            bsiBoard.set(player, text);
        }
    }

    private void hideAll(Player player) {
        bsiBottomLine.removePlayer(player);
        bsiBoard.hide(player);
    }
}
