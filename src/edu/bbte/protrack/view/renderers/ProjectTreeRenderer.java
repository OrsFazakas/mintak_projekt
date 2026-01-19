package edu.bbte.protrack.view.renderers;

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
                setIcon(getDefaultClosedIcon());
                setFont(getFont().deriveFont(Font.BOLD));
                setForeground(new Color(50, 50, 50));
            } else if (userObject instanceof Task) {
                Task task = (Task) userObject;

                // Színes prioritás ikon
                setIcon(createPriorityIcon(task.getPriority()));

                // Betűstílus és szín a státusz alapján
                if (task.isCompleted()) {
                    setFont(getFont().deriveFont(Font.ITALIC));
                    setForeground(new Color(0, 128, 0)); // Zöld - kész
                } else if (task.isOverdue()) {
                    setFont(getFont().deriveFont(Font.BOLD));
                    setForeground(Color.RED); // Piros - késésben
                } else {
                    setFont(getFont().deriveFont(Font.PLAIN));
                    setForeground(Color.BLACK);
                }
            }
        }
        return this;
    }

    /**
     * Létrehoz egy színes kör ikont a prioritás alapján.
     */
    private Icon createPriorityIcon(Task.Priority priority) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Háttér kör
                Color color;
                switch (priority) {
                    case HIGH:
                        color = new Color(220, 53, 69); // Piros
                        break;
                    case MEDIUM:
                        color = new Color(255, 193, 7); // Sárga
                        break;
                    case LOW:
                        color = new Color(40, 167, 69); // Zöld
                        break;
                    default:
                        color = Color.GRAY;
                }

                g2d.setColor(color);
                g2d.fillOval(x + 2, y + 2, 12, 12);

                // Szegély
                g2d.setColor(color.darker());
                g2d.drawOval(x + 2, y + 2, 12, 12);

                g2d.dispose();
            }

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };
    }
}
