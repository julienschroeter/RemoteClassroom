package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static client.ExecCmd.execGeneralCommand;
import static client.ExecCmd.execRemoteClassroomCommand;

/**
 * Starter for application
 * @author TimoNeon, Julien Schroeter
 */
public class ClassroomClient {
    public final int PORT = 6868;

    /**
     * Starter for application
     * @param args
     */
    public static void main(String[] args) {
        ClassroomClient app = new ClassroomClient();
        app.listener();
    }

    /**
     * Listener and treats commands from ClassroomController
     */
    private void listener() {
        while(true) {
            try {
                ServerSocket ss = new ServerSocket(PORT);
                Socket sock = ss.accept();

                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter out = new PrintWriter(sock.getOutputStream());
                CmdParser parser = new CmdParser();

                String tmp;
                while((tmp=in.readLine()) != null) {
                    if(parser.parseRemoteClassroomCommand(tmp)) {
                        execRemoteClassroomCommand(tmp);
                        out.print("ok");
                    } else if(parser.parse(tmp)) {
                        execGeneralCommand(tmp);
                        out.print("ok");
                    } else {
                        out.print("fail");
                    }
                }
                out.flush();

                in.close();
                out.close();

                sock.close();
                ss.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}
