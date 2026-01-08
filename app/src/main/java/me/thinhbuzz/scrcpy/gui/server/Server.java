package me.thinhbuzz.scrcpy.gui.server;

import me.thinhbuzz.scrcpy.gui.server.commands.ListCommand;

public final class Server {
    public static void main(String... args) {
        Ln.i("Hello World!");
        try {
            new Server().start(args);
        } catch (Exception e) {
            Ln.e("Server.Main", e);
        }
    }

    void start(String[] args) {
        Ln.i("Starting server...");
        CliArgs cliArgs = new CliArgs(args);
        if (cliArgs.has("list")) {
            new ListCommand(cliArgs).run();
        }
        Ln.i("Parsed args: " + cliArgs);
        Ln.i("Done.");
    }
}
