package edu.bbte.protrack.logic.creators;

import edu.bbte.protrack.model.entities.Task;
import edu.bbte.protrack.model.entities.Employee;

/**
 * Abstract Factory implementáció Sales (Értékesítés) projektekhez.
 */
public class SalesProjectFactory implements ProjectFactory {
    @Override
    public Task createTask(String name, double cost) {
        return new Task("[SALES] " + name, cost, "Sales Representative");
    }

    @Override
    public Employee createDefaultEmployee(String name) {
        return new Employee(name, "Account Manager", 40.0);
    }
}
