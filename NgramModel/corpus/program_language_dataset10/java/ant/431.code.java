package org.apache.tools.ant.taskdefs.optional.extension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
public class ExtensionSet
    extends DataType {
    private final ArrayList extensions = new ArrayList();
    private final ArrayList extensionsFilesets = new ArrayList();
    public void addExtension(final ExtensionAdapter extensionAdapter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        extensions.add(extensionAdapter);
    }
    public void addLibfileset(final LibFileSet fileSet) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        extensionsFilesets.add(fileSet);
    }
    public void addFileset(final FileSet fileSet) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        extensionsFilesets.add(fileSet);
    }
    public Extension[] toExtensions(final Project proj)
        throws BuildException {
        if (isReference()) {
            return ((ExtensionSet) getCheckedRef()).toExtensions(proj);
        }
        dieOnCircularReference();
        final ArrayList extensionsList = ExtensionUtil.toExtensions(extensions);
        ExtensionUtil.extractExtensions(proj, extensionsList, extensionsFilesets);
        return (Extension[]) extensionsList.toArray(new Extension[extensionsList.size()]);
    }
    public void setRefid(final Reference reference)
        throws BuildException {
        if (!extensions.isEmpty() || !extensionsFilesets.isEmpty()) {
            throw tooManyAttributes();
        }
        super.setRefid(reference);
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            for (Iterator i = extensions.iterator(); i.hasNext(); ) {
                pushAndInvokeCircularReferenceCheck((ExtensionAdapter) i.next(),
                                                    stk, p);
            }
            for (Iterator i = extensionsFilesets.iterator(); i.hasNext(); ) {
                pushAndInvokeCircularReferenceCheck((FileSet) i.next(), stk, p);
            }
            setChecked(true);
        }
    }
    public String toString() {
        return "ExtensionSet" + Arrays.asList(toExtensions(getProject()));
    }
}
