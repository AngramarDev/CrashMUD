package uk.co.crashcraft.crashmud.console;

import uk.co.crashcraft.crashmud.Main;
import uk.co.crashcraft.crashmud.client.GlobalVars;
import uk.co.crashcraft.crashmud.ircbot.IRCBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHandler implements Runnable {
    private GlobalVars gVars;
    private IRCBot ircBot;
    
    public ConsoleHandler (GlobalVars globalVars, IRCBot irc) {
        ircBot = irc;
        gVars = globalVars;    
    }
    
    public void run() {
        while (true) {
            try {
                InputStreamReader converter = new InputStreamReader(System.in);
                BufferedReader in = new BufferedReader(converter);

                while (true){
                    String consoleInput = in.readLine();
                    String[] args = consoleInput.split(" ");
                    String cmd = args[0];
                    
                    if (cmd.equals("stop")) {
                        System.out.println("Shutting down server.");
                        gVars.sendServerMessage("The server is going down for halt NOW!");
                        ircBot.writeToChannel("[NOTICE] The server is shutting down!");
                        ircBot.writeToServer("QUIT Shutdown");
                        Main.serverSocket.close();
                        System.exit(1);
                    } else if (cmd.equals("help")) {
                        System.out.println("stop: Saves data and shuts down the servers");
                        System.out.println("help: Shows this command help menu");
                    } else {
                        System.out.println("Unknown Command!");
                        System.out.println("Use 'help' for a list of commands.");
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                break;
            } catch (IOException e) {
            }
        }
    }
}
