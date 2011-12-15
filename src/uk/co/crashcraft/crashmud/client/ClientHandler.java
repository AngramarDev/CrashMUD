package uk.co.crashcraft.crashmud.client;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;

public class ClientHandler {
    
    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;
    public CommandHandler cmdHandler;
    private boolean atLogin = false;

    public ClientHandler (Socket s) throws IOException {
        socket = s;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.println("  ___                                             \n" +
                    " / _ \\                                            \n" +
                    "/ /_\\ \\_ __   __ _ _ __ __ _ _ __ ___   __ _ _ __ \n" +
                    "|  _  | '_ \\ / _` | '__/ _` | '_ ` _ \\ / _` | '__|\n" +
                    "| | | | | | | (_| | | | (_| | | | | | | (_| | |   \n" +
                    "\\_| |_/_| |_|\\__, |_|  \\__,_|_| |_| |_|\\__,_|_|   \n" +
                    "              __/ |                               \n" +
                    "             |___/         By Crashdoom           \n");
        out.println("You are connected to the ALPHA server");
        out.println("If you're registered please use    ':connect <name> <pass>'");
        out.println("If you want to join as a guest use ':connect guest'\n");
        out.println("Use command 'QUIT' to exit (In UPPER case!)\n");
        out.flush();
        cmdHandler = new CommandHandler();
        System.out.println("Client connected: " + s.getInetAddress());
        while (s.isConnected()) {
            String inData = in.readLine();
            String[] cmd = inData.split(" ");
            if (!cmdHandler.processCommand(cmd[0])) {
                if (cmd[0].equals("QUIT")) {
                    out.println("Goodbye!");
                    out.flush();
                    break;
                } else if (cmd[0].equals(":connect")) {
                    if (cmd.length != 2) {
                        out.println("Invalid parameters!");
                        out.flush();
                    } else {
                        out.println("Logged in as: " + cmd[1]);
                        out.flush();
                    }
                } else {
                    if (cmd[0].startsWith(":")) {
                        out.println("Unknown Command.");
                        out.flush();
                    }
                }
            }
        }
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
