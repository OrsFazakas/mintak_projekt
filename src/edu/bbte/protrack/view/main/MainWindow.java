package edu.bbte.protrack.view.main;

import edu.bbte.protrack.commands.AddTaskCommand;
import edu.bbte.protrack.commands.Command;
import edu.bbte.protrack.commands.CommandManager;
import edu.bbte.protrack.model.entities.Task;
import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.observer.ProjectEvent;
import edu.bbte.protrack.observer.ProjectObservable;
import edu.bbte.protrack.observer.ProjectObserver;
import edu.bbte.protrack.persistence.ProjectPersistence;
import edu.bbte.protrack.view.components.ProjectTreePanel;
import edu.bbte.protrack.view.components.StatisticsPanel;
import edu.bbte.protrack.view.dialogs.AddTaskDialog;
import edu.bbte.protrack.view.dialogs.ProjectWizardDialog;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Az alkalmazás főablaka.
 */
public class MainWindow extends JFrame implements ProjectObserver {
    private final CommandManager commandManager;
    private TaskGroup rootProject;
    private final ProjectObservable eventManager;
    private final ProjectPersistence persistence;

    private ProjectTreePanel treePanel;
    private StatisticsPanel statsPanel;
    private JLabel statusLabel;
    private JPanel detailsPanel;

    public MainWindow() {
        // Logikai motor és adatmodell inicializálása
        this.commandManager = new CommandManager();
        this.eventManager = new ProjectObservable();
        this.persistence = new ProjectPersistence();

        // Próbáljuk betölteni a korábban mentett projektet
        TaskGroup loaded = persistence.load();
        if (loaded != null) {
            this.rootProject = loaded;
        } else {
            this.rootProject = new TaskGroup("Saját Projekt Portfólió");
        }

        setTitle("ProTrack Architect - ERP Light");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1100, 700);
        setLayout(new BorderLayout());

        initComponents();
        initMenu();

        // Feliratkozás az eseményekre
        eventManager.addObserver(this);
        eventManager.addObserver(statsPanel);

