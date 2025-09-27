package xyz.huskydog.queue;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import net.minestom.server.Auth;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.Color;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.particle.Particle;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.biome.Biome;
import net.minestom.server.world.biome.BiomeEffects;
import org.jetbrains.annotations.NotNull;

public class Queue {

    private static final Color BLACK = new Color(0x000000);
    private static final double MAX_SIZE = 16.0;

    // Initialize the server
    public MinecraftServer SERVER;
    private ViaMinestom via;

    public Queue() {
        switch (Config.PROXY.toUpperCase()) {
            case "NONE" -> {
                // SERVER = MinecraftServer.init(new Auth.Online());
                SERVER = MinecraftServer.init();
            }
            case "VELOCITY" -> {
                final String velocitySecret = System.getProperty("velocitySecret");
                if (Objects.isNull(velocitySecret)) {
                    MinecraftServer.LOGGER.warn("You have enabled Velocity but haven't provided a secret. Set the 'velocitySecret' property.");
                    return;
                }
                SERVER = MinecraftServer.init(new Auth.Velocity(velocitySecret));
                MinecraftServer.LOGGER.info("Enabling Velocity proxy");
            }
            default -> {
                SERVER = MinecraftServer.init(new Auth.Online());
                MinecraftServer.LOGGER.warn("You've specified an unknown proxy [{}] which isn't supported!", Config.PROXY);
            }
        }

        via = new ViaMinestom();
        setupServer();
    }

    public void start() {

        // use localhost as viaproxy runs on the actual host/port
        SERVER.start("127.0.0.1", 0);
        via.onEnable();

        MinecraftServer.LOGGER.info("Queue v{} for Minecraft {} started on {}:{}",
            Main.VERSION,
            Main.MC_VERSION,
            MinecraftServer.getServer().getAddress(),
            MinecraftServer.getServer().getPort()
        );
    }

    private void setupServer() {
        MinecraftServer.setBrandName("Queue");
        final RegistryKey<@NotNull DimensionType> voidDimension = MinecraftServer.getDimensionTypeRegistry().register(
            "minecraft:queue",
            DimensionType.builder()
                .effects("minecraft:the_end")
                // restrict height to prevent people from glitching out
                .minY(0)
                .height(16)
                .logicalHeight(16)
                .build()
        );

        final RegistryKey<@NotNull Biome> biome = MinecraftServer.getBiomeRegistry().register(
            "minecraft:queue",
            Biome.builder()
                .effects(
                    BiomeEffects
                        .builder()
                        .biomeParticle(new BiomeEffects.Particle(0.3f, Particle.UNDERWATER))
                        .fogColor(BLACK)
                        .skyColor(BLACK)
                        .waterColor(BLACK)
                        .waterFogColor(BLACK)
                        .build()
                )
                .build()
        );

        // create ground for players to stand on
        final InstanceContainer world = MinecraftServer.getInstanceManager().createInstanceContainer(voidDimension);
        world.setGenerator((final GenerationUnit unit) -> {
            unit.modifier().fillBiome(biome);
            unit.modifier().fillHeight(0, 1, Block.BARRIER);
        });

        // use custom player class to drop tab list packets
        MinecraftServer.getConnectionManager().setPlayerProvider(MinestomPlayer::new);

        // handle login
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerConfigurationEvent.class, (event) -> {
            final Player player = event.getPlayer();
            MinecraftServer.LOGGER.info("[{}] has connected!", player.getUsername());

            event.setSpawningInstance(world);
            // Spawn somewhere random so people don't spawn on top of each other
            player.setRespawnPoint(new Pos(ThreadLocalRandom.current().nextDouble(-MAX_SIZE, MAX_SIZE), 1, ThreadLocalRandom.current().nextDouble(-MAX_SIZE, MAX_SIZE)));
            if (Config.HIDE_PLAYERS) {
                player.setAutoViewable(false);
                // waiting on https://github.com/Minestom/Minestom/pull/2113 to hide players in tab
            }
            player.setGameMode(GameMode.ADVENTURE);
        });

        // keep players within the border and wrap them around the other side if they go out of bounds
        MinecraftServer.getGlobalEventHandler().addListener(PlayerMoveEvent.class, (event) -> {
            double newX = event.getNewPosition().x();
            double newZ = event.getNewPosition().z();
            if (-MAX_SIZE > newX || newX > MAX_SIZE) newX = -Math.min(Math.max(-MAX_SIZE, newX), MAX_SIZE);
            if (-MAX_SIZE > newZ || newZ > MAX_SIZE) newZ = -Math.min(Math.max(-MAX_SIZE, newZ), MAX_SIZE);

            event.setNewPosition(new Pos(newX, event.getNewPosition().y(), newZ, event.getNewPosition().yaw(), event.getNewPosition().pitch()));
        });

        MinecraftServer.getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, (event) -> {
            MinecraftServer.LOGGER.info("[{}] has disconnected!", event.getPlayer().getUsername());
        });

        if (Config.DISABLE_CHAT) {
            MinecraftServer.getGlobalEventHandler().addListener(PlayerChatEvent.class, (event) -> {
                event.setCancelled(true);
            });
        }

        MinecraftServer.getSchedulerManager().buildShutdownTask(() -> {
            via.onDisable();
        });
    }
}
