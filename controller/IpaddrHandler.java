/**
/**
 * 
 */
/**
/**
 * 
 */
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
 * @author Julien Schroeter
 *
 */
public class IpaddrHandler {
    public static class Form {
	Connection _c;
	
	public Form(Connection c) {
	    this(c, null);
	}

	public Form(Connection c, final IPAddr entry) {
	    this._c = c;
	    
	    // create form
	    final JFrame formFrame = new JFrame((entry == null) ? "Neue IP-Adresse" : "IP-Adresse bearbeiten");
	    formFrame.setLayout(new GridLayout(2, 1));
	    
	    JPanel inputPanel = new JPanel();
	    inputPanel.setLayout(new GridLayout(2,2));
	    inputPanel.add(new JLabel("IP-Adresse: "));
	    final JTextField ipAddrTextfield = new JTextField();
	    ipAddrTextfield.setColumns(16);
	    inputPanel.add(ipAddrTextfield);
	    
	    inputPanel.add(new JLabel("Anmerkungen: "));
	    final JTextField notesTextfield = new JTextField();
	    inputPanel.add(notesTextfield);
	    formFrame.add(inputPanel);
	    
	    JPanel oneColPanel = new JPanel();
	    oneColPanel.setLayout(new GridLayout(2,1));
	    
	    final JCheckBox statusCheckbox = new JCheckBox("Aktiviert");
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
			JOptionPane.showMessageDialog(formFrame, "Ungültige IP-Adresse.", "Fehler", JOptionPane.ERROR_MESSAGE);
		    } else if(entry == null) {
			// add new entry
			try {
			    PreparedStatement psAddIp = _c.prepareStatement("INSERT INTO `IP_ADDR` (`IP_ADDR`, `NOTES`, `STATUS`) VALUES (?,?,?)");
			    psAddIp.setString(1, ipAddr);
			    psAddIp.setString(2, notes);
			    psAddIp.setBoolean(3, status);
			    psAddIp.execute();
			} catch (SQLException e1) {
			    e1.printStackTrace();
			}
		    } else {
			// just editing
			
		    }
		}
	    });
	    
	    oneColPanel.add(saveBtn);
	    formFrame.add(oneColPanel);
	    
	    formFrame.setVisible(true);
	    formFrame.pack();
	}
    }
}
