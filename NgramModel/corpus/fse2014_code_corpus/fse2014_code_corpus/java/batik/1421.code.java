package org.apache.batik.util.gui.resource;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import org.apache.batik.util.resources.ResourceFormatException;
import org.apache.batik.util.resources.ResourceManager;
public class MenuFactory extends ResourceManager {
    private static final String TYPE_MENU          = "MENU";
    private static final String TYPE_ITEM          = "ITEM";
    private static final String TYPE_RADIO         = "RADIO";
    private static final String TYPE_CHECK         = "CHECK";
    private static final String SEPARATOR          = "-";
    private static final String TYPE_SUFFIX        = ".type";
    private static final String TEXT_SUFFIX        = ".text";
    private static final String MNEMONIC_SUFFIX    = ".mnemonic";
    private static final String ACCELERATOR_SUFFIX = ".accelerator";
    private static final String ACTION_SUFFIX      = ".action";
    private static final String SELECTED_SUFFIX    = ".selected";
    private static final String ENABLED_SUFFIX     = ".enabled";
    private static final String ICON_SUFFIX        = ".icon";
    private ActionMap actions;
    private ButtonGroup buttonGroup;
    public MenuFactory(ResourceBundle rb, ActionMap am) {
        super(rb);
        actions = am;
        buttonGroup = null;
    }
    public JMenuBar createJMenuBar(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        return createJMenuBar(name, null);
    }
    public JMenuBar createJMenuBar(String name, String specialization)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JMenuBar result = new JMenuBar();
        List     menus  = getSpecializedStringList(name, specialization);
        Iterator it     = menus.iterator();
        while (it.hasNext()) {
            result.add(createJMenuComponent((String)it.next(), specialization));
        }
        return result;
    }
    protected String getSpecializedString(String name, String specialization) {
        String s;
        try {
            s = getString(name + '.' + specialization);
        } catch (MissingResourceException mre) {
            s = getString(name);
        }
        return s;
    }
    protected List getSpecializedStringList(String name,
                                            String specialization) {
        List l;
        try {
            l = getStringList(name + '.' + specialization);
        } catch (MissingResourceException mre) {
            l = getStringList(name);
        }
        return l;
    }
    protected boolean getSpecializedBoolean(String name,
                                            String specialization) {
        boolean b;
        try {
            b = getBoolean(name + '.' + specialization);
        } catch (MissingResourceException mre) {
            b = getBoolean(name);
        }
        return b;
    }
    protected JComponent createJMenuComponent(String name,
                                              String specialization)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        if (name.equals(SEPARATOR)) {
            buttonGroup = null;
            return new JSeparator();
        }
        String     type = getSpecializedString(name + TYPE_SUFFIX,
                                               specialization);
        JComponent item = null;
        if (type.equals(TYPE_RADIO)) {
            if (buttonGroup == null) {
                buttonGroup = new ButtonGroup();
            }
        } else {
            buttonGroup = null;
        }
        if (type.equals(TYPE_MENU)) {
            item = createJMenu(name, specialization);
        } else if (type.equals(TYPE_ITEM)) {
            item = createJMenuItem(name, specialization);
        } else if (type.equals(TYPE_RADIO)) {
            item = createJRadioButtonMenuItem(name, specialization);
            buttonGroup.add((AbstractButton)item);
        } else if (type.equals(TYPE_CHECK)) {
            item = createJCheckBoxMenuItem(name, specialization);
        } else {
            throw new ResourceFormatException("Malformed resource",
                                              bundle.getClass().getName(),
                                              name+TYPE_SUFFIX);
        }
        return item;
    }
    public JMenu createJMenu(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        return createJMenu(name, null);
    }
    public JMenu createJMenu(String name, String specialization)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JMenu result = new JMenu(getSpecializedString(name + TEXT_SUFFIX,
                                                      specialization));
        initializeJMenuItem(result, name, specialization);
        List     items = getSpecializedStringList(name, specialization);
        Iterator it    = items.iterator();
        while (it.hasNext()) {
            result.add(createJMenuComponent((String)it.next(), specialization));
        }
        return result;
    }
    public JMenuItem createJMenuItem(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        return createJMenuItem(name, null);
    }
    public JMenuItem createJMenuItem(String name, String specialization)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JMenuItem result = new JMenuItem(getSpecializedString(name + TEXT_SUFFIX,
                                                              specialization));
        initializeJMenuItem(result, name, specialization);
        return result;
    }
    public JRadioButtonMenuItem createJRadioButtonMenuItem(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        return createJRadioButtonMenuItem(name, null);
    }
    public JRadioButtonMenuItem createJRadioButtonMenuItem
            (String name, String specialization)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JRadioButtonMenuItem result;
        result = new JRadioButtonMenuItem
            (getSpecializedString(name + TEXT_SUFFIX, specialization));
        initializeJMenuItem(result, name, specialization);
        try {
            result.setSelected(getSpecializedBoolean(name + SELECTED_SUFFIX,
                                                     specialization));
        } catch (MissingResourceException e) {
        }
        return result;
    }
    public JCheckBoxMenuItem createJCheckBoxMenuItem(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        return createJCheckBoxMenuItem(name, null);
    }
    public JCheckBoxMenuItem createJCheckBoxMenuItem(String name,
                                                     String specialization)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JCheckBoxMenuItem result;
        result = new JCheckBoxMenuItem(getSpecializedString(name + TEXT_SUFFIX,
                                                            specialization));
        initializeJMenuItem(result, name, specialization);
        try {
            result.setSelected(getSpecializedBoolean(name + SELECTED_SUFFIX,
                                                     specialization));
        } catch (MissingResourceException e) {
        }
        return result;
    }
    protected void initializeJMenuItem(JMenuItem item, String name,
                                       String specialization)
        throws ResourceFormatException,
               MissingListenerException {
        try {
            Action a = actions.getAction
                (getSpecializedString(name + ACTION_SUFFIX, specialization));
            if (a == null) {
                throw new MissingListenerException("", "Action",
                                                   name+ACTION_SUFFIX);
            }
            item.setAction(a);
            item.setText(getSpecializedString(name + TEXT_SUFFIX,
                                              specialization));
            if (a instanceof JComponentModifier) {
                ((JComponentModifier)a).addJComponent(item);
            }
        } catch (MissingResourceException e) {
        }
        try {
            String s = getSpecializedString(name + ICON_SUFFIX, specialization);
            URL url  = actions.getClass().getResource(s);
            if (url != null) {
                item.setIcon(new ImageIcon(url));
            }
        } catch (MissingResourceException e) {
        }
        try {
            String str = getSpecializedString(name + MNEMONIC_SUFFIX,
                                              specialization);
            if (str.length() == 1) {
                item.setMnemonic(str.charAt(0));
            } else {
                throw new ResourceFormatException("Malformed mnemonic",
                                                  bundle.getClass().getName(),
                                                  name+MNEMONIC_SUFFIX);
            }
        } catch (MissingResourceException e) {
        }
        try {
            if (!(item instanceof JMenu)) {
                String str = getSpecializedString(name + ACCELERATOR_SUFFIX,
                                                  specialization);
                KeyStroke ks = KeyStroke.getKeyStroke(str);
                if (ks != null) {
                    item.setAccelerator(ks);
                } else {
                    throw new ResourceFormatException
                        ("Malformed accelerator",
                         bundle.getClass().getName(),
                         name+ACCELERATOR_SUFFIX);
                }
            }
        } catch (MissingResourceException e) {
        }
        try {
            item.setEnabled(getSpecializedBoolean(name + ENABLED_SUFFIX,
                                                  specialization));
        } catch (MissingResourceException e) {
        }
    }
}
