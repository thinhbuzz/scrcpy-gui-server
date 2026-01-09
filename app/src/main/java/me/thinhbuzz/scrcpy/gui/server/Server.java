package me.thinhbuzz.scrcpy.gui.server;

import me.thinhbuzz.scrcpy.gui.server.commands.ListCommand;

public final class Server {
    // CLASSPATH=/data/local/tmp/scrcpy-gui-server app_process / me.thinhbuzz.scrcpy.gui.server.Server --list app --list-type system && rm /data/local/tmp/scrcpy-gui-server
    public static void main(String... args) {
        try {
            new Server().start(args);
        } catch (Exception e) {
            Ln.e("Server.Main", e);
        }
    }

    void start(String[] args) {
        CliArgs cliArgs = new CliArgs(args);
        if (cliArgs.has("list")) {
            new ListCommand(cliArgs).run();
            return;
        }
        if (cliArgs.has("version")) {
            Ln.i("Scrcpy GUI Server version: 1.0.2 - 1002");
        }
    }
}
