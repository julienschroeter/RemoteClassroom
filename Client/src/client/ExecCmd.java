package client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
        put("hello", new RemoteClassroomCmd() {
            @Override
            public void action() {
                System.out.println("Hello World!");
            }
        });
        put("update", new RemoteClassroomCmd() {
            @Override
            public void action() {
                try {
                    // Get current directory
                    String currentDirectory = ExecCmd.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                    currentDirectory = currentDirectory.substring(0, currentDirectory.length()-(currentDirectory.length() - currentDirectory.lastIndexOf("/")));

                    URL jarfile = new URL("jar:file://" + currentDirectory + "/ClientUpdate.jar!/");
                    URLClassLoader classLoader = new URLClassLoader(new URL[]{ jarfile });

                    Class updateClass = classLoader.loadClass("clientUpdate.Init");
                    Object updateInit = updateClass.newInstance();

                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (InstantiationException ex) {
                    ex.printStackTrace();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }};


    public static void execRemoteClassroomCommand(String cmd) {
        cmd = cmd.substring(("sys://").length());
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
