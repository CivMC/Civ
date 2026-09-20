package vg.civcraft.mc.civmodcore.chat.dialog;

import io.papermc.paper.dialog.DialogResponseView;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
@FunctionalInterface
public interface DialogCallback {
    public void handle(
        @NotNull Player clicker,
        @NotNull DialogResponseView view
    );
}
