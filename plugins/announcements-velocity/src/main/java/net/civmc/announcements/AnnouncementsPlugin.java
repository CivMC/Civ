package net.civmc.announcements;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.civmc.announcements.update.UpdateCommand;
import net.civmc.announcements.update.UpdateListener;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

@Plugin(id = "civannouncements", name = "Civ Announcements", version = "1.0.0",
    url = "https://civmc.net", description = "Sends various announcements", authors = {"Huskydog9988"})
public class AnnouncementsPlugin {

    private final CronParser cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private record Announcement(Component message, boolean title, Duration bossBarLength) {

    }

    private final Map<Cron, Announcement> scheduledAnnouncements = new HashMap<>();
    private final Map<Cron, ZonedDateTime> lastExecutionTimes = new ConcurrentHashMap<>();

    private final BossBarManager bossBars;

    private ZonedDateTime startupTime;

    private @Nullable CommentedConfigurationNode config;

    @Inject
    public AnnouncementsPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.bossBars = new BossBarManager(server, this);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        startupTime = ZonedDateTime.now();

        loadConfig();
        scheduleTasks();

        // task to check if a scheduled announcement should be sent
        server.getScheduler().buildTask(this, this::sendScheduledMessages).repeat(1, TimeUnit.SECONDS).schedule();
        bossBars.init();
        server.getEventManager().register(this, bossBars);

        Component barMessage = MiniMessage.miniMessage().deserialize(config.node("restart").node("bar").getString());
        Component joinMessage = MiniMessage.miniMessage().deserialize(config.node("restart").node("message").getString());
        Component kickMessage = MiniMessage.miniMessage().deserialize(config.node("restart").node("kick").getString());

        UpdateListener listener = new UpdateListener(server, this, joinMessage, kickMessage);
        server.getEventManager().register(this, listener);
        server.getCommandManager().register(server.getCommandManager().metaBuilder("update").plugin(this).build(),
            new UpdateCommand(server, this, listener, bossBars, barMessage));
    }

    private void scheduleTasks() {
        var minimessageSerializer = MiniMessage.miniMessage();

        // read scheduled announcements from config
        List<? extends ConfigurationNode> announcements = config.node("scheduledAnnouncements").childrenList();
        for (ConfigurationNode announcement : announcements) {
            Cron cron = cronParser.parse(Objects.requireNonNull(announcement.node("cron").getString()));
            // convert message to Component
            Component formatedMsg = minimessageSerializer.deserialize(Objects.requireNonNull(announcement.node("message").getString()));
            boolean showTitle = announcement.node("title").getBoolean(false);
            int bossBar = announcement.node("bossbar_seconds").getInt(0);
            scheduledAnnouncements.put(cron, new Announcement(formatedMsg, showTitle, bossBar > 0 ? Duration.ofSeconds(bossBar) : null));
        }
    }

    /**
     * Checks if a scheduled message needs to be sent, and sends them if so
     */
    private void sendScheduledMessages() {
        ZonedDateTime now = ZonedDateTime.now().withNano(0);

        for (Cron cron : scheduledAnnouncements.keySet()) {
            ExecutionTime executionTime = ExecutionTime.forCron(cron);

            // get last time a cron *should* have run
            executionTime.lastExecution(now).ifPresent(lastExecution -> {
                if (lastExecution.isBefore(startupTime)) {
                    return;
                }
                // Check if we should execute, and if con already executed at that time
                if (executionTime.isMatch(now) && !lastExecution.equals(lastExecutionTimes.get(cron))) {
                    // Execute and update the last execution time
                    Announcement announcement = scheduledAnnouncements.get(cron);
                    lastExecutionTimes.put(cron, lastExecution);
                    if (announcement.bossBarLength != null) {
                        BossBar bossBar = BossBar.bossBar(announcement.message, 1f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS);
                        bossBars.addBossBar(bossBar, announcement.message, Instant.now().plus(announcement.bossBarLength));
                    } else if (announcement.title) {
                        // send title to all players
                        Title title = Title.title(announcement.message, Component.empty());
                        server.showTitle(title);
                    } else {
                        server.sendMessage(announcement.message);
                    }

                    logger.info("Announcement sent: {}", PlainTextComponentSerializer.plainText().serialize(announcement.message));
                }
            });
        }
    }


    /**
     * Loads the config from disk, and creates it if necessary
     */
    private void loadConfig() {
        try {
            // ensure data directory exists
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
        } catch (IOException e) {
            logger.error("Could not create data directory: {}", dataDirectory, e);
            return;
        }

        // create config file if it doesn't exist
        Path configFile = dataDirectory.resolve("config.yml");
        if (!Files.exists(configFile)) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile);
                    logger.info("Default configuration file created.");
                } else {
                    logger.error("Default configuration file is missing in resources!");
                    return;
                }
            } catch (IOException e) {
                logger.error("Could not create default configuration file: {}", configFile, e);
            }
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder().path(configFile).build();
        try {
            config = loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load configuration file: " + configFile, e);
        }
    }
}
