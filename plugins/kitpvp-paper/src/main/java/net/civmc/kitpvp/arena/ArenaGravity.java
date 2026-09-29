package net.civmc.kitpvp.arena;

import java.util.Locale;
import java.util.Optional;

public enum ArenaGravity {
    MAIN("Main", 0.08D),
    ZORWETH("Zorweth", 0.04D);

    private final String displayName;
    private final double value;

    ArenaGravity(String displayName, double value) {
        this.displayName = displayName;
        this.value = value;
    }

    public String displayName() {
        return displayName;
    }

    public double value() {
        return value;
    }

    public ArenaGravity alternate() {
        return this == MAIN ? ZORWETH : MAIN;
    }

    public static Optional<ArenaGravity> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
