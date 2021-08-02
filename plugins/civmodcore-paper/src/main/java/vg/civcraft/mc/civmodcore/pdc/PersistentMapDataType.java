package vg.civcraft.mc.civmodcore.pdc;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * This class enables easier encoding and decoding of maps.
 */
public abstract class PersistentMapDataType<K, V> implements PersistentDataType<PersistentDataContainer, Map<K, V>> {

	private final PersistentDataType<NamespacedKey, K> keyEncoder;
	private final PersistentDataType<?, V> valueEncoder;

	public PersistentMapDataType(@Nonnull final PersistentDataType<NamespacedKey, K> keyEncoder,
								 @Nonnull final PersistentDataType<?, V> valueEncoder) {
		this.keyEncoder = Objects.requireNonNull(keyEncoder);
		this.valueEncoder = Objects.requireNonNull(valueEncoder);
	}

	/** {@inheritDoc} */
	@Nonnull
	@Override
	public Class<PersistentDataContainer> getPrimitiveType() {
		return PersistentDataContainer.class;
	}

	/** {@inheritDoc} */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Nonnull
	@Override
	public Class<Map<K, V>> getComplexType() {
		return (Class<Map<K, V>>) ((Class<? extends Map>) Map.class);
	}

	/**
	 * @param initialSize Initial size of the map.
	 * @return Returns a new map that's used in {@link #fromPrimitive(PersistentDataContainer, PersistentDataAdapterContext)}.
	 */
	@Nonnull
	protected abstract Map<K, V> newMap(int initialSize);

	/** {@inheritDoc} */
	@Nonnull
	@Override
	public PersistentDataContainer toPrimitive(@Nonnull final Map<K, V> map,
											   @Nonnull final PersistentDataAdapterContext adapter) {
		final var pdc = adapter.newPersistentDataContainer();
		map.forEach((key, value) -> pdc.set(
				this.keyEncoder.toPrimitive(key, adapter),
				this.valueEncoder, value));
		return pdc;
	}

	/** {@inheritDoc} */
	@Nonnull
	@Override
	public Map<K, V> fromPrimitive(@Nonnull final PersistentDataContainer pdc,
								   @Nonnull final PersistentDataAdapterContext adapter) {
		final Set<NamespacedKey> keys = pdc.getKeys();
		final Map<K, V> map = newMap(keys.size());
		keys.forEach((key) -> map.put(
				this.keyEncoder.fromPrimitive(key, adapter),
				Objects.requireNonNull(pdc.get(key, this.valueEncoder))));
		return map;
	}

}