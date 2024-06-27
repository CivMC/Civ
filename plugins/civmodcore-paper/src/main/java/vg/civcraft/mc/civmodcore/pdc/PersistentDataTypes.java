package vg.civcraft.mc.civmodcore.pdc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class PersistentDataTypes {

    /**
     * Converts Components to Strings and vice versa.
     */
    public static final PersistentDataType<String, Component> COMPONENT = new AbstractPersistentDataType<>(String.class, Component.class) {
        @NotNull
        @Override
        public String toPrimitive(@NotNull final Component component,
                                  @NotNull final PersistentDataAdapterContext adapter) {
            return GsonComponentSerializer.gson().serialize(component);
        }

        @NotNull
        @Override
        public Component fromPrimitive(@NotNull final String raw,
                                       @NotNull final PersistentDataAdapterContext adapter) {
            return GsonComponentSerializer.gson().deserialize(raw);
        }
    };

}
