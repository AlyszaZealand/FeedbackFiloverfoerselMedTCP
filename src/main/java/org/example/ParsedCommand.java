package org.example;

public class ParsedCommand {
    private final String command;
    private final String parameter;

    public ParsedCommand(String command, String parameter) {
        this.command = command;
        this.parameter = parameter;
    }

    public String getCommand() {
        return command;
    }

    public String getParameter() {
        return parameter;
    }
}
