package net.minelink.ctplus.util;

import static com.google.common.base.Preconditions.*;

public final class Strings2 {
    private Strings2() {
    }

    public static String stripLeading(String str, String leading) {
        if (checkNotNull(str, "Null string").startsWith(checkNotNull(leading, "Null leading string"))) {
            return str.substring(leading.length());
        } else {
            return str;
        }
    }

    public static String stripLeading(String str, char leading) {
        if (!checkNotNull(str, "Null string").isEmpty() && str.charAt(0) == leading) {
            return str.substring(1);
        } else {
            return str;
        }
    }
}
