package edu.bbte.protrack.observer;

import edu.bbte.protrack.model.entities.ProjectComponent;

//Az esemény típusa és a hozzá tartozó adat.
public class ProjectEvent {
    public enum EventType { COMPONENT_ADDED, COMPONENT_REMOVED, DATA_CHANGED }

    private final EventType type;
    private final ProjectComponent source;

    public ProjectEvent(EventType type, ProjectComponent source) {
        this.type = type;
        this.source = source;
    }

    public EventType getType() { return type; }
    public ProjectComponent getSource() { return source; }
}