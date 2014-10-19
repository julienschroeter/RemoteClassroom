package controller;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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


/**
 * 
 */

/**
 * Provides functionality to send a command to clients
 * @author Julien Schroeter
 *
 */
public class CommandExec {
    private Connection _c;
    
    private CommandExec _cmdExec = this;
    
    private JFrame _frame;
    private DefaultListModel<Command> _cmdListModel;
    private JList<Command> _cmdList;
    private JButton _btnExec, _btnInsertCmd;
    
    /**
     * Displays a dialog to select the command to be sent
     * @param c Connection
     */
    public CommandExec(Connection c) {
		this._c = c;
		
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
		_btnExec = new JButton("Ausf\u00fchren");
		_btnExec.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
			if(_cmdList.getSelectedIndex() == -1)
			    JOptionPane.showMessageDialog(_frame, "Einen Befehl zum Ausf\u00fchren ausw\u00e4hlen.", "Fehler", JOptionPane.ERROR_MESSAGE);
			else
			    _cmdExec.executeCommand(_cmdList.getSelectedValue());
		    }
		});
		btnPanel.add(_btnExec);
		
		_btnInsertCmd = new JButton("Befehl eingeben");
		_btnInsertCmd.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
				String cmdStr = JOptionPane.showInputDialog("Befehl eingeben: ");
				if(cmdStr.length() < 1)
				    JOptionPane.showMessageDialog(_frame, "Einen Befehl zum Ausf\u00fchren eingeben.", "Fehler", JOptionPane.ERROR_MESSAGE);
				else {
				    Command cmd = new Command();
				    cmd.setCommand(cmdStr);
				    _cmdExec.executeCommand(cmd);
				}
		    }
		});
		btnPanel.add(_btnInsertCmd);
		
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
					rsGetCommands.getString("COMMAND")
				));
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}
    }
    
    /**
     * Sends command and returns status and error messages
     * @param cmd Command
     * @see controller.Datatypes.Command
     */
    private void executeCommand(final Command cmd) {
		final int done = 0;
		_frame.dispose();
		
		final JFrame execFrame = new JFrame("Befehl ausf\u00fchren ...");
		execFrame.setLayout(new BoxLayout(execFrame.getContentPane(), BoxLayout.Y_AXIS));
		
		final JTextArea msgField = new JTextArea("Befehl ausf\u00fchren: \n" + cmd.getCommand() + "\n");
		msgField.setRows(4);
		msgField.setColumns(30);
		msgField.setEditable(false);
		execFrame.add(msgField);
		
		JPanel controlsPanel = new JPanel(new FlowLayout());
		final JButton ctrlBtn = new JButton("Vorgang starten");
		controlsPanel.add(ctrlBtn);
		
		final JProgressBar progressBar = new JProgressBar();
		controlsPanel.add(progressBar);
		
		execFrame.add(controlsPanel);
		execFrame.pack();
		execFrame.setVisible(true);
		
		final Thread th = new Thread(new Runnable() {
	    		@Override
	    		public void run() {
	    		    ctrlBtn.setEnabled(false);
	    		    Socket sender = null;
	    		    List<String> ips = new ArrayList<String>();
	    		    
	    		    try {
		    			// Get ip addresses
		        		Statement stGetIps = _c.createStatement();
		        		ResultSet rsGetIps = stGetIps.executeQuery("SELECT `IP_ADDR` FROM `IP_ADDR` WHERE `STATUS`=1");
		        		
		        		while(rsGetIps.next())
		        		    ips.add(rsGetIps.getString("IP_ADDR"));
		        		
		        		progressBar.setMaximum(ips.size());
		    			
		    			for(String ip : ips) {
                            try {
                                progressBar.setValue(progressBar.getValue()+1);
                                InetAddress target = InetAddress.getByName(ip);
                                if(target.isReachable(500)) {
                                    sender = new Socket(ip, 6868);
                                    PrintWriter out = new PrintWriter(sender.getOutputStream());
                                    out.print(cmd.getCommand());
                                    out.flush();
                                    out.close();

                                    // Getting feedback
                                    BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));
                                    if( ! in.readLine().equals("ok")) {
                                        msgField.append("\n" + ip + " - Ung\u00fcltige Antwort");
                                    }
                                    in.close();
                                } else {
                                    msgField.append("\n" + ip + " - Nicht gefunden");
                                }
                                execFrame.pack();
                            } catch (ConnectException ex) {
                                msgField.append("\n" + ip + " - Keine Antwort");
                                ex.printStackTrace();
                            } catch (UnknownHostException e) {
                                msgField.append("\n" + ip + " - Keine Antwort");
                                e.printStackTrace();
                            } catch (NullPointerException ex) {
                                ex.printStackTrace();
                            } catch (SocketException ex) {
                                ex.printStackTrace();
                            }finally {
                                execFrame.pack();
                            }
                        }
		    			
		    			sender.close();
	    		    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Wir haben ein Problem! [IOException]");
                        e.printStackTrace();
                    } catch (SQLException e1) {
                        JOptionPane.showMessageDialog(null, "Wir haben ein Problem! [SQLException]");
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
}
