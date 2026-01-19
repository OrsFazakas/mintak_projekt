package edu.bbte.protrack.view.main;

import edu.bbte.protrack.commands.AddTaskCommand;
import edu.bbte.protrack.commands.Command;
import edu.bbte.protrack.commands.CommandManager;
import edu.bbte.protrack.model.entities.Task;
import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.observer.ProjectEvent;
import edu.bbte.protrack.observer.ProjectObservable;
import edu.bbte.protrack.observer.ProjectObserver;
import edu.bbte.protrack.view.components.ProjectTreePanel;
import edu.bbte.protrack.view.components.StatisticsPanel;
import edu.bbte.protrack.view.dialogs.AddTaskDialog;
import edu.bbte.protrack.view.dialogs.ProjectWizardDialog;

import javax.swing.*;
import java.awt.*;

/**
 * Az alkalmazás főablaka.
 */
public class MainWindow extends JFrame implements ProjectObserver {
    private final CommandManager commandManager;
    private final TaskGroup rootProject;
    private final ProjectObservable eventManager; // Most már tiszta példányosítás lesz

    private ProjectTreePanel treePanel;
    private StatisticsPanel statsPanel;
    private JLabel statusLabel;

    public MainWindow() {
        // Logikai motor és adatmodell inicializálása
        this.commandManager = new CommandManager();
        this.rootProject = new TaskGroup("Saját Projekt Portfólió");

        // JAVÍTÁS: Nincs többé névtelen belső osztály hiba
        this.eventManager = new ProjectObservable();

        setTitle("ProTrack Architect - ERP Light");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLayout(new BorderLayout());

        initComponents();
        initMenu();

        // Feliratkozás az eseményekre
        eventManager.addObserver(this);
        eventManager.addObserver(statsPanel);

        setLocationRelativeTo(null);
    }

    private void initComponents() {
        treePanel = new ProjectTreePanel(rootProject);
        add(new JScrollPane(treePanel), BorderLayout.WEST);

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.add(new JLabel("Válasszon elemet a fában a műveletekhez."));
        add(detailsPanel, BorderLayout.CENTER);

        statsPanel = new StatisticsPanel(rootProject);
        add(statsPanel, BorderLayout.EAST);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Rendszer kész.");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu projectMenu = new JMenu("Projekt");
        JMenuItem newProjectItem = new JMenuItem("Új Projekt fázis (Builder)...");
        newProjectItem.addActionListener(e -> handleNewProject());
        JMenuItem addTaskItem = new JMenuItem("Új Feladat (Abstract Factory)...");
        addTaskItem.addActionListener(e -> handleAddTask());

        projectMenu.add(newProjectItem);
        projectMenu.add(addTaskItem);

        JMenu editMenu = new JMenu("Szerkesztés");
        JMenuItem undoItem = new JMenuItem("Visszavonás (Undo)");
        undoItem.addActionListener(e -> {
            if (commandManager.canUndo()) {
                commandManager.undo();
                eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, rootProject));
            }
        });

        JMenuItem redoItem = new JMenuItem("Újra (Redo)");
        redoItem.addActionListener(e -> {
            if (commandManager.canRedo()) {
                commandManager.redo();
                eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, rootProject));
            }
        });

        editMenu.add(undoItem);
        editMenu.add(redoItem);

        menuBar.add(projectMenu);
        menuBar.add(editMenu);
        setJMenuBar(menuBar);
    }

    private void handleNewProject() {
        ProjectWizardDialog wizard = new ProjectWizardDialog(this);
        wizard.setVisible(true);
        TaskGroup newGroup = wizard.getResult();
        if (newGroup != null) {
            executeProjectCommand(new AddTaskCommand(rootProject, newGroup));
        }
    }

    private void handleAddTask() {
        AddTaskDialog dialog = new AddTaskDialog(this);
        dialog.setVisible(true);
        Task newTask = dialog.getResultTask();
        if (newTask != null) {
            executeProjectCommand(new AddTaskCommand(rootProject, newTask));
        }
    }

    private void executeProjectCommand(Command cmd) {
        commandManager.executeCommand(cmd);
        statusLabel.setText("Utolsó művelet: " + cmd.getName());
        eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.COMPONENT_ADDED, rootProject));
    }

    @Override
    public void onProjectChanged(ProjectEvent event) {
        treePanel.refreshTree();
        statusLabel.setText(String.format("Portfólió érték: %.2f €", rootProject.getCalculateTotalCost()));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}