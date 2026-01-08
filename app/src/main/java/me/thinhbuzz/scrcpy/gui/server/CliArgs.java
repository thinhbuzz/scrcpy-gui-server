package me.thinhbuzz.scrcpy.gui.server;

import java.util.HashMap;
import java.util.Map;

public class CliArgs {
    private final Map<String, String> argsMap = new HashMap<>();

    public CliArgs(String[] args) {
        parse(args);
    }

    private void parse(String[] args) {
        if (args == null) return;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("-")) {
                String key = arg.replaceAll("^-+", "");
                String value = "true";

                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    value = args[i + 1];
                    i++;
                }

                argsMap.put(key, value);
            }
        }
    }

    public boolean has(String key) {
        return argsMap.containsKey(key);
    }

    public String get(String key) {
        return argsMap.get(key);
    }

    public String get(String key, String defaultValue) {
        return argsMap.getOrDefault(key, defaultValue);
    }

    public Integer getInt(String key) {
        String val = argsMap.get(key);
        try {
            return val != null ? Integer.parseInt(val) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int getInt(String key, int defaultValue) {
        Integer val = getInt(key);
        return val != null ? val : defaultValue;
    }

    public String toString() {
        return argsMap.toString();
    }
}