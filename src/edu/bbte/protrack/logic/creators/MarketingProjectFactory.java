package edu.bbte.protrack.logic.creators;

import edu.bbte.protrack.model.entities.Task;
import edu.bbte.protrack.model.entities.Employee;

/**
 * Abstract Factory implementáció Marketing projektekhez.
 */
public class MarketingProjectFactory implements ProjectFactory {
    @Override
    public Task createTask(String name, double cost) {
        return new Task("[MKT] " + name, cost, "Marketing Manager");
    }

    @Override
    public Employee createDefaultEmployee(String name) {
        return new Employee(name);
    }
}
