package edu.bbte.protrack.view.renderers;

import edu.bbte.protrack.model.entities.ProjectComponent;
import edu.bbte.protrack.model.entities.Task;
import edu.bbte.protrack.model.entities.TaskGroup;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * Egyedi megjelenítő a fához, amely típusfüggő ikonokat és formázást biztosít.
 */
public class ProjectTreeRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();

            if (userObject instanceof ProjectComponent) {
                ProjectComponent comp = (ProjectComponent) userObject;

                // Szöveg beállítása: Név + Költség
                setText(String.format("%s (%.0f €)", comp.getName(), comp.getCalculateTotalCost()));

                // Ikonok és stílusok a Composite típusa alapján
                if (comp instanceof TaskGroup) {
                    setFont(getFont().deriveFont(Font.BOLD));
                    // Itt beállíthatnál mappaikont: setIcon(UIManager.getIcon("FileView.directoryIcon"));
                } else if (comp instanceof Task) {
                    Task task = (Task) comp;
                    // Ha kész van, áthúzott vagy zöld lehetne
                    if (task.getCompletionPercentage() == 100) {
                        setForeground(Color.GREEN.darker());
                    }
                }
            }
        }
        return this;
    }
}