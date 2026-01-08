package me.thinhbuzz.scrcpy.gui.server;

import android.util.Log;

/**
 * Log both to Android logger (so that logs are visible in "adb logcat") and standard output/error (so that they are visible in the terminal
 * directly).
 */
public final class Ln {

    private static final String TAG = "ScrcpyGUI";
    private static final String PREFIX = "[Server] ";

    private Ln() {
    }

    public static void i(String message) {
        Log.i(TAG, message);
        System.out.print(PREFIX + "INFO: " + message + '\n');
    }

    public static void e(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
        System.err.print(PREFIX + "ERROR: " + message + "\n");
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }

    public static void e(String message) {
        e(message, null);
    }
}
