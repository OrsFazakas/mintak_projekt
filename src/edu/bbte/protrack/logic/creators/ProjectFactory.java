package edu.bbte.protrack.logic.creators;

import edu.bbte.protrack.model.entities.Task;
import edu.bbte.protrack.model.entities.Employee;

/**
 * Absztrakt gyár interfész, amely különböző típusú projekt-elemek
 * létrehozásáért felel.
 */
public interface ProjectFactory {
    Task createTask(String name, double cost);
    Employee createDefaultEmployee(String name);
}