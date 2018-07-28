package org.apache.tools.ant.types.resources;
import java.util.Collection;
import java.util.Iterator;
public abstract class BaseResourceCollectionWrapper
    extends AbstractResourceCollectionWrapper {
    private Collection coll = null;
    protected Iterator createIterator() {
        return cacheCollection().iterator();
    }
	protected int getSize() {
        return cacheCollection().size();
    }
    protected abstract Collection getCollection();
    private synchronized Collection cacheCollection() {
        if (coll == null || !isCache()) {
            coll = getCollection();
        }
        return coll;
    }
}
