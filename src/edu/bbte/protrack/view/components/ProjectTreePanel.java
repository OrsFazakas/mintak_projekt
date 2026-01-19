package edu.bbte.protrack.view.components;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.view.renderers.ProjectTreeRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

/**
 * A Composite struktúra vizuális megjelenítése JTree használatával.
 */
public class ProjectTreePanel extends JPanel {
    private final JTree tree;
    private final TaskGroup rootData;

    public ProjectTreePanel(TaskGroup rootData) {
        this.rootData = rootData;
        setLayout(new BorderLayout());

        // A fa inicializálása a modell alapján
        DefaultMutableTreeNode rootNode = createNodes(rootData);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

        tree = new JTree(treeModel);
        tree.setCellRenderer(new ProjectTreeRenderer()); // Egyedi ikonok beállítása

        add(new JScrollPane(tree), BorderLayout.CENTER);
        setPreferredSize(new Dimension(300, 0));
    }

    /**
     * Rekurzívan bejárja a Composite struktúrát és felépíti a JTree csomópontjait.
     */
    private DefaultMutableTreeNode createNodes(ProjectComponent component) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(component);

        if (component instanceof TaskGroup) {
            TaskGroup group = (TaskGroup) component;
            for (ProjectComponent child : group.getChildren()) {
                node.add(createNodes(child));
            }
        }
        return node;
    }

    /**
     * Frissíti a fa nézetet, ha a modell változik (pl. Undo/Redo után).
     */
    public void refreshTree() {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(createNodes(rootData));
        model.reload();
    }

    public JTree getTree() {
        return tree;
    }
}