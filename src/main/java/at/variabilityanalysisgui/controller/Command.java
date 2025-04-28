package at.variabilityanalysisgui.controller;

public interface Command {
    void execute();
    void undo();
    default void redo() {
        execute();
    }
}
