package uk.co.crashcraft.crashmud.client;

import uk.co.crashcraft.crashmud.Main;
import uk.co.crashcraft.crashmud.MySQL;
import uk.co.crashcraft.crashmud.Ranks;
import uk.co.crashcraft.crashmud.environment.Environment;
import uk.co.crashcraft.crashmud.environment.Variables;
import uk.co.crashcraft.crashmud.ircbot.IRCBot;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.ResultSet;
import java.sql.SQLException;

import static uk.co.crashcraft.crashmud.Ranks.IMMORTAL;

public class ClientHandler implements Runnable {
    
    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;
    public String username = null;
    public GlobalVars gVars;
    public KeepAlive keepAlive;
    public Environment environment;
    public IRCBot ircBot;
    public Ranks rank = Ranks.MORTAL;
    public String title = null;
    public Integer currentRoom = 0;
    public boolean killed = false;
    public boolean banned = false;
    private boolean atLogin = false;
    private volatile Thread thisThread;

    public ClientHandler (GlobalVars vars, KeepAlive kAlive, IRCBot irc, Environment enviro, Socket s) {
        environment = enviro;
        ircBot = irc;
        gVars = vars;
        keepAlive = kAlive;
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
            if (gVars.connectionCount == Main.maxConnections) {
                out.println("Sorry, we currently have too many connections! Please try again later.");
                out.flush();
                System.out.println("Client declined: " + socket.getInetAddress());
                socket.close();
                thisThread = null;
                return;
            }
            keepAlive.activeSockets.put(socket, this);
            gVars.incCount();
            out.println("You are connected to the ALPHA server");
            out.println("If you're registered please use    ':connect <name> <pass>'");
            out.println("If you want to join as a guest use ':connect guest'\n");
            out.println("Use command 'QUIT' to exit (In UPPER case!)\n");
            out.flush();
            System.out.println("Client connected: " + socket.getInetAddress());
            while (socket.isConnected()) {
                String inData = in.readLine();
                String[] cmd = inData.split(" ");
                Boolean isRoom = false;
                if (username != null) {
                    Integer room = environment.isExitValid(currentRoom, cmd[0], username, out);
                    if (room != 0) {
                        isRoom = true;
                        currentRoom = room;
                    }
                }
                if (isRoom) {
                    // Ignore parsing the rest
                } else if (cmd[0].equals("QUIT")) {
                    gVars.decCount();
                    out.println("Goodbye!");
                    out.flush();
                    if (username != null) {
                        gVars.sendGlobalMessage(username, "has signed off (Quitting).");
                        gVars.delActiveUser(username);
                    }
                    System.out.println("Client disconnecting: " + socket.getInetAddress());
                    socket.close();
                    thisThread = null;
                    return;
                } else if (cmd[0].toLowerCase().equals("look")) {
                    if (username == null) {
                        out.println("You must be logged in to perform this action!");
                        out.flush();
                    } else if (environment.doesRoomExist(currentRoom)) {
                        environment.getRoom(currentRoom, out);
                    }
                } else if (cmd[0].equals(":connect")) {
                    if (username != null) {
                        out.println("You are already logged in as " + username);
                    } else if (cmd.length != 3) {
                        out.println("Invalid Parameters! Syntax - :connect <character> <password>");
                        out.flush();
                    } else if (gVars.checkActiveUser(cmd[1])) {
                        out.println("This username is already active!");
                        out.flush();
                    } else if (Main.mysql.countRows("SELECT count(*) as userCount FROM `users` WHERE `user` = '" + cmd[1] + "' AND `banned` = '1'") != 0) {
                        out.println(ANSI.BACKGROUND_RED + "This account has been banned and may not login!" + ANSI.BACKGROUND_BLACK);
                        out.flush();
                    } else {
                        String user = cmd[1];
                        String pass = Main.md5Encrypt(cmd[2]);
                        if (Main.mysql.countRows("SELECT count(*) as userCount FROM `users` WHERE `user` = '" + user + "'") == 0) {
                            out.println("Either that player does not exist, or has a different password.");
                        } else {
                            if (Main.mysql.countRows("SELECT count(*) as userCount FROM `users` WHERE `user` = '" + user + "' AND `pass` = '" + pass + "'") == 0) {
                                out.println("Either that player does not exist, or has a different password.");
                            } else {
                                out.println(ANSI.BACKGROUND_BLUE + "Welcome, " + user + "!" + ANSI.BACKGROUND_BLACK);
                                username = user;
                                gVars.addActiveUser(user, this, socket);
                                Main.mysql.countRows("SELECT count(*) as userCount FROM `users` WHERE `user` = '" + user + "' AND `wizard` = '1'");
                                //if ( == 1) {
                                ResultSet userData = Main.mysql.getUserData(user);
                                title = userData.getString(1);
                                String rankName = "";
                                switch (userData.getInt(2)) {
                                    case 1:
                                        rank = Ranks.IMMORTAL;
                                        rankName = "Immortal!";
                                        break;
                                    
                                    case 2:
                                        rank = Ranks.WIZARD;
                                        rankName = "a Wizard!";
                                        break;
                                    
                                    case 3:
                                        rank = Ranks.GOD;
                                        rankName = "a God!";
                                        break;
                                }
                                if (rank.getCode() >= Ranks.IMMORTAL.getCode()) {
                                    out.println(ANSI.BACKGROUND_GREEN + ANSI.WHITE + "You are " + rankName + ANSI.BACKGROUND_BLACK);
                                }
                                currentRoom = userData.getInt(3);
                                environment.getRoom(currentRoom, out, true, username);
                            }
                        }
                        out.flush();
                    }
                } else if (cmd[0].equals(":teleport")) {
                    if (username == null) {
                        out.println("You must be logged in to perform this action!");
                        out.flush();
                    } else if (rank.getCode() >= Ranks.IMMORTAL.getCode()) {
                        if (cmd.length <= 1) {
                            out.println(ANSI.BACKGROUND_RED + "Invalid Parameters! Syntax - :teleport <zone>" + ANSI.BACKGROUND_BLACK);
                            out.flush();
                        } else {
                            try {
                                Integer teleportTo = Integer.parseInt(cmd[1]);
                                if (environment.doesRoomExist(teleportTo)) {
                                    currentRoom = teleportTo;
                                    environment.changeRoom(teleportTo, username, out);
                                } else {
                                    out.println(ANSI.BACKGROUND_RED + "Zone with ID: " + teleportTo + " does not exist." + ANSI.BACKGROUND_BLACK);
                                    out.flush();
                                }
                            } catch (NumberFormatException nFE) {
                                out.println(ANSI.BACKGROUND_RED + "Invalid Parameters! Syntax - :teleport <zone>" + ANSI.BACKGROUND_BLACK);
                                out.flush();
                            }
                        }
                    } else {
                        out.println(ANSI.BACKGROUND_RED + "You do not have permission to execute this command. (WIZ_STOP)" + ANSI.BACKGROUND_BLACK);
                        out.flush();
                    }
                } else if (cmd[0].equals(":stop")) {
                    if (username == null) {
                        out.println("You must be logged in to perform this action!");
                        out.flush();
                    } else if (rank.getCode() == Ranks.GOD.getCode()) {
                        gVars.sendServerMessage("The server is going down for halt NOW!");
                        ircBot.writeToChannel("[NOTICE] The server is shutting down!");
                        ircBot.writeToServer("QUIT Shutdown");
                        Main.serverSocket.close();
                        System.exit(1);
                    } else {
                        out.println(ANSI.BACKGROUND_RED + "You do not have permission to execute this command. (WIZ_STOP)" + ANSI.BACKGROUND_BLACK);
                        out.flush();
                    }
                } else if (cmd[0].equals(":update")) {
                    if (username == null) {
                        out.println("You must be logged in to perform this action!");
                        out.flush();
                    } else if (cmd.length <= 1) {
                        out.println("Invalid Parameters! Syntax - :update <command>");
                        out.flush();
                    } else if (rank.getCode() >= Ranks.WIZARD.getCode()) {
                        if (cmd[1].equals("environments")) {
                            environment.getEnvironmentData();
                            out.println(ANSI.GREEN + "Environments updated." + ANSI.WHITE);
                            out.flush();
                        } else if (cmd[1].equals("exits")) {
                            environment.getEnvironmentExits();
                            out.println(ANSI.GREEN + "Environment exits updated." + ANSI.WHITE);
                            out.flush();
                        } else {
                            out.println("Invalid Command! Syntax - :update <command>");
                            out.flush();
                        }
                    } else {
                        out.println(ANSI.BACKGROUND_RED + "You do not have permission to execute this command. (WIZ_STOP)" + ANSI.BACKGROUND_BLACK);
                        out.flush();
                    }
                } else if (cmd[0].equals(":kick")) {
                    if (username == null) {
                        out.println("You must be logged in to perform this action!");
                        out.flush();
                    } else if (cmd.length != 2) {
                        out.println("Invalid Parameters! Syntax - :kick <player>");
                        out.flush();
                    } else if (rank.getCode() <= Ranks.WIZARD.getCode()) {
                        out.println(ANSI.BACKGROUND_RED + "You do not have permission to execute this command. (WIZ_KICK)" + ANSI.BACKGROUND_BLACK);
                        out.flush();
                    } else {
                        if (!gVars.checkActiveUser(cmd[1])) {
                            out.println("That user is not connected!");
                        } else if (!gVars.isAuthorised(username, cmd[1])) {
                            out.println(ANSI.BACKGROUND_RED + "You do not have permission to execute this command against this user. (WIZ_KICK)" + ANSI.BACKGROUND_BLACK);
                        } else {
                            out.println("User disconnected.");
                            gVars.kickActiveUser(cmd[1], keepAlive);
                        }
                        out.flush();
                    }
                } else if (cmd[0].equals(":help")) {
                    out.println("Basic Commands:");
                    out.println("Chatting: '" + ANSI.CYAN + "@<message>" + ANSI.WHITE + "'");
                    out.println("Logging off: '" + ANSI.CYAN + "QUIT" + ANSI.WHITE + "' (all caps)");
                    if (rank.getCode() >= IMMORTAL.getCode()) {
                        out.println("Wizard Commands:");
                        out.println("Shun player: '" + ANSI.RED + ":shun <player>" + ANSI.WHITE + "'");
                        out.println("Teleport: '" + ANSI.RED + ":teleport <zone>" + ANSI.WHITE + "'");
                        if (rank.getCode() >= Ranks.WIZARD.getCode()) {
                            out.println("Update environments: '" + ANSI.RED + ":update environments" + ANSI.WHITE + "'");
                            out.println("Update exits: '" + ANSI.RED + ":update exits" + ANSI.WHITE + "'");
                            out.println("Kick player: '" + ANSI.RED + ":kick <player>" + ANSI.WHITE + "'");
                            if (rank.getCode() == Ranks.GOD.getCode()) {
                                out.println("Stop the server: '" + ANSI.RED + ":stop" + ANSI.WHITE + "'");
                            }
                        }
                    }
                    out.flush();
                } else if (cmd[0].startsWith("@")) {
                    // Say Command
                    if (username == null) {
                        out.println("You must be logged in to perform this action!");
                        out.flush();
                    } else {
                        String user = username;
                        switch (rank) {
                            case IMMORTAL:
                                user = ANSI.BLUE + "[GM] " + user + ANSI.WHITE;
                                break;
                            
                            case WIZARD:
                                user = ANSI.YELLOW + "[WIZ] " + user + ANSI.WHITE;
                                break;
                            
                            case GOD:
                                user = ANSI.RED + "[DEV] " + user + ANSI.WHITE;
                                break;
                        }
                        gVars.sendPublicMessage(user, inData.substring(1), currentRoom);
                    }
                } else {
                    if (!inData.isEmpty()) {
                        out.println(ANSI.BACKGROUND_RED + "I don't understand '" + cmd[0] + "'. Type :help for a list of commands." + ANSI.BACKGROUND_BLACK);
                        out.flush();
                    }
                }
            }
        } catch (SocketTimeoutException e) {
            if (!socket.isConnected()) {
                if (killed)
                    return;

                System.out.println("Client dropped: " + socket.getInetAddress());
                gVars.decCount();
                if (Main.debug) {
                    e.printStackTrace();
                }
                if (username != null) {
                    gVars.sendGlobalMessage(username, "has signed off (Connection Dropped).");
                    gVars.delActiveUser(username);
                }
                try {
                    socket.close();
                } catch (IOException e1) {
                    gVars.decCount();
                    if (Main.debug) {
                        e1.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            if (killed)
                return;

            System.out.println("Client dropped: " + socket.getInetAddress());
            gVars.decCount();
            if (Main.debug) {
                e.printStackTrace();
            }
            if (username != null) {
                gVars.sendGlobalMessage(username, "has signed off (Connection Dropped).");
                gVars.delActiveUser(username);
            }
            try {
                socket.close();
            } catch (IOException e1) {
                gVars.decCount();
                if (Main.debug) {
                    e1.printStackTrace();
                }
            }
        } catch (NullPointerException e) {
            if (killed)
                return;

            gVars.decCount();
            if (Main.debug) {
                e.printStackTrace();
            }
            if (username != null) {
                gVars.sendGlobalMessage(username, "has signed off (Connection Dropped).");
                gVars.delActiveUser(username);
            }
            System.out.println("Client dropped: " + socket.getInetAddress());
        } catch (SQLException e) {
            if (killed)
                return;

            gVars.decCount();
            if (Main.debug) {
                e.printStackTrace();
            }
            if (username != null) {
                gVars.sendGlobalMessage(username, "has signed off (Connection Dropped).");
                gVars.delActiveUser(username);
            }
            System.out.println("Client dropped: " + socket.getInetAddress());
        }
    }

    public void killThread () {
        thisThread = null;
    }
}
