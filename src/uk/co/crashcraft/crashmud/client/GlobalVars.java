package uk.co.crashcraft.crashmud.client;

import uk.co.crashcraft.crashmud.Main;
import uk.co.crashcraft.crashmud.Ranks;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class GlobalVars {
    public static Map<String, ClientHandler> activeUsers = new HashMap<String, ClientHandler>();
    public static Map<String, Socket> activeSockets = new HashMap<String, Socket>();
    public int connectionCount = 0;
    public String testString = "this is a test";

    public void GlobalVars () {
        System.out.print("Initiated Global Variable Handler");
    }

    public void incCount () {
        connectionCount++;
    }

    public void decCount () {
        connectionCount--;
    }
    
    public void addActiveUser (String user, ClientHandler clientHandler, Socket socket) {
        activeUsers.put(user, clientHandler);
        activeSockets.put(user, socket);
    }

    public void delActiveUser (String user) {
        activeUsers.remove(user);
        activeSockets.remove(user);
    }
    
    public boolean checkActiveUser (String user) {
        return activeUsers.containsKey(user);
    }

    public boolean isAuthorised (String user, String target) {
        int userRank = activeUsers.get(user).rank.getCode();

        if (!checkActiveUser(target)) {
            return false;
        }

        int targetRank = activeUsers.get(target).rank.getCode();
        if (userRank > targetRank)
            return true;

        return false;
    }
    
    public void kickActiveUser (String user, KeepAlive keepAlive) {
        ClientHandler client = activeUsers.get(user);
        Socket socket = activeSockets.get(user);
        try {
            PrintWriter out = client.out;
            out.println(ANSI.BACKGROUND_RED +  "You have been forcefully disconnected!");
            decCount();
            keepAlive.activeSockets.remove(socket);
            delActiveUser(user);
            System.out.println("Client killed: " + socket.getInetAddress());
            sendGlobalMessage(user, "has signed off (Kicked by Wizard).");
            client.killed = true;
            socket.close();
        } catch (IOException e) {
            
        }

    }

    public void sendPublicMessage (String sender, String message, Integer currentRoom) {
        for (Map.Entry<String, ClientHandler> user : activeUsers.entrySet()) {
            if (user.getValue().currentRoom.equals(currentRoom)) {
                PrintWriter out = user.getValue().out;
                out.println(sender + ": " + message);
                out.flush();
            }
        }
    }
    
    public void sendGlobalMessage (String sender, String message) {
        for (Map.Entry<String, Socket> user : activeSockets.entrySet()) {
            if (user.getKey() != sender) {
                Socket socket = user.getValue();
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("*** " + sender + " " + message);
                } catch (IOException e) {
                    
                }
            }
        }
    }

    public void sendServerMessage (String message) {
        for (Map.Entry<String, Socket> user : activeSockets.entrySet()) {
            Socket socket = user.getValue();
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("*** [SERVER] " + message);
            } catch (IOException e) {
            }
        }
    }
}
