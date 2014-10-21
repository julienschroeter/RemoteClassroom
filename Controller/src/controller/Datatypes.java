/**
 * 
 */
package controller;

/**
 * Class which holds data models
 * @author Julien Schroeter
 *
 */
public class Datatypes {
    public static class IPAddr {
        private int _id;
        private String _ipaddr, _notes;
        private boolean _status;

        public IPAddr() {

        }

        public IPAddr(int id, String ipaddr, String notes, boolean status) {
            this._id = id;
            this._ipaddr = ipaddr;
            this._notes = notes;
            this._status = status;
        }

        public int getId() {
            return _id;
        }

        public void setId(int id) {
            this._id = id;
        }

        public String getIpaddr() {
            return _ipaddr;
        }

        public void setIpaddr(String ipaddr) {
            this._ipaddr = ipaddr;
        }

        public String getNotes() {
            return _notes;
        }

        public void setNotes(String notes) {
            this._notes = notes;
        }

        public boolean getStatus() {
            return _status;
        }

        public void setStatus(boolean status) {
	    this._status = status;
	}
    }
    
    public static class Command {
        private int _id;
        private String _label, _command;
        private boolean _required;


        public Command() {

        }

        public Command(String command) {
            this._command = command;
        }

        public Command(int id, String label, String command, boolean required) {
            this._id = id;
            this._label = label;
            this._command = command;
            this._required = required;
        }

        public int getId() {
            return _id;
        }

        public void setId(int id) {
            this._id = id;
        }

        public String getLabel() {
            return _label;
        }

        public void setLabel(String label) {
            this._label = label;
        }

        public String getCommand() {
            return _command;
        }

        public void setCommand(String command) {
            this._command = command;
        }

        public boolean getRequired() {
            return _required;
        }

        public void setRequired(boolean required) {
            this._required = required;
        }

        @Override
        public String toString() {
            return this._label;
        }
    }

    public static class Action {
        public void action(String ip) {

        }
    }
}
