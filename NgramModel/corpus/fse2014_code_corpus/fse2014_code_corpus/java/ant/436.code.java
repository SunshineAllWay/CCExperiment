package org.apache.tools.ant.taskdefs.optional.extension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
public final class JarLibManifestTask extends Task {
    private static final String MANIFEST_VERSION = "1.0";
    private static final String CREATED_BY = "Created-By";
    private File destFile;
    private Extension extension;
    private final ArrayList dependencies = new ArrayList();
    private final ArrayList optionals = new ArrayList();
    private final ArrayList extraAttributes = new ArrayList();
    public void setDestfile(final File destFile) {
        this.destFile = destFile;
    }
    public void addConfiguredExtension(final ExtensionAdapter extensionAdapter)
            throws BuildException {
        if (null != extension) {
            throw new BuildException("Can not have multiple extensions defined in one library.");
        }
        extension = extensionAdapter.toExtension();
    }
    public void addConfiguredDepends(final ExtensionSet extensionSet) {
        dependencies.add(extensionSet);
    }
    public void addConfiguredOptions(final ExtensionSet extensionSet) {
        optionals.add(extensionSet);
    }
    public void addConfiguredAttribute(final ExtraAttribute attribute) {
        extraAttributes.add(attribute);
    }
    public void execute() throws BuildException {
        validate();
        final Manifest manifest = new Manifest();
        final Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION);
        attributes.putValue(CREATED_BY, "Apache Ant "
                + getProject().getProperty(MagicNames.ANT_VERSION));
        appendExtraAttributes(attributes);
        if (null != extension) {
            Extension.addExtension(extension, attributes);
        }
        final ArrayList depends = toExtensions(dependencies);
        appendExtensionList(attributes, Extension.EXTENSION_LIST, "lib", depends.size());
        appendLibraryList(attributes, "lib", depends);
        final ArrayList option = toExtensions(optionals);
        appendExtensionList(attributes, Extension.OPTIONAL_EXTENSION_LIST, "opt", option.size());
        appendLibraryList(attributes, "opt", option);
        try {
            log("Generating manifest " + destFile.getAbsoluteFile(), Project.MSG_INFO);
            writeManifest(manifest);
        } catch (final IOException ioe) {
            throw new BuildException(ioe.getMessage(), ioe);
        }
    }
    private void validate() throws BuildException {
        if (null == destFile) {
            throw new BuildException("Destfile attribute not specified.");
        }
        if (destFile.exists() && !destFile.isFile()) {
            throw new BuildException(destFile + " is not a file.");
        }
    }
    private void appendExtraAttributes(final Attributes attributes) {
        final Iterator iterator = extraAttributes.iterator();
        while (iterator.hasNext()) {
            final ExtraAttribute attribute =
                (ExtraAttribute) iterator.next();
            attributes.putValue(attribute.getName(),
                                 attribute.getValue());
        }
    }
    private void writeManifest(final Manifest manifest) throws IOException {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(destFile);
            manifest.write(output);
            output.flush();
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
        }
    }
    private void appendLibraryList(final Attributes attributes, final String listPrefix,
            final ArrayList extensions) throws BuildException {
        final int size = extensions.size();
        for (int i = 0; i < size; i++) {
            final Extension ext = (Extension) extensions.get(i);
            final String prefix = listPrefix + i + "-";
            Extension.addExtension(ext, prefix, attributes);
        }
    }
    private void appendExtensionList(final Attributes attributes,
            final Attributes.Name extensionKey, final String listPrefix, final int size) {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; i++) {
            sb.append(listPrefix);
            sb.append(i);
            sb.append(' ');
        }
        attributes.put(extensionKey, sb.toString());
    }
    private ArrayList toExtensions(final ArrayList extensionSets) throws BuildException {
        final ArrayList results = new ArrayList();
        final int size = extensionSets.size();
        for (int i = 0; i < size; i++) {
            final ExtensionSet set = (ExtensionSet) extensionSets.get(i);
            final Extension[] extensions = set.toExtensions(getProject());
            for (int j = 0; j < extensions.length; j++) {
                results.add(extensions[ j ]);
            }
        }
        return results;
    }
}
