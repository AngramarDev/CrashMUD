package uk.co.crashcraft.crashmud.client;

import uk.co.crashcraft.crashmud.Main;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class KeepAlive implements Runnable {
    public Map<Socket, ClientHandler> activeSockets = new HashMap<Socket, ClientHandler>();
    public GlobalVars gVars;
    
    public KeepAlive (GlobalVars globalVars) {
        gVars = globalVars;
    }
    
    public void run () {
        System.out.println("Keep Alive active!");
        while (true) {
            for (Map.Entry<Socket, ClientHandler> client : activeSockets.entrySet()) {
                Socket socket = client.getKey();
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.write(ANSI.WHITE);
                    out.flush();
                } catch (IOException e) {
                    System.out.println("Client dropped: " + socket.getInetAddress() + " (Ping Timeout)");
                    gVars.decCount();
                    if (Main.debug) {
                        e.printStackTrace();
                        return;
                    }
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        if (Main.debug) {
                            e1.printStackTrace();
                            return;
                        }
                    }
                    client.getValue().killThread();
                    activeSockets.remove(socket);
                    ClientHandler clientHandler = client.getValue();
                    if (clientHandler.username != null) {
                        gVars.sendGlobalMessage(clientHandler.username, "has signed off (Ping Timeout).");
                        gVars.delActiveUser(clientHandler.username);
                    }
                }
            }
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                // Must be shutting down?
            }
        }
    }
}
