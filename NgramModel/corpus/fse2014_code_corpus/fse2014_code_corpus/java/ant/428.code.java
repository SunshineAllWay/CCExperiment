package org.apache.tools.ant.taskdefs.optional.extension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.apache.tools.ant.util.DeweyDecimal;
import org.apache.tools.ant.util.StringUtils;
public final class Extension {
    public static final Attributes.Name EXTENSION_LIST
        = new Attributes.Name("Extension-List");
    public static final Attributes.Name OPTIONAL_EXTENSION_LIST
        = new Attributes.Name("Optional-Extension-List");
    public static final Attributes.Name EXTENSION_NAME =
        new Attributes.Name("Extension-Name");
    public static final Attributes.Name SPECIFICATION_VERSION
        = Attributes.Name.SPECIFICATION_VERSION;
    public static final Attributes.Name SPECIFICATION_VENDOR
        = Attributes.Name.SPECIFICATION_VENDOR;
    public static final Attributes.Name IMPLEMENTATION_VERSION
        = Attributes.Name.IMPLEMENTATION_VERSION;
    public static final Attributes.Name IMPLEMENTATION_VENDOR
        = Attributes.Name.IMPLEMENTATION_VENDOR;
    public static final Attributes.Name IMPLEMENTATION_URL
        = new Attributes.Name("Implementation-URL");
    public static final Attributes.Name IMPLEMENTATION_VENDOR_ID
        = new Attributes.Name("Implementation-Vendor-Id");
    public static final Compatibility COMPATIBLE
        = new Compatibility("COMPATIBLE");
    public static final Compatibility REQUIRE_SPECIFICATION_UPGRADE
        = new Compatibility("REQUIRE_SPECIFICATION_UPGRADE");
    public static final Compatibility REQUIRE_VENDOR_SWITCH
        = new Compatibility("REQUIRE_VENDOR_SWITCH");
    public static final Compatibility REQUIRE_IMPLEMENTATION_UPGRADE
        = new Compatibility("REQUIRE_IMPLEMENTATION_UPGRADE");
    public static final Compatibility INCOMPATIBLE
        = new Compatibility("INCOMPATIBLE");
    private String extensionName;
    private DeweyDecimal specificationVersion;
    private String specificationVendor;
    private String implementationVendorID;
    private String implementationVendor;
    private DeweyDecimal implementationVersion;
    private String implementationURL;
    public static Extension[] getAvailable(final Manifest manifest) {
        if (null == manifest) {
            return new Extension[ 0 ];
        }
        final ArrayList results = new ArrayList();
        final Attributes mainAttributes = manifest.getMainAttributes();
        if (null != mainAttributes) {
            final Extension extension = getExtension("", mainAttributes);
            if (null != extension) {
                results.add(extension);
            }
        }
        final Map entries = manifest.getEntries();
        final Iterator keys = entries.keySet().iterator();
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final Attributes attributes = (Attributes) entries.get(key);
            final Extension extension = getExtension("", attributes);
            if (null != extension) {
                results.add(extension);
            }
        }
        return (Extension[]) results.toArray(new Extension[results.size()]);
    }
    public static Extension[] getRequired(final Manifest manifest) {
        return getListed(manifest, Attributes.Name.EXTENSION_LIST);
    }
    public static Extension[] getOptions(final Manifest manifest) {
        return getListed(manifest, OPTIONAL_EXTENSION_LIST);
    }
    public static void addExtension(final Extension extension,
                                     final Attributes attributes) {
        addExtension(extension, "", attributes);
    }
    public static void addExtension(final Extension extension,
                                     final String prefix,
                                     final Attributes attributes) {
        attributes.putValue(prefix + EXTENSION_NAME,
                             extension.getExtensionName());
        final String specificationVendor = extension.getSpecificationVendor();
        if (null != specificationVendor) {
            attributes.putValue(prefix + SPECIFICATION_VENDOR,
                                 specificationVendor);
        }
        final DeweyDecimal specificationVersion
            = extension.getSpecificationVersion();
        if (null != specificationVersion) {
            attributes.putValue(prefix + SPECIFICATION_VERSION,
                                 specificationVersion.toString());
        }
        final String implementationVendorID
            = extension.getImplementationVendorID();
        if (null != implementationVendorID) {
            attributes.putValue(prefix + IMPLEMENTATION_VENDOR_ID,
                                 implementationVendorID);
        }
        final String implementationVendor = extension.getImplementationVendor();
        if (null != implementationVendor) {
            attributes.putValue(prefix + IMPLEMENTATION_VENDOR,
                                 implementationVendor);
        }
        final DeweyDecimal implementationVersion
            = extension.getImplementationVersion();
        if (null != implementationVersion) {
            attributes.putValue(prefix + IMPLEMENTATION_VERSION,
                                 implementationVersion.toString());
        }
        final String implementationURL = extension.getImplementationURL();
        if (null != implementationURL) {
            attributes.putValue(prefix + IMPLEMENTATION_URL,
                                 implementationURL);
        }
    }
    public Extension(final String extensionName,
                      final String specificationVersion,
                      final String specificationVendor,
                      final String implementationVersion,
                      final String implementationVendor,
                      final String implementationVendorId,
                      final String implementationURL) {
        this.extensionName = extensionName;
        this.specificationVendor = specificationVendor;
        if (null != specificationVersion) {
            try {
                this.specificationVersion
                    = new DeweyDecimal(specificationVersion);
            } catch (final NumberFormatException nfe) {
                final String error = "Bad specification version format '"
                    + specificationVersion + "' in '" + extensionName
                    + "'. (Reason: " + nfe + ")";
                throw new IllegalArgumentException(error);
            }
        }
        this.implementationURL = implementationURL;
        this.implementationVendor = implementationVendor;
        this.implementationVendorID = implementationVendorId;
        if (null != implementationVersion) {
            try {
                this.implementationVersion
                    = new DeweyDecimal(implementationVersion);
            } catch (final NumberFormatException nfe) {
                final String error = "Bad implementation version format '"
                    + implementationVersion + "' in '" + extensionName
                    + "'. (Reason: " + nfe + ")";
                throw new IllegalArgumentException(error);
            }
        }
        if (null == this.extensionName) {
            throw new NullPointerException("extensionName property is null");
        }
    }
    public String getExtensionName() {
        return extensionName;
    }
    public String getSpecificationVendor() {
        return specificationVendor;
    }
    public DeweyDecimal getSpecificationVersion() {
        return specificationVersion;
    }
    public String getImplementationURL() {
        return implementationURL;
    }
    public String getImplementationVendor() {
        return implementationVendor;
    }
    public String getImplementationVendorID() {
        return implementationVendorID;
    }
    public DeweyDecimal getImplementationVersion() {
        return implementationVersion;
    }
    public Compatibility getCompatibilityWith(final Extension required) {
        if (!extensionName.equals(required.getExtensionName())) {
            return INCOMPATIBLE;
        }
        final DeweyDecimal requiredSpecificationVersion
            = required.getSpecificationVersion();
        if (null != requiredSpecificationVersion) {
            if (null == specificationVersion
                || !isCompatible(specificationVersion, requiredSpecificationVersion)) {
                return REQUIRE_SPECIFICATION_UPGRADE;
            }
        }
        final String requiredImplementationVendorID
            = required.getImplementationVendorID();
        if (null != requiredImplementationVendorID) {
            if (null == implementationVendorID
                || !implementationVendorID.equals(requiredImplementationVendorID)) {
                return REQUIRE_VENDOR_SWITCH;
            }
        }
        final DeweyDecimal requiredImplementationVersion
            = required.getImplementationVersion();
        if (null != requiredImplementationVersion) {
            if (null == implementationVersion
                || !isCompatible(implementationVersion, requiredImplementationVersion)) {
                return REQUIRE_IMPLEMENTATION_UPGRADE;
            }
        }
        return COMPATIBLE;
    }
    public boolean isCompatibleWith(final Extension required) {
        return (COMPATIBLE == getCompatibilityWith(required));
    }
    public String toString() {
        final String brace = ": ";
        final StringBuffer sb = new StringBuffer(EXTENSION_NAME.toString());
        sb.append(brace);
        sb.append(extensionName);
        sb.append(StringUtils.LINE_SEP);
        if (null != specificationVersion) {
            sb.append(SPECIFICATION_VERSION);
            sb.append(brace);
            sb.append(specificationVersion);
            sb.append(StringUtils.LINE_SEP);
        }
        if (null != specificationVendor) {
            sb.append(SPECIFICATION_VENDOR);
            sb.append(brace);
            sb.append(specificationVendor);
            sb.append(StringUtils.LINE_SEP);
        }
        if (null != implementationVersion) {
            sb.append(IMPLEMENTATION_VERSION);
            sb.append(brace);
            sb.append(implementationVersion);
            sb.append(StringUtils.LINE_SEP);
        }
        if (null != implementationVendorID) {
            sb.append(IMPLEMENTATION_VENDOR_ID);
            sb.append(brace);
            sb.append(implementationVendorID);
            sb.append(StringUtils.LINE_SEP);
        }
        if (null != implementationVendor) {
            sb.append(IMPLEMENTATION_VENDOR);
            sb.append(brace);
            sb.append(implementationVendor);
            sb.append(StringUtils.LINE_SEP);
        }
        if (null != implementationURL) {
            sb.append(IMPLEMENTATION_URL);
            sb.append(brace);
            sb.append(implementationURL);
            sb.append(StringUtils.LINE_SEP);
        }
        return sb.toString();
    }
    private boolean isCompatible(final DeweyDecimal first,
                                 final DeweyDecimal second) {
        return first.isGreaterThanOrEqual(second);
    }
    private static Extension[] getListed(final Manifest manifest,
                                          final Attributes.Name listKey) {
        final ArrayList results = new ArrayList();
        final Attributes mainAttributes = manifest.getMainAttributes();
        if (null != mainAttributes) {
            getExtension(mainAttributes, results, listKey);
        }
        final Map entries = manifest.getEntries();
        final Iterator keys = entries.keySet().iterator();
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final Attributes attributes = (Attributes) entries.get(key);
            getExtension(attributes, results, listKey);
        }
        return (Extension[]) results.toArray(new Extension[results.size()]);
    }
    private static void getExtension(final Attributes attributes,
                                     final ArrayList required,
                                     final Attributes.Name listKey) {
        final String names = attributes.getValue(listKey);
        if (null == names) {
            return;
        }
        final String[] extentions = split(names, " ");
        for (int i = 0; i < extentions.length; i++) {
            final String prefix = extentions[ i ] + "-";
            final Extension extension = getExtension(prefix, attributes);
            if (null != extension) {
                required.add(extension);
            }
        }
    }
    private static String[] split(final String string,
                                        final String onToken) {
        final StringTokenizer tokenizer = new StringTokenizer(string, onToken);
        final String[] result = new String[ tokenizer.countTokens() ];
        for (int i = 0; i < result.length; i++) {
            result[ i ] = tokenizer.nextToken();
        }
        return result;
    }
    private static Extension getExtension(final String prefix,
                                          final Attributes attributes) {
        final String nameKey = prefix + EXTENSION_NAME;
        final String name = getTrimmedString(attributes.getValue(nameKey));
        if (null == name) {
            return null;
        }
        final String specVendorKey = prefix + SPECIFICATION_VENDOR;
        final String specVendor
            = getTrimmedString(attributes.getValue(specVendorKey));
        final String specVersionKey = prefix + SPECIFICATION_VERSION;
        final String specVersion
            = getTrimmedString(attributes.getValue(specVersionKey));
        final String impVersionKey = prefix + IMPLEMENTATION_VERSION;
        final String impVersion
            = getTrimmedString(attributes.getValue(impVersionKey));
        final String impVendorKey = prefix + IMPLEMENTATION_VENDOR;
        final String impVendor
            = getTrimmedString(attributes.getValue(impVendorKey));
        final String impVendorIDKey = prefix + IMPLEMENTATION_VENDOR_ID;
        final String impVendorId
            = getTrimmedString(attributes.getValue(impVendorIDKey));
        final String impURLKey = prefix + IMPLEMENTATION_URL;
        final String impURL = getTrimmedString(attributes.getValue(impURLKey));
        return new Extension(name, specVersion, specVendor, impVersion,
                              impVendor, impVendorId, impURL);
    }
    private static String getTrimmedString(final String value) {
        return null == value ? null : value.trim();
    }
}
