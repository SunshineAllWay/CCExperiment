package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.XMLFragment;
import org.apache.tools.ant.util.DOMElementWriter;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
public class EchoXML extends XMLFragment {
    private File file;
    private boolean append;
    private NamespacePolicy namespacePolicy = NamespacePolicy.DEFAULT;
    private static final String ERROR_NO_XML = "No nested XML specified";
    public void setFile(File f) {
        file = f;
    }
    public void setNamespacePolicy(NamespacePolicy n) {
        namespacePolicy = n;
    }
    public void setAppend(boolean b) {
        append = b;
    }
    public void execute() {
        DOMElementWriter writer =
            new DOMElementWriter(!append, namespacePolicy.getPolicy());
        OutputStream os = null;
        try {
            if (file != null) {
                os = new FileOutputStream(file.getAbsolutePath(), append);
            } else {
                os = new LogOutputStream(this, Project.MSG_INFO);
            }
            Node n = getFragment().getFirstChild();
            if (n == null) {
                throw new BuildException(ERROR_NO_XML);
            }
            writer.write((Element) n, os);
        } catch (BuildException e) {
            throw e;
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            FileUtils.close(os);
        }
    }
    public static class NamespacePolicy extends EnumeratedAttribute {
        private static final String IGNORE = "ignore";
        private static final String ELEMENTS = "elementsOnly";
        private static final String ALL = "all";
        public static final NamespacePolicy DEFAULT
            = new NamespacePolicy(IGNORE);
        public NamespacePolicy() {}
        public NamespacePolicy(String s) {
            setValue(s);
        }
        public String[] getValues() {
            return new String[] {IGNORE, ELEMENTS, ALL};
        }
        public DOMElementWriter.XmlNamespacePolicy getPolicy() {
            String s = getValue();
            if (IGNORE.equalsIgnoreCase(s)) {
                return DOMElementWriter.XmlNamespacePolicy.IGNORE;
            } else if (ELEMENTS.equalsIgnoreCase(s)) {
                return
                    DOMElementWriter.XmlNamespacePolicy.ONLY_QUALIFY_ELEMENTS;
            } else if (ALL.equalsIgnoreCase(s)) {
                return DOMElementWriter.XmlNamespacePolicy.QUALIFY_ALL;
            } else {
                throw new BuildException("Invalid namespace policy: " + s);
            }
        }
    }
}
