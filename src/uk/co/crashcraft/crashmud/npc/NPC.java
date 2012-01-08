package uk.co.crashcraft.crashmud.npc;

import uk.co.crashcraft.crashmud.Main;
import uk.co.crashcraft.crashmud.client.GlobalVars;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class NPC implements Runnable {
    
    private HashMap<String, String> npcData = null;
    private GlobalVars globalVars;
    
    public NPC (GlobalVars gVars) {
        globalVars = gVars;
    }
    
    public void run () {
        try {
            getNPCData();
            Thread.sleep(15000);
            while (true) {
                for (Integer i = 0; npcData.size() > i; i++) {
                    String isPathfind = "does";
                    if (!doPathfind(i))
                        isPathfind = isPathfind + " not";
                
                    System.out.print("NPC with ID " + i + " " + isPathfind + " use path finding.");
                }
                getNPCData();
                Thread.sleep(15000);
            }
        } catch (InterruptedException e) {
            // Probably server shutting down...
        }
    }

    public boolean doesExist (Integer id) {
        return npcData.containsKey(id + ":name");
    }
    
    public boolean hasFlag (Integer id, Variables flag) {
        if (npcData.containsKey(id + ":flag")) {
            String[] flags = npcData.get(id + ":flag").split(",");
            for (Integer i = 0; flags.length > i; i++) {
                if (flags[i].equals(flag.toString()))
                    return true;
            }
        }
        return false;
    }

    public boolean doPathfind (Integer id) {
        return hasFlag(id, Variables.NO_PATH);
    }
    
    public void getNPCData () {
        ResultSet rs = Main.mysql.getData("SELECT id, name, description, room, flags FROM `npcs`");
        try {
            while (true) {
                npcData.put(rs.getInt(1) + ":name", rs.getString(2));
                npcData.put(rs.getInt(1) + ":desc", rs.getString(3));
                npcData.put(rs.getInt(1) + ":room", String.valueOf(rs.getInt(4)));
                npcData.put(rs.getInt(1) + ":flag", rs.getString(5));
                //System.out.println("Adding (" + rs.getInt(1) + "): " + rs.getString(2));
                //System.out.println("Desc: " + rs.getString(3));
                rs.next();
            }
        } catch (SQLException e) {
            if (!e.toString().contains("After end of result set")) {
                System.out.println("MySQL Error: " + e.toString());
            }
            npcData.put("0:name", "Bouncer");
            npcData.put("0:desc", "A tall dark cloaked figure looms ominously over you as you look up at him, snarling slightly down at you as if to say you shouldn't be here...");
            npcData.put("0:room", String.valueOf(1));
            npcData.put("0:flag", "NO_PATH,NO_TALK,NO_PASS,BOT_WIZ");
        } finally {
            System.out.println("Found " + npcData.size() / 4 + " NPC(s)");
        }
    }
    
}
