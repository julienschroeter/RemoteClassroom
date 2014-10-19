package client;

/**
 * @author Julien Schroeter
 */
public class CmdParser {

    /**
     * Parses validity of command to execute. Filters between RemoteClassroom specific and general commands.
     * @param cmd Command to parse
     * @return Whether this is a valid command or not
     */
    public boolean parse(String cmd) {
        if(cmd.startsWith("sys://")) {
            // Command is RemoteClassroom specific
            // Check if command is defined
            if(parseRemoteClassroomCommand(cmd))
                return true;
        } else
            return true;

        return false;
    }

    /**
     * Parses if this is a valid RemoteClassroom specific command or not.
     * @param cmd Command to parse
     * @return Whether this is a valid command or not
     */
    public boolean parseRemoteClassroomCommand(String cmd) {
        if(cmd.startsWith("sys://")) {
            cmd = cmd.substring(("sys://").length());
            if(ExecCmd.remoteClassroomCmds.containsKey(cmd))
                return true;
        }

        return false;
    }

    /**
     * Converts raw RemoteClassroom specific command to executable RemoteClassroom specific command
     * @param cmd RemoteClassroom specific command
     * @return Executable RemoteClassroom specific command
     */
    public String getRemoteClassroomCmd(String cmd) {
        if(parseRemoteClassroomCommand(cmd))
            return cmd.substring(("sys://").length());

        return null;
    }
}
