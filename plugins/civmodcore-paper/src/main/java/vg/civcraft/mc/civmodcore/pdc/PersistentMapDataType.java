package vg.civcraft.mc.civmodcore.pdc;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * This class enables easier encoding and decoding of maps.
 */
public abstract class PersistentMapDataType<K, V> implements PersistentDataType<PersistentDataContainer, Map<K, V>> {

    private final PersistentDataType<NamespacedKey, K> keyEncoder;
    private final PersistentDataType<?, V> valueEncoder;

    public PersistentMapDataType(@NotNull final PersistentDataType<NamespacedKey, K> keyEncoder,
                                 @NotNull final PersistentDataType<?, V> valueEncoder) {
        this.keyEncoder = Objects.requireNonNull(keyEncoder);
        this.valueEncoder = Objects.requireNonNull(valueEncoder);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NotNull
    @Override
    public Class<Map<K, V>> getComplexType() {
        return (Class<Map<K, V>>) ((Class<? extends Map>) Map.class);
    }

    /**
     * @param initialSize Initial size of the map.
     * @return Returns a new map that's used in {@link #fromPrimitive(PersistentDataContainer, PersistentDataAdapterContext)}.
     */
    @NotNull
    protected abstract Map<K, V> newMap(int initialSize);

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public PersistentDataContainer toPrimitive(@NotNull final Map<K, V> map,
                                               @NotNull final PersistentDataAdapterContext adapter) {
        final var pdc = adapter.newPersistentDataContainer();
        map.forEach((key, value) -> pdc.set(
            this.keyEncoder.toPrimitive(key, adapter),
            this.valueEncoder, value));
        return pdc;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public Map<K, V> fromPrimitive(@NotNull final PersistentDataContainer pdc,
                                   @NotNull final PersistentDataAdapterContext adapter) {
        final Set<NamespacedKey> keys = pdc.getKeys();
        final Map<K, V> map = newMap(keys.size());
        keys.forEach((key) -> map.put(
            this.keyEncoder.fromPrimitive(key, adapter),
            Objects.requireNonNull(pdc.get(key, this.valueEncoder))));
        return map;
    }

}
