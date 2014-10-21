package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Julien Schroeter
 */
public class Configuration {

    public static String getProperty(Connection c, String property) {
        try {
            PreparedStatement psGetProperty = c.prepareStatement("SELECT * FROM `SYS_PREFERENCES` WHERE `KEY`=?");
            psGetProperty.setString(1, property);
            ResultSet rsPropertyVal = psGetProperty.executeQuery();

            String val = "";
            while(rsPropertyVal.next()) {
                val = rsPropertyVal.getString("VALUE");
            }

            return val;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
