package vg.civcraft.mc.civmodcore.pdc;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public abstract class AbstractPersistentDataType<P, C> implements PersistentDataType<P, C> {

	protected final Class<P> primitiveClass;
	protected final Class<C> complexClass;

	public AbstractPersistentDataType(@Nonnull final Class<P> primitiveClass,
									  @Nonnull final Class<C> complexClass) {
		this.primitiveClass = Objects.requireNonNull(primitiveClass);
		this.complexClass = Objects.requireNonNull(complexClass);
	}

	/** {@inheritDoc} */
	@Nonnull
	@Override
	public final Class<P> getPrimitiveType() {
		return this.primitiveClass;
	}

	/** {@inheritDoc} */
	@Nonnull
	@Override
	public final Class<C> getComplexType() {
		return this.complexClass;
	}

	/** {@inheritDoc} */
	@Nonnull
	@Override
	public abstract P toPrimitive(@Nonnull C instance,
								  @Nonnull PersistentDataAdapterContext adapter);

	/** {@inheritDoc} */
	@Nonnull
	@Override
	public abstract C fromPrimitive(@Nonnull P raw,
									@Nonnull PersistentDataAdapterContext adapter);

}
