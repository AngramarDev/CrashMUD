package uk.co.crashcraft.crashmud;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import static uk.co.crashcraft.crashmud.Downloader.download;

public class MySQL implements Runnable {
    private String url      = "jdbc:mysql://localhost/crashkittysql2";
    private String user     = "crashkittysql2";
    private String pass     = "meow123";

    private Connection conn     = null;

    public MySQL () {
        try {
            final File file = new File("lib/mysql-connector-java-bin.jar");
            if (!file.exists() || file.length() == 0)
                download(new URL("http://adam-walker.me.uk/mysql-connector-java-bin.jar"), file);
            if (!file.exists() || file.length() == 0)
                throw new FileNotFoundException(file.getAbsolutePath() + file.getName());
            Class.forName ("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println ("Database connection established");
            executeQuery("CREATE TABLE IF NOT EXISTS `users` (`id` int(11) NOT NULL auto_increment, `user` varchar(255), `pass` varchar(255), `email` varchar(255), `wizard` enum('0','1') DEFAULT '0', PRIMARY KEY  (`id`))");
        } catch (Exception e)
        {
            System.err.println ("Cannot connect to database server");
            System.err.println ("MySQL Error: " + e.toString());
            System.exit(-1);
        }
    }

    public int countRows(String query) {
        ResultSet rs = null;
        int rowCount = 0;
        try {
            Statement statement = conn.createStatement();
            // select the number of rows in the table
            rs = statement.executeQuery(query);
            // get the number of rows from the result set
            if (rs.next())
            {
                if (rs.getInt("userCount") > 0)
                {
                    rowCount = 1;
                }
            } else {
                rowCount = 0;
            }
            statement.close();
            return rowCount;
        } catch (SQLException e) {
            e.printStackTrace();
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e1) {
                    System.err.println ("MySQL Error (2): " + e1.toString());
                }
            }
            return rowCount;
        }
    }
    
    public ResultSet getUserData (String user) {
        // SELECT count(*) as userCount FROM `users` WHERE `user` = '" + user + "'
        ResultSet rs = null;
        try {
            Statement statement = conn.createStatement();
            // select the number of rows in the table
            rs = statement.executeQuery("SELECT title, wizard, env_room FROM `users` WHERE `user` = '" + user + "'");
            // get the number of rows from the result set
            if (rs.next())
            {
                //statement.close();
                return rs;
            }
        } catch (SQLException e) {
            System.err.println("MySQL Error: " + e.toString());
        }
        return null;
    }

    public ResultSet getData (String query) {
        // SELECT count(*) as userCount FROM `users` WHERE `user` = '" + user + "'
        ResultSet rs = null;
        try {
            Statement statement = conn.createStatement();
            // select the number of rows in the table
            rs = statement.executeQuery(query);
            // get the number of rows from the result set
            if (rs.next())
            {
                //statement.close();
                return rs;
            }
        } catch (SQLException e) {
            System.err.println("MySQL Error: " + e.toString());
        }
        return null;
    }
    
    public void executeQuery (String query) {
        try {
            Statement statement = conn.createStatement();
            statement.execute(query);
            statement.close();
        } catch (SQLException e) {
            System.err.println("MySQL Error: " + e.toString());
        }
    }

    public void run () {
        while (true) {
            if (conn != null) {
                try {
                    Statement statement = conn.createStatement();
                    statement.executeUpdate("/* ping */ SELECT 1");
                    statement.close();
                } catch (SQLException e) {
                    System.err.println("MySQL Fatal Error: " + e.toString());
                    Main.globalVars.sendServerMessage("The server is going down for halt NOW!");
                    try {
                        Main.serverSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    System.exit(-1);
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
