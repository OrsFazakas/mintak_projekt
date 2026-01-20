package edu.bbte.protrack.logic.creators;

import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.model.entities.ProjectComponent;

//Builder minta a komplex projektek (TaskGroup) összeállításához.
public class ProjectBuilder {
    private String projectName;
    private TaskGroup rootGroup;

    public ProjectBuilder(String name) {
        this.projectName = name;
        this.rootGroup = new TaskGroup(name);
    }

    public ProjectBuilder addInitialComponent(ProjectComponent component) {
        this.rootGroup.addComponent(component);
        return this;
    }


    //Véglegesíti az építést. Itt végezhető el a végső validáció is.
    public TaskGroup build() {
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalStateException("A projekt neve nem lehet üres!");
        }
        return rootGroup;
    }
}