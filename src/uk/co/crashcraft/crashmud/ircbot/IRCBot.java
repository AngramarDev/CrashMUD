package uk.co.crashcraft.crashmud.ircbot;

import uk.co.crashcraft.crashmud.Main;
import uk.co.crashcraft.crashmud.client.GlobalVars;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IRCBot implements Runnable {
    private String server   = "irc.bitsjointirc.net";
    private Integer port    = 6667;
    private String username = "Angramar";
    private String password = "kaboom14"; // NickServ pass
    private String channel  = "#angramar";

    private Boolean identified = false;
    
    private BufferedWriter bw;
    private BufferedReader br;

    private GlobalVars gVars;

    public IRCBot (GlobalVars globalVars) {
        gVars = globalVars;
    }

    public void run () {
        try {
            System.out.println("IRC: Connecting...");
            //our socket we're connected with
            Socket irc = new Socket(server, port);
            //out output stream
            bw = new BufferedWriter( new OutputStreamWriter( irc.getOutputStream() ) );
            //our input stream
            br = new BufferedReader( new InputStreamReader( irc.getInputStream() ) );

            writeToServer("USER "+ username +" 8 * :Angramar MUD Server");
            writeToServer("NICK "+ username);

            while (true) {
                try{
                    String line=null;
                    while((line=br.readLine()) != null){
                        //String parsedLine=parseLine(line);
                        //System.out.println("IRC: " + parsedLine);

                        if(line.matches("^PING.*")) {
                            String[] data = line.split(":");
                            bw.write("PONG " + data[1] + "\n");
                            bw.flush();
                        } else {
                            String[] data = line.split(":");
                            
                            if (line.startsWith("ERROR")) {
                                System.out.println("IRC: Fatal Error: " + data[0]);
                                return;
                            }

                            if (data.length > 2) {
                                boolean isOwner = false;
                                String nickname = null;
                                String ident    = null;
                                String host     = null;
   
                                if (data[1].contains("!") && data[1].contains("@") && !data[1].contains("NICK")) {
                                    nickname = data[1].split("!")[0];
                                    ident    = data[1].split(nickname + "!")[1].split("@")[0];
                                    host     = data[1].split(nickname + "!")[1].split("@")[1].split(" ")[0];                                
                                    
                                    if (host.equals("frozy.doridian.de")) {
                                        isOwner = true;    
                                    }
                                }

                                String cmd = data[2];
                            
                                if(cmd.contains("This nickname is registered")) {
                                    if (nickname != null) {
                                        if (nickname.equals("NickServ") && ident.equals("services") && host.equals("bitsjoint.net") && !identified) {
                                            writeToUser("NickServ", "IDENTIFY " + password);
                                            System.out.println("IRC: Identified to NickServ");
                                            writeToServer("JOIN "+ channel);

                                            System.out.println("IRC: Connected!");
                                            writeToChannel("The ALPHA server is now online! Address: frozy.doridian.de Port: " + Main.port);
                                            identified = true;
                                        } else {
                                            System.out.println("IRC: " + nickname + "!" + ident + "@" + host + " attempted to impersonate NickServ!");
                                        }
                                    }
                                } else if(cmd.startsWith("!status")) {
                                    writeToChannel("There are currently " + gVars.connectionCount + " out of " + Main.maxConnections + " max users connected.");
                                } else if (cmd.startsWith("!isadmin")) {
                                    if (isOwner) {
                                        writeToChannel(nickname + ": You are an Admin!");
                                    } else {
                                        writeToChannel(nickname + ": You are NOT an Admin!");
                                    }
                                }
                            }
                        }
                    }
                } catch(IOException e) {
                    System.out.println("IRC: Failed to read from server: "+e.getMessage());
                    break;
                }
            }
            
            irc.close();
            System.out.println("IRC: Closed Connection.");

        } catch (UnknownHostException e) {
            System.out.print("IRC: Unable to connect to " + server + ":" + port);
        } catch (IOException e) {
            System.out.print("IRC: Disconnected! (Remote host closed the connection)");
        }
    }

    public String parseLine(String line){
        Pattern p=Pattern.compile(":\\w*!\\w*@\\w*.* PRIVMSG #\\w* :");
        Matcher m=p.matcher(line);

        if(m.find())
            return line.substring(m.end());
        return line;
    }

    public void writeToChannel(String text){
        try{
            bw.write("PRIVMSG "+ channel +" :"+text+"\n");
            bw.flush();
        }catch(IOException e){
            System.out.println("IRC: Failure when trying to write to server: "+e.getMessage());
        }
    }
    
    public void writeToUser(String user, String text){
        try{
            bw.write("PRIVMSG "+ user +" :"+text+"\n");
            bw.flush();
        }catch(IOException e){
            System.out.println("IRC: Failure when trying to write to server: "+e.getMessage());
        }
    }

    public void writeToServer(String text){
        try{
            bw.write(text+"\n");
            bw.flush();
        }catch(IOException e){
            System.out.println("IRC: Failure when trying to write to server: "+e.getMessage());
        }
    }

}
