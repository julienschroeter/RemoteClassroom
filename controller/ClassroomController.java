package controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import controller.Datatypes.IPAddr;


public class ClassroomController {    
    private String _frameTitle = "Remote Classroom";
    private Dimension _frameSize = new Dimension(400,500);
    private JMenuBar _menubar = new JMenuBar();
    private final int _port = 6868;
    private Connection _c = null;
    
    private JFrame _frame;
    
    private DefaultTableModel _ipTblModel;
    private JTable _ipTbl;
    
    private ClassroomController controller = this;
    
    public ClassroomController(Connection c) {
	this._c = c;
	this.constructMenubar();
	this.constructFrame();
	this.initializeData();
    }
    
    private void constructMenubar() {
	// File
	JMenu filemenu = new JMenu("Datei");
	filemenu.add(new AbstractAction("Neue IP-Adresse"){
	    @Override
	    public void actionPerformed(ActionEvent ae) {
		new IpaddrHandler.Form(_c, controller);
	    }
	});
	
	filemenu.addSeparator();
	
	filemenu.add(new AbstractAction("Über ...") {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(_frame, "(c) Copyright 2014 \n TimoNeon und Julien Schroeter");
	    }
	});
	
	filemenu.add(new AbstractAction("Einstellungen"){
	   @Override
	   public void actionPerformed(ActionEvent ae) {
	       
	   }
	});
	
	filemenu.addSeparator();
	
	filemenu.add(new AbstractAction("Beenden") {
	    @Override
	    public void actionPerformed(ActionEvent ae) {
		System.exit(0);
	    }
	});
	
	_menubar.add(filemenu);
	
	
	// Edit
	JMenu editmenu = new JMenu("Bearbeiten");
	editmenu.add(new AbstractAction("IP-Adresse bearbeiten"){
	    @Override
	    public void actionPerformed(ActionEvent ae) {
		if(_ipTbl.getSelectedRowCount() != 1) {
		    JOptionPane.showMessageDialog(_frame, "Bitte wählen Sie einen Eintrag aus.", "Fehler", JOptionPane.ERROR_MESSAGE);
		} else {
		    int id = (Integer)_ipTbl.getValueAt(
			    _ipTbl.getSelectedRow(),
			    0);
		    try {
			PreparedStatement psGetEntry = _c.prepareStatement("SELECT * FROM `IP_ADDR` WHERE `ID`=?");
			psGetEntry.setInt(1, id);
			ResultSet rsEntry = psGetEntry.executeQuery();
			IPAddr ipEntry = null;
			while(rsEntry.next()) {
			    ipEntry = new IPAddr(
				    rsEntry.getInt("ID"),
				    rsEntry.getString("IP_ADDR"),
				    rsEntry.getString("NOTES"),
				    rsEntry.getBoolean("STATUS"));
			}
			new IpaddrHandler.Form(_c, controller, ipEntry);
		    } catch (SQLException e) {
			e.printStackTrace();
		    }
		}
	    }
	});
	
	editmenu.add(new AbstractAction("IP-Adresse löschen"){
	   @Override
	   public void actionPerformed(ActionEvent ae) {
	       if(_ipTbl.getSelectedRowCount() != 1) {
		    JOptionPane.showMessageDialog(_frame, "Bitte wählen Sie einen Eintrag aus.", "Fehler", JOptionPane.ERROR_MESSAGE);
		} else {
		    int deleteConfirmation = JOptionPane.showConfirmDialog(_frame, "Wollen Sie diese IP-Adresse wirklich löschen?", "Bestätigung erforderlich", JOptionPane.YES_NO_OPTION);
		    if(deleteConfirmation == JOptionPane.OK_OPTION) {
			int id = (Integer)_ipTbl.getValueAt(
				_ipTbl.getSelectedRow(),
				0);
			try {
			    PreparedStatement psDeleteEntry = _c.prepareStatement("DELETE FROM `IP_ADDR` WHERE `ID`=?");
			    psDeleteEntry.setInt(1, id);
			    psDeleteEntry.execute();
			    controller.initializeData();
			} catch (SQLException e) {
			    e.printStackTrace();
			}
		    }
		}
	   }
	});
	
	
	_menubar.add(editmenu);
	
	// Command
	JMenu commandmenu = new JMenu("Befehl");
	commandmenu.add(new AbstractAction("Befehl ausführen") {
	    @Override
	    public void actionPerformed(ActionEvent ae) {
		
	    }
	});
	
	commandmenu.add(new AbstractAction("Befehle verwalten"){
	    @Override
	    public void actionPerformed(ActionEvent ae) {
		new CommandManager(_c);
	    }
	});
	
