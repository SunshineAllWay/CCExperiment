package org.apache.tools.ant.taskdefs;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.CollectionUtils;
import org.apache.tools.ant.util.FileUtils;
public class Manifest {
    public static final String ATTRIBUTE_MANIFEST_VERSION
        = "Manifest-Version";
    public static final String ATTRIBUTE_SIGNATURE_VERSION
        = "Signature-Version";
    public static final String ATTRIBUTE_NAME = "Name";
    public static final String ATTRIBUTE_FROM = "From";
    public static final String ATTRIBUTE_CLASSPATH = "Class-Path";
    public static final  String DEFAULT_MANIFEST_VERSION = "1.0";
    public static final int MAX_LINE_LENGTH = 72;
    public static final int MAX_SECTION_LENGTH = MAX_LINE_LENGTH - 2;
    public static final String EOL = "\r\n";
    public static final String ERROR_FROM_FORBIDDEN = "Manifest attributes should not start "
                        + "with \"" + ATTRIBUTE_FROM + "\" in \"";
    public static final String JAR_ENCODING = "UTF-8";
    private static final String ATTRIBUTE_MANIFEST_VERSION_LC =
        ATTRIBUTE_MANIFEST_VERSION.toLowerCase(Locale.ENGLISH);
    private static final String ATTRIBUTE_NAME_LC =
        ATTRIBUTE_NAME.toLowerCase(Locale.ENGLISH);
    private static final String ATTRIBUTE_FROM_LC =
        ATTRIBUTE_FROM.toLowerCase(Locale.ENGLISH);
    private static final String ATTRIBUTE_CLASSPATH_LC =
        ATTRIBUTE_CLASSPATH.toLowerCase(Locale.ENGLISH);
    public static class Attribute {
        private static final int MAX_NAME_VALUE_LENGTH = 68;
        private static final int MAX_NAME_LENGTH = 70;
        private String name = null;
        private Vector values = new Vector();
        private int currentIndex = 0;
        public Attribute() {
        }
        public Attribute(String line) throws ManifestException {
            parse(line);
        }
        public Attribute(String name, String value) {
            this.name = name;
            setValue(value);
        }
        public int hashCode() {
            int hashCode = 0;
            if (name != null) {
                hashCode += getKey().hashCode();
            }
            hashCode += values.hashCode();
            return hashCode;
        }
        public boolean equals(Object rhs) {
            if (rhs == null || rhs.getClass() != getClass()) {
                return false;
            }
            if (rhs == this) {
                return true;
            }
            Attribute rhsAttribute = (Attribute) rhs;
            String lhsKey = getKey();
            String rhsKey = rhsAttribute.getKey();
            if ((lhsKey == null && rhsKey != null)
                 || (lhsKey != null && !lhsKey.equals(rhsKey))) {
                return false;
            }
            return values.equals(rhsAttribute.values);
        }
        public void parse(String line) throws ManifestException {
            int index = line.indexOf(": ");
            if (index == -1) {
                throw new ManifestException("Manifest line \"" + line
                    + "\" is not valid as it does not "
                    + "contain a name and a value separated by ': ' ");
            }
            name = line.substring(0, index);
            setValue(line.substring(index + 2));
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public String getKey() {
            if (name == null) {
                return null;
            }
            return name.toLowerCase(Locale.ENGLISH);
        }
        public void setValue(String value) {
            if (currentIndex >= values.size()) {
                values.addElement(value);
                currentIndex = values.size() - 1;
            } else {
                values.setElementAt(value, currentIndex);
            }
        }
        public String getValue() {
            if (values.size() == 0) {
                return null;
            }
            String fullValue = "";
            for (Enumeration e = getValues(); e.hasMoreElements();) {
                String value = (String) e.nextElement();
                fullValue += value + " ";
            }
            return fullValue.trim();
        }
        public void addValue(String value) {
            currentIndex++;
            setValue(value);
        }
        public Enumeration getValues() {
            return values.elements();
        }
        public void addContinuation(String line) {
            String currentValue = (String) values.elementAt(currentIndex);
            setValue(currentValue + line.substring(1));
        }
        public void write(PrintWriter writer) throws IOException {
            write(writer, false);
        }
        public void write(PrintWriter writer, boolean flatten)
            throws IOException {
            if (!flatten) {
            for (Enumeration e = getValues(); e.hasMoreElements();) {
                writeValue(writer, (String) e.nextElement());
            }
            } else {
                writeValue(writer, getValue());
            }
        }
        private void writeValue(PrintWriter writer, String value)
             throws IOException {
            String line = null;
            int nameLength = name.getBytes(JAR_ENCODING).length;
            if (nameLength > MAX_NAME_VALUE_LENGTH) {
                if (nameLength > MAX_NAME_LENGTH) {
                    throw new IOException("Unable to write manifest line "
                            + name + ": " + value);
                }
                writer.print(name + ": " + EOL);
                line = " " + value;
            } else {
                line = name + ": " + value;
            }
            while (line.getBytes(JAR_ENCODING).length > MAX_SECTION_LENGTH) {
                int breakIndex = MAX_SECTION_LENGTH;
                if (breakIndex >= line.length()) {
                    breakIndex = line.length() - 1;
                }
                String section = line.substring(0, breakIndex);
                while (section.getBytes(JAR_ENCODING).length > MAX_SECTION_LENGTH
                     && breakIndex > 0) {
                    breakIndex--;
                    section = line.substring(0, breakIndex);
                }
                if (breakIndex == 0) {
                    throw new IOException("Unable to write manifest line "
                        + name + ": " + value);
                }
                writer.print(section + EOL);
                line = " " + line.substring(breakIndex);
            }
            writer.print(line + EOL);
        }
    }
    public static class Section {
        private Vector warnings = new Vector();
        private String name = null;
        private Map attributes = new LinkedHashMap();
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public String read(BufferedReader reader)
             throws ManifestException, IOException {
            Attribute attribute = null;
            while (true) {
                String line = reader.readLine();
                if (line == null || line.length() == 0) {
                    return null;
                }
                if (line.charAt(0) == ' ') {
                    if (attribute == null) {
                        if (name != null) {
                            name += line.substring(1);
                        } else {
                            throw new ManifestException("Can't start an "
                                + "attribute with a continuation line " + line);
                        }
                    } else {
                        attribute.addContinuation(line);
                    }
                } else {
                    attribute = new Attribute(line);
                    String nameReadAhead = addAttributeAndCheck(attribute);
                    attribute = getAttribute(attribute.getKey());
                    if (nameReadAhead != null) {
                        return nameReadAhead;
                    }
                }
            }
        }
        public void merge(Section section) throws ManifestException {
            merge(section, false);
        }
        public void merge(Section section, boolean mergeClassPaths)
            throws ManifestException {
            if (name == null && section.getName() != null
                || (name != null && section.getName() != null
                    && !(name.toLowerCase(Locale.ENGLISH)
                         .equals(section.getName().toLowerCase(Locale.ENGLISH))))
                ) {
                throw new ManifestException("Unable to merge sections "
                    + "with different names");
            }
            Enumeration e = section.getAttributeKeys();
            Attribute classpathAttribute = null;
            while (e.hasMoreElements()) {
                String attributeName = (String) e.nextElement();
                Attribute attribute = section.getAttribute(attributeName);
                if (attributeName.equalsIgnoreCase(ATTRIBUTE_CLASSPATH)) {
                    if (classpathAttribute == null) {
                        classpathAttribute = new Attribute();
                        classpathAttribute.setName(ATTRIBUTE_CLASSPATH);
                    }
                    Enumeration cpe = attribute.getValues();
                    while (cpe.hasMoreElements()) {
                        String value = (String) cpe.nextElement();
                        classpathAttribute.addValue(value);
                    }
                } else {
                    storeAttribute(attribute);
                }
            }
            if (classpathAttribute != null) {
                if (mergeClassPaths) {
                    Attribute currentCp = getAttribute(ATTRIBUTE_CLASSPATH);
                    if (currentCp != null) {
                        for (Enumeration attribEnum = currentCp.getValues();
                             attribEnum.hasMoreElements(); ) {
                            String value = (String) attribEnum.nextElement();
                            classpathAttribute.addValue(value);
                        }
                    }
                }
                storeAttribute(classpathAttribute);
            }
            Enumeration warnEnum = section.warnings.elements();
            while (warnEnum.hasMoreElements()) {
                warnings.addElement(warnEnum.nextElement());
            }
        }
        public void write(PrintWriter writer) throws IOException {
            write(writer, false);
        }
        public void write(PrintWriter writer, boolean flatten)
            throws IOException {
            if (name != null) {
                Attribute nameAttr = new Attribute(ATTRIBUTE_NAME, name);
                nameAttr.write(writer);
            }
            Enumeration e = getAttributeKeys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                Attribute attribute = getAttribute(key);
                attribute.write(writer, flatten);
            }
            writer.print(EOL);
        }
        public Attribute getAttribute(String attributeName) {
            return (Attribute) attributes.get(attributeName.toLowerCase(Locale.ENGLISH));
        }
        public Enumeration getAttributeKeys() {
            return CollectionUtils.asEnumeration(attributes.keySet().iterator());
        }
        public String getAttributeValue(String attributeName) {
            Attribute attribute = getAttribute(attributeName.toLowerCase(Locale.ENGLISH));
            if (attribute == null) {
                return null;
            }
            return attribute.getValue();
        }
        public void removeAttribute(String attributeName) {
            String key = attributeName.toLowerCase(Locale.ENGLISH);
            attributes.remove(key);
        }
        public void addConfiguredAttribute(Attribute attribute)
             throws ManifestException {
            String check = addAttributeAndCheck(attribute);
            if (check != null) {
                throw new BuildException("Specify the section name using "
                    + "the \"name\" attribute of the <section> element rather "
                    + "than using a \"Name\" manifest attribute");
            }
        }
        public String addAttributeAndCheck(Attribute attribute)
             throws ManifestException {
            if (attribute.getName() == null || attribute.getValue() == null) {
                throw new BuildException("Attributes must have name and value");
            }
            String attributeKey = attribute.getKey();
            if (attributeKey.equals(ATTRIBUTE_NAME_LC)) {
                warnings.addElement("\"" + ATTRIBUTE_NAME + "\" attributes "
                    + "should not occur in the main section and must be the "
                    + "first element in all other sections: \""
                    + attribute.getName() + ": " + attribute.getValue() + "\"");
                return attribute.getValue();
            }
            if (attributeKey.startsWith(ATTRIBUTE_FROM_LC)) {
                warnings.addElement(ERROR_FROM_FORBIDDEN
                    + attribute.getName() + ": " + attribute.getValue() + "\"");
            } else {
                if (attributeKey.equals(ATTRIBUTE_CLASSPATH_LC)) {
                    Attribute classpathAttribute =
                        (Attribute) attributes.get(attributeKey);
                    if (classpathAttribute == null) {
                        storeAttribute(attribute);
                    } else {
                        warnings.addElement("Multiple Class-Path attributes "
                            + "are supported but violate the Jar "
                            + "specification and may not be correctly "
                            + "processed in all environments");
                        Enumeration e = attribute.getValues();
                        while (e.hasMoreElements()) {
                            String value = (String) e.nextElement();
                            classpathAttribute.addValue(value);
                        }
                    }
                } else if (attributes.containsKey(attributeKey)) {
                    throw new ManifestException("The attribute \""
                        + attribute.getName() + "\" may not occur more "
                        + "than once in the same section");
                } else {
                    storeAttribute(attribute);
                }
            }
            return null;
        }
        public Object clone() {
            Section cloned = new Section();
            cloned.setName(name);
            Enumeration e = getAttributeKeys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                Attribute attribute = getAttribute(key);
                cloned.storeAttribute(new Attribute(attribute.getName(),
                                                    attribute.getValue()));
            }
            return cloned;
        }
        private void storeAttribute(Attribute attribute) {
            if (attribute == null) {
                return;
            }
            String attributeKey = attribute.getKey();
            attributes.put(attributeKey, attribute);
        }
        public Enumeration getWarnings() {
            return warnings.elements();
        }
        public int hashCode() {
            return attributes.hashCode();
        }
        public boolean equals(Object rhs) {
            if (rhs == null || rhs.getClass() != getClass()) {
                return false;
            }
            if (rhs == this) {
                return true;
            }
            Section rhsSection = (Section) rhs;
            return attributes.equals(rhsSection.attributes);
        }
    }
    private String manifestVersion = DEFAULT_MANIFEST_VERSION;
    private Section mainSection = new Section();
    private Map sections = new LinkedHashMap();
    public static Manifest getDefaultManifest() throws BuildException {
        InputStream in = null;
        InputStreamReader insr = null;
        try {
            String defManifest = "/org/apache/tools/ant/defaultManifest.mf";
            in = Manifest.class.getResourceAsStream(defManifest);
            if (in == null) {
                throw new BuildException("Could not find default manifest: "
                    + defManifest);
            }
            try {
                insr = new InputStreamReader(in, "UTF-8");
                Manifest defaultManifest = new Manifest(insr);
                String version = System.getProperty("java.runtime.version");
                if (version == null) {
                    version = System.getProperty("java.vm.version");
                }
                Attribute createdBy = new Attribute("Created-By",
                    version + " ("
                    + System.getProperty("java.vm.vendor") + ")");
                defaultManifest.getMainSection().storeAttribute(createdBy);
                return defaultManifest;
            } catch (UnsupportedEncodingException e) {
                insr = new InputStreamReader(in);
                return new Manifest(insr);
            }
        } catch (ManifestException e) {
            throw new BuildException("Default manifest is invalid !!", e);
        } catch (IOException e) {
            throw new BuildException("Unable to read default manifest", e);
        } finally {
            FileUtils.close(insr);
            FileUtils.close(in);
        }
    }
    public Manifest() {
        manifestVersion = null;
    }
    public Manifest(Reader r) throws ManifestException, IOException {
        BufferedReader reader = new BufferedReader(r);
        String nextSectionName = mainSection.read(reader);
        String readManifestVersion
            = mainSection.getAttributeValue(ATTRIBUTE_MANIFEST_VERSION);
        if (readManifestVersion != null) {
            manifestVersion = readManifestVersion;
            mainSection.removeAttribute(ATTRIBUTE_MANIFEST_VERSION);
        }
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0) {
                continue;
            }
            Section section = new Section();
            if (nextSectionName == null) {
                Attribute sectionName = new Attribute(line);
                if (!sectionName.getName().equalsIgnoreCase(ATTRIBUTE_NAME)) {
                    throw new ManifestException("Manifest sections should "
                        + "start with a \"" + ATTRIBUTE_NAME
                        + "\" attribute and not \""
                        + sectionName.getName() + "\"");
                }
                nextSectionName = sectionName.getValue();
            } else {
                Attribute firstAttribute = new Attribute(line);
                section.addAttributeAndCheck(firstAttribute);
            }
            section.setName(nextSectionName);
            nextSectionName = section.read(reader);
            addConfiguredSection(section);
        }
    }
    public void addConfiguredSection(Section section)
         throws ManifestException {
        String sectionName = section.getName();
        if (sectionName == null) {
            throw new BuildException("Sections must have a name");
        }
        sections.put(sectionName, section);
    }
    public void addConfiguredAttribute(Attribute attribute)
         throws ManifestException {
        if (attribute.getKey() == null || attribute.getValue() == null) {
            throw new BuildException("Attributes must have name and value");
        }
        if (attribute.getKey().equals(ATTRIBUTE_MANIFEST_VERSION_LC)) {
            manifestVersion = attribute.getValue();
        } else {
            mainSection.addConfiguredAttribute(attribute);
        }
    }
    public void merge(Manifest other) throws ManifestException {
        merge(other, false);
    }
    public void merge(Manifest other, boolean overwriteMain)
         throws ManifestException {
        merge(other, overwriteMain, false);
    }
    public void merge(Manifest other, boolean overwriteMain,
                      boolean mergeClassPaths)
         throws ManifestException {
        if (other != null) {
             if (overwriteMain) {
                 mainSection = (Section) other.mainSection.clone();
             } else {
                 mainSection.merge(other.mainSection, mergeClassPaths);
             }
             if (other.manifestVersion != null) {
                 manifestVersion = other.manifestVersion;
             }
             Enumeration e = other.getSectionNames();
             while (e.hasMoreElements()) {
                 String sectionName = (String) e.nextElement();
                 Section ourSection = (Section) sections.get(sectionName);
                 Section otherSection
                    = (Section) other.sections.get(sectionName);
                 if (ourSection == null) {
                     if (otherSection != null) {
                         addConfiguredSection((Section) otherSection.clone());
                     }
                 } else {
                     ourSection.merge(otherSection, mergeClassPaths);
                 }
             }
         }
    }
    public void write(PrintWriter writer) throws IOException {
        write(writer, false);
    }
    public void write(PrintWriter writer, boolean flatten) throws IOException {
        writer.print(ATTRIBUTE_MANIFEST_VERSION + ": " + manifestVersion + EOL);
        String signatureVersion
            = mainSection.getAttributeValue(ATTRIBUTE_SIGNATURE_VERSION);
        if (signatureVersion != null) {
            writer.print(ATTRIBUTE_SIGNATURE_VERSION + ": "
                + signatureVersion + EOL);
            mainSection.removeAttribute(ATTRIBUTE_SIGNATURE_VERSION);
        }
        mainSection.write(writer, flatten);
        if (signatureVersion != null) {
            try {
                Attribute svAttr = new Attribute(ATTRIBUTE_SIGNATURE_VERSION,
                    signatureVersion);
                mainSection.addConfiguredAttribute(svAttr);
            } catch (ManifestException e) {
            }
        }
        Iterator e = sections.keySet().iterator();
        while (e.hasNext()) {
            String sectionName = (String) e.next();
            Section section = getSection(sectionName);
            section.write(writer, flatten);
        }
    }
    public String toString() {
        StringWriter sw = new StringWriter();
        try {
            write(new PrintWriter(sw));
        } catch (IOException e) {
            return null;
        }
        return sw.toString();
    }
    public Enumeration getWarnings() {
        Vector warnings = new Vector();
        Enumeration warnEnum = mainSection.getWarnings();
        while (warnEnum.hasMoreElements()) {
            warnings.addElement(warnEnum.nextElement());
        }
        Iterator e = sections.values().iterator();
        while (e.hasNext()) {
            Section section = (Section) e.next();
            Enumeration e2 = section.getWarnings();
            while (e2.hasMoreElements()) {
                warnings.addElement(e2.nextElement());
            }
        }
        return warnings.elements();
    }
    public int hashCode() {
        int hashCode = 0;
        if (manifestVersion != null) {
            hashCode += manifestVersion.hashCode();
        }
        hashCode += mainSection.hashCode();
        hashCode += sections.hashCode();
        return hashCode;
    }
    public boolean equals(Object rhs) {
        if (rhs == null || rhs.getClass() != getClass()) {
            return false;
        }
        if (rhs == this) {
            return true;
        }
        Manifest rhsManifest = (Manifest) rhs;
        if (manifestVersion == null) {
            if (rhsManifest.manifestVersion != null) {
                return false;
            }
        } else if (!manifestVersion.equals(rhsManifest.manifestVersion)) {
            return false;
        }
        if (!mainSection.equals(rhsManifest.mainSection)) {
            return false;
        }
        return sections.equals(rhsManifest.sections);
    }
    public String getManifestVersion() {
        return manifestVersion;
    }
    public Section getMainSection() {
        return mainSection;
    }
    public Section getSection(String name) {
        return (Section) sections.get(name);
    }
    public Enumeration getSectionNames() {
        return CollectionUtils.asEnumeration(sections.keySet().iterator());
    }
}
