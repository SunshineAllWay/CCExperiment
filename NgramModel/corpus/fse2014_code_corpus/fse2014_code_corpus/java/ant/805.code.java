package org.apache.tools.ant.util;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
public class ScriptFixBSFPath {
    private static final String UTIL_OPTIONAL_PACKAGE
        = "org.apache.tools.ant.util.optional";
    private static final String BSF_PACKAGE = "org.apache.bsf";
    private static final String BSF_MANAGER = BSF_PACKAGE + ".BSFManager";
    private static final String BSF_SCRIPT_RUNNER
        = UTIL_OPTIONAL_PACKAGE + ".ScriptRunner";
    private static final String[] BSF_LANGUAGES =
        new String[] {
            "js",         "org.mozilla.javascript.Scriptable",
            "javascript", "org.mozilla.javascript.Scriptable",
            "jacl",       "tcl.lang.Interp",
            "netrexx",    "netrexx.lang.Rexx",
            "nrx",        "netrexx.lang.Rexx",
            "jython",     "org.python.core.Py",
            "py",         "org.python.core.Py",
            "xslt",       "org.apache.xpath.objects.XObject"};
    private static final Map BSF_LANGUAGE_MAP = new HashMap();
    static {
        for (int i = 0; i < BSF_LANGUAGES.length; i = i + 2) {
            BSF_LANGUAGE_MAP.put(BSF_LANGUAGES[i], BSF_LANGUAGES[i + 1]);
        }
    }
    private File getClassSource(ClassLoader loader, String className) {
        return LoaderUtils.getResourceSource(
            loader,
            LoaderUtils.classNameToResource(className));
    }
    private File getClassSource(String className) {
        return getClassSource(getClass().getClassLoader(), className);
    }
    public void fixClassLoader(ClassLoader loader, String language) {
        if (loader == getClass().getClassLoader()
            || !(loader instanceof AntClassLoader)) {
            return;
        }
        ClassLoader myLoader = getClass().getClassLoader();
        AntClassLoader fixLoader = (AntClassLoader) loader;
        File bsfSource = getClassSource(BSF_MANAGER);
        boolean needMoveRunner = (bsfSource == null);
        String languageClassName = (String) BSF_LANGUAGE_MAP.get(language);
        boolean needMoveBsf =
            bsfSource != null
            && languageClassName != null
            && !LoaderUtils.classExists(myLoader, languageClassName)
            && LoaderUtils.classExists(loader, languageClassName);
        needMoveRunner = needMoveRunner || needMoveBsf;
        if (bsfSource == null) {
            bsfSource = getClassSource(loader, BSF_MANAGER);
        }
        if (bsfSource == null) {
            throw new BuildException(
                "Unable to find BSF classes for scripting");
        }
        if (needMoveBsf) {
            fixLoader.addPathComponent(bsfSource);
            fixLoader.addLoaderPackageRoot(BSF_PACKAGE);
        }
        if (needMoveRunner) {
            fixLoader.addPathComponent(
                LoaderUtils.getResourceSource(
                    fixLoader,
                    LoaderUtils.classNameToResource(BSF_SCRIPT_RUNNER)));
            fixLoader.addLoaderPackageRoot(UTIL_OPTIONAL_PACKAGE);
        }
    }
}
