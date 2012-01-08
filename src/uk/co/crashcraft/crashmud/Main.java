package uk.co.crashcraft.crashmud;

import uk.co.crashcraft.crashmud.client.ClientHandler;
import uk.co.crashcraft.crashmud.client.GlobalVars;
import uk.co.crashcraft.crashmud.client.KeepAlive;
import uk.co.crashcraft.crashmud.console.ConsoleHandler;
import uk.co.crashcraft.crashmud.environment.Environment;
import uk.co.crashcraft.crashmud.ircbot.IRCBot;
import uk.co.crashcraft.crashmud.npc.NPC;

import java.net.*;
import java.io.*;
import java.security.MessageDigest;

public class Main {

    public static String version = "0.1.1 Alpha";
    public static ServerSocket serverSocket;
    public static boolean shutdownServer = false;
    public static int port = 4444;
    public static int maxConnections = 5;
    public static GlobalVars globalVars;
    public static boolean debug = true;
    public static MySQL mysql = null;

    public static void main(String args[]) throws IOException {
        System.out.println("  ___                                             \n" +
                " / _ \\                                            \n" +
                "/ /_\\ \\_ __   __ _ _ __ __ _ _ __ ___   __ _ _ __ \n" +
                "|  _  | '_ \\ / _` | '__/ _` | '_ ` _ \\ / _` | '__|\n" +
                "| | | | | | | (_| | | | (_| | | | | | | (_| | |   \n" +
                "\\_| |_/_| |_|\\__, |_|  \\__,_|_| |_| |_|\\__,_|_|   \n" +
                "              __/ |                               \n" +
                "             |___/                                ");
        System.out.println("MUD Server Version " + version);
        System.out.println("Starting on port " + port);
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Could not listen on port: " + port);
            System.exit(-1);
        }
        System.out.println("Now accepting connections!");
        System.out.println("Maximum connections: " + maxConnections);
        globalVars = new GlobalVars();
        mysql = new MySQL();
        new Thread(mysql).start();
        IRCBot ircbot = new IRCBot(globalVars);
        new Thread(ircbot).start();
        Runnable console = new ConsoleHandler(globalVars, ircbot);
        new Thread(console).start();
        KeepAlive kAlive = new KeepAlive(globalVars);
        new Thread(kAlive).start();
        NPC npc = new NPC(globalVars);
        new Thread(npc).start();
        Environment environment = new Environment(globalVars);
        new Thread(environment).start();
        while (!shutdownServer) {
            Socket s = serverSocket.accept();
            Runnable client = new ClientHandler(globalVars, kAlive, ircbot, environment, s);
            new Thread(client).start();
        }
        serverSocket.close();
    }

    public static String md5Encrypt(String string) {
        String encrypted = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] stringBytes = string.getBytes();

            digest.reset();
            digest.update(stringBytes);
            byte[] message = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < message.length; i++) {
                hexString.append(Integer.toHexString(
                        0xFF & message[i]));
            }
            encrypted = hexString.toString();
        }
        catch (Exception e) {
        }
        return encrypted;
    }
}
