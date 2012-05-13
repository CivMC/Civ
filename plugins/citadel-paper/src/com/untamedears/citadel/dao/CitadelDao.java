package com.untamedears.citadel.dao;

import com.lennardf1989.bukkitex.MyDatabase;
import com.untamedears.citadel.entity.Faction;
import com.untamedears.citadel.entity.FactionMember;
import com.untamedears.citadel.entity.Reinforcement;
import com.untamedears.citadel.entity.ReinforcementKey;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/18/12
 * Time: 3:58 PM
 */
public class CitadelDao extends MyDatabase {

    public CitadelDao(JavaPlugin plugin) {
        super(plugin);

        Configuration config = plugin.getConfig();

        initializeDatabase(
                config.getString("database.driver"),
                config.getString("database.url"),
                config.getString("database.username"),
                config.getString("database.password"),
                config.getString("database.isolation"),
                config.getBoolean("database.logging", false),
                config.getBoolean("database.rebuild", true)
        );

        config.set("database.rebuild", false);
        plugin.saveConfig();
    }

    @Override
    protected List<Class<?>> getDatabaseClasses() {
        return Arrays.asList(Faction.class, FactionMember.class, Reinforcement.class, ReinforcementKey.class);
    }

    public void save(Object object) {
        getDatabase().save(object);
    }

    public void delete(Object object) {
        getDatabase().delete(object);
    }

    public Faction getOrCreateFaction(String name, String founder) {
        Faction faction = findGroupByName(name);
        if (faction == null) {
            faction = new Faction(name, founder);
            save(faction);
        }
        return faction;
    }

    public Faction findGroupByName(String name) {
        return getDatabase().createQuery(Faction.class, "find faction where name = :name")
                .setParameter("name", name)
                .findUnique();
    }
    
    public boolean hasGroupMember(String groupName, String memberName) {
        return getDatabase().createQuery(FactionMember.class, "find factionMember where memberName = :memberName and factionName = :groupName")
                .setParameter("memberName", memberName)
                .setParameter("groupName", groupName)
                .findRowCount() > 0;
    }

    public FactionMember findGroupMember(String groupName, String memberName) {
        return getDatabase().createQuery(FactionMember.class, "find factionMember where memberName = :memberName and factionName = :groupName")
                .setParameter("memberName", memberName)
                .setParameter("groupName", groupName)
                .findUnique();
    }
    
    public Set<FactionMember> findGroupMembers(String groupName) {
        return getDatabase().createQuery(FactionMember.class, "find factionMember where factionName = :groupName")
                .setParameter("groupName", groupName)
                .findSet();
    }

    public Reinforcement findReinforcement(Block block) {
        return findReinforcement(block.getLocation());
    }

    public Reinforcement findReinforcement(Location location) {
        return getDatabase().createQuery(Reinforcement.class, "find reinforcement where x = :x and y = :y and z = :z and world = :world")
                .setParameter("x", location.getX())
                .setParameter("y", location.getY())
                .setParameter("z", location.getZ())
                .setParameter("world", location.getWorld().getName())
                .findUnique();
    }
}