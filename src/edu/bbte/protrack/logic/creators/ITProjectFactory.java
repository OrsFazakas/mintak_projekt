package edu.bbte.protrack.logic.creators;

import edu.bbte.protrack.model.entities.Task;
import edu.bbte.protrack.model.entities.Employee;

public class ITProjectFactory implements ProjectFactory {
    @Override
    public Task createTask(String name, double cost) {
        // IT projektekben alapértelmezetten egy "Fejlesztő" van hozzárendelve
        return new Task("[IT] " + name, cost, "Unassigned Developer");
    }

    @Override
    public Employee createDefaultEmployee(String name) {
        return new Employee(name, "Software Engineer", 50.0);
    }
}