package edu.bbte.protrack.view.main;

import edu.bbte.protrack.commands.CommandManager;
import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.observer.ProjectObservable;

/**
 * Összeköti a nézetet a parancsokkal és az eseménykezeléssel.
 */
public class MainController extends ProjectObservable {
    private final CommandManager commandManager;
    private final TaskGroup root;

    public MainController(TaskGroup root, CommandManager commandManager) {
        this.root = root;
        this.commandManager = commandManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public TaskGroup getRoot() {
        return root;
    }
}