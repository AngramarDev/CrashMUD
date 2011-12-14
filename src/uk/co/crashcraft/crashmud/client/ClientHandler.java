package uk.co.crashcraft.crashmud.client;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;

public class ClientHandler {
    
    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;
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
                    "             |___/                                ");
        out.println("You are connected to the ALPHA server");
        out.print("Please enter your name: ");
        out.flush();
        System.out.println("Client connected: " + s.getInetAddress());
        while (s.isConnected()) {
            String inData = in.readLine();
            out.println("You said: " + inData);
            out.flush();
        }
        try {
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
