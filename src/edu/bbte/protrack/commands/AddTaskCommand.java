package edu.bbte.protrack.commands;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.TaskGroup;

public class AddTaskCommand implements Command {
    private final TaskGroup targetGroup;
    private final ProjectComponent componentToAdd;

    public AddTaskCommand(TaskGroup targetGroup, ProjectComponent componentToAdd) {
        this.targetGroup = targetGroup;
        this.componentToAdd = componentToAdd;
    }

    @Override
    public void execute() {
        targetGroup.addComponent(componentToAdd);
    }

    @Override
    public void undo() {
        targetGroup.removeComponent(componentToAdd);
    }

    @Override
    public String getName() {
        return "Elem hozzáadása: " + componentToAdd.getName();
    }
}