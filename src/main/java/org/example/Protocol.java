package org.example;

public class Protocol {
    public static final String COMMAND_GET = "GET";
    public static final String COMMAND_SEPARATOR = "|";
    public static final String RESPONSE_OK = "OK";
    public static final String RESPONSE_ERROR = "ERROR";
    
    public static String buildGetRequest(String filename) {
        return COMMAND_GET + COMMAND_SEPARATOR + filename;
    }
    
    public static String buildOkResponse() {
        return RESPONSE_OK;
    }
    
    public static String buildErrorResponse(String message) {
        return RESPONSE_ERROR + COMMAND_SEPARATOR + message;
    }
    
    public static ParsedCommand parseCommand(String commandString) throws ProtocolException {
        if (commandString == null || commandString.isEmpty()) {
            throw new ProtocolException("Command cannot be empty");
        }
        
        String[] parts = commandString.split("\\|", 2);
        if (parts.length != 2) {
            throw new ProtocolException("Invalid command format. Expected: COMMAND|parameter");
        }
        
        String command = parts[0].trim();
        String parameter = parts[1].trim();
        
        return new ParsedCommand(command, parameter);
    }
    
    public static class ParsedCommand {
        public final String command;
        public final String parameter;
        
        public ParsedCommand(String command, String parameter) {
            this.command = command;
            this.parameter = parameter;
        }
    }
    
    public static class ProtocolException extends Exception {
        public ProtocolException(String message) {
            super(message);
        }
    }
}
