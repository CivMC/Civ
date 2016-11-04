package com.untamedears.JukeAlert.model;

import java.text.ParseException;

import org.bukkit.ChatColor;

/**
 * Enum that represents a type of action that a snitch can record, and the value
 * that goes into the database for said action
 *
 *
 */
public enum LoggedAction {

    // ONCE THIS GOES INTO PRODUCTION, _NEVER_ CHANGE THESE, only mark some as not used and add more with larger values!
    UNKNOWN(0x7FFFFFFF, "Unknown", ChatColor.WHITE, 0),
    KILL(0, "Killed", ChatColor.DARK_RED, 3),
    BLOCK_PLACE(1, "Block Place", ChatColor.DARK_RED, 2),
    BLOCK_BREAK(2, "Block Break", ChatColor.DARK_RED, 2),
    BUCKET_FILL(3, "Bucket Fill", ChatColor.GREEN, 2),
    BUCKET_EMPTY(4, "Bucket Empty", ChatColor.DARK_RED, 2),
    ENTRY(5, "Entry", ChatColor.BLUE, 1),
    USED(6, "Used", ChatColor.GREEN, 2),
    IGNITED(7, "Ignited", ChatColor.GOLD, 2),
    BLOCK_BURN(8, "Block Burn", ChatColor.DARK_RED, 2),
    BLOCK_USED(9, "Used", ChatColor.GREEN, 2),
    LOGIN(10, "Login", ChatColor.GREEN, 1),
    LOGOUT(11, "Logout", ChatColor.GREEN, 1),
    EXCHANGE(12, "Exchanged", ChatColor.DARK_GRAY, 2),
    VEHICLE_DESTROY(13, "Destroyed", ChatColor.DARK_RED, 3),
    ENTITY_MOUNT(14, "Mount", ChatColor.RED, 3),
    ENTITY_DISMOUNT(15, "Dismount", ChatColor.GOLD, 3);
    
    private int value;
    private String actionString;
    private ChatColor actionColor;
    private int actionTextType;

    // constructor, has to be private
    private LoggedAction(int value, String actionString, ChatColor actionColor, int actionTextType) {
        this.value = value;
        this.actionString = actionString;
        this.actionColor = actionColor;
        this.actionTextType = actionTextType;
    }

    public int getLoggedActionId() {
        return this.value;
    }
    
    public String getActionString(){
        return this.actionString;
    }
    
    public ChatColor getActionColor(){
        return this.actionColor;
    }
    
    public int getActionTextType(){
        return this.actionTextType;
    }

    public static LoggedAction getFromId(int id) {
        switch(id) {
            case 0: return KILL;
            case 1: return BLOCK_PLACE;
            case 2: return BLOCK_BREAK;
            case 3: return BUCKET_FILL;
            case 4: return BUCKET_EMPTY;
            case 5: return ENTRY;
            case 6: return USED;
            case 7: return IGNITED;
            case 8: return BLOCK_BURN;
            case 9: return BLOCK_USED;
            case 10: return LOGIN;
            case 11: return LOGOUT;
            case 12: return EXCHANGE;
            case 13: return VEHICLE_DESTROY;
            case 14: return ENTITY_MOUNT;
            case 15: return ENTITY_DISMOUNT;
            default: return UNKNOWN;
        }
    }
    
    /**
     * Attempts to match a string to one of the LoggedAction actions
     * @param action - string representation of the action (case insensitive). 
     *                 Either human-friendly (e.g. "Buket Fill") or one of the enum names (e.g. "BLOCK_BURN")
     * @return matching LoggedAction
     * @throws ParseException if the string couldn't be decoded to a LoggedAction
     */
    public static LoggedAction fromString(String action) throws ParseException{        
        for (LoggedAction a : LoggedAction.values()){
            String actionName = a.toString();
            String actionDisp = a.actionString;
            if (actionName.equalsIgnoreCase(action) || actionDisp.equalsIgnoreCase(action)){
                return a;
            }
        }
        throw new ParseException("Couldn't convert " + action + " to a LoggedAction", 0);
    }
}
