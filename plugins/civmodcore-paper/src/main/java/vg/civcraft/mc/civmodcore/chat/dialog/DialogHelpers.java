package vg.civcraft.mc.civmodcore.chat.dialog;

import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.nbt.NbtCompound;

@SuppressWarnings("UnstableApiUsage")
public final class DialogHelpers {
    public static @NotNull DialogAction customClick(
        final @NotNull Key actionKey,
        final @NotNull Consumer<@NotNull NbtCompound> editor
    ) {
        final var nbt = new NbtCompound();
        editor.accept(nbt);
        return DialogAction.customClick(
            Objects.requireNonNull(actionKey),
            BinaryTagHolder.encode(nbt.internal(), PaperAdventure.NBT_CODEC)
        );
    }

    /// Ensures that the retrieved string is never null.
    ///
    /// @apiNote Use this instead of [DialogResponseView#getText]. Keep in mind that this makes it impossible to test
    ///          whether the Dialog included the value within its payload, so use this only when you don't care.
    public static @NotNull String getAssuredText(
        final @NotNull DialogResponseView view,
        final @NotNull String inputId
    ) {
        return Objects.requireNonNullElse(
            view.getText(Objects.requireNonNull(inputId)),
            ""
        );
    }

    /// Ensures that the text is either null or non-blank.
    ///
    /// @apiNote Use this instead of [DialogResponseView#getText].
    public static @Nullable String getTrimmedText(
        final @NotNull DialogResponseView view,
        final @NotNull String inputId
    ) {
        return StringUtils.trimToNull(
            view.getText(Objects.requireNonNull(inputId))
        );
    }
}
