package xyz.huskydog.queue;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class Utils {
    // thanks to https://github.com/LOOHP/Limbo/blob/75191a83f732ff37eac77c669040edc27dd0e011/src/main/java/com/loohp/limbo/utils/DataTypeIO.java#L53
    public static void writeString(DataOutputStream out, String string, Charset charset) throws IOException {
        byte[] bytes = string.getBytes(charset);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    public static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & -128) != 0) {
            out.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        out.writeByte(value);
    }
}