	_menubar.add(commandmenu);
    }
    
    private void constructFrame() {
	_frame = new JFrame(_frameTitle);
	_frame.setSize(_frameSize);
	_frame.setJMenuBar(_menubar);
	
	// TODO stop database on close
	_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	// INITIALIZE JTABLE FOR IP_ADDR
	String [] cols = {
		"ID",
		"IP_ADDR",
		"NOTES",
		"STATUS"
	};
	_ipTblModel = new DefaultTableModel(cols, 0) {
	    @Override
	    public boolean isCellEditable(int row, int col) {
		return false;
	    }
	};
	
	_ipTbl = new JTable(_ipTblModel);
	_ipTbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	_ipTbl.setPreferredSize(_frameSize);
	_ipTbl.getTableHeader().setReorderingAllowed(false);
	
	// Set ID invisible
	TableColumn ipCol = _ipTbl.getColumn("ID");
	ipCol.setMaxWidth(0);
	ipCol.setMinWidth(0);
	ipCol.setPreferredWidth(0);
	ipCol.setResizable(false);
	
	JScrollPane scrollpane = new JScrollPane(_ipTbl);
	
	_frame.add(scrollpane);
	_frame.pack();
	
	_frame.setVisible(true);
    }
    
    public void initializeData() {
	_ipTblModel.setRowCount(0);
	
	try {
	    Statement stIPAddr = _c.createStatement();
	    ResultSet rsIPAddr = stIPAddr.executeQuery("SELECT * FROM `IP_ADDR`");
	    while(rsIPAddr.next()) {
		_ipTblModel.addRow(new Object[]{
			rsIPAddr.getInt("ID"), 
			rsIPAddr.getString("IP_ADDR"), 
			rsIPAddr.getString("NOTES"), 
			rsIPAddr.getBoolean("STATUS")
		});
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	
    }
    	
    	/*
    	private void buildFrame()
    	{
    		final JFrame frame = new JFrame("Remote Classroom");
    		frame.setSize(_frameSize);
    		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
    		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		
    		JLabel label = new JLabel("IP-Adressen (Eine pro Zeile):");
    		frame.add(label);
    		
    		frame.add(_ipAddrs);
    		
    		JPanel buttonPane = new JPanel();
    		
    		JButton saveButton = new JButton("Änderungen speichern");
    		saveButton.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseClicked(MouseEvent me)
    			{
    				try {
    					PrintWriter out = new PrintWriter(_ipsFile, "UTF-8");
    					String[] ips = _ipAddrs.getText().split("\n");
    					
    					for(String ip : ips)
    					{
    						out.write(ip + "\n");
    					}
    					out.flush();
    					out.close();
    				} catch (FileNotFoundException e) {
    					e.printStackTrace();
    				} catch (UnsupportedEncodingException e) {
    					e.printStackTrace();
    				}
    				
    			}
    		});
    		buttonPane.add(saveButton);
    		
    		final JButton executeButton = new JButton("Ausführen");
    		executeButton.addActionListener(new ActionListener()
    		{
    			@Override
    			public void actionPerformed(ActionEvent ae) {
    				// Deactive button
    				executeButton.setEnabled(false);
    				Thread th = new Thread(new Runnable() {
    					
    					@Override
    					public void run() {
    						Socket sender = null;
    						String[] ips = _ipAddrs.getText().split("\n");
    						String[] failed = new String[ips.length];
    						int i = 0;
    						for(String ip : ips)
    						{
    							if(Pattern.matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b", ip))
    							{
    								try {
    									try {
    										sender = new Socket(ip, _port);
    										PrintWriter out = new PrintWriter(sender.getOutputStream());
    										out.print("format");
    										out.flush();
    										out.close();
    										
    										// Getting feedback
    										BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));
    										if( ! in.readLine().equals("ok")){
    											failed[i++] = ip;
    										}
    										
    										sender.close();
    									}
    									catch(ConnectException ce)
    									{
    										failed[i++] = ip;
    									}
    								} catch (UnknownHostException e) {
    									e.printStackTrace();
    								} catch (IOException e) {
    									e.printStackTrace();
    								}
    							}
    							else
    								failed[i++] = ip;
    						}
    						
    						String output = "";
    						// Output errors
    						for(int j=0; j<i;j++)
    							output += failed[j] + "\n";
    							
    						if(i > 0)
    							JOptionPane.showMessageDialog(frame, "Einige Computer konnten nicht angesteuert werden: \n" + output, "Fehler: Daten NICHT gelÔøΩscht", JOptionPane.ERROR_MESSAGE);
    						
    						// reactive button
    						executeButton.setEnabled(true);
    					}
    				});
    				th.start();
    				
    				JOptionPane.showMessageDialog(frame, "Befehl wurde versandt.");
    			}
    		});
    		buttonPane.add(executeButton);
    		
    		frame.add(buttonPane);
    		
    		
    		frame.setVisible(true);
    	}
    	
    	private void loadIpAddr()
    	{
    		try {
    			BufferedReader in = new BufferedReader(new FileReader(_ipsFile));
    			String ip;
    			while((ip = in.readLine()) != null)
    			{
    				_ipAddrs.append(ip + "\n");
    				
    				ip = in.readLine();
    			}
    		in.close();
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    				e.printStackTrace();
    		}
    	}*/

}
