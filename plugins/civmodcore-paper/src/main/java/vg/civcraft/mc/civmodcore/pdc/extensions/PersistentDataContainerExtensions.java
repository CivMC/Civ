package vg.civcraft.mc.civmodcore.pdc.extensions;

import java.util.Map;
import net.minecraft.nbt.Tag;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

/**
 * Set of extension methods for {@link PersistentDataContainer}.
 */
public final class PersistentDataContainerExtensions {
    /**
     * @param self The PersistentDataContainer to get the internal NBT of.
     * @return Returns the PDC's inner-map.
     */
    @NotNull
    public static Map<String, Tag> getRaw(@NotNull final PersistentDataContainer self) {
        return ((CraftPersistentDataContainer) self).getRaw();
    }

    /**
     * @param self The PersistentDataContainer to get the size of.
     * @return Returns the PDC's size.
     */
    public static int size(@NotNull final PersistentDataContainer self) {
        return getRaw(self).size();
    }
}
