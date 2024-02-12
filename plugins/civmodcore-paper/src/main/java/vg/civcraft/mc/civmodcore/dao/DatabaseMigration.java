package vg.civcraft.mc.civmodcore.dao;

import java.sql.SQLException;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Interface to allow for more object-oriented based migration. I've noticed as plugins get older, their DAOs get
 * larger and larger to accommodate more and more migrations. This interface allows migrations to be split up into
 * individually functional classes with the convention of NEVER using plugin APIs: that anything the migration needs
 * to do should be encapsulated within the migration class.
 */
public interface DatabaseMigration {

	/**
	 * @return Returns this migration's id -- 0, 1, 2, etc, must be unique.
	 */
	int getMigrationId();

	/**
	 * @return Returns whether errors in this migration should be ignored.
	 */
	default boolean shouldIgnoreErrors() {
		return false;
	}

	/**
	 * @return Returns this migration's queries. Each query will be run in sequences. Must not be null or empty!
	 */
	@Nonnull
	String[] getMigrationQueries();

	/**
	 * An optional callback that'll run after the migration has completed.
	 *
	 * @return Returns whether the callback completed successfully.
	 */
	default boolean migrationCallback(@Nonnull final ManagedDatasource datasource) throws SQLException {
		return true;
	}

	/**
	 * @param datasource The datasource to register this migration to.
	 */
	default void registerMigration(@Nonnull final ManagedDatasource datasource) {
		final var queries = getMigrationQueries();
		if (ArrayUtils.isEmpty(queries)) {
			throw new IllegalArgumentException("Migration queries cannot be null or empty!");
		}
		datasource.registerMigration(getMigrationId(),
				shouldIgnoreErrors(),
				() -> migrationCallback(datasource),
				queries);
	}

}
