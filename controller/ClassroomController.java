package controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
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
    
    private DefaultTableModel _ipTblModel;
    private JTable _ipTbl;
    
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
		
	    }
	});
	
	filemenu.add(new AbstractAction("Einstellungen"){
	   @Override
	   public void actionPerformed(ActionEvent ae) {
	       
	   }
	});
	
	filemenu.add(new AbstractAction("Beenden") {
	    
	    @Override
	    public void actionPerformed(ActionEvent e) {
		
	    }
	});
	
	_menubar.add(filemenu);
	
	
	// Edit
	JMenu editmenu = new JMenu("Bearbeiten");
	
	
	_menubar.add(editmenu);
	
	// Command
	JMenu commandmenu = new JMenu("Befehl");
	
	
	_menubar.add(commandmenu);
    }
    
    private void constructFrame() {
	final JFrame frame = new JFrame(_frameTitle);
	frame.setSize(_frameSize);
	frame.setJMenuBar(_menubar);
	
	// TODO stop database on close
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
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
	
	frame.add(scrollpane);
	frame.pack();
	
	frame.setVisible(true);
    }
    
    private void initializeData() {
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
    		
    		JButton saveButton = new JButton("€nderungen speichern");
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
    		
    		final JButton executeButton = new JButton("AusfŸhren");
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
    							JOptionPane.showMessageDialog(frame, "Einige Computer konnten nicht angesteuert werden: \n" + output, "Fehler: Daten NICHT gelï¿½scht", JOptionPane.ERROR_MESSAGE);
    						
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
