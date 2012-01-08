package uk.co.crashcraft.crashmud.environment;

import uk.co.crashcraft.crashmud.Main;
import uk.co.crashcraft.crashmud.client.ANSI;
import uk.co.crashcraft.crashmud.client.ClientHandler;
import uk.co.crashcraft.crashmud.client.GlobalVars;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Environment implements Runnable {
    private GlobalVars globalVars;
    private HashMap<String, String> environmentData = new HashMap<String, String>();
    private HashMap<String, String[]> environmentExits = new HashMap<String, String[]>();

    public Environment (GlobalVars gVars) {
        globalVars = gVars;
    }

    public void run () {
        getEnvironmentData();
        getEnvironmentExits();
        while (true) {
            /**try {
                getEnvironmentData();
                Thread.sleep(600000); // 10 minute sleep
            } catch (InterruptedException e) {
                // Meh :3
            }**/
        }
    }
    
    public String getRoomName (Integer id) {
        return environmentData.get(id + ":name");
    }

    public String getRoomDesc (Integer id) {
        return environmentData.get(id + ":desc");
    }
    
    public String getRoomExits (Integer id) {
        String exits = "";
        Boolean first = true;
        for (Map.Entry<String, String[]> exit : environmentExits.entrySet()) {
            if (exit.getKey().startsWith(id + ":")) {
                String[] tempArray = exit.getValue();
                if (!first) {
                    exits = exits + ", ";
                } else {
                    first = false;
                }
                exits = exits + tempArray[1];
            }
        }
        return exits;
    }

    public void getRoom(Integer id, PrintWriter out) {
        getRoom(id, out, false, null);
    }

    public void getRoom(Integer id, PrintWriter out, Boolean joinedRoom, String uname) {
        if (doesRoomExist(id)) {
            out.println(ANSI.BACKGROUND_BLUE + getRoomName(id) + ANSI.BACKGROUND_BLACK);
            out.println(getRoomDesc(id));
            String exits = getRoomExits(id);
            if (exits.isEmpty()) {
                out.println("Exits: None");
            } else {
                out.println("Exits: " + exits);
            }
            Boolean first = true;
            String users = "";
            for (Map.Entry<String, ClientHandler> user : GlobalVars.activeUsers.entrySet()) {
                if (user.getValue().currentRoom.equals(id)) {
                    if (joinedRoom && !user.getValue().username.equals(uname)) {
                        PrintWriter uOut = user.getValue().out;
                        uOut.println(uname + " entered the room.");
                        uOut.flush();
                    }
                    if (!first) {
                        users = users + ", ";
                    } else {
                        first = false;
                    }
                    String username = "";
                    switch (user.getValue().rank) {
                        case IMMORTAL:
                            username = ANSI.BLUE + user.getValue().username + ANSI.WHITE;
                            break;

                        case WIZARD:
                            username = ANSI.YELLOW + user.getValue().username + ANSI.WHITE;
                            break;

                        case GOD:
                            username = ANSI.RED + user.getValue().username + ANSI.WHITE;
                            break;

                        default:
                            username = user.getValue().username;
                            break;
                    }
                    users = users + username;
                }
            }
            if (users.isEmpty()) {
                out.println("It's too dark to notice anyone else in here...");
            } else {
                out.println("Users: " + users);
            }
            out.flush();
        }
    }
    
    public Integer isExitValid (Integer currentRoom, String trigger, String username, PrintWriter out) {
        for (Map.Entry<String, String[]> exit : environmentExits.entrySet()) {
            if (exit.getKey().startsWith(currentRoom + ":")) {
                String[] tempArray = exit.getValue();
                if (tempArray[1].equals(trigger)) {
                    if (Integer.parseInt(tempArray[2]) == currentRoom) {
                        out.println("You wander through the exit but somehow end up back in the SAME room you tried to leave! Maybe you should go a different way...");
                        out.flush();
                    } else {
                        for (Map.Entry<String, ClientHandler> user : GlobalVars.activeUsers.entrySet()) {
                            if (user.getValue().currentRoom.equals(currentRoom)) {
                                if (!user.getValue().username.equals(username)) {
                                    PrintWriter uOut = user.getValue().out;
                                    uOut.println(username + " left the room.");
                                    uOut.flush();
                                }
                            }
                        }
                        changeRoom(Integer.parseInt(tempArray[2]), username, out);
                    }
                    return Integer.parseInt(tempArray[2]);
                }
            }
        }
        return 0;
    }

    public void changeRoom(Integer id, String username, PrintWriter out) {
        Main.mysql.executeQuery("UPDATE `users` SET `env_room` = '" + id + "' WHERE `user` = '" + username + "'");
        getRoom(id, out, true, username);
    }

    public boolean doesRoomExist (Integer id) {
        return environmentData.get(id + ":name") != null;
    }
    
    public void getEnvironmentData () {
        ResultSet rs = Main.mysql.getData("SELECT id, name, description FROM `environments`");
        try {
            while (true) {
                environmentData.put(rs.getInt(1) + ":name", rs.getString(2));
                environmentData.put(rs.getInt(1) + ":desc", rs.getString(3));
                //System.out.println("Adding (" + rs.getInt(1) + "): " + rs.getString(2));
                //System.out.println("Desc: " + rs.getString(3));
                rs.next();
            }
        } catch (SQLException e) {
            if (!e.toString().contains("After end of result set")) {
                System.err.println("MySQL Error: " + e.toString());
            }
        } finally {
            System.out.println("Found " + environmentData.size() / 2 + " environment(s)");
        }
    }

    public void getEnvironmentExits () {
        ResultSet rs = Main.mysql.getData("SELECT * FROM `environments_exits`");
        try {
            while (true) {
                String[] tempString = new String[6];
                tempString[0] = rs.getString(2); // Name
                tempString[1] = rs.getString(3); // Trigger
                tempString[2] = String.valueOf(rs.getInt(5)); // Links to
                tempString[3] = rs.getString(6); // State
                tempString[4] = rs.getString(7); // Creator
                tempString[5] = rs.getString(8); // Password
                environmentExits.put(rs.getInt(4) + ":" + rs.getInt(1), tempString); // Room ID
                rs.next();
            }
        } catch (SQLException e) {
            if (!e.toString().contains("After end of result set")) {
                System.err.println("MySQL Error: " + e.toString());
            }
        } catch (NullPointerException e) {
            System.err.println("MySQL Error: " + e.toString());
        } finally {
            System.out.println("Found " + environmentExits.size() + " exit(s)");
        }
    }
}
