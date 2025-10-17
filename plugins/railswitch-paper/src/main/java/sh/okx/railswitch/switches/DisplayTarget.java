package sh.okx.railswitch.switches;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a target location and text for displaying rail switch information.
 */
public record DisplayTarget(Location location, Component text) {

    /**
     * Creates a new display target.
     *
     * @param location The location to display the text
     * @param text     The text component to display
     */
    public DisplayTarget(Location location, Component text) {
        this.location = location.clone();
        this.text = text;
    }

    /**
     * Gets the display location.
     *
     * @return The location
     */
    @Override
    public Location location() {
        return location.clone();
    }

    /**
     * Gets the display text.
     *
     * @return The text component
     */
    @Override
    public Component text() {
        return text;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof DisplayTarget)) return false;
        DisplayTarget that = (DisplayTarget) other;
        UUID w1 = this.location.getWorld() != null ? this.location.getWorld().getUID() : null;
        UUID w2 = that.location.getWorld() != null ? that.location.getWorld().getUID() : null;
        return Objects.equals(w1, w2)
            && location.getBlockX() == that.location.getBlockX()
            && location.getBlockY() == that.location.getBlockY()
            && location.getBlockZ() == that.location.getBlockZ()
            && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        UUID w = location.getWorld() != null ? location.getWorld().getUID() : null;
        return Objects.hash(w, location.getBlockX(), location.getBlockY(), location.getBlockZ(), text);
    }
}
