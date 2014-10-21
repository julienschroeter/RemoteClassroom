/**
 * 
 */
package controller;

import java.sql.*;

import javax.swing.JOptionPane;


/**
 * Checks database validity and creates required tables
 * @author Julien Schroeter
 */
public class Setup {
    private static Connection _c;
    private static int _nTbls = 0;
    private static int _nTblsExpected = 3;
    
    private static String[] _tblsCreateQueries = {
        "CREATE TABLE 'IP_ADDR' ('ID' INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , 'IP_ADDR' VARCHAR NOT NULL , 'NOTES' VARCHAR, 'STATUS' BOOL DEFAULT 1)",
        "CREATE TABLE 'COMMANDS' ('ID' INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , 'IDENTIFIER' VARCHAR , 'LABEL' VARCHAR NOT NULL , 'COMMAND' VARCHAR NOT NULL, 'REQUIRED' BOOL DEFAULT 0)",
        "CREATE TABLE 'SYS_PREFERENCES' ('KEY' VARCHAR NOT NULL  UNIQUE , 'VALUE' VARCHAR)",
        "INSERT INTO `COMMANDS` (`LABEL`,`COMMAND`,`IDENTIFIER`,`REQUIRED`) VALUES ('Verfügbarkeit testen','sys://hello','PING',1)",
        "INSERT INTO `COMMANDS` (`LABEL`,`COMMAND`) VALUES ('E:\\ formatieren','cmd /c start cmd.exe /K \"del e:\\* /s /q && exit \"')",
        "INSERT INTO `COMMANDS` (`LABEL`,`COMMAND`,`IDENTIFIER`,`REQUIRED`) VALUES ('Clients nach 2 Minuten herunterfahren', 'cmd /c start cmd.exe /K \"shutdown /t 120 /s && exit \"','SHUTDOWN',1)",
        "INSERT INTO `COMMANDS` (`LABEL`,`COMMAND`,`IDENTIFIER`,`REQUIRED`) VALUES ('Clientanwendung beenden', 'sys://quit','QUIT',1)",
        "INSERT INTO `SYS_PREFERENCES` (`KEY`,`VALUE`) VALUES ('PORT_CMD', 6868)",
        "INSERT INTO `SYS_PREFERENCES` (`KEY`,`VALUE`) VALUES ('PORT_FILE', 6869)"
    };
    
    /**
     * Checks database validity and creates required tables
     * @param c Connection
     * @return Table validity
     */
    public static boolean initializeDB(Connection c) {
		_c = c;
		
		try {
		    Statement sNumTbls = _c.createStatement();
		    sNumTbls.execute("SELECT COUNT(*) FROM `sqlite_master` WHERE `type`='table' AND `name`!='sqlite_sequence'");
		    ResultSet rsNumTbls = sNumTbls.getResultSet();
		    
		    while(rsNumTbls.next()) {
		    	_nTbls = rsNumTbls.getInt(1);
		    }
		    
		    if(_nTbls == 0) {
				// Create Tables
				_c.setAutoCommit(false);
				Statement createTbls = _c.createStatement();
				for(String sql : _tblsCreateQueries) {
				    createTbls.addBatch(sql);
				}
				createTbls.executeBatch();
				_c.commit();
				_c.setAutoCommit(true);
				
				return true;
		    } else if(_nTbls != _nTblsExpected) {
				// Broken database
				JOptionPane.showMessageDialog(null, "Broken database. Try to reinstall the application.", "Broken database", JOptionPane.ERROR_MESSAGE);
				return false;
		    } else if(_nTbls == _nTblsExpected) {
				// Database already setted up
				return true;
		    } else {
				// Unknown error
				JOptionPane.showMessageDialog(null, "Undefined database error.", "Something went wrong", JOptionPane.ERROR_MESSAGE);
		    }
		}
		catch (SQLException e) {
		    e.printStackTrace();
		}
		
		return false;
    }  
}
