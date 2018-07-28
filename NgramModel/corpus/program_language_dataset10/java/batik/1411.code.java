package org.apache.batik.util.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.batik.util.resources.ResourceManager;
public class LocationBar extends JPanel {
    protected static final String RESOURCES =
        "org.apache.batik.util.gui.resources.LocationBar";
    protected static ResourceBundle bundle;
    protected static ResourceManager rManager;
    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        rManager = new ResourceManager(bundle);
    }
    protected JComboBox comboBox;
    public LocationBar() {
        super(new BorderLayout(5, 5));
        JLabel label = new JLabel(rManager.getString("Panel.label"));
        add("West", label);
        try {
            String s = rManager.getString("Panel.icon");
            URL url  = getClass().getResource(s);
            if (url != null) {
                label.setIcon(new ImageIcon(url));
            }
        } catch (MissingResourceException e) {
        }
        add("Center", comboBox = new JComboBox());
        comboBox.setEditable(true);
    }
    public void addActionListener(ActionListener listener) {
        comboBox.addActionListener(listener);
    }
    public String getText() {
        return (String)comboBox.getEditor().getItem();
    }
    public void setText(String text) {
        comboBox.getEditor().setItem(text);
    }
    public void addToHistory(String text) {
        comboBox.addItem(text);
        comboBox.setPreferredSize
            (new Dimension(0, comboBox.getPreferredSize().height));
    }
}
