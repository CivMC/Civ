package net.civmc.kitpvp.ranked;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.listeners.PearlCoolDownListener;
import net.civmc.kitpvp.KitApplier;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.arena.ArenaManager;
import net.civmc.kitpvp.arena.data.Arena;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.TemporaryNodeMergeStrategy;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RankedQueueManager {

    private final KitPvpDao kitDao;
    private final RankedDao dao;
    private final ArenaManager arenaManager;

    private final Arena arena;

    private final SequencedMap<Player, Double> queued = new LinkedHashMap<>();

    private final List<RankedMatch> matches = new ArrayList<>();

    public RankedQueueManager(KitPvpDao kitDao, RankedDao dao, ArenaManager arenaManager, Arena arena) {
        this.kitDao = kitDao;
        this.dao = dao;
        this.arenaManager = arenaManager;

        this.arena = arena;

        Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            try {
                for (RankedMatch match : matches) {
                    if (Instant.now().isAfter(match.started().plus(30, ChronoUnit.MINUTES))) {
                        endMatch(match, null);
                    }
                }
            } catch (RuntimeException ex) {
                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Ticking matches", ex);
            }
        }, 20, 20);
        Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            try {
                for (RankedDao.Rank rank : dao.getTop(10)) {
                    UserManager userManager = LuckPermsProvider.get().getUserManager();
                    userManager.loadUser(rank.player()).thenAccept(user -> {
                        if (user == null) {
                            return;
                        }
                        user.data().add(
                            PermissionNode.builder()
                                .permission("ajqueue.priority.8")
                                .expiry(5, TimeUnit.MINUTES)
                                .build(),
                            TemporaryNodeMergeStrategy.REPLACE_EXISTING_IF_DURATION_LONGER);
                        userManager.saveUser(user);
                    });
                }
            } catch (RuntimeException ex) {
                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Ticking queue priority", ex);
            }
        }, 20 * 60, 20 * 60);
    }

    public boolean isInQueue(Player player) {
        return this.queued.containsKey(player);
    }

    public void leaveQueue(Player player) {
        this.queued.remove(player);
    }

    public void loseMatch(Player player) {
        RankedMatch match = null;
        UUID winner = null;
        for (Iterator<RankedMatch> iterator = matches.iterator(); iterator.hasNext(); ) {
            RankedMatch rankedMatch = iterator.next();
            if (rankedMatch.player().equals(player)) {
                iterator.remove();
                winner = rankedMatch.opponent().getUniqueId();
                match = rankedMatch;
                break;
            } else if (rankedMatch.opponent().equals(player)) {
                iterator.remove();
                winner = rankedMatch.player().getUniqueId();
                match = rankedMatch;
                break;
            }
        }
        if (match == null) {
            return;
        }

        endMatch(match, winner);
    }

    private String formatChange(double change) {
        DecimalFormat format = new DecimalFormat("#");
        format.setPositivePrefix("+");
        return format.format(change);
    }

    private void endMatch(RankedMatch match, UUID winner) {
        Player player = match.player();
        Player opponent = match.opponent();
        UUID matchPlayer = player.getUniqueId();
        UUID matchOpponent = opponent.getUniqueId();

        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            dao.updateElo(matchPlayer, matchOpponent, winner);
        });

        double playerElo = match.playerElo();
        double opponentElo = match.opponentElo();

        Elo.EloChange playerChange = Elo.getChange(match.playerElo(), match.opponentElo());
        Elo.EloChange opponentChange = Elo.getChange(match.opponentElo(), match.playerElo());

        if (winner == null) {
            player.sendMessage(Component.text("The match has ended in a draw because it timed out! (30 minutes)", NamedTextColor.GRAY));
            opponent.sendMessage(Component.text("The match has ended in a draw because it timed out! (30 minutes)", NamedTextColor.GRAY));
            playerElo += playerChange.draw();
            opponentElo += opponentChange.draw();
        } else if (winner.equals(player.getUniqueId())) {
            player.sendMessage(Component.text("You have won!", NamedTextColor.GREEN));
            opponent.sendMessage(Component.text("You lost", NamedTextColor.RED));
            playerElo += playerChange.win();
            opponentElo += opponentChange.loss();
        } else {
            opponent.sendMessage(Component.text("You have won!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("You lost", NamedTextColor.RED));
            playerElo += playerChange.loss();
            opponentElo += opponentChange.win();
        }

        player.sendMessage(Component.text("Your elo is now ", NamedTextColor.GRAY)
            .append(Component.text(Math.round(playerElo), NamedTextColor.WHITE))
            .append(Component.text(" (change: " + formatChange(playerElo - match.playerElo()) + ")", NamedTextColor.GRAY)));
        opponent.sendMessage(Component.text("Your elo is now ", NamedTextColor.GRAY)
            .append(Component.text(Math.round(opponentElo), NamedTextColor.WHITE))
            .append(Component.text(" (change: " + formatChange(opponentElo - match.opponentElo()) + ")", NamedTextColor.GRAY)));

        arenaManager.deleteLoadedArena(match.arena());
    }

    public void joinQueue(Player player) {
        JavaPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int kitId = dao.getKit(player.getUniqueId());
            double elo = dao.getElo(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (kitId == -1) {
                    player.sendMessage(Component.text("You cannot join the ranked queue because you do not have a kit selected!", NamedTextColor.RED));
                    return;
                }
                Kit kit = kitDao.getKit(kitId);
                int points = KitCost.getCost(kit.items());
                if (points > KitCost.MAX_POINTS) {
                    player.sendMessage(Component.text("Your kit is too expensive! Kits may be a maximum of " + KitCost.MAX_POINTS + ", your kit costs " + points + " points.", NamedTextColor.RED));
                    return;
                }

                if (player.isOnline()) {
                    queued.put(player, elo);
                    scanQueue();
                }
                player.sendMessage(Component.text("You have joined the ranked queue", NamedTextColor.YELLOW));
            });
        });
    }

    private void scanQueue() {
        // TODO make this algorithm better at preferring earlier players
        List<Map.Entry<Player, Double>> entries = new ArrayList<>(queued.sequencedEntrySet());
        for (int i = 0; i < entries.size(); i++) {
            for (int j = i + 1; j < entries.size(); j++) {
                Map.Entry<Player, Double> playerEntry = entries.get(i);
                Map.Entry<Player, Double> opponentEntry = entries.get(j);
                if (Math.abs(playerEntry.getValue() - opponentEntry.getValue()) < 300) {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        var temp = playerEntry;
                        playerEntry = opponentEntry;
                        opponentEntry = temp;
                    }
                    startMatch(playerEntry.getKey(), playerEntry.getValue(), opponentEntry.getKey(), opponentEntry.getValue());
                    break;
                }
            }

        }
    }

    private void startMatch(Player player, double playerElo, Player opponent, double opponentElo) {
        boolean created = arenaManager.createRankedArena(arena, loaded -> {
            World world = Bukkit.getWorld(arenaManager.getArenaName(loaded));
            Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                Kit playerKit = kitDao.getKit(dao.getKit(player.getUniqueId()));
                Kit opponentKit = kitDao.getKit(dao.getKit(opponent.getUniqueId()));
                Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                    if (!player.isOnline() || !opponent.isOnline() || playerKit == null || opponentKit == null || KitCost.getCost(playerKit.items()) > KitCost.MAX_POINTS || KitCost.getCost(opponentKit.items()) > KitCost.MAX_POINTS) {
                        // TODO better error handling
                        player.sendMessage(Component.text("An error occurred, please requeue.", NamedTextColor.RED));
                        arenaManager.deleteLoadedArena(loaded);
                        return;
                    }

                    player.teleport(new Location(world, 42.5, 72, 33.5, -45, 0));
                    opponent.teleport(new Location(world, 96.5, 72, 89.5, 135, 0));
                    player.setGameMode(GameMode.SURVIVAL);
                    opponent.setGameMode(GameMode.SURVIVAL);
                    KitApplier.applyKit(playerKit, player);
                    KitApplier.applyKit(opponentKit, opponent);
                    matches.add(new RankedMatch(player, playerElo, opponent, opponentElo, loaded, Instant.now()));

                    Elo.EloChange playerChange = Elo.getChange(playerElo, opponentElo);
                    Elo.EloChange opponentChange = Elo.getChange(opponentElo, playerElo);

                    player.sendMessage(Component.text("You (elo: " + ((int) Math.round(playerElo)) + ") have been paired with " + opponent.getName() + " (elo: " + ((int) Math.round(opponentElo)) + ")", NamedTextColor.YELLOW));
                    player.sendMessage(Component.text("Win: +" + Math.round(playerChange.win()) + " elo, lose: " + Math.round(playerChange.loss()) + " elo", NamedTextColor.GRAY));

                    opponent.sendMessage(Component.text("You (elo: " + ((int) Math.round(opponentElo)) + ") have been paired with " + player.getName() + " (elo: " + ((int) Math.round(playerElo)) + ")", NamedTextColor.YELLOW));
                    opponent.sendMessage(Component.text("Win: +" + Math.round(opponentChange.win()) + " elo, lose: " + Math.round(opponentChange.loss()) + " elo", NamedTextColor.GRAY));

                    PearlCoolDownListener cooldown = Finale.getPlugin().getPearlCoolDownListener();
                    if (cooldown != null) {
                        cooldown.putOnCooldown(player);
                        cooldown.putOnCooldown(opponent);
                    }

                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0f);
                    opponent.playSound(opponent.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0f);
                });
            });
        });
        if (created) {
            queued.remove(player);
            queued.remove(opponent);
        }
    }
}
