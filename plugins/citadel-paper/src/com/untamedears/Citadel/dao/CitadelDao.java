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
    private static final String CREATE_REINFORCEMENTS = "CREATE TABLE IF NOT EXISTS REINFORCEMENTS (x INTEGER,y INTEGER,z INTEGER,world TEXT,durability INTEGER);";
    private static final String CREATE_GROUPS = "CREATE TABLE IF NOT EXISTS GROUPS (grpName TEXT,member TEXT);";
    private static final String CREATE_REGISTRY = "CREATE TABLE IF NOT EXISTS REGISTRY (x INTEGER,y INTEGER,z INTEGER,world TEXT,grp TEXT);";

    private static final String INSERT_GROUP = "INSERT INTO GROUPS (grpName,member) values (?,?);";
    private static final String SELECT_GROUP = "SELECT grpName FROM GROUPS WHERE grpName=? AND member=?;";
    private static final String DELETE_GROUP = "DELETE FROM GROUPS WHERE grpName=? AND member=?;";

    private static final String INSERT_REINFORCEMENT = "INSERT INTO REINFORCEMENTS (x,y,z,world,durability) values (?,?,?,?,?);";
    private static final String UPDATE_REINFORCEMENT = "UPDATE REINFORCEMENTS SET DURABILITY=DURABILITY-? WHERE x=? AND y=? AND z=? AND world=?;";
    private static final String SELECT_REINFORCEMENT = "SELECT DURABILITY FROM REINFORCEMENTS WHERE x=? AND y=? AND z=? AND world=?;";
    private static final String DELETE_REINFORCEMENT = "DELETE FROM REINFORCEMENTS WHERE x=:x AND y=? AND z=? AND world=?;";

    private static final String SELECT_REINFORCEMENTS = "SELECT X, Y, Z FROM REINFORCEMENTS WHERE DURABILITY >= 1 AND x<=? AND x>=? AND y<=? AND y>=? AND z<=? AND z>=? AND world=?";
    private static final String UPDATE_REINFORCEMENTS = "UPDATE REINFORCEMENTS SET DURABILITY = DURABILITY - 1 WHERE DURABILITY >= 1 AND x<=? AND x>=? AND y<=? AND y>=? AND z<=? AND z>=? AND world=?";
    
    private static final String INSERT_REGISTRY = "INSERT INTO REGISTRY (x,y,z,world,grp) VALUES (?,?,?,?,?)";
    private static final String SELECT_REGISTRY = "SELECT grp FROM REGISTRY WHERE x=? AND y=? AND z=? AND world=?";

    private Connection conn;

    public CitadelDao() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:plugins/Citadel/Citadel.db");
            conn.createStatement().execute(CREATE_REINFORCEMENTS);
            conn.createStatement().execute(CREATE_GROUPS);
            conn.createStatement().execute(CREATE_REGISTRY);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addPlayerToGroup(String group, String player) {
        try {
            PreparedStatement tmp = conn.prepareStatement(INSERT_GROUP);
            tmp.setString(1, group);
            tmp.setString(2, player);
            tmp.execute();
            tmp.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removePlayerFromGroup(String group, String player) {
        try {
            PreparedStatement tmp = conn.prepareStatement(DELETE_GROUP);
            tmp.setString(1, group);
            tmp.setString(2, player);
            tmp.execute();
            tmp.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPlayerInGroup(String group, String player) {
        try {
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public DelayedReinforcement addReinforcement(Block block, Material material) {
        try {
            PreparedStatement tmp = conn.prepareStatement(INSERT_REINFORCEMENT);
            setBlockParameters(tmp, block, 1);
            tmp.setInt(5, Citadel.materialStrengths.get(material));

            return new DelayedReinforcement(tmp);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer updateReinforcement(Block block, int durabilityDamage) {
        try {
            Integer durability = null;
            PreparedStatement tmp = conn.prepareStatement(UPDATE_REINFORCEMENT);
            tmp.setInt(1, durabilityDamage);
            setBlockParameters(tmp, block, 2);
            tmp.execute();
            if (tmp.getUpdateCount() > 0) {
                tmp.close();
                tmp = conn.prepareStatement(SELECT_REINFORCEMENT);
                setBlockParameters(tmp, block, 1);
                tmp.execute();

                ResultSet result = tmp.getResultSet();
                if (result.next()) {
                    durability = result.getInt(1);
                }

                result.close();
            }
            tmp.close();

            return durability;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeReinforcement(Block block) {
        try {
            PreparedStatement tmp = conn.prepareStatement(DELETE_REINFORCEMENT);
            setBlockParameters(tmp, block, 1);
            tmp.execute();
            tmp.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public ResultSet selectReinforcements(String worldName, Integer smallestX, Integer largestX, 
    		Integer smallestY, Integer largestY, Integer smallestZ, Integer largestZ){
    	try {
    		PreparedStatement tmp = conn.prepareStatement(SELECT_REINFORCEMENTS);
    		tmp.setString(1, worldName);
			tmp.setInt(2, largestX);
			tmp.setInt(3, smallestX);
			tmp.setInt(4, largestY); 
			tmp.setInt(5, smallestY);
			tmp.execute();
			ResultSet result = tmp.getResultSet();
			return result;
    	} catch (SQLException e){
    		throw new RuntimeException(e);
    	}
    }
    
    public void updateReinforcements(String worldName, Integer smallestX, Integer largestX, 
    		Integer smallestY, Integer largestY, Integer smallestZ, Integer largestZ){
    	try {
    		PreparedStatement tmp = conn.prepareStatement(UPDATE_REINFORCEMENTS);
    		tmp.setString(1, worldName);
			tmp.setInt(2, largestX);
			tmp.setInt(3, smallestX);
			tmp.setInt(4, largestY); 
			tmp.setInt(5, smallestY);
			tmp.execute();
    	} catch (SQLException e){
    		throw new RuntimeException(e);
    	}
    }

    public void addRegisteredGroup(Block block, String group) {
        try {
            PreparedStatement tmp = this.conn.prepareStatement(INSERT_REGISTRY);
            setBlockParameters(tmp, block, 1);
            tmp.setString(5, group);
            tmp.execute();
            tmp.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getRegisteredGroup(Block block) {
        try {
            String group = null;
            PreparedStatement tmp = conn.prepareStatement(SELECT_REGISTRY);
            setBlockParameters(tmp, block, 1);
            tmp.execute();
            ResultSet result = tmp.getResultSet();
            if (result.next()) {
                group = result.getString(1);
            }

            result.close();
            tmp.close();

            return group;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setBlockParameters(PreparedStatement tmp, Block block, int index) {
        try {
            tmp.setInt(index++, block.getX());
            tmp.setInt(index++, block.getY());
            tmp.setInt(index++, block.getZ());
            tmp.setString(index, block.getWorld().getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
