package org.apache.maven.artifact.repository;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
public class ArtifactRepositoryPolicy
{
    public static final String UPDATE_POLICY_NEVER = "never";
    public static final String UPDATE_POLICY_ALWAYS = "always";
    public static final String UPDATE_POLICY_DAILY = "daily";
    public static final String UPDATE_POLICY_INTERVAL = "interval";
    public static final String CHECKSUM_POLICY_FAIL = "fail";
    public static final String CHECKSUM_POLICY_WARN = "warn";
    public static final String CHECKSUM_POLICY_IGNORE = "ignore";
    private boolean enabled;
    private String updatePolicy;
    private String checksumPolicy;
    public ArtifactRepositoryPolicy()
    {
        this( true, null, null );
    }
    public ArtifactRepositoryPolicy( ArtifactRepositoryPolicy policy )
    {
        this( policy.isEnabled(), policy.getUpdatePolicy(), policy.getChecksumPolicy() );
    }
    public ArtifactRepositoryPolicy( boolean enabled, String updatePolicy, String checksumPolicy )
    {
        this.enabled = enabled;
        if ( updatePolicy == null )
        {
            updatePolicy = UPDATE_POLICY_DAILY;
        }
        this.updatePolicy = updatePolicy;
        if ( checksumPolicy == null )
        {
            checksumPolicy = CHECKSUM_POLICY_WARN;
        }
        this.checksumPolicy = checksumPolicy;
    }
    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;
    }
    public void setUpdatePolicy( String updatePolicy )
    {
        if ( updatePolicy != null )
        {
            this.updatePolicy = updatePolicy;
        }
    }
    public void setChecksumPolicy( String checksumPolicy )
    {
        if ( checksumPolicy != null )
        {
            this.checksumPolicy = checksumPolicy;
        }
    }
    public boolean isEnabled()
    {
        return enabled;
    }
    public String getUpdatePolicy()
    {
        return updatePolicy;
    }
    public String getChecksumPolicy()
    {
        return checksumPolicy;
    }
    public boolean checkOutOfDate( Date lastModified )
    {
        boolean checkForUpdates = false;
        if ( UPDATE_POLICY_ALWAYS.equals( updatePolicy ) )
        {
            checkForUpdates = true;
        }
        else if ( UPDATE_POLICY_DAILY.equals( updatePolicy ) )
        {
            Calendar cal = Calendar.getInstance();
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );
            if ( cal.getTime().after( lastModified ) )
            {
                checkForUpdates = true;
            }
        }
        else if ( updatePolicy.startsWith( UPDATE_POLICY_INTERVAL ) )
        {
            String s = updatePolicy.substring( UPDATE_POLICY_INTERVAL.length() + 1 );
            int minutes = Integer.valueOf( s );
            Calendar cal = Calendar.getInstance();
            cal.add( Calendar.MINUTE, -minutes );
            if ( cal.getTime().after( lastModified ) )
            {
                checkForUpdates = true;
            }
        }
        return checkForUpdates;
    }
    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 64 );
        buffer.append( "{enabled=" );
        buffer.append( enabled );
        buffer.append( ", checksums=" );
        buffer.append( checksumPolicy );
        buffer.append( ", updates=" );
        buffer.append( updatePolicy );
        buffer.append( "}" );
        return buffer.toString();
    }
    public void merge( ArtifactRepositoryPolicy policy )
    {
        if ( policy != null && policy.isEnabled() )
        {
            setEnabled( true );
            if ( ordinalOfChecksumPolicy( policy.getChecksumPolicy() ) < ordinalOfChecksumPolicy( getChecksumPolicy() ) )
            {
                setChecksumPolicy( policy.getChecksumPolicy() );
            }
            if ( ordinalOfUpdatePolicy( policy.getUpdatePolicy() ) < ordinalOfUpdatePolicy( getUpdatePolicy() ) )
            {
                setUpdatePolicy( policy.getUpdatePolicy() );
            }
        }
    }
    private int ordinalOfChecksumPolicy( String policy )
    {
        if ( ArtifactRepositoryPolicy.CHECKSUM_POLICY_FAIL.equals( policy ) )
        {
            return 2;
        }
        else if ( ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals( policy ) )
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }
    private int ordinalOfUpdatePolicy( String policy )
    {
        if ( ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY.equals( policy ) )
        {
            return 1440;
        }
        else if ( ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS.equals( policy ) )
        {
            return 0;
        }
        else if ( policy != null && policy.startsWith( ArtifactRepositoryPolicy.UPDATE_POLICY_INTERVAL ) )
        {
            String s = policy.substring( UPDATE_POLICY_INTERVAL.length() + 1 );
            return Integer.valueOf( s );
        }
        else
        {
            return Integer.MAX_VALUE;
        }
    }
}
