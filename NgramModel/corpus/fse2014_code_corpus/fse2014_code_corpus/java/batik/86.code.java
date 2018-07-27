package org.apache.batik.apps.svgbrowser;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSlider;
import org.apache.batik.util.gui.ExtendedGridBagConstraints;
public class JPEGOptionPanel extends OptionPanel {
    protected JSlider quality;
    public JPEGOptionPanel() {
        super(new GridBagLayout());
        ExtendedGridBagConstraints constraints = 
            new ExtendedGridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.setGridBounds(0, 0, 1, 1);
        add(new JLabel(resources.getString("JPEGOptionPanel.label")), 
            constraints);
        quality = new JSlider();
        quality.setMinimum(0);
        quality.setMaximum(100);
        quality.setMajorTickSpacing(10);
        quality.setMinorTickSpacing(5);
        quality.setPaintTicks(true);
        quality.setPaintLabels(true);
        quality.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        Hashtable labels = new Hashtable();
        for (int i=0; i < 100; i+=10) {
            labels.put(new Integer(i), new JLabel("0."+i/10));
        }
        labels.put(new Integer(100), new JLabel("1"));
        quality.setLabelTable(labels);
        Dimension dim = quality.getPreferredSize();
        quality.setPreferredSize(new Dimension(350, dim.height));
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.setGridBounds(1, 0, 1, 1);
        add(quality, constraints);
    }
    public float getQuality() {
        return quality.getValue()/100f;
    }
    public static float showDialog(Component parent) {
        String title = resources.getString("JPEGOptionPanel.dialog.title");
        JPEGOptionPanel panel = new JPEGOptionPanel();
        Dialog dialog = new Dialog(parent, title, panel);
        dialog.pack();
        dialog.setVisible(true);
        return panel.getQuality();
    }
}
