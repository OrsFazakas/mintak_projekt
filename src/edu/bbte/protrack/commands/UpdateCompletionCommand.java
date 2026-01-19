package edu.bbte.protrack.commands;

import edu.bbte.protrack.model.entities.Task;

public class UpdateCompletionCommand implements Command {
    private final Task task;
    private final int newValue;
    private final int oldValue;

    public UpdateCompletionCommand(Task task, int newValue) {
        this.task = task;
        this.newValue = newValue;
        this.oldValue = task.getCompletionPercentage();
    }

    @Override
    public void execute() {
        task.setCompletion(newValue);
    }

    @Override
    public void undo() {
        task.setCompletion(oldValue);
    }

    @Override
    public String getName() {
        return "Haladás módosítása itt: " + task.getName();
    }
}