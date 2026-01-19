package edu.bbte.protrack.logic.services;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.model.specifications.Specification;
import java.util.ArrayList;
import java.util.List;

/**
 * Szolgáltatás a projektek elemzésére specifikációk alapján.
 */
public class ProjectStatisticsService {

    /**
     * Kiszűri a csoportból azokat az elemeket, amelyek megfelelnek a megadott szabálynak.
     */
    public List<ProjectComponent> filterComponents(TaskGroup group, Specification spec) {
        List<ProjectComponent> result = new ArrayList<>();
        for (ProjectComponent child : group.getChildren()) {
            if (spec.isSatisfiedBy(child)) {
                result.add(child);
            }
            // Ha a gyerek is egy csoport, rekurzívan is mehetnénk tovább,
            // de maradjunk az egyszerű listázásnál.
        }
        return result;
    }
}