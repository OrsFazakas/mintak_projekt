package edu.bbte.protrack.model.specifications;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.Task;

//Specifikáció a késésben lévő (overdue) feladatokhoz.
public class OverdueSpecification implements Specification {
    @Override
    public boolean isSatisfiedBy(ProjectComponent component) {
        if (component instanceof Task) {
            Task task = (Task) component;
            return task.isOverdue();
        }
        return false;
    }
}
