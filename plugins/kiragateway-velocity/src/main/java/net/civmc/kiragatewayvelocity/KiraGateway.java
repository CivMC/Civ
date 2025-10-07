package net.civmc.kiragatewayvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import net.civmc.kiragatewayvelocity.auth.AuthcodeManager;
import net.civmc.kiragatewayvelocity.commands.DiscordAuth;
import net.civmc.kiragatewayvelocity.rabbit.RabbitCommands;
import net.civmc.kiragatewayvelocity.rabbit.RabbitHandler;

@Plugin(
    id = "kiragateway",
    name = "kiragateway",
    version = "1.0",
    url = "https://civmc.net",
    authors = {"Huskydog9988"}
)
public class KiraGateway {

    public static final String PROXY_SERVER_NAME = "proxy";

    private final ProxyServer proxy;
    public final Logger logger;
    public final Path dataDirectory;

    private static KiraGateway instance;
    private AuthcodeManager authcodeManager;
    private RabbitHandler rabbitHandler;
    private RabbitCommands rabbitCommands;
    private @Nullable CommentedConfigurationNode config;

    @Inject
    public KiraGateway(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        config = Config.loadConfig();
        if (config == null) {
            logger.error("Failed to load configuration");
            return;
        }

        authcodeManager = new AuthcodeManager(12);
        rabbitHandler = new RabbitHandler(
            Config.getRabbitConfig(config),
            config.node("rabbitmq", "incomingQueue").getString(),
            config.node("rabbitmq", "outgoingQueue").getString(),
            logger
        );
        if (!rabbitHandler.setup()) {
            logger.error("Failed to setup rabbitmq");
            return;
        }
        rabbitHandler.beginAsyncListen();
        rabbitCommands = new RabbitCommands(rabbitHandler);

        registerCommands();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        rabbitHandler.shutdown();
    }

    private void registerCommands() {
        CommandManager commandManager = proxy.getCommandManager();

        // discordauth command
        CommandMeta discordAuthMeta = commandManager.metaBuilder("discordauth")
            // .aliases("auth")
            .plugin(this)
            .build();
        commandManager.register(discordAuthMeta, new DiscordAuth());
    }

    public AuthcodeManager getAuthcodeManager() {
        return authcodeManager;
    }

    public RabbitCommands getRabbit() {
        return rabbitCommands;
    }

    public static KiraGateway getInstance() {
        return instance;
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public CommentedConfigurationNode getConfig() {
        return config;
    }
}
