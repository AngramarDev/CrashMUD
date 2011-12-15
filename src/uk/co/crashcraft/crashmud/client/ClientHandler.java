package uk.co.crashcraft.crashmud.client;

import uk.co.crashcraft.crashmud.Main;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    
    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;
    public CommandHandler cmdHandler;
    public String username = null;
    public GlobalVars gVars;
    private boolean atLogin = false;
    private volatile Thread thisThread;

    public ClientHandler (GlobalVars vars, Socket s) {
        gVars = vars;
        socket = s;
    }

    public void run () {
        thisThread = Thread.currentThread();
        try {
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
            System.out.println("Client connected: " + socket.getInetAddress());
            while (socket.isConnected()) {
                String inData = in.readLine();
                String[] cmd = inData.split(" ");
                if (!cmdHandler.processCommand(cmd[0])) {
                    if (cmd[0].equals("QUIT")) {
                        out.println("Goodbye!");
                        out.flush();
                        System.out.println("Client disconnecting: " + socket.getInetAddress());
                        socket.close();
                        thisThread = null;
                        return;
                    } else if (cmd[0].equals(":connect")) {
                        if (cmd.length != 2) {
                            out.println("Invalid parameters!");
                            out.flush();
                        } else {
                            if (username != null) {
                                out.println("You're logged in as: " + username);
                                out.flush();
                            //} else if (gVars.activeUsers.contains(cmd[1])) {
                            //    out.println("This username is already in use!");
                            //    out.flush();
                            } else { 
                                out.println("Logged in as: " + cmd[1]);
                                out.flush();
                                //gVars.activeUsers.add(cmd[1]);
                                username = cmd[1];
                            }
                        }
                    } else {
                        if (cmd[0].startsWith(":")) {
                            out.println("Unknown Command.");
                            out.flush();
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (username != null) {
                gVars.activeUsers.remove(username);
            }
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } catch (NullPointerException e) {
            if (username != null) {
                gVars.activeUsers.remove(username);
            }
        }
    }
}
