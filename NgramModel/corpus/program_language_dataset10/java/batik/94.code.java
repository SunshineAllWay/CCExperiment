package org.apache.batik.apps.svgbrowser;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import org.apache.batik.util.gui.ExtendedGridBagConstraints;
public class PNGOptionPanel extends OptionPanel {
    protected JCheckBox check;
    public PNGOptionPanel() {
        super(new GridBagLayout());
        ExtendedGridBagConstraints constraints =
            new ExtendedGridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.setGridBounds(0, 0, 1, 1);
        add(new JLabel(resources.getString("PNGOptionPanel.label")),
            constraints);
        check = new JCheckBox();
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.setGridBounds(1, 0, 1, 1);
        add(check, constraints);
    }
    public boolean isIndexed() {
        return check.isSelected();
    }
    public static boolean showDialog(Component parent) {
        String title = resources.getString("PNGOptionPanel.dialog.title");
        PNGOptionPanel panel = new PNGOptionPanel();
        Dialog dialog = new Dialog(parent, title, panel);
        dialog.pack();
        dialog.setVisible(true);
        return panel.isIndexed();
    }
}
