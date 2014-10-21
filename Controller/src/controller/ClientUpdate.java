package controller;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import controller.Datatypes.Action;
import controller.Datatypes.Command;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

/**
 * @author Julien Schroeter
 */
public class ClientUpdate {
    private Connection _c;


    public ClientUpdate(Connection c) {
        this._c = c;
        final CommandExec cmdSender = new CommandExec(_c);

        final JFileChooser fileCooser = new JFileChooser();
        fileCooser.setMultiSelectionEnabled(false);
        fileCooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().toLowerCase().endsWith(".jar");
            }

            @Override
            public String getDescription() {
                return "Java archive (*.jar)";
            }
        });

        int result = fileCooser.showOpenDialog(null);

        if(result == JFileChooser.APPROVE_OPTION) {
            cmdSender.executeCommandWindow(new Action(){
                @Override
                public void action(String ip) {
                    try {
                        cmdSender.executeCommand(ip, new Command("sys://update"));
                        cmdSender.sendFile(ip, fileCooser.getSelectedFile().getAbsolutePath());

                        // Shutdown command to restart client application
                        Statement psGetShutdownCmd = _c.createStatement();
                        ResultSet rsGetShutdownCmd = psGetShutdownCmd.executeQuery("SELECT * FROM `COMMANDS` WHERE `IDENTIFIER`='SHUTDOWN'");
                        String cmd = "";
                        while(rsGetShutdownCmd.next()) {
                            cmd = rsGetShutdownCmd.getString("COMMAND");
                        }

                        cmdSender.executeCommand(ip, new Command(cmd));

                        cmdSender.displayMessage("----- " + ip + " wurde erfolgreich aktualisiert -----");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }
}