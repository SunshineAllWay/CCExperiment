package org.apache.batik.apps.svgbrowser;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Document;
import org.apache.batik.bridge.DefaultExternalResourceSecurity;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.EmbededExternalResourceSecurity;
import org.apache.batik.bridge.EmbededScriptSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.NoLoadExternalResourceSecurity;
import org.apache.batik.bridge.NoLoadScriptSecurity;
import org.apache.batik.bridge.RelaxedExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedScriptSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.dom.StyleSheetProcessingInstruction;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.dom.util.HashTable;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.ext.swing.JAffineTransformChooser;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.gvt.Overlay;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderListener;
import org.apache.batik.swing.svg.LinkActivationEvent;
import org.apache.batik.swing.svg.LinkActivationListener;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.apache.batik.swing.svg.SVGFileFilter;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherEvent;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherListener;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.Platform;
import org.apache.batik.util.Service;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;
import org.apache.batik.util.gui.JErrorPane;
import org.apache.batik.util.gui.LocationBar;
import org.apache.batik.util.gui.MemoryMonitor;
import org.apache.batik.util.gui.URIChooser;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.JComponentModifier;
import org.apache.batik.util.gui.resource.MenuFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.gui.resource.ToolBarFactory;
import org.apache.batik.util.gui.xmleditor.XMLDocument;
import org.apache.batik.util.gui.xmleditor.XMLTextEditor;
import org.apache.batik.util.resources.ResourceManager;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.svg.SVGDocument;
public class JSVGViewerFrame
    extends    JFrame
    implements ActionMap,
               SVGDocumentLoaderListener,
               GVTTreeBuilderListener,
               SVGLoadEventDispatcherListener,
               GVTTreeRendererListener,
               LinkActivationListener,
               UpdateManagerListener {
    private static String EOL;
    static {
        try {
            EOL = System.getProperty("line.separator", "\n");
        } catch (SecurityException e) {
            EOL = "\n";
        }
    }
    protected static boolean priorJDK1_4 = true;
    protected static final String JDK_1_4_PRESENCE_TEST_CLASS
        = "java.util.logging.LoggingPermission";
    static {
        try {
            Class.forName(JDK_1_4_PRESENCE_TEST_CLASS);
            priorJDK1_4 = false;
        } catch (ClassNotFoundException e) {
        }
    }
    public static final String RESOURCES =
        "org.apache.batik.apps.svgbrowser.resources.GUI";
    public static final String ABOUT_ACTION = "AboutAction";
    public static final String OPEN_ACTION = "OpenAction";
    public static final String OPEN_LOCATION_ACTION = "OpenLocationAction";
    public static final String NEW_WINDOW_ACTION = "NewWindowAction";
    public static final String RELOAD_ACTION = "ReloadAction";
    public static final String SAVE_AS_ACTION = "SaveAsAction";
    public static final String BACK_ACTION = "BackAction";
    public static final String FORWARD_ACTION = "ForwardAction";
    public static final String FULL_SCREEN_ACTION = "FullScreenAction";
    public static final String PRINT_ACTION = "PrintAction";
    public static final String EXPORT_AS_JPG_ACTION = "ExportAsJPGAction";
    public static final String EXPORT_AS_PNG_ACTION = "ExportAsPNGAction";
    public static final String EXPORT_AS_TIFF_ACTION = "ExportAsTIFFAction";
    public static final String PREFERENCES_ACTION = "PreferencesAction";
    public static final String CLOSE_ACTION = "CloseAction";
    public static final String VIEW_SOURCE_ACTION = "ViewSourceAction";
    public static final String EXIT_ACTION = "ExitAction";
    public static final String RESET_TRANSFORM_ACTION = "ResetTransformAction";
    public static final String ZOOM_IN_ACTION = "ZoomInAction";
    public static final String ZOOM_OUT_ACTION = "ZoomOutAction";
    public static final String PREVIOUS_TRANSFORM_ACTION = "PreviousTransformAction";
    public static final String NEXT_TRANSFORM_ACTION = "NextTransformAction";
    public static final String USE_STYLESHEET_ACTION = "UseStylesheetAction";
    public static final String PLAY_ACTION = "PlayAction";
    public static final String PAUSE_ACTION = "PauseAction";
    public static final String STOP_ACTION = "StopAction";
    public static final String MONITOR_ACTION = "MonitorAction";
    public static final String DOM_VIEWER_ACTION = "DOMViewerAction";
    public static final String SET_TRANSFORM_ACTION = "SetTransformAction";
    public static final String FIND_DIALOG_ACTION = "FindDialogAction";
    public static final String THUMBNAIL_DIALOG_ACTION = "ThumbnailDialogAction";
    public static final String FLUSH_ACTION = "FlushAction";
    public static final String TOGGLE_DEBUGGER_ACTION = "ToggleDebuggerAction";
    public static final Cursor WAIT_CURSOR =
        new Cursor(Cursor.WAIT_CURSOR);
    public static final Cursor DEFAULT_CURSOR =
        new Cursor(Cursor.DEFAULT_CURSOR);
    public static final String PROPERTY_OS_NAME
        = Resources.getString("JSVGViewerFrame.property.os.name");
    public static final String PROPERTY_OS_NAME_DEFAULT
        = Resources.getString("JSVGViewerFrame.property.os.name.default");
    public static final String PROPERTY_OS_WINDOWS_PREFIX
        = Resources.getString("JSVGViewerFrame.property.os.windows.prefix");
    protected static final String OPEN_TITLE = "Open.title";
    protected static Vector handlers;
    protected static SquiggleInputHandler defaultHandler = new SVGInputHandler();
    protected static ResourceBundle bundle;
    protected static ResourceManager resources;
    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }
    protected Application application;
    protected Canvas svgCanvas;
    protected class Canvas extends JSVGCanvas {
        public Canvas(SVGUserAgent ua, boolean eventsEnabled,
                      boolean selectableText) {
            super(ua, eventsEnabled, selectableText);
        }
        public Object getRhinoInterpreter() {
            if (bridgeContext == null) {
                return null;
            }
            return bridgeContext.getInterpreter("text/ecmascript");
        }
        protected class JSVGViewerDOMViewerController
                implements DOMViewerController {
            public boolean canEdit() {
                return getUpdateManager() != null;
            }
            public ElementOverlayManager createSelectionManager() {
                if (canEdit()) {
                    return new ElementOverlayManager(Canvas.this);
                }
                return null;
            }
            public org.w3c.dom.Document getDocument() {
                return Canvas.this.svgDocument;
            }
            public void performUpdate(Runnable r) {
                if (canEdit()) {
                    getUpdateManager().getUpdateRunnableQueue().invokeLater(r);
                } else {
                    r.run();
                }
            }
            public void removeSelectionOverlay(Overlay selectionOverlay) {
                getOverlays().remove(selectionOverlay);
            }
            public void selectNode(Node node) {
                DOMViewerAction dViewerAction =
                    (DOMViewerAction) getAction(DOM_VIEWER_ACTION);
                dViewerAction.openDOMViewer();
                domViewer.selectNode(node);
            }
        }
    }
    protected JPanel svgCanvasPanel;
    protected JWindow window;
    protected static JFrame memoryMonitorFrame;
    protected File currentPath = new File("");
    protected File currentSavePath = new File("");
    protected BackAction backAction = new BackAction();
    protected ForwardAction forwardAction = new ForwardAction();
    protected PlayAction playAction = new PlayAction();
    protected PauseAction pauseAction = new PauseAction();
    protected StopAction stopAction = new StopAction();
    protected PreviousTransformAction previousTransformAction =
        new PreviousTransformAction();
    protected NextTransformAction nextTransformAction =
        new NextTransformAction();
    protected UseStylesheetAction useStylesheetAction =
        new UseStylesheetAction();
    protected boolean debug;
    protected boolean autoAdjust = true;
    protected boolean managerStopped;
    protected SVGUserAgent userAgent = new UserAgent();
    protected SVGDocument svgDocument;
    protected URIChooser uriChooser;
    protected DOMViewer domViewer;
    protected FindDialog findDialog;
    protected ThumbnailDialog thumbnailDialog;
    protected JAffineTransformChooser.Dialog transformDialog;
    protected LocationBar locationBar;
    protected StatusBar statusBar;
    protected String title;
    protected LocalHistory localHistory;
    protected TransformHistory transformHistory = new TransformHistory();
    protected String alternateStyleSheet;
    protected Debugger debugger;
    public JSVGViewerFrame(Application app) {
        application = app;
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                application.closeJSVGViewerFrame(JSVGViewerFrame.this);
            }
        });
        svgCanvas = new Canvas(userAgent, true, true) {
                Dimension screenSize;
                {
                    screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    setMaximumSize(screenSize);
                }
                public Dimension getPreferredSize(){
                    Dimension s = super.getPreferredSize();
                    if (s.width > screenSize.width) s.width =screenSize.width;
                    if (s.height > screenSize.height) s.height = screenSize.height;
                    return s;
                }
                public void setMySize(Dimension d) {
                    setPreferredSize(d);
                    invalidate();
                    if (autoAdjust) {
                        setExtendedState(getExtendedState() & ~MAXIMIZED_BOTH);
                        pack();
                    }
                }
                public void setDisableInteractions(boolean b) {
                    super.setDisableInteractions(b);
                    ((Action)listeners.get(SET_TRANSFORM_ACTION)) .setEnabled(!b);
                    if (thumbnailDialog != null)
                        thumbnailDialog.setInteractionEnabled(!b);
                }
            };
        javax.swing.ActionMap map = svgCanvas.getActionMap();
        map.put(FULL_SCREEN_ACTION, new FullScreenAction());
        javax.swing.InputMap imap = svgCanvas.getInputMap(JComponent.WHEN_FOCUSED);
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
        imap.put(key, FULL_SCREEN_ACTION);
        svgCanvas.setDoubleBufferedRendering(true);
        listeners.put(ABOUT_ACTION, new AboutAction());
        listeners.put(OPEN_ACTION, new OpenAction());
        listeners.put(OPEN_LOCATION_ACTION, new OpenLocationAction());
        listeners.put(NEW_WINDOW_ACTION, new NewWindowAction());
        listeners.put(RELOAD_ACTION, new ReloadAction());
        listeners.put(SAVE_AS_ACTION, new SaveAsAction());
        listeners.put(BACK_ACTION, backAction);
        listeners.put(FORWARD_ACTION, forwardAction);
        listeners.put(PRINT_ACTION, new PrintAction());
        listeners.put(EXPORT_AS_JPG_ACTION, new ExportAsJPGAction());
        listeners.put(EXPORT_AS_PNG_ACTION, new ExportAsPNGAction());
        listeners.put(EXPORT_AS_TIFF_ACTION, new ExportAsTIFFAction());
        listeners.put(PREFERENCES_ACTION, new PreferencesAction());
        listeners.put(CLOSE_ACTION, new CloseAction());
        listeners.put(EXIT_ACTION, application.createExitAction(this));
        listeners.put(VIEW_SOURCE_ACTION, new ViewSourceAction());
        javax.swing.ActionMap cMap = svgCanvas.getActionMap();
        listeners.put(RESET_TRANSFORM_ACTION,
                      cMap.get(JSVGCanvas.RESET_TRANSFORM_ACTION));
        listeners.put(ZOOM_IN_ACTION,
                      cMap.get(JSVGCanvas.ZOOM_IN_ACTION));
        listeners.put(ZOOM_OUT_ACTION,
                      cMap.get(JSVGCanvas.ZOOM_OUT_ACTION));
        listeners.put(PREVIOUS_TRANSFORM_ACTION, previousTransformAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_K, KeyEvent.CTRL_MASK);
        imap.put(key, previousTransformAction);
        listeners.put(NEXT_TRANSFORM_ACTION, nextTransformAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_MASK);
        imap.put(key, nextTransformAction);
        listeners.put(USE_STYLESHEET_ACTION, useStylesheetAction);
        listeners.put(PLAY_ACTION, playAction);
        listeners.put(PAUSE_ACTION, pauseAction);
        listeners.put(STOP_ACTION, stopAction);
        listeners.put(MONITOR_ACTION, new MonitorAction());
        listeners.put(DOM_VIEWER_ACTION, new DOMViewerAction());
        listeners.put(SET_TRANSFORM_ACTION, new SetTransformAction());
        listeners.put(FIND_DIALOG_ACTION, new FindDialogAction());
        listeners.put(THUMBNAIL_DIALOG_ACTION, new ThumbnailDialogAction());
        listeners.put(FLUSH_ACTION, new FlushAction());
        listeners.put(TOGGLE_DEBUGGER_ACTION, new ToggleDebuggerAction());
        JPanel p = null;
        try {
            MenuFactory mf = new MenuFactory(bundle, this);
            JMenuBar mb =
                mf.createJMenuBar("MenuBar", application.getUISpecialization());
            setJMenuBar(mb);
            localHistory = new LocalHistory(mb, this);
            String[] uri = application.getVisitedURIs();
            for (int i=0; i<uri.length; i++) {
                if (uri[i] != null && !"".equals(uri[i])) {
                    localHistory.update(uri[i]);
                }
            }
            p = new JPanel(new BorderLayout());
            ToolBarFactory tbf = new ToolBarFactory(bundle, this);
            JToolBar tb = tbf.createJToolBar("ToolBar");
            tb.setFloatable(false);
            getContentPane().add(p, BorderLayout.NORTH);
            p.add(tb, BorderLayout.NORTH);
            p.add(new javax.swing.JSeparator(), BorderLayout.CENTER);
            p.add(locationBar = new LocationBar(), BorderLayout.SOUTH);
            locationBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        } catch (MissingResourceException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        svgCanvasPanel = new JPanel(new BorderLayout());
        svgCanvasPanel.setBorder(BorderFactory.createEtchedBorder());
        svgCanvasPanel.add(svgCanvas, BorderLayout.CENTER);
        p = new JPanel(new BorderLayout());
        p.add(svgCanvasPanel, BorderLayout.CENTER);
        p.add(statusBar = new StatusBar(), BorderLayout.SOUTH);
        getContentPane().add(p, BorderLayout.CENTER);
        svgCanvas.addSVGDocumentLoaderListener(this);
        svgCanvas.addGVTTreeBuilderListener(this);
        svgCanvas.addSVGLoadEventDispatcherListener(this);
        svgCanvas.addGVTTreeRendererListener(this);
        svgCanvas.addLinkActivationListener(this);
        svgCanvas.addUpdateManagerListener(this);
        svgCanvas.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseMoved(MouseEvent e) {
                    if (svgDocument == null) {
                        statusBar.setXPosition(e.getX());
                        statusBar.setYPosition(e.getY());
                    } else {
                        try {
                            AffineTransform at;
                            at = svgCanvas.getViewBoxTransform();
                            if (at != null) {
                                at = at.createInverse();
                                Point2D p2d =
                                    at.transform(new Point2D.Float(e.getX(), e.getY()),
                                                 null);
                                statusBar.setXPosition((float)p2d.getX());
                                statusBar.setYPosition((float)p2d.getY());
                                return;
                            }
                        } catch (NoninvertibleTransformException ex) {
                        }
                        statusBar.setXPosition(e.getX());
                        statusBar.setYPosition(e.getY());
                    }
                }
            });
        svgCanvas.addMouseListener(new MouseAdapter() {
                public void mouseExited(MouseEvent e) {
                    Dimension dim = svgCanvas.getSize();
                    if (svgDocument == null) {
                        statusBar.setWidth(dim.width);
                        statusBar.setHeight(dim.height);
                    } else {
                        try {
                            AffineTransform at;
                            at = svgCanvas.getViewBoxTransform();
                            if (at != null) {
                                at = at.createInverse();
                                Point2D o =
                                    at.transform(new Point2D.Float(0, 0),
                                                 null);
                                Point2D p2d =
                                    at.transform(new Point2D.Float(dim.width,
                                                                   dim.height),
                                                 null);
                                statusBar.setWidth((float)(p2d.getX() - o.getX()));
                                statusBar.setHeight((float)(p2d.getY() - o.getY()));
                                return;
                            }
                        } catch (NoninvertibleTransformException ex) {
                        }
                        statusBar.setWidth(dim.width);
                        statusBar.setHeight(dim.height);
                    }
                }
            });
        svgCanvas.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    Dimension dim = svgCanvas.getSize();
                    if (svgDocument == null) {
                        statusBar.setWidth(dim.width);
                        statusBar.setHeight(dim.height);
                    } else {
                        try {
                            AffineTransform at;
                            at = svgCanvas.getViewBoxTransform();
                            if (at != null) {
                                at = at.createInverse();
                                Point2D o =
                                    at.transform(new Point2D.Float(0, 0),
                                                 null);
                                Point2D p2d =
                                    at.transform(new Point2D.Float(dim.width,
                                                                   dim.height),
                                                 null);
                                statusBar.setWidth((float)(p2d.getX() - o.getX()));
                                statusBar.setHeight((float)(p2d.getY() - o.getY()));
                                return;
                            }
                        } catch (NoninvertibleTransformException ex) {
                        }
                        statusBar.setWidth(dim.width);
                        statusBar.setHeight(dim.height);
                    }
                }
            });
        locationBar.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String st = locationBar.getText().trim();
                int i = st.indexOf( '#' );
                String t = "";
                if (i != -1) {
                    t = st.substring(i + 1);
                    st = st.substring(0, i);
                }
                if (st.equals(""))
                    return;
                try{
                    File f = new File(st);
                    if (f.exists()) {
                        if (f.isDirectory()) {
                            return;
                        } else {
                            try {
                                st = f.getCanonicalPath();
                                if (st.startsWith("/")) {
                                    st = "file:" + st;
                                } else {
                                    st = "file:/" + st;
                                }
                            } catch (IOException ex) {
                            }
                        }
                    }
                }catch(SecurityException se){
                }
                String fi = svgCanvas.getFragmentIdentifier();
                if (svgDocument != null) {
                    ParsedURL docPURL
                        = new ParsedURL(svgDocument.getURL());
                    ParsedURL purl = new ParsedURL(docPURL, st);
                    fi = (fi == null) ? "" : fi;
                    if (docPURL.equals(purl) && t.equals(fi)) {
                        return;
                    }
                }
                if (t.length() != 0) {
                    st += '#' + t;
                }
                locationBar.setText(st);
                locationBar.addToHistory(st);
                showSVGDocument(st);
            }
        });
    }
    public void dispose() {
        hideDebugger();
        svgCanvas.dispose();
        super.dispose();
    }
    public void setDebug(boolean b) {
        debug = b;
    }
    public void setAutoAdjust(boolean b) {
        autoAdjust = b;
    }
    public JSVGCanvas getJSVGCanvas() {
        return svgCanvas;
    }
    private static File makeAbsolute(File f){
        if(!f.isAbsolute()){
            return f.getAbsoluteFile();
        }
        return f;
    }
    public void showDebugger() {
        if (debugger == null && Debugger.isPresent) {
            debugger = new Debugger(this, locationBar.getText());
            debugger.initialize();
        }
    }
    public void hideDebugger() {
        if (debugger != null) {
            debugger.clearAllBreakpoints();
            debugger.go();
            debugger.dispose();
            debugger = null;
        }
    }
    protected static class Debugger {
        protected static boolean isPresent;
        protected static Class debuggerClass;
        protected static Class contextFactoryClass;
        protected static final int CLEAR_ALL_BREAKPOINTS_METHOD = 0;
        protected static final int GO_METHOD                    = 1;
        protected static final int SET_EXIT_ACTION_METHOD       = 2;
        protected static final int ATTACH_TO_METHOD             = 3;
        protected static final int DETACH_METHOD                = 4;
        protected static final int DISPOSE_METHOD               = 5;
        protected static final int GET_DEBUG_FRAME_METHOD       = 6;
        protected static Constructor debuggerConstructor;
        protected static Method[] debuggerMethods;
        protected static Class rhinoInterpreterClass;
        protected static Method getContextFactoryMethod;
        static {
            try {
                Class dc =
                    Class.forName("org.mozilla.javascript.tools.debugger.Main");
                Class cfc =
                    Class.forName("org.mozilla.javascript.ContextFactory");
                rhinoInterpreterClass = Class.forName
                    ("org.apache.batik.script.rhino.RhinoInterpreter");
                debuggerConstructor =
                    dc.getConstructor(new Class[] { String.class });
                debuggerMethods = new Method[] {
                    dc.getMethod("clearAllBreakpoints", (Class[]) null),
                    dc.getMethod("go", (Class[]) null),
                    dc.getMethod("setExitAction", new Class[] {Runnable.class}),
                    dc.getMethod("attachTo", new Class[] { cfc }),
                    dc.getMethod("detach", (Class[]) null),
                    dc.getMethod("dispose", (Class[]) null),
                    dc.getMethod("getDebugFrame", (Class[]) null)
                };
                getContextFactoryMethod =
                    rhinoInterpreterClass.getMethod("getContextFactory",
                                                    (Class[]) null);
                debuggerClass = dc;
                isPresent = true;
            } catch (ClassNotFoundException cnfe) {
            } catch (NoSuchMethodException nsme) {
            } catch (SecurityException se) {
            }
        }
        protected Object debuggerInstance;
        protected JSVGViewerFrame svgFrame;
        public Debugger(JSVGViewerFrame frame, String url) {
            svgFrame = frame;
            try {
                debuggerInstance = debuggerConstructor.newInstance
                    (new Object[] { "JavaScript Debugger - " + url });
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae.getMessage());
            } catch (InvocationTargetException ite) {
                ite.printStackTrace();
                throw new RuntimeException(ite.getMessage());
            } catch (InstantiationException ie) {
                throw new RuntimeException(ie.getMessage());
            }
        }
        public void setDocumentURL(String url) {
            getDebugFrame().setTitle("JavaScript Debugger - " + url);
        }
        public void initialize() {
            JFrame   debugGui = getDebugFrame();
            JMenuBar menuBar  = debugGui.getJMenuBar();
            JMenu    menu     = menuBar.getMenu(0);
            menu.getItem(0).setEnabled(false); 
            menu.getItem(1).setEnabled(false); 
            menu.getItem(3).setText
                (Resources.getString("Close.text")); 
            menu.getItem(3).setAccelerator
                (KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK));
            debugGui.setSize(600, 460);
            debugGui.pack();
            setExitAction(new Runnable() {
                    public void run() {
                        svgFrame.hideDebugger();
                    }});
            WindowAdapter wa = new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        svgFrame.hideDebugger();
                    }};
            debugGui.addWindowListener(wa);
            debugGui.setVisible(true);
            attach();
        }
        public void attach() {
            Object interpreter = svgFrame.svgCanvas.getRhinoInterpreter();
            if (interpreter != null) {
                attachTo(getContextFactory(interpreter));
            }
        }
        protected JFrame getDebugFrame() {
            try {
                return (JFrame) debuggerMethods[GET_DEBUG_FRAME_METHOD].invoke
                    (debuggerInstance, (Object[]) null);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite.getMessage());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae.getMessage());
            }
        }
        protected void setExitAction(Runnable r) {
            try {
                debuggerMethods[SET_EXIT_ACTION_METHOD].invoke
                    (debuggerInstance, new Object[] { r });
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite.getMessage());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae.getMessage());
            }
        }
        public void attachTo(Object contextFactory) {
            try {
                debuggerMethods[ATTACH_TO_METHOD].invoke
                    (debuggerInstance, new Object[] { contextFactory });
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite.getMessage());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae.getMessage());
            }
        }
        public void detach() {
            try {
                debuggerMethods[DETACH_METHOD].invoke(debuggerInstance,
                                                      (Object[]) null);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite.getMessage());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae.getMessage());
            }
        }
        public void go() {
            try {
                debuggerMethods[GO_METHOD].invoke(debuggerInstance,
                                                  (Object[]) null);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite.getMessage());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae.getMessage());
            }
        }
        public void clearAllBreakpoints() {
            try {
                debuggerMethods[CLEAR_ALL_BREAKPOINTS_METHOD].invoke
                    (debuggerInstance, (Object[]) null);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite.getMessage());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae.getMessage());
            }
        }
        public void dispose() {
            try {
                debuggerMethods[DISPOSE_METHOD].invoke(debuggerInstance,
                                                       (Object[]) null);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite.getMessage());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae.getMessage());
            }
        }
        protected Object getContextFactory(Object rhinoInterpreter) {
            try {
                return getContextFactoryMethod.invoke(rhinoInterpreter,
                                                      (Object[]) null);
            } catch (InvocationTargetException ite) {
                throw new RuntimeException(ite.getMessage());
            } catch (IllegalAccessException iae) {
                throw new RuntimeException(iae.getMessage());
            }
        }
    }
    public class AboutAction extends AbstractAction {
        public AboutAction(){
        }
        public void actionPerformed(ActionEvent e){
            AboutDialog dlg = new AboutDialog(JSVGViewerFrame.this);
            dlg.setSize(dlg.getPreferredSize());
            dlg.setLocationRelativeTo(JSVGViewerFrame.this);
            dlg.setVisible(true);
            dlg.toFront();
        }
    }
    public class OpenAction extends AbstractAction {
        public OpenAction() {
        }
        public void actionPerformed(ActionEvent e) {
            File f = null;
            if (Platform.isOSX) {
                FileDialog fileDialog =
                    new FileDialog(JSVGViewerFrame.this,
                                   Resources.getString(OPEN_TITLE));
                fileDialog.setFilenameFilter(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        Iterator iter = getHandlers().iterator();
                        while (iter.hasNext()) {
                            SquiggleInputHandler handler
                                = (SquiggleInputHandler)iter.next();
                            if (handler.accept(new File(dir, name))) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
                fileDialog.setVisible(true);
                String filename = fileDialog.getFile();
                if (fileDialog != null) {
                    String dirname = fileDialog.getDirectory();
                    f = new File(dirname, filename);
                }
            } else {
                JFileChooser fileChooser = null;
                String os = System.getProperty(PROPERTY_OS_NAME, PROPERTY_OS_NAME_DEFAULT);
                SecurityManager sm = System.getSecurityManager();
                if ( priorJDK1_4 && sm != null && os.indexOf(PROPERTY_OS_WINDOWS_PREFIX) != -1 ){
                    fileChooser = new JFileChooser(makeAbsolute(currentPath),
                                                   new WindowsAltFileSystemView());
                } else {
                    fileChooser = new JFileChooser(makeAbsolute(currentPath));
                }
                fileChooser.setFileHidingEnabled(false);
                fileChooser.setFileSelectionMode
                    (JFileChooser.FILES_ONLY);
                Iterator iter = getHandlers().iterator();
                while (iter.hasNext()) {
                    SquiggleInputHandler handler
                        = (SquiggleInputHandler)iter.next();
                    fileChooser.addChoosableFileFilter
                        (new SquiggleInputHandlerFilter(handler));
                }
                int choice = fileChooser.showOpenDialog(JSVGViewerFrame.this);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    f = fileChooser.getSelectedFile();
                    currentPath = f;
                }
            }
            if (f != null) {
                try {
                    String furl = f.toURL().toString();
                    showSVGDocument(furl);
                } catch (MalformedURLException ex) {
                    if (userAgent != null) {
                        userAgent.displayError(ex);
                    }
                }
            }
        }
    }
    public void showSVGDocument(String uri){
        try {
            ParsedURL purl = new ParsedURL(uri);
            SquiggleInputHandler
                handler = getInputHandler(purl);
            handler.handle(purl,
                           JSVGViewerFrame.this);
        } catch (Exception e) {
            if (userAgent != null) {
                userAgent.displayError(e);
            }
        }
    }
    public SquiggleInputHandler getInputHandler(ParsedURL purl) throws IOException {
        Iterator iter = getHandlers().iterator();
        SquiggleInputHandler handler = null;
        while (iter.hasNext()) {
            SquiggleInputHandler curHandler =
                (SquiggleInputHandler)iter.next();
            if (curHandler.accept(purl)) {
                handler = curHandler;
                break;
            }
        }
        if (handler == null) {
            handler = defaultHandler;
        }
        return handler;
    }
    protected static Vector getHandlers() {
        if (handlers != null) {
            return handlers;
        }
        handlers = new Vector();
        registerHandler(new SVGInputHandler());
        Iterator iter = Service.providers(SquiggleInputHandler.class);
        while (iter.hasNext()) {
            SquiggleInputHandler handler
                = (SquiggleInputHandler)iter.next();
            registerHandler(handler);
        }
        return handlers;
    }
    public static synchronized
        void registerHandler(SquiggleInputHandler handler) {
        Vector handlers = getHandlers();
        handlers.addElement(handler);
    }
    public class OpenLocationAction extends AbstractAction {
        public OpenLocationAction() {}
        public void actionPerformed(ActionEvent e) {
            if (uriChooser == null) {
                uriChooser = new URIChooser(JSVGViewerFrame.this);
                uriChooser.setFileFilter(new SVGFileFilter());
                uriChooser.pack();
                Rectangle fr = getBounds();
                Dimension sd = uriChooser.getSize();
                uriChooser.setLocation(fr.x + (fr.width  - sd.width) / 2,
                                       fr.y + (fr.height - sd.height) / 2);
            }
            if (uriChooser.showDialog() == URIChooser.OK_OPTION) {
                String s = uriChooser.getText();
                if (s == null) return;
                int i = s.indexOf( '#' );
                String t = "";
                if (i != -1) {
                    t = s.substring(i + 1);
                    s = s.substring(0, i);
                }
                if (!s.equals("")) {
                    File f = new File(s);
                    if (f.exists()) {
                        if (f.isDirectory()) {
                            s = null;
                        } else {
                            try {
                                s = f.getCanonicalPath();
                                if (s.startsWith("/")) {
                                    s = "file:" + s;
                                } else {
                                    s = "file:/" + s;
                                }
                            } catch (IOException ex) {
                            }
                        }
                    }
                    if (s != null) {
                        if (svgDocument != null) {
                            ParsedURL docPURL
                                = new ParsedURL(svgDocument.getURL());
                            ParsedURL purl = new ParsedURL(docPURL, s);
                            String fi = svgCanvas.getFragmentIdentifier();
                            if (docPURL.equals(purl) && t.equals(fi)) {
                                return;
                            }
                        }
                        if (t.length() != 0) {
                            s += '#' + t;
                        }
                        showSVGDocument(s);
                    }
                }
            }
        }
    }
    public class NewWindowAction extends AbstractAction {
        public NewWindowAction() {}
        public void actionPerformed(ActionEvent e) {
            JSVGViewerFrame vf = application.createAndShowJSVGViewerFrame();
            vf.autoAdjust = autoAdjust;
            vf.debug = debug;
            vf.svgCanvas.setProgressivePaint(svgCanvas.getProgressivePaint());
            vf.svgCanvas.setDoubleBufferedRendering
                (svgCanvas.getDoubleBufferedRendering());
        }
    }
    public class PreferencesAction extends AbstractAction {
        public PreferencesAction() {}
        public void actionPerformed(ActionEvent e) {
            application.showPreferenceDialog(JSVGViewerFrame.this);
        }
    }
    public class CloseAction extends AbstractAction {
        public CloseAction() {}
        public void actionPerformed(ActionEvent e) {
            application.closeJSVGViewerFrame(JSVGViewerFrame.this);
        }
    }
    public class ReloadAction extends AbstractAction {
        public ReloadAction() {}
        public void actionPerformed(ActionEvent e) {
            if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == 1) {
                svgCanvas.flushImageCache();
            }
            if (svgDocument != null) {
                localHistory.reload();
            }
        }
    }
    public class BackAction extends    AbstractAction
                            implements JComponentModifier {
        List components = new LinkedList();
        public BackAction() {}
        public void actionPerformed(ActionEvent e) {
            if (localHistory.canGoBack()) {
                localHistory.back();
            }
        }
        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }
        protected void update() {
            boolean b = localHistory.canGoBack();
            Iterator it = components.iterator();
            while (it.hasNext()) {
                ((JComponent)it.next()).setEnabled(b);
            }
        }
    }
    public class ForwardAction extends    AbstractAction
                               implements JComponentModifier {
        List components = new LinkedList();
        public ForwardAction() {}
        public void actionPerformed(ActionEvent e) {
            if (localHistory.canGoForward()) {
                localHistory.forward();
            }
        }
        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }
        protected void update() {
            boolean b = localHistory.canGoForward();
            Iterator it = components.iterator();
            while (it.hasNext()) {
                ((JComponent)it.next()).setEnabled(b);
            }
        }
    }
    public class PrintAction extends AbstractAction {
        public PrintAction() {}
        public void actionPerformed(ActionEvent e) {
            if (svgDocument != null) {
                final SVGDocument doc = svgDocument;
                new Thread() {
                    public void run(){
                        String uri = doc.getURL();
                        String fragment = svgCanvas.getFragmentIdentifier();
                        if (fragment != null) {
                            uri += '#' +fragment;
                        }
                        PrintTranscoder pt = new PrintTranscoder();
                        if (application.getXMLParserClassName() != null) {
                            pt.addTranscodingHint
                                (JPEGTranscoder.KEY_XML_PARSER_CLASSNAME,
                                    application.getXMLParserClassName());
                        }
                        pt.addTranscodingHint(PrintTranscoder.KEY_SHOW_PAGE_DIALOG,
                                              Boolean.TRUE);
                        pt.addTranscodingHint(PrintTranscoder.KEY_SHOW_PRINTER_DIALOG,
                                              Boolean.TRUE);
                        pt.transcode(new TranscoderInput(uri), null);
                        try {
                            pt.print();
                        } catch (PrinterException ex) {
                            userAgent.displayError(ex);
                        }
                    }
                }.start();
            }
        }
    }
    public class SaveAsAction extends AbstractAction {
        public SaveAsAction() {}
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser;
            fileChooser = new JFileChooser(makeAbsolute(currentSavePath));
            fileChooser.setDialogTitle(resources.getString("SaveAs.title"));
            fileChooser.setFileHidingEnabled(false);
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.addChoosableFileFilter(new ImageFileFilter(".svg"));
            int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
            if (choice != JFileChooser.APPROVE_OPTION)
                return;
            final File f = fileChooser.getSelectedFile();
            SVGOptionPanel sop;
            sop = SVGOptionPanel.showDialog(JSVGViewerFrame.this);
            final boolean useXMLBase  = sop.getUseXMLBase();
            final boolean prettyPrint = sop.getPrettyPrint();
            sop = null;
            final SVGDocument svgDoc = svgCanvas.getSVGDocument();
            if (svgDoc == null) return;
            statusBar.setMessage(resources.getString("Message.saveAs"));
            currentSavePath = f;
            OutputStreamWriter w = null;
            try {
                OutputStream tos = null;
                tos = new FileOutputStream(f);
                tos = new BufferedOutputStream(tos);
                w = new OutputStreamWriter(tos, "utf-8");
            } catch (Exception ex) {
                userAgent.displayError(ex);
                return;
            }
            final OutputStreamWriter writer  = w;
            final Runnable doneRun = new Runnable() {
                    public void run() {
                        String doneStr = resources.getString("Message.done");
                        statusBar.setMessage(doneStr);
                    }
                };
            Runnable r = new Runnable() {
                    public void run() {
                        try {
                            writer.write
                                ("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                                writer.write (EOL);
                            Node fc = svgDoc.getFirstChild();
                            if (fc.getNodeType() != Node.DOCUMENT_TYPE_NODE) {
                                writer.write ("<!DOCTYPE svg PUBLIC '");
                                writer.write (SVGConstants.SVG_PUBLIC_ID);
                                writer.write ("' '");
                                writer.write (SVGConstants.SVG_SYSTEM_ID);
                                writer.write ("'>");
                                writer.write (EOL);
                                writer.write (EOL);
                            }
                            Element root = svgDoc.getRootElement();
                            boolean doXMLBase = useXMLBase;
                            if (root.hasAttributeNS
                                (XMLConstants.XML_NAMESPACE_URI, "base"))
                                doXMLBase = false;
                            if (doXMLBase) {
                                root.setAttributeNS
                                    (XMLConstants.XML_NAMESPACE_URI,
                                     "xml:base",
                                     svgDoc.getURL());
                            }
                            if (prettyPrint) {
                                SVGTranscoder trans = new SVGTranscoder();
                                trans.transcode(new TranscoderInput(svgDoc),
                                                new TranscoderOutput(writer));
                            } else {
                                DOMUtilities.writeDocument(svgDoc, writer);
                            }
                            writer.close();
                            if (doXMLBase)
                                root.removeAttributeNS
                                    (XMLConstants.XML_NAMESPACE_URI,
                                     "xml:base");
                            if (EventQueue.isDispatchThread()) {
                                doneRun.run();
                            } else {
                                EventQueue.invokeLater(doneRun);
                            }
                        } catch (Exception ex) {
                            userAgent.displayError(ex);
                        }
                    }
                };
            UpdateManager um = svgCanvas.getUpdateManager();
            if ((um != null) && (um.isRunning())) {
                um.getUpdateRunnableQueue().invokeLater(r);
            } else {
                r.run();
            }
        }
    }
    public class ExportAsJPGAction extends AbstractAction {
        public ExportAsJPGAction() {}
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser =
                new JFileChooser(makeAbsolute(currentSavePath));
            fileChooser.setDialogTitle(resources.getString("ExportAsJPG.title"));
            fileChooser.setFileHidingEnabled(false);
            fileChooser.setFileSelectionMode
                (JFileChooser.FILES_ONLY);
            fileChooser.addChoosableFileFilter(new ImageFileFilter(".jpg"));
            int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                float quality =
                    JPEGOptionPanel.showDialog(JSVGViewerFrame.this);
                final File f = fileChooser.getSelectedFile();
                BufferedImage buffer = svgCanvas.getOffScreen();
                if (buffer != null) {
                    statusBar.setMessage
                        (resources.getString("Message.exportAsJPG"));
                    int w = buffer.getWidth();
                    int h = buffer.getHeight();
                    final ImageTranscoder trans = new JPEGTranscoder();
                    if (application.getXMLParserClassName() != null) {
                        trans.addTranscodingHint
                            (JPEGTranscoder.KEY_XML_PARSER_CLASSNAME,
                                application.getXMLParserClassName());
                    }
                    trans.addTranscodingHint
                        (JPEGTranscoder.KEY_QUALITY, new Float(quality));
                    final BufferedImage img = trans.createImage(w, h);
                    Graphics2D g2d = img.createGraphics();
                    g2d.setColor(Color.white);
                    g2d.fillRect(0, 0, w, h);
                    g2d.drawImage(buffer, null, 0, 0);
                    new Thread() {
                        public void run() {
                            try {
                                currentSavePath = f;
                                OutputStream ostream =
                                    new BufferedOutputStream(new FileOutputStream(f));
                                trans.writeImage(img, new TranscoderOutput(ostream));
                                ostream.close();
                            } catch (Exception ex) { }
                            statusBar.setMessage
                                (resources.getString("Message.done"));
                        }
                    }.start();
                }
            }
        }
    }
    public class ExportAsPNGAction extends AbstractAction {
        public ExportAsPNGAction() {}
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser =
                new JFileChooser(makeAbsolute(currentSavePath));
            fileChooser.setDialogTitle(resources.getString("ExportAsPNG.title"));
            fileChooser.setFileHidingEnabled(false);
            fileChooser.setFileSelectionMode
                (JFileChooser.FILES_ONLY);
            fileChooser.addChoosableFileFilter(new ImageFileFilter(".png"));
            int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                boolean isIndexed = PNGOptionPanel.showDialog(JSVGViewerFrame.this);
                final File f = fileChooser.getSelectedFile();
                BufferedImage buffer = svgCanvas.getOffScreen();
                if (buffer != null) {
                    statusBar.setMessage
                        (resources.getString("Message.exportAsPNG"));
                    int w = buffer.getWidth();
                    int h = buffer.getHeight();
                    final ImageTranscoder trans = new PNGTranscoder();
                    if (application.getXMLParserClassName() != null) {
                        trans.addTranscodingHint
                            (JPEGTranscoder.KEY_XML_PARSER_CLASSNAME,
                                application.getXMLParserClassName());
                    }
                    trans.addTranscodingHint(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE,
                                             Boolean.TRUE );
                    if(isIndexed){
                        trans.addTranscodingHint(PNGTranscoder.KEY_INDEXED, new Integer(8));
                    }
                    final BufferedImage img = trans.createImage(w, h);
                    Graphics2D g2d = img.createGraphics();
                    g2d.drawImage(buffer, null, 0, 0);
                    new Thread() {
                        public void run() {
                            try {
                                currentSavePath = f;
                                OutputStream ostream =
                                    new BufferedOutputStream(new FileOutputStream(f));
                                trans.writeImage(img,
                                                 new TranscoderOutput(ostream));
                                ostream.close();
                            } catch (Exception ex) {}
                            statusBar.setMessage
                                (resources.getString("Message.done"));
                        }
                    }.start();
                }
            }
        }
    }
    public class ExportAsTIFFAction extends AbstractAction {
        public ExportAsTIFFAction() {}
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser =
                new JFileChooser(makeAbsolute(currentSavePath));
            fileChooser.setDialogTitle(resources.getString("ExportAsTIFF.title"));
            fileChooser.setFileHidingEnabled(false);
            fileChooser.setFileSelectionMode
                (JFileChooser.FILES_ONLY);
            fileChooser.addChoosableFileFilter(new ImageFileFilter(".tiff"));
            int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
            if (choice == JFileChooser.APPROVE_OPTION) {
                final File f = fileChooser.getSelectedFile();
                BufferedImage buffer = svgCanvas.getOffScreen();
                if (buffer != null) {
                    statusBar.setMessage
                        (resources.getString("Message.exportAsTIFF"));
                    int w = buffer.getWidth();
                    int h = buffer.getHeight();
                    final ImageTranscoder trans = new TIFFTranscoder();
                    if (application.getXMLParserClassName() != null) {
                        trans.addTranscodingHint
                            (JPEGTranscoder.KEY_XML_PARSER_CLASSNAME,
                                application.getXMLParserClassName());
                    }
                    final BufferedImage img = trans.createImage(w, h);
                    Graphics2D g2d = img.createGraphics();
                    g2d.drawImage(buffer, null, 0, 0);
                    new Thread() {
                        public void run() {
                            try {
                                currentSavePath = f;
                                OutputStream ostream = new BufferedOutputStream
                                    (new FileOutputStream(f));
                                trans.writeImage
                                    (img, new TranscoderOutput(ostream));
                                ostream.close();
                            } catch (Exception ex) {}
                            statusBar.setMessage
                                (resources.getString("Message.done"));
                        }
                    }.start();
                }
            }
        }
    }
    public class ViewSourceAction extends AbstractAction {
        public ViewSourceAction() {}
        public void actionPerformed(ActionEvent e) {
            if (svgDocument == null) {
                return;
            }
            final ParsedURL u = new ParsedURL(svgDocument.getURL());
            final JFrame fr = new JFrame(u.toString());
            fr.setSize(resources.getInteger("ViewSource.width"),
                       resources.getInteger("ViewSource.height"));
            final XMLTextEditor ta  = new XMLTextEditor();
            ta.setFont(new Font("monospaced", Font.PLAIN, 12));
            JScrollPane scroll = new JScrollPane();
            scroll.getViewport().add(ta);
            scroll.setVerticalScrollBarPolicy
                (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            fr.getContentPane().add(scroll, BorderLayout.CENTER);
            new Thread() {
                public void run() {
                    char [] buffer = new char[4096];
                    try {
                        Document  doc = new XMLDocument();
                        ParsedURL purl = new ParsedURL(svgDocument.getURL());
                        InputStream is
                            = u.openStream(getInputHandler(purl).
                                           getHandledMimeTypes());
                        Reader in = XMLUtilities.createXMLDocumentReader(is);
                        int len;
                        while ((len=in.read(buffer, 0, buffer.length)) != -1) {
                            doc.insertString(doc.getLength(),
                                             new String(buffer, 0, len), null);
                        }
                        ta.setDocument(doc);
                        ta.setEditable(false);
                        fr.setVisible(true);
                    } catch (Exception ex) {
                        userAgent.displayError(ex);
                    }
                }
            }.start();
        }
    }
    public class FlushAction extends AbstractAction {
        public FlushAction() {}
        public void actionPerformed(ActionEvent e) {
            svgCanvas.flush();
            svgCanvas.setRenderingTransform(svgCanvas.getRenderingTransform());
        }
    }
    public class ToggleDebuggerAction extends AbstractAction {
        public ToggleDebuggerAction() {
            super("Toggle Debugger Action");
        }
        public void actionPerformed(ActionEvent e) {
            if (debugger == null) {
                showDebugger();
            } else {
                hideDebugger();
            }
        }
    }
    public class PreviousTransformAction extends    AbstractAction
                                         implements JComponentModifier {
        List components = new LinkedList();
        public PreviousTransformAction() {}
        public void actionPerformed(ActionEvent e) {
            if (transformHistory.canGoBack()) {
                transformHistory.back();
                update();
                nextTransformAction.update();
                svgCanvas.setRenderingTransform(transformHistory.currentTransform());
            }
        }
        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }
        protected void update() {
            boolean b = transformHistory.canGoBack();
            Iterator it = components.iterator();
            while (it.hasNext()) {
                ((JComponent)it.next()).setEnabled(b);
            }
        }
    }
    public class NextTransformAction extends    AbstractAction
                                         implements JComponentModifier {
        List components = new LinkedList();
        public NextTransformAction() {}
        public void actionPerformed(ActionEvent e) {
            if (transformHistory.canGoForward()) {
                transformHistory.forward();
                update();
                previousTransformAction.update();
                svgCanvas.setRenderingTransform(transformHistory.currentTransform());
            }
        }
        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }
        protected void update() {
            boolean b = transformHistory.canGoForward();
            Iterator it = components.iterator();
            while (it.hasNext()) {
                ((JComponent)it.next()).setEnabled(b);
            }
        }
    }
    public class UseStylesheetAction extends    AbstractAction
                                     implements JComponentModifier {
        List components = new LinkedList();
        public UseStylesheetAction() {}
        public void actionPerformed(ActionEvent e) {
        }
        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }
        protected void update() {
            alternateStyleSheet = null;
            Iterator it = components.iterator();
            SVGDocument doc = svgCanvas.getSVGDocument();
            while (it.hasNext()) {
                JComponent stylesheetMenu = (JComponent)it.next();
                stylesheetMenu.removeAll();
                stylesheetMenu.setEnabled(false);
                ButtonGroup buttonGroup = new ButtonGroup();
                for (Node n = doc.getFirstChild();
                     n != null && n.getNodeType() != Node.ELEMENT_NODE;
                     n = n.getNextSibling()) {
                    if (n instanceof StyleSheetProcessingInstruction) {
                        StyleSheetProcessingInstruction sspi;
                        sspi = (StyleSheetProcessingInstruction)n;
                        HashTable attrs = sspi.getPseudoAttributes();
                        final String title = (String)attrs.get("title");
                        String alt = (String)attrs.get("alternate");
                        if (title != null && "yes".equals(alt)) {
                            JRadioButtonMenuItem button;
                            button = new JRadioButtonMenuItem(title);
                            button.addActionListener
                                (new java.awt.event.ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        SVGOMDocument doc;
                                        doc = (SVGOMDocument)svgCanvas.getSVGDocument();
                                        doc.clearViewCSS();
                                        alternateStyleSheet = title;
                                        svgCanvas.setSVGDocument(doc);
                                    }
                                });
                            buttonGroup.add(button);
                            stylesheetMenu.add(button);
                            stylesheetMenu.setEnabled(true);
                        }
                    }
                }
            }
        }
    }
    public class PlayAction extends   AbstractAction
                            implements JComponentModifier {
        List components = new LinkedList();
        public PlayAction() {}
        public void actionPerformed(ActionEvent e) {
            svgCanvas.resumeProcessing();
        }
        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }
        public void update(boolean enabled) {
            Iterator it = components.iterator();
            while (it.hasNext()) {
                ((JComponent)it.next()).setEnabled(enabled);
            }
        }
    }
    public class PauseAction extends   AbstractAction
                            implements JComponentModifier {
        List components = new LinkedList();
        public PauseAction() {}
        public void actionPerformed(ActionEvent e) {
            svgCanvas.suspendProcessing();
        }
        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }
        public void update(boolean enabled) {
            Iterator it = components.iterator();
            while (it.hasNext()) {
                ((JComponent)it.next()).setEnabled(enabled);
            }
        }
    }
    public class StopAction extends    AbstractAction
                            implements JComponentModifier {
        List components = new LinkedList();
        public StopAction() {}
        public void actionPerformed(ActionEvent e) {
            svgCanvas.stopProcessing();
        }
        public void addJComponent(JComponent c) {
            components.add(c);
            c.setEnabled(false);
        }
        public void update(boolean enabled) {
            Iterator it = components.iterator();
            while (it.hasNext()) {
                ((JComponent)it.next()).setEnabled(enabled);
            }
        }
    }
    public class SetTransformAction extends AbstractAction {
        public SetTransformAction(){}
        public void actionPerformed(ActionEvent e){
            if (transformDialog == null){
                transformDialog
                    = JAffineTransformChooser.createDialog
                    (JSVGViewerFrame.this,
                     resources.getString("SetTransform.title"));
            }
            AffineTransform txf = transformDialog.showDialog();
            if(txf != null){
                AffineTransform at = svgCanvas.getRenderingTransform();
                if(at == null){
                    at = new AffineTransform();
                }
                txf.concatenate(at);
                svgCanvas.setRenderingTransform(txf);
            }
        }
    }
    public class MonitorAction extends AbstractAction {
        public MonitorAction() {}
        public void actionPerformed(ActionEvent e) {
            if (memoryMonitorFrame == null) {
                memoryMonitorFrame = new MemoryMonitor();
                Rectangle fr = getBounds();
                Dimension md = memoryMonitorFrame.getSize();
                memoryMonitorFrame.setLocation(fr.x + (fr.width  - md.width) / 2,
                                               fr.y + (fr.height - md.height) / 2);
            }
            memoryMonitorFrame.setVisible(true);
        }
    }
    public class FindDialogAction extends AbstractAction {
        public FindDialogAction() {}
        public void actionPerformed(ActionEvent e) {
            if (findDialog == null) {
                findDialog = new FindDialog(JSVGViewerFrame.this, svgCanvas);
                findDialog.setGraphicsNode(svgCanvas.getGraphicsNode());
                findDialog.pack();
                Rectangle fr = getBounds();
                Dimension td = findDialog.getSize();
                findDialog.setLocation(fr.x + (fr.width  - td.width) / 2,
                                       fr.y + (fr.height - td.height) / 2);
            }
            findDialog.setVisible(true);
        }
    }
    public class ThumbnailDialogAction extends AbstractAction {
        public ThumbnailDialogAction() {}
        public void actionPerformed(ActionEvent e) {
            if (thumbnailDialog == null) {
                thumbnailDialog
                    = new ThumbnailDialog(JSVGViewerFrame.this, svgCanvas);
                thumbnailDialog.pack();
                Rectangle fr = getBounds();
                Dimension td = thumbnailDialog.getSize();
                thumbnailDialog.setLocation(fr.x + (fr.width  - td.width) / 2,
                                            fr.y + (fr.height - td.height) / 2);
            }
            thumbnailDialog.setInteractionEnabled
                (!svgCanvas.getDisableInteractions());
            thumbnailDialog.setVisible(true);
        }
    }
    public class FullScreenAction extends AbstractAction {
        public FullScreenAction() {}
        public void actionPerformed(ActionEvent e) {
            if (window == null || !window.isVisible()) {
                if (window == null) {
                    window = new JWindow(JSVGViewerFrame.this);
                    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
                    window.setSize(size);
                }
                svgCanvas.getParent().remove(svgCanvas);
                window.getContentPane().add(svgCanvas);
                window.setVisible(true);
                window.toFront();
                svgCanvas.requestFocus();
            } else {
                svgCanvas.getParent().remove(svgCanvas);
                svgCanvasPanel.add(svgCanvas, BorderLayout.CENTER);
                window.setVisible(false);
            }
        }
    }
    public class DOMViewerAction extends AbstractAction {
        public DOMViewerAction() {
        }
        public void actionPerformed(ActionEvent e) {
            openDOMViewer();
        }
        public void openDOMViewer() {
            if (domViewer == null || domViewer.isDisplayable()) {
                domViewer = new DOMViewer
                    (svgCanvas.new JSVGViewerDOMViewerController());
                Rectangle fr = getBounds();
                Dimension td = domViewer.getSize();
                domViewer.setLocation(fr.x + (fr.width - td.width) / 2,
                                      fr.y + (fr.height - td.height) / 2);
            }
            domViewer.setVisible(true);
        }
        public DOMViewer getDOMViewer() {
            return domViewer;
        }
    }
    protected Map listeners = new HashMap();
    public Action getAction(String key) throws MissingListenerException {
        Action result = (Action)listeners.get(key);
        if (result == null) {
            throw new MissingListenerException("Can't find action.", RESOURCES, key);
        }
        return result;
    }
    long time; 
    public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
        String msg = resources.getString("Message.documentLoad");
        if (debug) {
            System.out.println(msg);
            time = System.currentTimeMillis();
        }
        statusBar.setMainMessage(msg);
        stopAction.update(true);
        svgCanvas.setCursor(WAIT_CURSOR);
    }
    public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
        if (debug) {
            System.out.print(resources.getString("Message.documentLoadTime"));
            System.out.println((System.currentTimeMillis() - time) + " ms");
        }
        setSVGDocument(e.getSVGDocument(),
                       e.getSVGDocument().getURL(),
                       e.getSVGDocument().getTitle());
    }
    public void setSVGDocument(SVGDocument svgDocument,
                               String svgDocumentURL,
                               String svgDocumentTitle) {
        this.svgDocument = svgDocument;
        if (domViewer != null) {
            if(domViewer.isVisible() && svgDocument != null) {
                domViewer.setDocument(svgDocument,
                                      (ViewCSS)svgDocument.getDocumentElement());
            } else {
                domViewer.dispose();
                domViewer = null;
            }
        }
        stopAction.update(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
        String s = svgDocumentURL;
        locationBar.setText(s);
        if (debugger != null) {
            debugger.detach();
            debugger.setDocumentURL(s);
        }
        if (title == null) {
            title = getTitle();
        }
        String dt = svgDocumentTitle;
        if (dt.length() != 0) {
            setTitle(title + ": " + dt);
        } else {
            int i = s.lastIndexOf("/");
            if (i == -1)
                i = s.lastIndexOf("\\");
            if (i == -1) {
                setTitle(title + ": " + s);
            } else {
                setTitle(title + ": " + s.substring(i + 1));
            }
        }
        localHistory.update(s);
        application.addVisitedURI(s);
        backAction.update();
        forwardAction.update();
        transformHistory = new TransformHistory();
        previousTransformAction.update();
        nextTransformAction.update();
        useStylesheetAction.update();
    }
    public void documentLoadingCancelled(SVGDocumentLoaderEvent e) {
        String msg = resources.getString("Message.documentCancelled");
        if (debug) {
            System.out.println(msg);
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(msg);
        stopAction.update(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
    }
    public void documentLoadingFailed(SVGDocumentLoaderEvent e) {
        String msg = resources.getString("Message.documentFailed");
        if (debug) {
            System.out.println(msg);
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(msg);
        stopAction.update(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
    }
    public void gvtBuildStarted(GVTTreeBuilderEvent e) {
        String msg = resources.getString("Message.treeBuild");
        if (debug) {
            System.out.println(msg);
            time = System.currentTimeMillis();
        }
        statusBar.setMainMessage(msg);
        stopAction.update(true);
        svgCanvas.setCursor(WAIT_CURSOR);
    }
    public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
        if (debug) {
            System.out.print(resources.getString("Message.treeBuildTime"));
            System.out.println((System.currentTimeMillis() - time) + " ms");
        }
        if (findDialog != null) {
            if(findDialog.isVisible()) {
                findDialog.setGraphicsNode(svgCanvas.getGraphicsNode());
            } else {
                findDialog.dispose();
                findDialog = null;
            }
        }
        stopAction.update(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
        svgCanvas.setSelectionOverlayXORMode
            (application.isSelectionOverlayXORMode());
        svgCanvas.requestFocus();  
        if (debugger != null) {
            debugger.attach();
        }
    }
    public void gvtBuildCancelled(GVTTreeBuilderEvent e) {
        String msg = resources.getString("Message.treeCancelled");
        if (debug) {
            System.out.println(msg);
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(msg);
        stopAction.update(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
        svgCanvas.setSelectionOverlayXORMode
            (application.isSelectionOverlayXORMode());
    }
    public void gvtBuildFailed(GVTTreeBuilderEvent e) {
        String msg = resources.getString("Message.treeFailed");
        if (debug) {
            System.out.println(msg);
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(msg);
        stopAction.update(false);
        svgCanvas.setCursor(DEFAULT_CURSOR);
        svgCanvas.setSelectionOverlayXORMode
            (application.isSelectionOverlayXORMode());
        if (autoAdjust) {
            pack();
        }
    }
    public void svgLoadEventDispatchStarted(SVGLoadEventDispatcherEvent e) {
        String msg = resources.getString("Message.onload");
        if (debug) {
            System.out.println(msg);
            time = System.currentTimeMillis();
        }
        stopAction.update(true);
        statusBar.setMainMessage(msg);
    }
    public void svgLoadEventDispatchCompleted(SVGLoadEventDispatcherEvent e) {
        if (debug) {
            System.out.print(resources.getString("Message.onloadTime"));
            System.out.println((System.currentTimeMillis() - time) + " ms");
        }
        stopAction.update(false);
        statusBar.setMainMessage("");
        statusBar.setMessage(resources.getString("Message.done"));
    }
    public void svgLoadEventDispatchCancelled(SVGLoadEventDispatcherEvent e) {
        String msg = resources.getString("Message.onloadCancelled");
        if (debug) {
            System.out.println(msg);
        }
        stopAction.update(false);
        statusBar.setMainMessage("");
        statusBar.setMessage(msg);
    }
    public void svgLoadEventDispatchFailed(SVGLoadEventDispatcherEvent e) {
        String msg = resources.getString("Message.onloadFailed");
        if (debug) {
            System.out.println(msg);
        }
        stopAction.update(false);
        statusBar.setMainMessage("");
        statusBar.setMessage(msg);
    }
    public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
        if (debug) {
            String msg = resources.getString("Message.treeRenderingPrep");
            System.out.println(msg);
            time = System.currentTimeMillis();
        }
        stopAction.update(true);
        svgCanvas.setCursor(WAIT_CURSOR);
        statusBar.setMainMessage(resources.getString("Message.treeRendering"));
    }
    public void gvtRenderingStarted(GVTTreeRendererEvent e) {
        if (debug) {
            String msg = resources.getString("Message.treeRenderingPrepTime");
            System.out.print(msg);
            System.out.println((System.currentTimeMillis() - time) + " ms");
            time = System.currentTimeMillis();
            msg = resources.getString("Message.treeRenderingStart");
            System.out.println(msg);
        }
    }
    public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
        if (debug) {
            String msg = resources.getString("Message.treeRenderingTime");
            System.out.print(msg);
            System.out.println((System.currentTimeMillis() - time) + " ms");
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(resources.getString("Message.done"));
        if (!svgCanvas.isDynamic() || managerStopped) {
            stopAction.update(false);
        }
        svgCanvas.setCursor(DEFAULT_CURSOR);
        transformHistory.update(svgCanvas.getRenderingTransform());
        previousTransformAction.update();
        nextTransformAction.update();
    }
    public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
        String msg = resources.getString("Message.treeRenderingCancelled");
        if (debug) {
            System.out.println(msg);
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(msg);
        if (!svgCanvas.isDynamic()) {
            stopAction.update(false);
        }
        svgCanvas.setCursor(DEFAULT_CURSOR);
    }
    public void gvtRenderingFailed(GVTTreeRendererEvent e) {
        String msg = resources.getString("Message.treeRenderingFailed");
        if (debug) {
            System.out.println(msg);
        }
        statusBar.setMainMessage("");
        statusBar.setMessage(msg);
        if (!svgCanvas.isDynamic()) {
            stopAction.update(false);
        }
        svgCanvas.setCursor(DEFAULT_CURSOR);
    }
    public void linkActivated(LinkActivationEvent e) {
        String s = e.getReferencedURI();
        if (svgDocument != null) {
            ParsedURL docURL = new ParsedURL(svgDocument.getURL());
            ParsedURL url    = new ParsedURL(docURL, s);
            if (!url.sameFile(docURL)) {
                return;
            }
            if (s.indexOf( '#' ) != -1) {
                localHistory.update(s);
                locationBar.setText(s);
                if (debugger != null) {
                    debugger.detach();
                    debugger.setDocumentURL(s);
                }
                application.addVisitedURI(s);
                backAction.update();
                forwardAction.update();
                transformHistory = new TransformHistory();
                previousTransformAction.update();
                nextTransformAction.update();
            }
        }
    }
    public void managerStarted(UpdateManagerEvent e) {
        if (debug) {
            String msg = resources.getString("Message.updateManagerStarted");
            System.out.println(msg);
        }
        managerStopped = false;
        playAction.update(false);
        pauseAction.update(true);
        stopAction.update(true);
    }
    public void managerSuspended(UpdateManagerEvent e) {
        if (debug) {
            String msg = resources.getString("Message.updateManagerSuspended");
            System.out.println(msg);
        }
        playAction.update(true);
        pauseAction.update(false);
    }
    public void managerResumed(UpdateManagerEvent e) {
        if (debug) {
            String msg = resources.getString("Message.updateManagerResumed");
            System.out.println(msg);
        }
        playAction.update(false);
        pauseAction.update(true);
    }
    public void managerStopped(UpdateManagerEvent e) {
        if (debug) {
            String msg = resources.getString("Message.updateManagerStopped");
            System.out.println(msg);
        }
        managerStopped = true;
        playAction.update(false);
        pauseAction.update(false);
        stopAction.update(false);
    }
    public void updateStarted(final UpdateManagerEvent e) {
    }
    public void updateCompleted(final UpdateManagerEvent e) {
    }
    public void updateFailed(UpdateManagerEvent e) {
    }
    protected class UserAgent implements SVGUserAgent {
        protected UserAgent() {
        }
        public void displayError(String message) {
            if (debug) {
                System.err.println(message);
            }
            JOptionPane pane =
                new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
            JDialog dialog = pane.createDialog(JSVGViewerFrame.this, "ERROR");
            dialog.setModal(false);
            dialog.setVisible(true);
        }
        public void displayError(Exception ex) {
            if (debug) {
                ex.printStackTrace();
            }
            JErrorPane pane = new JErrorPane(ex, JOptionPane.ERROR_MESSAGE);
            JDialog dialog = pane.createDialog(JSVGViewerFrame.this, "ERROR");
            dialog.setModal(false);
            dialog.setVisible(true);
        }
        public void displayMessage(String message) {
            statusBar.setMessage(message);
        }
        public void showAlert(String message) {
            svgCanvas.showAlert(message);
        }
        public String showPrompt(String message) {
            return svgCanvas.showPrompt(message);
        }
        public String showPrompt(String message, String defaultValue) {
            return svgCanvas.showPrompt(message, defaultValue);
        }
        public boolean showConfirm(String message) {
            return svgCanvas.showConfirm(message);
        }
        public float getPixelUnitToMillimeter() {
            return 0.26458333333333333333333333333333f; 
        }
        public float getPixelToMM() {
            return getPixelUnitToMillimeter();
        }
        public String getDefaultFontFamily() {
            return application.getDefaultFontFamily();
        }
        public float getMediumFontSize() {
            return 9f * 25.4f / (72f * getPixelUnitToMillimeter());
        }
        public float getLighterFontWeight(float f) {
            int weight = ((int)((f+50)/100))*100;
            switch (weight) {
            case 100: return 100;
            case 200: return 100;
            case 300: return 200;
            case 400: return 300;
            case 500: return 400;
            case 600: return 400;
            case 700: return 400;
            case 800: return 400;
            case 900: return 400;
            default:
                throw new IllegalArgumentException("Bad Font Weight: " + f);
            }
        }
        public float getBolderFontWeight(float f) {
            int weight = ((int)((f+50)/100))*100;
            switch (weight) {
            case 100: return 600;
            case 200: return 600;
            case 300: return 600;
            case 400: return 600;
            case 500: return 600;
            case 600: return 700;
            case 700: return 800;
            case 800: return 900;
            case 900: return 900;
            default:
                throw new IllegalArgumentException("Bad Font Weight: " + f);
            }
        }
        public String getLanguages() {
            return application.getLanguages();
        }
        public String getUserStyleSheetURI() {
            return application.getUserStyleSheetURI();
        }
        public String getXMLParserClassName() {
            return application.getXMLParserClassName();
        }
        public boolean isXMLParserValidating() {
            return application.isXMLParserValidating();
        }
        public String getMedia() {
            return application.getMedia();
        }
        public String getAlternateStyleSheet() {
            return alternateStyleSheet;
        }
        public void openLink(String uri, boolean newc) {
            if (newc) {
                application.openLink(uri);
            } else {
                showSVGDocument(uri);
            }
        }
        public boolean supportExtension(String s) {
            return false;
        }
        public void handleElement(Element elt, Object data){
        }
        public ScriptSecurity getScriptSecurity(String scriptType,
                                                ParsedURL scriptURL,
                                                ParsedURL docURL){
            if (!application.canLoadScriptType(scriptType)) {
                return new NoLoadScriptSecurity(scriptType);
            } else {
                switch(application.getAllowedScriptOrigin()) {
                case ResourceOrigin.ANY:
                    return new RelaxedScriptSecurity(scriptType,
                                                     scriptURL,
                                                     docURL);
                case ResourceOrigin.DOCUMENT:
                    return new DefaultScriptSecurity(scriptType,
                                                     scriptURL,
                                                     docURL);
                case ResourceOrigin.EMBEDED:
                    return new EmbededScriptSecurity(scriptType,
                                                     scriptURL,
                                                     docURL);
                default:
                    return new NoLoadScriptSecurity(scriptType);
                }
            }
        }
        public void checkLoadScript(String scriptType,
                                    ParsedURL scriptURL,
                                    ParsedURL docURL) throws SecurityException {
            ScriptSecurity s = getScriptSecurity(scriptType,
                                                     scriptURL,
                                                     docURL);
            if (s != null) {
                s.checkLoadScript();
            }
        }
        public ExternalResourceSecurity
            getExternalResourceSecurity(ParsedURL resourceURL,
                                        ParsedURL docURL){
            switch(application.getAllowedExternalResourceOrigin()) {
            case ResourceOrigin.ANY:
                return new RelaxedExternalResourceSecurity(resourceURL,
                                                           docURL);
            case ResourceOrigin.DOCUMENT:
                return new DefaultExternalResourceSecurity(resourceURL,
                                                           docURL);
            case ResourceOrigin.EMBEDED:
                return new EmbededExternalResourceSecurity(resourceURL);
            default:
                return new NoLoadExternalResourceSecurity();
            }
        }
        public void
            checkLoadExternalResource(ParsedURL resourceURL,
                                      ParsedURL docURL) throws SecurityException {
            ExternalResourceSecurity s
                =  getExternalResourceSecurity(resourceURL, docURL);
            if (s != null) {
                s.checkLoadExternalResource();
            }
        }
    }
    protected static class ImageFileFilter extends FileFilter {
        protected String extension;
        public ImageFileFilter(String extension) {
            this.extension = extension;
        }
        public boolean accept(File f) {
            boolean accept = false;
            String fileName = null;
            if (f != null) {
                if (f.isDirectory()) {
                    accept = true;
                } else {
                    fileName = f.getPath().toLowerCase();
                    if (fileName.endsWith(extension)) {
                        accept = true;
                    }
                }
            }
            return accept;
        }
        public String getDescription() {
            return extension;
        }
    }
}
