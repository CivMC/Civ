package com.untamedears.Citadel.dao;

import com.untamedears.Citadel.Citadel;
import com.untamedears.Citadel.DelayedReinforcement;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.sql.*;

/**
 * Created by IntelliJ IDEA.
 * User: chrisrico
 * Date: 3/18/12
 * Time: 3:58 PM
 */
public class CitadelDao {
    private static final String CREATE_REINFORCEMENTS = "CREATE TABLE IF NOT EXISTS REENFORCEMENTS (x INTEGER,y INTEGER,z INTEGER,world TEXT,durability INTEGER);";
    private static final String CREATE_GROUPS = "CREATE TABLE IF NOT EXISTS GROUPS (grpName TEXT,member TEXT);";
    private static final String CREATE_REGISTRY = "CREATE TABLE IF NOT EXISTS REGISTRY (x INTEGER,y INTEGER,z INTEGER,world TEXT,grp TEXT);";
    
    private static final String INSERT_GROUP = "INSERT INTO GROUPS (grpName,member) values (?,?);";
    private static final String SELECT_GROUP = "SELECT grpName FROM GROUPS WHERE grpName=? AND member=?;";
    private static final String DELETE_GROUP = "DELETE FROM GROUPS WHERE grpName=? AND member=?;";

    private static final String INSERT_REINFORCEMENT = "INSERT INTO REENFORCEMENTS (x,y,z,world,durability) values (?,?,?,?,?);";
    private static final String UPDATE_REINFORCEMENT = "UPDATE REENFORCEMENTS SET DURABILITY=DURABILITY-? WHERE x=? AND y=? AND z=? AND world=?;";
    private static final String SELECT_REINFORCEMENT = "SELECT DURABILITY FROM REENFORCEMENTS WHERE x=? AND y=? AND z=? AND world=?;";
    private static final String DELETE_REINFORCEMENT = "DELETE FROM REENFORCEMENTS WHERE x=:x AND y=? AND z=? AND world=?;";
    
    private static final String SELECT_REGISTRY = "SELECT grp FROM REGISTRY WHERE x=? AND y=? AND z=? AND world=?";

    private Connection conn;

    public CitadelDao() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");

        conn = DriverManager.getConnection("jdbc:sqlite:plugins/Citadel/Citadel.db");
        conn.createStatement().execute(CREATE_REINFORCEMENTS);
        conn.createStatement().execute(CREATE_GROUPS);
        conn.createStatement().execute(CREATE_REGISTRY);
    }

    public void addPlayerToGroup(String group, String player) throws SQLException {
        PreparedStatement tmp = conn.prepareStatement(INSERT_GROUP);
        tmp.setString(1, group);
        tmp.setString(2, player);
        tmp.execute();
        tmp.close();
    }

    public void removePlayerFromGroup(String group, String player) throws SQLException {
        PreparedStatement tmp = conn.prepareStatement(DELETE_GROUP);
        tmp.setString(1, group);
        tmp.setString(2, player);
        tmp.execute();
        tmp.close();
    }

    public boolean isPlayerInGroup(String group, String player) throws SQLException {
        boolean isInGroup;
        PreparedStatement tmp = conn.prepareStatement(SELECT_GROUP);
        tmp.setString(1, group);
        tmp.setString(2, player);
        tmp.execute();
        ResultSet result = tmp.getResultSet();
        isInGroup = result.next();

        result.close();
        tmp.close();

        return isInGroup;
    }

    public DelayedReinforcement addReinforcement(Block block, Material material) throws SQLException {
        PreparedStatement tmp = conn.prepareStatement(INSERT_REINFORCEMENT);
        setBlockParameters(tmp, block);
        tmp.setInt(5, Citadel.materialStrengths.get(material));

        return new DelayedReinforcement(tmp);
    }

    public Integer updateReinforcement(Block block, int durabilityDamage) throws SQLException {
        Integer durability = null;
        PreparedStatement tmp = conn.prepareStatement(UPDATE_REINFORCEMENT + SELECT_REINFORCEMENT);
        tmp.setInt(1, durabilityDamage);
        setBlockParameters(tmp, block);
        setBlockParameters(tmp, block);
        tmp.execute();
        ResultSet result = tmp.getResultSet();
        if (result.next()) {
            durability = result.getInt(1);
        }

        result.close();
        tmp.close();

        return durability;
    }

    public void removeReinforcement(Block block) throws SQLException {
        PreparedStatement tmp = conn.prepareStatement(DELETE_REINFORCEMENT);
        setBlockParameters(tmp, block);
        tmp.execute();
        tmp.close();
    }
    
    public void addRegisteredGroup(Block block, String group) throws SQLException {
        PreparedStatement tmp = this.conn.prepareStatement(
                "INSERT INTO REGISTRY (x,y,z,world,grp) VALUES (?,?,?,?,?)");
        setBlockParameters(tmp, block);
        tmp.setString(5, group);
        tmp.execute();
        tmp.close();
    }

    public String getRegisteredGroup(Block block) throws SQLException {
        String group = null;
        PreparedStatement tmp = conn.prepareStatement(SELECT_REGISTRY);
        setBlockParameters(tmp, block);
        tmp.execute();
        ResultSet result = tmp.getResultSet();
        if (result.next()) {
            group = result.getString(1);
        }

        result.close();
        tmp.close();

        return group;
    }

    private void setBlockParameters(PreparedStatement tmp, Block block) throws SQLException {
        int modifier = tmp.getParameterMetaData().getParameterCount();
        tmp.setInt(1 + modifier, block.getX());
        tmp.setInt(2 + modifier, block.getY());
        tmp.setInt(3 + modifier, block.getZ());
        tmp.setString(4 + modifier, block.getWorld().getName());
    }

    public void close() throws SQLException {
        conn.close();
    }
}
