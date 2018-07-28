package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelperRepository;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.URLResource;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;
public class ImportTask extends Task {
    private String file;
    private boolean optional;
    private String targetPrefix;
    private String prefixSeparator = ".";
    private final Union resources = new Union();
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public ImportTask() {
        resources.setCache(true);
    }
    public void setOptional(boolean optional) {
        this.optional = optional;
    }
    public void setFile(String file) {
        this.file = file;
    }
    public void setAs(String prefix) {
        targetPrefix = prefix;
    }
    public void setPrefixSeparator(String s) {
        prefixSeparator = s;
    }
    public void add(ResourceCollection r) {
        resources.add(r);
    }
    public void execute() {
        if (file == null && resources.size() == 0) {
            throw new BuildException("import requires file attribute or"
                                     + " at least one nested resource");
        }
        if (getOwningTarget() == null
            || !"".equals(getOwningTarget().getName())) {
            throw new BuildException("import only allowed as a top-level task");
        }
        ProjectHelper helper =
                (ProjectHelper) getProject().
                    getReference(ProjectHelper.PROJECTHELPER_REFERENCE);
        if (helper == null) {
            throw new BuildException("import requires support in ProjectHelper");
        }
        Vector importStack = helper.getImportStack();
        if (importStack.size() == 0) {
            throw new BuildException("import requires support in ProjectHelper");
        }
        if (getLocation() == null || getLocation().getFileName() == null) {
            throw new BuildException("Unable to get location of import task");
        }
        Union resourcesToImport = new Union(getProject(), resources);
        Resource fromFileAttribute = getFileAttributeResource();
        if (fromFileAttribute != null) {
            resources.add(fromFileAttribute);
        }
        for (Iterator i = resourcesToImport.iterator(); i.hasNext(); ) {
            importResource(helper, (Resource) i.next());
        }
    }
    private void importResource(ProjectHelper helper,
                                Resource importedResource) {
        Vector importStack = helper.getImportStack();
        getProject().log("Importing file " + importedResource + " from "
                         + getLocation().getFileName(), Project.MSG_VERBOSE);
        if (!importedResource.isExists()) {
            String message =
                "Cannot find " + importedResource + " imported from "
                + getLocation().getFileName();
            if (optional) {
                getProject().log(message, Project.MSG_VERBOSE);
                return;
            } else {
                throw new BuildException(message);
            }
        }
        File importedFile = null;
        FileProvider fp = (FileProvider) importedResource.as(FileProvider.class);
        if (fp != null) {
            importedFile = fp.getFile();
        }
        if (!isInIncludeMode() &&
            (importStack.contains(importedResource)
             || (importedFile != null && importStack.contains(importedFile))
             )
            ) {
            getProject().log(
                "Skipped already imported file:\n   "
                + importedResource + "\n", Project.MSG_VERBOSE);
            return;
        }
        String oldPrefix = ProjectHelper.getCurrentTargetPrefix();
        boolean oldIncludeMode = ProjectHelper.isInIncludeMode();
        String oldSep = ProjectHelper.getCurrentPrefixSeparator();
        try {
            String prefix = targetPrefix;
            if (isInIncludeMode() && oldPrefix != null
                && targetPrefix != null) {
                prefix = oldPrefix + oldSep + targetPrefix;
            }
            setProjectHelperProps(prefix, prefixSeparator,
                                  isInIncludeMode());
            ProjectHelper subHelper = ProjectHelperRepository.getInstance().getProjectHelperForBuildFile(
                    importedResource);
            subHelper.getImportStack().addAll(helper.getImportStack());
            subHelper.getExtensionStack().addAll(helper.getExtensionStack());
            getProject().addReference(ProjectHelper.PROJECTHELPER_REFERENCE, subHelper);
            subHelper.parse(getProject(), importedResource);
            getProject().addReference(ProjectHelper.PROJECTHELPER_REFERENCE, helper);
            helper.getImportStack().clear();
            helper.getImportStack().addAll(subHelper.getImportStack());
            helper.getExtensionStack().clear();
            helper.getExtensionStack().addAll(subHelper.getExtensionStack());
        } catch (BuildException ex) {
            throw ProjectHelper.addLocationToBuildException(
                ex, getLocation());
        } finally {
            setProjectHelperProps(oldPrefix, oldSep, oldIncludeMode);
        }
    }
    private Resource getFileAttributeResource() {
        if (file != null) {
            File buildFile =
                new File(getLocation().getFileName()).getAbsoluteFile();
            if (buildFile.exists()) {
                File buildFileParent = new File(buildFile.getParent());
                File importedFile =
                    FILE_UTILS.resolveFile(buildFileParent, file);
                return new FileResource(importedFile);
            }
            try {
                URL buildFileURL = new URL(getLocation().getFileName());
                URL importedFile = new URL(buildFileURL, file);
                return new URLResource(importedFile);
            } catch (MalformedURLException ex) {
                log(ex.toString(), Project.MSG_VERBOSE);
            }
            throw new BuildException("failed to resolve " + file
                                     + " relative to "
                                     + getLocation().getFileName());
        }
        return null;
    }
    protected final boolean isInIncludeMode() {
        return "include".equals(getTaskType());
    }
    private static void setProjectHelperProps(String prefix,
                                              String prefixSep,
                                              boolean inIncludeMode) {
        ProjectHelper.setCurrentTargetPrefix(prefix);
        ProjectHelper.setCurrentPrefixSeparator(prefixSep);
        ProjectHelper.setInIncludeMode(inIncludeMode);
    }
}
