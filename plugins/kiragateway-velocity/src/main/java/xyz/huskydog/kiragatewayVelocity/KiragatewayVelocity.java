package xyz.huskydog.kiragatewayVelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
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
import xyz.huskydog.kiragatewayVelocity.auth.AuthcodeManager;
import xyz.huskydog.kiragatewayVelocity.commands.DiscordAuth;
import xyz.huskydog.kiragatewayVelocity.rabbit.RabbitCommands;
import xyz.huskydog.kiragatewayVelocity.rabbit.RabbitHandler;

@Plugin(
    id = "kiragateway-velocity",
    name = "kiragateway-velocity",
    version = BuildConstants.VERSION,
    url = "https://civmc.net",
    authors = {"Huskydog9988"}
)
public class KiragatewayVelocity {

    public final ProxyServer proxy;
    public final Logger logger;
    public final Path dataDirectory;

    private static KiragatewayVelocity instance;
    private AuthcodeManager authcodeManager;
    private RabbitHandler rabbitHandler;
    private RabbitCommands rabbitCommands;
    private @Nullable CommentedConfigurationNode config;

    @Inject
    public KiragatewayVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;

        config = Config.loadConfig();
        if (config == null) {
            logger.error("Failed to load configuration, shutting down");
            proxy.shutdown();
            return;
        }

        authcodeManager = new AuthcodeManager(12);
        rabbitHandler = new RabbitHandler(
            Config.getRabbitConfig(config),
            config.getString("rabbitmq.incomingQueue"),
            config.getString("rabbitmq.outgoingQueue"), // Outgoing queue name
            logger
        );
        if (!rabbitHandler.setup()) {
            logger.error("Failed to setup rabbitmq, shutting down");
            proxy.shutdown();
            return;
        }
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

    public static KiragatewayVelocity getInstance() {
        return instance;
    }
}
