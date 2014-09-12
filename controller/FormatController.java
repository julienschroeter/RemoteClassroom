package app;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class FormatController {
	private Dimension _frameSize = new Dimension(400,500);
	private final JTextArea _ipAddrs =  new JTextArea();
	private final int _port = 6868;
	private final String _ipsFile = "ips.txt";
	
	public static void main(String[] args)
	{
		FormatController app = new FormatController();
		app.buildFrame();
		
		app.loadIpAddr();
		
	}
	
	private void buildFrame()
	{
		final JFrame frame = new JFrame("Partition formatieren");
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
							JOptionPane.showMessageDialog(frame, "Einige Computer konnten nicht angesteuert werden: \n" + output, "Fehler: Daten NICHT gelöscht", JOptionPane.ERROR_MESSAGE);
						
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
	}

}
