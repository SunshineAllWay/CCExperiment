package org.apache.batik.apps.svgbrowser;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.StringReader;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.gui.xmleditor.XMLTextEditor;
import org.apache.batik.util.XMLConstants;
import org.apache.batik.util.resources.ResourceManager;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
public class NodePickerPanel extends JPanel implements ActionMap {
    private static final int VIEW_MODE = 1;
    private static final int EDIT_MODE = 2;
    private static final int ADD_NEW_ELEMENT = 3;
    private static final String RESOURCES =
        "org.apache.batik.apps.svgbrowser.resources.NodePickerPanelMessages";
    private static ResourceBundle bundle;
    private static ResourceManager resources;
    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }
    private JTable attributesTable;
    private TableModelListener tableModelListener;
    private JScrollPane attributePane;
    private JPanel attributesPanel;
    private ButtonFactory buttonFactory;
    private JButton addButton;
    private JButton removeButton;
    private JLabel attributesLabel;
    private JButton applyButton;
    private JButton resetButton;
    private JPanel choosePanel;
    private SVGInputPanel svgInputPanel;
    private JLabel isWellFormedLabel;
    private JLabel svgInputPanelNameLabel;
    private boolean shouldProcessUpdate = true;
    private Element previewElement;
    private Element clonedElement;
    private Node parentElement;
    private int mode;
    private boolean isDirty;
    private EventListenerList eventListeners =
        new EventListenerList();
    private NodePickerController controller;
    private Map listeners = new HashMap(10);
    public NodePickerPanel(NodePickerController controller) {
        super(new GridBagLayout());
        this.controller = controller;
        initialize();
    }
    private void initialize() {
        addButtonActions();
        GridBagConstraints grid = new GridBagConstraints();
        grid.gridx = 1;
        grid.gridy = 1;
        grid.anchor = GridBagConstraints.NORTHWEST;
        grid.fill = GridBagConstraints.NONE;
        grid.insets = new Insets(5, 5, 0, 5);
        attributesLabel = new JLabel();
        String attributesLabelValue = resources
                .getString("AttributesTable.name");
        attributesLabel.setText(attributesLabelValue);
        this.add(attributesLabel, grid);
        grid.gridx = 1;
        grid.gridy = 2;
        grid.gridwidth = 2;
        grid.weightx = 1.0;
        grid.weighty = 0.3;
        grid.fill = GridBagConstraints.BOTH;
        grid.anchor = GridBagConstraints.CENTER;
        grid.insets = new Insets(0, 0, 0, 5);
        this.add(getAttributesPanel(), grid);
        grid.weightx = 0;
        grid.weighty = 0;
        grid.gridwidth = 1;
        grid.gridx = 1;
        grid.gridy = 3;
        grid.anchor = GridBagConstraints.NORTHWEST;
        grid.fill = GridBagConstraints.NONE;
        grid.insets = new Insets(0, 5, 0, 5);
        svgInputPanelNameLabel = new JLabel();
        String svgInputLabelValue = resources.getString("InputPanelLabel.name");
        svgInputPanelNameLabel.setText(svgInputLabelValue);
        this.add(svgInputPanelNameLabel, grid);
        grid.gridx = 1;
        grid.gridy = 4;
        grid.gridwidth = 2;
        grid.weightx = 1.0;
        grid.weighty = 1.0;
        grid.fill = GridBagConstraints.BOTH;
        grid.anchor = GridBagConstraints.CENTER;
        grid.insets = new Insets(0, 5, 0, 10);
        this.add(getSvgInputPanel(), grid);
        grid.weightx = 0;
        grid.weighty = 0;
        grid.gridwidth = 1;
        grid.gridx = 1;
        grid.gridy = 5;
        grid.anchor = GridBagConstraints.NORTHWEST;
        grid.fill = GridBagConstraints.NONE;
        grid.insets = new Insets(5, 5, 0, 5);
        isWellFormedLabel = new JLabel();
        String isWellFormedLabelVal =
            resources.getString("IsWellFormedLabel.wellFormed");
        isWellFormedLabel.setText(isWellFormedLabelVal);
        this.add(isWellFormedLabel, grid);
        grid.weightx = 0;
        grid.weighty = 0;
        grid.gridwidth = 1;
        grid.gridx = 2;
        grid.gridy = 5;
        grid.anchor = GridBagConstraints.EAST;
        grid.insets = new Insets(0, 0, 0, 5);
        this.add(getChoosePanel(), grid);
        enterViewMode();
    }
    private ButtonFactory getButtonFactory() {
        if (buttonFactory == null) {
            buttonFactory = new ButtonFactory(bundle, this);
        }
        return buttonFactory;
    }
    private void addButtonActions() {
        listeners.put("ApplyButtonAction", new ApplyButtonAction());
        listeners.put("ResetButtonAction", new ResetButtonAction());
        listeners.put("AddButtonAction", new AddButtonAction());
        listeners.put("RemoveButtonAction", new RemoveButtonAction());
    }
    private JButton getAddButton() {
        if (addButton == null) {
            addButton = getButtonFactory().createJButton("AddButton");
            addButton.addFocusListener(new NodePickerEditListener());
        }
        return addButton;
    }
    private JButton getRemoveButton() {
        if (removeButton == null) {
            removeButton = getButtonFactory().createJButton("RemoveButton");
            removeButton.addFocusListener(new NodePickerEditListener());
        }
        return removeButton;
    }
    private JButton getApplyButton() {
        if (applyButton == null) {
            applyButton = getButtonFactory().createJButton("ApplyButton");
        }
        return applyButton;
    }
    private JButton getResetButton() {
        if (resetButton == null) {
            resetButton = getButtonFactory().createJButton("ResetButton");
        }
        return resetButton;
    }
    private JPanel getAttributesPanel() {
        if (attributesPanel == null) {
            attributesPanel = new JPanel(new GridBagLayout());
            GridBagConstraints g11 = new GridBagConstraints();
            g11.gridx = 1;
            g11.gridy = 1;
            g11.fill = GridBagConstraints.BOTH;
            g11.anchor = GridBagConstraints.CENTER;
            g11.weightx = 4.0;
            g11.weighty = 1.0;
            g11.gridheight = 5;
            g11.gridwidth = 2;
            g11.insets = new Insets(5, 5, 5, 0);
            GridBagConstraints g12 = new GridBagConstraints();
            g12.gridx = 3;
            g12.gridy = 1;
            g12.fill = GridBagConstraints.HORIZONTAL;
            g12.anchor = GridBagConstraints.NORTH;
            g12.insets = new Insets(5, 20, 0, 5);
            g12.weightx = 1.0;
            GridBagConstraints g32 = new GridBagConstraints();
            g32.gridx = 3;
            g32.gridy = 3;
            g32.fill = GridBagConstraints.HORIZONTAL;
            g32.anchor = GridBagConstraints.NORTH;
            g32.insets = new Insets(5, 20, 0, 5);
            g32.weightx = 1.0;
            attributesTable = new JTable();
            attributesTable.setModel(new AttributesTableModel(10, 2));
            tableModelListener = new AttributesTableModelListener();
            attributesTable.getModel()
                    .addTableModelListener(tableModelListener);
            attributesTable.addFocusListener(new NodePickerEditListener());
            attributePane = new JScrollPane();
            attributePane.getViewport().add(attributesTable);
            attributesPanel.add(attributePane, g11);
            attributesPanel.add(getAddButton(), g12);
            attributesPanel.add(getRemoveButton(), g32);
        }
        return attributesPanel;
    }
    private SVGInputPanel getSvgInputPanel() {
        if (svgInputPanel == null) {
            svgInputPanel = new SVGInputPanel();
            svgInputPanel.getNodeXmlArea().getDocument().addDocumentListener
                (new XMLAreaListener());
            svgInputPanel.getNodeXmlArea().addFocusListener
                (new NodePickerEditListener());
        }
        return svgInputPanel;
    }
    private JPanel getChoosePanel() {
        if (choosePanel == null) {
            choosePanel = new JPanel(new GridBagLayout());
            GridBagConstraints g11 = new GridBagConstraints();
            g11.gridx = 1;
            g11.gridy = 1;
            g11.weightx = 0.5;
            g11.anchor = GridBagConstraints.WEST;
            g11.fill = GridBagConstraints.HORIZONTAL;
            g11.insets = new Insets(5, 5, 5, 5);
            GridBagConstraints g12 = new GridBagConstraints();
            g12.gridx = 2;
            g12.gridy = 1;
            g12.weightx = 0.5;
            g12.anchor = GridBagConstraints.EAST;
            g12.fill = GridBagConstraints.HORIZONTAL;
            g12.insets = new Insets(5, 5, 5, 5);
            choosePanel.add(getApplyButton(), g11);
            choosePanel.add(getResetButton(), g12);
        }
        return choosePanel;
    }
    public String getResults() {
        return getSvgInputPanel().getNodeXmlArea().getText();
    }
    private void updateViewAfterSvgInput(Element referentElement,
            Element elementToUpdate) {
        if (referentElement != null) {
            String isWellFormedLabelVal =
                resources.getString("IsWellFormedLabel.wellFormed");
            isWellFormedLabel.setText(isWellFormedLabelVal);
            getApplyButton().setEnabled(true);
            attributesTable.setEnabled(true);
            updateElementAttributes(elementToUpdate, referentElement);
            shouldProcessUpdate = false;
            updateAttributesTable(elementToUpdate);
            shouldProcessUpdate = true;
        } else {
            String isWellFormedLabelVal =
                resources.getString("IsWellFormedLabel.notWellFormed");
            isWellFormedLabel.setText(isWellFormedLabelVal);
            getApplyButton().setEnabled(false);
            attributesTable.setEnabled(false);
        }
    }
    private void updateElementAttributes(Element elem, Element referentElement) {
        removeAttributes(elem);
        NamedNodeMap newNodeMap = referentElement.getAttributes();
        for (int i = newNodeMap.getLength() - 1; i >= 0; i--) {
            Node newAttr = newNodeMap.item(i);
            String qualifiedName = newAttr.getNodeName();
            String attributeValue = newAttr.getNodeValue();
            String prefix = DOMUtilities.getPrefix(qualifiedName);
            String namespaceURI = getNamespaceURI(prefix);
            elem.setAttributeNS(namespaceURI, qualifiedName, attributeValue);
        }
    }
    private void updateElementAttributes
            (Element element, AttributesTableModel tableModel) {
        removeAttributes(element);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String newAttrName = (String) tableModel.getAttrNameAt(i);
            String newAttrValue = (String) tableModel.getAttrValueAt(i);
            if (newAttrName != null && newAttrName.length() > 0) {
                String namespaceURI;
                if (newAttrName.equals(XMLConstants.XMLNS_PREFIX)) {
                    namespaceURI = XMLConstants.XMLNS_NAMESPACE_URI;
                } else {
                    String prefix = DOMUtilities.getPrefix(newAttrName);
                    namespaceURI = getNamespaceURI(prefix);
                }
                if (newAttrValue != null) {
                    element.setAttributeNS
                        (namespaceURI, newAttrName, newAttrValue);
                } else {
                    element.setAttributeNS(namespaceURI, newAttrName, "");
                }
            }
        }
    }
    private void removeAttributes(Element element) {
        NamedNodeMap oldNodeMap = element.getAttributes();
        int n = oldNodeMap.getLength();
        for (int i = n - 1; i >= 0; i--) {
            element.removeAttributeNode((Attr) oldNodeMap.item(i));
        }
    }
    private String getNamespaceURI(String prefix) {
        String namespaceURI = null;
        if (prefix != null) {
            if (prefix.equals(SVGConstants.XMLNS_PREFIX)) {
                namespaceURI = SVGConstants.XMLNS_NAMESPACE_URI;
            } else {
                AbstractNode n;
                if (mode == EDIT_MODE) {
                    n = (AbstractNode) previewElement;
                    namespaceURI = n.lookupNamespaceURI(prefix);
                } else if (mode == ADD_NEW_ELEMENT) {
                    n = (AbstractNode) parentElement;
                    namespaceURI = n.lookupNamespaceURI(prefix);
                }
            }
        }
        return namespaceURI;
    }
    private void updateAttributesTable(Element elem) {
        NamedNodeMap map = elem.getAttributes();
        AttributesTableModel tableModel =
            (AttributesTableModel) attributesTable.getModel();
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            String attrName = (String) tableModel.getValueAt(i, 0);
            String newAttrValue = "";
            if (attrName != null) {
                newAttrValue = elem.getAttributeNS(null, attrName);
            }
            if (attrName == null || newAttrValue.length() == 0) {
                tableModel.removeRow(i);
            }
            if (newAttrValue.length() > 0) {
                tableModel.setValueAt(newAttrValue, i, 1);
            }
        }
        for (int i = 0; i < map.getLength(); i++) {
            Node attr = map.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();
            if (tableModel.getValueForName(attrName) == null) {
                Vector rowData = new Vector();
                rowData.add(attrName);
                rowData.add(attrValue);
                tableModel.addRow(rowData);
            }
        }
    }
    private void updateNodeXmlArea(Node node) {
        getSvgInputPanel().getNodeXmlArea().setText(DOMUtilities.getXML(node));
    }
    private Element getPreviewElement() {
        return previewElement;
    }
    public void setPreviewElement(Element elem) {
        if (previewElement != elem && isDirty) {
            if (!promptForChanges()) {
                return;
            }
        }
        this.previewElement = elem;
        enterViewMode();
        updateNodeXmlArea(elem);
        updateAttributesTable(elem);
    }
    boolean panelHiding() {
        return !isDirty || promptForChanges();
    }
    private int getMode() {
        return mode;
    }
    public void enterViewMode() {
        if (mode != VIEW_MODE) {
            mode = VIEW_MODE;
            getApplyButton().setEnabled(false);
            getResetButton().setEnabled(false);
            getRemoveButton().setEnabled(true);
            getAddButton().setEnabled(true);
            String isWellFormedLabelVal =
                resources.getString("IsWellFormedLabel.wellFormed");
            isWellFormedLabel.setText(isWellFormedLabelVal);
        }
    }
    public void enterEditMode() {
        if (mode != EDIT_MODE) {
            mode = EDIT_MODE;
            clonedElement = (Element) previewElement.cloneNode(true);
            getApplyButton().setEnabled(true);
            getResetButton().setEnabled(true);
        }
    }
    public void enterAddNewElementMode(Element newElement, Node parent) {
        if (mode != ADD_NEW_ELEMENT) {
            mode = ADD_NEW_ELEMENT;
            previewElement = newElement;
            clonedElement = (Element) newElement.cloneNode(true);
            parentElement = parent;
            updateNodeXmlArea(newElement);
            getApplyButton().setEnabled(true);
            getResetButton().setEnabled(true);
        }
    }
    public void updateOnDocumentChange(String mutationEventType, Node targetNode) {
        if (mode == VIEW_MODE) {
            if (this.isShowing() &&
                    shouldUpdate(mutationEventType,
                                 targetNode,
                                 getPreviewElement())) {
                setPreviewElement(getPreviewElement());
            }
        }
    }
    private boolean shouldUpdate(String mutationEventType, Node affectedNode,
            Node currentNode) {
        if (mutationEventType.equals("DOMNodeInserted")) {
            if (DOMUtilities.isAncestorOf(currentNode, affectedNode)) {
                return true;
            }
        } else if (mutationEventType.equals("DOMNodeRemoved")) {
            if (DOMUtilities.isAncestorOf(currentNode, affectedNode)) {
                return true;
            }
        } else if (mutationEventType.equals("DOMAttrModified")) {
            if (DOMUtilities.isAncestorOf(currentNode, affectedNode)
                    || currentNode == affectedNode) {
                return true;
            }
        } else if (mutationEventType.equals("DOMCharDataModified")) {
            if (DOMUtilities.isAncestorOf(currentNode, affectedNode)) {
                return true;
            }
        }
        return false;
    }
    private Element parseXml(String xmlString) {
        Document doc = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            javax.xml.parsers.DocumentBuilder parser = factory
                    .newDocumentBuilder();
            parser.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException exception)
                        throws SAXException {
                }
                public void fatalError(SAXParseException exception)
                        throws SAXException {
                }
                public void warning(SAXParseException exception)
                        throws SAXException {
                }
            });
            doc = parser.parse(new InputSource(new StringReader(xmlString)));
        } catch (ParserConfigurationException e1) {
        } catch (SAXException e1) {
        } catch (IOException e1) {
        }
        if (doc != null) {
            return doc.getDocumentElement();
        }
        return null;
    }
    public void setEditable(boolean editable) {
        getSvgInputPanel().getNodeXmlArea().setEditable(editable);
        getResetButton().setEnabled(editable);
        getApplyButton().setEnabled(editable);
        getAddButton().setEnabled(editable);
        getRemoveButton().setEnabled(editable);
        attributesTable.setEnabled(editable);
    }
    private boolean isANodePickerComponent(Component component) {
        return SwingUtilities.getAncestorOfClass(NodePickerPanel.class,
                                                 component) != null;
    }
    public boolean promptForChanges() {
        if (getApplyButton().isEnabled() && isElementModified()) {
            String confirmString = resources.getString("ConfirmDialog.message");
            int option = JOptionPane.showConfirmDialog(getSvgInputPanel(),
                                                       confirmString);
            if (option == JOptionPane.YES_OPTION) {
                getApplyButton().doClick();
            } else if (option == JOptionPane.CANCEL_OPTION) {
                return false;
            } else {
                getResetButton().doClick();
            }
        } else {
            getResetButton().doClick();
        }
        isDirty = false;
        return true;
    }
    private boolean isElementModified() {
        if (getMode() == EDIT_MODE) {
            return !DOMUtilities.getXML(previewElement).equals
                (getSvgInputPanel().getNodeXmlArea().getText());
        } else if (getMode() == ADD_NEW_ELEMENT) {
            return true;
        }
        return false;
    }
    protected class NodePickerEditListener extends FocusAdapter {
        public void focusGained(FocusEvent e) {
            if (getMode() == VIEW_MODE) {
                enterEditMode();
            }
            setEditable(controller.isEditable()
                    && controller.canEdit(previewElement));
            isDirty = isElementModified();
        }
    }
    protected class XMLAreaListener implements DocumentListener {
        public void changedUpdate(DocumentEvent e) {
            isDirty = isElementModified();
        }
        public void insertUpdate(DocumentEvent e) {
            updateNodePicker(e);
            isDirty = isElementModified();
        }
        public void removeUpdate(DocumentEvent e) {
            updateNodePicker(e);
            isDirty = isElementModified();
        }
        private void updateNodePicker(DocumentEvent e) {
            if (getMode() == EDIT_MODE) {
                updateViewAfterSvgInput
                    (parseXml(svgInputPanel.getNodeXmlArea().getText()),
                     clonedElement);
            } else if (getMode() == ADD_NEW_ELEMENT) {
                updateViewAfterSvgInput
                    (parseXml(svgInputPanel.getNodeXmlArea().getText()),
                     previewElement);
            }
        }
    }
    protected class AttributesTableModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            if (e.getType() == TableModelEvent.UPDATE && shouldProcessUpdate) {
                updateNodePicker(e);
            }
        }
        private void updateNodePicker(TableModelEvent e) {
            if (getMode() == EDIT_MODE) {
                updateElementAttributes
                    (clonedElement, (AttributesTableModel) (e.getSource()));
                updateNodeXmlArea(clonedElement);
            } else if (getMode() == ADD_NEW_ELEMENT) {
                updateElementAttributes
                    (previewElement, (AttributesTableModel) (e.getSource()));
                updateNodeXmlArea(previewElement);
            }
        }
    }
    protected class ApplyButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            isDirty = false;
            String xmlAreaText = getResults();
            if (getMode() == EDIT_MODE) {
                fireUpdateElement
                    (new NodePickerEvent
                        (NodePickerPanel.this,
                         xmlAreaText,
                         previewElement,
                         NodePickerEvent.EDIT_ELEMENT));
            } else if (getMode() == ADD_NEW_ELEMENT) {
                fireAddNewElement
                    (new NodePickerEvent
                        (NodePickerPanel.this,
                         xmlAreaText,
                         parentElement,
                         NodePickerEvent.ADD_NEW_ELEMENT));
            }
            enterViewMode();
        }
    }
    protected class ResetButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            isDirty = false;
            setPreviewElement(getPreviewElement());
        }
    }
    protected class AddButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (getMode() == VIEW_MODE) {
                enterEditMode();
            }
            DefaultTableModel model =
                (DefaultTableModel) attributesTable.getModel();
            shouldProcessUpdate = false;
            model.addRow((Vector) null);
            shouldProcessUpdate = true;
        }
    }
    protected class RemoveButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (getMode() == VIEW_MODE) {
                enterEditMode();
            }
            Element contextElement = clonedElement;
            if (getMode() == ADD_NEW_ELEMENT) {
                contextElement = previewElement;
            }
            DefaultTableModel model =
                (DefaultTableModel) attributesTable.getModel();
            int[] selectedRows = attributesTable.getSelectedRows();
            for (int i = 0; i < selectedRows.length; i++) {
                String attrName = (String) model.getValueAt(selectedRows[i], 0);
                if (attrName != null) {
                    String prefix = DOMUtilities.getPrefix(attrName);
                    String localName = DOMUtilities.getLocalName(attrName);
                    String namespaceURI = getNamespaceURI(prefix);
                    contextElement.removeAttributeNS(namespaceURI, localName);
                }
            }
            shouldProcessUpdate = false;
            updateAttributesTable(contextElement);
            shouldProcessUpdate = true;
            updateNodeXmlArea(contextElement);
        }
    }
    public Action getAction(String key) throws MissingListenerException {
        return (Action) listeners.get(key);
    }
    public static class AttributesTableModel extends DefaultTableModel {
        public AttributesTableModel(int rowCount, int columnCount) {
            super(rowCount, columnCount);
        }
        public String getColumnName(int column) {
            if (column == 0) {
                return resources.getString("AttributesTable.column1");
            } else {
                return resources.getString("AttributesTable.column2");
            }
        }
        public Object getValueForName(Object attrName) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, 0) != null
                        && getValueAt(i, 0).equals(attrName)) {
                    return getValueAt(i, 1);
                }
            }
            return null;
        }
        public Object getAttrNameAt(int i) {
            return getValueAt(i, 0);
        }
        public Object getAttrValueAt(int i) {
            return getValueAt(i, 1);
        }
        public int getRow(Object attrName) {
            for (int i = 0; i < getRowCount(); i++) {
                if (getValueAt(i, 0) != null
                        && getValueAt(i, 0).equals(attrName)) {
                    return i;
                }
            }
            return -1;
        }
    }
    public void fireUpdateElement(NodePickerEvent event) {
        Object[] listeners = eventListeners.getListenerList();
        int length = listeners.length;
        for (int i = 0; i < length; i += 2) {
            if (listeners[i] == NodePickerListener.class) {
                ((NodePickerListener) listeners[i + 1])
                        .updateElement(event);
            }
        }
    }
    public void fireAddNewElement(NodePickerEvent event) {
        Object[] listeners = eventListeners.getListenerList();
        int length = listeners.length;
        for (int i = 0; i < length; i += 2) {
            if (listeners[i] == NodePickerListener.class) {
                ((NodePickerListener) listeners[i + 1])
                        .addNewElement(event);
            }
        }
    }
    public void addListener(NodePickerListener listener) {
        eventListeners.add(NodePickerListener.class, listener);
    }
    public static class NodePickerEvent extends EventObject {
        public static final int EDIT_ELEMENT = 1;
        public static final int ADD_NEW_ELEMENT = 2;
        private int type;
        private String result;
        private Node contextNode;
        public NodePickerEvent(Object source, String result, Node contextNode,
                               int type) {
            super(source);
            this.result = result;
            this.contextNode = contextNode;
        }
        public String getResult() {
            return result;
        }
        public Node getContextNode() {
            return contextNode;
        }
        public int getType() {
            return type;
        }
    }
    public static interface NodePickerListener extends EventListener {
        void updateElement(NodePickerEvent event);
        void addNewElement(NodePickerEvent event);
    }
    public static class NodePickerAdapter implements NodePickerListener {
        public void addNewElement(NodePickerEvent event) {
        }
        public void updateElement(NodePickerEvent event) {
        }
    }
    protected class SVGInputPanel extends JPanel {
        protected XMLTextEditor nodeXmlArea;
        public SVGInputPanel() {
            super(new BorderLayout());
            add(new JScrollPane(getNodeXmlArea()));
        }
        protected XMLTextEditor getNodeXmlArea() {
            if (nodeXmlArea == null) {
                nodeXmlArea = new XMLTextEditor();
                nodeXmlArea.setEditable(true);
            }
            return nodeXmlArea;
        }
    }
    public static class NameEditorDialog extends JDialog implements ActionMap {
        public static final int OK_OPTION = 0;
        public static final int CANCEL_OPTION = 1;
        protected static final String RESOURCES =
            "org.apache.batik.apps.svgbrowser.resources.NameEditorDialogMessages";
        protected static ResourceBundle bundle;
        protected static ResourceManager resources;
        static {
            bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
            resources = new ResourceManager(bundle);
        }
        protected int returnCode;
        protected JPanel mainPanel;
        protected ButtonFactory buttonFactory;
        protected JLabel nodeNameLabel;
        protected JTextField nodeNameField;
        protected JButton okButton;
        protected JButton cancelButton;
        protected Map listeners = new HashMap(10);
        public NameEditorDialog(Frame frame) {
            super(frame, true);
            this.setResizable(false);
            this.setModal(true);
            initialize();
        }
        protected void initialize() {
            this.setSize(resources.getInteger("Dialog.width"),
                         resources.getInteger("Dialog.height"));
            this.setTitle(resources.getString("Dialog.title"));
            addButtonActions();
            this.setContentPane(getMainPanel());
        }
        protected ButtonFactory getButtonFactory() {
            if (buttonFactory == null) {
                buttonFactory = new ButtonFactory(bundle, this);
            }
            return buttonFactory;
        }
        protected void addButtonActions() {
            listeners.put("OKButtonAction", new OKButtonAction());
            listeners.put("CancelButtonAction", new CancelButtonAction());
        }
        public int showDialog() {
            setVisible(true);
            return returnCode;
        }
        protected JButton getOkButton() {
            if (okButton == null) {
                okButton = getButtonFactory().createJButton("OKButton");
                this.getRootPane().setDefaultButton(okButton);
            }
            return okButton;
        }
        protected JButton getCancelButton() {
            if (cancelButton == null) {
                cancelButton = getButtonFactory().createJButton("CancelButton");
            }
            return cancelButton;
        }
        protected JPanel getMainPanel() {
            if (mainPanel == null) {
                mainPanel = new JPanel(new GridBagLayout());
                GridBagConstraints gridBag = new GridBagConstraints();
                gridBag.gridx = 1;
                gridBag.gridy = 1;
                gridBag.fill = GridBagConstraints.NONE;
                gridBag.insets = new Insets(5, 5, 5, 5);
                mainPanel.add(getNodeNameLabel(), gridBag);
                gridBag.gridx = 2;
                gridBag.weightx = 1.0;
                gridBag.weighty = 1.0;
                gridBag.fill = GridBagConstraints.HORIZONTAL;
                gridBag.anchor = GridBagConstraints.CENTER;
                mainPanel.add(getNodeNameField(), gridBag);
                gridBag.gridx = 1;
                gridBag.gridy = 2;
                gridBag.weightx = 0;
                gridBag.weighty = 0;
                gridBag.anchor = GridBagConstraints.EAST;
                gridBag.fill = GridBagConstraints.HORIZONTAL;
                mainPanel.add(getOkButton(), gridBag);
                gridBag.gridx = 2;
                gridBag.gridy = 2;
                gridBag.anchor = GridBagConstraints.EAST;
                mainPanel.add(getCancelButton(), gridBag);
            }
            return mainPanel;
        }
        public JLabel getNodeNameLabel() {
            if (nodeNameLabel == null) {
                nodeNameLabel = new JLabel();
                nodeNameLabel.setText(resources.getString("Dialog.label"));
            }
            return nodeNameLabel;
        }
        protected JTextField getNodeNameField() {
            if (nodeNameField == null) {
                nodeNameField = new JTextField();
            }
            return nodeNameField;
        }
        public String getResults() {
            return nodeNameField.getText();
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
        public Action getAction(String key) throws MissingListenerException {
            return (Action) listeners.get(key);
        }
    }
}
