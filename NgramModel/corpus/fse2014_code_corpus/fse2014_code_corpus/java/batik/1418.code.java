package org.apache.batik.util.gui.resource;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
public class JToolbarButton extends JButton {
    public JToolbarButton() {
        initialize();
    }
    public JToolbarButton(String txt) {
        super(txt);
        initialize();
    }
    protected void initialize() {
        if (!System.getProperty("java.version").startsWith("1.3")) {
            setOpaque(false);
            setBackground(new java.awt.Color(0, 0, 0, 0));
        }
        setBorderPainted(false);
        setMargin(new Insets(2, 2, 2, 2));
        addMouseListener(new MouseListener());
    }
    protected class MouseListener extends MouseAdapter {
        public void mouseEntered(MouseEvent ev) {
            setBorderPainted(true);
        }
        public void mouseExited(MouseEvent ev) {
            setBorderPainted(false);
        }
    }
}
