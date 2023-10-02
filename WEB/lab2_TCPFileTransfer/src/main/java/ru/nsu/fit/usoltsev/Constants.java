package ru.nsu.fit.usoltsev;


/**
 * Interface with used constants
 */
public final class Constants {
    public static int MIN_PORT_NUMBER = 1;
    public static int MAX_PORT_NUMBER = 65536; // 65535 = Short.MAX_VALUE * 2 + 1
    public static int BUFFER_SIZE = 2096; // in bytes
    public static int TERABYTE = 1024 * 1024 * 1024;
    public static int MAX_FILE_NAME_SIZE = 4096;
    public static Boolean SUCCESS = true;
    public static Boolean FAIL = false;
}
