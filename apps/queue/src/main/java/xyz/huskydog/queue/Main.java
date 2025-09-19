package xyz.huskydog.queue;

import java.io.File;
import java.nio.file.Path;

public class Main {

    public static final String VERSION = "1.0.0";
    public static final String MC_VERSION = "1.21.8";

    public static void main(String[] args) {
        Queue queue = new Queue();
        queue.start();
    }

    public static File getCwd() {
        return new File(System.getProperty("user.dir")).toPath().resolve("viaproxy").toFile();
    }
}
