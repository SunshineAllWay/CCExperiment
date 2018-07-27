package org.apache.tools.ant.types.resources;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.tools.ant.types.Resource;
public class LazyResourceCollectionWrapper extends
        AbstractResourceCollectionWrapper {
    private List cachedResources = new ArrayList();
    private FilteringIterator filteringIterator;
    protected Iterator createIterator() {
        Iterator iterator;
        if (isCache()) {
            if (filteringIterator == null) {
                filteringIterator = new FilteringIterator(
                        getResourceCollection().iterator());
            }
            iterator = new CachedIterator(filteringIterator);
        } else {
            iterator = new FilteringIterator(getResourceCollection().iterator());
        }
        return iterator;
    }
    protected int getSize() {
        Iterator it = createIterator();
        int size = 0;
        while (it.hasNext()) {
            it.next();
            size++;
        }
        return size;
    }
    protected boolean filterResource(Resource r) {
        return false;
    }
    private class FilteringIterator implements Iterator {
        Resource next = null;
        boolean ended = false;
        protected final Iterator it;
        public FilteringIterator(Iterator it) {
            this.it = it;
        }
        public boolean hasNext() {
            if (ended) {
                return false;
            }
            while (next == null) {
                if (!it.hasNext()) {
                    ended = true;
                    return false;
                }
                next = (Resource) it.next();
                if (filterResource(next)) {
                    next = null;
                }
            }
            return true;
        }
        public Object next() {
            if (!hasNext()) {
                throw new UnsupportedOperationException();
            }
            Resource r = next;
            next = null;
            return r;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    private class CachedIterator implements Iterator {
        int cusrsor = 0;
        private final Iterator it;
        public CachedIterator(Iterator it) {
            this.it = it;
        }
        public boolean hasNext() {
            synchronized (cachedResources) {
                if (cachedResources.size() > cusrsor) {
                    return true;
                }
                if (!it.hasNext()) {
                    return false;
                }
                Resource r = (Resource) it.next();
                cachedResources.add(r);
            }
            return true;
        }
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            synchronized (cachedResources) {
                return cachedResources.get(cusrsor++);
            }
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
