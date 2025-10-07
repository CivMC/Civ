package net.civmc.kitpvp.arena;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.civmc.kitpvp.arena.data.Arena;
import net.civmc.kitpvp.arena.data.ArenaDao;
import net.civmc.kitpvp.arena.gui.ArenaGui;
import net.civmc.kitpvp.ranked.RankedDao;
import net.civmc.kitpvp.ranked.RankedQueueManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class ArenaCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ArenaDao dao;
    private final RankedDao rankedDao;
    private final RankedQueueManager rankedQueueManager;
    private final ArenaManager manager;
    private final PrivateArenaListener privateArenaListener;

    public ArenaCommand(JavaPlugin plugin, ArenaDao dao, RankedDao rankedDao, RankedQueueManager rankedQueueManager, ArenaManager manager, PrivateArenaListener privateArenaListener) {
        this.plugin = plugin;
        this.dao = dao;
        this.rankedDao = rankedDao;
        this.rankedQueueManager = rankedQueueManager;
        this.manager = manager;
        this.privateArenaListener = privateArenaListener;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("list")) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    List<String> arenas = manager.listArenas();
                    TextComponent message = Component.text("Available arenas: ", NamedTextColor.GOLD);
                    for (String arena : arenas) {
                        message = message.append(Component.text("\n- " + arena, NamedTextColor.YELLOW));
                    }
                    player.sendMessage(message);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Error listing arenas", e);
                    player.sendMessage(Component.text("Error listing arenas", NamedTextColor.RED));
                }
            });
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("create")) {
            if (args.length != 3) {
                return false;
            }
            if (!player.hasPermission("kitpvp.admin")) {
                player.sendMessage(Component.text("No permission", NamedTextColor.RED));
                return true;
            }

            String arenaName = args[1];
            Material icon = Material.matchMaterial(args[2]);
            if (icon == null) {
                player.sendMessage(Component.text("Invalid material for icon", NamedTextColor.RED));
                return true;
            }

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    if (!manager.listArenas().contains(arenaName)) {
                        player.sendMessage(Component.text("Could not find world with that arena name", NamedTextColor.RED));
                        return;
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                boolean success = dao.newArena(new Arena(arenaName, null, null, player.getLocation(), icon));
                if (success) {
                    player.sendMessage(Component.text("Created arena", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Could not create arena. Does an arena with that name already exist?", NamedTextColor.RED));
                }
            });
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("delete")) {
            if (args.length != 2) {
                return false;
            }
            if (!player.hasPermission("kitpvp.admin")) {
                player.sendMessage(Component.text("No permission", NamedTextColor.RED));
                return true;
            }

            String arenaName = args[1];

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean success = dao.deleteArena(arenaName);
                if (success) {
                    player.sendMessage(Component.text("Deleted arena", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Could not delete arena. Does it exist?", NamedTextColor.RED));
                }
            });
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("displayname")) {
            if (args.length < 3) {
                return false;
            }
            if (!player.hasPermission("kitpvp.admin")) {
                player.sendMessage(Component.text("No permission", NamedTextColor.RED));
                return true;
            }

            String arenaName = args[1];
            String displayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean success = dao.setDisplayName(arenaName, displayName);
                if (success) {
                    player.sendMessage(Component.text("Set display name of arena", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Could not set display name of arena. Does it exist?", NamedTextColor.RED));
                }
            });
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("category")) {
            if (args.length < 3) {
                return false;
            }
            if (!player.hasPermission("kitpvp.admin")) {
                player.sendMessage(Component.text("No permission", NamedTextColor.RED));
                return true;
            }

            String arenaName = args[1];
            String category = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                boolean success = dao.setCategory(arenaName, category);
                if (success) {
                    player.sendMessage(Component.text("Set category of arena", NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("Could not set category of arena. Does it exist?", NamedTextColor.RED));
                }
            });
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("cap")) {
            if (args.length < 2) {
                return false;
            }
            if (!player.hasPermission("kitpvp.admin")) {
                player.sendMessage(Component.text("No permission", NamedTextColor.RED));
                return true;
            }

            int arenas = Integer.parseInt(args[1]);
            manager.setMaxArenas(arenas);
            player.sendMessage(Component.text("Set arena cap to " + arenas, NamedTextColor.GREEN));
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("add")) {
            if (args.length < 2) {
                return false;
            }

            LoadedArena playerArena = null;
            for (LoadedArena arena : manager.getArenas()) {
                if (!arena.ranked() && arena.owner().equals(player.getPlayerProfile())) {
                    playerArena = arena;
                    break;
                }
            }
            if (playerArena == null) {
                player.sendMessage(Component.text("You do not currently have an arena", NamedTextColor.RED));
                return true;
            }

            Player invited = Bukkit.getPlayer(args[1]);
            if (invited == null) {
                player.sendMessage(Component.text("Could not find a player with that name", NamedTextColor.RED));
                return true;
            }

            if (invited.equals(player)) {
                player.sendMessage(Component.text("You cannot invite yourself to your own arena", NamedTextColor.RED));
                return true;
            }

            List<PlayerProfile> invitedPlayers = playerArena.invitedPlayers();
            if (invitedPlayers == null) {
                player.sendMessage(Component.text("Your arena is public, so you cannot add any players", NamedTextColor.RED));
                return true;
            }
            if (invitedPlayers.contains(invited.getPlayerProfile())) {
                player.sendMessage(Component.text("You have already invited that player to your arena", NamedTextColor.RED));
                return true;
            }

            invitedPlayers.add(invited.getPlayerProfile());
            player.sendMessage(Component.text("Invited " + invited.getName() + " to your arena. They can join via /arena", NamedTextColor.GREEN));
            String suffix = player.getName().toLowerCase().endsWith("s") ? "'" : "'s";
            invited.sendMessage(Component.text("You have been invited to " + player.getName() + suffix + " private arena. You can join via /arena", NamedTextColor.GREEN));
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                return false;
            }

            LoadedArena playerArena = null;
            for (LoadedArena arena : manager.getArenas()) {
                if (!arena.ranked() && arena.owner().equals(player.getPlayerProfile())) {
                    playerArena = arena;
                    break;
                }
            }
            if (playerArena == null) {
                player.sendMessage(Component.text("You do not currently have an arena", NamedTextColor.RED));
                return true;
            }

            PlayerProfile removed = null;
            List<PlayerProfile> invitedPlayers = playerArena.invitedPlayers();
            if (invitedPlayers == null) {
                player.sendMessage(Component.text("Your arena is public, so you cannot add any players", NamedTextColor.RED));
                return true;
            }
            for (Iterator<PlayerProfile> iterator = invitedPlayers.iterator(); iterator.hasNext(); ) {
                PlayerProfile invitedPlayer = iterator.next();
                if (invitedPlayer.getName().equalsIgnoreCase(args[1])) {
                    iterator.remove();
                    removed = invitedPlayer;
                    break;
                }
            }
            if (removed == null) {
                player.sendMessage(Component.text("That player has not been invited to your arena", NamedTextColor.RED));
                return true;
            }

            player.sendMessage(Component.text("Removed " + removed.getName() + " from your arena.", NamedTextColor.GREEN));

            String suffix = player.getName().toLowerCase().endsWith("s") ? "'" : "'s";
            Player removedPlayer = Bukkit.getPlayer(removed.getId());
            if (removedPlayer != null) {
                removedPlayer.sendMessage(Component.text("You have been removed from " + player.getName() + suffix + " private arena.", NamedTextColor.YELLOW));
                privateArenaListener.remove(playerArena, removedPlayer);
            }
            return true;
        } else if (args.length == 0) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                double elo = rankedDao.getElo(player.getUniqueId());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    new ArenaGui(dao, rankedQueueManager, elo, manager).open(player);
                });
            });
            return true;
        }
        return false;
    }
}
