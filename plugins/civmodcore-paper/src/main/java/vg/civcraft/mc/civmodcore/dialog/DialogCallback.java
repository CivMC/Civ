package vg.civcraft.mc.civmodcore.dialog;

import io.papermc.paper.dialog.DialogResponseView;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
@FunctionalInterface
public interface DialogCallback {
    public void handle(
        @NotNull DialogResponseView view
    );
}
