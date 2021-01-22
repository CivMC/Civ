package sh.okx.railswitch.switches;

import org.apache.commons.lang3.StringUtils;

/**
 * Switch type matcher, will match a type to a tag.
 */
public enum SwitchType {
    
    NORMAL("[destination]"),
    INVERTED("[!destination]");
    
    private final String tag;
    
    /**
     * Defines a new switch type by its tag. Should there be a duplicate tag, the {@link SwitchType#find(String) find()}
     * function will return the first value found.
     *
     * @param tag The tag to associate with this type.
     */
    SwitchType(String tag) {
        this.tag = tag;
    }
    
    /**
     * Attempts to match a switch type to a switch tag.
     *
     * @param tag The tag to match with a type.
     * @return Returns a match switch type, or null if none are found.
     */
    public static SwitchType find(String tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        for (SwitchType type : values()) {
            if (StringUtils.equalsIgnoreCase(tag, type.tag)) {
                return type;
            }
        }
        return null;
    }
    
}
