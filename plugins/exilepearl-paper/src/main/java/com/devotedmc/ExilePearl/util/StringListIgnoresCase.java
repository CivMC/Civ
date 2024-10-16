package com.devotedmc.ExilePearl.util;

import java.util.ArrayList;

/**
 * String List class with an override for seeing if
 * it contains a string value, ignoring case
 * @author Gordon
 *
 */
@SuppressWarnings("serial")
public class StringListIgnoresCase extends ArrayList<String> {

	@Override
    public boolean contains(Object o) {
        String paramStr = (String)o;
        for (String s : this) {
            if (paramStr.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
}
