package net.civmc.zorweth;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.bukkit.plugin.java.JavaPlugin;

public final class ZorwethPlugin extends JavaPlugin {

    private static ZorwethPlugin instance;

    private Clipboard rocketClipboard;

    public static ZorwethPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        this.rocketClipboard = loadRocketClipboard();
    }

    public Clipboard getRocketClipboard() {
        return this.rocketClipboard;
    }

    private Clipboard loadRocketClipboard() {
        final File file = new File(getDataFolder(), "rocket.schem");
        final ClipboardFormat format = ClipboardFormats.findByFile(file);
        if (format == null) {
            throw new IllegalStateException("Could not find clipboard format for " + file.getPath());
        }

        try (FileInputStream stream = new FileInputStream(file);
             ClipboardReader reader = format.getReader(stream)) {
            return reader.read();
        } catch (final IOException exception) {
            throw new IllegalStateException("Failed to load " + file.getPath(), exception);
        }
    }
}
