package vg.civcraft.mc.civmodcore.chat.dialog;

import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import java.util.Objects;
import java.util.function.Consumer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public final class DialogHelpers {
    public static @NotNull DialogAction customClick(
        final @NotNull Key actionKey,
        final @NotNull Consumer<@NotNull CompoundTag> editor
    ) {
        final var nbt = new CompoundTag();
        editor.accept(nbt);
        return DialogAction.customClick(
            Objects.requireNonNull(actionKey),
            BinaryTagHolder.encode(nbt, PaperAdventure.NBT_CODEC)
        );
    }
}
