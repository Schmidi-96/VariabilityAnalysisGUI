/***
 The MIT License (MIT)

 Copyright (c) 2025 Michael Schmidhammer
 */

package at.variabilityanalysisgui.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Stack;

public class UndoManager {
    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    // Properties for UI binding
    private final SimpleBooleanProperty canUndo = new SimpleBooleanProperty(false);
    private final SimpleBooleanProperty canRedo = new SimpleBooleanProperty(false);

    public BooleanProperty canUndoProperty() { return canUndo; }
    public BooleanProperty canRedoProperty() { return canRedo; }
    public boolean getCanUndo() { return canUndo.get(); }
    public boolean getCanRedo() { return canRedo.get(); }

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
        updateProperties();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
            updateProperties();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.redo();
            undoStack.push(command);
            updateProperties();
        }
    }

    private void updateProperties() {
        canUndo.set(!undoStack.isEmpty());
        canRedo.set(!redoStack.isEmpty());
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
        updateProperties();
    }
}
