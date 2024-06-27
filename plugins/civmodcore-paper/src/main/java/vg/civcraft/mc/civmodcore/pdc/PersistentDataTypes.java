package vg.civcraft.mc.civmodcore.pdc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.persistence.PersistentDataType;

public final class PersistentDataTypes {
    /**
     * Converts Components to Strings and vice versa.
     */
    public static final PersistentDataType<String, Component> COMPONENT = AbstractPersistentDataType.flatValued(
        String.class,
        Component.class,
        GsonComponentSerializer.gson()::serialize,
        GsonComponentSerializer.gson()::deserialize
    );
}
