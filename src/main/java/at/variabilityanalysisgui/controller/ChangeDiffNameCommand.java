/***
 The MIT License (MIT)

 Copyright (c) 2025 Michael Schmidhammer
 */

package at.variabilityanalysisgui.controller;

import javafx.beans.property.StringProperty;

public class ChangeDiffNameCommand implements Command {
    private final StringProperty targetProperty;
    private final String oldValue;
    private final String newValue;

    public ChangeDiffNameCommand(StringProperty targetProperty, String oldValue, String newValue) {
        this.targetProperty = targetProperty;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void execute() {
        targetProperty.set(newValue);
        System.out.println("ChangeDiffNameCommand executed: " + targetProperty.get());
    }

    @Override
    public void undo() {
        targetProperty.set(oldValue);
    }
}
