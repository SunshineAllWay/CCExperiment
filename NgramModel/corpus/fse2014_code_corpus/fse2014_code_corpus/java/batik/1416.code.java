package org.apache.batik.util.gui.resource;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import org.apache.batik.util.resources.ResourceFormatException;
import org.apache.batik.util.resources.ResourceManager;
public class ButtonFactory extends ResourceManager {
    private static final String ICON_SUFFIX        = ".icon";
    private static final String TEXT_SUFFIX        = ".text";
    private static final String MNEMONIC_SUFFIX    = ".mnemonic";
    private static final String ACTION_SUFFIX      = ".action";
    private static final String SELECTED_SUFFIX    = ".selected";
    private static final String TOOLTIP_SUFFIX     = ".tooltip";
    private ActionMap actions;
    public ButtonFactory(ResourceBundle rb, ActionMap am) {
        super(rb);
        actions = am;
    }
    public JButton createJButton(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JButton result;
        try {
            result = new JButton(getString(name+TEXT_SUFFIX));
        } catch (MissingResourceException e) {
            result = new JButton();
        }
        initializeButton(result, name);
        return result;
    }
    public JButton createJToolbarButton(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JButton result;
        try {
            result = new JToolbarButton(getString(name+TEXT_SUFFIX));
        } catch (MissingResourceException e) {
            result = new JToolbarButton();
        }
        initializeButton(result, name);
        return result;
    }
    public JToggleButton createJToolbarToggleButton(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JToggleButton result;
        try {
            result = new JToolbarToggleButton(getString(name+TEXT_SUFFIX));
        } catch (MissingResourceException e) {
            result = new JToolbarToggleButton();
        }
        initializeButton(result, name);
        return result;
    }
    public JRadioButton createJRadioButton(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JRadioButton result = new JRadioButton(getString(name+TEXT_SUFFIX));
        initializeButton(result, name);
        try {
            result.setSelected(getBoolean(name+SELECTED_SUFFIX));
        } catch (MissingResourceException e) {
        }
        return result;
    }
    public JCheckBox createJCheckBox(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JCheckBox result = new JCheckBox(getString(name+TEXT_SUFFIX));
        initializeButton(result, name);
        try {
            result.setSelected(getBoolean(name+SELECTED_SUFFIX));
        } catch (MissingResourceException e) {
        }
        return result;
    }
    private void initializeButton(AbstractButton b, String name)
        throws ResourceFormatException, MissingListenerException {
        try {
            Action a = actions.getAction(getString(name+ACTION_SUFFIX));
            if (a == null) {
                throw new MissingListenerException("", "Action",
                                                   name+ACTION_SUFFIX);
            }
            b.setAction(a);
            try {
                b.setText(getString(name+TEXT_SUFFIX));
            } catch (MissingResourceException mre) {
            }
            if (a instanceof JComponentModifier) {
                ((JComponentModifier)a).addJComponent(b);
            }
        } catch (MissingResourceException e) {
        }
        try {
            String s = getString(name+ICON_SUFFIX);
            URL url  = actions.getClass().getResource(s);
            if (url != null) {
                b.setIcon(new ImageIcon(url));
            }
        } catch (MissingResourceException e) {
        }
        try {
            String str = getString(name+MNEMONIC_SUFFIX);
            if (str.length() == 1) {
                b.setMnemonic(str.charAt(0));
            } else {
                throw new ResourceFormatException("Malformed mnemonic",
                                                  bundle.getClass().getName(),
                                                  name+MNEMONIC_SUFFIX);
            }
        } catch (MissingResourceException e) {
        }
        try {
            String s = getString(name+TOOLTIP_SUFFIX);
            if (s != null) {
                b.setToolTipText(s);
            }
        } catch (MissingResourceException e) {
        }
    }
}
