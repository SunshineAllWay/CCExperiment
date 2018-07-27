package org.apache.tools.ant.filters.util;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.BaseFilterReader;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.types.AntFilterReader;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Parameterizable;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
public final class ChainReaderHelper {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    public Reader primaryReader;
    public int bufferSize = DEFAULT_BUFFER_SIZE;
    public Vector filterChains = new Vector();
    private Project project = null;
    public void setPrimaryReader(Reader rdr) {
        primaryReader = rdr;
    }
    public void setProject(final Project project) {
        this.project = project;
    }
    public Project getProject() {
        return project;
    }
    public void setBufferSize(int size) {
        bufferSize = size;
    }
    public void setFilterChains(Vector fchain) {
        filterChains = fchain;
    }
    public Reader getAssembledReader() throws BuildException {
        if (primaryReader == null) {
            throw new BuildException("primaryReader must not be null.");
        }
        Reader instream = primaryReader;
        final int filterReadersCount = filterChains.size();
        final Vector finalFilters = new Vector();
        final ArrayList classLoadersToCleanUp =
            new ArrayList();
        for (int i = 0; i < filterReadersCount; i++) {
            final FilterChain filterchain =
                (FilterChain) filterChains.elementAt(i);
            final Vector filterReaders = filterchain.getFilterReaders();
            final int readerCount = filterReaders.size();
            for (int j = 0; j < readerCount; j++) {
                finalFilters.addElement(filterReaders.elementAt(j));
            }
        }
        final int filtersCount = finalFilters.size();
        if (filtersCount > 0) {
            boolean success = false;
            try {
                for (int i = 0; i < filtersCount; i++) {
                    Object o = finalFilters.elementAt(i);
                    if (o instanceof AntFilterReader) {
                        instream =
                            expandReader((AntFilterReader) finalFilters.elementAt(i),
                                         instream, classLoadersToCleanUp);
                    } else if (o instanceof ChainableReader) {
                        setProjectOnObject(o);
                        instream = ((ChainableReader) o).chain(instream);
                        setProjectOnObject(instream);
                    }
                }
                success = true;
            } finally {
                if (!success && classLoadersToCleanUp.size() > 0) {
                    cleanUpClassLoaders(classLoadersToCleanUp);
                }
            }
        }
        final Reader finalReader = instream;
        return classLoadersToCleanUp.size() == 0 ? finalReader
            : new FilterReader(finalReader) {
                    public void close() throws IOException {
                        FileUtils.close(in);
                        cleanUpClassLoaders(classLoadersToCleanUp);
                    }
                    protected void finalize() throws Throwable {
                        try {
                            close();
                        } finally {
                            super.finalize();
                        }
                    }
                };
    }
    private void setProjectOnObject(Object obj) {
        if (project == null) {
            return;
        }
        if (obj instanceof BaseFilterReader) {
            ((BaseFilterReader) obj).setProject(project);
            return;
        }
        project.setProjectReference(obj);
    }
    private static void cleanUpClassLoaders(List loaders) {
        for (Iterator it = loaders.iterator(); it.hasNext(); ) {
            ((AntClassLoader) it.next()).cleanup();
        }
    }
    public String readFully(Reader rdr)
        throws IOException {
        return FileUtils.readFully(rdr, bufferSize);
    }
    private Reader expandReader(final AntFilterReader filter,
                                final Reader ancestor,
                                final List classLoadersToCleanUp) {
        final String className = filter.getClassName();
        final Path classpath = filter.getClasspath();
        final Project pro = filter.getProject();
        if (className != null) {
            try {
                Class clazz = null;
                if (classpath == null) {
                    clazz = Class.forName(className);
                } else {
                    AntClassLoader al = pro.createClassLoader(classpath);
                    classLoadersToCleanUp.add(al);
                    clazz = Class.forName(className, true, al);
                }
                if (clazz != null) {
                    if (!FilterReader.class.isAssignableFrom(clazz)) {
                        throw new BuildException(className + " does not extend"
                                                 + " java.io.FilterReader");
                    }
                    final Constructor[] constructors = clazz.getConstructors();
                    int j = 0;
                    boolean consPresent = false;
                    for (; j < constructors.length; j++) {
                        Class[] types = constructors[j].getParameterTypes();
                        if (types.length == 1
                            && types[0].isAssignableFrom(Reader.class)) {
                            consPresent = true;
                            break;
                        }
                    }
                    if (!consPresent) {
                        throw new BuildException(className + " does not define"
                                                 + " a public constructor"
                                                 + " that takes in a Reader"
                                                 + " as its single argument.");
                    }
                    final Reader[] rdr = {ancestor};
                    Reader instream =
                        (Reader) constructors[j].newInstance((Object[]) rdr);
                    setProjectOnObject(instream);
                    if (Parameterizable.class.isAssignableFrom(clazz)) {
                        final Parameter[] params = filter.getParams();
                        ((Parameterizable) instream).setParameters(params);
                    }
                    return instream;
                }
            } catch (final ClassNotFoundException cnfe) {
                throw new BuildException(cnfe);
            } catch (final InstantiationException ie) {
                throw new BuildException(ie);
            } catch (final IllegalAccessException iae) {
                throw new BuildException(iae);
            } catch (final InvocationTargetException ite) {
                throw new BuildException(ite);
            }
        }
        return ancestor;
    }
}
