package controller;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import controller.Datatypes.IPAddr;

/**
 * Provides functionality to manage ip addresses
 * @author Julien Schroeter
 *
 */
public class IpaddrHandler {
	Connection _c;
	
	/**
	 * Displays form to add a new ip address and provides functionality to validate and save it.
	 * @param c Connection
	 * @param controller ClassroomController
	 */
	public IpaddrHandler(Connection c, ClassroomController controller) {
	    this(c, controller, null);
	}
	
	/**
	 * Displays form to edit an existing ip address and provides functionality to validate and save it.<br />
	 * Set IPAddr entry = null to create a new one
	 * @param c Connection
	 * @param controller ClassroomController
	 * @param entry IPAddr
	 */
	public IpaddrHandler(Connection c, final ClassroomController controller, final IPAddr entry) {
	    this._c = c;
	    
	    // create form
	    final JFrame formFrame = new JFrame((entry == null) ? "Neue IP-Adresse" : "IP-Adresse bearbeiten");
	    formFrame.setLayout(new GridLayout(2, 1));
	    
	    JPanel inputPanel = new JPanel();
	    inputPanel.setLayout(new GridLayout(2,2));
	    inputPanel.add(new JLabel("IP-Adresse: "));
	    final JTextField ipAddrTextfield = new JTextField((entry == null) ? "" : entry.getIpaddr());
	    ipAddrTextfield.setColumns(16);
	    inputPanel.add(ipAddrTextfield);
	    
	    inputPanel.add(new JLabel("Bezeichnung: "));
	    final JTextField notesTextfield = new JTextField((entry == null) ? "" : entry.getNotes());
	    inputPanel.add(notesTextfield);
	    formFrame.add(inputPanel);
	    
	    JPanel oneColPanel = new JPanel();
	    oneColPanel.setLayout(new GridLayout(2,1));
	    
	    final JCheckBox statusCheckbox = new JCheckBox("Aktiviert");
	    statusCheckbox.setSelected((entry == null) ? true : entry.getStatus());
	    oneColPanel.add(statusCheckbox);
	    
	    JButton saveBtn = new JButton("Speichern");
	    
	    saveBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    String ipAddr = ipAddrTextfield.getText();
			    String notes = notesTextfield.getText();
			    boolean status = statusCheckbox.isSelected();
			    
			    // validation
			    if(!Pattern.matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b", ipAddr)) {
			    	JOptionPane.showMessageDialog(formFrame, "Ung\u00fcltige IP-Adresse.", "Fehler", JOptionPane.ERROR_MESSAGE);
			    } else if(entry == null) {
					// add new entry
					try {
					    PreparedStatement psAddIp = _c.prepareStatement("INSERT INTO `IP_ADDR` (`IP_ADDR`, `NOTES`, `STATUS`) VALUES (?,?,?)");
					    psAddIp.setString(1, ipAddr);
					    psAddIp.setString(2, notes);
					    psAddIp.setBoolean(3, status);
					    psAddIp.execute();
					    formFrame.dispose();
					    controller.initializeData();
					} catch (SQLException e1) {
					    e1.printStackTrace();
					}
			    } else {
					// just editing
					int id = entry.getId();
					
					try {
					    PreparedStatement psEditIp = _c.prepareStatement("UPDATE `IP_ADDR` SET `IP_ADDR`=?, `NOTES`=?,`STATUS`=? WHERE `ID`=?");
					    psEditIp.setString(1, ipAddr);
					    psEditIp.setString(2, notes);
					    psEditIp.setBoolean(3, status);
					    psEditIp.setInt(4, id);
					    psEditIp.execute();
					    formFrame.dispose();
					    controller.initializeData();
					} catch (SQLException e1) {
					    e1.printStackTrace();
					}
			    }
			}
	    });
	    
	    oneColPanel.add(saveBtn);
	    formFrame.add(oneColPanel);
	    
	    formFrame.setVisible(true);
	    formFrame.pack();
	}
}
