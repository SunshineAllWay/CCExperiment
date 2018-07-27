package org.apache.tools.ant.taskdefs.optional.extension;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.apache.tools.ant.util.DeweyDecimal;
import org.apache.tools.ant.util.StringUtils;
public final class Specification {
    private static final String MISSING = "Missing ";
    public static final Attributes.Name SPECIFICATION_TITLE
        = Attributes.Name.SPECIFICATION_TITLE;
    public static final Attributes.Name SPECIFICATION_VERSION
        = Attributes.Name.SPECIFICATION_VERSION;
    public static final Attributes.Name SPECIFICATION_VENDOR
        = Attributes.Name.SPECIFICATION_VENDOR;
    public static final Attributes.Name IMPLEMENTATION_TITLE
        = Attributes.Name.IMPLEMENTATION_TITLE;
    public static final Attributes.Name IMPLEMENTATION_VERSION
        = Attributes.Name.IMPLEMENTATION_VERSION;
    public static final Attributes.Name IMPLEMENTATION_VENDOR
        = Attributes.Name.IMPLEMENTATION_VENDOR;
    public static final Compatibility COMPATIBLE =
        new Compatibility("COMPATIBLE");
    public static final Compatibility REQUIRE_SPECIFICATION_UPGRADE =
        new Compatibility("REQUIRE_SPECIFICATION_UPGRADE");
    public static final Compatibility REQUIRE_VENDOR_SWITCH =
        new Compatibility("REQUIRE_VENDOR_SWITCH");
    public static final Compatibility REQUIRE_IMPLEMENTATION_CHANGE =
        new Compatibility("REQUIRE_IMPLEMENTATION_CHANGE");
    public static final Compatibility INCOMPATIBLE =
        new Compatibility("INCOMPATIBLE");
    private String specificationTitle;
    private DeweyDecimal specificationVersion;
    private String specificationVendor;
    private String implementationTitle;
    private String implementationVendor;
    private String implementationVersion;
    private String[] sections;
    public static Specification[] getSpecifications(final Manifest manifest)
        throws ParseException {
        if (null == manifest) {
            return new Specification[ 0 ];
        }
        final ArrayList results = new ArrayList();
        final Map entries = manifest.getEntries();
        final Iterator keys = entries.keySet().iterator();
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final Attributes attributes = (Attributes) entries.get(key);
            final Specification specification
                = getSpecification(key, attributes);
            if (null != specification) {
                results.add(specification);
            }
        }
        final ArrayList trimmedResults = removeDuplicates(results);
        return (Specification[]) trimmedResults.toArray(new Specification[trimmedResults.size()]);
    }
    public Specification(final String specificationTitle,
                          final String specificationVersion,
                          final String specificationVendor,
                          final String implementationTitle,
                          final String implementationVersion,
                          final String implementationVendor) {
        this(specificationTitle, specificationVersion, specificationVendor,
              implementationTitle, implementationVersion, implementationVendor,
              null);
    }
    public Specification(final String specificationTitle,
                          final String specificationVersion,
                          final String specificationVendor,
                          final String implementationTitle,
                          final String implementationVersion,
                          final String implementationVendor,
                          final String[] sections) {
        this.specificationTitle = specificationTitle;
        this.specificationVendor = specificationVendor;
        if (null != specificationVersion) {
            try {
                this.specificationVersion
                    = new DeweyDecimal(specificationVersion);
            } catch (final NumberFormatException nfe) {
                final String error = "Bad specification version format '"
                    + specificationVersion + "' in '" + specificationTitle
                    + "'. (Reason: " + nfe + ")";
                throw new IllegalArgumentException(error);
            }
        }
        this.implementationTitle = implementationTitle;
        this.implementationVendor = implementationVendor;
        this.implementationVersion = implementationVersion;
        if (null == this.specificationTitle) {
            throw new NullPointerException("specificationTitle");
        }
        String[] copy = null;
        if (null != sections) {
            copy = new String[ sections.length ];
            System.arraycopy(sections, 0, copy, 0, sections.length);
        }
        this.sections = copy;
    }
    public String getSpecificationTitle() {
        return specificationTitle;
    }
    public String getSpecificationVendor() {
        return specificationVendor;
    }
    public String getImplementationTitle() {
        return implementationTitle;
    }
    public DeweyDecimal getSpecificationVersion() {
        return specificationVersion;
    }
    public String getImplementationVendor() {
        return implementationVendor;
    }
    public String getImplementationVersion() {
        return implementationVersion;
    }
    public String[] getSections() {
        if (null == sections) {
            return null;
        }
        final String[] newSections = new String[ sections.length ];
        System.arraycopy(sections, 0, newSections, 0, sections.length);
        return newSections;
    }
    public Compatibility getCompatibilityWith(final Specification other) {
        if (!specificationTitle.equals(other.getSpecificationTitle())) {
            return INCOMPATIBLE;
        }
        final DeweyDecimal otherSpecificationVersion
            = other.getSpecificationVersion();
        if (null != specificationVersion) {
            if (null == otherSpecificationVersion
                || !isCompatible(specificationVersion, otherSpecificationVersion)) {
                return REQUIRE_SPECIFICATION_UPGRADE;
            }
        }
        final String otherImplementationVendor
            = other.getImplementationVendor();
        if (null != implementationVendor) {
            if (null == otherImplementationVendor
                || !implementationVendor.equals(otherImplementationVendor)) {
                return REQUIRE_VENDOR_SWITCH;
            }
        }
        final String otherImplementationVersion
            = other.getImplementationVersion();
        if (null != implementationVersion) {
            if (null == otherImplementationVersion
                || !implementationVersion.equals(otherImplementationVersion)) {
                return REQUIRE_IMPLEMENTATION_CHANGE;
            }
        }
        return COMPATIBLE;
    }
    public boolean isCompatibleWith(final Specification other) {
        return (COMPATIBLE == getCompatibilityWith(other));
    }
    public String toString() {
        final String brace = ": ";
        final StringBuffer sb
            = new StringBuffer(SPECIFICATION_TITLE.toString());
        sb.append(brace);
        sb.append(specificationTitle);
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
        if (null != implementationTitle) {
            sb.append(IMPLEMENTATION_TITLE);
            sb.append(brace);
            sb.append(implementationTitle);
            sb.append(StringUtils.LINE_SEP);
        }
        if (null != implementationVersion) {
            sb.append(IMPLEMENTATION_VERSION);
            sb.append(brace);
            sb.append(implementationVersion);
            sb.append(StringUtils.LINE_SEP);
        }
        if (null != implementationVendor) {
            sb.append(IMPLEMENTATION_VENDOR);
            sb.append(brace);
            sb.append(implementationVendor);
            sb.append(StringUtils.LINE_SEP);
        }
        return sb.toString();
    }
    private boolean isCompatible(final DeweyDecimal first,
                                 final DeweyDecimal second) {
        return first.isGreaterThanOrEqual(second);
    }
    private static ArrayList removeDuplicates(final ArrayList list) {
        final ArrayList results = new ArrayList();
        final ArrayList sections = new ArrayList();
        while (list.size() > 0) {
            final Specification specification = (Specification) list.remove(0);
            final Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                final Specification other = (Specification) iterator.next();
                if (isEqual(specification, other)) {
                    final String[] otherSections = other.getSections();
                    if (null != otherSections) {
                        sections.addAll(Arrays.asList(otherSections));
                    }
                    iterator.remove();
                }
            }
            final Specification merged =
                mergeInSections(specification, sections);
            results.add(merged);
            sections.clear();
        }
        return results;
    }
    private static boolean isEqual(final Specification specification,
                                    final Specification other) {
        return
            specification.getSpecificationTitle().equals(other.getSpecificationTitle())
            && specification.getSpecificationVersion().isEqual(other.getSpecificationVersion())
            && specification.getSpecificationVendor().equals(other.getSpecificationVendor())
            && specification.getImplementationTitle().equals(other.getImplementationTitle())
            && specification.getImplementationVersion().equals(other.getImplementationVersion())
            && specification.getImplementationVendor().equals(other.getImplementationVendor());
    }
    private static Specification mergeInSections(final Specification specification,
                                              final ArrayList sectionsToAdd) {
        if (0 == sectionsToAdd.size()) {
            return specification;
        }
        sectionsToAdd.addAll(Arrays.asList(specification.getSections()));
        final String[] sections =
            (String[]) sectionsToAdd.toArray(new String[sectionsToAdd.size()]);
        return new Specification(specification.getSpecificationTitle(),
                specification.getSpecificationVersion().toString(),
                specification.getSpecificationVendor(),
                specification.getImplementationTitle(),
                specification.getImplementationVersion(),
                specification.getImplementationVendor(),
                sections);
    }
    private static String getTrimmedString(final String value) {
        return value == null ? null : value.trim();
    }
    private static Specification getSpecification(final String section,
                                                   final Attributes attributes)
        throws ParseException {
        final String name
            = getTrimmedString(attributes.getValue(SPECIFICATION_TITLE));
        if (null == name) {
            return null;
        }
        final String specVendor
            = getTrimmedString(attributes.getValue(SPECIFICATION_VENDOR));
        if (null == specVendor) {
            throw new ParseException(MISSING + SPECIFICATION_VENDOR, 0);
        }
        final String specVersion
            = getTrimmedString(attributes.getValue(SPECIFICATION_VERSION));
        if (null == specVersion) {
            throw new ParseException(MISSING + SPECIFICATION_VERSION, 0);
        }
        final String impTitle
            = getTrimmedString(attributes.getValue(IMPLEMENTATION_TITLE));
        if (null == impTitle) {
            throw new ParseException(MISSING + IMPLEMENTATION_TITLE, 0);
        }
        final String impVersion
            = getTrimmedString(attributes.getValue(IMPLEMENTATION_VERSION));
        if (null == impVersion) {
            throw new ParseException(MISSING + IMPLEMENTATION_VERSION, 0);
        }
        final String impVendor
            = getTrimmedString(attributes.getValue(IMPLEMENTATION_VENDOR));
        if (null == impVendor) {
            throw new ParseException(MISSING + IMPLEMENTATION_VENDOR, 0);
        }
        return new Specification(name, specVersion, specVendor,
                                  impTitle, impVersion, impVendor,
                                  new String[]{section});
    }
}
