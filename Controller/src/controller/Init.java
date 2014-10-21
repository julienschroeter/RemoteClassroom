/**
 * 
 */
package controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JOptionPane;



/**
 * @author Julien Schroeter
 * Starts the application and creates database and tables if required
 */
public class Init {
    /* Database */
    private static Connection _c = null;
    private static String dbFile = "data.sqlite";
    private static String dbPath = System.getProperty("user.home") + System.getProperty("file.separator") + "AppData" + System.getProperty("file.separator") + "Roaming" + System.getProperty("file.separator") + ".REMOTECLASSROOMCONTROLLER" + System.getProperty("file.separator");
    
    public static void main(String[] args) {


		Init appInit = new Init();
		appInit.dbConnect();
		
		boolean setup = Setup.initializeDB(_c);
		
		if(setup) {
            if(args.length == 1) {
                // NOT TESTED
                // Firstly import file of IP addresses
                // REMOVE AFTER FIRST RELEASE
                try {
                    BufferedReader in = new BufferedReader(new FileReader(args[0]));
                    String ip;
                    _c.setAutoCommit(false);
                    PreparedStatement psAddIps = _c.prepareStatement("INSERT INTO `IP_ADDR` (`IP_ADDR`) VALUES (?)");
                    while((ip = in.readLine()) != null)
                    {
                        psAddIps.setString(1,ip);
                        psAddIps.addBatch();
                    }
                    psAddIps.executeBatch();
                    _c.commit();
                    _c.setAutoCommit(true);
                    in.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


		    // Start ClassroomContoller
		    new ClassroomController(_c);
		} else {
		    JOptionPane.showMessageDialog(null, "Error while Setup. Exiting the application now.", "Error", JOptionPane.ERROR_MESSAGE);
		    System.exit(0);
		}
    }
    
    /**
     * Creates database file (if required) and connects to database
     */
    public void dbConnect() {
		// Create folders
		new File(dbPath).mkdirs();
		
		
		// Connect to database
		try {
		    Class.forName("org.sqlite.JDBC");
		    _c = DriverManager.getConnection("jdbc:sqlite:" + dbPath + dbFile);
		} catch (ClassNotFoundException e) {
		    e.printStackTrace();
		    JOptionPane.showMessageDialog(null, "Invalid database drivers.", "Database Error", JOptionPane.ERROR_MESSAGE);
		} catch (SQLException e) {
		    e.printStackTrace();
		    JOptionPane.showMessageDialog(null, "Not able to connect to database", "Database Error", JOptionPane.ERROR_MESSAGE);
		}
    }
}
