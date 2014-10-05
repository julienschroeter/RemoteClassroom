/**
 * 
 */
package controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import controller.Datatypes.Command;

/**
 * @author Julien Schroeter
 *
 */
public class CommandManager {
    private Connection _c;
    private CommandManager manager = this;
    
    private JList<Command> _cmdList;
    private DefaultListModel<Command> _cmdListModel;
    
    private JFrame _frame;
    private JButton _btnNew;
    private JButton _btnEdit;
    private JButton _btnDelete;
    
    public CommandManager(Connection c) {
	_c = c;
	
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
	_btnNew = new JButton("Neu");
	btnPanel.add(_btnNew);
	
	_btnEdit = new JButton("Bearbeiten");
	btnPanel.add(_btnEdit);
	
	_btnDelete = new JButton("L�schen");
	btnPanel.add(_btnDelete);
	
	_frame.add(btnPanel);
	
	// Initialize Commands
	this.initializeData();
	
	this.newEntry();
	this.editEntry();
	this.deleteEntry();
	
	_frame.pack();
	_frame.setResizable(false);
	_frame.setVisible(true);
    }
    
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
    
    private void newEntry() {
	_btnNew.addActionListener(new ActionListener(){
	   @Override
	   public void actionPerformed(ActionEvent ae) {
	       // Build frame
	       final JFrame addFrame = new JFrame("Neuer Befehl");
	       addFrame.setLayout(new GridLayout(3, 2));
	       
	       addFrame.add(new JLabel("Titel: "));
	       final JTextField labelTextfield = new JTextField();
	       addFrame.add(labelTextfield);
	       
	       addFrame.add(new JLabel("Befehl: "));
	       final JTextField cmdTextfield = new JTextField("cmd /c start cmd.exe /K \" &&exit\"");
	       cmdTextfield.setColumns(20);
	       addFrame.add(cmdTextfield);
	       
	       JButton addSaveBtn = new JButton("Speichern");
	       addSaveBtn.addActionListener(new ActionListener(){
		   @Override
		   public void actionPerformed(ActionEvent ae) {
		       String label = labelTextfield.getText();
		       String cmd = cmdTextfield.getText();
		       // Validation
		       if(label.length() < 1 || cmd.length() < 1) {
			   JOptionPane.showMessageDialog(addFrame, "Bitte geben Sie einen Titel und einen Befehl an.", "Fehler", JOptionPane.ERROR_MESSAGE);
		       } else {
			   // Save
			   try {
			       PreparedStatement psSaveCmd = _c.prepareStatement("INSERT INTO `COMMANDS` (`LABEL`, `COMMAND`) VALUES (?,?)");
			       psSaveCmd.setString(1, label);
			       psSaveCmd.setString(2, cmd);
			       psSaveCmd.execute();
			       addFrame.dispose();
			       manager.initializeData();
			   } catch(SQLException ex) {
			       ex.printStackTrace();
			   }
		       }
		   }
	       });
	       addFrame.add(addSaveBtn);
	       
	       addFrame.pack();
	       addFrame.setVisible(true);
	   }
	});
    }
    
    private void editEntry() {
	_btnEdit.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent ae) {
		if(_cmdList.getSelectedIndex() == -1) {
		    JOptionPane.showMessageDialog(_frame, "Sie m�ssen den Befehl ausw�hlen, den Sie bearbeiten m�chten.", "Fehler", JOptionPane.ERROR_MESSAGE);
		} else {
		    final Command entry = _cmdList.getSelectedValue();
		    // Build frame
		    final JFrame addFrame = new JFrame("Neuer Befehl");
		    addFrame.setLayout(new GridLayout(3, 2));
		    
		    addFrame.add(new JLabel("Titel: "));
		    final JTextField labelTextfield = new JTextField(entry.getLabel());
		    addFrame.add(labelTextfield);
		    
		    addFrame.add(new JLabel("Befehl: "));
		    final JTextField cmdTextfield = new JTextField(entry.getCommand());
		    cmdTextfield.setColumns(20);
		    addFrame.add(cmdTextfield);

		    JButton addSaveBtn = new JButton("Speichern");
		    addSaveBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
			    String label = labelTextfield.getText();
			    String cmd = cmdTextfield.getText();
			    // Validation
			    if(label.length() < 1 || cmd.length() < 1) {
				JOptionPane.showMessageDialog(addFrame, "Bitte geben Sie einen Titel und einen Befehl an.", "Fehler", JOptionPane.ERROR_MESSAGE);
			    } else {
				// Save
				try {
				    PreparedStatement psSaveCmd = _c.prepareStatement("UPDATE `COMMANDS` SET `LABEL`=?, `COMMAND`=? WHERE `ID`=?");
				    psSaveCmd.setString(1, label);
				    psSaveCmd.setString(2, cmd);
				    psSaveCmd.setInt(3, entry.getId());
				    psSaveCmd.execute();
				    addFrame.dispose();
				    manager.initializeData();
				} catch(SQLException ex) {
				    ex.printStackTrace();
				}
			    }
			}
		    });
		    addFrame.add(addSaveBtn);

		    addFrame.pack();
		    addFrame.setVisible(true);
		}
	    }
	});
    }
    
    private void deleteEntry() {
	_btnDelete.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent ae) {
		if(_cmdList.getSelectedIndex() == -1) {
		    JOptionPane.showMessageDialog(_frame, "Sie m�ssen den Befehl ausw�hlen, den Sie l�schen m�chten.", "Fehler", JOptionPane.ERROR_MESSAGE);
		} else {
		    int deleteConfirmation = JOptionPane.showConfirmDialog(_frame, "Wollen Sie diesen Befehl wirklich l�schen?", "Best�tigung erforderlich", JOptionPane.YES_NO_OPTION);
		    if(deleteConfirmation == JOptionPane.OK_OPTION) {
			int id = _cmdList.getSelectedValue().getId();
			try {
			    PreparedStatement psDeleteEntry = _c.prepareStatement("DELETE FROM `COMMANDS` WHERE `ID`=?");
			    psDeleteEntry.setInt(1, id);
			    psDeleteEntry.execute();
			    manager.initializeData();
			} catch(SQLException ex) {
			    ex.printStackTrace();
			}
		    }
		}
	    }
	});
    }
}
