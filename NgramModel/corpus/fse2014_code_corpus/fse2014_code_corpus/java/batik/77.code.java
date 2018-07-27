package org.apache.batik.apps.svgbrowser;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.batik.apps.svgbrowser.DOMDocumentTree.DOMDocumentTreeAdapter;
import org.apache.batik.apps.svgbrowser.DOMDocumentTree.DOMDocumentTreeEvent;
import org.apache.batik.apps.svgbrowser.DOMDocumentTree.DropCompletedInfo;
import org.apache.batik.apps.svgbrowser.DropDownHistoryModel.RedoPopUpMenuModel;
import org.apache.batik.apps.svgbrowser.DropDownHistoryModel.UndoPopUpMenuModel;
import org.apache.batik.apps.svgbrowser.HistoryBrowser.DocumentCommandController;
import org.apache.batik.apps.svgbrowser.NodePickerPanel.NameEditorDialog;
import org.apache.batik.apps.svgbrowser.NodePickerPanel.NodePickerAdapter;
import org.apache.batik.apps.svgbrowser.NodePickerPanel.NodePickerEvent;
import org.apache.batik.apps.svgbrowser.NodeTemplates.NodeTemplateDescriptor;
import org.apache.batik.bridge.svg12.ContentManager;
import org.apache.batik.bridge.svg12.DefaultXBLManager;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.svg12.XBLOMContentElement;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.dom.xbl.NodeXBL;
import org.apache.batik.dom.xbl.XBLManager;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.util.gui.DropDownComponent;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MutationEvent;
public class DOMViewer extends JFrame implements ActionMap {
    protected static final String RESOURCE =
        "org.apache.batik.apps.svgbrowser.resources.DOMViewerMessages";
    protected static ResourceBundle bundle;
    protected static ResourceManager resources;
    static {
        bundle = ResourceBundle.getBundle(RESOURCE, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }
    protected Map listeners = new HashMap();
    protected ButtonFactory buttonFactory;
    protected Panel panel;
    protected boolean showWhitespace = true;
    protected boolean isCapturingClickEnabled;
    protected DOMViewerController domViewerController;
    protected ElementOverlayManager elementOverlayManager;
    protected boolean isElementOverlayEnabled;
    protected HistoryBrowserInterface historyBrowserInterface;
    protected boolean canEdit = true;
    protected JToggleButton overlayButton;
    public DOMViewer(DOMViewerController controller) {
        super(resources.getString("Frame.title"));
        setSize(resources.getInteger("Frame.width"),
                resources.getInteger("Frame.height"));
        domViewerController = controller;
        elementOverlayManager = domViewerController.createSelectionManager();
        if (elementOverlayManager != null) {
            elementOverlayManager
                    .setController(new DOMViewerElementOverlayController());
        }
        historyBrowserInterface =
            new HistoryBrowserInterface
                (new DocumentCommandController(controller));
        listeners.put("CloseButtonAction", new CloseButtonAction());
        listeners.put("UndoButtonAction", new UndoButtonAction());
        listeners.put("RedoButtonAction", new RedoButtonAction());
        listeners.put("CapturingClickButtonAction",
                      new CapturingClickButtonAction());
        listeners.put("OverlayButtonAction", new OverlayButtonAction());
        panel = new Panel();
        getContentPane().add(panel);
        JPanel p = new JPanel(new BorderLayout());
        JCheckBox cb =
            new JCheckBox(resources.getString("ShowWhitespaceCheckbox.text"));
        cb.setSelected(showWhitespace);
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent ie) {
                setShowWhitespace(ie.getStateChange() == ItemEvent.SELECTED);
            }
        });
        p.add(cb, BorderLayout.WEST);
        p.add(getButtonFactory().createJButton("CloseButton"),
              BorderLayout.EAST);
        getContentPane().add(p, BorderLayout.SOUTH);
        Document document = domViewerController.getDocument();
        if (document != null) {
            panel.setDocument(document, null);
        }
    }
    public void setShowWhitespace(boolean state) {
        showWhitespace = state;
        if (panel.document != null)
            panel.setDocument(panel.document);
    }
    public void setDocument(Document doc) {
        panel.setDocument(doc);
    }
    public void setDocument(Document doc, ViewCSS view) {
        panel.setDocument(doc, view);
    }
    public boolean canEdit() {
        return domViewerController.canEdit() && canEdit;
    }
    public void setEditable(boolean canEdit) {
        this.canEdit = canEdit;
    }
    public void selectNode(Node node) {
        panel.selectNode(node);
    }
    public void resetHistory() {
        historyBrowserInterface.getHistoryBrowser().resetHistory();
    }
    private ButtonFactory getButtonFactory() {
        if (buttonFactory == null) {
            buttonFactory = new ButtonFactory(bundle, this);
        }
        return buttonFactory;
    }
    public Action getAction(String key) throws MissingListenerException {
        return (Action)listeners.get(key);
    }
    private void addChangesToHistory() {
        historyBrowserInterface.performCurrentCompoundCommand();
    }
    protected class CloseButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (panel.attributePanel.panelHiding()) {
                panel.tree.setSelectionRow(0);
                DOMViewer.this.dispose();
            }
        }
    }
    protected class UndoButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            addChangesToHistory();
            historyBrowserInterface.getHistoryBrowser().undo();
        }
    }
    protected class RedoButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            addChangesToHistory();
            historyBrowserInterface.getHistoryBrowser().redo();
        }
    }
    protected class CapturingClickButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            JToggleButton btn = (JToggleButton) e.getSource();
            isCapturingClickEnabled = btn.isSelected();
            if (!isCapturingClickEnabled) {
                btn.setToolTipText
                    (resources.getString("CapturingClickButton.tooltip"));
            } else {
                btn.setToolTipText
                    (resources.getString("CapturingClickButton.disableText"));
            }
        }
    }
    protected void toggleOverlay() {
        isElementOverlayEnabled = overlayButton.isSelected();
        if (!isElementOverlayEnabled) {
            overlayButton.setToolTipText
                (resources.getString("OverlayButton.tooltip"));
        } else {
            overlayButton.setToolTipText
                (resources.getString("OverlayButton.disableText"));
        }
        if (elementOverlayManager != null) {
            elementOverlayManager.setOverlayEnabled(isElementOverlayEnabled);
            elementOverlayManager.repaint();
        }
    }
    protected class OverlayButtonAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            toggleOverlay();
        }
    }
    protected class DOMViewerNodePickerController
            implements NodePickerController {
        public boolean isEditable() {
            return DOMViewer.this.canEdit();
        }
        public boolean canEdit(Element el) {
            if (panel == null || panel.document == null || true
                    ) {
                return true;
            }
            return false;
        }
    }
    protected class DOMViewerDOMDocumentTreeController
            implements DOMDocumentTreeController {
        public boolean isDNDSupported() {
            return canEdit();
        }
    }
    protected class DOMViewerElementOverlayController
            implements ElementOverlayController {
        public boolean isOverlayEnabled() {
            return canEdit() && isElementOverlayEnabled;
        }
    }
    public class Panel extends JPanel {
        public static final String NODE_INSERTED = "DOMNodeInserted";
        public static final String NODE_REMOVED = "DOMNodeRemoved";
        public static final String ATTRIBUTE_MODIFIED = "DOMAttrModified";
        public static final String CHAR_DATA_MODIFIED = "DOMCharacterDataModified";
        protected Document document;
        protected EventListener nodeInsertion;
        protected EventListener nodeRemoval;
        protected EventListener attrModification;
        protected EventListener charDataModification;
        protected EventListener capturingListener;
        protected ViewCSS viewCSS;
        protected DOMDocumentTree tree;
        protected JSplitPane splitPane;
        protected JPanel rightPanel = new JPanel(new BorderLayout());
        protected JTable propertiesTable = new JTable();
        protected NodePickerPanel attributePanel =
            new NodePickerPanel(new DOMViewerNodePickerController());
        {
            attributePanel.addListener(new NodePickerAdapter() {
                public void updateElement(NodePickerEvent event) {
                    String result = event.getResult();
                    Element targetElement = (Element) event.getContextNode();
                    Element newElem = wrapAndParse(result, targetElement);
                    addChangesToHistory();
                    AbstractCompoundCommand cmd = historyBrowserInterface
                            .createNodeChangedCommand(newElem);
                    Node parent = targetElement.getParentNode();
                    Node nextSibling = targetElement.getNextSibling();
                    cmd.addCommand(historyBrowserInterface
                            .createRemoveChildCommand(parent, targetElement));
                    cmd.addCommand(historyBrowserInterface
                            .createInsertChildCommand(parent, nextSibling,
                                    newElem));
                    historyBrowserInterface.performCompoundUpdateCommand(cmd);
                    attributePanel.setPreviewElement(newElem);
                }
                public void addNewElement(NodePickerEvent event) {
                    String result = event.getResult();
                    Element targetElement = (Element) event.getContextNode();
                    Element newElem = wrapAndParse(result, targetElement);
                    addChangesToHistory();
                    historyBrowserInterface.appendChild(targetElement,
                            newElem);
                    attributePanel.setPreviewElement(newElem);
                }
                private Element wrapAndParse(String toParse, Node startingNode) {
                    Map prefixMap = new HashMap();
                    int j = 0;
                    for (Node currentNode = startingNode;
                         currentNode != null;
                         currentNode = currentNode.getParentNode()) {
                        NamedNodeMap nMap = currentNode.getAttributes();
                        for (int i = 0; nMap != null && i < nMap.getLength(); i++) {
                            Attr atr = (Attr) nMap.item(i);
                            String prefix = atr.getPrefix();
                            String localName = atr.getLocalName();
                            String namespaceURI = atr.getValue();
                            if (prefix != null
                                    && prefix.equals(SVGConstants.XMLNS_PREFIX)) {
                                String attrName = SVGConstants.XMLNS_PREFIX
                                        + ":" + localName;
                                if (!prefixMap.containsKey(attrName)) {
                                    prefixMap.put(attrName, namespaceURI);
                                }
                            }
                            if ((j != 0 || currentNode == document
                                    .getDocumentElement())
                                    && atr.getNodeName().equals(
                                            SVGConstants.XMLNS_PREFIX)
                                    && !prefixMap
                                            .containsKey(SVGConstants.XMLNS_PREFIX)) {
                                prefixMap.put(SVGConstants.XMLNS_PREFIX, atr
                                        .getNodeValue());
                            }
                        }
                        j++;
                    }
                    Document doc = panel.document;
                    SAXDocumentFactory df = new SAXDocumentFactory(
                            doc.getImplementation(),
                            XMLResourceDescriptor.getXMLParserClassName());
                    URL urlObj = null;
                    if (doc instanceof SVGOMDocument) {
                        urlObj = ((SVGOMDocument) doc).getURLObject();
                    }
                    String uri = (urlObj == null) ? "" : urlObj.toString();
                    Node node = DOMUtilities.parseXML(toParse, doc, uri,
                            prefixMap, SVGConstants.SVG_SVG_TAG, df);
                    return (Element) node.getFirstChild();
                }
                private void selectNewNode(final Element elem) {
                    domViewerController.performUpdate(new Runnable() {
                        public void run() {
                            selectNode(elem);
                        };
                    });
                }
            });
        }
        protected GridBagConstraints attributePanelLayout =
            new GridBagConstraints();
        {
            attributePanelLayout.gridx = 1;
            attributePanelLayout.gridy = 1;
            attributePanelLayout.gridheight = 2;
            attributePanelLayout.weightx = 1.0;
            attributePanelLayout.weighty = 1.0;
            attributePanelLayout.fill = GridBagConstraints.BOTH;
        }
        protected GridBagConstraints propertiesTableLayout =
            new GridBagConstraints();
        {
            propertiesTableLayout.gridx = 1;
            propertiesTableLayout.gridy = 3;
            propertiesTableLayout.weightx = 1.0;
            propertiesTableLayout.weighty = 1.0;
            propertiesTableLayout.fill = GridBagConstraints.BOTH;
        }
        protected JPanel elementPanel = new JPanel(new GridBagLayout());
        {
            JScrollPane pane2 = new JScrollPane();
            pane2.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                    .createEmptyBorder(2, 0, 2, 2), BorderFactory
                    .createCompoundBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createEmptyBorder(), resources
                                    .getString("CSSValuesPanel.title")),
                            BorderFactory.createLoweredBevelBorder())));
            pane2.getViewport().add(propertiesTable);
            elementPanel.add(attributePanel, attributePanelLayout);
            elementPanel.add(pane2, propertiesTableLayout);
        }
        protected class CharacterPanel extends JPanel {
            protected Node node;
            protected JTextArea textArea = new JTextArea();
            public CharacterPanel(BorderLayout layout) {
                super(layout);
            }
            public JTextArea getTextArea() {
                return textArea;
            }
            public void setTextArea(JTextArea textArea) {
                this.textArea = textArea;
            }
            public Node getNode() {
                return node;
            }
            public void setNode(Node node) {
                this.node = node;
            }
        }
        protected CharacterPanel characterDataPanel = new CharacterPanel(new BorderLayout());
        {
            characterDataPanel.setBorder
                (BorderFactory.createCompoundBorder
                 (BorderFactory.createEmptyBorder(2, 0, 2, 2),
                  BorderFactory.createCompoundBorder
                  (BorderFactory.createTitledBorder
                   (BorderFactory.createEmptyBorder(),
                    resources.getString("CDataPanel.title")),
                   BorderFactory.createLoweredBevelBorder())));
            JScrollPane pane = new JScrollPane();
            JTextArea textArea = new JTextArea();
            characterDataPanel.setTextArea(textArea);
            pane.getViewport().add(textArea);
            characterDataPanel.add(pane);
            textArea.setEditable(true);
            textArea.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    if (canEdit()) {
                        Node contextNode = characterDataPanel.getNode();
                        String newValue = characterDataPanel.getTextArea()
                                .getText();
                        switch (contextNode.getNodeType()) {
                        case Node.COMMENT_NODE:
                        case Node.TEXT_NODE:
                        case Node.CDATA_SECTION_NODE:
                            addChangesToHistory();
                            historyBrowserInterface.setNodeValue(contextNode,
                                    newValue);
                            break;
                        }
                    }
                }
            });
        }
        protected JTextArea documentInfo = new JTextArea();
        protected JPanel documentInfoPanel = new JPanel(new BorderLayout());
        {
            documentInfoPanel.setBorder
                (BorderFactory.createCompoundBorder
                 (BorderFactory.createEmptyBorder(2, 0, 2, 2),
                  BorderFactory.createCompoundBorder
                  (BorderFactory.createTitledBorder
                   (BorderFactory.createEmptyBorder(),
                    resources.getString("DocumentInfoPanel.title")),
                   BorderFactory.createLoweredBevelBorder())));
            JScrollPane pane = new JScrollPane();
            pane.getViewport().add(documentInfo);
            documentInfoPanel.add(pane);
            documentInfo.setEditable(false);
        }
        public Panel() {
            super(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder
                      (BorderFactory.createEmptyBorder(),
                       resources.getString("DOMViewerPanel.title")));
            JToolBar tb =
                new JToolBar(resources.getString("DOMViewerToolbar.name"));
            tb.setFloatable(false);
            JButton undoButton = getButtonFactory().createJToolbarButton("UndoButton");
            undoButton.setDisabledIcon
                (new ImageIcon
                    (getClass().getResource(resources.getString("UndoButton.disabledIcon"))));
            DropDownComponent undoDD = new DropDownComponent(undoButton);
            undoDD.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
            undoDD.setMaximumSize(new Dimension(44, 25));
            undoDD.setPreferredSize(new Dimension(44, 25));
            tb.add(undoDD);
            UndoPopUpMenuModel undoModel = new UndoPopUpMenuModel(undoDD
                    .getPopupMenu(), historyBrowserInterface);
            undoDD.getPopupMenu().setModel(undoModel);
            JButton redoButton = getButtonFactory().createJToolbarButton("RedoButton");
            redoButton.setDisabledIcon
                (new ImageIcon
                    (getClass().getResource(resources.getString("RedoButton.disabledIcon"))));
            DropDownComponent redoDD = new DropDownComponent(redoButton);
            redoDD.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
            redoDD.setMaximumSize(new Dimension(44, 25));
            redoDD.setPreferredSize(new Dimension(44, 25));
            tb.add(redoDD);
            RedoPopUpMenuModel redoModel = new RedoPopUpMenuModel(redoDD
                    .getPopupMenu(), historyBrowserInterface);
            redoDD.getPopupMenu().setModel(redoModel);
            JToggleButton capturingClickButton = getButtonFactory()
                    .createJToolbarToggleButton("CapturingClickButton");
            capturingClickButton.setEnabled(true);
            capturingClickButton.setPreferredSize(new Dimension(32, 25));
            tb.add(capturingClickButton);
            overlayButton =
                getButtonFactory().createJToolbarToggleButton("OverlayButton");
            overlayButton.setEnabled(true);
            overlayButton.setPreferredSize(new Dimension(32, 25));
            tb.add(overlayButton);
            add(tb, BorderLayout.NORTH);
            TreeNode root;
            root = new DefaultMutableTreeNode
                (resources.getString("EmptyDocument.text"));
            tree = new DOMDocumentTree
                (root, new DOMViewerDOMDocumentTreeController());
            tree.setCellRenderer(new NodeRenderer());
            tree.putClientProperty("JTree.lineStyle", "Angled");
            tree.addListener(new DOMDocumentTreeAdapter() {
                public void dropCompleted(DOMDocumentTreeEvent event) {
                    DropCompletedInfo info = (DropCompletedInfo) event
                            .getSource();
                    addChangesToHistory();
                    AbstractCompoundCommand cmd = historyBrowserInterface
                            .createNodesDroppedCommand(info.getChildren());
                    int n = info.getChildren().size();
                    for (int i = 0; i < n; i++) {
                        Node node = (Node) info.getChildren().get(i);
                        if (!DOMUtilities.isAnyNodeAncestorOf(info
                                .getChildren(), node)) {
                            cmd.addCommand(historyBrowserInterface
                                    .createInsertChildCommand(info.getParent(),
                                            info.getSibling(), node));
                        }
                    }
                    historyBrowserInterface.performCompoundUpdateCommand(cmd);
                }
            });
            tree.addTreeSelectionListener(new DOMTreeSelectionListener());
            tree.addMouseListener(new TreePopUpListener());
            JScrollPane treePane = new JScrollPane();
            treePane.setBorder(BorderFactory.createCompoundBorder
                               (BorderFactory.createEmptyBorder(2, 2, 2, 0),
                                BorderFactory.createCompoundBorder
                                (BorderFactory.createTitledBorder
                                 (BorderFactory.createEmptyBorder(),
                                  resources.getString("DOMViewer.title")),
                                 BorderFactory.createLoweredBevelBorder())));
            treePane.getViewport().add(tree);
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                       true, 
                                       treePane,
                                       rightPanel);
            int loc = resources.getInteger("SplitPane.dividerLocation");
            splitPane.setDividerLocation(loc);
            add(splitPane);
        }
        public void setDocument(Document doc) {
            setDocument(doc, null);
        }
        public void setDocument(Document doc, ViewCSS view) {
            if (document != null) {
                if (document != doc) {
                    removeDomMutationListeners(document);
                    addDomMutationListeners(doc);
                    removeCapturingListener(document);
                    addCapturingListener(doc);
                }
            }
            else {
                addDomMutationListeners(doc);
                addCapturingListener(doc);
            }
            resetHistory();
            document = doc;
            viewCSS = view;
            TreeNode root = createTree(doc, showWhitespace);
            ((DefaultTreeModel) tree.getModel()).setRoot(root);
            if (rightPanel.getComponentCount() != 0) {
                rightPanel.remove(0);
                splitPane.revalidate();
                splitPane.repaint();
            }
        }
        protected void addDomMutationListeners(Document doc) {
            EventTarget target = (EventTarget) doc;
            nodeInsertion = new NodeInsertionHandler();
            target.addEventListener(NODE_INSERTED, nodeInsertion, true);
            nodeRemoval = new NodeRemovalHandler();
            target.addEventListener(NODE_REMOVED, nodeRemoval, true);
            attrModification = new AttributeModificationHandler();
            target.addEventListener(ATTRIBUTE_MODIFIED, attrModification,
                    true);
            charDataModification = new CharDataModificationHandler();
            target.addEventListener(CHAR_DATA_MODIFIED,
                    charDataModification, true);
        }
        protected void removeDomMutationListeners(Document doc) {
            if (doc != null) {
                EventTarget target = (EventTarget) doc;
                target.removeEventListener(NODE_INSERTED, nodeInsertion, true);
                target.removeEventListener(NODE_REMOVED, nodeRemoval, true);
                target.removeEventListener(ATTRIBUTE_MODIFIED,
                        attrModification, true);
                target.removeEventListener(CHAR_DATA_MODIFIED,
                        charDataModification, true);
            }
        }
        protected void addCapturingListener(Document doc) {
            EventTarget target = (EventTarget) doc.getDocumentElement();
            capturingListener = new CapturingClickHandler();
            target.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE,
                    capturingListener, true);
        }
        protected void removeCapturingListener(Document doc) {
            if (doc != null) {
                EventTarget target = (EventTarget) doc.getDocumentElement();
                target.removeEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE,
                        capturingListener, true);
            }
        }
        protected class NodeInsertionHandler implements EventListener {
            public void handleEvent(final Event evt) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        MutationEvent mevt = (MutationEvent) evt;
                        Node targetNode = (Node) mevt.getTarget();
                        DefaultMutableTreeNode parentNode = findNode(tree,
                                targetNode.getParentNode());
                        DefaultMutableTreeNode insertedNode =
                            (DefaultMutableTreeNode)
                            createTree(targetNode, showWhitespace);
                        DefaultTreeModel model =
                            (DefaultTreeModel) tree.getModel();
                        DefaultMutableTreeNode newParentNode =
                            (DefaultMutableTreeNode)
                            createTree(targetNode.getParentNode(),
                                       showWhitespace);
                        int index = findIndexToInsert(parentNode, newParentNode);
                        if (index != -1) {
                            model.insertNodeInto
                                (insertedNode, parentNode, index);
                        }
                        attributePanel.updateOnDocumentChange(mevt.getType(),
                                                              targetNode);
                    }
                };
                refreshGUI(runnable);
                registerDocumentChange((MutationEvent)evt);
            }
            protected int findIndexToInsert
                    (DefaultMutableTreeNode parentNode,
                     DefaultMutableTreeNode newParentNode) {
                int index = -1;
                if (parentNode == null || newParentNode == null) {
                    return index;
                }
                Enumeration oldChildren = parentNode.children();
                Enumeration newChildren = newParentNode.children();
                int count = 0;
                while (oldChildren.hasMoreElements()) {
                    DefaultMutableTreeNode currentOldChild =
                        (DefaultMutableTreeNode) oldChildren.nextElement();
                    DefaultMutableTreeNode currentNewChild =
                        (DefaultMutableTreeNode) newChildren.nextElement();
                    Node oldChild =
                        ((NodeInfo) currentOldChild.getUserObject()).getNode();
                    Node newChild =
                        ((NodeInfo) currentNewChild.getUserObject()).getNode();
                    if (oldChild != newChild) {
                        return count;
                    }
                    count++;
                }
                return count;
            }
        }
        protected class NodeRemovalHandler implements EventListener {
            public void handleEvent(final Event evt) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        MutationEvent mevt = (MutationEvent) evt;
                        Node targetNode = (Node) mevt.getTarget();
                        DefaultMutableTreeNode treeNode = findNode(tree,
                                targetNode);
                        DefaultTreeModel model = (DefaultTreeModel) tree
                                .getModel();
                        if (treeNode != null) {
                            model.removeNodeFromParent(treeNode);
                        }
                        attributePanel.updateOnDocumentChange(mevt.getType(),
                                targetNode);
                    }
                };
                refreshGUI(runnable);
                registerDocumentChange((MutationEvent)evt);
            }
        }
        protected class AttributeModificationHandler implements EventListener {
            public void handleEvent(final Event evt) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        MutationEvent mevt = (MutationEvent) evt;
                        Element targetElement = (Element) mevt.getTarget();
                        DefaultTreeModel model = (DefaultTreeModel) tree
                                .getModel();
                        model.nodeChanged(findNode(tree, targetElement));
                        attributePanel.updateOnDocumentChange(mevt.getType(),
                                targetElement);
                    }
                };
                refreshGUI(runnable);
                registerDocumentChange((MutationEvent) evt);
            }
        }
        protected class CharDataModificationHandler implements EventListener {
            public void handleEvent(final Event evt) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        MutationEvent mevt = (MutationEvent) evt;
                        Node targetNode = (Node) mevt.getTarget();
                        if (characterDataPanel.getNode() == targetNode) {
                            characterDataPanel.getTextArea().setText(
                                    targetNode.getNodeValue());
                            attributePanel.updateOnDocumentChange(mevt
                                    .getType(), targetNode);
                        }
                    }
                };
                refreshGUI(runnable);
                if (characterDataPanel.getNode() == evt.getTarget()) {
                    registerDocumentChange((MutationEvent) evt);
                }
            }
        }
        protected void refreshGUI(Runnable runnable) {
            if (canEdit()) {
                try {
                    SwingUtilities.invokeAndWait(runnable);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        protected void registerNodeInserted(MutationEvent mevt) {
            Node targetNode = (Node) mevt.getTarget();
            historyBrowserInterface.addToCurrentCompoundCommand
                (historyBrowserInterface.createNodeInsertedCommand
                    (targetNode.getParentNode(),
                     targetNode.getNextSibling(),
                     targetNode));
        }
        protected void registerNodeRemoved(MutationEvent mevt) {
            Node targetNode = (Node) mevt.getTarget();
            historyBrowserInterface.addToCurrentCompoundCommand
                (historyBrowserInterface.createNodeRemovedCommand
                    (mevt.getRelatedNode(),
                     targetNode.getNextSibling(),
                     targetNode));
        }
        protected void registerAttributeAdded(MutationEvent mevt) {
            Element targetElement = (Element) mevt.getTarget();
            historyBrowserInterface.addToCurrentCompoundCommand
                (historyBrowserInterface.createAttributeAddedCommand
                    (targetElement,
                     mevt.getAttrName(),
                     mevt.getNewValue(),
                     null));
        }
        protected void registerAttributeRemoved(MutationEvent mevt) {
            Element targetElement = (Element) mevt.getTarget();
            historyBrowserInterface.addToCurrentCompoundCommand
                (historyBrowserInterface.createAttributeRemovedCommand
                    (targetElement,
                     mevt.getAttrName(),
                     mevt.getPrevValue(),
                     null));
        }
        protected void registerAttributeModified(MutationEvent mevt) {
            Element targetElement = (Element) mevt.getTarget();
            historyBrowserInterface.addToCurrentCompoundCommand
                (historyBrowserInterface.createAttributeModifiedCommand
                    (targetElement,
                     mevt.getAttrName(),
                     mevt.getPrevValue(),
                     mevt.getNewValue(),
                     null));
        }
        protected void registerAttributeChanged(MutationEvent mevt) {
            switch (mevt.getAttrChange()) {
                case MutationEvent.ADDITION:
                    registerAttributeAdded(mevt);
                    break;
                case MutationEvent.REMOVAL:
                    registerAttributeRemoved(mevt);
                    break;
                case MutationEvent.MODIFICATION:
                    registerAttributeModified(mevt);
                    break;
                default:
                    registerAttributeModified(mevt);
                    break;
            }
        }
        protected void registerCharDataModified(MutationEvent mevt) {
            Node targetNode = (Node) mevt.getTarget();
            historyBrowserInterface.addToCurrentCompoundCommand
                (historyBrowserInterface.createCharDataModifiedCommand
                    (targetNode,
                     mevt.getPrevValue(),
                     mevt.getNewValue()));
        }
        protected boolean shouldRegisterDocumentChange() {
            return canEdit() &&
                historyBrowserInterface.getHistoryBrowser().getState()
                    == HistoryBrowser.IDLE;
        }
        protected void registerDocumentChange(MutationEvent mevt) {
            if (shouldRegisterDocumentChange()) {
                String type = mevt.getType();
                if (type.equals(NODE_INSERTED)) {
                    registerNodeInserted(mevt);
                } else if (type.equals(NODE_REMOVED)) {
                    registerNodeRemoved(mevt);
                } else if (type.equals(ATTRIBUTE_MODIFIED)) {
                    registerAttributeChanged(mevt);
                } else if (type.equals(CHAR_DATA_MODIFIED)) {
                    registerCharDataModified(mevt);
                }
            }
        }
        protected class CapturingClickHandler implements EventListener {
            public void handleEvent(Event evt) {
                if (isCapturingClickEnabled) {
                    Element targetElement = (Element) evt.getTarget();
                    selectNode(targetElement);
                }
            }
        }
        protected MutableTreeNode createTree(Node node,
                                             boolean showWhitespace) {
            DefaultMutableTreeNode result;
            result = new DefaultMutableTreeNode(new NodeInfo(node));
            for (Node n = node.getFirstChild();
                 n != null;
                 n = n.getNextSibling()) {
                if (!showWhitespace && (n instanceof org.w3c.dom.Text)) {
                    String txt = n.getNodeValue();
                    if (txt.trim().length() == 0)
                        continue;
                }
                result.add(createTree(n, showWhitespace));
            }
            if (node instanceof NodeXBL) {
                Element shadowTree = ((NodeXBL) node).getXblShadowTree();
                if (shadowTree != null) {
                    DefaultMutableTreeNode shadowNode
                        = new DefaultMutableTreeNode
                            (new ShadowNodeInfo(shadowTree));
                    shadowNode.add(createTree(shadowTree, showWhitespace));
                    result.add(shadowNode);
                }
            }
            if (node instanceof XBLOMContentElement) {
                AbstractDocument doc
                    = (AbstractDocument) node.getOwnerDocument();
                XBLManager xm = doc.getXBLManager();
                if (xm instanceof DefaultXBLManager) {
                    DefaultMutableTreeNode selectedContentNode
                        = new DefaultMutableTreeNode(new ContentNodeInfo(node));
                    DefaultXBLManager dxm = (DefaultXBLManager) xm;
                    ContentManager cm = dxm.getContentManager(node);
                    if (cm != null) {
                        NodeList nl
                            = cm.getSelectedContent((XBLOMContentElement) node);
                        for (int i = 0; i < nl.getLength(); i++) {
                            selectedContentNode.add(createTree(nl.item(i),
                                                               showWhitespace));
                        }
                        result.add(selectedContentNode);
                    }
                }
            }
            return result;
        }
        protected DefaultMutableTreeNode findNode(JTree theTree, Node node) {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) theTree
                    .getModel().getRoot();
            Enumeration treeNodes = root.breadthFirstEnumeration();
            while (treeNodes.hasMoreElements()) {
                DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) treeNodes
                        .nextElement();
                NodeInfo userObject = (NodeInfo) currentNode.getUserObject();
                if (userObject.getNode() == node) {
                    return currentNode;
                }
            }
            return null;
        }
        public void selectNode(final Node targetNode) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DefaultMutableTreeNode node = findNode(tree, targetNode);
                    if (node != null) {
                        TreeNode[] path = node.getPath();
                        TreePath tp = new TreePath(path);
                        tree.setSelectionPath(tp);
                        tree.scrollPathToVisible(tp);
                    }
                }
            });
        }
        protected class TreePopUpListener extends MouseAdapter {
            protected JPopupMenu treePopupMenu;
            public TreePopUpListener() {
                treePopupMenu = new JPopupMenu();
                treePopupMenu.add(createTemplatesMenu(resources
                        .getString("ContextMenuItem.insertNewNode")));
                JMenuItem item = new JMenuItem(resources
                        .getString("ContextMenuItem.createNewElement"));
                treePopupMenu.add(item);
                item.addActionListener(new TreeNodeAdder());
                item = new JMenuItem(resources
                        .getString("ContextMenuItem.removeSelection"));
                item.addActionListener(new TreeNodeRemover());
                treePopupMenu.add(item);
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    if (tree.getSelectionPaths() != null) {
                        showPopUp(e);
                    }
                }
            }
            public void mousePressed(MouseEvent e) {
                JTree sourceTree = (JTree) e.getSource();
                TreePath targetPath = sourceTree.getPathForLocation(e.getX(), e
                        .getY());
                if (!e.isControlDown() && !e.isShiftDown()) {
                    sourceTree.setSelectionPath(targetPath);
                } else {
                    sourceTree.addSelectionPath(targetPath);
                }
                if (e.isPopupTrigger() && e.getClickCount() == 1) {
                    showPopUp(e);
                }
            }
            private void showPopUp(MouseEvent e) {
                if (canEdit()) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null && path.getPathCount() > 1) {
                        treePopupMenu.show((Component) e.getSource(), e.getX(),
                                e.getY());
                    }
                }
            }
        }
        protected class TreeNodeAdder implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                NameEditorDialog nameEditorDialog = new NameEditorDialog(
                        DOMViewer.this);
                nameEditorDialog.setLocationRelativeTo(DOMViewer.this);
                int results = nameEditorDialog.showDialog();
                if (results == NameEditorDialog.OK_OPTION) {
                    Element elementToAdd = document.createElementNS(
                            SVGConstants.SVG_NAMESPACE_URI, nameEditorDialog
                                    .getResults());
                    if (rightPanel.getComponentCount() != 0) {
                        rightPanel.remove(0);
                    }
                    rightPanel.add(elementPanel);
                    TreePath[] treePaths = tree.getSelectionPaths();
                    if (treePaths != null) {
                        TreePath treePath = treePaths[treePaths.length - 1];
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath
                                .getLastPathComponent();
                        NodeInfo nodeInfo = (NodeInfo) node.getUserObject();
                        attributePanel.enterAddNewElementMode(elementToAdd,
                                nodeInfo.getNode());
                    }
                }
            }
        }
        protected class NodeTemplateParser implements ActionListener {
            protected String toParse;
            protected short nodeType;
            public NodeTemplateParser(String toParse, short nodeType) {
                this.toParse = toParse;
                this.nodeType = nodeType;
            }
            public void actionPerformed(ActionEvent e) {
                Node nodeToAdd = null;
                switch (nodeType) {
                case Node.ELEMENT_NODE:
                    URL urlObj = null;
                    if (document instanceof SVGOMDocument) {
                        urlObj = ((SVGOMDocument) document).getURLObject();
                    }
                    String uri = (urlObj == null) ? "" : urlObj.toString();
                    Map prefixes = new HashMap();
                    prefixes.put(SVGConstants.XMLNS_PREFIX,
                            SVGConstants.SVG_NAMESPACE_URI);
                    prefixes.put(SVGConstants.XMLNS_PREFIX + ":"
                            + SVGConstants.XLINK_PREFIX,
                            SVGConstants.XLINK_NAMESPACE_URI);
                    SAXDocumentFactory df = new SAXDocumentFactory(document
                            .getImplementation(), XMLResourceDescriptor
                            .getXMLParserClassName());
                    DocumentFragment documentFragment = (DocumentFragment) DOMUtilities
                            .parseXML(toParse, document, uri, prefixes,
                                    SVGConstants.SVG_SVG_TAG, df);
                    nodeToAdd = documentFragment.getFirstChild();
                    break;
                case Node.TEXT_NODE:
                    nodeToAdd = document.createTextNode(toParse);
                    break;
                case Node.COMMENT_NODE:
                    nodeToAdd = document.createComment(toParse);
                    break;
                case Node.CDATA_SECTION_NODE:
                    nodeToAdd = document.createCDATASection(toParse);
                }
                TreePath[] treePaths = tree.getSelectionPaths();
                if (treePaths != null) {
                    TreePath treePath = treePaths[treePaths.length - 1];
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath
                            .getLastPathComponent();
                    NodeInfo nodeInfo = (NodeInfo) node.getUserObject();
                    addChangesToHistory();
                    historyBrowserInterface.appendChild(nodeInfo.getNode(),
                            nodeToAdd);
                }
            }
        }
        protected JMenu createTemplatesMenu(String name) {
            NodeTemplates templates = new NodeTemplates();
            JMenu submenu = new JMenu(name);
            HashMap menuMap = new HashMap();
            ArrayList categoriesList = templates.getCategories();
            int n = categoriesList.size();
            for (int i = 0; i < n; i++) {
                String category = categoriesList.get(i).toString();
                JMenu currentMenu = new JMenu(category);
                submenu.add(currentMenu);
                menuMap.put(category, currentMenu);
            }
            ArrayList values =
                new ArrayList(templates.getNodeTemplatesMap().values());
            Collections.sort(values, new Comparator() {
                public int compare(Object o1, Object o2) {
                    NodeTemplateDescriptor n1 = (NodeTemplateDescriptor) o1;
                    NodeTemplateDescriptor n2 = (NodeTemplateDescriptor) o2;
                    return n1.getName().compareTo(n2.getName());
                }
            });
            Iterator iter = values.iterator();
            while (iter.hasNext()) {
                NodeTemplateDescriptor desc =
                    (NodeTemplateDescriptor) iter .next();
                String toParse = desc.getXmlValue();
                short nodeType = desc.getType();
                String nodeCategory = desc.getCategory();
                JMenuItem currentItem = new JMenuItem(desc.getName());
                currentItem.addActionListener
                    (new NodeTemplateParser(toParse, nodeType));
                JMenu currentSubmenu = (JMenu)menuMap.get(nodeCategory);
                currentSubmenu.add(currentItem);
            }
            return submenu;
        }
        protected class TreeNodeRemover implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                addChangesToHistory();
                AbstractCompoundCommand cmd = historyBrowserInterface
                        .createRemoveSelectedTreeNodesCommand(null);
                TreePath[] treePaths = tree.getSelectionPaths();
                for (int i = 0; treePaths != null && i < treePaths.length; i++) {
                    TreePath treePath = treePaths[i];
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath
                            .getLastPathComponent();
                    NodeInfo nodeInfo = (NodeInfo) node.getUserObject();
                    if (DOMUtilities.isParentOf(nodeInfo.getNode(),
                            nodeInfo.getNode().getParentNode())) {
                        cmd.addCommand(historyBrowserInterface
                                .createRemoveChildCommand(nodeInfo.getNode()
                                        .getParentNode(), nodeInfo.getNode()));
                    }
                }
                historyBrowserInterface.performCompoundUpdateCommand(cmd);
            }
        }
        protected class DOMTreeSelectionListener
                implements TreeSelectionListener {
            public void valueChanged(TreeSelectionEvent ev) {
                if (elementOverlayManager != null) {
                    handleElementSelection(ev);
                }
                DefaultMutableTreeNode mtn;
                mtn = (DefaultMutableTreeNode)
                    tree.getLastSelectedPathComponent();
                if (mtn == null) {
                    return;
                }
                if (rightPanel.getComponentCount() != 0) {
                    rightPanel.remove(0);
                }
                Object nodeInfo = mtn.getUserObject();
                if (nodeInfo instanceof NodeInfo) {
                    Node node = ((NodeInfo) nodeInfo).getNode();
                    switch (node.getNodeType()) {
                    case Node.DOCUMENT_NODE:
                        documentInfo.setText
                            (createDocumentText((Document) node));
                        rightPanel.add(documentInfoPanel);
                        break;
                    case Node.ELEMENT_NODE:
                        propertiesTable.setModel(new NodeCSSValuesModel(node));
                        attributePanel.promptForChanges();
                        attributePanel.setPreviewElement((Element) node);
                        rightPanel.add(elementPanel);
                        break;
                    case Node.COMMENT_NODE:
                    case Node.TEXT_NODE:
                    case Node.CDATA_SECTION_NODE:
                        characterDataPanel.setNode(node);
                        characterDataPanel.getTextArea().setText
                            (node.getNodeValue());
                        rightPanel.add(characterDataPanel);
                    }
                }
                splitPane.revalidate();
                splitPane.repaint();
            }
            protected String createDocumentText(Document doc) {
                StringBuffer sb = new StringBuffer();
                sb.append("Nodes: ");
                sb.append(nodeCount(doc));
                return sb.toString();
            }
            protected int nodeCount(Node node) {
                int result = 1;
                for (Node n = node.getFirstChild();
                     n != null;
                     n = n.getNextSibling()) {
                    result += nodeCount(n);
                }
                return result;
            }
            protected void handleElementSelection(TreeSelectionEvent ev) {
                TreePath[] paths = ev.getPaths();
                for (int i = 0; i < paths.length; i++) {
                    TreePath path = paths[i];
                    DefaultMutableTreeNode mtn =
                        (DefaultMutableTreeNode) path.getLastPathComponent();
                    Object nodeInfo = mtn.getUserObject();
                    if (nodeInfo instanceof NodeInfo) {
                        Node node = ((NodeInfo) nodeInfo).getNode();
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            if (ev.isAddedPath(path)) {
                                elementOverlayManager.addElement
                                    ((Element) node);
                            } else {
                                elementOverlayManager.removeElement
                                    ((Element) node);
                            }
                        }
                    }
                }
                elementOverlayManager.repaint();
            }
        }
        protected class NodeRenderer extends DefaultTreeCellRenderer {
            protected ImageIcon elementIcon;
            protected ImageIcon commentIcon;
            protected ImageIcon piIcon;
            protected ImageIcon textIcon;
            public NodeRenderer() {
                String s;
                s = resources.getString("Element.icon");
                elementIcon = new ImageIcon(getClass().getResource(s));
                s = resources.getString("Comment.icon");
                commentIcon = new ImageIcon(getClass().getResource(s));
                s = resources.getString("PI.icon");
                piIcon = new ImageIcon(getClass().getResource(s));
                s = resources.getString("Text.icon");
                textIcon = new ImageIcon(getClass().getResource(s));
            }
            public Component getTreeCellRendererComponent(JTree tree,
                                                          Object value,
                                                          boolean sel,
                                                          boolean expanded,
                                                          boolean leaf,
                                                          int row,
                                                          boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded,
                                                   leaf, row, hasFocus);
                switch (getNodeType(value)) {
                case Node.ELEMENT_NODE:
                    setIcon(elementIcon);
                    break;
                case Node.COMMENT_NODE:
                    setIcon(commentIcon);
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    setIcon(piIcon);
                    break;
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    setIcon(textIcon);
                    break;
                }
                return this;
            }
            protected short getNodeType(Object value) {
                DefaultMutableTreeNode mtn = (DefaultMutableTreeNode)value;
                Object obj = mtn.getUserObject();
                if (obj instanceof NodeInfo) {
                    Node node = ((NodeInfo)obj).getNode();
                    return node.getNodeType();
                }
                return -1;
            }
        }
        protected class NodeCSSValuesModel extends AbstractTableModel {
            protected Node node;
            protected CSSStyleDeclaration style;
            protected List propertyNames;
            public NodeCSSValuesModel(Node n) {
                node = n;
                if (viewCSS != null) {
                    style = viewCSS.getComputedStyle((Element)n, null);
                    propertyNames = new ArrayList();
                    if (style != null) {
                        for (int i = 0; i < style.getLength(); i++) {
                            propertyNames.add(style.item(i));
                        }
                        Collections.sort(propertyNames);
                    }
                }
            }
            public String getColumnName(int col) {
                if (col == 0) {
                    return resources.getString("CSSValuesTable.column1");
                } else {
                    return resources.getString("CSSValuesTable.column2");
                }
            }
            public int getColumnCount() {
                return 2;
            }
            public int getRowCount() {
                if (style == null) {
                    return 0;
                }
                return style.getLength();
            }
            public boolean isCellEditable(int row, int col) {
                return false;
            }
            public Object getValueAt(int row, int col) {
                String prop = (String)propertyNames.get(row);
                if (col == 0) {
                    return prop;
                } else {
                    return style.getPropertyValue(prop);
                }
            }
        }
    } 
    protected static class NodeInfo {
        protected Node node;
        public NodeInfo(Node n) {
            node = n;
        }
        public Node getNode() {
            return node;
        }
        public String toString() {
            if (node instanceof Element) {
                Element e = (Element) node;
                String id = e.getAttribute(SVGConstants.SVG_ID_ATTRIBUTE);
                if (id.length() != 0) {
                    return node.getNodeName() + " \"" + id + "\"";
                }
            }
            return node.getNodeName();
        }
    }
    protected static class ShadowNodeInfo extends NodeInfo {
        public ShadowNodeInfo(Node n) {
            super(n);
        }
        public String toString() {
            return "shadow tree";
        }
    }
    protected static class ContentNodeInfo extends NodeInfo {
        public ContentNodeInfo(Node n) {
            super(n);
        }
        public String toString() {
            return "selected content";
        }
    }
}
