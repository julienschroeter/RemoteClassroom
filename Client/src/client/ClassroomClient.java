package client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import static client.ExecCmd.execGeneralCommand;
import static client.ExecCmd.execRemoteClassroomCommand;

/**
 * Starter for application
 * @author Timo Bauer, Julien Schroeter
 */
public class ClassroomClient {
    private static Properties _config = new Properties();

    /**
     * Starter for application
     * @param args
     */
    public static void main(String[] args) {
        try {
            _config.load(new FileInputStream("RemoteClassroom.conf"));
            // Create config file if needed
            if(_config.isEmpty()) {
                _config.setProperty("UPDATER.V","");
                _config.setProperty("PORT.DEFAULT","6868");
                _config.setProperty("PORT.FILE","6869");
                _config.setProperty("IP.CONTROLLER","");
                _config.store(new FileOutputStream("RemoteClassroom.conf"), "");
            }

            ClassroomClient app = new ClassroomClient();
            app.listener();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Listener and treats commands from ClassroomController
     */
    private void listener() {
        while(true) {
            try {
                ServerSocket ssock = new ServerSocket(Integer.parseInt(_config.getProperty("PORT.DEFAULT")));
                Socket sock = ssock.accept();
                String senderIp = sock.getRemoteSocketAddress().toString();

                if(senderIp.startsWith("/")) senderIp = senderIp.substring(1);
                if(senderIp.indexOf(":") != -1) senderIp = senderIp.split(":")[0];

                if(senderIp.equals(_config.getProperty("IP.CONTROLLER"))) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    PrintWriter out = new PrintWriter(sock.getOutputStream());
                    CmdParser parser = new CmdParser();

                    String tmp;
                    while((tmp=in.readLine()) != null) {
                        if(parser.parseRemoteClassroomCommand(tmp)) {
                            execRemoteClassroomCommand(tmp);
                            out.println("ok");
                            out.flush();
                        } else if(parser.parse(tmp)) {
                            execGeneralCommand(tmp);
                            out.println("ok");
                            out.flush();
                        } else {
                            out.println("fail");
                            out.flush();
                        }
                    }

                    in.close();
                    out.close();
                } else
                    System.out.println("Connection refused: " + senderIp);
                sock.close();
                ssock.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}
