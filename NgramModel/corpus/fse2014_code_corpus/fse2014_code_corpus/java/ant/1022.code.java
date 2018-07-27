package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
public class XmlPropertyTest extends BuildFileTest {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public XmlPropertyTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/xmlproperty.xml");
    }
    public void testFile() {
        testProperties("test");
    }
    public void testResource() {
        testProperties("testResource");
    }
    private void testProperties(String target) {
        executeTarget(target);
        assertEquals("true", getProject().getProperty("root-tag(myattr)"));
        assertEquals("Text", getProject().getProperty("root-tag.inner-tag"));
        assertEquals("val",
                     getProject().getProperty("root-tag.inner-tag(someattr)"));
        assertEquals("false", getProject().getProperty("root-tag.a2.a3.a4"));
        assertEquals("CDATA failed",
            "<test>", getProject().getProperty("root-tag.cdatatag"));
    }
    public void testDTD() {
        executeTarget("testdtd");
        assertEquals("Text", getProject().getProperty("root-tag.inner-tag"));
    }
    public void testNone () {
        doTest("testNone", false, false, false, false, false);
    }
    public void testKeeproot() {
        doTest("testKeeproot", true, false, false, false, false);
    }
    public void testCollapse () {
        doTest("testCollapse", false, true, false, false, false);
    }
    public void testSemantic () {
        doTest("testSemantic", false, false, true, false, false);
    }
    public void testKeeprootCollapse () {
        doTest("testKeeprootCollapse", true, true, false, false, false);
    }
    public void testKeeprootSemantic () {
        doTest("testKeeprootSemantic", true, false, true, false, false);
    }
    public void testCollapseSemantic () {
        doTest("testCollapseSemantic", false, true, true, false, false);
    }
    public void testKeeprootCollapseSemantic () {
        doTest("testKeeprootCollapseSemantic", true, true, true, false, false);
    }
    public void testInclude () {
        doTest("testInclude", false, false, false, true, false);
    }
    public void testSemanticInclude () {
        doTest("testSemanticInclude", false, false, true, true, false);
    }
    public void testSemanticLocal () {
        doTest("testSemanticInclude", false, false, true, false, true);
    }
    public void testNeedsCatalog() {
        executeTarget("testneedscat");
        assertEquals("true", getProject().getProperty("skinconfig.foo"));
    }
    private void doTest(String msg, boolean keepRoot, boolean collapse,
                        boolean semantic, boolean include, boolean localRoot) {
        Enumeration iter =
            getFiles(new File(System.getProperty("root"), "src/etc/testcases/taskdefs/xmlproperty/inputs"));
        while (iter.hasMoreElements()) {
            File inputFile = (File) iter.nextElement();
            File workingDir;
            if ( localRoot ) {
                workingDir = inputFile.getParentFile();
            } else {
                workingDir = FILE_UTILS.resolveFile(new File("."), ".");
            }
            try {
                File propertyFile = getGoldfile(inputFile, keepRoot, collapse,
                                                semantic, include, localRoot);
                if (!propertyFile.exists()) {
                    continue;
                }
                Project p = new Project();
                XmlProperty xmlproperty = new XmlProperty();
                xmlproperty.setProject(p);
                xmlproperty.setFile(inputFile);
                xmlproperty.setKeeproot(keepRoot);
                xmlproperty.setCollapseAttributes(collapse);
                xmlproperty.setSemanticAttributes(semantic);
                xmlproperty.setIncludeSemanticAttribute(include);
                xmlproperty.setRootDirectory(workingDir);
                p.setNewProperty("override.property.test", "foo");
                xmlproperty.execute();
                Properties props = new Properties();
                props.load(new FileInputStream(propertyFile));
                ensureProperties(msg, inputFile, workingDir, p, props);
                ensureReferences(msg, inputFile, p.getReferences());
            } catch (IOException ex) {
                fail(ex.toString());
            }
        }
    }
    private static void ensureProperties (String msg, File inputFile,
                                          File workingDir, Project p,
                                          Properties properties) {
        Hashtable xmlproperties = p.getProperties();
        Enumeration propertyKeyEnum = properties.propertyNames();
        while(propertyKeyEnum.hasMoreElements()){
            String currentKey = propertyKeyEnum.nextElement().toString();
            String assertMsg = msg + "-" + inputFile.getName()
                + " Key=" + currentKey;
            String propertyValue = properties.getProperty(currentKey);
            String xmlValue = (String)xmlproperties.get(currentKey);
            if ( propertyValue.indexOf("ID.") == 0 ) {
                String id = currentKey;
                Object obj = p.getReferences().get(id);
                if ( obj == null ) {
                    fail(assertMsg + " Object ID does not exist.");
                }
                propertyValue =
                    propertyValue.substring(3, propertyValue.length());
                if (propertyValue.equals("path")) {
                    if (!(obj instanceof Path)) {
                        fail(assertMsg + " Path ID is a "
                             + obj.getClass().getName());
                    }
                } else {
                    assertEquals(assertMsg, propertyValue, obj.toString());
                }
            } else {
                if (propertyValue.indexOf("FILE.") == 0) {
                    String fileName =
                        propertyValue.substring(5, propertyValue.length());
                    File f = new File(workingDir, fileName);
                    propertyValue = f.getAbsolutePath();
                }
                assertEquals(assertMsg, propertyValue, xmlValue);
            }
        }
    }
    private static void printProperties(Hashtable xmlproperties) {
        Enumeration keyEnum = xmlproperties.keys();
        while (keyEnum.hasMoreElements()) {
            String currentKey = keyEnum.nextElement().toString();
            System.out.println(currentKey + " = "
                               + xmlproperties.get(currentKey));
        }
    }
    private static void ensureReferences (String msg, File inputFile,
                                          Hashtable references) {
        Enumeration referenceKeyEnum = references.keys();
        while(referenceKeyEnum.hasMoreElements()){
            String currentKey = referenceKeyEnum.nextElement().toString();
            Object currentValue = references.get(currentKey);
            if (currentValue instanceof Path) {
            } else if (currentValue instanceof String) {
            } else {
                if( ! currentKey.startsWith("ant.") ) {
                    fail(msg + "-" + inputFile.getName() + " Key="
                         + currentKey + " is not a recognized type.");
                }
            }
        }
    }
    private static File getGoldfile (File input, boolean keepRoot,
                                     boolean collapse, boolean semantic,
                                     boolean include, boolean localRoot) {
        String baseName = input.getName().toLowerCase();
        if (baseName.endsWith(".xml")) {
            baseName = baseName.substring(0, baseName.length() - 4)
                + ".properties";
        }
        File dir = input.getParentFile().getParentFile();
        String goldFileFolder = "goldfiles/";
        if (keepRoot) {
            goldFileFolder += "keeproot-";
        } else {
            goldFileFolder += "nokeeproot-";
        }
        if (semantic) {
            goldFileFolder += "semantic-";
            if (include) {
                goldFileFolder += "include-";
            }
        } else {
            if (collapse) {
                goldFileFolder += "collapse-";
            } else {
                goldFileFolder += "nocollapse-";
            }
        }
        return new File(dir, goldFileFolder + baseName);
    }
    private static Enumeration getFiles (final File startingDir) {
        Vector result = new Vector();
        getFiles(startingDir, result);
        return result.elements();
    }
    private static void getFiles (final File startingDir, Vector collect) {
        FileFilter filter = new FileFilter() {
            public boolean accept (File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    return (file.getPath().indexOf("taskdefs") > 0 &&
                            file.getPath().toLowerCase().endsWith(".xml") );
                }
            }
        };
        File[] files = startingDir.listFiles(filter);
        for (int i=0;i<files.length;i++) {
            File f = files[i];
            if (!f.isDirectory()) {
                collect.addElement(f);
            } else {
                getFiles(f, collect);
            }
        }
    }
}
