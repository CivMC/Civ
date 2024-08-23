package vg.civcraft.mc.civmodcore.pdc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public final class PersistentDataTypes {

    public static final String DECODER_ERROR = "Was unable to decode that %s! [%s]";

    /**
     * Boolean data type... because <i>believe it or not</i> but PDC doesn't already have this ಠ_ಠ
     */
    public static final PersistentDataType<Byte, Boolean> BOOLEAN = new AbstractPersistentDataType<>(Byte.class, Boolean.class) {
        @NotNull
        @Override
        public Byte toPrimitive(@NotNull final Boolean bool,
                                @NotNull final PersistentDataAdapterContext adapter) {
            return (byte) (bool ? 1 : 0);
        }

        @NotNull
        @Override
        public Boolean fromPrimitive(@NotNull final Byte raw,
                                     @NotNull final PersistentDataAdapterContext adapter) {
            return raw != (byte) 0;
        }
    };

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
