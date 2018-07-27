package org.apache.batik.apps.svgbrowser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.Authenticator;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.util.ApplicationSecurityEnforcer;
import org.apache.batik.util.Platform;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.util.resources.ResourceManager;
public class Main implements Application {
    public static final String UNKNOWN_SCRIPT_TYPE_LOAD_KEY_EXTENSION
        = ".load";
    public static final String PROPERTY_USER_HOME = "user.home";
    public static final String PROPERTY_JAVA_SECURITY_POLICY
        = "java.security.policy";
    public static final String BATIK_CONFIGURATION_SUBDIRECTORY = ".batik";
    public static final String SQUIGGLE_CONFIGURATION_FILE = "preferences.xml";
    public static final String SQUIGGLE_POLICY_FILE = "__svgbrowser.policy";
    public static final String POLICY_GRANT_SCRIPT_NETWORK_ACCESS
        = "grant {\n  permission java.net.SocketPermission \"*\", \"listen, connect, resolve, accept\";\n};\n\n";
    public static final String POLICY_GRANT_SCRIPT_FILE_ACCESS
        = "grant {\n  permission java.io.FilePermission \"<<ALL FILES>>\", \"read\";\n};\n\n";
    public static final String PREFERENCE_KEY_VISITED_URI_LIST
        = "preference.key.visited.uri.list";
    public static final String PREFERENCE_KEY_VISITED_URI_LIST_LENGTH
        = "preference.key.visited.uri.list.length";
    public static final String URI_SEPARATOR = " ";
    public static final String DEFAULT_DEFAULT_FONT_FAMILY
        = "Arial, Helvetica, sans-serif";
    public static final String SVG_INITIALIZATION = "resources/init.svg";
    protected String svgInitializationURI;
    public static void main(String[] args) {
        new Main(args);
    }
    public static final String RESOURCES =
        "org.apache.batik.apps.svgbrowser.resources.Main";
    public static final String SQUIGGLE_SECURITY_POLICY
        = "org/apache/batik/apps/svgbrowser/resources/svgbrowser.policy";
    protected static ResourceBundle bundle;
    protected static ResourceManager resources;
    static {
        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());
        resources = new ResourceManager(bundle);
    }
    protected static ImageIcon frameIcon = new ImageIcon
        (Main.class.getResource(resources.getString("Frame.icon")));
    protected XMLPreferenceManager preferenceManager;
    public static final int MAX_VISITED_URIS = 10;
    protected Vector lastVisited = new Vector();
    protected int maxVisitedURIs = MAX_VISITED_URIS;
    protected String[] arguments;
    protected boolean overrideSecurityPolicy = false;
    protected ApplicationSecurityEnforcer securityEnforcer;
    protected Map handlers = new HashMap();
    {
        handlers.put("-font-size", new FontSizeHandler());
    }
    protected List viewerFrames = new LinkedList();
    protected PreferenceDialog preferenceDialog;
    protected String uiSpecialization;
    public Main(String[] args) {
        arguments = args;
        if (Platform.isOSX) {
            uiSpecialization = "OSX";
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            try {
                Class Application = Class.forName("com.apple.eawt.Application");
                Class ApplicationListener =
                    Class.forName("com.apple.eawt.ApplicationListener");
                Class ApplicationEvent =
                    Class.forName("com.apple.eawt.ApplicationEvent");
                Method getApplication = Application.getMethod("getApplication",
                                                              new Class[0]);
                Method addApplicationListener =
                    Application.getMethod("addApplicationListener",
                                          new Class[] { ApplicationListener });
                final Method setHandled =
                    ApplicationEvent.getMethod("setHandled",
                                               new Class[] { Boolean.TYPE });
                Method setEnabledPreferencesMenu =
                    Application.getMethod("setEnabledPreferencesMenu",
                                          new Class[] { Boolean.TYPE });
                InvocationHandler listenerHandler = new InvocationHandler() {
                    public Object invoke(Object proxy, Method method,
                                         Object[] args) {
                        String name = method.getName();
                        if (name.equals("handleAbout")) {
                            JSVGViewerFrame relativeTo =
                                (JSVGViewerFrame) viewerFrames.get(0);
                            AboutDialog dlg = new AboutDialog(relativeTo);
                            dlg.setSize(dlg.getPreferredSize());
                            dlg.setLocationRelativeTo(relativeTo);
                            dlg.setVisible(true);
                            dlg.toFront();
                        } else if (name.equals("handlePreferences")) {
                            JSVGViewerFrame relativeTo =
                                (JSVGViewerFrame) viewerFrames.get(0);
                            showPreferenceDialog(relativeTo);
                        } else if (name.equals("handleQuit")) {
                        } else {
                            return null;
                        }
                        try {
                            setHandled.invoke(args[0],
                                              new Object[] { Boolean.TRUE });
                        } catch (Exception e) {
                        }
                        return null;
                    }
                };
                Object application = getApplication.invoke(null, (Object[]) null);
                setEnabledPreferencesMenu.invoke(application,
                                                 new Object[] { Boolean.TRUE });
                Object listener =
                    Proxy.newProxyInstance(Main.class.getClassLoader(),
                                           new Class[] { ApplicationListener },
                                           listenerHandler);
                addApplicationListener.invoke(application,
                                              new Object[] { listener });
            } catch (Exception ex) {
                ex.printStackTrace();
                uiSpecialization = null;
            }
        }
        Map defaults = new HashMap(11);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_LANGUAGES,
                     Locale.getDefault().getLanguage());
        defaults.put(PreferenceDialog.PREFERENCE_KEY_SHOW_RENDERING,
                     Boolean.FALSE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_AUTO_ADJUST_WINDOW,
                     Boolean.TRUE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_SELECTION_XOR_MODE,
                     Boolean.FALSE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_ENABLE_DOUBLE_BUFFERING,
                     Boolean.TRUE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_SHOW_DEBUG_TRACE,
                     Boolean.FALSE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_PROXY_HOST,
                     "");
        defaults.put(PreferenceDialog.PREFERENCE_KEY_PROXY_PORT,
                     "");
        defaults.put(PreferenceDialog.PREFERENCE_KEY_CSS_MEDIA,
                     "screen");
        defaults.put(PreferenceDialog.PREFERENCE_KEY_DEFAULT_FONT_FAMILY,
                     DEFAULT_DEFAULT_FONT_FAMILY);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_IS_XML_PARSER_VALIDATING,
                     Boolean.FALSE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_ENFORCE_SECURE_SCRIPTING,
                     Boolean.TRUE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_GRANT_SCRIPT_FILE_ACCESS,
                     Boolean.FALSE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_GRANT_SCRIPT_NETWORK_ACCESS,
                     Boolean.FALSE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_LOAD_JAVA,
                     Boolean.TRUE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_LOAD_ECMASCRIPT,
                     Boolean.TRUE);
        defaults.put(PreferenceDialog.PREFERENCE_KEY_ALLOWED_SCRIPT_ORIGIN,
                     new Integer(ResourceOrigin.DOCUMENT));
        defaults.put(PreferenceDialog.PREFERENCE_KEY_ALLOWED_EXTERNAL_RESOURCE_ORIGIN,
                     new Integer(ResourceOrigin.ANY));
        defaults.put(PREFERENCE_KEY_VISITED_URI_LIST,
                     "");
        defaults.put(PREFERENCE_KEY_VISITED_URI_LIST_LENGTH,
                     new Integer(MAX_VISITED_URIS));
        defaults.put(PreferenceDialog.PREFERENCE_KEY_ANIMATION_RATE_LIMITING_MODE,
                     new Integer(1));
        defaults.put(PreferenceDialog.PREFERENCE_KEY_ANIMATION_RATE_LIMITING_CPU,
                     new Float(0.75f));
        defaults.put(PreferenceDialog.PREFERENCE_KEY_ANIMATION_RATE_LIMITING_FPS,
                     new Float(10));
        defaults.put(PreferenceDialog.PREFERENCE_KEY_USER_STYLESHEET_ENABLED,
                     Boolean.TRUE);
        securityEnforcer
            = new ApplicationSecurityEnforcer(this.getClass(),
                                              SQUIGGLE_SECURITY_POLICY);
        try {
            preferenceManager = new XMLPreferenceManager(SQUIGGLE_CONFIGURATION_FILE,
                                                         defaults);
            String dir = System.getProperty(PROPERTY_USER_HOME);
            File f = new File(dir, BATIK_CONFIGURATION_SUBDIRECTORY);
            f.mkdir();
            XMLPreferenceManager.setPreferenceDirectory(f.getCanonicalPath());
            preferenceManager.load();
            setPreferences();
            initializeLastVisited();
            Authenticator.setDefault(new JAuthenticator());
        } catch (Exception e) {
            e.printStackTrace();
        }
        final AboutDialog initDialog = new AboutDialog();
        ((BorderLayout) initDialog.getContentPane().getLayout()).setVgap(8);
        final JProgressBar pb = new JProgressBar(0, 3);
        initDialog.getContentPane().add(pb, BorderLayout.SOUTH);
        Dimension ss = initDialog.getToolkit().getScreenSize();
        Dimension ds = initDialog.getPreferredSize();
        initDialog.setLocation((ss.width  - ds.width) / 2,
                               (ss.height - ds.height) / 2);
        initDialog.setSize(ds);
        initDialog.setVisible(true);
        final JSVGViewerFrame v = new JSVGViewerFrame(this);
        JSVGCanvas c = v.getJSVGCanvas();
        c.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
            public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
                pb.setValue(1);
            }
            public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
                pb.setValue(2);
            }
        });
        c.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
            public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
                pb.setValue(3);
            }
        });
        c.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                initDialog.dispose();
                v.dispose();
                System.gc();
                run();
            }
        });
        c.setSize(100, 100);
        svgInitializationURI = Main.class.getResource(SVG_INITIALIZATION).toString();
        c.loadSVGDocument(svgInitializationURI);
    }
    public void installCustomPolicyFile() throws IOException {
        String securityPolicyProperty
            = System.getProperty(PROPERTY_JAVA_SECURITY_POLICY);
        if (overrideSecurityPolicy
            ||
            securityPolicyProperty == null
            ||
            "".equals(securityPolicyProperty)) {
            ParsedURL policyURL = new ParsedURL(securityEnforcer.getPolicyURL());
            String dir = System.getProperty(PROPERTY_USER_HOME);
            File batikConfigDir = new File(dir, BATIK_CONFIGURATION_SUBDIRECTORY);
            File policyFile = new File(batikConfigDir, SQUIGGLE_POLICY_FILE);
            Reader r = new BufferedReader(new InputStreamReader(policyURL.openStream()));
            Writer w = new FileWriter(policyFile);
            char[] buf = new char[1024];
            int n = 0;
            while ( (n=r.read(buf, 0, buf.length)) != -1 ) {
                w.write(buf, 0, n);
            }
            r.close();
            boolean grantScriptNetworkAccess
                = preferenceManager.getBoolean
                (PreferenceDialog.PREFERENCE_KEY_GRANT_SCRIPT_NETWORK_ACCESS);
            boolean grantScriptFileAccess
                = preferenceManager.getBoolean
                (PreferenceDialog.PREFERENCE_KEY_GRANT_SCRIPT_FILE_ACCESS);
            if (grantScriptNetworkAccess) {
                w.write(POLICY_GRANT_SCRIPT_NETWORK_ACCESS);
            }
            if (grantScriptFileAccess) {
                w.write(POLICY_GRANT_SCRIPT_FILE_ACCESS);
            }
            w.close();
            overrideSecurityPolicy = true;
            System.setProperty(PROPERTY_JAVA_SECURITY_POLICY,
                               policyFile.toURL().toString());
        }
    }
    public void run() {
        try {
            int i = 0;
            for (; i < arguments.length; i++) {
                OptionHandler oh = (OptionHandler)handlers.get(arguments[i]);
                if (oh == null) {
                    break;
                }
                i = oh.handleOption(i);
            }
            JSVGViewerFrame frame = createAndShowJSVGViewerFrame();
            while (i < arguments.length) {
                if (arguments[i].length() == 0) {
                    i++;
                    continue;
                }
                File file = new File(arguments[i]);
                String uri = null;
                try{
                    if (file.canRead()) {
                        uri = file.toURL().toString();
                    }
                }catch(SecurityException se){
                }
                if(uri == null){
                    uri = arguments[i];
                    ParsedURL purl = null;
                    purl = new ParsedURL(arguments[i]);
                    if (!purl.complete())
                        uri = null;
                }
                if (uri != null) {
                    if (frame == null)
                        frame = createAndShowJSVGViewerFrame();
                    frame.showSVGDocument(uri);
                    frame = null;
                } else {
                    JOptionPane.showMessageDialog
                        (frame,
                         resources.getString("Error.skipping.file")
                         + arguments[i]);
                }
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            printUsage();
        }
    }
    protected void printUsage() {
        System.out.println();
        System.out.println(resources.getString("Command.header"));
        System.out.println(resources.getString("Command.syntax"));
        System.out.println();
        System.out.println(resources.getString("Command.options"));
        Iterator it = handlers.keySet().iterator();
        while (it.hasNext()) {
            String s = (String)it.next();
            System.out.println(((OptionHandler)handlers.get(s)).getDescription());
        }
    }
    protected interface OptionHandler {
        int handleOption(int i);
        String getDescription();
    }
    protected class FontSizeHandler implements OptionHandler {
        public int handleOption(int i) {
            int size = Integer.parseInt(arguments[++i]);
            Font font = new Font("Dialog", Font.PLAIN, size);
            FontUIResource fontRes = new FontUIResource(font);
            UIManager.put("CheckBox.font", fontRes);
            UIManager.put("PopupMenu.font", fontRes);
            UIManager.put("TextPane.font", fontRes);
            UIManager.put("MenuItem.font", fontRes);
            UIManager.put("ComboBox.font", fontRes);
            UIManager.put("Button.font", fontRes);
            UIManager.put("Tree.font", fontRes);
            UIManager.put("ScrollPane.font", fontRes);
            UIManager.put("TabbedPane.font", fontRes);
            UIManager.put("EditorPane.font", fontRes);
            UIManager.put("TitledBorder.font", fontRes);
            UIManager.put("Menu.font", fontRes);
            UIManager.put("TextArea.font", fontRes);
            UIManager.put("OptionPane.font", fontRes);
            UIManager.put("DesktopIcon.font", fontRes);
            UIManager.put("MenuBar.font", fontRes);
            UIManager.put("ToolBar.font", fontRes);
            UIManager.put("RadioButton.font", fontRes);
            UIManager.put("RadioButtonMenuItem.font", fontRes);
            UIManager.put("ToggleButton.font", fontRes);
            UIManager.put("ToolTip.font", fontRes);
            UIManager.put("ProgressBar.font", fontRes);
            UIManager.put("TableHeader.font", fontRes);
            UIManager.put("Panel.font", fontRes);
            UIManager.put("List.font", fontRes);
            UIManager.put("ColorChooser.font", fontRes);
            UIManager.put("PasswordField.font", fontRes);
            UIManager.put("TextField.font", fontRes);
            UIManager.put("Table.font", fontRes);
            UIManager.put("Label.font", fontRes);
            UIManager.put("InternalFrameTitlePane.font", fontRes);
            UIManager.put("CheckBoxMenuItem.font", fontRes);
            return i;
        }
        public String getDescription() {
            return resources.getString("Command.font-size");
        }
    }
    public JSVGViewerFrame createAndShowJSVGViewerFrame() {
        JSVGViewerFrame mainFrame = new JSVGViewerFrame(this);
        mainFrame.setSize(resources.getInteger("Frame.width"),
                          resources.getInteger("Frame.height"));
        mainFrame.setIconImage(frameIcon.getImage());
        mainFrame.setTitle(resources.getString("Frame.title"));
        mainFrame.setVisible(true);
        viewerFrames.add(mainFrame);
        setPreferences(mainFrame);
        return mainFrame;
    }
    public void closeJSVGViewerFrame(JSVGViewerFrame f) {
        f.getJSVGCanvas().stopProcessing();
        viewerFrames.remove(f);
        if (viewerFrames.size() == 0) {
            System.exit(0);
        }
        f.dispose();
    }
    public Action createExitAction(JSVGViewerFrame vf) {
        return new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };
    }
    public void openLink(String url) {
        JSVGViewerFrame f = createAndShowJSVGViewerFrame();
        f.getJSVGCanvas().loadSVGDocument(url);
    }
    public String getXMLParserClassName() {
        return XMLResourceDescriptor.getXMLParserClassName();
    }
    public boolean isXMLParserValidating() {
        return preferenceManager.getBoolean
            (PreferenceDialog.PREFERENCE_KEY_IS_XML_PARSER_VALIDATING);
    }
    public void showPreferenceDialog(JSVGViewerFrame f) {
        if (preferenceDialog == null) {
            preferenceDialog = new PreferenceDialog(f, preferenceManager);
        }
        if (preferenceDialog.showDialog() == PreferenceDialog.OK_OPTION) {
            try {
                preferenceManager.save();
                setPreferences();
            } catch (Exception e) {
            }
        }
    }
    private void setPreferences() throws IOException {
        Iterator it = viewerFrames.iterator();
        while (it.hasNext()) {
            setPreferences((JSVGViewerFrame)it.next());
        }
        System.setProperty("proxyHost", preferenceManager.getString
                           (PreferenceDialog.PREFERENCE_KEY_PROXY_HOST));
        System.setProperty("proxyPort", preferenceManager.getString
                           (PreferenceDialog.PREFERENCE_KEY_PROXY_PORT));
        installCustomPolicyFile();
        securityEnforcer.enforceSecurity
            (preferenceManager.getBoolean
             (PreferenceDialog.PREFERENCE_KEY_ENFORCE_SECURE_SCRIPTING)
             );
    }
    private void setPreferences(JSVGViewerFrame vf) {
        boolean db = preferenceManager.getBoolean
            (PreferenceDialog.PREFERENCE_KEY_ENABLE_DOUBLE_BUFFERING);
        vf.getJSVGCanvas().setDoubleBufferedRendering(db);
        boolean sr = preferenceManager.getBoolean
            (PreferenceDialog.PREFERENCE_KEY_SHOW_RENDERING);
        vf.getJSVGCanvas().setProgressivePaint(sr);
        boolean d = preferenceManager.getBoolean
            (PreferenceDialog.PREFERENCE_KEY_SHOW_DEBUG_TRACE);
        vf.setDebug(d);
        boolean aa = preferenceManager.getBoolean
            (PreferenceDialog.PREFERENCE_KEY_AUTO_ADJUST_WINDOW);
        vf.setAutoAdjust(aa);
        boolean dd = preferenceManager.getBoolean
            (PreferenceDialog.PREFERENCE_KEY_SELECTION_XOR_MODE);
        vf.getJSVGCanvas().setSelectionOverlayXORMode(dd);
        int al = preferenceManager.getInteger
            (PreferenceDialog.PREFERENCE_KEY_ANIMATION_RATE_LIMITING_MODE);
        if (al < 0 || al > 2) {
            al = 1;
        }
        switch (al) {
            case 0: 
                vf.getJSVGCanvas().setAnimationLimitingNone();
                break;
            case 1: { 
                float pc = preferenceManager.getFloat
                    (PreferenceDialog.PREFERENCE_KEY_ANIMATION_RATE_LIMITING_CPU);
                if (pc <= 0f || pc > 1.0f) {
                    pc = 0.75f;
                }
                vf.getJSVGCanvas().setAnimationLimitingCPU(pc);
                break;
            }
            case 2: { 
                float fps = preferenceManager.getFloat
                    (PreferenceDialog.PREFERENCE_KEY_ANIMATION_RATE_LIMITING_FPS);
                if (fps <= 0f) {
                    fps = 10f;
                }
                vf.getJSVGCanvas().setAnimationLimitingFPS(fps);
                break;
            }
        }
    }
    public String getLanguages() {
        String s = preferenceManager.getString
            (PreferenceDialog.PREFERENCE_KEY_LANGUAGES);
        return (s == null)
            ? Locale.getDefault().getLanguage()
            : s;
    }
    public String getUserStyleSheetURI() {
        boolean enabled = preferenceManager.getBoolean
            (PreferenceDialog.PREFERENCE_KEY_USER_STYLESHEET_ENABLED);
        String ssPath = preferenceManager.getString
            (PreferenceDialog.PREFERENCE_KEY_USER_STYLESHEET);
        if (!enabled || ssPath.length() == 0) {
            return null;
        }
        try {
            File f = new File(ssPath);
            if (f.exists()) {
                return f.toURL().toString();
            }
        } catch (IOException ioe) {
        }
        return ssPath;
    }
    public String getDefaultFontFamily() {
        return preferenceManager.getString
            (PreferenceDialog.PREFERENCE_KEY_DEFAULT_FONT_FAMILY);
    }
    public String getMedia() {
        String s = preferenceManager.getString
            (PreferenceDialog.PREFERENCE_KEY_CSS_MEDIA);
        return (s == null) ? "screen" : s;
    }
    public boolean isSelectionOverlayXORMode() {
        return preferenceManager.getBoolean
            (PreferenceDialog.PREFERENCE_KEY_SELECTION_XOR_MODE);
    }
    public boolean canLoadScriptType(String scriptType){
        if (SVGConstants.SVG_SCRIPT_TYPE_ECMASCRIPT.equals(scriptType)
                || SVGConstants.SVG_SCRIPT_TYPE_APPLICATION_ECMASCRIPT
                    .equals(scriptType)
                || SVGConstants.SVG_SCRIPT_TYPE_JAVASCRIPT.equals(scriptType)
                || SVGConstants.SVG_SCRIPT_TYPE_APPLICATION_JAVASCRIPT
                    .equals(scriptType)) {
            return preferenceManager.getBoolean
                (PreferenceDialog.PREFERENCE_KEY_LOAD_ECMASCRIPT);
        } else if (SVGConstants.SVG_SCRIPT_TYPE_JAVA.equals(scriptType)) {
            return preferenceManager.getBoolean
                (PreferenceDialog.PREFERENCE_KEY_LOAD_JAVA);
        } else {
            return preferenceManager.getBoolean
                (scriptType + UNKNOWN_SCRIPT_TYPE_LOAD_KEY_EXTENSION);
        }
    }
    public int getAllowedScriptOrigin() {
        int ret = preferenceManager.getInteger
            (PreferenceDialog.PREFERENCE_KEY_ALLOWED_SCRIPT_ORIGIN);
        return ret;
    }
    public int getAllowedExternalResourceOrigin() {
        int ret = preferenceManager.getInteger
            (PreferenceDialog.PREFERENCE_KEY_ALLOWED_EXTERNAL_RESOURCE_ORIGIN);
        return ret;
    }
    public void addVisitedURI(String uri) {
        if(svgInitializationURI.equals(uri)) {
            return;
        }
        int maxVisitedURIs =
            preferenceManager.getInteger
            (PREFERENCE_KEY_VISITED_URI_LIST_LENGTH);
        if (maxVisitedURIs < 0) {
            maxVisitedURIs = 0;
        }
        if (lastVisited.contains(uri)) {
            lastVisited.removeElement(uri);
        }
        while (lastVisited.size() > 0 && lastVisited.size() > (maxVisitedURIs-1)) {
            lastVisited.removeElementAt(0);
        }
        if (maxVisitedURIs > 0) {
            lastVisited.addElement(uri);
        }
        StringBuffer lastVisitedBuffer = new StringBuffer( lastVisited.size() * 8 );
        for (int i=0; i<lastVisited.size(); i++) {
            lastVisitedBuffer.append
                (URLEncoder.encode(lastVisited.get(i).toString()));
            lastVisitedBuffer.append(URI_SEPARATOR);
        }
        preferenceManager.setString
            (PREFERENCE_KEY_VISITED_URI_LIST,
             lastVisitedBuffer.toString());
        try {
            preferenceManager.save();
        } catch (Exception e) {
        }
    }
    public String[] getVisitedURIs() {
        String[] visitedURIs = new String[lastVisited.size()];
        lastVisited.toArray(visitedURIs);
        return visitedURIs;
    }
    public String getUISpecialization() {
        return uiSpecialization;
    }
    protected void initializeLastVisited(){
        String lastVisitedStr
            = preferenceManager.getString(PREFERENCE_KEY_VISITED_URI_LIST);
        StringTokenizer st
            = new StringTokenizer(lastVisitedStr,
                                  URI_SEPARATOR);
        int n = st.countTokens();
        int maxVisitedURIs
            = preferenceManager.getInteger
            (PREFERENCE_KEY_VISITED_URI_LIST_LENGTH);
        if (n > maxVisitedURIs) {
            n = maxVisitedURIs;
        }
        for (int i=0; i<n; i++) {
                lastVisited.addElement(URLDecoder.decode(st.nextToken()));
        }
    }
}
