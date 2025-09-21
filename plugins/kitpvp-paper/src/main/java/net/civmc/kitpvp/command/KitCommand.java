package net.civmc.kitpvp.command;

import java.util.logging.Level;
import net.civmc.kitpvp.KitApplier;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.anvil.AnvilGui;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.gui.KitListGui;
import net.civmc.kitpvp.ranked.RankedDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class KitCommand implements CommandExecutor {

    private final KitPvpDao dao;
    private final RankedDao rankedDao;
    private final AnvilGui anvilGui;

    public KitCommand(KitPvpDao dao, RankedDao rankedDao, AnvilGui anvilGui) {
        this.dao = dao;
        this.rankedDao = rankedDao;
        this.anvilGui = anvilGui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (player.getWorld().getName().startsWith("rankedarena.")) {
            player.sendMessage(Component.text("Kit command is deactivated in ranked arenas.", NamedTextColor.RED));
            return true;
        }

        KitPvpPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
        if (args.length > 0 && args[0].equalsIgnoreCase("copy")) {
            if (args.length < 3 || args.length > 4) {
                return false;
            }

            boolean isPublic = args[1].equalsIgnoreCase("public");
            boolean isAdmin = player.hasPermission("kitpvp.admin");

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Kit kit;
                String name;
                if (isPublic) {
                    name = "public kits";
                    kit = dao.getKit(args[2], null);
                } else {
                    OfflinePlayer copiedPlayer = Bukkit.getOfflinePlayer(args[1]);
                    name = copiedPlayer.getName();
                    kit = dao.getKit(args[2], copiedPlayer.getUniqueId());
                }
                if (kit != null) {
                    try {
                        String kitName = args.length == 4 ? args[3] : args[2];
                        if (!isAdmin) {
                            int ownedKits = 0;
                            for (Kit playerKit : dao.getKits(player.getUniqueId())) {
                                if (!playerKit.isPublic()) {
                                    ownedKits++;
                                }
                            }
                            if (ownedKits >= 50) {
                                player.sendMessage(Component.text("You cannot create any more kits!", NamedTextColor.RED));
                                return;
                            }
                        }
                        if (!Kit.checkValidName(player, kitName)) {
                            return;
                        }
                        Kit newKit = dao.createKit(kitName, player.getUniqueId());
                        if (newKit == null) {
                            player.sendMessage(Component.text("You already have a kit named '" + kitName + "'!", NamedTextColor.RED));
                            player.sendMessage(Component.text("Try using a different kit name: /" + label + " copy " + args[1] + " " + args[2] + " <your kit name>", NamedTextColor.RED));
                            return;
                        }
                        dao.updateKit(newKit.id(), kit.icon(), kit.items());
                        if (args.length == 4) {
                            player.sendMessage(Component.text("Copied kit '" + kit.name() + "' from " + name + " as '" + newKit.name() + "'", NamedTextColor.GREEN));
                        } else {
                            player.sendMessage(Component.text("Copied kit '" + kit.name() + "' from " + name, NamedTextColor.GREEN));
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Error copying kit", e);
                        player.sendMessage(Component.text("Error copying kit", NamedTextColor.RED));
                    }
                } else {
                    player.sendMessage(Component.text((isPublic ? "Public kit" : "Kit") + " '" + args[2] + "' not found", NamedTextColor.RED));
                }
            });
            return true;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("load")) {
            if (args.length < 2 || args.length > 3) {
                return false;
            }

            boolean isPublic = args.length == 3 && args[1].equalsIgnoreCase("public");
            String kitName = isPublic ? args[2] : args[1];

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Kit kit = dao.getKit(kitName, isPublic ? null : player.getUniqueId());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (kit != null) {
                        KitApplier.applyKit(kit, player);
                    } else {
                        player.sendMessage(Component.text((isPublic ? "Public kit" : "Kit") + " '" + args[1] + "' not found", NamedTextColor.RED));
                    }
                });
            });
            return true;
        } else if (args.length == 0) {
            new KitListGui(dao, rankedDao, anvilGui, player);
            return true;
        }
        return false;
    }
}
