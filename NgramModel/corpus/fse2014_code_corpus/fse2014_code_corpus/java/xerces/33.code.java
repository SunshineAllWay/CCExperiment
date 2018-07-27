package ui;                    
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
public class TreeView extends JFrame implements ActionListener, TextListener {
    private static final long serialVersionUID = 3688504394090098738L;
    static final boolean DEBUG = true;
    static final String 
        DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.DOMParser";
    static int WARNING = 0;
    static int ERROR=1;
    static int FATAL_ERROR=2;
    static final String title = "TreeViewer";
    static final String openString = "Open";
    static final String quitString = "Quit";
    static final String reloadString = "Reload current XML file";
    static final String expandString = "Expand Tree";
    static final String collapseString = "Collapse Tree";
    ErrorStorer ef;
    String fname;
    DOMTree m_tree;
    JTextArea sourceText, messageText;
    Vector textLine;
    FileNameInput fni;
    DOMParserSaveEncoding parser;
    Image openFolder;
    Image closedFolder;
    Image leafImage;
    public TreeView() {
        this(null);
    }
    public TreeView(String uri) {
        super(uri);
        openFolder = DefaultImages.createOpenFolderImage();
        closedFolder = DefaultImages.createClosedFolderImage();
        leafImage = DefaultImages.createLeafImage();
        parser = new DOMParserSaveEncoding();
        ef = new ErrorStorer();
        fname = uri;
        JMenuBar jmb = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem item;
        item = new JMenuItem(openString);
        fileMenu.add(item);
        item.addActionListener(this);
        item = new JMenuItem(quitString);
        fileMenu.add(item);
        item.addActionListener(this);
        JMenu shortcutMenu = new JMenu("Shortcuts");
        item = new JMenuItem(expandString);
        shortcutMenu.add(item);
        item.addActionListener(this);
        item = new JMenuItem(collapseString);
        shortcutMenu.add(item);
        item.addActionListener(this);
        item = new JMenuItem(reloadString);
        shortcutMenu.add(item);
        item.addActionListener(this);
        jmb.add(fileMenu);
        jmb.add(shortcutMenu);
        setJMenuBar(jmb);
        getContentPane().add(createUI(fname));
    }
    JComponent createUI(String filename) {
        if (DEBUG) System.out.println("START createUI:"+filename);
        messageText = new JTextArea(3,40);
        messageText.setFont(new Font("dialog", Font.PLAIN, 12));
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(new JScrollPane(messageText) {
            private static final long serialVersionUID = 3978426918603075632L;
            public Dimension getPreferredSize(){
                Dimension size = TreeView.this.getSize();
                return new Dimension(size.width, size.height / 4);
                }
            public Dimension getMinimumSize(){
                return new Dimension(100, 100);
                }
            },
            BorderLayout.CENTER);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Messages"),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
        sourceText = new JTextArea();
        sourceText.setFont(new Font("monospaced", Font.PLAIN, 12));
        sourceText.setBackground(Color.white);
        sourceText.setForeground(Color.black);
        sourceText.setSelectedTextColor(Color.black);
        sourceText.setSelectionColor(Color.red);
        sourceText.setEditable(false);
        JPanel sourcePanel = new JPanel(new BorderLayout());
        sourcePanel.add(new JScrollPane(sourceText){
            private static final long serialVersionUID = 4121135831458068789L;
            public Dimension getPreferredSize(){
                Dimension size = TreeView.this.getSize();
                return new Dimension(size.width / 2, size.height * 3 / 5);
                }
            public Dimension getMinimumSize(){
                return new Dimension(100, 100);
                }
            },
            BorderLayout.CENTER);
        sourcePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Source View"),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
        JPanel treePanel = new JPanel(new BorderLayout());
        m_tree = new DOMTree();
        m_tree.setCellRenderer(new XMLTreeCellRenderer());
        m_tree.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        m_tree.addTreeSelectionListener(
            new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    TreeNode node = (TreeNode)
                        (e.getPath().getLastPathComponent());
                    nodeSelected(node);
                }
            }
        );
        m_tree.setRowHeight(18);
        m_tree.setFont(new Font("dialog", Font.PLAIN, 12));
        treePanel.add(new JScrollPane(m_tree) {
            private static final long serialVersionUID = 3977860665971126320L;
            public Dimension getPreferredSize(){
                Dimension size = TreeView.this.getSize();
                return new Dimension(size.width / 2, size.height * 3 / 5);
                }
            public Dimension getMinimumSize(){
                return new Dimension(100, 100);
                }
            },
            BorderLayout.CENTER);
        treePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Tree View"),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)
            ));
        refreshUI(filename);
        JComponent split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            true, treePanel, sourcePanel);
        JComponent mainSplitPane =
            new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                           true, split, messagePanel);
        if (DEBUG) System.out.println("END createUI:"+filename);
        return mainSplitPane;
    }
    void refreshUI(String filename) {
        if (DEBUG) System.out.println("START refreshUI:"+filename);
        messageText.selectAll();
        messageText.cut();
        if (filename == null || filename.length() == 0) {
            messageText.setForeground(Color.red);
            messageText.append("No input XML filename specified:"+filename+"\n");
            return;
        }
        fname = filename;
        Document newRoot = getRoot(filename);
        if (newRoot == null) {
            messageText.setForeground(Color.red);
            messageText.append("Unable to get new DOM Tree for:"+filename+"\n");
            return;
        }
        m_tree.setDocument(newRoot);
        sourceText.selectAll();
        sourceText.cut();
        readXMLFile(fname, sourceText);
        setTitle(title+": "+filename);
        if (m_tree!= null)
            expandTree();
        if (ef != null && ef.getErrorNodes()!=null       
                    && ef.getErrorNodes().size() > 0 ) {
            messageText.setForeground(Color.red);
            messageText.append("XML source, "+fname+" has errors.\n");
            messageText.append("Please click on red Tree View items for details.\n");
            Hashtable errors = ef.getErrorNodes();
            Iterator entries = errors.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                Node node = (Node) entry.getKey();
                ParseError parseError = (ParseError) entry.getValue();
                messageText.append("node="+node.getNodeName()
                +", error="+parseError.getMsg()+"\n");
            }
        }
        if (DEBUG) System.out.println("END refreshUI:"+filename);
    }
    public Document getRoot(String filename) {
        if (DEBUG) System.out.println("START getRoot:"+filename);
        if (filename == null || filename.length() == 0)
        return null;
        try {
            ef.resetErrors();
            parser.setErrorHandler(ef);
            parser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false); 
            parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
            parser.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
            parser.parse(filename);
            Document document = parser.getDocument();
            return document;
        } catch (Exception e) {
             System.err.println( "Error: Invalid XML document could not get ROOT" );
             System.exit( 1 );
        }
        return null;
    }
    synchronized void readXMLFile(String filename, JTextArea ta) {
        if (DEBUG) System.out.println("START readXMLFile"+filename);
        if (filename == null || filename.length() == 0)
            return;
        InputStream fis = null;
        BufferedReader dis = null;
        try {
            java.net.URL file = createURL(filename);
            fis = file.openStream();
            String javaEncoding = parser.getJavaEncoding(); 
            try
            {
            dis = new BufferedReader(new InputStreamReader(fis, javaEncoding ));
            }
            catch( UnsupportedEncodingException ex )
            {
            dis = new BufferedReader(new InputStreamReader(fis ));
            }
        } catch (Exception ex) {
            System.err.println("ERROR: Xerces.readXMLFile: "+ex);
            return;
        }
        String line;
        int len = 0;
        textLine = new Vector();
        String nl = "\n";
        int nllen = nl.length();
        StringBuffer sb = new StringBuffer();
        try{
            readline: while ((line = dis.readLine()) != null) {
                sb.append(line+nl);
                textLine.addElement(new Integer(len));
                len += line.length()+nllen;
            }
            ta.append(sb.toString());
        } catch (IOException io) {
            System.err.println(io);
            return;
        }
        if (DEBUG) System.out.println("END readXMLFile"+filename);
        return;
    }
    void nodeSelected(TreeNode treeNode) {
        Node node = m_tree.getNode(treeNode);
        if( node == null )     
            return;
        StringBuffer sb = new StringBuffer();
        messageText.selectAll();
        messageText.cut();
       Object errorObject = ef == null ? null : ef.getError(node);
       if (errorObject != null) {
           messageText.setForeground(Color.red);
           ParseError eip = (ParseError)errorObject;
           sb.append("Error: "+eip.getMsg()+"\n");
           int lineNo = eip.getLineNo();
           int pos  = 0;
           int next = 0;                       
           int sizeOfTextLine = textLine.size();
           if( lineNo < sizeOfTextLine )
              {
              pos = ((Integer)textLine.elementAt(lineNo-1)).intValue();
              next = (lineNo == sizeOfTextLine ) ?
               pos :
               (((Integer)textLine.elementAt(lineNo)).intValue());
              }
           else
              {
              pos  = (( Integer) textLine.elementAt( sizeOfTextLine - 1 )).intValue();
              next = pos + 2;
              }
           sourceText.select(pos, next );
       } else {
           messageText.setForeground(Color.black);
           sourceText.select(0, 0 );
       }
        if (node.getNodeType() == Node.ELEMENT_NODE
                    || node.getNodeType() == Node.TEXT_NODE 
                || node.getNodeType() == Node.CDATA_SECTION_NODE )
                 {
                    sb.append(node.toString());
            }
        messageText.append(sb.toString());
    }
    public void textValueChanged(TextEvent e) {
        try {
            if (fni != null)
                fni.setVisible(false);
            fname = ((JTextField)e.getSource()).getText();
            if (DEBUG) System.out.println("textValueChanged:"+fname);
            refreshUI(fname);
        } catch (Exception ex) {
           System.err.println( "Error: while trying to refresh gui" );
           System.exit( 1 );
        }
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (DEBUG) System.err.println("ACTION: "+e.getActionCommand()+", "+e.paramString());
        if (e.getActionCommand().equals(quitString)) {
          System.exit(0);
        }
        else if (e.getActionCommand().equals(openString)) {
            fni = new FileNameInput("Open File");
            fni.addTextListener(this);
            fni.setVisible(true);
        }
        else if (e.getActionCommand().equals(expandString)) {
            expandTree();
        }
        else if (e.getActionCommand().equals(collapseString)) {
            int rows = m_tree.getRowCount();
            for (int i = 0; i < rows; i++) {
                m_tree.collapseRow(i);
            }
        }
        else
            refreshUI(fname);
    }
    void expandTree() {
        int rows = 0;
        for (int levels=0; levels <= 4; levels++) {
            rows=m_tree.getRowCount();
            for (int i = 0; i < rows; i++) {
                m_tree.expandRow(i);
            }
        }
    }
    class XMLTreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 3761130444229720113L;
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                          boolean selected, boolean expanded,
                          boolean leaf, int row,
                              boolean hasFocus)
        {
            Node node = ((DOMTree)tree).getNode(value);
            Component comp = super.getTreeCellRendererComponent(tree, value,
                           selected,  expanded, leaf,  row, hasFocus);
            if (selected) {
                comp.setBackground(Color.blue);
            }
            if (ef != null
            && ef.getErrorNodes() != null
            && value != null
            && node != null
            && ef.getErrorNodes().containsKey( node )) {
                comp.setForeground(Color.red);
            }
            if (node != null) {
                if (leaf) {
                    setIcon(new ImageIcon(leafImage));
                } else if (expanded) {
                    setIcon(new ImageIcon(openFolder));
                } else {
                    setIcon(new ImageIcon(closedFolder));
                }
            }
            if (node != null && node instanceof Element) {
                Element txNode = (Element)node;
                Attr txAtt = (Attr)txNode.getAttributeNode("gender");
                if (txAtt != null) {
                    if (txAtt.getValue().equals("male")) {
                        setIcon(new ImageIcon("male.gif"));
                    } else
                    if (txAtt.getValue().equals("female")) {
                        setIcon(new ImageIcon("female.gif"));
                    }
                }
            }
            return comp;
        }
    }
    class FileNameInput extends JFrame implements ActionListener {
        private static final long serialVersionUID = 3257562893292615472L;
        JLabel fileLabel;
        JTextField textField;
        JButton ok;
        JButton cancel;
        Vector textListeners;
        public FileNameInput() {
            this("");
        }
        public FileNameInput(String title) {
            super(title);
            fileLabel = new JLabel("Enter XML file name:");
            textField = new JTextField();
            textField.addActionListener(this);
            ok = new JButton("ok");
            cancel = new JButton("cancel");
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(ok);
            buttonPanel.add(cancel);
            ok.addActionListener(this);
            cancel.addActionListener(this);
            getContentPane().add(fileLabel, BorderLayout.NORTH);
            getContentPane().add(textField, BorderLayout.CENTER);
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            setSize(400,100);
        }
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == ok || e.getSource() == textField) {
                System.out.println("FileNameInput: pressed OK");
                    TextEvent event = new TextEvent(textField, TextEvent.TEXT_VALUE_CHANGED);
                    deliverEvent(event);
                    setVisible(false);
            } else
            if (e.getSource() == cancel) {
                System.out.println("FileNameInput: pressed cancel");
                    setVisible(false);
            }
        }
        public void addTextListener(TextListener listener) {
            if (listener == null)
                return;
            if (textListeners == null)
               textListeners = new Vector();
            textListeners.addElement(listener);
            }
        public void removeTextListener(TextListener listener) {
            if (listener == null || textListeners == null)
                return;
            textListeners.removeElement(listener);
            }
        protected void deliverEvent(EventObject evt) {
            if (evt instanceof TextEvent) {
                TextEvent event = (TextEvent)evt;
                Vector l;
                synchronized (textListeners) { l = (Vector)textListeners.clone(); }
                int size = l.size();
                for (int i = 0; i < size; i++)
                    ((TextListener)l.elementAt(i)).textValueChanged(event);
                }
            }
    }
    static URL createURL(String name) throws Exception {
        try {
                URL u = new URL(name);
                return u;
        } catch (MalformedURLException ex) {
        }
        URL u = new URL("file:" + new File(name).getAbsolutePath());
        return u;
    }    
    class ErrorStorer 
        implements ErrorHandler
    {
        Hashtable errorNodes = null;
        public ErrorStorer() {
        }
        public Hashtable getErrorNodes() {
            return errorNodes;
        }
        public Object getError(Node node) {
            if (errorNodes == null)
                return null;
            return errorNodes.get(node);
        }
        public void resetErrors() {
            if (errorNodes != null)
            errorNodes.clear();
        }
        public void warning(SAXParseException ex) {
            handleError(ex, WARNING);
        }
        public void error(SAXParseException ex) {
            handleError(ex, ERROR);
        }
        public void fatalError(SAXParseException ex) throws SAXException {
            handleError(ex, FATAL_ERROR);
        }
        private void handleError(SAXParseException ex, int type) {
            System.out.println("!!! handleError: "+ex.getMessage());
            StringBuffer errorString = new StringBuffer();
            errorString.append("at line number, ");
            errorString.append(ex.getLineNumber());
            errorString.append(": ");
            errorString.append(ex.getMessage());
            Node current = null ;
            try
              {
              current = ( Node ) parser.getProperty( "http://apache.org/xml/properties/dom/current-element-node" );
              }
            catch( SAXException exception  )
              {
               ;
              }
            if (current == null) {
                System.err.println("Error in handleError. getCurrentNode()==null!");
                return;
            }
            if (errorNodes == null)
                errorNodes = new Hashtable();
            ParseError previous = (ParseError) errorNodes.get(current);
            ParseError eip  = null;
            if (previous != null) {
                eip = previous;
                errorString = new StringBuffer(previous.getMsg()+"\n"+errorString.toString());
                eip.setMsg(errorString.toString());
            } else {
                eip = new
                    ParseError(
                        ex.getSystemId(), 
                        ex.getLineNumber(), 
                        ex.getColumnNumber(),
                        "",  
                        errorString.toString());
            }
            errorNodes.put(current, eip);
        }
    }
    class ParseError extends Object {
        String fileName;
        int lineNo;
        int charOffset;
        Object key;
        String msg;
        public ParseError(String fileName, int lineNo, int charOffset,
                           Object key, 
                           String msg)
        {
            this. fileName=fileName;
            this. lineNo=lineNo;
            this. charOffset=charOffset;
            this. key=key;
            this. msg=msg;
        }
        public String getFileName() { return fileName; }
        public int getLineNo() { return lineNo; }
        public int getCharOffset() { return charOffset;}
        public Object getKey() { return key; }
        public String getMsg() { return msg; }
        public void setMsg(String s) { msg = s; }
    }
    public static void main(String argv[]) {
        int parserNameIndex = -1;
        String parserName = DEFAULT_PARSER_NAME;
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            if (arg.startsWith("-")) {
                if (arg.equals("-p")) {
                    if (i == argv.length - 1) {
                        System.err.println("error: missing parser class");
                        System.exit(1);
                    }
                    parserName = argv[++i];
                    parserNameIndex = i;
                    continue;
                }
                if (arg.equals("-h")) {
                    printUsage();
                    System.exit(1);
                }
            }
            System.err.println(arg+':');
            JFrame frame = null;
            if (parserNameIndex == argv.length-1) {
                frame = new TreeView("");
            } else {
                frame = new TreeView(arg);
            }
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent e) {
                 System.exit(0);
             }
            });
            frame.setSize(790, 590);
            frame.setVisible(true);
        }
    } 
    private static void printUsage() {
        System.err.println("usage: java ui.TreeViewer (options) uri ...");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -p name  Specify DOM parser class by name.");
        System.err.println("           Default parser: "+DEFAULT_PARSER_NAME);
        System.err.println("  -h       This help screen.");
    } 
} 
