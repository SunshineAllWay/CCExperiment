package org.apache.maven.toolchain;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
public final class RequirementMatcherFactory
{
    private RequirementMatcherFactory()
    {
    }
    public static RequirementMatcher createExactMatcher( String provideValue )
    {
        return new ExactMatcher( provideValue );
    }
    public static RequirementMatcher createVersionMatcher( String provideValue )
    {
        return new VersionMatcher( provideValue );
    }
    private static final class ExactMatcher
        implements RequirementMatcher
    {
        private String provides;
        private ExactMatcher( String provides )
        {
            this.provides = provides;
        }
        public boolean matches( String requirement )
        {
            return provides.equalsIgnoreCase( requirement );
        }
    }
    private static final class VersionMatcher
        implements RequirementMatcher
    {
        DefaultArtifactVersion version;
        private VersionMatcher( String version )
        {
            this.version = new DefaultArtifactVersion( version );
        }
        public boolean matches( String requirement )
        {
            try
            {
                VersionRange range = VersionRange.createFromVersionSpec( requirement );
                if ( range.hasRestrictions() )
                {
                    return range.containsVersion( version );
                }
                else
                {
                    return range.getRecommendedVersion().compareTo( version ) == 0;
                }
            }
            catch ( InvalidVersionSpecificationException ex )
            {
                ex.printStackTrace();
                return false;
            }
        }
    }
}