package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

public class FormatClient {
	ServerSocket _ss;
	Socket _s;
	boolean _suceeded;
	
	public static void main(String[] args) {
		new FormatClient();
		System.out.println("dr");
	}
	
	public FormatClient() {
		init();
	}
	
	void init(){
		try {
			_ss = new ServerSocket(6868);
			_s = _ss.accept();
			BufferedReader in = new BufferedReader(new InputStreamReader(_s.getInputStream()));
			String tmp;
			while((tmp=in.readLine())!=null){
				if(tmp.equalsIgnoreCase("format")) _suceeded = format();
			}
			PrintWriter out = new PrintWriter(_s.getOutputStream());
			
			if(_suceeded) out.println("ok");
			if(!_suceeded) out.println("fail");
			
			out.flush();
			out.close();
			
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Der Port 6868 wir bereits benutzt");
		}
	}
	
	@SuppressWarnings("unused")
	boolean format(){
		boolean sucessfull = true;
	//	try {
			//final Process p = Runtime.getRuntime().exec("cmd /c start cmd.exe /K \"del e:\\* /s /q && exit \"");
		System.out.println("FORMAT");	
		sucessfull = true;
		/*} catch (IOException e) {
			e.printStackTrace();
			sucessfull = false;
		}*/
		
		return sucessfull;
	}

}
