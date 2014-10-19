package client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author TimoNeon, Julien Schroeter
 */
public class ExecCmd {

    public static Map<String, RemoteClassroomCmd> remoteClassroomCmds = new HashMap<String, RemoteClassroomCmd>() {{
        put("quit", new RemoteClassroomCmd() {
            @Override
            public void action() {
                System.exit(0);
            }
        });
    }};


    public static void execRemoteClassroomCommand(String cmd) {
        remoteClassroomCmds.get(cmd).action();
    }

    /**
     * Executes Non RemoteClassroom related commands
     * @param cmd Non RemoteClassroom related command
     * @throws IOException
     */
    public static void execGeneralCommand(String cmd) throws IOException {
        Process exec = Runtime.getRuntime().exec(cmd);
    }
}
