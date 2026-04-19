package com.devotedmc.ExilePearl.core;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExilePearlPlugin;
import com.devotedmc.ExilePearl.PearlType;
import com.devotedmc.ExilePearl.RepairMaterial;
import com.devotedmc.ExilePearl.config.PearlConfig;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class CoreLoreGeneratorTest {

    private PearlConfig config;
    private ExilePearl pearl;
    private CoreLoreGenerator generator;
    private MockedStatic<ExilePearlPlugin> pluginStatic;

    @BeforeEach
    void setUp() {
        config = Mockito.mock(PearlConfig.class);
        Mockito.when(config.getPearlHealthMaxValue()).thenReturn(1000);
        Mockito.when(config.getPearlHealthDecayHumanIntervalMin()).thenReturn(1440); // 1 day
        Mockito.when(config.getPearlHealthDecayIntervalMin()).thenReturn(60);        // hourly tick
        Mockito.when(config.getPearlHealthDecayAmount()).thenReturn(1);              // -> 24/day
        Mockito.when(config.getPearlHealthDecayHumanInterval()).thenReturn("day");
        Mockito.when(config.getRepairMaterials(Mockito.any())).thenReturn(null);
        Mockito.when(config.getDefaultPearlType()).thenReturn(PearlType.EXILE);
        Mockito.when(config.getUpgradeMaterials()).thenReturn(null);

        pearl = Mockito.mock(ExilePearl.class);
        Mockito.when(pearl.getItemName()).thenReturn("Exile Pearl");
        Mockito.when(pearl.getPlayerName()).thenReturn("TestPlayer");
        Mockito.when(pearl.getPearlId()).thenReturn(12345);
        Mockito.when(pearl.getPearledOn()).thenReturn(new Date(0));
        Mockito.when(pearl.getKillerName()).thenReturn("KillerPlayer");
        Mockito.when(pearl.getPlayerId()).thenReturn(UUID.randomUUID());
        Mockito.when(pearl.getPearlType()).thenReturn(PearlType.EXILE);
        Mockito.when(pearl.getHealth()).thenReturn(240); // exactly 10 days at 24/day
        Mockito.when(pearl.isActive()).thenReturn(true);
        Mockito.when(pearl.getLongTimeMultiplier()).thenReturn(1.0);

        ExilePearlApi api = Mockito.mock(ExilePearlApi.class);
        Mockito.when(api.isBanStickEnabled()).thenReturn(false);
        pluginStatic = Mockito.mockStatic(ExilePearlPlugin.class);
        pluginStatic.when(ExilePearlPlugin::getApi).thenReturn(api);

        generator = new CoreLoreGenerator(config, null);
    }

    @AfterEach
    void tearDown() {
        pluginStatic.close();
    }

    @Test
    void generateLore_includesTimeRemainingForActivePearl() {
        List<String> lore = generator.generateLore(pearl);
        String timeRemaining = findLine(lore, "Time remaining:");
        Assertions.assertNotNull(timeRemaining, "Expected a 'Time remaining' line, got: " + lore);
        Assertions.assertTrue(timeRemaining.contains("10"), "Expected 10 days for health=240 / 24-per-day, got: " + timeRemaining);
        Assertions.assertTrue(timeRemaining.contains("day"), "Expected unit 'day' in: " + timeRemaining);
    }

    @Test
    void generateLore_omitsTimeRemainingForInactivePearl() {
        Mockito.when(pearl.isActive()).thenReturn(false);
        List<String> lore = generator.generateLore(pearl);
        Assertions.assertNull(findLine(lore, "Time remaining:"),
            "Inactive pearl should not show time remaining (already shows 'suspended due to Inactivity'); got: " + lore);
    }

    @Test
    void generateLore_omitsTimeRemainingWhenHealthIsZero() {
        Mockito.when(pearl.getHealth()).thenReturn(0);
        List<String> lore = generator.generateLore(pearl);
        Assertions.assertNull(findLine(lore, "Time remaining:"), "Zero health should hide time remaining; got: " + lore);
    }

    @Test
    void generateLore_omitsTimeRemainingWhenDecayDisabled() {
        Mockito.when(config.getPearlHealthDecayAmount()).thenReturn(0);
        List<String> lore = generator.generateLore(pearl);
        Assertions.assertNull(findLine(lore, "Time remaining:"), "Decay disabled should hide time remaining; got: " + lore);
    }

    @Test
    void generateLore_timeRemainingRoundsUpForPartialInterval() {
        Mockito.when(pearl.getHealth()).thenReturn(241); // 24*10 + 1
        List<String> lore = generator.generateLore(pearl);
        String timeRemaining = findLine(lore, "Time remaining:");
        Assertions.assertNotNull(timeRemaining);
        Assertions.assertTrue(timeRemaining.contains("11"), "241 health at 24/day should round up to 11 days, got: " + timeRemaining);
    }

    @Test
    void generateLore_timeRemainingUsesConfiguredUnit() {
        Mockito.when(config.getPearlHealthDecayHumanInterval()).thenReturn("week");
        List<String> lore = generator.generateLore(pearl);
        String timeRemaining = findLine(lore, "Time remaining:");
        Assertions.assertNotNull(timeRemaining);
        Assertions.assertTrue(timeRemaining.contains("week"), "Configured unit 'week' should be used, got: " + timeRemaining);
    }

    @Test
    void generateLore_timeRemainingAppearsRightAfterHealth() {
        List<String> lore = generator.generateLore(pearl);
        int healthIdx = indexOfContaining(lore, "Health:");
        int timeIdx = indexOfContaining(lore, "Time remaining:");
        Assertions.assertTrue(healthIdx >= 0 && timeIdx == healthIdx + 1,
            "Time remaining should be immediately after Health; got lore: " + lore);
    }

    @Test
    void generateLore_doesNotMutateConfiguredUnitForRepairLine() {
        Mockito.when(config.getPearlHealthDecayHumanInterval()).thenReturn("day");
        Mockito.when(pearl.getHealth()).thenReturn(24); // 1 interval
        List<String> lore = generator.generateLore(pearl);
        String timeRemaining = findLine(lore, "Time remaining:");
        Assertions.assertNotNull(timeRemaining);
        // Regression guard: no English-plural "s" injected, matches existing repair-line style
        Assertions.assertFalse(timeRemaining.contains("days"),
            "Lore must not append plural 's' to configured unit; got: " + timeRemaining);
    }

    private static String findLine(List<String> lore, String marker) {
        for (String line : lore) {
            if (line.contains(marker)) {
                return line;
            }
        }
        return null;
    }

    private static int indexOfContaining(List<String> lore, String marker) {
        for (int i = 0; i < lore.size(); i++) {
            if (lore.get(i).contains(marker)) {
                return i;
            }
        }
        return -1;
    }
}
