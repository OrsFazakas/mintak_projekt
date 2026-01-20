package edu.bbte.protrack.model.specifications;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.Task;

//Specifikáció a hamarosan lejáró (2 napon belül) feladatokhoz.
public class DueSoonSpecification implements Specification {
    @Override
    public boolean isSatisfiedBy(ProjectComponent component) {
        if (component instanceof Task) {
            Task task = (Task) component;
            return task.isDueSoon();
        }
        return false;
    }
}
