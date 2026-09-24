package net.civmc.kitpvp.arena;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.civmc.kitpvp.arena.data.Arena;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

class ArenaGravityTest {

    @Test
    void parsesGravityNamesCaseInsensitively() {
        assertEquals(ArenaGravity.MAIN, ArenaGravity.parse("main").orElseThrow());
        assertEquals(ArenaGravity.ZORWETH, ArenaGravity.parse("ZoRwEtH").orElseThrow());
    }

    @Test
    void rejectsUnknownGravityNames() {
        assertTrue(ArenaGravity.parse("moon").isEmpty());
        assertTrue(ArenaGravity.parse(null).isEmpty());
    }

    @Test
    void usesExpectedServerGravityValues() {
        assertEquals(0.08D, ArenaGravity.MAIN.value());
        assertEquals(0.04D, ArenaGravity.ZORWETH.value());
    }

    @Test
    void alternatesBetweenGravitySettings() {
        assertEquals(ArenaGravity.ZORWETH, ArenaGravity.MAIN.alternate());
        assertEquals(ArenaGravity.MAIN, ArenaGravity.ZORWETH.alternate());
    }

    @Test
    void newArenaStartsWithMainGravityUnlocked() {
        LoadedArena loaded = newLoadedArena();

        assertEquals(ArenaGravity.MAIN, loaded.gravity());
        assertFalse(loaded.gravityLocked());
    }

    @Test
    void preventsGravityChangesAfterLocking() {
        LoadedArena loaded = newLoadedArena();

        assertTrue(loaded.setGravity(ArenaGravity.ZORWETH));
        assertEquals(ArenaGravity.ZORWETH, loaded.gravity());

        loaded.lockGravity();

        assertFalse(loaded.setGravity(ArenaGravity.MAIN));
        assertEquals(ArenaGravity.ZORWETH, loaded.gravity());
    }

    private static LoadedArena newLoadedArena() {
        Arena arena = new Arena("test", null, null, null, Material.STONE);
        return new LoadedArena(null, arena, null, -1);
    }
}
