package net.civmc.kiragatewayvelocity;

public class KiraUtil {

    public static String cleanUp(String s) {
        return s.replaceAll("[^\\p{InBasic_Latin}\\p{InLatin-1Supplement}]", "");
    }

}
