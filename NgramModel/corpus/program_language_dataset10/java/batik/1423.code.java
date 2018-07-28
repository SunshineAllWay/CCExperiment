package org.apache.batik.util.gui.resource;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.apache.batik.util.resources.ResourceFormatException;
import org.apache.batik.util.resources.ResourceManager;
public class ToolBarFactory extends ResourceManager {
    private static final String SEPARATOR = "-";
    private ButtonFactory buttonFactory;
    public ToolBarFactory(ResourceBundle rb, ActionMap am) {
        super(rb);
        buttonFactory = new ButtonFactory(rb, am);
    }
    public JToolBar createJToolBar(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        JToolBar result  = new JToolBar();
        List     buttons = getStringList(name);
        Iterator it      = buttons.iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            if (s.equals(SEPARATOR)) {
                result.add(new JToolbarSeparator());
            } else {
                result.add(createJButton(s));
            }
        }
        return result;
    }
    public JButton createJButton(String name)
        throws MissingResourceException,
               ResourceFormatException,
               MissingListenerException {
        return buttonFactory.createJToolbarButton(name);
    }
}
