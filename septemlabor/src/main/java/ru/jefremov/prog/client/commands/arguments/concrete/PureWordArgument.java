package ru.jefremov.prog.client.commands.arguments.concrete;

import ru.jefremov.prog.client.commands.Argumentable;
import ru.jefremov.prog.client.commands.arguments.ArgumentPlacement;
import ru.jefremov.prog.client.commands.arguments.primitive.StringArgument;
import ru.jefremov.prog.client.exceptions.command.IllegalCommandArgumentException;
import ru.jefremov.prog.client.exceptions.command.InvalidCommandArgumentException;

/**
 * Аргумент, отвечающий за название билета.
 */
public class PureWordArgument extends StringArgument {
    private final String comment = "It is forbidden to use quotation marks, spaces and brackets";
    public PureWordArgument(String name, ArgumentPlacement placement, Argumentable argumentable, String regex) {
        super(name, placement, argumentable, regex);
    }

    @Override
    protected String parseValue(String textForm) throws InvalidCommandArgumentException, IllegalCommandArgumentException {
        return textForm;
    }

    @Override
    protected boolean checkValue(String value) throws IllegalCommandArgumentException {
        if (value==null) throw new IllegalCommandArgumentException(name, "blank input");
        if (value.contains("\'") || value.contains("\"") || value.contains(" ") ||
                value.contains("(") || value.contains(")") || value.contains("{") ||
                value.contains("}") || value.contains("<") || value.contains(">") ||
                value.contains(".") || value.contains("") ||
                value.contains("\n")) throw new IllegalCommandArgumentException(name, comment);
        return true;
    }
}
