package org.apache.tools.ant.util;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public class ScriptRunnerCreator {
    private static final String AUTO = "auto";
    private static final String OATAU = "org.apache.tools.ant.util";
    private static final String UTIL_OPT = OATAU + ".optional";
    private static final String BSF = "bsf";
    private static final String BSF_PACK = "org.apache.bsf";
    private static final String BSF_MANAGER = BSF_PACK + ".BSFManager";
    private static final String BSF_RUNNER = UTIL_OPT + ".ScriptRunner";
    private static final String JAVAX = "javax";
    private static final String JAVAX_MANAGER = "javax.script.ScriptEngineManager";
    private static final String JAVAX_RUNNER = UTIL_OPT + ".JavaxScriptRunner";
    private Project     project;
    private String      manager;
    private String      language;
    private ClassLoader scriptLoader = null;
    public ScriptRunnerCreator(Project project) {
        this.project = project;
    }
    public synchronized ScriptRunnerBase createRunner(
        String manager, String language, ClassLoader classLoader) {
        this.manager      = manager;
        this.language     = language;
        this.scriptLoader = classLoader;
        if (language == null) {
            throw new BuildException("script language must be specified");
        }
        if (!manager.equals(AUTO) && !manager.equals(JAVAX) && !manager.equals(BSF)) {
            throw new BuildException("Unsupported language prefix " + manager);
        }
        ScriptRunnerBase ret = null;
        ret = createRunner(BSF, BSF_MANAGER, BSF_RUNNER);
        if (ret == null) {
            ret = createRunner(JAVAX, JAVAX_MANAGER, JAVAX_RUNNER);
        }
        if (ret != null) {
            return ret;
        }
        if (JAVAX.equals(manager)) {
            throw new BuildException(
                    "Unable to load the script engine manager " + "(" + JAVAX_MANAGER + ")");
        }
        if (BSF.equals(manager)) {
            throw new BuildException(
                    "Unable to load the BSF script engine manager " + "(" + BSF_MANAGER + ")");
        }
        throw new BuildException("Unable to load a script engine manager "
                + "(" + BSF_MANAGER + " or " + JAVAX_MANAGER + ")");
    }
    private ScriptRunnerBase createRunner(
        String checkManager, String managerClass, String runnerClass) {
        ScriptRunnerBase runner = null;
        if (!manager.equals(AUTO) && !manager.equals(checkManager)) {
            return null;
        }
        if (scriptLoader.getResource(LoaderUtils.classNameToResource(managerClass)) == null) {
            return null;
        }
        if (managerClass.equals(BSF_MANAGER)) {
            new ScriptFixBSFPath().fixClassLoader(scriptLoader, language);
        }
        try {
            runner = (ScriptRunnerBase) Class.forName(
                    runnerClass, true, scriptLoader).newInstance();
            runner.setProject(project);
        } catch (Exception ex) {
            throw ReflectUtil.toBuildException(ex);
        }
        runner.setLanguage(language);
        runner.setScriptClassLoader(scriptLoader);
        return runner;
    }
}
