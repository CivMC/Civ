/**
 * Created by Aleksey on 14.07.2017.
 */

package isaac.bastion.storage;

import isaac.bastion.BastionType;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class Database {
	public static void registerMigrations(ManagedDatasource db) {
		db.registerMigration(0, false,
				"create table if not exists `bastion_blocks`("
						+ "bastion_id int(10) unsigned NOT NULL AUTO_INCREMENT,"
						+ "bastion_type varchar(40) DEFAULT '" + BastionType.getDefaultType() + "',"
						+ "loc_x int(10),"
						+ "loc_y int(10),"
						+ "loc_z int(10),"
						+ "loc_world varchar(40) NOT NULL,"
						+ "placed bigint(20) Unsigned,"
						+ "fraction float(20) Unsigned,"
						+ "PRIMARY KEY (`bastion_id`));");

		db.registerMigration(1, false,
				"ALTER TABLE bastion_blocks ADD COLUMN IF NOT EXISTS bastion_type VARCHAR(40) DEFAULT '"
						+ BastionType.getDefaultType() + "';");

		db.registerMigration(2, false,
				"ALTER TABLE bastion_blocks ADD COLUMN IF NOT EXISTS dead TINYINT(1) DEFAULT 0;");

		db.registerMigration(3, false,
				"create table if not exists `bastion_groups`("
						+ "bastion_group_id int not null,"
						+ "allowed_group_id int not null,"
						+ "PRIMARY KEY (bastion_group_id, allowed_group_id));");
	}
}
