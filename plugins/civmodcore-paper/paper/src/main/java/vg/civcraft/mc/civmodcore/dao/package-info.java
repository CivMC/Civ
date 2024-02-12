/**
 * Convenient Database handling for shared use by all plugins.
 * 
 * Wraps HikariCP for easy connection pooling.
 * 
 * Plugins should use the {@link vg.civcraft.mc.civmodcore.dao.ManagedDatasource} class.
 * 
 * If you know what you're doing and know that the Managed class isn't fit for you,
 * you can directly leverage {@link vg.civcraft.mc.civmodcore.dao.ConnectionPool}.
 * 
 * @author ProgrammerDan
 *
 */
package vg.civcraft.mc.civmodcore.dao;
