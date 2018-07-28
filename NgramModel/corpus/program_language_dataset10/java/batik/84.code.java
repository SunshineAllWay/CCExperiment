package org.apache.batik.apps.svgbrowser;
import java.util.ArrayList;
import org.apache.batik.apps.svgbrowser.HistoryBrowser.CommandController;
import org.apache.batik.apps.svgbrowser.HistoryBrowser.HistoryBrowserEvent;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
public class HistoryBrowserInterface {
    private static final String ATTRIBUTE_ADDED_COMMAND = "Attribute added: ";
    private static final String ATTRIBUTE_REMOVED_COMMAND = "Attribute removed: ";
    private static final String ATTRIBUTE_MODIFIED_COMMAND = "Attribute modified: ";
    private static final String NODE_INSERTED_COMMAND = "Node inserted: ";
    private static final String NODE_REMOVED_COMMAND = "Node removed: ";
    private static final String CHAR_DATA_MODIFIED_COMMAND = "Node value changed: ";
    private static final String OUTER_EDIT_COMMAND = "Document changed outside DOM Viewer";
    private static final String COMPOUND_TREE_NODE_DROP = "Node moved";
    private static final String REMOVE_SELECTED_NODES = "Nodes removed";
    protected HistoryBrowser historyBrowser;
    protected AbstractCompoundCommand currentCompoundCommand;
    public HistoryBrowserInterface(CommandController commandController) {
        historyBrowser = new HistoryBrowser(commandController);
    }
    public void setCommmandController(CommandController newCommandController) {
        historyBrowser.setCommandController(newCommandController);
    }
    public CompoundUpdateCommand
            createCompoundUpdateCommand(String commandName) {
        CompoundUpdateCommand cmd = new CompoundUpdateCommand(commandName);
        return cmd;
    }
    public CompoundUpdateCommand createNodeChangedCommand(Node node) {
        return new CompoundUpdateCommand(getNodeChangedCommandName(node));
    }
    public CompoundUpdateCommand createNodesDroppedCommand(ArrayList nodes) {
        return new CompoundUpdateCommand(COMPOUND_TREE_NODE_DROP);
    }
    public CompoundUpdateCommand
            createRemoveSelectedTreeNodesCommand(ArrayList nodes) {
        return new CompoundUpdateCommand(REMOVE_SELECTED_NODES);
    }
    public void performCompoundUpdateCommand(UndoableCommand command) {
        historyBrowser.addCommand(command);
    }
    public static class CompoundUpdateCommand extends AbstractCompoundCommand {
        public CompoundUpdateCommand(String commandName) {
            setName(commandName);
        }
    }
    public HistoryBrowser getHistoryBrowser() {
        return historyBrowser;
    }
    public void nodeInserted(Node newParent, Node newSibling, Node contextNode) {
        historyBrowser.addCommand(createNodeInsertedCommand(newParent,
                newSibling, contextNode));
    }
    public NodeInsertedCommand createNodeInsertedCommand(Node newParent,
                                                         Node newSibling,
                                                         Node contextNode) {
        return new NodeInsertedCommand
            (NODE_INSERTED_COMMAND + getBracketedNodeName(contextNode),
             newParent, newSibling, contextNode);
    }
    public static class NodeInsertedCommand extends AbstractUndoableCommand {
        protected Node newSibling;
        protected Node newParent;
        protected Node contextNode;
        public NodeInsertedCommand(String commandName, Node parent,
                                   Node sibling, Node contextNode) {
            setName(commandName);
            this.newParent = parent;
            this.contextNode = contextNode;
            this.newSibling = sibling;
        }
        public void execute() {
        }
        public void undo() {
            newParent.removeChild(contextNode);
        }
        public void redo() {
            if (newSibling != null) {
                newParent.insertBefore(contextNode, newSibling);
            } else {
                newParent.appendChild(contextNode);
            }
        }
        public boolean shouldExecute() {
            if (newParent == null || contextNode == null) {
                return false;
            }
            return true;
        }
    }
    public void nodeRemoved(Node oldParent, Node oldSibling, Node contextNode) {
        historyBrowser.addCommand
            (createNodeRemovedCommand(oldParent, oldSibling, contextNode));
    }
    public NodeRemovedCommand createNodeRemovedCommand(Node oldParent,
                                                       Node oldSibling,
                                                       Node contextNode) {
        return new NodeRemovedCommand
            (NODE_REMOVED_COMMAND + getBracketedNodeName(contextNode),
             oldParent, oldSibling, contextNode);
    }
    public static class NodeRemovedCommand extends AbstractUndoableCommand {
        protected Node oldSibling;
        protected Node oldParent;
        protected Node contextNode;
        public NodeRemovedCommand(String commandName, Node oldParent,
                                  Node oldSibling, Node contextNode) {
            setName(commandName);
            this.oldParent = oldParent;
            this.contextNode = contextNode;
            this.oldSibling = oldSibling;
        }
        public void execute() {
        }
        public void undo() {
            if (oldSibling != null) {
                oldParent.insertBefore(contextNode, oldSibling);
            } else {
                oldParent.appendChild(contextNode);
            }
        }
        public void redo() {
            oldParent.removeChild(contextNode);
        }
        public boolean shouldExecute() {
            if (oldParent == null || contextNode == null) {
                return false;
            }
            return true;
        }
    }
    public void attributeAdded(Element contextElement, String attributeName,
                               String newAttributeValue, String namespaceURI) {
        historyBrowser.addCommand
            (createAttributeAddedCommand(contextElement, attributeName,
                                         newAttributeValue, namespaceURI));
    }
    public AttributeAddedCommand
            createAttributeAddedCommand(Element contextElement,
                                        String attributeName,
                                        String newAttributeValue,
                                        String namespaceURI) {
        return new AttributeAddedCommand
            (ATTRIBUTE_ADDED_COMMAND + getBracketedNodeName(contextElement),
             contextElement, attributeName, newAttributeValue, namespaceURI);
    }
    public static class AttributeAddedCommand extends AbstractUndoableCommand {
        protected Element contextElement;
        protected String attributeName;
        protected String newValue;
        protected String namespaceURI;
        public AttributeAddedCommand(String commandName,
                                     Element contextElement,
                                     String attributeName,
                                     String newAttributeValue,
                                     String namespaceURI) {
            setName(commandName);
            this.contextElement = contextElement;
            this.attributeName = attributeName;
            this.newValue = newAttributeValue;
            this.namespaceURI = namespaceURI;
        }
        public void execute() {
        }
        public void undo() {
            contextElement.removeAttributeNS(namespaceURI, attributeName);
        }
        public void redo() {
            contextElement.setAttributeNS
                (namespaceURI, attributeName, newValue);
        }
        public boolean shouldExecute() {
            if (contextElement == null || attributeName.length() == 0) {
                return false;
            }
            return true;
        }
    }
    public void attributeRemoved(Element contextElement,
                                 String attributeName,
                                 String prevAttributeValue,
                                 String namespaceURI) {
        historyBrowser.addCommand
            (createAttributeRemovedCommand(contextElement, attributeName,
                                           prevAttributeValue, namespaceURI));
    }
    public AttributeRemovedCommand
            createAttributeRemovedCommand(Element contextElement,
                                          String attributeName,
                                          String prevAttributeValue,
                                          String namespaceURI) {
        return new AttributeRemovedCommand
            (ATTRIBUTE_REMOVED_COMMAND + getBracketedNodeName(contextElement),
             contextElement, attributeName, prevAttributeValue, namespaceURI);
    }
    public static class AttributeRemovedCommand extends AbstractUndoableCommand {
        protected Element contextElement;
        protected String attributeName;
        protected String prevValue;
        protected String namespaceURI;
        public AttributeRemovedCommand(String commandName,
                                       Element contextElement,
                                       String attributeName,
                                       String prevAttributeValue,
                                       String namespaceURI) {
            setName(commandName);
            this.contextElement = contextElement;
            this.attributeName = attributeName;
            this.prevValue = prevAttributeValue;
            this.namespaceURI = namespaceURI;
        }
        public void execute() {
        }
        public void undo() {
            contextElement.setAttributeNS
                (namespaceURI, attributeName, prevValue);
        }
        public void redo() {
            contextElement.removeAttributeNS(namespaceURI, attributeName);
        }
        public boolean shouldExecute() {
            if (contextElement == null || attributeName.length() == 0) {
                return false;
            }
            return true;
        }
    }
    public void attributeModified(Element contextElement,
                                  String attributeName,
                                  String prevAttributeValue,
                                  String newAttributeValue,
                                  String namespaceURI) {
        historyBrowser.addCommand
            (createAttributeModifiedCommand(contextElement, attributeName,
                                            prevAttributeValue,
                                            newAttributeValue, namespaceURI));
    }
    public AttributeModifiedCommand
            createAttributeModifiedCommand(Element contextElement,
                                           String attributeName,
                                           String prevAttributeValue,
                                           String newAttributeValue,
                                           String namespaceURI) {
        return new AttributeModifiedCommand
            (ATTRIBUTE_MODIFIED_COMMAND + getBracketedNodeName(contextElement),
             contextElement, attributeName, prevAttributeValue,
             newAttributeValue, namespaceURI);
    }
    public static class AttributeModifiedCommand extends AbstractUndoableCommand {
        protected Element contextElement;
        protected String attributeName;
        protected String prevAttributeValue;
        protected String newAttributeValue;
        protected String namespaceURI;
        public AttributeModifiedCommand(String commandName,
                                        Element contextElement,
                                        String attributeName,
                                        String prevAttributeValue,
                                        String newAttributeValue,
                                        String namespaceURI) {
            setName(commandName);
            this.contextElement = contextElement;
            this.attributeName = attributeName;
            this.prevAttributeValue = prevAttributeValue;
            this.newAttributeValue = newAttributeValue;
            this.namespaceURI = namespaceURI;
        }
        public void execute() {
        }
        public void undo() {
            contextElement.setAttributeNS
                (namespaceURI, attributeName, prevAttributeValue);
        }
        public void redo() {
            contextElement.setAttributeNS
                (namespaceURI, attributeName, newAttributeValue);
        }
        public boolean shouldExecute() {
            if (contextElement == null || attributeName.length() == 0) {
                return false;
            }
            return true;
        }
    }
    public void charDataModified(Node contextNode, String oldValue,
                                 String newValue) {
        historyBrowser.addCommand
            (createCharDataModifiedCommand(contextNode, oldValue, newValue));
    }
    public CharDataModifiedCommand
            createCharDataModifiedCommand(Node contextNode,
                                          String oldValue,
                                          String newValue) {
        return new CharDataModifiedCommand
            (CHAR_DATA_MODIFIED_COMMAND + getBracketedNodeName(contextNode),
             contextNode, oldValue, newValue);
    }
    public static class CharDataModifiedCommand extends AbstractUndoableCommand {
        protected Node contextNode;
        protected String oldValue;
        protected String newValue;
        public CharDataModifiedCommand(String commandName, Node contextNode,
                String oldValue, String newValue) {
            setName(commandName);
            this.contextNode = contextNode;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        public void execute() {
        }
        public void undo() {
            contextNode.setNodeValue(oldValue);
        }
        public void redo() {
            contextNode.setNodeValue(newValue);
        }
        public boolean shouldExecute() {
            if (contextNode == null) {
                return false;
            }
            return true;
        }
    }
    public void appendChild(Node parent, Node child) {
        historyBrowser.addCommand(createAppendChildCommand(parent, child));
    }
    public AppendChildCommand createAppendChildCommand(Node parent,
                                                       Node child) {
        return new AppendChildCommand
            (getAppendChildCommandName(parent, child), parent, child);
    }
    public static class AppendChildCommand extends AbstractUndoableCommand {
        protected Node oldParentNode;
        protected Node oldNextSibling;
        protected Node parentNode;
        protected Node childNode;
        public AppendChildCommand(String commandName, Node parentNode,
                                  Node childNode) {
            setName(commandName);
            this.oldParentNode = childNode.getParentNode();
            this.oldNextSibling = childNode.getNextSibling();
            this.parentNode = parentNode;
            this.childNode = childNode;
        }
        public void execute() {
            parentNode.appendChild(childNode);
        }
        public void undo() {
            if (oldParentNode != null) {
                oldParentNode.insertBefore(childNode, oldNextSibling);
            } else {
                parentNode.removeChild(childNode);
            }
        }
        public void redo() {
            execute();
        }
        public boolean shouldExecute() {
            if (parentNode == null || childNode == null) {
                return false;
            }
            return true;
        }
    }
    public void insertChildBefore(Node parent, Node sibling, Node child) {
        if (sibling == null) {
            historyBrowser.addCommand(createAppendChildCommand(parent, child));
        } else {
            historyBrowser.addCommand
                (createInsertNodeBeforeCommand(parent, sibling, child));
        }
    }
    public UndoableCommand createInsertChildCommand(Node parent,
                                                    Node sibling,
                                                    Node child) {
        if (sibling == null) {
            return createAppendChildCommand(parent, child);
        } else {
            return createInsertNodeBeforeCommand(parent, sibling, child);
        }
    }
    public InsertNodeBeforeCommand createInsertNodeBeforeCommand(Node parent,
                                                                 Node sibling,
                                                                 Node child) {
        return new InsertNodeBeforeCommand
            (getInsertBeforeCommandName(parent, child, sibling),
             parent, sibling, child);
    }
    public static class InsertNodeBeforeCommand extends AbstractUndoableCommand {
        protected Node oldParent;
        protected Node oldNextSibling;
        protected Node newNextSibling;
        protected Node parent;
        protected Node child;
        public InsertNodeBeforeCommand(String commandName, Node parent,
                                       Node sibling, Node child) {
            setName(commandName);
            this.oldParent = child.getParentNode();
            this.oldNextSibling = child.getNextSibling();
            this.parent = parent;
            this.child = child;
            this.newNextSibling = sibling;
        }
        public void execute() {
            if (newNextSibling != null) {
                parent.insertBefore(child, newNextSibling);
            } else {
                parent.appendChild(child);
            }
        }
        public void undo() {
            if (oldParent != null) {
                oldParent.insertBefore(child, oldNextSibling);
            } else {
                parent.removeChild(child);
            }
        }
        public void redo() {
            execute();
        }
        public boolean shouldExecute() {
            if (parent == null || child == null) {
                return false;
            }
            return true;
        }
    }
    public void replaceChild(Node parent, Node newChild, Node oldChild) {
    }
    public static class ReplaceChildCommand extends AbstractUndoableCommand {
        protected Node oldParent;
        protected Node oldNextSibling;
        protected Node newNextSibling;
        protected Node parent;
        protected Node child;
        public ReplaceChildCommand(String commandName, Node parent,
                                   Node sibling, Node child) {
            setName(commandName);
            this.oldParent = child.getParentNode();
            this.oldNextSibling = child.getNextSibling();
            this.parent = parent;
            this.child = child;
            this.newNextSibling = sibling;
        }
        public void execute() {
            if (newNextSibling != null) {
                parent.insertBefore(child, newNextSibling);
            } else {
                parent.appendChild(child);
            }
        }
        public void undo() {
            if (oldParent != null) {
                oldParent.insertBefore(child, oldNextSibling);
            } else {
                parent.removeChild(child);
            }
        }
        public void redo() {
            execute();
        }
        public boolean shouldExecute() {
            if (parent == null || child == null) {
                return false;
            }
            return true;
        }
    }
    public void removeChild(Node parent, Node child) {
        historyBrowser.addCommand(createRemoveChildCommand(parent, child));
    }
    public RemoveChildCommand createRemoveChildCommand(Node parent,
                                                       Node child) {
        return new RemoveChildCommand
            (getRemoveChildCommandName(parent, child), parent, child);
    }
    public static class RemoveChildCommand extends AbstractUndoableCommand {
        protected Node parentNode;
        protected Node childNode;
        protected int indexInChildrenArray;
        public RemoveChildCommand(String commandName, Node parentNode,
                                  Node childNode) {
            setName(commandName);
            this.parentNode = parentNode;
            this.childNode = childNode;
        }
        public void execute() {
            indexInChildrenArray =
                DOMUtilities.getChildIndex(childNode, parentNode);
            parentNode.removeChild(childNode);
        }
        public void undo() {
            Node refChild =
                parentNode.getChildNodes().item(indexInChildrenArray);
            parentNode.insertBefore(childNode, refChild);
        }
        public void redo() {
            parentNode.removeChild(childNode);
        }
        public boolean shouldExecute() {
            if (parentNode == null || childNode == null) {
                return false;
            }
            return true;
        }
    }
    public void setNodeValue(Node contextNode, String newValue) {
        historyBrowser.addCommand
            (createChangeNodeValueCommand(contextNode, newValue));
    }
    public ChangeNodeValueCommand
            createChangeNodeValueCommand(Node contextNode, String newValue) {
        return new ChangeNodeValueCommand
            (getChangeNodeValueCommandName(contextNode, newValue),
             contextNode, newValue);
    }
    public static class ChangeNodeValueCommand extends AbstractUndoableCommand {
        protected Node contextNode;
        protected String newValue;
        public ChangeNodeValueCommand(String commandName, Node contextNode,
                                      String newValue) {
            setName(commandName);
            this.contextNode = contextNode;
            this.newValue = newValue;
        }
        public void execute() {
            String oldNodeValue = contextNode.getNodeValue();
            contextNode.setNodeValue(newValue);
            newValue = oldNodeValue;
        }
        public void undo() {
            execute();
        }
        public void redo() {
            execute();
        }
        public boolean shouldExecute() {
            if (contextNode == null) {
                return false;
            }
            return true;
        }
    }
    public AbstractCompoundCommand getCurrentCompoundCommand() {
        if (currentCompoundCommand == null) {
            currentCompoundCommand =
                createCompoundUpdateCommand(OUTER_EDIT_COMMAND);
        }
        return currentCompoundCommand;
    }
    public void addToCurrentCompoundCommand(AbstractUndoableCommand cmd) {
        getCurrentCompoundCommand().addCommand(cmd);
        historyBrowser.fireDoCompoundEdit
            (new HistoryBrowserEvent(getCurrentCompoundCommand()));
    }
    public void performCurrentCompoundCommand() {
        if (getCurrentCompoundCommand().getCommandNumber() > 0) {
            historyBrowser.addCommand(getCurrentCompoundCommand());
            historyBrowser.fireCompoundEditPerformed
                (new HistoryBrowserEvent(currentCompoundCommand));
            currentCompoundCommand = null;
        }
    }
    private String getNodeAsString(Node node) {
        String id = "";
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) node;
            id = e.getAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE);
        }
        if (id.length() != 0) {
            return node.getNodeName() + " \"" + id + "\"";
        }
        return node.getNodeName();
    }
    private String getBracketedNodeName(Node node) {
        return "(" + getNodeAsString(node) + ")";
    }
    private String getAppendChildCommandName(Node parentNode, Node childNode) {
        return "Append " + getNodeAsString(childNode) + " to "
                + getNodeAsString(parentNode);
    }
    private String getInsertBeforeCommandName(Node parentNode, Node childNode,
                                              Node siblingNode) {
        return "Insert " + getNodeAsString(childNode) + " to "
                + getNodeAsString(parentNode) + " before "
                + getNodeAsString(siblingNode);
    }
    private String getRemoveChildCommandName(Node parent, Node child) {
        return "Remove " + getNodeAsString(child) + " from "
                + getNodeAsString(parent);
    }
    private String getChangeNodeValueCommandName(Node contextNode,
                                                 String newValue) {
        return "Change " + getNodeAsString(contextNode) + " value to "
                + newValue;
    }
    private String getNodeChangedCommandName(Node node) {
        return "Node " + getNodeAsString(node) + " changed";
    }
}
