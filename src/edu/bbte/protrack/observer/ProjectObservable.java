package edu.bbte.protrack.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * A megfigyelhető alany (Subject).
 * Kezeli a regisztrált megfigyelőket és publikus metódust biztosít az értesítéshez.
 */
public class ProjectObservable {
    private final List<ProjectObserver> observers = new ArrayList<>();

    public void addObserver(ProjectObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(ProjectObserver observer) {
        observers.remove(observer);
    }

    /**
     * Ez az a metódus, amit a MainWindow hiányolt.
     * Publikussá tesszük, hogy a Controller vagy a Main ablak kívülről is meghívhassa.
     */
    public void trigger(ProjectEvent event) {
        for (ProjectObserver observer : observers) {
            observer.onProjectChanged(event);
        }
    }
}