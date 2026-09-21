package net.civmc.civproxy.sessionlimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

class SessionLimitConfigTest {

    @Test
    void missingSectionIsDisabledWithDefaults() {
        final SessionLimitConfig config = SessionLimitConfig.load(BasicConfigurationNode.root().node("session-limit"));
        assertFalse(config.enabled());
        assertTrue(config.dryRun());
        assertEquals("main", config.server());
        assertEquals(Duration.ofHours(8), config.limit());
        assertEquals(Duration.ofHours(1), config.warning());
        assertEquals(Duration.ofMinutes(10), config.breakReset());
        assertEquals(Duration.ofMinutes(5), config.queueGrace());
        assertEquals(List.of(Duration.ofMinutes(60), Duration.ofMinutes(30), Duration.ofMinutes(15),
            Duration.ofMinutes(5), Duration.ofMinutes(1)), config.chatWarnings());
    }

    @Test
    void readsFractionalMinutesAndSortsWarnings() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.node("enabled").set(true);
        node.node("dry-run").set(false);
        node.node("limit-minutes").set(3);
        node.node("warning-minutes").set(2);
        node.node("break-reset-minutes").set(0.5);
        node.node("chat-warnings-minutes").setList(Double.class, List.of(0.5, 2D, 1D));

        final SessionLimitConfig config = SessionLimitConfig.load(node);
        assertTrue(config.enabled());
        assertFalse(config.dryRun());
        assertEquals(Duration.ofMinutes(3), config.limit());
        assertEquals(Duration.ofSeconds(30), config.breakReset());
        assertEquals(List.of(Duration.ofMinutes(2), Duration.ofMinutes(1), Duration.ofSeconds(30)), config.chatWarnings());
    }

    @Test
    void warningCannotExceedTheLimit() throws SerializationException {
        final BasicConfigurationNode node = BasicConfigurationNode.root();
        node.node("limit-minutes").set(30);
        node.node("warning-minutes").set(60);
        assertEquals(Duration.ofMinutes(30), SessionLimitConfig.load(node).warning());
    }
}
