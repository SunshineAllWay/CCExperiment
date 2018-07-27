package org.apache.batik.apps.svgbrowser;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.batik.util.resources.ResourceManager;
public class OptionPanel extends JPanel {
    public static final String RESOURCES =
        "org.apache.batik.apps.svgbrowser.resources.GUI";
    protected static ResourceBundle bundle;
    protected static ResourceManager resources;
    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }
    public OptionPanel(LayoutManager layout) {
        super(layout);
    }
    public static class Dialog extends JDialog {
        protected JButton ok;
        protected JPanel panel;
        public Dialog(Component parent, String title, JPanel panel) {
            super(JOptionPane.getFrameForComponent(parent), title);
            setModal(true);
            this.panel = panel;
            getContentPane().add(panel, BorderLayout.CENTER);
            getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);
        }
        protected JPanel createButtonPanel() {
            JPanel panel = new JPanel(new FlowLayout());
            ok = new JButton(resources.getString("OKButton.text"));
            ok.addActionListener(new OKButtonAction());
            panel.add(ok);
            return panel;
        }
        protected class OKButtonAction extends AbstractAction {
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        }
    }
}
