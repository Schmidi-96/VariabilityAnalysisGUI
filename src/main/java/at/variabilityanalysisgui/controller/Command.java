/***
 The MIT License (MIT)

 Copyright (c) 2025 Michael Schmidhammer
 */

package at.variabilityanalysisgui.controller;

public interface Command {
    void execute();
    void undo();
    default void redo() {
        execute();
    }
}
