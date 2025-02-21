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
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.slf4j.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

@Plugin(id = "civannouncements", name = "Civ Announcements", version = "1.0.0",
    url = "https://civmc.net", description = "Sends various announcements", authors = {"Huskydog9988"})
public class AnnouncementsPlugin {

    private final CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private final Map<Cron, Component> scheduledAnnouncements = new ConcurrentHashMap<>();
    private AnnouncementsConfig config;


    @Inject
    public AnnouncementsPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        logger.info("Initializing Announcements plugin");

        loadConfig();
        scheduleTasks();
    }

    private void scheduleTasks() {
        var serializer = JSONComponentSerializer.json();

        // ensure config exists
        if (config == null) {
            logger.info("Config is null");
            return;
        }

        for (AnnouncementsConfig.ScheduledAnnouncement item : config.getScheduledAnnouncements()) {
            Cron cron = parser.parse(item.getCron());
            // convert json message to Component
            var formatedMsg = serializer.deserialize(item.getMessage());

            scheduledAnnouncements.put(cron, formatedMsg);
        }
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

        // task to check if a scheduled announcement should be sent
        server.getScheduler().buildTask(this, this::sendScheduledMessages).repeat(1, TimeUnit.SECONDS).schedule();
    }

    /**
     * Checks if a scheduled message needs to be sent, and sends them if so
     */
    private void sendScheduledMessages() {
        ZonedDateTime now = ZonedDateTime.now();

        for (Cron cron : scheduledAnnouncements.keySet()) {
            var executionTime = ExecutionTime.forCron(cron);

            // if now is a time the
            if (executionTime.isMatch(now)) {
                server.sendMessage(scheduledAnnouncements.get(cron));
            }
        }
    }

    /**
     * Loads the config from disk, and creates it if necessary
     */
    private void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
        } catch (IOException e) {
            logger.error("Could not create data directory: {}", dataDirectory, e);
            return;
        }

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

        try (InputStream in = Files.newInputStream(configFile)) {
            // register config class with snakeyaml so it knows how to populate the config
            Yaml yaml = new Yaml(new Constructor(AnnouncementsConfig.class, new LoaderOptions()));
            config = yaml.load(in);
        } catch (IOException e) {
            logger.error("Could not read config file: {}", configFile, e);
        }
    }
}
