package net.civmc.kitpvp;

import java.sql.SQLException;
import java.util.List;
import net.civmc.kitpvp.anvil.AnvilGui;
import net.civmc.kitpvp.arena.ArenaCleaner;
import net.civmc.kitpvp.arena.ArenaCommand;
import net.civmc.kitpvp.arena.ArenaManager;
import net.civmc.kitpvp.arena.MysqlLoader;
import net.civmc.kitpvp.arena.PrivateArenaListener;
import net.civmc.kitpvp.arena.RespawnListener;
import net.civmc.kitpvp.arena.data.Arena;
import net.civmc.kitpvp.arena.data.SqlArenaDao;
import net.civmc.kitpvp.command.ClearCommand;
import net.civmc.kitpvp.command.KitCommand;
import net.civmc.kitpvp.ranked.EloCommand;
import net.civmc.kitpvp.ranked.RankedCommand;
import net.civmc.kitpvp.ranked.RankedPlaceholders;
import net.civmc.kitpvp.ranked.RankedPlayers;
import net.civmc.kitpvp.ranked.RankedQueueListener;
import net.civmc.kitpvp.ranked.RankedQueueManager;
import net.civmc.kitpvp.ranked.SqlRankedDao;
import net.civmc.kitpvp.ranked.UnrankedCommand;
import net.civmc.kitpvp.snapshot.DeathListener;
import net.civmc.kitpvp.snapshot.InventorySnapshotManager;
import net.civmc.kitpvp.snapshot.ViewInventorySnapshotCommand;
import net.civmc.kitpvp.spawn.SetSpawnCommand;
import net.civmc.kitpvp.spawn.SpawnCommand;
import net.civmc.kitpvp.spawn.SpawnListener;
import net.civmc.kitpvp.spawn.SpawnProvider;
import net.civmc.kitpvp.spawn.SqlSpawnProvider;
import net.civmc.kitpvp.sql.SqlKitPvpDao;
import net.civmc.kitpvp.warp.WarpMain;
import org.bukkit.Bukkit;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.DatabaseCredentials;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class KitPvpPlugin extends ACivMod {

    private ManagedDatasource source;

    @Override
    public void onEnable() {
        AnvilGui anvilGui = new AnvilGui();
        getServer().getPluginManager().registerEvents(anvilGui, this);

        saveDefaultConfig();
        DatabaseCredentials credentials = (DatabaseCredentials) getConfig().get("database");
        source = ManagedDatasource.construct(this, credentials);
        SqlKitPvpDao dao = new SqlKitPvpDao(source);
        SqlRankedDao ranked = new SqlRankedDao(source);
        getCommand("kit").setExecutor(new KitCommand(dao, ranked, anvilGui));
        new WarpMain(this, source);
        getCommand("clear").setExecutor(new ClearCommand());

        InventorySnapshotManager inventorySnapshotManager = new InventorySnapshotManager();
        DeathListener deathListener = new DeathListener(inventorySnapshotManager);
        getServer().getPluginManager().registerEvents(deathListener, this);
        getCommand("viewinventorysnapshot").setExecutor(new ViewInventorySnapshotCommand(inventorySnapshotManager));

        if (Bukkit.getPluginManager().isPluginEnabled("BreweryX")) {
            getServer().getPluginManager().registerEvents(new DrunkDeathListener(), this);
        }

        SpawnProvider spawnProvider = new SqlSpawnProvider(source);
        getCommand("spawn").setExecutor(new SpawnCommand(spawnProvider));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(spawnProvider));
        getServer().getPluginManager().registerEvents(new SpawnListener(spawnProvider), this);

        int maxArenas = getConfig().getInt("max_arenas");
        try {
            ArenaManager manager = new ArenaManager(maxArenas, this, spawnProvider, new MysqlLoader(source));
            SqlArenaDao arenaDao = new SqlArenaDao(source);
            source.updateDatabase();

            List<Arena> arenas = arenaDao.getArenas();
            Arena rankedArena = null;
            for (Arena arena : arenas) {
                if (arena.name().equals(getConfig().getString("ranked_arena"))) {
                    rankedArena = arena;
                }
            }

            RankedPlayers players = new RankedPlayers(ranked);
            RankedQueueManager queueManager = new RankedQueueManager(dao, ranked, manager, spawnProvider, rankedArena, players);
            getCommand("ranked").setExecutor(new RankedCommand(queueManager));
            getCommand("unranked").setExecutor(new UnrankedCommand(queueManager));
            getCommand("elo").setExecutor(new EloCommand(players));
            getServer().getPluginManager().registerEvents(new RankedQueueListener(queueManager, deathListener), this);
            new RankedPlaceholders(players).register();

            PrivateArenaListener privateArenaListener = new PrivateArenaListener(spawnProvider, manager);
            getServer().getPluginManager().registerEvents(privateArenaListener, this);
            getCommand("arena").setExecutor(new ArenaCommand(this, arenaDao, ranked, queueManager, manager, privateArenaListener));
            getServer().getPluginManager().registerEvents(new RespawnListener(manager), this);
            Bukkit.getScheduler().runTaskTimer(this, new ArenaCleaner(manager), 20 * 60, 20 * 60);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        this.source.close();
    }
}
