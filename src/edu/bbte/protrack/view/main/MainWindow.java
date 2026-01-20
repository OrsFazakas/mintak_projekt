package edu.bbte.protrack.view.main;

import edu.bbte.protrack.commands.AddTaskCommand;
import edu.bbte.protrack.commands.DeleteCommand;
import edu.bbte.protrack.commands.UpdateCompletionCommand;
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
import edu.bbte.protrack.view.dialogs.EditTaskDialog;
import edu.bbte.protrack.view.dialogs.ProjectWizardDialog;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.KeyEvent;
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
    private JProgressBar progressBar;

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
        // Kereső toolbar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Keresés és szűrés"));

        searchPanel.add(new JLabel("🔍 Keresés:"));
        JTextField searchField = new JTextField(15);
        searchPanel.add(searchField);

        searchPanel.add(new JLabel("  Szűrés:"));
        JComboBox<String> filterCombo = new JComboBox<>(new String[] {
                "Összes", "Folyamatban", "Befejezett", "Késésben", "Magas prioritás"
        });
        searchPanel.add(filterCombo);

        JButton searchButton = new JButton("Keresés");
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim().toLowerCase();
            String filter = (String) filterCombo.getSelectedItem();
            highlightMatchingTasks(searchText, filter);
        });
        searchPanel.add(searchButton);

        JButton clearButton = new JButton("Törlés");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            filterCombo.setSelectedIndex(0);
            treePanel.refreshTree();
            statusLabel.setText("Szűrés törölve.");
        });
        searchPanel.add(clearButton);

        add(searchPanel, BorderLayout.NORTH);

        treePanel = new ProjectTreePanel(rootProject);
        add(new JScrollPane(treePanel), BorderLayout.WEST);

        detailsPanel = new JPanel(new GridBagLayout());
        showWelcomePanel(); // Üdvözlő panel megjelenítése
        add(detailsPanel, BorderLayout.CENTER);

        statsPanel = new StatisticsPanel(rootProject);
        add(statsPanel, BorderLayout.EAST);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        statusLabel = new JLabel("Rendszer kész.");
        statusBar.add(statusLabel);

        statusBar.add(new JLabel("  |  Haladás:"));
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(rootProject.getCompletionPercentage());
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(200, 20));
        statusBar.add(progressBar);

        add(statusBar, BorderLayout.SOUTH);

        // Fa kijelölés eseménykezelő
        treePanel.getTree().addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePanel.getTree().getLastSelectedPathComponent();
            if (node == null)
                return;

            Object userObject = node.getUserObject();
            showDetails(userObject);
            if (userObject instanceof edu.bbte.protrack.model.entities.ProjectComponent) {
                statsPanel.setSelectedComponent((edu.bbte.protrack.model.entities.ProjectComponent) userObject);
            }
        });

        // Dupla-kattintás szerkesztéshez
        treePanel.getTree().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePanel.getTree()
                            .getLastSelectedPathComponent();
                    if (node != null) {
                        Object userObject = node.getUserObject();
                        if (userObject instanceof Task) {
                            handleEditTask((Task) userObject);
                        } else if (userObject instanceof TaskGroup && userObject != rootProject) {
                            handleRenameProject((TaskGroup) userObject);
                        }
                    }
                }
            }
        });

        // Jobb-klikk kontextus menü
        setupContextMenu();
    }

    /**
     * Üdvözlő panel megjelenítése induláskor.
     */
    private void showWelcomePanel() {
        detailsPanel.removeAll();
        detailsPanel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 20, 10, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel welcomeLabel = new JLabel("🎯 Üdvözöljük a ProTrack-ben!");
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(java.awt.Font.BOLD, 24f));
        detailsPanel.add(welcomeLabel, gbc);

        gbc.gridy++;
        JLabel subtitleLabel = new JLabel("Projektmenedzsment alkalmazás tervezési mintákkal.");
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(java.awt.Font.ITALIC, 14f));
        detailsPanel.add(subtitleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(30, 20, 5, 20);
        detailsPanel.add(new JLabel("📌 Gyors tippek:"), gbc);

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(5, 40, 5, 20);
        detailsPanel.add(new JLabel("• Ctrl+N - Új feladat létrehozása"), gbc);
        gbc.gridy++;
        detailsPanel.add(new JLabel("• Ctrl+S - Projekt mentése"), gbc);
        gbc.gridy++;
        detailsPanel.add(new JLabel("• Ctrl+Z - Visszavonás"), gbc);
        gbc.gridy++;
        detailsPanel.add(new JLabel("• Delete - Kijelölt elem törlése"), gbc);
        gbc.gridy++;
        detailsPanel.add(new JLabel("• Jobb-klikk - Gyors műveletek"), gbc);

        gbc.gridy++;
        gbc.insets = new java.awt.Insets(30, 20, 10, 20);
        JLabel selectLabel = new JLabel("👈 Válasszon egy elemet a bal oldali fában a kezdéshez!");
        selectLabel.setForeground(new java.awt.Color(100, 100, 100));
        detailsPanel.add(selectLabel, gbc);

        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    /**
     * Jobb-klikk kontextus menü beállítása.
     */
    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem completeItem = new JMenuItem("✅ Befejezés (100%)");
        completeItem.addActionListener(e -> {
            Task task = getSelectedTask();
            if (task != null) {
                task.setCompletion(100);
                eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
                showDetails(task);
            }
        });

        JMenuItem resetItem = new JMenuItem("🔄 Visszaállítás (0%)");
        resetItem.addActionListener(e -> {
            Task task = getSelectedTask();
            if (task != null) {
                task.setCompletion(0);
                eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
                showDetails(task);
            }
        });

        JMenu priorityMenu = new JMenu("🎯 Prioritás beállítása");
        JMenuItem highPriority = new JMenuItem("🔴 Magas");
        highPriority.addActionListener(e -> setPriority(Task.Priority.HIGH));
        JMenuItem mediumPriority = new JMenuItem("🟡 Közepes");
        mediumPriority.addActionListener(e -> setPriority(Task.Priority.MEDIUM));
        JMenuItem lowPriority = new JMenuItem("🟢 Alacsony");
        lowPriority.addActionListener(e -> setPriority(Task.Priority.LOW));
        priorityMenu.add(highPriority);
        priorityMenu.add(mediumPriority);
        priorityMenu.add(lowPriority);

        JMenuItem deleteItem = new JMenuItem("🗑️ Törlés");
        deleteItem.addActionListener(e -> handleDelete());

        JMenuItem newTaskItem = new JMenuItem("➕ Új feladat ide");
        newTaskItem.addActionListener(e -> handleAddTask());

        contextMenu.add(completeItem);
        contextMenu.add(resetItem);
        contextMenu.addSeparator();
        contextMenu.add(priorityMenu);
        contextMenu.addSeparator();
        contextMenu.add(newTaskItem);
        contextMenu.add(deleteItem);

        treePanel.getTree().setComponentPopupMenu(contextMenu);
    }

    /**
     * Visszaadja a kiválasztott Task-ot, vagy null-t ha nincs.
     */
    private Task getSelectedTask() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePanel.getTree().getLastSelectedPathComponent();
        if (node != null && node.getUserObject() instanceof Task) {
            return (Task) node.getUserObject();
        }
        return null;
    }

    /**
     * Beállítja a kiválasztott task prioritását.
     */
    private void setPriority(Task.Priority priority) {
        Task task = getSelectedTask();
        if (task != null) {
            task.setPriority(priority);
            eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
            showDetails(task);
        }
    }

    /**
     * Feladat szerkesztése dialógussal.
     */
    private void handleEditTask(Task task) {
        EditTaskDialog dialog = new EditTaskDialog(this, task);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
            showDetails(task);
        }
    }

    /**
     * Projekt/mappa átnevezése.
     */
    private void handleRenameProject(TaskGroup group) {
        String newName = JOptionPane.showInputDialog(this,
                "Adja meg az új nevet:",
                group.getName());

        if (newName != null && !newName.trim().isEmpty()) {
            group.setName(newName.trim());
            eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, group));
            showDetails(group);
        }
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu projectMenu = new JMenu("Projekt");
        JMenuItem newProjectItem = new JMenuItem("Új projekt");
        newProjectItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        newProjectItem.addActionListener(e -> handleNewProject());
        JMenuItem addTaskItem = new JMenuItem("Új feladat");
        addTaskItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        addTaskItem.addActionListener(e -> handleAddTask());

        projectMenu.add(newProjectItem);
        projectMenu.add(addTaskItem);
        projectMenu.addSeparator();

        JMenuItem saveItem = new JMenuItem("Mentés");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> handleSave());
        projectMenu.add(saveItem);

        JMenuItem loadItem = new JMenuItem("Betöltés");
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        loadItem.addActionListener(e -> handleLoad());
        projectMenu.add(loadItem);

        projectMenu.addSeparator();
        JMenuItem exitItem = new JMenuItem("Kilépés");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        exitItem.addActionListener(e -> handleExit());
        projectMenu.add(exitItem);

        JMenu editMenu = new JMenu("Szerkesztés");
        JMenuItem undoItem = new JMenuItem("Visszavonás");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> {
            if (commandManager.canUndo()) {
                commandManager.undo();
                eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, rootProject));
            }
        });

        JMenuItem redoItem = new JMenuItem("Újra");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> {
            if (commandManager.canRedo()) {
                commandManager.redo();
                eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, rootProject));
            }
        });

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();

        JMenuItem deleteItem = new JMenuItem("Törlés");
        deleteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        deleteItem.addActionListener(e -> handleDelete());
        editMenu.add(deleteItem);

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

    /**
     * Törli a kiválasztott elemet a projektből.
     */
    private void handleDelete() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePanel.getTree().getLastSelectedPathComponent();
        if (node == null || node.getUserObject() == rootProject) {
            JOptionPane.showMessageDialog(this,
                    "Válasszon ki egy elemet a törléshez!\n(A gyökér projekt nem törölhető)",
                    "Törlés",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object userObject = node.getUserObject();
        if (!(userObject instanceof edu.bbte.protrack.model.entities.ProjectComponent)) {
            return;
        }

        edu.bbte.protrack.model.entities.ProjectComponent component = (edu.bbte.protrack.model.entities.ProjectComponent) userObject;

        // Megkeressük a szülő TaskGroup-ot
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
        if (parentNode == null || !(parentNode.getUserObject() instanceof TaskGroup)) {
            JOptionPane.showMessageDialog(this, "Nem sikerült megtalálni a szülő elemet.");
            return;
        }

        TaskGroup parent = (TaskGroup) parentNode.getUserObject();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Biztosan törölni szeretné: " + component.getName() + "?",
                "Törlés megerősítése",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            DeleteCommand deleteCmd = new DeleteCommand(parent, component);
            commandManager.executeCommand(deleteCmd);
            statusLabel.setText("Törölve: " + component.getName());
            eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, rootProject));
        }
    }

    /**
     * Keresés és szűrés a projektben.
     */
    private void highlightMatchingTasks(String searchText, String filter) {
        java.util.List<Task> matchingTasks = new java.util.ArrayList<>();
        findMatchingTasks(rootProject, searchText, filter, matchingTasks);

        if (matchingTasks.isEmpty()) {
            statusLabel.setText("Nincs találat a keresési feltételeknek megfelelően.");
        } else {
            statusLabel.setText("Találatok száma: " + matchingTasks.size());
            // Első találat kijelölése és megjelenítése
            if (!matchingTasks.isEmpty()) {
                Task firstMatch = matchingTasks.get(0);
                showDetails(firstMatch);

                // Részletek megjelenítése az összes találattal
                StringBuilder sb = new StringBuilder();
                sb.append("📋 Találatok (").append(matchingTasks.size()).append(" db):\n\n");
                for (Task t : matchingTasks) {
                    sb.append("• ").append(t.getName());
                    if (t.isOverdue())
                        sb.append(" 🚨");
                    if (t.isCompleted())
                        sb.append(" ✅");
                    sb.append("\n");
                }

                JOptionPane.showMessageDialog(this, sb.toString(), "Keresési eredmények",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Rekurzívan megkeresi a szűrési feltételeknek megfelelő taskokat.
     */
    private void findMatchingTasks(TaskGroup group, String searchText, String filter, java.util.List<Task> results) {
        for (edu.bbte.protrack.model.entities.ProjectComponent comp : group.getChildren()) {
            if (comp instanceof Task) {
                Task task = (Task) comp;
                boolean matches = true;

                // Név keresés
                if (!searchText.isEmpty()) {
                    matches = task.getName().toLowerCase().contains(searchText);
                }

                // Szűrő alkalmazása
                if (matches && !"Összes".equals(filter)) {
                    switch (filter) {
                        case "Folyamatban":
                            matches = !task.isCompleted();
                            break;
                        case "Befejezett":
                            matches = task.isCompleted();
                            break;
                        case "Késésben":
                            matches = task.isOverdue();
                            break;
                        case "Magas prioritás":
                            matches = task.getPriority() == Task.Priority.HIGH;
                            break;
                    }
                }

                if (matches) {
                    results.add(task);
                }
            } else if (comp instanceof TaskGroup) {
                findMatchingTasks((TaskGroup) comp, searchText, filter, results);
            }
        }
    }

    @Override
    public void onProjectChanged(ProjectEvent event) {
        treePanel.refreshTree();
        statusLabel.setText(String.format("Portfólió érték: %.2f €", rootProject.getCalculateTotalCost()));
        progressBar.setValue(rootProject.getCompletionPercentage());
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
            JLabel titleLabel = new JLabel("📋 Feladat részletei");
            titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 16f));
            detailsPanel.add(titleLabel, gbc);

            // Név
            gbc.gridy++;
            detailsPanel.add(new JLabel("Név: " + task.getName()), gbc);

            // Prioritás - színes háttérrel
            gbc.gridy++;
            JPanel priorityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            priorityPanel.setOpaque(false);

            JLabel priorityLabel = new JLabel(" " + task.getPriority().getDisplayName() + " ");
            priorityLabel.setOpaque(true);
            priorityLabel.setFont(priorityLabel.getFont().deriveFont(java.awt.Font.BOLD));

            switch (task.getPriority()) {
                case HIGH:
                    priorityLabel.setBackground(new java.awt.Color(220, 53, 69));
                    priorityLabel.setForeground(java.awt.Color.WHITE);
                    break;
                case MEDIUM:
                    priorityLabel.setBackground(new java.awt.Color(255, 193, 7));
                    priorityLabel.setForeground(java.awt.Color.BLACK);
                    break;
                case LOW:
                    priorityLabel.setBackground(new java.awt.Color(40, 167, 69));
                    priorityLabel.setForeground(java.awt.Color.WHITE);
                    break;
            }

            priorityPanel.add(new JLabel("Prioritás: "));
            priorityPanel.add(priorityLabel);
            detailsPanel.add(priorityPanel, gbc);

            // Költség
            gbc.gridy++;
            detailsPanel.add(new JLabel(String.format("Költség: %.0f €", task.getBaseCost())), gbc);

            // Határidő
            gbc.gridy++;
            String deadlineStr = task.getDeadline() != null ? task.getDeadline().toString() : "Nincs megadva";
            JLabel deadlineLabel = new JLabel("Határidő: " + deadlineStr);
            if (task.isOverdue()) {
                deadlineLabel.setText("🚨 KÉSÉSBEN: " + deadlineStr);
                deadlineLabel.setForeground(java.awt.Color.RED);
            } else if (task.isDueSoon()) {
                deadlineLabel.setText("⚠️ Hamarosan lejár: " + deadlineStr);
                deadlineLabel.setForeground(java.awt.Color.ORANGE);
            }
            detailsPanel.add(deadlineLabel, gbc);

            // Felelős
            gbc.gridy++;
            edu.bbte.protrack.model.entities.Employee emp = task.getEmployee();
            String felelősNév = (emp != null) ? emp.getName() : "Nincs hozzárendelve";
            detailsPanel.add(new JLabel("👤 Felelős: " + felelősNév), gbc);

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
            final int[] lastValue = { task.getCompletionPercentage() };
            progress.addChangeListener(e -> {
                progressLabel.setText("Haladás: " + progress.getValue() + "%");
                if (!progress.getValueIsAdjusting() && progress.getValue() != lastValue[0]) {
                    // COMMAND MINTA: UpdateCompletionCommand - Undo támogatással
                    UpdateCompletionCommand cmd = new UpdateCompletionCommand(task, progress.getValue());
                    commandManager.executeCommand(cmd);
                    lastValue[0] = progress.getValue();
                    eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
                    showDetails(task);
                }
            });
            detailsPanel.add(progress, gbc);

            // Gombok
            gbc.gridy++;
            JPanel buttonPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 10, 5));

            JButton completeBtn = new JButton("✅ Befejezés (100%)");
            completeBtn.setEnabled(!task.isCompleted());
            completeBtn.addActionListener(e -> {
                UpdateCompletionCommand cmd = new UpdateCompletionCommand(task, 100);
                commandManager.executeCommand(cmd);
                eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
                showDetails(task);
            });
            buttonPanel.add(completeBtn);

            JButton resetBtn = new JButton("🔄 Visszaállítás (0%)");
            resetBtn.addActionListener(e -> {
                UpdateCompletionCommand cmd = new UpdateCompletionCommand(task, 0);
                commandManager.executeCommand(cmd);
                eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
                showDetails(task);
            });
            buttonPanel.add(resetBtn);

            JButton editBtn = new JButton("✏️ Szerkesztés");
            editBtn.addActionListener(e -> {
                EditTaskDialog dialog = new EditTaskDialog(this, task);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    eventManager.trigger(new ProjectEvent(ProjectEvent.EventType.DATA_CHANGED, task));
                    showDetails(task);
                }
            });
            buttonPanel.add(editBtn);

            detailsPanel.add(buttonPanel, gbc);

        } else if (obj instanceof TaskGroup) {
            TaskGroup group = (TaskGroup) obj;

            // Cím
            JLabel titleLabel = new JLabel("📁 Projekt részletei");
            titleLabel.setFont(titleLabel.getFont().deriveFont(java.awt.Font.BOLD, 16f));
            detailsPanel.add(titleLabel, gbc);

            gbc.gridy++;
            detailsPanel.add(new JLabel("Név: " + group.getName()), gbc);

            gbc.gridy++;
            detailsPanel.add(new JLabel(String.format("Összköltség: %.0f €", group.getCalculateTotalCost())), gbc);

            gbc.gridy++;
            detailsPanel.add(new JLabel("Átlagos haladás: " + group.getCompletionPercentage() + "%"), gbc);

            // Task összesítő
            gbc.gridy++;
            gbc.insets = new java.awt.Insets(15, 10, 5, 10);
            JLabel summaryTitle = new JLabel("📊 Feladat összesítő:");
            summaryTitle.setFont(summaryTitle.getFont().deriveFont(java.awt.Font.BOLD));
            detailsPanel.add(summaryTitle, gbc);

            // Számoljuk meg a taskokat
            int[] counts = countTasks(group);
            int total = counts[0];
            int completed = counts[1];
            int overdue = counts[2];
            int inProgress = total - completed;

            gbc.insets = new java.awt.Insets(5, 30, 5, 10);
            gbc.gridy++;
            detailsPanel.add(new JLabel("📋 Összes feladat: " + total), gbc);

            gbc.gridy++;
            JLabel completedLabel = new JLabel("✅ Befejezett: " + completed);
            completedLabel.setForeground(new java.awt.Color(0, 128, 0));
            detailsPanel.add(completedLabel, gbc);

            gbc.gridy++;
            JLabel inProgressLabel = new JLabel("⏳ Folyamatban: " + inProgress);
            inProgressLabel.setForeground(java.awt.Color.ORANGE);
            detailsPanel.add(inProgressLabel, gbc);

            gbc.gridy++;
            JLabel overdueLabel = new JLabel("🚨 Késésben: " + overdue);
            overdueLabel.setForeground(java.awt.Color.RED);
            detailsPanel.add(overdueLabel, gbc);

            // Átnevezés gomb (nem a gyökérhez)
            if (group != rootProject) {
                gbc.gridy++;
                gbc.insets = new java.awt.Insets(15, 10, 5, 10);
                JButton renameBtn = new JButton("✏️ Átnevezés");
                renameBtn.addActionListener(e -> handleRenameProject(group));
                detailsPanel.add(renameBtn, gbc);
            }
        }

        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    /**
     * Rekurzívan megszámolja a taskokat egy csoportban.
     * 
     * @return int[] {összes, befejezett, késésben}
     */
    private int[] countTasks(TaskGroup group) {
        int total = 0;
        int completed = 0;
        int overdue = 0;

        for (edu.bbte.protrack.model.entities.ProjectComponent comp : group.getChildren()) {
            if (comp instanceof Task) {
                Task task = (Task) comp;
                total++;
                if (task.isCompleted()) {
                    completed++;
                }
                if (task.isOverdue()) {
                    overdue++;
                }
            } else if (comp instanceof TaskGroup) {
                int[] subCounts = countTasks((TaskGroup) comp);
                total += subCounts[0];
                completed += subCounts[1];
                overdue += subCounts[2];
            }
        }

        return new int[] { total, completed, overdue };
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