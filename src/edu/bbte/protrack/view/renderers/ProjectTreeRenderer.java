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

            if (userObject instanceof TaskGroup) {
                setIcon(getDefaultClosedIcon()); // Mappa ikon a fázisnak
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (userObject instanceof Task) {
                setIcon(getDefaultLeafIcon()); // Fájl ikon a feladatnak
                setFont(getFont().deriveFont(Font.PLAIN));
            }
        }
        return this;
    }
}