        // Bezáráskor mentés kérdezése
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        setLocationRelativeTo(null);
    }

    private void initComponents() {
        treePanel = new ProjectTreePanel(rootProject);
        add(new JScrollPane(treePanel), BorderLayout.WEST);

        detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.add(new JLabel("Válasszon elemet a fában a műveletekhez."));
        add(detailsPanel, BorderLayout.CENTER);

        statsPanel = new StatisticsPanel(rootProject);
        add(statsPanel, BorderLayout.EAST);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Rendszer kész.");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);

        treePanel.getTree().addTreeSelectionListener(e -> {
            // 1. Megszerezzük a kijelölt csomópontot
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePanel.getTree().getLastSelectedPathComponent();
            if (node == null)
                return;

            // 2. Kinyerjük belőle a mi objektumunkat (Task vagy TaskGroup)
            Object userObject = node.getUserObject();

            // 3. Frissítjük a középső panelt és a statisztikákat
            showDetails(userObject);
            if (userObject instanceof edu.bbte.protrack.model.entities.ProjectComponent) {
                statsPanel.setSelectedComponent((edu.bbte.protrack.model.entities.ProjectComponent) userObject);
            }
        });
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu projectMenu = new JMenu("Projekt");
        JMenuItem newProjectItem = new JMenuItem("Új projekt");
        newProjectItem.addActionListener(e -> handleNewProject());
        JMenuItem addTaskItem = new JMenuItem("Új feladat");
        addTaskItem.addActionListener(e -> handleAddTask());

        projectMenu.add(newProjectItem);
        projectMenu.add(addTaskItem);
        projectMenu.addSeparator();

        JMenuItem saveItem = new JMenuItem("Mentés");
        saveItem.addActionListener(e -> handleSave());
        projectMenu.add(saveItem);

        JMenuItem loadItem = new JMenuItem("Betöltés");
        loadItem.addActionListener(e -> handleLoad());
        projectMenu.add(loadItem);

        projectMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Kilépés");
        exitItem.addActionListener(e -> handleExit());
        projectMenu.add(exitItem);

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
        TaskGroup targetGroup = getSelectedTaskGroup();
        if (targetGroup == null) {
            JOptionPane.showMessageDialog(this,
                    "Válasszon ki egy mappát/projektet a fában, ahová az új fázist szeretné hozzáadni!",
                    "Nincs kiválasztott elem",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        ProjectWizardDialog wizard = new ProjectWizardDialog(this);
        wizard.setVisible(true);
        TaskGroup newGroup = wizard.getResult();
        if (newGroup != null) {
            executeProjectCommand(new AddTaskCommand(targetGroup, newGroup));
        }
    }

    private void handleAddTask() {
        TaskGroup targetGroup = getSelectedTaskGroup();
        if (targetGroup == null) {
            JOptionPane.showMessageDialog(this,
                    "Válasszon ki egy mappát/projektet a fában, ahová az új feladatot szeretné hozzáadni!",
                    "Nincs kiválasztott elem",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        AddTaskDialog dialog = new AddTaskDialog(this);
        dialog.setVisible(true);
        Task newTask = dialog.getResultTask();
        if (newTask != null) {
            executeProjectCommand(new AddTaskCommand(targetGroup, newTask));
        }
    }

    /**
     * Visszaadja a fában kiválasztott TaskGroup-ot.
     * Ha Task van kiválasztva, null-t ad vissza (nem lehet oda hozzáadni).
     * Ha nincs kijelölés, a rootProject-et adja vissza.
     */
    private TaskGroup getSelectedTaskGroup() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePanel.getTree().getLastSelectedPathComponent();
        if (node == null) {
            // Ha nincs kijelölés, használjuk a gyökeret
            return rootProject;
        }

        Object userObject = node.getUserObject();
        if (userObject instanceof TaskGroup) {
            return (TaskGroup) userObject;
        }
        // Ha Task van kiválasztva, a szülő TaskGroup-ot keressük
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent != null && parent.getUserObject() instanceof TaskGroup) {
            return (TaskGroup) parent.getUserObject();
        }
        return rootProject;
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

    private void showDetails(Object obj) {
        detailsPanel.removeAll();
        detailsPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 10, 5, 10);
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        if (obj instanceof Task) {
            Task task = (Task) obj;

            // Cím
            JLabel titleLabel = new JLabel("📋 Feladat szerkesztése");
            titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 16f));
            detailsPanel.add(titleLabel, gbc);

            // Név
            gbc.gridy++;
            detailsPanel.add(new JLabel("Név: " + task.getName()), gbc);

            // Költség
            gbc.gridy++;
            detailsPanel.add(new JLabel(String.format("Költség: %.0f €", task.getBaseCost())), gbc);

            // Felelős
            gbc.gridy++;
            detailsPanel.add(new JLabel("Felelős: " + task.getAssignedEmployee()), gbc);

            // Haladás címke a százalékkal
            gbc.gridy++;
            gbc.gridwidth = 1;
            JLabel progressLabel = new JLabel("Haladás: " + task.getCompletionPercentage() + "%");
            progressLabel.setFont(progressLabel.getFont().deriveFont(java.awt.Font.BOLD));
            detailsPanel.add(progressLabel, gbc);

            // Állapot ikon
            gbc.gridx = 1;
            String status = task.isCompleted() ? "✅ KÉSZ" : "⏳ Folyamatban";
            JLabel statusLabel2 = new JLabel(status);
            statusLabel2.setForeground(task.isCompleted() ? new java.awt.Color(0, 150, 0) : java.awt.Color.ORANGE);
            detailsPanel.add(statusLabel2, gbc);

            // Csúszka
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.gridwidth = 2;
            JSlider progress = new JSlider(0, 100, task.getCompletionPercentage());
            progress.setMajorTickSpacing(25);
            progress.setMinorTickSpacing(5);
            progress.setPaintTicks(true);
            progress.setPaintLabels(true);
            progress.addChangeListener(e -> {
                progressLabel.setText("Haladás: " + progress.getValue() + "%");
                if (!progress.getValueIsAdjusting()) {
                    task.setCompletion(progress.getValue());
                    eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
                    showDetails(task); // Frissítsük a panelt az állapot ikonhoz
                }
            });
            detailsPanel.add(progress, gbc);

            // Gombok
            gbc.gridy++;
            JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

            JButton completeBtn = new JButton("✅ Befejezés (100%)");
            completeBtn.setEnabled(!task.isCompleted());
            completeBtn.addActionListener(e -> {
                task.setCompletion(100);
                eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
                showDetails(task);
            });
            buttonPanel.add(completeBtn);

            JButton resetBtn = new JButton("🔄 Visszaállítás (0%)");
            resetBtn.addActionListener(e -> {
                task.setCompletion(0);
                eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
                showDetails(task);
            });
            buttonPanel.add(resetBtn);

            detailsPanel.add(buttonPanel, gbc);

        } else if (obj instanceof TaskGroup) {
            TaskGroup group = (TaskGroup) obj;

            // Cím
            JLabel titleLabel = new JLabel("📁 Fázis részletei");
            titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 16f));
            detailsPanel.add(titleLabel, gbc);

            gbc.gridy++;
            detailsPanel.add(new JLabel("Név: " + group.getName()), gbc);

            gbc.gridy++;
            detailsPanel.add(new JLabel("Elemek száma: " + group.getChildren().size()), gbc);

            gbc.gridy++;
            detailsPanel.add(new JLabel(String.format("Összköltség: %.0f €", group.getCalculateTotalCost())), gbc);

            gbc.gridy++;
            detailsPanel.add(new JLabel("Átlagos haladás: " + group.getCompletionPercentage() + "%"), gbc);
        }

        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    /**
     * Menti a projektet fájlba - fájlválasztóval.
     */
    private void handleSave() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Projekt mentése");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "ProTrack projekt (*.protrack)", "protrack"));
        fileChooser.setSelectedFile(new java.io.File("projekt.protrack"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            // Ha nem .protrack kiterjesztésű, hozzáadjuk
            if (!file.getName().toLowerCase().endsWith(".protrack")) {
                file = new java.io.File(file.getAbsolutePath() + ".protrack");
            }

            ProjectPersistence filePersistence = new ProjectPersistence(file.getAbsolutePath());
            if (filePersistence.save(rootProject)) {
                statusLabel.setText("Projekt mentve: " + file.getName());
                JOptionPane.showMessageDialog(this,
                        "A projekt sikeresen mentve lett.\n" + file.getAbsolutePath(),
                        "Mentés sikeres",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Hiba történt a mentés során!",
                        "Mentési hiba",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Betölti a projektet fájlból - fájlválasztóval.
     */
    private void handleLoad() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Projekt betöltése");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "ProTrack projekt (*.protrack)", "protrack"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();

            int confirm = JOptionPane.showConfirmDialog(this,
                    "A jelenlegi projekt felülíródik. Folytatja?\n" + file.getName(),
                    "Betöltés megerősítése",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                ProjectPersistence filePersistence = new ProjectPersistence(file.getAbsolutePath());
                TaskGroup loaded = filePersistence.load();
                if (loaded != null) {
                    this.rootProject = loaded;
                    treePanel.updateRootProject(rootProject);
                    statsPanel.updateRootProject(rootProject);
                    eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, rootProject));
                    statusLabel.setText("Projekt betöltve: " + file.getName());
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Hiba történt a betöltés során!",
                            "Betöltési hiba",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    /**
     * Kilépés előtt megkérdezi, hogy menteni szeretné-e.
     */
    private void handleExit() {
        int option = JOptionPane.showConfirmDialog(this,
                "Szeretné menteni a projektet kilépés előtt?",
                "Kilépés",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            handleSave();
            System.exit(0);
        } else if (option == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
        // CANCEL esetén nem csinálunk semmit, marad az ablak nyitva
    }

}