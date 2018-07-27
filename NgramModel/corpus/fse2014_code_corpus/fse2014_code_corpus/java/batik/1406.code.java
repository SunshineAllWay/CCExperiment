package org.apache.batik.util.gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;
public class CSSMediaPanel extends JPanel implements ActionMap {
    protected static final String RESOURCES =
        "org.apache.batik.util.gui.resources.CSSMediaPanel";
    protected static ResourceBundle bundle;
    protected static ResourceManager resources;
    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }
    protected JButton removeButton;
    protected JButton addButton;
    protected JButton clearButton;
    protected DefaultListModel listModel = new DefaultListModel();
    protected JList mediaList;
    public CSSMediaPanel() {
        super(new GridBagLayout());
        listeners.put("AddButtonAction", new AddButtonAction());
        listeners.put("RemoveButtonAction", new RemoveButtonAction());
        listeners.put("ClearButtonAction", new ClearButtonAction());
        setBorder(BorderFactory.createTitledBorder
                  (BorderFactory.createEtchedBorder(),
                   resources.getString("Panel.title")));
        ExtendedGridBagConstraints constraints =
            new ExtendedGridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);
        mediaList = new JList();
        mediaList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mediaList.setModel(listModel);
        mediaList.addListSelectionListener(new MediaListSelectionListener());
        listModel.addListDataListener(new MediaListDataListener());
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.setGridBounds(0, 0, 1, 3);
        scrollPane.getViewport().add(mediaList);
        add(scrollPane, constraints);
        ButtonFactory bf = new ButtonFactory(bundle, this);
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        addButton = bf.createJButton("AddButton");
        constraints.setGridBounds(1, 0, 1, 1);
        add(addButton, constraints);
        removeButton = bf.createJButton("RemoveButton");
        constraints.setGridBounds(1, 1, 1, 1);
        add(removeButton, constraints);
        clearButton = bf.createJButton("ClearButton");
        constraints.setGridBounds(1, 2, 1, 1);
        add(clearButton, constraints);
        updateButtons();
    }
    protected void updateButtons() {
        removeButton.setEnabled(!mediaList.isSelectionEmpty());
        clearButton.setEnabled(!listModel.isEmpty());
    }
    public void setMedia(List mediaList) {
        listModel.removeAllElements();
        Iterator iter = mediaList.iterator();
        while (iter.hasNext()) {
            listModel.addElement(iter.next());
        }
    }
    public void setMedia(String media) {
        listModel.removeAllElements();
        StringTokenizer tokens = new StringTokenizer(media, " ");
        while (tokens.hasMoreTokens()) {
            listModel.addElement(tokens.nextToken());
        }
    }
    public List getMedia() {
        List media = new ArrayList(listModel.size());
        Enumeration e = listModel.elements();
        while (e.hasMoreElements()) {
            media.add(e.nextElement());
        }
        return media;
    }
    public String getMediaAsString() {
        StringBuffer buffer = new StringBuffer();
        Enumeration e = listModel.elements();
        while (e.hasMoreElements()) {
            buffer.append((String)e.nextElement());
            buffer.append( ' ' );
        }
        return buffer.toString();
    }
    public static int showDialog(Component parent, String title) {
        return showDialog(parent, title, "");
    }
    public static int showDialog(Component parent,
                                 String title,
                                 List mediaList) {
        Dialog dialog = new Dialog(parent, title, mediaList);
        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
        return dialog.getReturnCode();
    }
    public static int showDialog(Component parent,
                                 String title,
                                 String media) {
        Dialog dialog = new Dialog(parent, title, media);
        dialog.setModal(true);
        dialog.pack();
        dialog.setVisible(true);
        return dialog.getReturnCode();
    }
    protected Map listeners = new HashMap();
    public Action getAction(String key) throws MissingListenerException {
        return (Action)listeners.get(key);
    }
    protected class AddButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            AddMediumDialog dialog = new AddMediumDialog(CSSMediaPanel.this);
            dialog.pack();
            dialog.setVisible(true);
            if ((dialog.getReturnCode() == AddMediumDialog.CANCEL_OPTION) ||
                (dialog.getMedium() == null)) {
                return;
            }
            String medium = dialog.getMedium().trim();
            if (medium.length() == 0 || listModel.contains(medium)) {
                return;
            }
            for (int i = 0; i < listModel.size() && medium != null; ++i) {
                String s = (String)listModel.getElementAt(i);
                int c = medium.compareTo(s);
                if (c == 0) {
                    medium = null;
                } else if (c < 0) {
                    listModel.insertElementAt(medium, i);
                    medium = null;
                }
            }
            if (medium != null) {
                listModel.addElement(medium);
            }
        }
    }
    protected class RemoveButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            int index = mediaList.getSelectedIndex();
            mediaList.clearSelection();
            if (index >= 0) {
                listModel.removeElementAt(index);
            }
        }
    }
    protected class ClearButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            mediaList.clearSelection();
            listModel.removeAllElements();
        }
    }
    protected class MediaListSelectionListener
        implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            updateButtons();
        }
    }
    protected class MediaListDataListener implements ListDataListener {
        public void contentsChanged(ListDataEvent e) {
            updateButtons();
        }
        public void intervalAdded(ListDataEvent e) {
            updateButtons();
        }
        public void intervalRemoved(ListDataEvent e) {
            updateButtons();
        }
    }
    public static class AddMediumDialog extends JDialog implements ActionMap {
        public static final int OK_OPTION = 0;
        public static final int CANCEL_OPTION = 1;
        protected JComboBox medium;
        protected int returnCode;
        public AddMediumDialog(Component parent) {
            super(JOptionPane.getFrameForComponent(parent),
                  resources.getString("AddMediumDialog.title"));
            setModal(true);
            listeners.put("OKButtonAction", new OKButtonAction());
            listeners.put("CancelButtonAction", new CancelButtonAction());
            getContentPane().add(createContentPanel(), BorderLayout.CENTER);
            getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
        }
        public String getMedium() {
            return (String)medium.getSelectedItem();
        }
        protected Component createContentPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            panel.add(new JLabel(resources.getString("AddMediumDialog.label")),
                      BorderLayout.WEST);
            medium = new JComboBox();
            medium.setEditable(true);
            String media = resources.getString("Media.list");
            StringTokenizer tokens = new StringTokenizer(media, " ");
            while (tokens.hasMoreTokens()) {
                medium.addItem(tokens.nextToken());
            }
            panel.add(medium, BorderLayout.CENTER);
            return panel;
        }
        protected Component createButtonsPanel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            ButtonFactory bf = new ButtonFactory(bundle, this);
            panel.add(bf.createJButton("OKButton"));
            panel.add(bf.createJButton("CancelButton"));
            return panel;
        }
        public int getReturnCode() {
            return returnCode;
        }
        protected Map listeners = new HashMap();
        public Action getAction(String key) throws MissingListenerException {
            return (Action)listeners.get(key);
        }
        protected class OKButtonAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                returnCode = OK_OPTION;
                dispose();
            }
        }
        protected class CancelButtonAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                returnCode = CANCEL_OPTION;
                dispose();
            }
        }
    }
    public static class Dialog extends JDialog implements ActionMap {
        public static final int OK_OPTION = 0;
        public static final int CANCEL_OPTION = 1;
        protected int returnCode;
        public Dialog() {
            this(null, "", "");
        }
        public Dialog(Component parent, String title, List mediaList) {
            super(JOptionPane.getFrameForComponent(parent), title);
            listeners.put("OKButtonAction", new OKButtonAction());
            listeners.put("CancelButtonAction", new CancelButtonAction());
            CSSMediaPanel panel = new CSSMediaPanel();
            panel.setMedia(mediaList);
            getContentPane().add(panel, BorderLayout.CENTER);
            getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
        }
        public Dialog(Component parent, String title, String media) {
            super(JOptionPane.getFrameForComponent(parent), title);
            listeners.put("OKButtonAction", new OKButtonAction());
            listeners.put("CancelButtonAction", new CancelButtonAction());
            CSSMediaPanel panel = new CSSMediaPanel();
            panel.setMedia(media);
            getContentPane().add(panel, BorderLayout.CENTER);
            getContentPane().add(createButtonsPanel(), BorderLayout.SOUTH);
        }
        public int getReturnCode() {
            return returnCode;
        }
        protected JPanel createButtonsPanel() {
            JPanel  p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            ButtonFactory bf = new ButtonFactory(bundle, this);
            p.add(bf.createJButton("OKButton"));
            p.add(bf.createJButton("CancelButton"));
            return p;
        }
        protected Map listeners = new HashMap();
        public Action getAction(String key) throws MissingListenerException {
            return (Action)listeners.get(key);
        }
        protected class OKButtonAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                returnCode = OK_OPTION;
                dispose();
            }
        }
        protected class CancelButtonAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                returnCode = CANCEL_OPTION;
                dispose();
            }
        }
    }
    public static void main(String [] args) {
        String media = "all aural braille embossed handheld print projection screen tty tv";
        int code = CSSMediaPanel.showDialog(null, "Test", media);
        System.out.println(code);
        System.exit(0);
    }
}
