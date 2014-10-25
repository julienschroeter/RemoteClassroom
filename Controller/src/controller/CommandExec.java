package controller;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import controller.Datatypes.Command;
import controller.Datatypes.Action;

/**
 * Provides functionality to send a command to clients
 * @author Julien Schroeter, TimoNeon
 *
 */
public class CommandExec {
    private Connection _c;
    
    private CommandExec _cmdExec = this;
    
    private JFrame _frame;
    private DefaultListModel<Command> _cmdListModel;
    private JList<Command> _cmdList;


    private final JTextArea _msgField = new JTextArea();
    final JProgressBar _progressBar = new JProgressBar();

    
    /**
     * Displays a dialog to select the command to be sent
     * @param c Connection
     */
    public CommandExec(Connection c) {
		this._c = c;
    }

    public void showSelection() {
		_frame = new JFrame("Befehle verwalten");
		_frame.setLayout(new BorderLayout());
		
		_cmdListModel = new DefaultListModel<Command>();
		_cmdList = new JList<Command>(_cmdListModel);
		_cmdList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane commandsListPane = new JScrollPane(_cmdList);
		commandsListPane.setPreferredSize(new Dimension(300,200));
		_frame.add(commandsListPane, BorderLayout.WEST);
		
		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
		JButton btnExec = new JButton("Ausf\u00fchren");
        btnExec.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
                if(_cmdList.getSelectedIndex() == -1)
                    JOptionPane.showMessageDialog(_frame, "Einen Befehl zum Ausf\u00fchren ausw\u00e4hlen.", "Fehler", JOptionPane.ERROR_MESSAGE);
                else {
                    displayMessage("Befehl ausf\u00fchren: \n" + _cmdList.getSelectedValue().getCommand() + "\n");
                    _frame.dispose();
                    _cmdExec.executeCommandWindow(new Action() {
                        @Override
                        public void action(String ip) throws Exception {
                            executeCommand(ip, _cmdList.getSelectedValue());
                        }
                    });
                }
            }
		});
		btnPanel.add(btnExec);
		
		JButton btnInsertCmd = new JButton("Befehl eingeben");
		btnInsertCmd.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
				String cmdStr = JOptionPane.showInputDialog(_frame, "Befehl eingeben: ", "cmd /c start cmd.exe /K \" &&exit\"");
				if(cmdStr.length() < 1)
				    JOptionPane.showMessageDialog(_frame, "Einen Befehl zum Ausf\u00fchren eingeben.", "Fehler", JOptionPane.ERROR_MESSAGE);
				else {
				    final Command cmd = new Command();
				    cmd.setCommand(cmdStr);
                    displayMessage("Befehl ausf\u00fchren: \n" + cmd.getCommand() + "\n");
                    _frame.dispose();
                    _cmdExec.executeCommandWindow(new Action() {
                        @Override
                        public void action(String ip) throws Exception {
                            executeCommand(ip, cmd);
                        }
                    });
				}
		    }
		});
		btnPanel.add(btnInsertCmd);
		
		_frame.add(btnPanel);
		
		// Initialize Commands
		this.initializeData();
		
		_frame.pack();
		_frame.setResizable(false);
		_frame.setVisible(true);
    }
    
    /**
     * Loads available commands and displays them in JList
     */
    public void initializeData() {
		_cmdListModel.setSize(0);
		
		try {
		    Statement stGetCommands = _c.createStatement();
		    ResultSet rsGetCommands = stGetCommands.executeQuery("SELECT * FROM `COMMANDS`");
		    while(rsGetCommands.next()) {
				_cmdListModel.addElement(new Command(
					rsGetCommands.getInt("ID"),
					rsGetCommands.getString("LABEL"),
					rsGetCommands.getString("COMMAND"),
                    rsGetCommands.getBoolean("REQUIRED")
				));
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}
    }
    
    /**
     * Returns status and error messages
     * @param action Method which holds commands and files to send to clients
     * @see controller.Datatypes.Action
     */
    public void executeCommandWindow(final Action action) {
		final JFrame execFrame = new JFrame("Befehl ausf\u00fchren ...");
		execFrame.setLayout(new BoxLayout(execFrame.getContentPane(), BoxLayout.Y_AXIS));

		_msgField.setRows(4);
		_msgField.setColumns(30);
		_msgField.setEditable(false);
		execFrame.add(_msgField);
		
		JPanel controlsPanel = new JPanel(new FlowLayout());
		final JButton ctrlBtn = new JButton("Vorgang starten");
		controlsPanel.add(ctrlBtn);

		controlsPanel.add(_progressBar);
		
		execFrame.add(controlsPanel);
		execFrame.pack();
		execFrame.setVisible(true);
		
		final Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
                ctrlBtn.setEnabled(false);
                List<String> ips = new ArrayList<String>();

                try {
                    // Get ip addresses
                    Statement stGetIps = _c.createStatement();
                    ResultSet rsGetIps = stGetIps.executeQuery("SELECT `IP_ADDR` FROM `IP_ADDR` WHERE `STATUS`=1");

                    while(rsGetIps.next())
                        ips.add(rsGetIps.getString("IP_ADDR"));

                    _progressBar.setMaximum(ips.size());

                    for(String ip : ips) {
                        try {
                            action.action(ip);
                        } catch (Exception ex) {
                            displayMessage(ip + " - Nicht gefunden");
                            ex.printStackTrace();
                        } finally {
                            _progressBar.setValue(_progressBar.getValue() + 1);
                            execFrame.pack();
                        }
                    }
                } catch (SQLException e1) {
                    e1.printStackTrace();
                } finally {
                    ctrlBtn.setText("Dialog schlie\u00dfen");
                    ctrlBtn.setEnabled(true);
                    ctrlBtn.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            execFrame.dispose();
                        }
                    });
                }
            }
		});
		
		ctrlBtn.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	th.start();
		    }
		});
    }

    /**
     * Sends command to execute to client
     * @param ip String IP address
     * @param cmd Command to execute
     * @throws Exception
     * @see controller.Datatypes.Command
     */
    public void executeCommand(String ip, Command cmd) throws Exception {
        Socket sender = new Socket(ip, Integer.parseInt(Configuration.getProperty(_c, "PORT_CMD")));
        sender.setKeepAlive(true);
        PrintWriter out = new PrintWriter(sender.getOutputStream(), true);
        out.println(cmd.getCommand());
        out.flush();

        // Getting feedback
        BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));
        if( ! in.readLine().equals("ok")) {
            displayMessage(ip + " - Ung\u00fcltige Antwort");
        }
        out.close();
        in.close();
        sender.close();
    }

    /**
     * Sends file to client.
     * @param ip String IP address
     * @param filepath String Path to local file
     * @throws Exception
     */
    public void sendFile(String ip, String filepath) throws Exception {
        ServerSocket ssock = new ServerSocket(Integer.parseInt(Configuration.getProperty(_c, "PORT_FILE")));
        Socket sock = ssock.accept();
        PrintStream sockOut = new PrintStream(sock.getOutputStream());

        FileInputStream fileIn = new FileInputStream(filepath);
        byte[] b = new byte[1];

        while((fileIn.read(b)) != -1) {
            sockOut.write(b);
            sockOut.flush();
        }

        sockOut.close();
        sock.close();
        ssock.close();
        fileIn.close();
    }

    public void displayMessage(String message) {
        _msgField.append(message + "\n");
    }
}
