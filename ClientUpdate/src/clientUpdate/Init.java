package clientUpdate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * Initializing client update process
 * @author Julien Schroeter
 */
public class Init implements Runnable {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private Properties _config = new Properties();

    public Init() {
        try {
            _config.load(new FileInputStream("RemoteClassroom.conf"));
            this.initializeUpdateFrame();
            Thread th = new Thread(this);
            th.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void initializeUpdateFrame() {
        JDialog updateFrame = new JDialog();
        updateFrame.setTitle("Softwareaktualisierung ...");
        updateFrame.setSize(860, 80);
        updateFrame.setResizable(false);
        updateFrame.setLocation(
                (Toolkit.getDefaultToolkit().getScreenSize().width / 2) - (updateFrame.getSize().width / 2),
                (Toolkit.getDefaultToolkit().getScreenSize().height / 2) - (updateFrame.getSize().height / 2)
        );
        updateFrame.setLayout(new BoxLayout(updateFrame.getContentPane(), BoxLayout.Y_AXIS));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBorder(new EmptyBorder(10,10,10,10));
        updateFrame.add(progressBar);

        statusLabel = new JLabel("Softwareaktualisierung l\u00e4uft; Rechner wird in wenigen Minuten heruntergefahren");
        statusLabel.setBorder(new EmptyBorder(0,10,5,10));
        updateFrame.add(statusLabel);

        updateFrame.setVisible(true);
    }

    @Override
    public void run() {
        try {
            Socket sock = new Socket(_config.getProperty("IP.CONTROLLER"), Integer.parseInt(_config.getProperty("PORT.FILE")));

            // Receiving file Client.jar
            String fileToUpdate = Init.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            fileToUpdate = fileToUpdate.substring(0, fileToUpdate.length()-(fileToUpdate.length() - fileToUpdate.lastIndexOf("/")));
            fileToUpdate = fileToUpdate.substring(0, fileToUpdate.length() - 1);
            fileToUpdate = fileToUpdate.substring(0, fileToUpdate.length()-(fileToUpdate.length() - fileToUpdate.lastIndexOf("/"))) + "/Client.jar";

            if(fileToUpdate.startsWith("file:"))
                fileToUpdate = fileToUpdate.substring(5);

            DataInputStream sockInBytes = new DataInputStream(sock.getInputStream());
            FileOutputStream fileOut = new FileOutputStream(fileToUpdate);

            byte[] b = new byte[1];

            while((sockInBytes.read(b)) != -1) {
                fileOut.write(b);
                fileOut.flush();
            }

            fileOut.close();
            sock.close();
            sockInBytes.close();
            statusLabel.setText("Softwareaktualisierung beendet; Rechner wird in wenigen Minuten heruntergefahren");
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(1);
            progressBar.setValue(1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
