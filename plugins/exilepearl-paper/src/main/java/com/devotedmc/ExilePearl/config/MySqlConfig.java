package com.devotedmc.ExilePearl.config;

public interface MySqlConfig {

	/**
	 * Gets the SQL host
	 * @return the SQL host
	 */
	String getMySqlHost();

	/**
	 * Gets the SQL user name
	 * @return the SQL user name
	 */
	String getMySqlUsername();
	
	/**
	 * Gets the SQL password
	 * @return the SQL password
	 */
	String getMySqlPassword();
	
	/**
	 * Gets the SQL name
	 * @return the SQL name
	 */
	String getMySqlName();
	
	/**
	 * Gets the SQL port
	 * @return the SQL port
	 */
	int getMySqlPort();
	
	/**
	 * Gets the SQL pool size
	 * @return the SQL pool size
	 */
	int getMySqlPoolSize();
	
	/**
	 * Gets the SQL connection timeout
	 * @return the SQL connection timeout
	 */
	int getMySqlConnectionTimeout();
	
	/**
	 * Gets the SQL idle timeout
	 * @return the SQL idle timeout
	 */
	int getMySqlIdleTimeout();
	
	/**
	 * Gets the SQL max lifetime
	 * @return the SQL max lifetime
	 */
	int getMySqlMaxLifetime();
	
	/**
	 * Gets whether to migrate prison pearl data
	 * @return whether to migrate or not
	 */
	boolean getMigratePrisonPearl();
}
