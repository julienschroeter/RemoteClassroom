package controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import controller.Datatypes.IPAddr;

/**
 * Main form
 * @author Julien Schroeter
 *
 */
public class ClassroomController {    
    private String _frameTitle = "Remote Classroom";
    private Dimension _frameSize = new Dimension(400,500);
    private JMenuBar _menubar = new JMenuBar();
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
    
    /**
     * Defines menubar items and actions for the main application
     */
    private void constructMenubar() {
		// File
		JMenu filemenu = new JMenu("Datei");
		filemenu.add(new AbstractAction("Neue IP-Adresse"){
		    @Override
		    public void actionPerformed(ActionEvent ae) {
			    new IpaddrHandler(_c, controller);
		    }
		});
		
		filemenu.addSeparator();
		
		filemenu.add(new AbstractAction("\u00dcber ...") {
		    @Override
		    public void actionPerformed(ActionEvent e) {
			    JOptionPane.showMessageDialog(_frame, "ClassroomController\n(c) Copyright 2015 \n Julien Schroeter, Timo Bauer", "\u00dcber ClassroomController", JOptionPane.PLAIN_MESSAGE);
		    }
		});
		
		/*filemenu.add(new AbstractAction("Einstellungen"){
		   @Override
		   public void actionPerformed(ActionEvent ae) {
		       
		   }
		});*/
		
		filemenu.addSeparator();
		
		filemenu.add(new AbstractAction("Beenden") {
		    @Override
		    public void actionPerformed(ActionEvent ae) {
                try {
                    _c.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    System.exit(0);
                }
            }
		});
		
		_menubar.add(filemenu);
		
		
		// Edit
		JMenu editmenu = new JMenu("Bearbeiten");
		editmenu.add(new AbstractAction("IP-Adresse bearbeiten"){
		    @Override
		    public void actionPerformed(ActionEvent ae) {
                if(_ipTbl.getSelectedRowCount() < 1) {
                    JOptionPane.showMessageDialog(_frame, "Bitte w\u00e4hlen Sie einen Eintrag aus.", "Fehler", JOptionPane.ERROR_MESSAGE);
                } else if(_ipTbl.getSelectedRowCount() > 1) {
                    JOptionPane.showMessageDialog(_frame, "Sie können nur einen Eintrag bearbeiten.", "Fehler", JOptionPane.ERROR_MESSAGE);
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
                        new IpaddrHandler(_c, controller, ipEntry);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
		    }
		});
		
		editmenu.add(new AbstractAction("IP-Adresse l\u00f6schen"){
		    @Override
		    public void actionPerformed(ActionEvent ae) {
                if(_ipTbl.getSelectedRowCount() < 1) {
                    JOptionPane.showMessageDialog(_frame, "Bitte w\u00e4hlen Sie einen Eintrag aus.", "Fehler", JOptionPane.ERROR_MESSAGE);
                } else {
                    int deleteConfirmation = JOptionPane.showConfirmDialog(_frame, "Wollen Sie die ausgew\u00e4lte(n) IP-Adresse(n) wirklich l\u00f6schen?", "Best\u00e4tigung erforderlich", JOptionPane.YES_NO_OPTION);
                    if(deleteConfirmation == JOptionPane.OK_OPTION) {
                        for(int r : _ipTbl.getSelectedRows()) {
                            int id = (Integer) _ipTbl.getValueAt(r, 0);

                            try {
                                PreparedStatement psDeleteEntry = _c.prepareStatement("DELETE FROM `IP_ADDR` WHERE `ID`=?");
                                psDeleteEntry.setInt(1, id);
                                psDeleteEntry.execute();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        controller.initializeData();
                    }
                }
            }
        });
		
		
		_menubar.add(editmenu);
		
		// Command
		JMenu commandmenu = new JMenu("Befehl");
		commandmenu.add(new AbstractAction("Befehl ausf\u00fchren") {
		    @Override
		    public void actionPerformed(ActionEvent ae) {
			    CommandExec cmdExec = new CommandExec(_c);
                cmdExec.showSelection();
		    }
		});
		
		commandmenu.add(new AbstractAction("Befehle verwalten"){
		    @Override
		    public void actionPerformed(ActionEvent ae) {
			    new CommandManager(_c);
		    }
		});

        commandmenu.addSeparator();

        commandmenu.add(new AbstractAction("Clientsoftware aktualisieren") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new ClientUpdate(_c);
            }
        });
		
		_menubar.add(commandmenu);
    }
    
    /**
     * Defines the main frame
     */
    private void constructFrame() {
		_frame = new JFrame(_frameTitle);
		_frame.setSize(_frameSize);
		_frame.setJMenuBar(_menubar);
		
		_frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    _c.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    System.exit(0);
                }
            }
        });
		
		// INITIALIZE JTABLE FOR IP_ADDR
		String [] cols = {
			"ID",
            "STATUS",
			"IP_ADDR",
			"NOTES"
		};
		_ipTblModel = new DefaultTableModel(cols, 0) {
		    @Override
		    public boolean isCellEditable(int row, int col) {
			    return (col==1 || col==3);
		    }
            @Override
            public Class getColumnClass(int columnIndex) {
                if(columnIndex == 1) {
                    return Boolean.class;
                }
                return String.class;
            }
            @Override
            public void fireTableCellUpdated(int row, int column) {
                Object data = _ipTbl.getValueAt(row, column);
                int entryId = (Integer) _ipTbl.getValueAt(row, 0);
                try {
                    PreparedStatement psUpdate;
                    if(column == 1) {
                        psUpdate = _c.prepareStatement("UPDATE `IP_ADDR` SET `STATUS`=? WHERE `ID`=?");
                        psUpdate.setBoolean(1, ((Boolean)data).booleanValue());
                        psUpdate.setInt(2, entryId);
                    } else {
                        psUpdate = _c.prepareStatement("UPDATE `IP_ADDR` SET `NOTES`=? WHERE `ID`=?");
                        psUpdate.setString(1, data.toString());
                        psUpdate.setInt(2, entryId);
                    }
                    psUpdate.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        };

		_ipTbl = new JTable(_ipTblModel);
		_ipTbl.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_ipTbl.setPreferredSize(_frameSize);
		_ipTbl.getTableHeader().setReorderingAllowed(false);
		
		// Set ID invisible
		TableColumn ipCol = _ipTbl.getColumn("ID");
		ipCol.setMaxWidth(0);
		ipCol.setMinWidth(0);
		ipCol.setPreferredWidth(0);
		ipCol.setResizable(false);

        TableColumn statusCol = _ipTbl.getColumn("STATUS");
        statusCol.setHeaderValue("");
        statusCol.setMaxWidth(16);
        statusCol.setPreferredWidth(16);

        _ipTbl.getColumn("IP_ADDR").setHeaderValue("IP Adresse");
        _ipTbl.getColumn("NOTES").setHeaderValue("Bezeichnung");


		JScrollPane scrollpane = new JScrollPane(_ipTbl);
		
		_frame.add(scrollpane);
		_frame.pack();
		
		_frame.setVisible(true);
    }
    
    /**
     * Loads ip addresses from database and refreshes JTable
     */
    public void initializeData() {
		_ipTblModel.setRowCount(0);
		
		try {
		    Statement stIPAddr = _c.createStatement();
		    ResultSet rsIPAddr = stIPAddr.executeQuery("SELECT * FROM `IP_ADDR`");
		    while(rsIPAddr.next()) {
                _ipTblModel.addRow(new Object[]{
                    rsIPAddr.getInt("ID"),
                    rsIPAddr.getBoolean("STATUS"),
                    rsIPAddr.getString("IP_ADDR"),
                    rsIPAddr.getString("NOTES")
                });
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	
    }
}
