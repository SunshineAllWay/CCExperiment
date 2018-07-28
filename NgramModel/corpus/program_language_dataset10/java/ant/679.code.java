package org.apache.tools.ant.types.resources;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.ant.util.MergingMapper;
public class MappedResourceCollection
        extends DataType implements ResourceCollection, Cloneable {
    private ResourceCollection nested = null;
    private Mapper mapper = null;
    private boolean enableMultipleMappings = false;
    private boolean cache = false;
    private Collection cachedColl = null;
    public synchronized void add(ResourceCollection c) throws BuildException {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (nested != null) {
            throw new BuildException("Only one resource collection can be"
                                     + " nested into mappedresources",
                                     getLocation());
        }
        setChecked(false);
        cachedColl = null;
        nested = c;
    }
    public Mapper createMapper() throws BuildException {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (mapper != null) {
            throw new BuildException("Cannot define more than one mapper",
                                     getLocation());
        }
        setChecked(false);
        mapper = new Mapper(getProject());
        cachedColl = null;
        return mapper;
    }
    public void add(FileNameMapper fileNameMapper) {
        createMapper().add(fileNameMapper);
    }
    public void setEnableMultipleMappings(boolean enableMultipleMappings) {
        this.enableMultipleMappings = enableMultipleMappings;
    }
    public void setCache(boolean cache) {
        this.cache = cache;
    }
    public boolean isFilesystemOnly() {
        if (isReference()) {
            return ((MappedResourceCollection) getCheckedRef())
                .isFilesystemOnly();
        }
        checkInitialized();
        return false;
    }
    public int size() {
        if (isReference()) {
            return ((MappedResourceCollection) getCheckedRef()).size();
        }
        checkInitialized();
        return cacheCollection().size();
    }
    public Iterator iterator() {
        if (isReference()) {
            return ((MappedResourceCollection) getCheckedRef()).iterator();
        }
        checkInitialized();
        return cacheCollection().iterator();
    }
    public void setRefid(Reference r) {
        if (nested != null || mapper != null) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public Object clone() {
        try {
            MappedResourceCollection c =
                (MappedResourceCollection) super.clone();
            c.nested = nested;
            c.mapper = mapper;
            c.cachedColl = null;
            return c;
        } catch (CloneNotSupportedException e) {
            throw new BuildException(e);
        }
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            checkInitialized();
            if (mapper != null) {
                pushAndInvokeCircularReferenceCheck(mapper, stk, p);
            }
            if (nested instanceof DataType) {
                pushAndInvokeCircularReferenceCheck((DataType) nested, stk, p);
            }
            setChecked(true);
        }
    }
    private void checkInitialized() {
        if (nested == null) {
            throw new BuildException("A nested resource collection element is"
                                     + " required", getLocation());
        }
        dieOnCircularReference();
    }
    private synchronized Collection cacheCollection() {
        if (cachedColl == null || !cache) {
            cachedColl = getCollection();
        }
        return cachedColl;
    }
    private Collection getCollection() {
        Collection collected = new ArrayList();
        FileNameMapper m =
            mapper != null ? mapper.getImplementation() : new IdentityMapper();
        for (Iterator iter = nested.iterator(); iter.hasNext(); ) {
            Resource r = (Resource) iter.next();
            if (enableMultipleMappings) {
                String[] n = m.mapFileName(r.getName());
                if (n != null) {
                    for (int i = 0; i < n.length; i++) {
                        collected.add(new MappedResource(r,
                                                         new MergingMapper(n[i]))
                                      );
                    }
                }
            } else {
                collected.add(new MappedResource(r, m));
            }
        }
        return collected;
    }
}
