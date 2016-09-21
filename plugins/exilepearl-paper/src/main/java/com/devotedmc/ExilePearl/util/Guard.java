package com.devotedmc.ExilePearl.util;

import org.apache.commons.lang.NullArgumentException;

public final class Guard {
    public static void ArgumentNotNull(Object argument, String parameterName) {
        if (parameterName == null) {
            throw new NullArgumentException("parameterName");
        }

        if (argument == null) {
            throw new NullArgumentException(parameterName);
        }
    }
    
    public static void ArgumentNotNullOrEmpty(String argument, String parameterName) {
        if (parameterName == null) {
            throw new NullArgumentException("parameterName");
        }

        if (argument == null) {
            throw new NullArgumentException(parameterName);
        }
        
        if (argument == "") {
        	throw new RuntimeException(parameterName + " can't be empty.");
        }
    }
    
    public static void ArgumentNotEquals(Object argument, String parameterName, Object other, String otherName) {
        if (argument.equals(other)) {
            throw new RuntimeException(parameterName + " can't be equal to " + otherName);
        }
    }
}
