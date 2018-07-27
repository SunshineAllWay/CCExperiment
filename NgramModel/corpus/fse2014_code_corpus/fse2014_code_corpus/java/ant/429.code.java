package org.apache.tools.ant.taskdefs.optional.extension;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.DeweyDecimal;
public class ExtensionAdapter extends DataType {
    private String extensionName;
    private DeweyDecimal specificationVersion;
    private String specificationVendor;
    private String implementationVendorID;
    private String implementationVendor;
    private DeweyDecimal implementationVersion;
    private String implementationURL;
    public void setExtensionName(final String extensionName) {
        verifyNotAReference();
        this.extensionName = extensionName;
    }
    public void setSpecificationVersion(final String specificationVersion) {
        verifyNotAReference();
        this.specificationVersion = new DeweyDecimal(specificationVersion);
    }
    public void setSpecificationVendor(final String specificationVendor) {
        verifyNotAReference();
        this.specificationVendor = specificationVendor;
    }
    public void setImplementationVendorId(final String implementationVendorID) {
        verifyNotAReference();
        this.implementationVendorID = implementationVendorID;
    }
    public void setImplementationVendor(final String implementationVendor) {
        verifyNotAReference();
        this.implementationVendor = implementationVendor;
    }
    public void setImplementationVersion(final String implementationVersion) {
        verifyNotAReference();
        this.implementationVersion = new DeweyDecimal(implementationVersion);
    }
    public void setImplementationUrl(final String implementationURL) {
        verifyNotAReference();
        this.implementationURL = implementationURL;
    }
    public void setRefid(final Reference reference)
        throws BuildException {
        if (null != extensionName
            || null != specificationVersion
            || null != specificationVendor
            || null != implementationVersion
            || null != implementationVendorID
            || null != implementationVendor
            || null != implementationURL) {
            throw tooManyAttributes();
        }
        super.setRefid(reference);
    }
    private void verifyNotAReference()
        throws BuildException {
        if (isReference()) {
            throw tooManyAttributes();
        }
    }
    Extension toExtension()
        throws BuildException {
        if (isReference()) {
            return ((ExtensionAdapter) getCheckedRef()).toExtension();
        }
        dieOnCircularReference();
        if (null == extensionName) {
            final String message = "Extension is missing name.";
            throw new BuildException(message);
        }
        String specificationVersionString = null;
        if (null != specificationVersion) {
            specificationVersionString = specificationVersion.toString();
        }
        String implementationVersionString = null;
        if (null != implementationVersion) {
            implementationVersionString = implementationVersion.toString();
        }
        return new Extension(extensionName,
                              specificationVersionString,
                              specificationVendor,
                              implementationVersionString,
                              implementationVendor,
                              implementationVendorID,
                              implementationURL);
    }
    public String toString() {
        return "{" + toExtension().toString() + "}";
    }
}
