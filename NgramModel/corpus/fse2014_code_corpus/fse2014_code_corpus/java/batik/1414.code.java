package org.apache.batik.util.gui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;
public class UserStyleDialog extends JDialog implements ActionMap {
    public static final int OK_OPTION = 0;
    public static final int CANCEL_OPTION = 1;
    protected static final String RESOURCES =
        "org.apache.batik.util.gui.resources.UserStyleDialog";
    protected static ResourceBundle bundle;
    protected static ResourceManager resources;
    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }
    protected Panel panel;
    protected String chosenPath;
    protected int returnCode;
    public UserStyleDialog(JFrame f) {
        super(f);
        setModal(true);
        setTitle(resources.getString("Dialog.title"));
        listeners.put("OKButtonAction",        new OKButtonAction());
        listeners.put("CancelButtonAction",    new CancelButtonAction());
        getContentPane().add(panel = new Panel());
        getContentPane().add( createButtonsPanel(), BorderLayout.SOUTH );
        pack();
    }
    public int showDialog() {
        pack();
        setVisible(true);
        return returnCode;
    }
    public String getPath() {
        return chosenPath;
    }
    public void setPath(String s) {
        chosenPath = s;
        panel.fileTextField.setText(s);
        panel.fileCheckBox.setSelected(true);
    }
    protected JPanel createButtonsPanel() {
        JPanel  p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ButtonFactory bf = new ButtonFactory(bundle, this);
        p.add(bf.createJButton("OKButton"));
        p.add(bf.createJButton("CancelButton"));
        return p;
    }
    protected class OKButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (panel.fileCheckBox.isSelected()) {
                String path = panel.fileTextField.getText();
                if (path.equals("")) {
                    JOptionPane.showMessageDialog
                        (UserStyleDialog.this,
                         resources.getString("StyleDialogError.text"),
                         resources.getString("StyleDialogError.title"),
                         JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    File f = new File(path);
                    if (f.exists()) {
                        if (f.isDirectory()) {
                            path = null;
                        } else {
                            path = "file:" + path;
                        }
                    }
                    chosenPath = path;
                }
            } else {
                chosenPath = null;
            }
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
    protected Map listeners = new HashMap();
    public Action getAction(String key) throws MissingListenerException {
        return (Action)listeners.get(key);
    }
    public static class Panel extends JPanel {
        protected JCheckBox fileCheckBox;
        protected JLabel fileLabel;
        protected JTextField fileTextField;
        protected JButton browseButton;
        public Panel() {
            super(new GridBagLayout());
            setBorder(BorderFactory.createTitledBorder
                      (BorderFactory.createEtchedBorder(),
                       resources.getString("Panel.title")));
            ExtendedGridBagConstraints constraints =
                new ExtendedGridBagConstraints();
            constraints.insets = new Insets(5, 5, 5, 5);
            fileCheckBox =
                new JCheckBox(resources.getString("PanelFileCheckBox.text"));
            fileCheckBox.addChangeListener(new FileCheckBoxChangeListener());
            constraints.weightx = 0;
            constraints.weighty = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.setGridBounds(0, 2, 3, 1);
            this.add(fileCheckBox, constraints);
            fileLabel = new JLabel(resources.getString("PanelFileLabel.text"));
            constraints.weightx = 0;
            constraints.weighty = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.setGridBounds(0, 3, 3, 1);
            this.add(fileLabel, constraints);
            fileTextField = new JTextField(30);
            constraints.weightx = 1.0;
            constraints.weighty = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.setGridBounds(0, 4, 2, 1);
            this.add(fileTextField, constraints);
            ButtonFactory bf = new ButtonFactory(bundle, null);
            constraints.weightx = 0;
            constraints.weighty = 0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.EAST;
            constraints.setGridBounds(2, 4, 1, 1);
            browseButton = bf.createJButton("PanelFileBrowseButton");
            this.add(browseButton, constraints);
            browseButton.addActionListener(new FileBrowseButtonAction());
            fileLabel.setEnabled(false);
            fileTextField.setEnabled(false);
            browseButton.setEnabled(false);
        }
        public String getPath() {
            if(fileCheckBox.isSelected()){
                return fileTextField.getText();
            }
            else{
                return null;
            }
        }
        public void setPath(String s) {
            if(s == null){
                fileTextField.setEnabled(false);
                fileCheckBox.setSelected(false);
            }
            else{
                fileTextField.setEnabled(true);
                fileTextField.setText(s);
                fileCheckBox.setSelected(true);
            }
        }
        protected class FileCheckBoxChangeListener implements ChangeListener {
            public void stateChanged(ChangeEvent e) {
                boolean selected = fileCheckBox.isSelected();
                fileLabel.setEnabled(selected);
                fileTextField.setEnabled(selected);
                browseButton.setEnabled(selected);
            }
        }
        protected class FileBrowseButtonAction extends AbstractAction {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(new File("."));
                fileChooser.setFileHidingEnabled(false);
                int choice = fileChooser.showOpenDialog(Panel.this);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    File f = fileChooser.getSelectedFile();
                    try {
                        fileTextField.setText(f.getCanonicalPath());
                    } catch (IOException ex) {
                    }
                }
            }
        }
    }
}
