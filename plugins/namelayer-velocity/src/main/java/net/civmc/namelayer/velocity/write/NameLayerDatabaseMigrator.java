package net.civmc.namelayer.velocity.write;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import net.civmc.nameapi.Migrator;
import org.slf4j.Logger;

public final class NameLayerDatabaseMigrator {

    private NameLayerDatabaseMigrator() {
    }

    public static boolean migrate(final DataSource dataSource, final Logger logger) {
        final Migrator migrator = new Migrator();
        migrator.registerMigration("namelayer", 0,
            "create table if not exists faction_id("
                + "group_id int not null auto_increment,"
                + "group_name varchar(255),"
                + "primary key(group_id),"
                + "index faction_id_index(group_name)) charset=latin1",
            "create table if not exists faction("
                + "group_name varchar(255),"
                + "founder varchar(36),"
                + "password varchar(255) default null,"
                + "discipline_flags int(11) not null default 0,"
                + "last_timestamp datetime not null default now(),"
                + "group_color varchar(12) not null default 'gray',"
                + "primary key(group_name)) charset=latin1",
            "alter table faction add column if not exists last_timestamp datetime not null default now()",
            "alter table faction add column if not exists group_color varchar(12) not null default 'gray'",
            "create table if not exists faction_member("
                + "group_id int not null,"
                + "member_name varchar(36),"
                + "role varchar(10) not null default 'MEMBERS',"
                + "unique key uq_group_member(group_id, member_name),"
                + "index faction_member_index(group_id)) charset=latin1",
            "create table if not exists blacklist("
                + "member_name varchar(36) not null,"
                + "group_id int not null) charset=latin1",
            "create table if not exists toggleAutoAccept("
                + "uuid varchar(36) not null,"
                + "primary key key_uuid(uuid))",
            "create table if not exists default_group("
                + "uuid varchar(36) not null,"
                + "defaultgroup varchar(255) not null,"
                + "primary key key_uuid(uuid))",
            "create table if not exists group_invitation("
                + "uuid varchar(36) not null,"
                + "groupName varchar(255) not null,"
                + "role varchar(10) not null default 'MEMBERS',"
                + "date datetime not null default now(),"
                + "constraint UQ_uuid_groupName unique(uuid, groupName)) charset=latin1",
            "create table if not exists nameLayerNameChanges("
                + "uuid varchar(36) not null,"
                + "oldName varchar(32) not null,"
                + "newName varchar(32) not null,"
                + "primary key(uuid)) charset=latin1",
            "create table if not exists namelayer_cache_version("
                + "id tinyint not null,"
                + "cache_version bigint not null,"
                + "primary key(id))",
            "insert ignore into namelayer_cache_version(id, cache_version) values (1, 0)",
            "create table if not exists permission_by_group_name("
                + "group_id int not null,"
                + "role varchar(40) not null,"
                + "permission_name varchar(128) not null,"
                + "primary key(group_id,role,permission_name))",
            "create table if not exists permissionByGroup("
                + "group_id int not null,"
                + "role varchar(40) not null,"
                + "perm_id int not null,"
                + "primary key(group_id,role,perm_id))",
            "create table if not exists permissionIdMapping("
                + "perm_id int not null,"
                + "name varchar(64) not null,"
                + "primary key(perm_id))",
            "insert ignore into permission_by_group_name(group_id, role, permission_name) "
                + "select pbg.group_id, pbg.role, pim.name from permissionByGroup pbg "
                + "inner join permissionIdMapping pim on pim.perm_id = pbg.perm_id",
            "drop table if exists permissionByGroup",
            "drop table if exists permissionIdMapping",
            "drop procedure if exists createGroup",
            "create definer=current_user procedure createGroup("
                + "in group_name varchar(255), "
                + "in founder varchar(36), "
                + "in password varchar(255), "
                + "in discipline_flags int(11)) "
                + "sql security invoker "
                + "begin"
                + " if (select (count(*) = 0) from faction_id q where q.group_name = group_name) is true then"
                + "  insert into faction_id(group_name) values (group_name); "
                + "  insert into faction(group_name, founder, password, discipline_flags) values (group_name, founder, password, discipline_flags);"
                + "  insert into faction_member (member_name, role, group_id) select founder, 'OWNER', f.group_id from faction_id f where f.group_name = group_name and founder is not null; "
                + "  select f.group_id from faction_id f where f.group_name = group_name; "
                + " end if; "
                + "end",
            "drop procedure if exists deletegroupfromtable",
            "create definer=current_user procedure deletegroupfromtable("
                + "in targetGroupName varchar(255),"
                + "in specialAdminGroup varchar(255)) "
                + "sql security invoker begin "
                + "delete fm.* from faction_member fm "
                + "inner join faction_id fi on fm.group_id = fi.group_id "
                + "where fi.group_name = targetGroupName;"
                + "delete b.* from blacklist b "
                + "inner join faction_id fi on b.group_id = fi.group_id "
                + "where fi.group_name = targetGroupName;"
                + "delete p.* from permission_by_group_name p "
                + "inner join faction_id fi on p.group_id = fi.group_id "
                + "where fi.group_name = targetGroupName;"
                + "delete from group_invitation where groupName = targetGroupName;"
                + "delete from default_group where defaultgroup = targetGroupName;"
                + "update faction_id set group_name = specialAdminGroup where group_name = targetGroupName;"
                + "delete from faction where group_name = targetGroupName;"
                + "end");
        try (Connection connection = dataSource.getConnection()) {
            migrator.migrate(connection);
            logger.info("NameLayer Velocity database migration completed");
            return true;
        } catch (final SQLException exception) {
            logger.error("NameLayer Velocity database migration failed", exception);
            return false;
        }
    }
}
