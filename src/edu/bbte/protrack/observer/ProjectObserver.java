package edu.bbte.protrack.observer;

//Observer interfész.
public interface ProjectObserver {
    //Ezt a metódust hívja meg a megfigyelt objektum, ha változás történik.
    void onProjectChanged(ProjectEvent event);
}