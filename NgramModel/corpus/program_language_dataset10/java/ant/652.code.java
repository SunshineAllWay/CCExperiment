package org.apache.tools.ant.types.resources;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Stack;
public abstract class AbstractClasspathResource extends Resource {
    private Path classpath;
    private Reference loader;
    private boolean parentFirst = true;
    public void setClasspath(Path classpath) {
        checkAttributesAllowed();
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
        setChecked(false);
    }
    public Path createClasspath() {
        checkChildrenAllowed();
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        setChecked(false);
        return classpath.createPath();
    }
    public void setClasspathRef(Reference r) {
        checkAttributesAllowed();
        createClasspath().setRefid(r);
    }
    public Path getClasspath() {
        if (isReference()) {
            return ((AbstractClasspathResource) getCheckedRef()).getClasspath();
        }
        dieOnCircularReference();
        return classpath;
    }
    public Reference getLoader() {
        if (isReference()) {
            return ((AbstractClasspathResource) getCheckedRef()).getLoader();
        }
        dieOnCircularReference();
        return loader;
    }
    public void setLoaderRef(Reference r) {
        checkAttributesAllowed();
        loader = r;
    }
    public void setParentFirst(boolean b) {
        parentFirst = b;
    }
    public void setRefid(Reference r) {
        if (loader != null || classpath != null) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public boolean isExists() {
        if (isReference()) {
            return  ((Resource) getCheckedRef()).isExists();
        }
        dieOnCircularReference();
        InputStream is = null;
        try {
            is = getInputStream();
            return is != null;
        } catch (IOException ex) {
            return false;
        } finally {
            FileUtils.close(is);
        }
    }
    public InputStream getInputStream() throws IOException {
        if (isReference()) {
            return ((Resource) getCheckedRef()).getInputStream();
        }
        dieOnCircularReference();
        final ClassLoaderWithFlag classLoader = getClassLoader();
        return !classLoader.needsCleanup()
            ? openInputStream(classLoader.getLoader())
            : new FilterInputStream(openInputStream(classLoader.getLoader())) {
                    public void close() throws IOException {
                        FileUtils.close(in);
                        classLoader.cleanup();
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
    protected ClassLoaderWithFlag getClassLoader() {
        ClassLoader cl = null;
        boolean clNeedsCleanup = false;
        if (loader != null) {
            cl = (ClassLoader) loader.getReferencedObject();
        }
        if (cl == null) {
            if (getClasspath() != null) {
                Path p = getClasspath().concatSystemClasspath("ignore");
                if (parentFirst) {
                    cl = getProject().createClassLoader(p);
                } else {
                    cl = AntClassLoader.newAntClassLoader(getProject()
                                                          .getCoreLoader(),
                                                          getProject(),
                                                          p, false);
                }
                clNeedsCleanup = loader == null;
            } else {
                cl = JavaResource.class.getClassLoader();
            }
            if (loader != null && cl != null) {
                getProject().addReference(loader.getRefId(), cl);
            }
        }
        return new ClassLoaderWithFlag(cl, clNeedsCleanup);
    }
    protected abstract InputStream openInputStream(ClassLoader cl) throws IOException;
    protected synchronized void dieOnCircularReference(Stack stk, Project p) {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            if (classpath != null) {
                pushAndInvokeCircularReferenceCheck(classpath, stk, p);
            }
            setChecked(true);
        }
    }
    public static class ClassLoaderWithFlag {
        private final ClassLoader loader;
        private final boolean cleanup;
        ClassLoaderWithFlag(ClassLoader l, boolean needsCleanup) {
            loader = l;
            cleanup = needsCleanup && l instanceof AntClassLoader;
        }
        public ClassLoader getLoader() { return loader; }
        public boolean needsCleanup() { return cleanup; }
        public void cleanup() {
            if (cleanup) {
                ((AntClassLoader) loader).cleanup();
            }
        }
    }
}
