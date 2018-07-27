package org.apache.maven.artifact.versioning;
import java.util.StringTokenizer;
public class DefaultArtifactVersion
    implements ArtifactVersion
{
    private Integer majorVersion;
    private Integer minorVersion;
    private Integer incrementalVersion;
    private Integer buildNumber;
    private String qualifier;
    private String unparsed;
    public DefaultArtifactVersion( String version )
    {
        parseVersion( version );
    }
    public int compareTo( Object o )
    {
        ArtifactVersion otherVersion = (ArtifactVersion) o;
        int result = getMajorVersion() - otherVersion.getMajorVersion();
        if ( result == 0 )
        {
            result = getMinorVersion() - otherVersion.getMinorVersion();
        }
        if ( result == 0 )
        {
            result = getIncrementalVersion() - otherVersion.getIncrementalVersion();
        }
        if ( result == 0 )
        {
            if ( qualifier != null )
            {
                String otherQualifier = otherVersion.getQualifier();
                if ( otherQualifier != null )
                {
                    if ( ( qualifier.length() > otherQualifier.length() )
                         && qualifier.startsWith( otherQualifier ) )
                    {
                        result = -1;
                    }
                    else if ( ( qualifier.length() < otherQualifier.length() )
                              && otherQualifier.startsWith( qualifier ) )
                    {
                        result = 1;
                    }
                    else
                    {
                        result = qualifier.compareTo( otherQualifier );
                    }
                }
                else
                {
                    result = -1;
                }
            }
            else if ( otherVersion.getQualifier() != null )
            {
                result = 1;
            }
            else
            {
                result = getBuildNumber() - otherVersion.getBuildNumber();
            }
        }
        return result;
    }
    public int getMajorVersion()
    {
        return majorVersion != null ? majorVersion.intValue() : 0;
    }
    public int getMinorVersion()
    {
        return minorVersion != null ? minorVersion.intValue() : 0;
    }
    public int getIncrementalVersion()
    {
        return incrementalVersion != null ? incrementalVersion.intValue() : 0;
    }
    public int getBuildNumber()
    {
        return buildNumber != null ? buildNumber.intValue() : 0;
    }
    public String getQualifier()
    {
        return qualifier;
    }
    public final void parseVersion( String version )
    {
        unparsed = version;
        int index = version.indexOf( "-" );
        String part1;
        String part2 = null;
        if ( index < 0 )
        {
            part1 = version;
        }
        else
        {
            part1 = version.substring( 0, index );
            part2 = version.substring( index + 1 );
        }
        if ( part2 != null )
        {
            try
            {
                if ( ( part2.length() == 1 ) || !part2.startsWith( "0" ) )
                {
                    buildNumber = Integer.valueOf( part2 );
                }
                else
                {
                    qualifier = part2;
                }
            }
            catch ( NumberFormatException e )
            {
                qualifier = part2;
            }
        }
        if ( ( part1.indexOf( "." ) < 0 ) && !part1.startsWith( "0" ) )
        {
            try
            {
                majorVersion = Integer.valueOf( part1 );
            }
            catch ( NumberFormatException e )
            {
                qualifier = version;
                buildNumber = null;
            }
        }
        else
        {
            boolean fallback = false;
            StringTokenizer tok = new StringTokenizer( part1, "." );
            try
            {
                majorVersion = getNextIntegerToken( tok );
                if ( tok.hasMoreTokens() )
                {
                    minorVersion = getNextIntegerToken( tok );
                }
                if ( tok.hasMoreTokens() )
                {
                    incrementalVersion = getNextIntegerToken( tok );
                }
                if ( tok.hasMoreTokens() )
                {
                    fallback = true;
                }
                if ( part1.indexOf( ".." ) >= 0 || part1.startsWith( "." ) || part1.endsWith( "." ) )
                {
                    fallback = true;
                }
            }
            catch ( NumberFormatException e )
            {
                fallback = true;
            }
            if ( fallback )
            {
                qualifier = version;
                majorVersion = null;
                minorVersion = null;
                incrementalVersion = null;
                buildNumber = null;
            }
        }
    }
    private static Integer getNextIntegerToken( StringTokenizer tok )
    {
        String s = tok.nextToken();
        if ( ( s.length() > 1 ) && s.startsWith( "0" ) )
        {
            throw new NumberFormatException( "Number part has a leading 0: '" + s + "'" );
        }
        return Integer.valueOf( s );
    }
    public String toString()
    {
        return unparsed;
    }
    public boolean equals( Object other )
    {
        if ( this == other )
        {
            return true;
        }
        if ( !( other instanceof ArtifactVersion ) )
        {
            return false;
        }
        return 0 == compareTo( other );
    }
    public int hashCode()
    {
        int result = 1229;
        result = 1223 * result + getMajorVersion();
        result = 1223 * result + getMinorVersion();
        result = 1223 * result + getIncrementalVersion();
        result = 1223 * result + getBuildNumber();
        if ( null != getQualifier() )
        {
            result = 1223 * result + getQualifier().hashCode();
        }
        return result;
    }
}
