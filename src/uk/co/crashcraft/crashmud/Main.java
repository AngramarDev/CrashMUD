package uk.co.crashcraft.crashmud;

import uk.co.crashcraft.crashmud.client.ClientHandler;

import java.net.*;
import java.io.*;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

public class Main {

    private static String version = "0.0.1 Alpha";
    public static ServerSocketChannel ssChannel;
    public static ServerSocket serverSocket;
    public static boolean shutdownServer = false;
    public static int port = 4444;

    public static void main(String args[]) {
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
        while (!shutdownServer) {
            try {
                Socket s = serverSocket.accept();
                ClientHandler client = new ClientHandler(s);
            } catch (IOException ex) {
                System.out.println("Exception: " + ex);
                try {
                    serverSocket.close();
                    System.exit(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
