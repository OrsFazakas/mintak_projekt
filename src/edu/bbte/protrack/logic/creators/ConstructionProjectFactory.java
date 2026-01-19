package edu.bbte.protrack.logic.creators;

import edu.bbte.protrack.model.entities.Task;
import edu.bbte.protrack.model.entities.Employee;

public class ConstructionProjectFactory implements ProjectFactory {
    @Override
    public Task createTask(String name, double cost) {
        return new Task("[CONST] " + name, cost, "Site Worker");
    }

    @Override
    public Employee createDefaultEmployee(String name) {
        return new Employee(name, "Foreman", 30.0);
    }
}