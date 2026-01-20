package edu.bbte.protrack.view.components;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.TaskGroup;
import edu.bbte.protrack.view.renderers.ProjectTreeRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

//A Composite struktúra vizuális megjelenítése JTree használatával.
public class ProjectTreePanel extends JPanel {
    private final JTree tree;
    private TaskGroup rootData;

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

        // Alapértelmezésben kinyitjuk a gyökeret
        tree.expandRow(0);
    }

    //Rekurzívan bejárja a Composite struktúrát és felépíti a JTree csomópontjait.
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

    //Frissíti a fa nézetet, megőrizve a kinyitott állapotokat és a kijelölést.
    public void refreshTree() {
        // 1. Mentjük a kijelölt elem útvonalát (nevek alapján)
        TreePath selectedPath = tree.getSelectionPath();
        List<String> selectedNames = getPathNames(selectedPath);

        // 2. Mentjük a kinyitott elemek útvonalait
        List<List<String>> expandedPaths = new ArrayList<>();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        saveExpandedPaths(root, new ArrayList<>(), expandedPaths);

        // 3. Újraépítjük a fát
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode newRoot = createNodes(rootData);
        model.setRoot(newRoot);

        // 4. Visszaállítjuk a kinyitott állapotokat
        for (List<String> pathNames : expandedPaths) {
            TreePath path = findPathByNames(newRoot, pathNames);
            if (path != null) {
                tree.expandPath(path);
            }
        }

        // 5. Visszaállítjuk a kijelölést
        if (!selectedNames.isEmpty()) {
            TreePath newSelectedPath = findPathByNames(newRoot, selectedNames);
            if (newSelectedPath != null) {
                tree.setSelectionPath(newSelectedPath);
            }
        }

        // Mindig kinyitjuk a gyökeret
        tree.expandRow(0);
    }

    //Lekéri egy TreePath elemneveit.
    private List<String> getPathNames(TreePath path) {
        List<String> names = new ArrayList<>();
        if (path != null) {
            for (Object node : path.getPath()) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                Object userObj = treeNode.getUserObject();
                if (userObj instanceof ProjectComponent) {
                    names.add(((ProjectComponent) userObj).getName());
                }
            }
        }
        return names;
    }

    //Rekurzívan elmenti a kinyitott elemek útvonalait.
    private void saveExpandedPaths(DefaultMutableTreeNode node, List<String> currentPath, List<List<String>> result) {
        Object userObj = node.getUserObject();
        if (userObj instanceof ProjectComponent) {
            currentPath.add(((ProjectComponent) userObj).getName());
        }

        TreePath treePath = new TreePath(node.getPath());
        if (tree.isExpanded(treePath)) {
            result.add(new ArrayList<>(currentPath));
        }

        Enumeration<?> children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            saveExpandedPaths(child, new ArrayList<>(currentPath), result);
        }
    }


    //Megkeresi a TreePath-t nevek alapján.
    private TreePath findPathByNames(DefaultMutableTreeNode root, List<String> names) {
        if (names.isEmpty())
            return null;

        DefaultMutableTreeNode current = root;
        List<DefaultMutableTreeNode> pathNodes = new ArrayList<>();
        pathNodes.add(root);

        for (int i = 1; i < names.size(); i++) {
            String targetName = names.get(i);
            boolean found = false;

            Enumeration<?> children = current.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
                Object userObj = child.getUserObject();
                if (userObj instanceof ProjectComponent) {
                    if (((ProjectComponent) userObj).getName().equals(targetName)) {
                        pathNodes.add(child);
                        current = child;
                        found = true;
                        break;
                    }
                }
            }

            if (!found)
                return null;
        }

        return new TreePath(pathNodes.toArray());
    }

    public JTree getTree() {
        return tree;
    }

    //Új gyökér projekt beállítása (pl. fájlból betöltéskor).
    public void updateRootProject(TaskGroup newRoot) {
        this.rootData = newRoot;
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.setRoot(createNodes(rootData));
        tree.expandRow(0);
    }
}