package com.untamedears.citadel.command;

import org.bukkit.command.CommandSender;

public abstract class ConsoleCommand implements Command {
    private final String name_;
    private String description_;
    private String usage_;
    private int minArguments_;
    private int maxArguments_;
    private String[] identifiers_;

    public ConsoleCommand(String name) {
        name_ = name;
        description_ = "";
        usage_ = "";
        minArguments_ = 0;
        maxArguments_ = 0;
        identifiers_ = new String[0];
    }

    public String getName() {
        return name_;
    }

    public String getDescription() {
        return description_;
    }

    public String getUsage() {
        return usage_;
    }

    public int getMinArguments() {
        return minArguments_;
    }

    public int getMaxArguments() {
        return maxArguments_;
    }

    public String[] getIdentifiers() {
        return identifiers_;
    }

    public boolean isIdentifier(CommandSender executor, String input) {
        for (String identifier : identifiers_) {
            if (input.equalsIgnoreCase(identifier)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isInProgress(CommandSender sender) {
        return false;
    }

    public void setDescription(String description) {
        description_ = description;
    }

    public void setUsage(String usage){
        usage_ = usage;
    }

    public void setArgumentRange(int min, int max) {
        minArguments_ = min;
        maxArguments_ = max;
    }

    public void setIdentifiers(String[] identifiers) {
        identifiers_ = identifiers;
    }
}
