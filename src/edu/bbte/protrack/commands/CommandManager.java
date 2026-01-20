package edu.bbte.protrack.commands;

import java.util.Stack;

//A parancsok végrehajtásáért és a történet kezeléséért felelős osztály.
public class CommandManager {
    private final Stack<Command> undoStack = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();

    //Végrehajt egy parancsot és elmenti a történetbe.
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // Új műveletnél a redo történet elvész
    }

    //Visszavonja az utolsó műveletet.
    public void undo() {
        if (!undoStack.isEmpty()) {
            Command command = undoStack.pop();
            command.undo();
            redoStack.push(command);
        }
    }

    //Újra végrehajtja a legutóbb visszavont műveletet.
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}