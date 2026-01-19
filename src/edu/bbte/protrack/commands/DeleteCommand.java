package edu.bbte.protrack.commands;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.TaskGroup;

/**
 * Command minta: Elem törlése a projektből.
 * Támogatja az undo műveletet (visszaállítja a törölt elemet).
 */
public class DeleteCommand implements Command {
    private final TaskGroup parent;
    private final ProjectComponent componentToDelete;
    private int originalIndex;

    public DeleteCommand(TaskGroup parent, ProjectComponent component) {
        this.parent = parent;
        this.componentToDelete = component;
        this.originalIndex = -1;
    }

    @Override
    public void execute() {
        // Megőrizzük az eredeti pozíciót az undo-hoz
        java.util.List<ProjectComponent> children = parent.getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i) == componentToDelete) {
                originalIndex = i;
                break;
            }
        }
        parent.removeComponent(componentToDelete);
    }

    @Override
    public void undo() {
        // Visszatesszük az eredeti pozícióba
        if (originalIndex >= 0) {
            parent.addComponentAt(componentToDelete, originalIndex);
        } else {
            parent.addComponent(componentToDelete);
        }
    }

    @Override
    public String getName() {
        return "Törlés: " + componentToDelete.getName();
    }
}
