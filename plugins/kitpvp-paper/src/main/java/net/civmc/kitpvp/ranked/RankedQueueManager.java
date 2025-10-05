package net.civmc.kitpvp.ranked;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.listeners.PearlCoolDownListener;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import net.civmc.kitpvp.KitApplier;
import net.civmc.kitpvp.KitPvpPlugin;
import net.civmc.kitpvp.arena.ArenaManager;
import net.civmc.kitpvp.arena.data.Arena;
import net.civmc.kitpvp.kit.Kit;
import net.civmc.kitpvp.kit.KitCost;
import net.civmc.kitpvp.kit.KitPvpDao;
import net.civmc.kitpvp.spawn.SpawnProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RankedQueueManager {

    private final KitPvpDao kitDao;
    private final RankedDao dao;
    private final ArenaManager arenaManager;
    private final SpawnProvider spawnProvider;

    private final Arena arena;

    private final SequencedMap<Player, QueuedPlayer> queued = new LinkedHashMap<>();
    private final SequencedMap<Player, QueuedPlayer> unrankedQueued = new LinkedHashMap<>();

    private final Map<UUID, RecentMatch> recentMatches = new HashMap<>();

    private final List<RankedMatch> matches = new ArrayList<>();

    private final AutoQueueSetting setting;

    private final RankedPlayers players;

    public RankedQueueManager(KitPvpDao kitDao, RankedDao dao, ArenaManager arenaManager, SpawnProvider spawnProvider, Arena arena, RankedPlayers players) {
        this.kitDao = kitDao;
        this.dao = dao;
        this.arenaManager = arenaManager;
        this.spawnProvider = spawnProvider;

        this.arena = arena;
        this.players = players;

        KitPvpMenu menu = new KitPvpMenu();
        this.setting = new AutoQueueSetting(JavaPlugin.getPlugin(KitPvpPlugin.class));
        menu.registerToParentMenu();
        menu.registerSetting(this.setting);

        Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            try {
                for (Iterator<RankedMatch> iterator = matches.iterator(); iterator.hasNext(); ) {
                    RankedMatch match = iterator.next();
                    if (Instant.now().isAfter(match.started().plus(10, ChronoUnit.MINUTES))) {
                        iterator.remove();
                        mostPotsWinsOrDraw(match);
                    }
                }
                scanQueue();
                scanQueueUnranked();
                for (RankedMatch match : matches) {
                    if (match.opponent().getY() > 90) {
                        match.opponent().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 50, 1, false, false));
                        match.opponent().sendMessage(Component.text("You are too high!", NamedTextColor.RED));
                    }
                    if (match.player().getY() > 90) {
                        match.player().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 50, 1, false, false));
                        match.player().sendMessage(Component.text("You are too high!", NamedTextColor.RED));
                    }
                }
            } catch (RuntimeException ex) {
                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Ticking matches", ex);
            }
        }, 20, 20);
        Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            try {
                for (Map.Entry<Player, QueuedPlayer> player : queued.entrySet()) {
                    if (player.getValue().valid()) {
                        player.getKey().sendMessage(Component.text("You are queued for ranked. Type /ranked to leave the queue", NamedTextColor.YELLOW));
                    }
                }
                for (Map.Entry<Player, QueuedPlayer> player : unrankedQueued.entrySet()) {
                    if (player.getValue().valid()) {
                        player.getKey().sendMessage(Component.text("You are queued for unranked. Type /unranked to leave the queue", NamedTextColor.YELLOW));
                    }
                }
            } catch (RuntimeException ex) {
                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().log(Level.WARNING, "Ticking queued players", ex);
            }
        }, 300, 300);
        /*Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
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
        }, 20 * 60, 20 * 60);*/
    }

    public RankedMatch getMatch(Player player) {
        for (RankedMatch match : matches) {
            if (match.player().equals(player) || match.opponent().equals(player)) {
                return match;
            }
        }
        return null;
    }

    private void mostPotsWinsOrDraw(RankedMatch match) {
        Player player = match.player();
        Player opponent = match.opponent();

        player.sendMessage(Component.text("The match has timed out! The player who dealt the most damage will win.", NamedTextColor.YELLOW));
        opponent.sendMessage(Component.text("The match has timed out! The player who dealt the most damage will win.", NamedTextColor.YELLOW));

        if (match.getOpponentDamageDealt() > match.getPlayerDamageDealt()) {
            endMatch(match, match.opponent().getUniqueId());
        } else if (match.getOpponentDamageDealt() < match.getPlayerDamageDealt()) {
            endMatch(match, match.player().getUniqueId());
        } else {
            endMatch(match, null);
        }
    }

    public boolean isInQueue(Player player) {
        QueuedPlayer queued = this.queued.get(player);
        return queued != null && queued.valid();
    }

    public boolean leaveQueue(Player player) {
        if (this.queued.remove(player) != null) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s left the ranked queue".formatted(player.getName()));
            return true;
        }
        return false;
    }

    public boolean isInUnrankedQueue(Player player) {
        QueuedPlayer queued = this.unrankedQueued.get(player);
        return queued != null && queued.valid();
    }

    public boolean leaveUnrankedQueue(Player player) {
        if (this.unrankedQueued.remove(player) != null) {
            JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s left the unranked queue".formatted(player.getName()));
            return true;
        }
        return false;
    }

    public boolean loseMatch(Player player) {
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
            return false;
        }

        endMatch(match, winner);
        return true;
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

        KitApplier.reset(player);
        player.setFallDistance(0);
        player.teleport(spawnProvider.getSpawn());
        KitApplier.reset(opponent);
        opponent.setFallDistance(0);
        opponent.teleport(spawnProvider.getSpawn());

        double playerElo = match.playerElo();
        double opponentElo = match.opponentElo();

        Elo.EloChange playerChange = Elo.getChange(match.playerElo(), match.opponentElo());
        Elo.EloChange opponentChange = Elo.getChange(match.opponentElo(), match.playerElo());

        if (winner == null) {
            player.sendMessage(Component.text("The match has ended in a draw because it timed out! (10 minutes)", NamedTextColor.GRAY));
            opponent.sendMessage(Component.text("The match has ended in a draw because it timed out! (10 minutes)", NamedTextColor.GRAY));

            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1, 1);
                opponent.playSound(opponent.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1, 1);
            }, 10);
            playerElo += playerChange.draw();
            opponentElo += opponentChange.draw();
            if (match.unranked()) {
                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s drew against %s".formatted(player.getName(), opponent.getName()));
            } else {
                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s (elo: %s, change: %s) drew against %s (elo: %s, change: %s)"
                    .formatted(player.getName(), Math.round(playerElo), formatChange(playerChange.draw()),
                        opponent.getName(), Math.round(opponentElo), formatChange(opponentChange.draw())));
            }
        } else if (winner.equals(player.getUniqueId())) {
            player.sendMessage(Component.text("You have won!", NamedTextColor.GREEN));
            opponent.sendMessage(Component.text("You lost", NamedTextColor.RED));
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                player.playSound(player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1, 1);
                opponent.playSound(opponent.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1, 1);
            }, 10);
            playerElo += playerChange.win();
            opponentElo += opponentChange.loss();
            if (match.unranked()) {
                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s won against %s".formatted(player.getName(), opponent.getName()));
            } else {
                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s (elo: %s, change: %s) won against %s (elo: %s, change: %s)"
                    .formatted(player.getName(), Math.round(playerElo), formatChange(playerChange.win()),
                        opponent.getName(), Math.round(opponentElo), formatChange(opponentChange.loss())));
            }
        } else {
            opponent.sendMessage(Component.text("You have won!", NamedTextColor.GREEN));
            player.sendMessage(Component.text("You lost", NamedTextColor.RED));
            Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1, 1);
                opponent.playSound(opponent.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1, 1);
            }, 10);
            playerElo += playerChange.loss();
            opponentElo += opponentChange.win();
            if (match.unranked()) {
                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s lost against %s".formatted(player.getName(), opponent.getName()));
            } else {
                JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s (elo: %s, change: %s) lost against %s (elo: %s, change: %s)"
                    .formatted(player.getName(), Math.round(playerElo), formatChange(playerChange.loss()),
                        opponent.getName(), Math.round(opponentElo), formatChange(opponentChange.win())));
            }
        }

        if (!match.unranked()) {
            player.sendMessage(Component.text("Your elo is now ", NamedTextColor.GRAY)
                .append(Component.text(Math.round(playerElo), NamedTextColor.WHITE))
                .append(Component.text(" (change: " + formatChange(playerElo - match.playerElo()) + ")", NamedTextColor.GRAY)));
            opponent.sendMessage(Component.text("Your elo is now ", NamedTextColor.GRAY)
                .append(Component.text(Math.round(opponentElo), NamedTextColor.WHITE))
                .append(Component.text(" (change: " + formatChange(opponentElo - match.opponentElo()) + ")", NamedTextColor.GRAY)));
            players.setElo(player.getUniqueId(), playerElo);
            players.setElo(opponent.getUniqueId(), opponentElo);
        }

        Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            if (!match.unranked()) {
                dao.updateElo(matchPlayer, matchOpponent, winner);
            }
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                arenaManager.deleteLoadedArena(match.arena());

                recentMatches.put(player.getUniqueId(), new RecentMatch(opponent.getUniqueId(), Instant.now()));
                recentMatches.put(opponent.getUniqueId(), new RecentMatch(player.getUniqueId(), Instant.now()));

                if (match.unranked()) {
                    joinUnrankedQueue(player, true);
                    joinUnrankedQueue(opponent, true);
                } else {
                    joinQueue(player, true);
                    joinQueue(opponent, true);
                }
            });
        });
    }

    public void joinQueue(Player player, boolean auto) {
        if (getMatch(player) != null) {
            return;
        }
        QueuedPlayer queue = this.queued.get(player);
        if (queue != null && !queue.valid() && !auto) {
            this.queued.put(player, new QueuedPlayer(queue.elo(), queue.joined(), false));
            player.sendMessage(Component.text("You have joined the ranked queue", NamedTextColor.YELLOW));
            return;
        }
        Integer value = setting.getValue(player);
        if (auto && (value == null || value <= 0)) {
            return;
        }
        JavaPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int kitId = dao.getKit(player.getUniqueId());
            double elo = dao.getElo(player.getUniqueId());
            Kit kit;
            if (kitId == -1) {
                kit = kitDao.getKit("Ranked", null);
            } else {
                kit = kitDao.getKit(kitId);
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (getMatch(player) != null) {
                    return;
                }
                if (kit == null) {
                    player.sendMessage(Component.text("You cannot join the ranked queue because you do not have a kit selected!", NamedTextColor.RED));
                    player.sendMessage(Component.text("Open a kit in /kit and click on the diamond sword to select it", NamedTextColor.RED));
                    return;
                }
                int points = KitCost.getCost(kit.items());
                if (points > KitCost.MAX_POINTS) {
                    player.sendMessage(Component.text("Your kit is too expensive! Kits may cost a maximum of " + KitCost.MAX_POINTS + " points, but your kit costs " + points + " points.", NamedTextColor.RED));
                    return;
                }

                if (player.isOnline()) {
                    if (auto) {
                        queued.put(player, new QueuedPlayer(elo, Instant.now().plusSeconds(value), true));
                    } else {
                        queued.put(player, new QueuedPlayer(elo, Instant.now(), false));
                    }
                    scanQueue();

                    JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s joined the ranked queue".formatted(player.getName()));
                }
                if (!auto) {
                    player.sendMessage(Component.text("You have joined the ranked queue", NamedTextColor.YELLOW));
                } else {
                    player.sendMessage(Component.text("You will rejoin the ranked queue in " + value + " seconds. Type /ranked leave to leave.", NamedTextColor.YELLOW));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.sendMessage(Component.text("Click to leave the ranked queue", NamedTextColor.RED, TextDecoration.BOLD, TextDecoration.UNDERLINED).hoverEvent(HoverEvent.showText(Component.text("Leave ranked queue"))).clickEvent(ClickEvent.runCommand("/ranked leave")));
                    }, 20);
                }
            });
        });
    }

    public void joinUnrankedQueue(Player player, boolean auto) {
        if (getMatch(player) != null) {
            return;
        }
        QueuedPlayer queue = this.unrankedQueued.get(player);
        if (queue != null && !queue.valid() && !auto) {
            this.unrankedQueued.put(player, new QueuedPlayer(queue.elo(), queue.joined(), false));
            player.sendMessage(Component.text("You have joined the unranked queue", NamedTextColor.YELLOW));
            return;
        }
        Integer value = setting.getValue(player);
        if (auto && (value == null || value <= 0)) {
            return;
        }
        JavaPlugin plugin = JavaPlugin.getPlugin(KitPvpPlugin.class);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int kitId = dao.getKit(player.getUniqueId());
            Kit kit;
            if (kitId == -1) {
                kit = kitDao.getKit("Ranked", null);
            } else {
                kit = kitDao.getKit(kitId);
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (kit == null) {
                    player.sendMessage(Component.text("You cannot join the ranked queue because you do not have a kit selected!", NamedTextColor.RED));
                    player.sendMessage(Component.text("Open a kit in /kit and click on the diamond sword to select it", NamedTextColor.RED));
                    return;
                }
                int points = KitCost.getCost(kit.items());
                if (points > KitCost.MAX_POINTS) {
                    player.sendMessage(Component.text("Your kit is too expensive! Kits may cost a maximum of " + KitCost.MAX_POINTS + " points, but your kit costs " + points + " points.", NamedTextColor.RED));
                    return;
                }

                if (player.isOnline()) {
                    if (auto) {
                        unrankedQueued.put(player, new QueuedPlayer(0, Instant.now().plusSeconds(value), true));
                    } else {
                        unrankedQueued.put(player, new QueuedPlayer(0, Instant.now(), false));
                    }
                    scanQueueUnranked();

                    JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s joined the unranked queue".formatted(player.getName()));
                }
                if (!auto) {
                    player.sendMessage(Component.text("You have joined the unranked queue", NamedTextColor.YELLOW));
                } else {
                    player.sendMessage(Component.text("You will rejoin the ranked queue in " + value + " seconds. Type /ranked to leave.", NamedTextColor.YELLOW));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        player.sendMessage(Component.text("Click to leave the unranked queue", NamedTextColor.RED, TextDecoration.BOLD, TextDecoration.UNDERLINED).hoverEvent(HoverEvent.showText(Component.text("Leave unranked queue"))).clickEvent(ClickEvent.runCommand("/unranked leave")));
                    }, 20);
                }
            });
        });
    }

    private void scanQueue() {
        // TODO make this algorithm better at preferring earlier players
        List<Map.Entry<Player, QueuedPlayer>> entries = new ArrayList<>(queued.sequencedEntrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Player, QueuedPlayer> playerEntry = entries.get(i);
            if (!playerEntry.getValue().valid()) {
                continue;
            }
            for (int j = i + 1; j < entries.size(); j++) {
                Map.Entry<Player, QueuedPlayer> opponentEntry = entries.get(j);
                if (!opponentEntry.getValue().valid()) {
                    continue;
                }

                int maxGap = 200;
                Instant earlier = playerEntry.getValue().joined();
                if (opponentEntry.getValue().joined().isBefore(earlier)) {
                    earlier = opponentEntry.getValue().joined();
                }
                double maxTime = earlier.until(Instant.now(), ChronoUnit.SECONDS);
                if (maxTime > 60) {
                    maxGap = 10000;
                } else if (maxTime > 40) {
                    maxGap = 400;
                } else if (maxTime > 20) {
                    maxGap = 300;
                }

                RecentMatch recent = recentMatches.get(playerEntry.getKey().getUniqueId());
                if (recent != null && recent.other().equals(opponentEntry.getKey().getUniqueId()) && recent.time().until(Instant.now(), ChronoUnit.SECONDS) < 45) {
                    continue;
                }

                RecentMatch recent2 = recentMatches.get(opponentEntry.getKey().getUniqueId());
                if (recent2 != null && recent2.other().equals(playerEntry.getKey().getUniqueId()) && recent2.time().until(Instant.now(), ChronoUnit.SECONDS) < 45) {
                    continue;
                }

                if (Math.abs(playerEntry.getValue().elo() - opponentEntry.getValue().elo()) < maxGap) {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        var temp = playerEntry;
                        playerEntry = opponentEntry;
                        opponentEntry = temp;
                    }
                    startMatch(playerEntry.getKey(), playerEntry.getValue().elo(), opponentEntry.getKey(), opponentEntry.getValue().elo(), false);
                    return;
                }
            }
        }
    }

    private void scanQueueUnranked() {
        List<Map.Entry<Player, QueuedPlayer>> entries = new ArrayList<>(unrankedQueued.sequencedEntrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Player, QueuedPlayer> playerEntry = entries.get(i);
            if (!playerEntry.getValue().valid()) {
                continue;
            }
            for (int j = i + 1; j < entries.size(); j++) {
                Map.Entry<Player, QueuedPlayer> opponentEntry = entries.get(j);
                if (!opponentEntry.getValue().valid()) {
                    continue;
                }

                RecentMatch recent = recentMatches.get(playerEntry.getKey().getUniqueId());
                if (recent != null && recent.other().equals(opponentEntry.getKey().getUniqueId()) && recent.time().until(Instant.now(), ChronoUnit.SECONDS) < 45) {
                    continue;
                }

                RecentMatch recent2 = recentMatches.get(opponentEntry.getKey().getUniqueId());
                if (recent2 != null && recent2.other().equals(playerEntry.getKey().getUniqueId()) && recent2.time().until(Instant.now(), ChronoUnit.SECONDS) < 45) {
                    continue;
                }

                if (ThreadLocalRandom.current().nextBoolean()) {
                    var temp = playerEntry;
                    playerEntry = opponentEntry;
                    opponentEntry = temp;
                }
                startMatch(playerEntry.getKey(), playerEntry.getValue().elo(), opponentEntry.getKey(), opponentEntry.getValue().elo(), true);
                return;
            }
        }
    }

    private void startMatch(Player player, double playerElo, Player opponent, double opponentElo, boolean unranked) {
        boolean created = arenaManager.createRankedArena(arena, loaded -> {
            World world = Bukkit.getWorld(arenaManager.getArenaName(loaded));
            Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                int playerKitId = dao.getKit(player.getUniqueId());
                Kit playerKit;
                if (playerKitId == -1) {
                    playerKit = kitDao.getKit("Ranked", null);
                } else {
                    playerKit = kitDao.getKit(playerKitId);
                }
                int opponentKitId = dao.getKit(opponent.getUniqueId());
                Kit opponentKit;
                if (opponentKitId == -1) {
                    opponentKit = kitDao.getKit("Ranked", null);
                } else {
                    opponentKit = kitDao.getKit(opponentKitId);
                }
                Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                    if (!player.isOnline() || !opponent.isOnline() || playerKit == null || opponentKit == null || KitCost.getCost(playerKit.items()) > KitCost.MAX_POINTS || KitCost.getCost(opponentKit.items()) > KitCost.MAX_POINTS || player.isDead() || opponent.isDead()) {
                        // TODO better error handling
                        player.sendMessage(Component.text("An error occurred, please requeue.", NamedTextColor.RED));
                        arenaManager.deleteLoadedArena(loaded);
                        JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("Error occurred between " + player.getName() + " and " + opponent.getName());
                        return;
                    }

                    WorldBorder border = world.getWorldBorder();
                    border.setCenter(72.50, 72.50);
                    border.setSize(143);
                    border.setSize(5, 8 * 60);
                    border.setDamageBuffer(0);
                    border.setDamageAmount(3);

                    player.closeInventory();
                    opponent.closeInventory();
                    player.setFallDistance(0);
                    player.teleport(new Location(world, 42.5, 72, 33.5, -45, 0));
                    opponent.setFallDistance(0);
                    opponent.teleport(new Location(world, 96.5, 72, 89.5, 135, 0));
                    player.setGameMode(GameMode.SURVIVAL);
                    opponent.setGameMode(GameMode.SURVIVAL);
                    KitApplier.applyKit(playerKit, player);
                    KitApplier.applyKit(opponentKit, opponent);
                    matches.add(new RankedMatch(player, playerElo, opponent, opponentElo, loaded, Instant.now(), unranked));

                    if (unranked) {
                        player.sendMessage(Component.text("You have been paired with " + opponent.getName() + ". You are playing unranked", NamedTextColor.YELLOW));
                        opponent.sendMessage(Component.text("You have been paired with " + player.getName() + ". You are playing unranked", NamedTextColor.YELLOW));

                        JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s started an unranked ranked fight against %s".formatted(player.getName(), opponent.getName()));
                    } else {
                        Elo.EloChange playerChange = Elo.getChange(playerElo, opponentElo);
                        Elo.EloChange opponentChange = Elo.getChange(opponentElo, playerElo);

                        JavaPlugin.getPlugin(KitPvpPlugin.class).getLogger().info("%s (elo: %s, win: %s, loss: %s) started a ranked fight against %s (elo: %s, win: %s, loss: %s)"
                            .formatted(player.getName(), Math.round(playerElo), Math.round(playerChange.win()), Math.round(playerChange.loss()),
                                opponent.getName(), Math.round(opponentElo), Math.round(opponentChange.win()), Math.round(opponentChange.loss())));

                        player.sendMessage(Component.text("You (elo: " + ((int) Math.round(playerElo)) + ") have been paired with " + opponent.getName() + " (elo: " + ((int) Math.round(opponentElo)) + ")", NamedTextColor.YELLOW));
                        player.sendMessage(Component.text("Win: +" + Math.round(playerChange.win()) + " elo, lose: " + Math.round(playerChange.loss()) + " elo", NamedTextColor.GRAY));

                        opponent.sendMessage(Component.text("You (elo: " + ((int) Math.round(opponentElo)) + ") have been paired with " + player.getName() + " (elo: " + ((int) Math.round(playerElo)) + ")", NamedTextColor.YELLOW));
                        opponent.sendMessage(Component.text("Win: +" + Math.round(opponentChange.win()) + " elo, lose: " + Math.round(opponentChange.loss()) + " elo", NamedTextColor.GRAY));
                    }
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
            unrankedQueued.remove(player);
            unrankedQueued.remove(opponent);
        }
    }

    record QueuedPlayer(double elo, Instant joined, boolean auto) {

        public boolean valid() {
            if (!auto) {
                return true;
            } else {
                return !Instant.now().isBefore(joined);
            }
        }
    }

    record RecentMatch(UUID other, Instant time) {

    }
}
