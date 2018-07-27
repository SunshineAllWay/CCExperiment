package org.apache.batik.util.gui.xmleditor;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JEditorPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Element;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
public class XMLTextEditor extends JEditorPane {
    protected UndoManager undoManager;
    public XMLTextEditor() {
        super();
        XMLEditorKit kit = new XMLEditorKit();
        setEditorKitForContentType(XMLEditorKit.XML_MIME_TYPE, kit);
        setContentType(XMLEditorKit.XML_MIME_TYPE);
        setBackground(Color.white);
        undoManager = new UndoManager();
        UndoableEditListener undoableEditHandler = new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                undoManager.addEdit(e.getEdit());
            }
        };
        getDocument().addUndoableEditListener(undoableEditHandler);
    }
    public void setText(String t) {
        super.setText(t);
        undoManager.discardAllEdits();
    }
    public void undo() {
        try {
            undoManager.undo();
        } catch (CannotUndoException ex) { }
    }
    public void redo() {
        try {
            undoManager.redo();
        } catch (CannotRedoException ex) { }
    }
    public void gotoLine(int line) {
        Element element =
            getDocument().getDefaultRootElement().getElement(line);
        if (element == null) { return; }
        int pos = element.getStartOffset();
        setCaretPosition(pos);
    }
}
