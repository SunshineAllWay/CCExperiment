package org.apache.tools.ant.types.resources;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.util.FileUtils;
public class ResourceList extends DataType implements ResourceCollection {
    private final Vector filterChains = new Vector();
    private final ArrayList textDocuments = new ArrayList();
    private final Union cachedResources = new Union();
    private volatile boolean cached = false;
    private String encoding = null;
    public ResourceList() {
        cachedResources.setCache(true);
    }
    public void add(ResourceCollection rc) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        textDocuments.add(rc);
        setChecked(false);
    }
    public final void addFilterChain(FilterChain filter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        filterChains.add(filter);
        setChecked(false);
    }
    public final void setEncoding(String encoding) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        this.encoding = encoding;
    }
    public void setRefid(Reference r) throws BuildException {
        if (encoding != null) {
            throw tooManyAttributes();
        }
        if (filterChains.size() > 0 || textDocuments.size() > 0) {
            throw noChildrenAllowed();
        }
        super.setRefid(r);
    }
    public final synchronized Iterator iterator() {
        if (isReference()) {
            return ((ResourceList) getCheckedRef()).iterator();
        }
        return cache().iterator();
    }
    public synchronized int size() {
        if (isReference()) {
            return ((ResourceList) getCheckedRef()).size();
        }
        return cache().size();
    }
    public synchronized boolean isFilesystemOnly() {
        if (isReference()) {
            return ((ResourceList) getCheckedRef()).isFilesystemOnly();
        }
        return cache().isFilesystemOnly();
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            for (Iterator iter = textDocuments.iterator(); iter.hasNext(); ) {
                Object o = (Object) iter.next();
                if (o instanceof DataType) {
                    pushAndInvokeCircularReferenceCheck((DataType) o, stk, p);
                }
            }
            for (Iterator iter = filterChains.iterator(); iter.hasNext(); ) {
                FilterChain fc = (FilterChain) iter.next();
                pushAndInvokeCircularReferenceCheck(fc, stk, p);
            }
            setChecked(true);
        }
    }
    private synchronized ResourceCollection cache() {
        if (!cached) {
            dieOnCircularReference();
            for (Iterator iter = textDocuments.iterator(); iter.hasNext(); ) {
                ResourceCollection rc = (ResourceCollection) iter.next();
                for (Iterator r = rc.iterator(); r.hasNext(); ) {
                    cachedResources.add(read((Resource) r.next()));
                }
            }
            cached = true;
        }
        return cachedResources;
    }
    private ResourceCollection read(Resource r) {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(r.getInputStream());
            Reader input = null;
            if (encoding == null) {
                input = new InputStreamReader(bis);
            } else {
                input = new InputStreamReader(bis, encoding);
            }
            ChainReaderHelper crh = new ChainReaderHelper();
            crh.setPrimaryReader(input);
            crh.setFilterChains(filterChains);
            crh.setProject(getProject());
            BufferedReader reader = new BufferedReader(crh.getAssembledReader());
            Union streamResources = new Union();
            streamResources.setCache(true);
            String line = null;
            while ((line = reader.readLine()) != null) {
                streamResources.add(parse(line));
            }
            return streamResources;
        } catch (final IOException ioe) {
            throw new BuildException("Unable to read resource " + r.getName()
                                     + ": " + ioe, ioe, getLocation());
        } finally {
            FileUtils.close(bis);
        }
    }
    private Resource parse(final String line) {
        PropertyHelper propertyHelper
            = (PropertyHelper) PropertyHelper.getPropertyHelper(getProject());
        Object expanded = propertyHelper.parseProperties(line);
        if (expanded instanceof Resource) {
            return (Resource) expanded;
        }
        String expandedLine = expanded.toString();
        int colon = expandedLine.indexOf(":");
        if (colon != -1) {
            try {
                return new URLResource(expandedLine);
            } catch (BuildException mfe) {
            }
        }
        return new FileResource(getProject(), expandedLine);
    }
}
